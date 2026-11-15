package com.mindtrace.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindtrace.app.ui.i18n.LocalAppLanguage
import com.mindtrace.app.ui.i18n.Text
import com.mindtrace.app.ui.theme.ThemeColorOptions
import com.mindtrace.app.ui.theme.ThemeModeOptions

/**
 * 统一的主题切换面板：模式（浅色 / 深色 / 跟随系统）+ 5 套主题色 + 一键深浅切换。
 *
 * 这是主题设置的唯一编辑界面；「我的」页顶栏的图标只做一键深浅切换，
 * 不重复打开第二个设置入口。
 */
@Composable
fun ThemePickerDialog(
    mode: String,
    color: String,
    systemDark: Boolean,
    onModeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val english = LocalAppLanguage.current.isEnglish
    val effectiveDark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }
    val selectedColor = ThemeColorOptions.firstOrNull { it.code == color }
        ?: ThemeColorOptions.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("外观与主题") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("主题模式", style = MaterialTheme.typography.titleSmall)
                SegmentedOptions(
                    options = ThemeModeOptions.map {
                        it.code to if (english) it.labelEn else it.label
                    },
                    selected = mode,
                    onSelect = onModeChange
                )

                Text("主题色", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ThemeColorOptions.forEach { option ->
                        val selected = option.code == color
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onColorChange(option.code) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(option.swatch)
                                    .border(
                                        width = 2.dp,
                                        color = if (selected) MaterialTheme.colorScheme.onSurface
                                        else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (english) option.labelEn else option.label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 即时预览：用当前选中的主色展示一张小卡片，避免"选完不知道长什么样"。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            selectedColor.swatch.copy(alpha = 0.16f),
                            MaterialTheme.shapes.medium
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(selectedColor.swatch)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("今天的心情", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "心迹 · 主题预览",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = { onModeChange(if (effectiveDark) "light" else "dark") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (effectiveDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("一键切换深色 / 浅色")
                }

                Text(
                    "主题切换立即生效，不需要重启。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        }
    )
}
