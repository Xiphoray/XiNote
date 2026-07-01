package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    BackHandler {
        viewModel.navigateToSettings()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Localization.getString("about_title", currentLanguage) ?: "关于 & 使用指南",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.FilledTonalIconButton(onClick = { viewModel.navigateToSettings() }, shape = RoundedCornerShape(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = Localization.getString("back", currentLanguage)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero card introducing XiNote
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString("app_title", currentLanguage) ?: "XiNote",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Localization.getString("about_app_desc", currentLanguage) ?: "一款基于 Jetpack Compose 构建的现代极简 Markdown 记事本，提供优雅的瀑布流视图、智能主题分类、快捷丰富的桌面组件，为您打造最纯粹、高效的灵感记录体验。",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            // Usages list
            Text(
                text = Localization.getString("about_features_title", currentLanguage) ?: "💡 核心功能速览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Usage 1: Editor & Preview
            AboutSection(
                title = Localization.getString("about_feature_1_title", currentLanguage) ?: "📝 Markdown 快捷编辑",
                content = Localization.getString("about_feature_1_desc", currentLanguage) ?: "✨ 快捷工具栏：一键插入标题、加粗、引用、代码块，告别繁琐语法。\n✨ 实时预览：点击底部切换按钮，瞬间查看精美的 Markdown 渲染效果。"
            )

            // Usage 2: Theme & Layout
            AboutSection(
                title = Localization.getString("about_feature_2_title", currentLanguage) ?: "🎨 个性配色与瀑布流",
                content = Localization.getString("about_feature_2_desc", currentLanguage) ?: "✨ 护眼卡片：提供多款精心调校的高颜值浅色与暗色笔记配色。\n✨ 智能归类：支持一键智能分配主题，应用自动记忆您的折叠习惯。\n✨ 优雅排版：双列瀑布流布局让长短不同的记事错落有致，美观实用。"
            )

            // Usage 3: Home Widget
            AboutSection(
                title = Localization.getString("about_feature_3_title", currentLanguage) ?: "🧩 桌面小组件",
                content = Localization.getString("about_feature_3_desc", currentLanguage) ?: "✨ 完美百搭：支持 0% - 100% 自由调节背景透明度，自然融入任何桌面壁纸。\n✨ 快捷入口：点击小组件右上角按钮，瞬间直达新建记事页面，灵感不掉线。"
            )

            // Usage 4: Productivity
            AboutSection(
                title = Localization.getString("about_feature_4_title", currentLanguage) ?: "🚀 效率倍增工具",
                content = Localization.getString("about_feature_4_desc", currentLanguage) ?: "✨ 多格式导出：支持一键将笔记生成为长图，或导出为 Word (.doc) 随时分享。\n✨ 自定义模板：长按主页“新建”按钮唤出模板，随时复用您的专属记录格式。\n✨ 全场景新建：支持长按桌面图标、下拉控制中心磁铁，或从外部应用分享文本快捷新建记事。"
            )

            // Usage 5: Backup
            AboutSection(
                title = Localization.getString("about_feature_5_title", currentLanguage) ?: "☁️ WebDAV 云端备份",
                content = Localization.getString("about_feature_5_desc", currentLanguage) ?: "✨ 数据无忧：支持坚果云、Nextcloud 等标准 WebDAV，随时双向备份防丢失。\n✨ 温馨提示：若使用坚果云，请在网页版“安全选项”中生成【应用密码】作为密码填入，切勿直接使用登录密码。"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AboutSection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
