$versionProperties = @{}
Get-Content "$PSScriptRoot\version.properties" | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)$') {
        $versionProperties[$matches[1].Trim()] = $matches[2].Trim()
    }
}
$releaseVersionName = $versionProperties['VERSION_NAME']
if ([string]::IsNullOrWhiteSpace($releaseVersionName)) {
    throw 'version.properties 中缺少 VERSION_NAME'
}

$pairs = @'
OPEN LAUNCHER	开放启动器
Designed for the dashboard	为车载仪表盘而设计
Introduction	介绍
Location Services	定位服务
Media Integration	媒体集成
Ready to Go	准备出发
WELCOME TO OPEN LAUNCHER	欢迎使用开放启动器
A clean, modern landscape dashboard designed to be the ultimate companion for your car's screen.	简洁现代的横屏仪表盘，为你的车机屏幕打造。
100% Offline-Based	完全离线运行
No reliance on a mobile signal or network connection to function. Speedometer, compass telemetry, and altimeter operate entirely offline.	无需移动信号或网络连接。速度、指南针和高度计均可离线工作。
Highly Customizable Dashboard	高度可定制的仪表盘
Tailor color accents, background gradients, typography fonts, system units, and drag-and-drop to rearrange your tiles.	自定义强调色、背景渐变、字体、单位，并拖动排列卡片。
Soundboard & Media Shortcuts	音效板与媒体快捷方式
Trigger custom soundboard sound effects, manage CarPlay & Android Auto shortcuts, and control active media players.	触发自定义音效，管理 CarPlay、Android Auto 快捷方式并控制媒体播放器。
TELEMETRY & WEATHER	遥测与天气
To compute your real-time speed, compass bearing, altitude telemetry, and update local weather conditions, Open Launcher requires high-precision GPS services.	为计算实时速度、航向和高度并更新天气，开放启动器需要高精度 GPS。
Permission Granted	权限已授予
Permission Required	需要权限
GPS telemetry is active and ready.	GPS 遥测已启用
Telemetry is currently disabled.	遥测当前已禁用
GRANT ACCESS	授予权限
MEDIA INTEGRATION	媒体集成
To capture live album art, track info, progress bars, and provide playback control from your dashboard cards, Open Launcher listens to active media notifications.	为显示专辑封面、曲目信息、进度并提供播放控制，开放启动器会监听媒体通知。
Notification Access Granted	通知访问已授予
Notification Access Required	需要通知访问
Music player widget is connected.	音乐播放器组件已连接
Now Playing dashboard will remain inactive.	正在播放组件将保持未激活
ENABLE MEDIA LISTENER	启用媒体监听
READY FOR THE ROAD!	准备上路！
You are all set up and ready to go. You can set Open Launcher as your default home app so it launches automatically whenever you start your vehicle.	设置完成。可将开放启动器设为默认主页，车辆启动时自动打开。
SET AS DEFAULT	设为默认
BACK	返回
GET STARTED	开始使用
CONTINUE	继续
SKIP FOR NOW	暂时跳过
FINISH SETUP	完成设置
SETTINGS	设置
Permissions	权限
Set as Default Launcher	设为默认启动器
Active — Open Launcher is the home app	已启用，开放启动器是主页应用
Required so the head unit boots into Open Launcher	车机启动时进入开放启动器所需
Notification Access	通知访问
Granted — media controls active	已授予，媒体控制已启用
Required for Now Playing widget	正在播放组件所需
Draw Over Other Apps	显示在其他应用上层
Granted — PIP overlay enabled	已授予，画中画浮窗已启用
Required for PIP floating window	画中画浮窗所需
Location Access	定位访问
Granted — GPS, compass & weather active	已授予，GPS、指南针和天气已启用
Required for compass, speed & weather	指南针、速度和天气所需
Vehicle	车辆
Vehicle Name	车辆名称
MY CAR	我的爱车
Sidebar Position	侧边栏位置
Left side	左侧
Right side	右侧
Bottom	底部
Shortcuts Side	快捷方式位置
Right — nav buttons on left	右侧，导航按钮在左
Left — nav buttons on right	左侧，导航按钮在右
Unit System	单位系统
Metric (°C, km)	公制（°C、公里）
Imperial (°F, mi)	英制（°F、英里）
Metric	公制
Imperial	英制
Sidebar	侧边栏
Empty	空
Add Slot	添加插槽
Append an empty shortcut to the sidebar	向侧边栏添加空快捷方式
Appearance	外观
Display Mode	显示模式
Always dark	始终深色
Always light	始终浅色
Follows system theme	跟随系统主题
Dark	深色
Light	浅色
Sunset	日落
Accent Color	强调色
UI highlight color	界面高亮颜色
Background	背景
Gradient	渐变
Solid color	纯色
Background Color	背景颜色
Gradient End Color	渐变结束颜色
Font Color	字体颜色
Custom text color in dark mode	深色模式下的自定义文字颜色
Use Gradient	使用渐变
Blend two colors as background	混合两种颜色作为背景
Gradient Direction	渐变方向
Top to Bottom	从上到下
Left to Right	从左到右
Vertical	垂直
Horizontal	水平
Diagonal	对角
Radial	径向
Set Wallpaper	设置壁纸
Custom wallpaper active	自定义壁纸已启用
Choose image from gallery	从图库选择图片
Wallpaper Dim	壁纸暗度
REMOVE WALLPAPER	移除壁纸
Typography	字体
Font	字体
Bold Font	粗体
Heavier weight across all text	让所有文字更粗
Text Scale	文字缩放
UI Scale	界面缩放
GPS & Calibration	GPS 与校准
Reset A-GPS Assistance Data	重置 A-GPS 辅助数据
Forces cold start to download fresh satellite orbits entirely offline	强制冷启动并离线下载最新卫星轨道
Magnetometer Sweep (Parking Lot)	磁力计校准（停车场）
Guided sweep — Android self-calibrates the compass while you circle	引导校准，绕圈行驶时 Android 会自动校准指南针
Compass Heading Offset	指南针航向偏移
Updates	更新
Check for Updates	检查更新
View releases on GitHub	在 GitHub 查看版本发布
Maintenance	维护
Reset to Defaults	恢复默认设置
Reset Settings	重置设置
Are you sure you want to reset all settings to default? This cannot be undone.	确定要将所有设置恢复默认吗？此操作无法撤销。
Reset	重置
Home	主页
APPS	应用
Settings	设置
Edit widgets	编辑组件
WIDGET LIBRARY	组件库
ADD	添加
ACTIVE	已启用
FULL	已满
APPLY	应用
CANCEL	取消
RESIZE	调整大小
CHOOSE APP	选择应用
No apps found	未找到应用
CHOOSE ICON	选择图标
NATIVE APP ICON	应用原图标
CUSTOMIZE ICON	自定义图标
CHANGE APP	更换应用
Add shortcut	添加快捷方式
Radio	收音机
Camera	相机
Music	音乐
Phone	电话
Any Player	任意播放器
FM/AM Radio	FM/AM 收音机
NO RADIO SOURCE	无收音机来源
ASSIGN RADIO APP	指定收音机应用
RADIO OFF	收音机关闭
NO MEDIA PLAYING	当前无媒体播放
ASSIGN CARPLAY APP	指定 CarPlay 应用
CLEAR CARPLAY APP	清除 CarPlay 应用
ASSIGN ANDROID AUTO APP	指定 Android Auto 应用
CLEAR ANDROID AUTO APP	清除 Android Auto 应用
CHOOSE CARPLAY APP	选择 CarPlay 应用
CHOOSE ANDROID AUTO APP	选择 Android Auto 应用
CHOOSE RADIO APP	选择收音机应用
CHOOSE PIP APP	选择画中画应用
OPEN	打开
Prev	上一首
Next	下一首
Pause	暂停
Play	播放
Source Selector	来源选择
PWR	电源
SEEK ►	搜索 ►
◄ SEEK	◄ 搜索
Clear	清除
CLEAR SOUND	清除音效
ASSIGN PAD SOUND	指定音效
PICK AUDIO FILE	选择音频文件
PRELOADED AUDIO	预置音频
CUSTOM AUDIO FILE	自定义音频文件
CUSTOM FILE ASSIGNED	已指定自定义文件
SAVE SOUND	保存音效
PAD LABEL	音效名称
Custom sound pads	自定义音效按钮
Error playing pad	播放音效失败
Current conditions	当前天气
Sunrise / sunset	日出 / 日落
GPS speed	GPS 速度
Speed & heading	速度与航向
Roll, pitch & altitude	横滚、俯仰与高度
Head Unit Health / Vitals	车机健康状态
Trip logs & stats	行程记录与统计
TRIP TRACKER	行程追踪
SOUNDBOARD	音效板
NOW PLAYING	正在播放
SPEEDOMETER	速度表
ALTIMETER	高度计
BARS VIEW	条形视图
DIAL GAUGES	仪表盘视图
DIAL TRACK	仪表盘轨迹
DIGITAL ONLY	仅数字
BEST RECORD	最佳记录
TAP SPEED TO TEST	点击速度开始测试
DRIVE [TIME]	驾驶［时间］
IDLE [TIME]	怠速［时间］
SYS STAT	系统状态
AVG SPEED // SPD	平均速度 // 速度
DISTANCE // DIST	距离 // 距离
ACCEL TEST // 0-100	加速测试 // 0-100
ACCEL TEST // 0-60	加速测试 // 0-60
'@ -split "`r?`n"

$map = @{}
foreach ($line in $pairs) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    $parts = $line -split "`t", 2
    $map[$parts[0]] = $parts[1]
}
$map['v0.0.5  ·  Made by David Lam  ·  2026'] = "v$releaseVersionName  ·  ayc404 制作  ·  2026"

$internalKeys = @(
    'CLOCK', 'WEATHER', 'TELEMETRY', 'SPEEDOMETER', 'ALTIMETER', 'SOUNDBOARD',
    'VITALS', 'READY', 'RUNNING', 'COMPLETE', 'ADD', 'ACTIVE', 'FULL',
    'Reset', 'Clear', 'Gradient', 'Font', 'Background', 'Vehicle', 'Sidebar',
    'Home', 'Settings', 'Radio', 'Camera', 'Music', 'Phone', 'OPEN',
    'Prev', 'Next', 'Pause', 'Play'
)
foreach ($key in $internalKeys) { $map.Remove($key) }
$map['ALL ${GRID_COLS * GRID_ROWS} CELLS OCCUPIED — REMOVE A WIDGET TO ADD MORE'] = '全部 ${GRID_COLS * GRID_ROWS} 个单元已占用，移除组件后才能继续添加'
$map['Clearing A-GPS cache...'] = '正在清理 A-GPS 缓存…'
$map['Cold start forced — go outdoors for a fresh satellite lock (2–3 min)'] = '已强制冷启动，请到室外获取新的卫星定位（约 2–3 分钟）'
$map['Diagonal (Linear)'] = '对角（线性）'
$map['Not supported by this device''s GPS driver — no data was cleared'] = '此设备的 GPS 驱动不支持该操作，未清除数据'
$map['Radial (Circular)'] = '径向（圆形）'
$map['Start your head unit''s radio app — or assign it below so Open Launcher can mirror and control it'] = '请启动车机收音机应用，或在下方指定应用，让开放启动器镜像并控制它'
$map['Sweep active: Drive slowly in two 360° circles... (${compassCountdown}s remaining)'] = '校准进行中：缓慢绕两圈 360°…剩余 ${compassCountdown} 秒'
$map['Sweep complete — check the compass widget; if heading is still off, use the manual offset below'] = '校准完成，请检查指南针组件；若航向仍有偏差，请使用下方手动偏移'
$map['Time & date'] = '时间与日期'
$map['Widget library'] = '组件库'
$map['Media controls'] = '媒体控制'

$files = Get-ChildItem "$PSScriptRoot\app\src\main\java" -Recurse -Filter *.kt
$utf8 = New-Object System.Text.UTF8Encoding($false)
foreach ($file in $files) {
    $source = [IO.File]::ReadAllText($file.FullName)
    foreach ($entry in $map.GetEnumerator()) {
        $source = $source.Replace('"' + $entry.Key + '"', '"' + $entry.Value + '"')
    }
    [IO.File]::WriteAllText($file.FullName, $source, $utf8)
}
