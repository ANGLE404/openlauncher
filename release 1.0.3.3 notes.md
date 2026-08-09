# 开放启动器 v1.0.3.3

## 媒体信息兼容性修复

- 保留 `dw2lam/openlauncher` 原始媒体播放器、曲目信息、专辑封面和播放控制实现。
- 恢复 `MEDIA_CONTENT_CONTROL` 声明，兼容部分车机 ROM 的媒体会话访问检查。
- 增加活动 MediaSession 变化监听，播放器在启动或切换后不再依赖通知回调才能被发现。
- 服务断开或销毁时主动清理会话监听和播放状态，避免显示过期曲目。

## 版本信息

- 版本名称：`1.0.3.3`
- 版本号：`10033`
- 构建者：`ayc404`
- APK：`openlauncher-zh-ayc404-v1.0.3.3.apk`

## 验证

- `regressionCheck` 通过
- `test` 通过（无 JVM 单元测试源）
- `lintRelease` 通过
- APK 签名 v1/v2/v3 验证通过
