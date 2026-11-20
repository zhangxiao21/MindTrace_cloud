package com.mindtrace.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import com.mindtrace.app.ui.i18n.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindtrace.app.BuildConfig
import com.mindtrace.app.ui.components.AppMark
import com.mindtrace.app.ui.components.InfoBanner
import com.mindtrace.app.ui.components.ScreenHeader
import com.mindtrace.app.ui.components.SectionTitle
import com.mindtrace.app.ui.components.SoftCard

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenSafety: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(title = "关于心迹", onBack = onBack)
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppMark(size = 64)
                Spacer(Modifier.height(12.dp))
                Text("心迹", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "给自己留一条线索",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "版本 ${BuildConfig.VERSION_NAME}（${BuildConfig.VERSION_CODE}）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SoftCard {
                SectionTitle("这是什么")
                Spacer(Modifier.height(6.dp))
                Text(
                    "心迹是一款本地优先的自我记录工具，帮助你记录情绪、睡眠、症状、用药和自评量表，" +
                        "并在复诊时生成一份便于沟通的报告。它不催促、不评分，也不把记录变成任务。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            InfoBanner(
                text = "心迹不是医疗器械，不提供诊断、处方或治疗建议，不能替代医生或心理咨询师。如果你正处于危险中，请立即拨打 120 / 110 或心理援助热线 12356。"
            )
        }
        item {
            SoftCard {
                SectionTitle("数据边界")
                Spacer(Modifier.height(6.dp))
                Bullet("默认完全不联网；只有在云备份页手动上传或下载时才会使用网络。")
                Bullet("上传的始终是加密后的 .mtrbackup，密码不会被保存；连接凭据只存在本机。")
                Bullet("数据库使用 SQLCipher 整体加密，密钥由 Android Keystore 保管。")
                Bullet("无账号、无广告、无统计 SDK、无自动云同步。")
                Bullet("报告、CSV 与备份只在你自己选择的位置生成。")
                Bullet("卸载、清除数据或删除全部数据会销毁密钥，数据无法恢复；请在更换手机或卸载前导出加密备份。")
                Bullet("更新时直接覆盖安装，不要先卸载；卸载会丢失数据。")
                Spacer(Modifier.height(6.dp))
                Text(
                    "权限说明：通知与精确提醒用于本地提醒；生物识别仅用于解锁，指纹/面容数据由系统处理；开机启动用于恢复提醒。网络权限仅用于手动云备份，应用本身没有服务器。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SoftCard {
                SectionTitle("量表与来源")
                Spacer(Modifier.height(6.dp))
                Text(
                    "心迹内置 56 个自评/筛查量表，包括 PHQ-9、GAD-7、PCL-5、WHO-5、K6、PSS-10、AUDIT、UCLA-3、ASRS-18 等，以及宗氏 SAS / SDS、HADS、GDS-15、AIS、ESS、AD8、CAGE、FTND、MSPSS 等国内医院常用量表。新增量表均附原始文献、可核对链接与使用边界，并在 NOTICE.md 中列出来源。部分量表受版权保护，电子版再分发前请确认授权。所有量表仅用于筛查和追踪，不是诊断。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "技术组件：Kotlin、Jetpack Compose、Material 3、AndroidX、SQLCipher for Android（Zetetic）。感谢这些开源与公开工具。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SoftCard {
                SectionTitle("危机资源")
                Spacer(Modifier.height(6.dp))
                Text(
                    "全国心理援助热线：12356（接听时间以当地为准）\n急救：120\n报警：110",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onOpenSafety, contentPadding = PaddingValues(0.dp)) {
                    Text("打开安全计划与紧急支持")
                }
            }
        }
        // 「隐私说明 / 使用条款 / 问题反馈」已统一放在设置页，这里不再重复放一组相同入口。
        // 关于页只保留独有的安全计划入口与版本、来源说明。
        item {
            Text(
                "心迹 · 本地优先 · MIT License\n记录属于你，也只属于你。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Text(
        "· $text",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 3.dp)
    )
}
