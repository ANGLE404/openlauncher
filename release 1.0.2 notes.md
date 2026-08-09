# Open Launcher 1.0.2

## 本次更新

- 全局切换为精致中文像素点阵字体：BoutiqueBitmap9x9 用于正文、标签和数据，FashionBitmap16 用于大标题。
- 统一软角圆角层级，优化卡片、按钮、弹窗与车机仪表的像素风密度。
- 增加低对比度像素网格背景，保持信息可读，不使用持续发光或高噪声动画。
- 修复浅色模式被旧黑色自定义背景或黑色渐变覆盖的问题。
- 防止重复注册定位监听，页面离开后清理旧定位，避免行程和指南针读取过期数据。
- 保留天气缓存到期清理、跨城刷新与请求取消保护。
- 版本统一为 `1.0.2`（`versionCode=10002`）。

## 字体授权

字体随 APK 和源码分发，授权文件位于 `app/src/main/assets/licenses/`：

- BoutiqueBitmap9x9：SIL Open Font License 1.1
- FashionBitmap16：SIL Open Font License 1.1
- ChillBitmap：SIL Open Font License 1.1 及作者声明

## 构建说明

Release APK 使用项目测试证书签名，适合个人车机安装和验证。升级安装时必须继续使用相同签名证书。
