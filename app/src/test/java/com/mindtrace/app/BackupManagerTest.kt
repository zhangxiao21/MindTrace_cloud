package com.mindtrace.app

import com.mindtrace.app.data.BackupCloudConfig
import com.mindtrace.app.data.BackupManager
import com.mindtrace.app.data.BackupPayload
import com.mindtrace.app.data.BackupSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.crypto.spec.SecretKeySpec

/**
 * 备份格式兼容性与云凭据加密的纯 JVM 测试。
 * 只覆盖序列化 / 加密信封，不需要 Android Context 或数据库。
 */
class BackupManagerTest {

    private val password = "correct horse battery staple".toCharArray()

    @Test
    fun v2_envelope_round_trips_settings_and_cloud_config() {
        val payload = BackupPayload(
            version = 2,
            exportedAt = 1_700_000_000_000L,
            settings = BackupSettings(themeMode = "dark", themeColor = "ocean", language = "en"),
            cloudBackup = BackupCloudConfig(
                provider = "webdav",
                webdavUrl = "https://dav.example.com/mindtrace.mtrbackup",
                webdavUser = "alice",
                webdavPassword = "wrapped-secret"
            )
        )
        val bytes = BackupManager.encodeEnvelope(payload, password)
        val restored = BackupManager.decodeEnvelope(bytes, password)

        assertEquals(2, restored.version)
        assertEquals("dark", restored.settings!!.themeMode)
        assertEquals("ocean", restored.settings!!.themeColor)
        assertEquals("alice", restored.cloudBackup!!.webdavUser)
        assertEquals("https://dav.example.com/mindtrace.mtrbackup", restored.cloudBackup!!.webdavUrl)
    }

    @Test
    fun legacy_v1_backup_without_new_fields_still_imports() {
        // 模拟 0.4.1 及更早版本：JSON 里没有 settings / cloudBackup 字段，
        // 文件头也是旧版 MTR\x01。
        val legacyJson = """{"version":1,"exportedAt":1690000000000,"checkIns":[],"medications":[]}"""
        val bytes = BackupManager.encodeTextEnvelope(legacyJson, password, formatVersion = 1)
        val restored = BackupManager.decodeEnvelope(bytes, password)

        assertEquals(1, restored.version)
        assertNull("旧备份不应带上设置", restored.settings)
        assertNull("旧备份不应带上云配置", restored.cloudBackup)
        assertTrue(restored.checkIns.isEmpty())
    }

    @Test
    fun wrong_password_is_rejected() {
        val bytes = BackupManager.encodeEnvelope(BackupPayload(version = 2), password)
        val failed = runCatching { BackupManager.decodeEnvelope(bytes, "wrong-password".toCharArray()) }
        assertTrue(failed.isFailure)
    }

    @Test
    fun cloud_credentials_get_an_extra_encryption_layer() {
        val key = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")
        val secret = "gitee-token-123456"
        val wrapped = BackupManager.wrapSecret(secret, key)

        assertFalse("密文不能包含明文", wrapped.contains(secret))
        assertNotEquals(
            "AES-GCM 使用随机 IV，两次包裹结果必须不同",
            wrapped,
            BackupManager.wrapSecret(secret, key)
        )
        assertEquals(secret, BackupManager.unwrapSecret(wrapped, key))
        assertEquals("", BackupManager.unwrapSecret("损坏的凭据", key))
        assertEquals("", BackupManager.wrapSecret("", key))
    }
}
