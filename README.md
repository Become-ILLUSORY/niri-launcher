# Niri Launcher

> Android 平板桌面 — niri scrollable-tiling × Noctalia v5 shell 美学

一个为 Android 平板设计的桌面启动器，将 [niri](https://github.com/niri-wm/niri) 的横向滚动平铺窗口管理与 [Noctalia](https://github.com/noctalia-dev/noctalia) v5 的视觉风格融合到 Android 触屏交互中。

## ✨ 核心特性

### niri 交互
- **横向滚动平铺列** — 应用按列排列，水平无限滚动
- **焦点列高亮** — 渐变描边 focus ring（Oklab 色彩）
- **弹簧动画** — 所有过渡使用 spring physics，手感丝滑
- **工作区切换** — 垂直滑动切换工作区，每个工作区独立列集

### Noctalia 视觉
- **深色主题** — `#0A0A14` 深空背景 + `#FFF59B` 暖黄主色
- **毛玻璃面板** — 控制中心、通知中心半透明模糊
- **圆角卡片** — 统一 12-20dp 圆角，层叠阴影
- **渐变焦点环** — Oklch 感知均匀色彩渐变

### Shell 功能
- **Top Bar** — 时钟、工作区指示器、系统托盘图标
- **Dock** — 底部常用应用栏，弹簧缩放反馈
- **App Drawer** — 上滑搜索所有应用，网格布局
- **控制中心** — 快捷开关（Wi-Fi、蓝牙、亮度滑条等）
- **通知中心** — 应用通知聚合展示
- **工作区概览** — 缩略图预览所有工作区

## 📐 架构

```
┌─────────────────────────────────────┐
│            TopBar                   │  ← 状态栏 / 时钟 / 托盘
├─────────────────────────────────────┤
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │ Col  │ │ Col  │ │ Col  │  →→→   │  ← 横向滚动平铺列
│  │ ┌──┐ │ │ ┌──┐ │ │ ┌──┐ │        │
│  │ │App│ │ │ │App│ │ │ │App│ │        │
│  │ └──┘ │ │ └──┘ │ │ └──┘ │        │
│  │ ┌──┐ │ │ ┌──┐ │ │      │        │
│  │ │App│ │ │ │App│ │ │      │        │
│  │ └──┘ │ │ └──┘ │ │      │        │
│  └──────┘ └──────┘ └──────┘        │
├─────────────────────────────────────┤
│           Dock  [App][App][App]     │  ← 底部 Dock
└─────────────────────────────────────┘
```

## 🛠 技术栈

| 组件 | 选型 |
|------|------|
| 语言 | Kotlin 2.1 |
| UI | Jetpack Compose + Material3 |
| 最低版本 | Android 8.0 (API 26) |
| 目标版本 | Android 15 (API 35) |
| 动画 | Compose Spring (自定义 stiffness/damping) |
| 构建 | Gradle 8.11 + AGP 8.7 |
| CI | GitHub Actions |

## 🔧 构建

```bash
# Debug
./gradlew assembleDebug

# Release (unsigned)
./gradlew assembleRelease
```

APK 输出在 `app/build/outputs/apk/`

## 📦 安装使用

1. 安装 APK 到 Android 平板
2. 按 Home 键 → 选择 "Niri" → 设置为默认启动器
3. 左右滑动浏览列，点击应用图标启动
4. 上滑打开 App Drawer，右上角打开控制中心

## 🗺 Roadmap

- [ ] 真实窗口平铺（需 root/priv-app，通过 `WindowContainerTransaction`）
- [ ] 通知监听服务（NotificationListenerService）
- [ ] 手势增强（三指滑动、边缘手势）
- [ ] 自定义壁纸支持
- [ ] 锁屏界面
- [ ] 桌面小部件（Widget）嵌入列
- [ ] 屏幕录制 / 截图 UI
- [ ] 圆角窗口规则（根据应用设置不同圆角）

## 📜 License

MIT — 基于 niri 的 GPLv2 精神与 Noctalia 的开放理念。
