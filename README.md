#Android Habit Tracker
##项目简介
本项目是一个基于 Java 的 Android 习惯打卡 App，用于创建每日习惯并定时提醒用户打卡，支持连续打卡天数统计。

##主要功能
添加习惯（设置名称和每日提醒时间）
显示习惯列表（含连续打卡天数和今日打卡状态）
每日打卡（标记当天已完成）
查看/编辑习惯详情（修改名称、提醒时间）
删除习惯（含打卡记录清理）
自动连续天数统计
定时通知提醒（AlarmManager + Notification）
开机自启恢复提醒（BootReceiver）

##使用技术
Java
Android Studio
XML Layout
RecyclerView + ListAdapter + DiffUtil
In-Memory Data Storage（ArrayList/Map 模式）
Material Design Components（CardView、TimePicker、TextInputLayout）
AlarmManager（定时提醒）
Notification + NotificationChannel（通知栏提醒）
BroadcastReceiver（系统广播处理）
Git

##项目结构

app/src/main/java/com/example/habittracker/
├── data/
│   ├── Goal.java                # 习惯数据实体类
│   ├── GoalDao.java             # 习惯数据操作（增删改查）
│   ├── Checkin.java             # 打卡记录实体类
│   ├── CheckinDao.java          # 打卡记录数据操作
│   └── AppDatabase.java         # 数据库入口（内存存储）
├── notification/
│   ├── ReminderHelper.java      # 闹钟调度工具类
│   └── ReminderReceiver.java    # 闹钟广播接收器（弹通知+重新调度）
├── ui/
│   ├── MainActivity.java        # 主页面（习惯列表）
│   ├── AddGoalActivity.java     # 添加习惯页面
│   ├── GoalAdapter.java         # 习惯列表适配器
│   └── GoalItem.java            # 列表项数据封装
├── HabitDetailActivity.java     # 习惯详情/编辑页面
├── BootReceiver.java            # 开机自启广播接收器
└── NotificationActionReceiver.java  # 通知栏快捷操作接收器
##运行方式
使用 Android Studio 打开项目；
等待 Gradle 同步完成（需配置阿里云 Maven 镜像）；
连接模拟器或真实设备（Android 7.0+）；
点击 Run 运行项目。

##开发过程
本项目使用 Git 进行版本管理，主要提交记录包括：

创建 Android 项目基础结构；
初步构建：完成所有功能编写、修复编译错误、生成可安装 APK；
添加通知权限请求与闹钟权限降级处理（Android 13/14 兼容）。

##作者信息
姓名：李秉桐
班级：数媒2班
学号：23120032006
