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
                        text = "关于 & 使用指南",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.FilledTonalIconButton(onClick = { viewModel.navigateToSettings() }, shape = RoundedCornerShape(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
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
                            text = "墨韵记事 (XiNote)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "一款融入中华古典美学设计的现代化 Markdown 记事工具。支持 Android 原生动态取色 (Material You) 方案，配合精心调校的卡片布局、置顶功能以及桌面小组件，为您带来雅致而高效的记录体验。",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }

            // Usages list
            Text(
                text = "💡 核心功能与用法介绍",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Usage 1: Editor & Preview
            AboutSection(
                title = "1. Markdown 快捷编辑 & 实时预览",
                content = "• 在编辑记事时，点击右上角的“眼睛/画笔”切换图标，即可在“编辑模式”与“预览模式”之间无缝切换。\n• 输入框上方集成了 Markdown 快捷工具栏（支持标题、加粗、斜体、行内代码、引用、分割线和代码块），当输入法弹出时，工具栏会自动升至输入法上方，避免遮挡，提升编辑效率。\n• 精心设计的编辑区已适配软键盘自动缩进（IME Padding），长文输入时可以通过滑动自如查看所有内容。"
            )

            // Usage 2: Native Dynamic Theme
            AboutSection(
                title = "2. Android 动态取色方案",
                content = "• 默认开启 Android 原生的动态取色方案（Material You）。软件会自动提取您的系统壁纸色彩作为应用主题色，实现全局视觉的一致性。\n• 中国古风元素不体现在生硬的取色上，而是完美融合在界面留白、卡片双线边框、雅致的衬线字体（Serif）排版以及精心雕琢的布局比例中。"
            )

            // Usage 3: Home Widget
            AboutSection(
                title = "3. 桌面小组件 (App Widget)",
                content = "• 您可以将墨韵记事小组件添加至手机桌面。小组件会实时同步您设置的记事列表，并支持小组件内直接点击“+”添加记事或点击卡片一键直达编辑页面。\n• 在设置页面，您可以自由调节小组件的背景不透明度（0% ~ 100%），轻松搭配各类风格的桌面壁纸。"
            )

            // Usage 4: WebDAV Backup
            AboutSection(
                title = "4. WebDAV 云备份配置指南",
                content = "• 墨韵记事支持坚果云、Nextcloud 等标准 WebDAV 云盘进行双向无损备份，保障数据安全。\n• 坚果云配置步骤：\n  1. 登录坚果云网页版，进入“账户信息” -> “安全选项”。\n  2. 在“第三方应用管理”中添加新应用并生成“应用密码”。\n  3. 将服务器地址（通常为 https://dav.jianguoyun.com/dav/）、账号（注册邮箱）以及刚刚生成的应用密码填入本软件的 WebDAV 配置项中。\n  4. 注意：必须使用生成的【应用密码】，直接输入网页登录密码会导致备份失败！"
            )

            // Usage 5: Share Formats
            AboutSection(
                title = "5. 全类型多格式导出分享",
                content = "• 点击记事编辑页面右上角的“更多设置”入口，您可以一键分享您的笔记：\n  - 【分享为纯文本】：以标准 Markdown 文本形式进行分享。\n  - 【分享为图片】：直接渲染笔记内容，生成一张排版古雅精致、带古风边框的 PNG 图片进行分享。\n  - 【分享为 Word】：生成标准的 DOC 文件进行分享，可在任何办公软件中完美打开编辑。"
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
