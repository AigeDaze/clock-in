# HabitTracker — 习惯打卡 App

## 项目简介

一款基于 Java 的 Android 习惯打卡应用。支持创建每日习惯、定时提醒打卡、连续天数统计、里程碑庆祝，并提供热力图统计、个性化主题、壁纸设置、双语切换等丰富的自定义功能。

## 主要功能

### 习惯管理
- 新建/编辑/删除习惯，设置名称与每日提醒时间
- 为习惯添加激励语，在通知中展示以激励坚持
- 每日打卡标记，横向待打卡卡片快速操作
- 连续打卡天数自动统计，7/21/50/100 天弹出里程碑庆祝弹窗

### 数据统计
- GitHub 风格近 30 天打卡热力图
- 近 6 个月月度完成率柱状图（MPAndroidChart）
- 按目标筛选统计数据

### 提醒系统
- AlarmManager 精确定时提醒
- 通知栏快捷打卡按钮，无需打开 App
- 提醒前检查今日打卡状态，已打卡则静默跳过
- 开机自启自动恢复所有提醒

### 个性化设置
- 中/英双语切换
- 9 色 Material 3 主题调色盘
- 自定义壁纸（裁剪 + 不透明度调节）
- 字体大小（4 档）与粗体开关
- 桌面 App Widget

### 主题适配
- Material Design 3 DayNight 主题
- Material You 动态取色（Android 12+）
- 完整深色模式支持

## 使用技术

| 类别 | 技术 |
|------|------|
| 语言 | Java |
| 构建 | Gradle 7.6 + AGP 7.4.2 |
| UI | Material Design 3、MaterialToolbar、MaterialCardView |
| 列表 | RecyclerView + ListAdapter + DiffUtil |
| 图表 | MPAndroidChart v3.1.0 |
| 存储 | 内存存储（ArrayList/Map，单线程 Executor） |
| 通知 | NotificationChannel + BigTextStyle + 快捷操作 |
| 提醒 | AlarmManager（setExactAndAllowWhileIdle） |
| 系统 | BroadcastReceiver（Alarm + Boot）、App Widget |
| 国际化 | values + values-en 双语言资源 |
| 主题 | Material 3 DayNight + DynamicColors + 自定义主题叠加 |
| 自定义 View | HeatmapView（Canvas 热力图）、CropImageView（壁纸裁剪） |

## 项目结构

```
app/src/main/java/com/example/habittracker/
├── data/
│   ├── Goal.java                  # 习惯实体（含激励语字段）
│   ├── GoalDao.java               # 习惯 DAO
│   ├── Checkin.java               # 打卡记录实体
│   ├── CheckinDao.java            # 打卡 DAO
│   └── AppDatabase.java           # 数据库入口
├── notification/
│   ├── ReminderHelper.java        # 闹钟调度
│   └── ReminderReceiver.java      # 闹钟接收器（通知 + 重新调度）
├── ui/
│   ├── MainActivity.java          # 主页面
│   ├── AddGoalActivity.java       # 添加习惯
│   ├── SettingsActivity.java      # 设置页面
│   ├── StatsActivity.java         # 统计页面
│   ├── CropWallpaperActivity.java # 壁纸裁剪
│   ├── BaseActivity.java          # 基类（主题/语言/字体/壁纸）
│   ├── GoalAdapter.java           # 列表适配器
│   ├── GoalItem.java              # 列表项
│   ├── HeatmapView.java           # 热力图自定义 View
│   ├── CropImageView.java         # 裁剪自定义 View
│   ├── LocaleHelper.java         # 语言切换
│   ├── ThemeColorHelper.java     # 主题色调
│   ├── WallpaperHelper.java      # 壁纸管理
│   └── FontHelper.java           # 字体设置
├── widget/
│   └── HabitWidgetProvider.java   # 桌面小组件
├── HabitDetailActivity.java       # 习惯详情/编辑
├── BootReceiver.java              # 开机自启
└── NotificationActionReceiver.java # 通知快捷操作
```

## 运行方式

1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步（需代理或阿里云镜像）
3. 连接 Android 7.0+ 设备或模拟器
4. 点击 Run 运行

## 版本记录

| 版本 | 内容 |
|------|------|
| v1.3 | 个性化设置系统：双语、主题色、壁纸、字体 |
| v1.2 | Material Design 3 + 深色模式适配 |
| v1.1 | 里程碑庆祝 + 统计页面 + App Widget |
| v1.0 | 基础功能：习惯 CRUD、打卡、提醒 |

## 作者

**李秉桐** | 数媒2班 | 23120032006
