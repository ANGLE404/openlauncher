<div align="center">
  <img width="256" height="256" alt="开放启动器图标" src="https://github.com/user-attachments/assets/4c5c4ddb-836d-4c59-8325-76b8c8d78bb3" />
  <h1>开放启动器 Open Launcher</h1>
  <p><strong>面向车载安卓车机的开源、离线优先桌面启动器。</strong></p>

  [![许可证：MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
  [![欢迎贡献](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://makeapullrequest.com)
</div>

---

## 项目简介

开放启动器专为后装安卓车机设计，重点是清晰、稳定、离线可用和可定制。它不依赖持续网络连接，适合安装在不同分辨率、不同硬件方案的车机设备上。

当前版本为 **v1.0.3.2**，由 **ayc404** 完成中文化和精致像素风现代化优化。

## 主要特性

### 模块化组件网格

- 支持拖动排列和调整组件大小
- 可以从组件库添加或移除组件
- 支持时钟、天气、指南针、遥测、速度表、高度计、车况、行程、媒体和音效板

### 车载仪表组件

- **媒体播放器**：显示曲目、专辑封面、进度和播放控制
- **AM/FM 收音机**：兼容 Choiceway/szchoiceway MCU，或映射能暴露 MediaSession 的车机收音机应用
- **速度表**：使用 GPS 提供独立速度显示
- **高度计**：显示实时海拔
- **行程追踪**：使用真实 GPS 记录并持久保存行程距离、驾驶时间、怠速时间和平均速度
- **车机状态**：显示可读取的 CPU、内存和设备/电池温度；硬件不提供数据时明确显示“不可用”，不生成模拟值
- **音效板**：支持预置音效和自定义音频文件
- **动态天气**：在线时更新天气，离线时显示最近一次缓存，并在无缓存时给出明确状态
- **GPS 指南针**：提供航向、经纬度和校准偏移

### 应用库与快捷方式

- 扫描已安装应用，包括系统级车机应用
- 支持 CarPlay、Android Auto 等接收端应用
- 侧边栏快捷方式支持拖动排序、长按编辑和自定义图标
- 侧边栏可放置在左侧、右侧或底部

### 日夜模式

- 始终深色
- 始终浅色
- 跟随系统主题
- 根据日出日落自动切换

### 中文化与现代化优化

- 内置覆盖当前全部中文界面的 BoutiqueBitmap9x9 简体中文点阵字体
- 中文、标签、时间、温度、速度和坐标统一使用清晰的像素点阵字形
- 提供精致 OEM、赛博科技、极简玻璃三种完整视觉风格
- 提供冰蓝科技、赛道橙、警示红、极光绿、紫色霓虹五种快捷主题色
- 支持独立调整强调色、背景、卡片、浮层、边框和主次文字颜色，并可恢复当前风格默认值
- 主要卡片、对话框、按钮和导航表面采用统一圆角层级
- 时间、温度、速度和海拔等主要读数居中显示，辅助状态保持低干扰
- 主题切换支持平滑颜色过渡
- 指南针指针支持平滑旋转和跨越 0/360° 的最短路径动画
- 保持纯横屏界面，并可随系统在左右两个横屏方向之间翻转
- 保持车机使用场景下的高对比度和低干扰动画

## APK 下载

当前测试版 APK：

[在 GitHub Releases 下载最新版 APK](https://github.com/ANGLE404/openlauncher/releases/latest)

该 APK 使用测试签名，适合个人安装和验证。后续升级需要继续使用相同签名。

## 构建项目

环境要求：

- JDK 17
- Android SDK Platform 36.1
- Android Build Tools 36.0.0
- Gradle 9.4.1

构建 release APK：

```bash
gradle.bat :app:assembleRelease
```

未签名产物位于：

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

### 版本管理

版本号统一维护在 `version.properties`：

```properties
VERSION_NAME=1.0.3.2
VERSION_CODE=10032
```

Gradle、APK 清单、设置页和首次启动页都会读取这一版本源。发布新版本时同步递增 `VERSION_NAME` 和 `VERSION_CODE`，并使用 `v<版本号>-ayc404` 创建 Git 标签。

## 后续计划

- 可分享、可导入的完整主题包
- 更多车机硬件和 MCU 适配
- OBD-II、胎压、电压等车辆数据接入
- 更完善的驾驶模式和停车模式
- 更多中文字体和无障碍显示选项

## 参与贡献

欢迎提交 Issue、功能建议和 Pull Request。建议先在 Issues 中确认是否已有相同问题或正在进行中的改动。

## 许可证

本项目使用 MIT 许可证。

## 致谢

感谢原作者 David Lam 提供 Open Launcher 项目基础，感谢所有参与测试和反馈的车机用户。
