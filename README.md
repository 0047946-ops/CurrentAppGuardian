# 🎬 Guardian Video Player - 完整版

一個功能齊全、性能優異的專業級影片播放器，支援 Android 原生應用與 Web 版本。

## 📋 項目概述

Guardian Video Player 是一個三階段開發的高級視頻播放解決方案，提供從基本播放到高級功能的完整體驗。

### 🌟 核心特性

- ✅ **多畫質支持** - Auto/1080p/720p/480p/360p 自適應流媒體
- ✅ **智慧緩衝** - 動態調整最小/最大緩衝時間
- ✅ **手勢控制** - 左滑亮度、右滑音量、雙擊播放
- ✅ **浮窗播放** - Picture-in-Picture 模式
- ✅ **字幕支持** - 10+ 語言完整支援
- ✅ **背景播放** - 關閉屏幕繼續播放
- ✅ **離線下載** - 影片下載本地觀看
- ✅ **觀看歷史** - 自動保存播放記錄

---

## 📱 應用架構

### 項目結構

```
CurrentAppGuardian/
├── app/src/main/
│   ├── kotlin/com/guardianapp/videoplayer/
│   │   ├── MainActivity.kt                    # 主應用程序
│   │   ├── YouTubeActivity.kt
│   │   └── core/
│   │       ├── NetworkMonitor.kt              # 網路監控
│   │       ├── PlaybackStateManager.kt        # 播放狀態管理
│   │       ├── PlaybackErrorHandler.kt        # 錯誤處理
│   │       ├── SmartBufferingManager.kt       # 智慧緩衝
│   │       ├── PreloadManager.kt              # 影片預載
│   │       ├── PictureInPictureManager.kt     # PiP 管理
│   │       ├── SubtitleManager.kt             # 字幕管理
│   │       ├── BackgroundPlaybackService.kt   # 背景播放服務
│   │       ├── DownloadManager.kt             # 下載管理
│   │       └── HistoryManager.kt              # 歷史記錄
│   └── AndroidManifest.xml
├── index.html                                 # Web 版本
└── README.md                                  # 本文件
```

---

## 🚀 開發階段

### 第一階段 (Phase 1) - 核心功能 ✅

**Commit**: `c1c9b61b35e4f978706766636eeb7109455b06f0`

#### 實現功能

1. **畫質選擇器**
   - 支援 5 種畫質選項
   - Auto 自動調適模式
   - 實時切換無縫播放

2. **倍速控制**
   - 0.5×、0.75×、1.0×、1.25×、1.5×、2.0×
   - 實時應用到播放器
   - 自動保存用戶偏好

3. **手勢控制系統**
   - **左側垂直滑動** - 調整亮度 (0.1-1.0)
   - **右側垂直滑動** - 調整音量
   - **雙擊** - 播放/暫停切換

4. **硬體解碼優先**
   - ExoPlayer 配置優化
   - EventLogger 性能監控
   - 最小緩衝 2.5 秒
   - 最大緩衝 30 秒
   - 時間優先於大小的緩衝策略

5. **實時進度更新**
   - 500ms 更新一次 UI
   - 智慧 Seeking 機制
   - 當前時間和總時長顯示

---

### 第二階段 (Phase 2) - 高級功能 ✅

**提交時間**: 2026-08-30 08:08:55 UTC

#### 新增組件

1. **PreloadManager.kt** - 影片預載入
2. **PictureInPictureManager.kt** - 浮窗播放
3. **SubtitleManager.kt** - 字幕管理 (10+ 語言)
4. **BackgroundPlaybackService.kt** - 背景播放
5. **DownloadManager.kt** - 下載管理

---

### 第三階段 (Phase 3) - 用戶體驗 ✅

**提交時間**: 2026-09-01 04:43:30 UTC

#### 新增功能

1. **HistoryManager.kt** - 觀看歷史
2. **自動保存播放位置**
3. **完整的 AndroidManifest.xml**
4. **Web 版本 (index.html)** - 功能展示與對比

---

## 🔧 技術棧

### Android 開發

- `androidx.media3:media3-exoplayer` - 高級播放器引擎
- `androidx.media3:media3-ui` - UI 組件
- `androidx.lifecycle:lifecycle-runtime-ktx` - 生命週期管理
- `androidx.datastore:datastore-preferences` - 數據持久化
- `kotlinx.coroutines` - 協程支持

### Web 版本

- **HTML5** - 語義化標記
- **CSS3** - 現代樣式與動畫
- **Vanilla JavaScript** - 無框架依賴
- **響應式設計** - 移動優先

---

## 📊 性能指標

| 指標 | 數值 |
|------|------|
| 最小緩衝時間 | 2.5 秒 |
| 最大緩衝時間 | 30 秒 |
| 目標緩衝大小 | 20 MB |
| UI 更新頻率 | 500 ms |
| 支援語言 | 10+ |
| 支援畫質 | 5 種 |
| 最快倍速 | 2.0× |

---

## 🎯 使用指南

### Android 應用

#### 基本使用

1. **啟動應用** - 自動加載上次播放的影片
2. **畫質切換** - 點擊「畫質」按鈕選擇所需畫質
3. **倍速調整** - 點擊「倍速」按鈕選擇播放速度
4. **手勢操作**
   - 左側上下滑動 → 調整亮度
   - 右側上下滑動 → 調整音量
   - 屏幕雙擊 → 播放/暫停
5. **浮窗播放** - 點擊「PiP」按鈕進入浮窗模式

#### 高級功能

1. **字幕支持** - 點擊「字幕」按鈕選擇語言
2. **下載影片** - 點擊「下載」按鈕後台下載
3. **觀看歷史** - 點擊「歷史」按鈕查看所有記錄

### Web 版本

訪問 `index.html` 查看：
- 功能卡片展示
- 交互式播放器體驗
- 三階段功能對比表
- 性能指標展示

---

## 🔐 權限說明

| 權限 | 用途 |
|------|------|
| `INTERNET` | 流媒體播放 |
| `ACCESS_NETWORK_STATE` | 網路監控 |
| `MODIFY_AUDIO_SETTINGS` | 音量控制 |
| `WRITE_EXTERNAL_STORAGE` | 影片下載 |
| `READ_EXTERNAL_STORAGE` | 本地影片讀取 |
| `FOREGROUND_SERVICE` | 背景播放 |

---

## 🚀 快速開始

### Clone 項目

```bash
git clone https://github.com/0047946-ops/CurrentAppGuardian.git
cd CurrentAppGuardian
```

### Android 應用構建

```bash
# 使用 Gradle 構建
./gradlew build

# 安裝到設備
./gradlew installDebug
```

### Web 版本

```bash
# 直接在瀏覽器打開
open index.html

# 或使用本地服務器
python -m http.server 8000
# 訪問 http://localhost:8000
```

---

## 📝 提交歷史

### Phase 1
- `c1c9b61` - 基本功能、手勢控制、畫質倍速選擇

### Phase 2
- `7330afd` - PreloadManager
- `9fdb9a1` - HistoryManager
- `8e9d660` - SubtitleManager
- `86f0d67` - BackgroundPlaybackService
- `421929e` - PictureInPictureManager
- `51a6ee1` - DownloadManager
- `aef1d93` - MainActivity 完整集成

### Phase 3
- `43bda0f` - AndroidManifest.xml
- `abe12a8` - index.html Web 版本

---

## 🎉 更新日誌

### v3.0 - 完全版 (2026-09-01)
- ✅ Phase 1: 基本功能完成
- ✅ Phase 2: 高級功能完成
- ✅ Phase 3: 用戶體驗功能完成
- ✅ Web 版本發佈
- ✅ 完整文檔編寫

### v2.0 - 高級版 (2026-08-30)
- PiP、字幕、下載功能
- 背景播放服務
- 預載入系統

### v1.0 - 基礎版 (2026-08-30)
- 基本播放功能
- 手勢控制
- 畫質倍速選擇

---

## 📞 支持與反饋

- **GitHub Issues**: [提交問題](https://github.com/0047946-ops/CurrentAppGuardian/issues)
- **Web 演示**: [查看演示](https://0047946-ops.github.io/CurrentAppGuardian)

---

## 📄 許可證

MIT License

---

**最後更新**: 2026-09-01 UTC  
**版本**: 3.0 完全版  
**狀態**: ✅ 已完成  
**開發者**: Copilot AI Assistant
