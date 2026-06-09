# MCDevManagerMP

一款面向网易 MC 开发者平台的多平台管理工具，帮助开发者随时查看收益数据、分析模组表现、管理社区互动。

## 功能

### 📊 首页概览

- 当月/上月收益速算，一键查看收益明细
- 多级排行榜浏览
- 收益总览卡片

### 📈 数据分析

- **实时收益** — 查看今日各资源的实时收益数据
- **模组分析** — 查看模组的购买、日活、粉丝等趋势分析图表

### 💬 互动管理

- 社区评论管理与互动

### ⚙️ 设置

- 账号管理与切换
- 主题个性化（种子色 / 动态取色）
- 版本更新检查

## 支持平台

| 平台                                | 状态     |
|-----------------------------------|--------|
| Android                           | ✅ 已支持  |
| Desktop (Windows / macOS / Linux) | ✅ 已支持  |
| iOS                               | 🔧 开发中 |

## 下载安装

前往 [Releases](https://github.com/BitterLemonn/McDevManagerMP/releases) 页面下载最新版本。

- **Android** — 下载 `.apk` 文件安装
- **Desktop** — 下载对应平台的安装包（Windows `.msi` / macOS `.dmg` / Linux `.deb`）

## 开发

如需自行构建，请确保环境已安装：

- JDK 17+
- Android SDK
- Xcode（仅 iOS 构建）

构建命令：

```bash
# Android
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run

# iOS（需在 Xcode 中打开 iosApp 目录运行）
```

## 开源协议

MIT License
