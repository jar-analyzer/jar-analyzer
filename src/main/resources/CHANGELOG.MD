# CHANGELOG

## 2.7

正式版本 `2.7` 发布

更新日志:

- [功能] 支持多种`FASTJSON`调用一键搜索 #17
- [功能] 支持命令行方式构建数据库 #13
- [功能] 添加快捷键`Ctrl+X`跳转方法调用界面 #16
- [功能] 新增显示内部类和自动修复类路径的配置
- [BUG] 解决某些情况下双击方法区域的空指针
- [BUG] 解决某些情况下查到类方法数量是空的问题
- [BUG] 当字符串列表巨大时点击查看程序会卡死 #14
- [BUG] 当添加`RT.JAR`分析后不会记录输入`JAR`文件 #15
- [BUG] 编码解码工具中`Base64`部分按钮错误
- [BUG] 当不存在配置文件时`GPT`无法打开
- [优化] 优化`SimpleFrame`中对`null`的显示
- [优化] 优化一些按钮的停留提示内容
- [优化] 字符串列表支持分页和去重排序等优化
- [优化] 优化启动脚本加入`DisableAttachMechanism`
- [优化] 优化各种分析功能获取类文件的逻辑
- [Y4-LOG] 内置`Y4-LOG`库支持写入日志文件
- [其他] 更新B站教程视频

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

在 `release` 中提供四种下载:

- `system` 使用系统 `JDK/JRE` 的启动脚本 (需要自行安装 `JDK/JRE`)
- `embed` 内置 `JRE 8` 的启动脚本 (**无需另外安装一键启动**)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JDK/JRE`)
- 简单的 `Jar` 文件，不提供启动脚本 (使用 `java -jar` 启动)

## 2.6-beta

该版本没有大功能更新，但大幅减小了体积

自己实现`json`序列化反序列化，以及`http`和`log`库进一步减小体积

内置简化`sqlite`驱动，可减小`50%`体积，仅保留以下`native`库:

- `linux x86_64`
- `windows x86_86`
- `mac aarch64`和`mac x86_64`

更新日志:

- [功能] 支持拖拽`jar`文件到`GUI`界面
- [功能] 增加`Show String`按钮查看所有字符串
- [功能] 增加`clean`按钮一键清除临时文件
- [功能] 增加`ChatGPT`插件并支持保存配置
- [BUG] 同目录只允许运行一个实例防止数据冲突
- [优化] 内置`sqlite`驱动删除不必要的部分
- [优化] 优化反编译部分的`LRU`缓存代码
- [Y4-JSON] 内置`Y4-JSON`库代替原`Fastjson`库
- [Y4-HTTP] 内置`Y4-HTTP`库代替原`http`库
- [Y4-LOG] 内置`Y4-LOG`库代替原`Log4j2`库
- [JRE] 使用更稳定的`Zulu JRE`支持内置版本
- [其他] 支持了英文版的文档

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

在 `release` 中提供四种下载:

- `system` 使用系统 `JDK/JRE` 的启动脚本 (需要自行安装 `JDK/JRE`)
- `embed` 内置 `JRE 8` 的启动脚本 (**无需另外安装一键启动**)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JDK/JRE`)
- 简单的 `Jar` 文件，不提供启动脚本 (使用 `java -jar` 启动)

## 2.5-beta

- [重要] 支持编写自定义`SQL`语句查询
- [BUG] 切换搜索模式时应该清空无效字段
- [优化] 反编译代码功能使用`LRU`缓存优化
- [优化] 解决某些情况下`UI`抖动的问题
- [优化] 不应该显示`access$`私有成员辅助方法
- [其他] 使用`properties`替换`yaml`减小体积
- [其他] 升级`sqlite-jdbc`和`log4j2`到最新版
- [其他] 删除意义不大的`chatgpt`模块
- [其他] 升级内置`Y4 JRE`到`1.8.0_402-b01`
- [其他] `README`加入解决中文乱码的办法

感谢以下用户的帮助和贡献：

- alwaystest

## 2.4-beta

- [功能] 支持`Runtime.exec`和`ProcessBuilder.start`一键搜索
- [功能] 支持`JNDI`漏洞`Context.lookup`一键搜索
- [功能] 支持`readObject`和`ScriptEngine.eval`一键搜索
- [功能] 支持`BCEL.loadClass`和`defineClass`一键搜索
- [功能] 支持`SPEL`和`OGNL`的一键搜索
- [功能] 支持手动刷新`spring`分析结果
- [BUG] 修复输入`classname`为`null`时的空指针
- [BUG] 修复在分析部分类的时候发生空指针异常的问题
- [BUG] 在使用`Lambda/Streamd/Runnable`等特性时丢失分析结果 #8
- [优化] 优化搜索结果为空时的用户体验
- [优化] 优化检查最新版本中的`json`解析代码
- [优化] 使用单独的服务器验证最新版避免`github api`访问不到
- [优化] 自行实现`http`库代替`okhttp`减小体积
- [其他] 代码格式化以及优化代码`import`部分
- [其他] 加大`CHANGELOG`的宽度以完全显示

感谢以下用户的帮助和贡献：

- whwlsfb
- wouijvziqy 

## 2.3-beta

- [重要] 增加分析前指定包名或类名黑名单配置 #6
- [重要] 增加`frame`中对象哈希追踪分析
- [BUG] 修复分析`frame`时偶尔`class-not-found`
- [BUG] 某些UI逻辑有问题，提升用户体验
- [优化] 搜索界面UI优化并添加部分中文提示
- [优化] 使用`fernflower`源码替换第三方库
- [其他] 更新日志应该按照从新到老的顺序
- [其他] 使用`versions`插件自动检查依赖版本
- [其他] 使用`log4j-iostreams`优化日志
- [其他] 代码重构并升级所有依赖版本到最新稳定版
- [其他] 内置`jre`更新到`y4-jre-1.8.0_392-b03`

## 2.2-beta

- [重要] 支持搜索时配置类名黑名单对结果进行过滤
- [重要] 支持搜索时选择`like`和`equals`两种模式
- [重要] 支持前后跳转：返回上一步和回到当前
- [功能] 程序内提供更新日志，集成`markdown`解析库
- [BUG] 分析`spring`由于`path`为空导致的`BUG` #5
- [优化] 删除某些巨大的依赖库并计划自行实现
- [优化] 搜索输入类名优化，支持`.`替换为`/`字符
- [其他] 删除不必要代码以及简单优化已有代码

## 2.1-beta

- [重要] 支持分析`spring controller`和`mapping`
- [BUG] 解决`mac/ubuntu`中`cfg/frame`分析乱码
- [功能] 支持选择`classes`目录进行分析
- [功能] 内置`consolas`字体启动时自动注册
- [功能] 启动时通过`github`的`api`检查更新
- [其他] 简单的性能和用户体验的优化
- [其他] 不再使用`exe`版本而是使用`bat/sh`脚本启动
- [其他] 发布`release`将包含`win`和`linux(ubuntu)`两种

## 2.0-beta

first release
