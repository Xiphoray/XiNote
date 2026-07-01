<div align="center">
  <img src="app/src/main/res/drawable/img_readme_banner_1782358738199.jpg" alt="XiNote Banner" width="100%" />
  
  <h1>XiNote 📝</h1>

  <p>
    一款优雅、现代且极简的高颜值 Markdown 记事本应用。<br>
    An elegant, modern, and minimalist Markdown note-taking app.
  </p>

  <p>
    <a href="https://android.com"><img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square&logo=android" alt="Platform" /></a>
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Language-Kotlin-blue?style=flat-square&logo=kotlin" alt="Language" /></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License" /></a>
  </p>

  <p>
    <a href="#中文版"><b>中文版</b></a> •
    <a href="#english-version"><b>English Version</b></a>
  </p>
</div>

---

<h2 id="中文版">🇨🇳 中文版</h2>

**XiNote** 是一款基于 Jetpack Compose 和 Material Design 3 打造的优雅、现代且极简的高颜值 Markdown 记事本应用。它致力于为用户提供最纯粹、高效的记录与灵感整理体验。

### 📖 目录

- [功能特性](#-功能特性)
- [技术栈](#-技术栈)
- [安装与运行](#-安装与运行)
- [架构设计](#-架构设计)
- [参与贡献](#-参与贡献)
- [开源协议](#-开源协议)

### ✨ 功能特性

#### 🌐 无缝多语言支持
*   **多语言切换**：默认中文，全面支持英语、日语、法语和西班牙语。
*   **全方位同步**：不仅应用内界面、设置选项实时切换，甚至**桌面小组件的文本**也会跟随语言无缝同步。

#### 📝 Markdown 编辑与实时渲染
*   **Markdown 快捷工具栏**：支持一键插入标题、加粗、斜体、行内代码、引用、分割线和代码块，无需记忆繁琐语法。
*   **即时预览**：通过底部的“编辑/预览”选项卡，瞬间切换查看精美的排版效果。

#### 🎨 绚丽个性化配色卡片与瀑布流视图
*   **高颜值配色**：提供薄荷绿、天空蓝、薰衣草紫、蔷薇粉、温暖橙、石板灰等多款经过深思熟虑、温润护眼的浅色与暗色卡片配色。
*   **全局主题分组**：笔记默认按主题智能折叠分组，置顶笔记独立展示。应用会自动记忆每个主题栏的折叠状态，下次打开依旧保持原有习惯。
*   **瀑布流布局**：主页在两列模式下采用优雅的瀑布流布局，无论内容长短都能紧凑排列，最大化空间利用率。

#### 🧩 深度自定义桌面小组件
*   **实时透明度调节**：内置高自由度的小组件设置中心，通过滑块在 `0% - 100%` 范围内精确调节桌面小组件的背景不透明度。
*   **系统主题完美适配**：小组件自动跟随系统的深/浅色主题，并在设置弹窗中提供了直观的可视化预览。

#### 🚀 效率倍增功能
*   **富格式导出与分享**：支持将笔记保存为长图，或者一键导出为 Word (`.doc`) 格式直接分享给他人。
*   **智能未保存提示**：编辑页的保存按钮根据是否有未保存内容智能变色，提供未保存退出保护，再也不用担心灵感丢失。
*   **内置快捷模板**：长按主页“新建”按钮即可调出自定义模板面板，在“设置”中可以无限制地添加、修改您的专属模板并永久保存。
*   **全场景快捷新建**：支持从其他应用直接分享文本到 XiNote 瞬间生成新记事；支持长按桌面图标快速新建笔记；支持将快捷方式加入下拉控制中心 (Quick Settings)。

### 🛠️ 技术栈

*   **语言**: Kotlin
*   **UI 框架**: Jetpack Compose (Material Design 3)
*   **架构模式**: 遵循现代 Android 开发的最佳实践，采用 MVVM (Model-View-ViewModel) 和单向数据流 (UDF)。
*   **本地存储**: Room Database (SQLite)
*   **并发处理**: Kotlin Coroutines & Flows
*   **桌面组件**: Android AppWidget Framework

### 🚀 安装与运行

如果您想在本地构建和运行此项目：

1. 克隆代码库：
   ```bash
   git clone https://github.com/xiphoray/xinote.git
   ```
2. 使用 **Android Studio** 打开该项目。
3. 同步 Gradle 文件。
4. 在模拟器或 Android 实体设备上编译并运行。

### 🏗️ 架构设计

XiNote 遵循严格的 **MVVM 架构**，将 UI 界面与业务逻辑清晰分离。通过 **Kotlin Flows** 实现单向数据流 (UDF)，确保用户界面始终安全、高效地反映 **Room** 数据库中的最新状态。

### 🤝 参与贡献

非常欢迎提交 Issue 和 Pull Request 来共同完善这个项目！

1. Fork 本项目
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到该分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

### 📄 开源协议

本项目基于 MIT 协议开源。详情请参阅 [`LICENSE`](LICENSE) 文件。

---

<h2 id="english-version">🇬🇧 English Version</h2>

**XiNote** is an elegant, modern, and minimalist Markdown note-taking app built with Jetpack Compose and Material Design 3. It is dedicated to providing users with the purest and most efficient experience for recording thoughts and organizing inspiration.

### 📖 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Installation](#-installation)
- [Architecture](#-architecture)
- [Contributing](#-contributing)
- [License](#-license)

### ✨ Features

#### 🌐 Seamless Multilingual Support
*   **Multilingual**: Chinese by default, with complete support for English, Japanese, French, and Spanish.
*   **Full Synchronization**: App screens, editing options, and even the **home screen widget texts** update in real-time when you switch languages!

#### 📝 Markdown Editor & Live Preview
*   **Markdown Shortcut Bar**: Insert headers, bold, italic, inline code, quotes, rules, and code blocks with a single tap.
*   **Instant Toggle**: Effortlessly switch between Edit and Preview tabs to see your formatted notes instantly.

#### 🎨 Vibrant Note Customization & Layouts
*   **Color Themes**: Beautiful eye-safe card palettes (Sage, Sky, Lavender, Rose, Peach, Slate, and Default).
*   **Global Topic Grouping**: Notes are intelligently grouped and collapsed by topics by default. The app remembers the collapsed state of each topic for your next visit.
*   **Staggered Grid Layout**: The home screen utilizes an elegant staggered grid layout in two-column mode, maximizing space utilization for notes of varying lengths.

#### 🧩 Interactive & Adaptive Home Widget
*   **Opacity Customization**: Adjust widget transparency from `0%` to `100%` using a precise slider inside the app.
*   **System Theme Harmony**: Perfectly conforms to system-wide light/dark themes, with an interactive visual preview canvas in the settings dialog.

#### 🚀 Productivity Boosters
*   **Export & Share**: Save notes as long images or export them directly as Word (`.doc`) documents to share with others.
*   **Smart Save States**: The save button intelligently responds to unsaved changes and prevents accidental exits without saving, ensuring no ideas are lost.
*   **Quick Templates**: Long-press the "Add" button on the home screen to bring up custom templates. Add, edit, and save unlimited templates in settings.
*   **Quick Actions**: Share text directly from other apps to XiNote to instantly create new notes. Long-press the home app icon or use Quick Settings tile for instant access.

### 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI Toolkit**: Jetpack Compose (Material Design 3)
*   **Architecture**: MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF)
*   **Database**: Room Database (SQLite)
*   **Concurrency**: Kotlin Coroutines & Flows
*   **Widgets**: Android AppWidget Framework

### 🚀 Installation

To build and run this app locally:

1. Clone the repository:
   ```bash
   git clone https://github.com/xiphoray/xinote.git
   ```
2. Open the project in **Android Studio**.
3. Sync the Gradle files.
4. Build and run the app on an emulator or physical Android device.

### 🏗️ Architecture

XiNote is built utilizing modern Android development practices. It follows the **MVVM architecture**, separating the UI from the business logic. Data flows unidirectionally (UDF) using **Kotlin Flows**, ensuring the UI always reflects the current database state securely and efficiently handled by **Room**.

### 🤝 Contributing

Contributions, issues, and feature requests are welcome! 

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for more information.
