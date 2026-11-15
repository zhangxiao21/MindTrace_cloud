package com.mindtrace.app.data

import androidx.room.withTransaction
import com.mindtrace.app.ui.i18n.translate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 备份中保存的非敏感界面设置。
 * PIN、数据库密钥、Keystore 密钥与云备份密码永远不进入备份。
 */
@Serializable
data class BackupSettings(
    val language: String = "system",
    val themeMode: String = "system",
    val themeColor: String = "sage",
    val crisisCountry: String = "CN",
    val medicationOrder: String = "",
    val defaultReportDays: Int = 30,
    val stealthNotifications: Boolean = true
)

/**
 * 备份中的云备份配置。
 *
 * 非敏感字段明文放在（已经整体加密的）payload 里；`webdavPassword` 与 `gitToken`
 * 再单独用「备份密码派生出的密钥」做一层 AES-GCM 包裹，避免凭据以明文形式出现在
 * 解密后的 JSON 中。恢复时先解密这层，再交给 CloudBackupStore 用本机 Keystore 重新加密保存。
 */
@Serializable
data class BackupCloudConfig(
    val provider: String = "webdav",
    val webdavUrl: String = "",
    val webdavUser: String = "",
    val webdavPassword: String = "",
    val gitOwner: String = "",
    val gitRepo: String = "",
    val gitBranch: String = "main",
    val gitPath: String = CloudConfig.DEFAULT_PATH,
    val gitToken: String = ""
)

@Serializable
data class BackupPayload(
    val version: Int = 2,
    val exportedAt: Long = 0,
    val profile: ProfileEntity? = null,
    val checkIns: List<CheckInEntity> = emptyList(),
    val symptoms: List<SymptomEntity> = emptyList(),
    val medications: List<MedicationEntity> = emptyList(),
    val medicationLogs: List<MedicationLogEntity> = emptyList(),
    val scales: List<ScaleAssessmentEntity> = emptyList(),
    val journals: List<JournalEntryEntity> = emptyList(),
    val safetyPlan: SafetyPlanEntity? = null,
    val contacts: List<CrisisContactEntity> = emptyList(),
    val diagnoses: List<DiagnosisEntity> = emptyList(),
    /** 0.4.2 起新增；旧版备份没有这两个字段，缺省为 null，不会覆盖本机现有配置。 */
    val settings: BackupSettings? = null,
    val cloudBackup: BackupCloudConfig? = null
)

object BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // 前 3 个字节是 MTR，第 4 个字节是容器格式版本。
    // v1：0.3.0–0.4.1 的旧备份；v2：0.4.2 起，payload 里增加设置与云备份配置。
    // 两种文件头都允许导入，旧备份的缺失字段由 kotlinx.serialization 默认值补齐。
    private val MAGIC_PREFIX = byteArrayOf(0x4D, 0x54, 0x52) // "MTR"
    private val MAGIC_V1 = byteArrayOf(0x4D, 0x54, 0x52, 0x01)
    private val MAGIC_V2 = byteArrayOf(0x4D, 0x54, 0x52, 0x02)
    private const val ITERATIONS = 200_000
    private const val MAX_BACKUP_BYTES = 64 * 1024 * 1024

    /** 收集数据；云凭据用备份密码派生出的密钥单独包裹后再写入 payload。 */
    suspend fun collect(container: AppContainer, secretKey: SecretKeySpec? = null): BackupPayload {
        val ui = container.settings.snapshot()
        val cloud = container.cloudBackupStore.load()
        val hasCloudConfig = cloud.webdavUrl.isNotBlank() || cloud.webdavUser.isNotBlank() ||
            cloud.webdavPassword.isNotBlank() || cloud.gitOwner.isNotBlank() ||
            cloud.gitRepo.isNotBlank() || cloud.gitToken.isNotBlank() ||
            cloud.gitPath != CloudConfig.DEFAULT_PATH || cloud.provider != CloudProvider.WEBDAV
        val cloudPayload = if (hasCloudConfig && secretKey != null) {
            BackupCloudConfig(
                provider = cloud.provider.key,
                webdavUrl = cloud.webdavUrl,
                webdavUser = cloud.webdavUser,
                webdavPassword = wrapSecret(cloud.webdavPassword, secretKey),
                gitOwner = cloud.gitOwner,
                gitRepo = cloud.gitRepo,
                gitBranch = cloud.gitBranch,
                gitPath = cloud.gitPath,
                gitToken = wrapSecret(cloud.gitToken, secretKey)
            )
        } else null

        return BackupPayload(
            version = 2,
            exportedAt = System.currentTimeMillis(),
            profile = container.profileRepository.get(),
            checkIns = container.checkInRepository.getAll(),
            symptoms = container.symptomRepository.getAll(),
            medications = container.medicationRepository.getAll(),
            medicationLogs = container.medicationRepository.getAllLogs(),
            scales = container.scaleRepository.getAll(),
            journals = container.journalRepository.getAll(),
            safetyPlan = container.safetyRepository.getPlan(),
            contacts = container.safetyRepository.getContacts(),
            diagnoses = container.diagnosisRepository.getAll(),
            settings = BackupSettings(
                language = ui.language,
                themeMode = ui.themeMode,
                themeColor = ui.themeColor,
                crisisCountry = ui.crisisCountry,
                medicationOrder = ui.medicationOrder,
                defaultReportDays = ui.defaultReportDays,
                stealthNotifications = ui.stealthNotifications
            ),
            cloudBackup = cloudPayload
        )
    }

    suspend fun writeEncrypted(
        container: AppContainer,
        output: OutputStream,
        password: CharArray
    ) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val fileIv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)

        val payload = collect(container, key)
        val text = json.encodeToString(BackupPayload.serializer(), payload)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, fileIv))
        val cipherText = cipher.doFinal(text.toByteArray(Charsets.UTF_8))

        DataOutputStream(output).use { out ->
            out.write(MAGIC_V2)
            out.writeInt(salt.size)
            out.write(salt)
            out.writeInt(fileIv.size)
            out.write(fileIv)
            out.writeInt(cipherText.size)
            out.write(cipherText)
            out.flush()
        }
    }

    suspend fun restoreEncrypted(
        container: AppContainer,
        input: InputStream,
        password: CharArray
    ): Result<Unit> = runCatching {
        val (key, plain) = decrypt(input, password)
        val payload = json.decodeFromString(BackupPayload.serializer(), plain.toString(Charsets.UTF_8))
        require(payload.version in 1..2) { "备份版本不受支持" }
        restore(container, payload, key)
    }

    private suspend fun restore(
        container: AppContainer,
        payload: BackupPayload,
        secretKey: SecretKeySpec
    ) {
        val db = container.database
        db.withTransaction {
            db.profileDao().clear()
            db.checkInDao().clear()
            db.symptomDao().clear()
            db.medicationLogDao().clear()
            db.medicationDao().clear()
            db.scaleDao().clear()
            db.journalDao().clear()
            db.safetyDao().clearContacts()
            db.safetyDao().clearPlan()
            db.diagnosisDao().clear()

            db.profileDao().upsert(payload.profile ?: ProfileEntity())
            payload.checkIns.forEach { db.checkInDao().upsert(it) }
            if (payload.symptoms.isEmpty()) {
                db.symptomDao().upsertAll(
                    SymptomCatalog.seedEntities(SymptomCatalog.suggestedFor(listOf("unsure")))
                )
            } else {
                db.symptomDao().upsertAll(payload.symptoms)
            }
            payload.medications.forEach { db.medicationDao().upsert(it) }
            payload.medicationLogs.forEach { db.medicationLogDao().upsert(it) }
            payload.scales.forEach { db.scaleDao().insert(it) }
            payload.journals.forEach { db.journalDao().upsert(it) }
            payload.safetyPlan?.let { db.safetyDao().upsertPlan(it) }
            payload.contacts.forEach { db.safetyDao().upsertContact(it) }
            payload.diagnoses.forEach { db.diagnosisDao().upsert(it) }
        }

        // 旧版备份（version 1）没有这两个字段：保持本机现有的设置与云配置不变。
        payload.settings?.let { container.settings.applyRestored(it) }
        payload.cloudBackup?.let { saved ->
            container.cloudBackupStore.save(
                CloudConfig(
                    provider = CloudProvider.fromKey(saved.provider),
                    webdavUrl = saved.webdavUrl,
                    webdavUser = saved.webdavUser,
                    webdavPassword = unwrapSecret(saved.webdavPassword, secretKey),
                    gitOwner = saved.gitOwner,
                    gitRepo = saved.gitRepo,
                    gitBranch = saved.gitBranch,
                    gitPath = saved.gitPath,
                    gitToken = unwrapSecret(saved.gitToken, secretKey)
                )
            )
        }
    }

    /** 供单元测试使用：序列化并加密一份 payload，返回可写入文件的字节。 */
    fun encodeEnvelope(
        payload: BackupPayload,
        password: CharArray,
        formatVersion: Int = 2
    ): ByteArray = encodeTextEnvelope(
        json.encodeToString(BackupPayload.serializer(), payload),
        password,
        formatVersion
    )

    /** 供单元测试使用：加密任意 JSON 文本，可模拟旧版备份缺少新增字段的情形。 */
    fun encodeTextEnvelope(
        text: String,
        password: CharArray,
        formatVersion: Int = 2
    ): ByteArray {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        val buffer = java.io.ByteArrayOutputStream()
        DataOutputStream(buffer).use { out ->
            out.write(if (formatVersion == 1) MAGIC_V1 else MAGIC_V2)
            out.writeInt(salt.size)
            out.write(salt)
            out.writeInt(iv.size)
            out.write(iv)
            out.writeInt(cipherText.size)
            out.write(cipherText)
        }
        return buffer.toByteArray()
    }

    /** 供单元测试使用：解密并反序列化，验证 v1 / v2 兼容性。 */
    fun decodeEnvelope(bytes: ByteArray, password: CharArray): BackupPayload {
        val (_, plain) = decrypt(java.io.ByteArrayInputStream(bytes), password)
        val payload = json.decodeFromString(BackupPayload.serializer(), plain.toString(Charsets.UTF_8))
        require(payload.version in 1..2) { "备份版本不受支持" }
        return payload
    }

    suspend fun writeCsv(container: AppContainer, output: OutputStream, language: AppLanguage) {
        val entries = container.checkInRepository.getAll()
        val symptomNames = container.symptomRepository.getAll().associate { it.id to it.name }
        val english = language.isEnglish
        OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
            writer.write('﻿'.code)
            if (english) {
                writer.write(
                    "Date,Mood (1-5),Anxiety (0-10),Irritability (0-10),Energy (0-10)," +
                        "Sleep hours,Sleep quality (0-10),Appetite (0-10),Concentration (0-10)," +
                        "Symptoms,Triggers,Self-harm thoughts,Medication,Side effects,Notes\r\n"
                )
            } else {
                writer.write(
                    "日期,情绪(1-5),焦虑(0-10),易怒(0-10),精力(0-10),睡眠小时,睡眠质量(0-10)," +
                        "食欲(0-10),注意力(0-10),症状,诱因,伤害自己的念头,服药情况,副作用,备注\r\n"
                )
            }
            entries.forEach { e ->
                val severity = parseSymptomSeverity(e.symptomSeverity)
                val symptoms = e.symptoms.split(',').filter { it.isNotBlank() }
                    .joinToString(if (english) "; " else "、") { id ->
                        val name = translate(symptomNames[id] ?: id, language)
                        severity[id]?.let { name + " ($it/10)" } ?: name
                    }
                val ideation = e.suicidalIdeation?.let { value ->
                    val label = SafetyCatalog.levels.firstOrNull { it.value == value }?.label
                        ?: value.toString()
                    translate(label, language)
                } ?: ""
                val triggers = e.triggers.split(',').filter { it.isNotBlank() }
                    .joinToString(if (english) "; " else "、") { translate(it, language) }
                val row = listOf(
                    e.date,
                    e.mood?.toString() ?: "",
                    e.anxiety?.toString() ?: "",
                    e.irritability?.toString() ?: "",
                    e.energy?.toString() ?: "",
                    e.sleepHours?.toString() ?: "",
                    e.sleepQuality?.toString() ?: "",
                    e.appetite?.toString() ?: "",
                    e.concentration?.toString() ?: "",
                    symptoms,
                    triggers,
                    ideation,
                    e.medicationNote,
                    e.sideEffects,
                    e.notes
                )
                writer.write(row.joinToString(",") { csvCell(it) })
                writer.write("\r\n")
            }
        }
    }

    private fun csvCell(value: String): String {
        if (value.isEmpty()) return ""
        val needsQuote = value.contains(',') || value.contains('"') ||
            value.contains('\n') || value.contains('\r')
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }

    /** 云凭据的第二层保护：与备份文件同一个派生密钥、独立随机 IV 的 AES-GCM。 */
    fun wrapSecret(plain: String, key: SecretKeySpec): String {
        if (plain.isBlank()) return ""
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv) + ":" +
            Base64.getEncoder().encodeToString(cipherText)
    }

    fun unwrapSecret(stored: String, key: SecretKeySpec): String {
        if (stored.isBlank()) return ""
        return runCatching {
            val parts = stored.split(':', limit = 2)
            require(parts.size == 2) { "凭据损坏" }
            val iv = Base64.getDecoder().decode(parts[0])
            val cipherText = Base64.getDecoder().decode(parts[1])
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        }.getOrDefault("")
    }

    private fun decrypt(input: InputStream, password: CharArray): Pair<SecretKeySpec, ByteArray> {
        DataInputStream(input).use { stream ->
            val magic = ByteArray(4)
            stream.readFully(magic)
            require(magic.take(3).toByteArray().contentEquals(MAGIC_PREFIX)) {
                "不是有效的心迹备份文件"
            }
            require(magic[3] == MAGIC_V1[3] || magic[3] == MAGIC_V2[3]) {
                "备份文件版本不受支持"
            }
            val saltLength = stream.readInt()
            require(saltLength in 8..64) { "备份文件损坏" }
            val salt = ByteArray(saltLength).also { stream.readFully(it) }
            val ivLength = stream.readInt()
            require(ivLength in 8..32) { "备份文件损坏" }
            val iv = ByteArray(ivLength).also { stream.readFully(it) }
            val cipherLength = stream.readInt()
            require(cipherLength in 1..MAX_BACKUP_BYTES) { "备份文件过大或已损坏" }
            val cipherText = ByteArray(cipherLength).also { stream.readFully(it) }

            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            return key to cipher.doFinal(cipherText)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, 256)
        return try {
            val encoded = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
            SecretKeySpec(encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
