# CHANGELOG

## 2.16

更新日志：

- [功能] 新增代理面板支持 `HTTP/SOCKS` 等代理
- [BUG] 修复多屏时搜索框和远程加载框位置问题

考虑到新版本体积越来越大，需要加入国内下载地址（自费购买`OSS`节省使用）

- [jar-analyzer-2.16.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.16/jar-analyzer-2.16.zip)
- [jar-analyzer-2.16-windows-full.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.16/jar-analyzer-2.16-windows-full.zip)
- [jar-analyzer-2.16-windows-system.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.16/jar-analyzer-2.16-windows-system.zip)

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- `windows-full` 版内置 `JRE 8` 环境 (**完整功能版本**)
- `windows-system` 版使用系统 `JRE` 的启动脚本 (推荐安装 `JRE/JDK 8`)
- `zip` 版仅包含简单的 `jar` 文件 (使用 `java -jar` 启动)

推荐使用 `windows-full` 版，包含完整功能，其他版本某些功能**可能**不可用

如果你是 `ubuntu/mac` 用户请使用 `zip` 版本（**核心功能**可以正常使用）

## 2.15

更新日志：

- [重要] 代码区域支持 `Ctrl+F` 搜索
- [重要] 支持通过 `HTTP` 远程加载
- [BUG] 启动 `logo` 拼写错误问题
- [优化] 优化命令行参数解析和处理部分代码
- [优化] 删除 `zip` 版本中不必要的文件
- [优化] 默认搜索选项改为模糊和调用搜索
- [优化] 取消打印 `gc` 信息以提升性能
- [优化] 使用稳定的 `okhttp` 代替轮子
- [其他] 更新依赖库到最新版
- [其他] 更新内置`JRE` 到 `8u412`
- [其他] 更换 `UI` 界面的作者头像
- [其他] 更新 `license` 时间信息

考虑到新版本体积越来越大，需要加入国内下载地址（自费购买`OSS`节省使用）

- [jar-analyzer-2.15.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.15/jar-analyzer-2.15.zip)
- [jar-analyzer-2.15-windows-full.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.15/jar-analyzer-2.15-windows-full.zip)
- [jar-analyzer-2.15-windows-system.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.15/jar-analyzer-2.15-windows-system.zip)

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)
- xishir (https://github.com/xishir)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- `windows-full` 版内置 `JRE 8` 环境 (**完整功能版本**)
- `windows-system` 版使用系统 `JRE` 的启动脚本 (推荐安装 `JRE/JDK 8`)
- `zip` 版仅包含简单的 `jar` 文件 (使用 `java -jar` 启动)

推荐使用 `windows-full` 版，包含完整功能，其他版本某些功能**可能**不可用

如果你是 `ubuntu/mac` 用户请使用 `zip` 版本（**核心功能**可以正常使用）

## 2.14

重构了 `UI` 允许放大和全屏显示

安全公告：[GHSA-x5h2-78p8-w943](https://github.com/jar-analyzer/jar-analyzer/security/advisories/GHSA-x5h2-78p8-w943)

更新日志：

- [重要] 重构 `UI` 允许放大和全屏显示
- [安全] 修复了几处 `SQL` 注入漏洞
- [RASP] 默认的 `RASP` 配置关闭反序列化
- [其他] 更新 `flatlaf` 和 `asm` 到最新版
- [其他] 更新 `maven` 插件到最新版

考虑到新版本体积越来越大，需要加入国内下载地址（自费购买`OSS`节省使用）

- [jar-analyzer-2.14.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.14/jar-analyzer-2.14.zip)
- [jar-analyzer-2.14-windows-full.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.14/jar-analyzer-2.14-windows-full.zip)
- [jar-analyzer-2.14-windows-system.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.14/jar-analyzer-2.14-windows-system.zip)

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- `windows-full` 版内置 `JRE 8` 环境 (**完整功能版本**)
- `windows-system` 版使用系统 `JRE` 的启动脚本 (推荐安装 `JRE/JDK 8`)
- `zip` 版仅包含简单的 `jar` 文件 (使用 `java -jar` 启动)

推荐使用 `windows-full` 版，包含完整功能，其他版本某些功能**可能**不可用

如果你是 `ubuntu/mac` 用户请使用 `zip` 版本（**核心功能**可以正常使用）

## 2.13

集成 `https://github.com/luelueking/ClazzSearcher` 作为 `jar-analyzer` 的命令行版本，未来版本可能会完善和修复

更新日志：

- [重要] 集成 [clazz-searcher](https://github.com/luelueking/ClazzSearcher) 项目作为命令行版本
- [功能] 提供 `Windows` 版本的启动 `EXE`
- [其他] 完善和优化 `README` 文档
- [RASP] 优化 `RASP` 相关的功能和文件名长度

考虑到新版本体积越来越大，需要加入国内下载地址（自费购买`OSS`节省使用）

- [jar-analyzer-2.13.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.13/jar-analyzer-2.13.zip)
- [jar-analyzer-2.13-windows-full.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.13/jar-analyzer-2.13-windows-full.zip)
- [jar-analyzer-2.13-windows-system.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.13/jar-analyzer-2.13-windows-system.zip)

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- `windows-full` 版内置 `JRE 8` 环境 (**完整功能版本**)
- `windows-system` 版使用系统 `JRE` 的启动脚本 (推荐安装 `JRE/JDK 8`)
- `zip` 版仅包含简单的 `jar` 文件 (使用 `java -jar` 启动)

推荐使用 `windows-full` 版，包含完整功能，其他版本某些功能**可能**不可用

如果你是 `ubuntu/mac` 用户请使用 `zip` 版本（**核心功能**可以正常使用）

## 2.12

该版本更新内容较多，主要支持了原 `V1` 版本灵活的 `SPEL` 表达式搜索，但当数据过大时分析速度明显下降，之后的版本会尝试提高；
另外删除了对 `Linux` 的专门版本，对于非 `Windows` 用户统一使用 `JAR` 版本即可，该版本保留了绝大多数核心的功能；
对于 `Windows` 两种版本，默认开启简易的 `RASP` 用于保护程序不受到攻击（考虑到后续可能加入网络相关功能以确保安全）

更新日志：

- [重要] 支持表达式搜索（用法参考 `README` 文档）
- [重要] 使用简易版自研 `RASP` 对程序进行保护
- [功能] 支持开启记录所有 `SQL` 语句到日志文件
- [BUG] 修复分析新 `JAR` 时不会刷新文件树的问题
- [优化] 使用 `ConcurrentLinkedQueue` 解决并发问题
- [优化] 使用阻塞队列线程池加速表达式搜索
- [其他] 修改发布 `JAR` 版本变成 `ZIP` 版本
- [其他] 更新内置 `JRE` 版本为 `zulu-8u402`
- [其他] 更新发布类型（删除 `linux` 版增加 `zip` 版）
- [其他] 完善 `CI` 对所有子项目进行测试

考虑到新版本体积越来越大，需要加入国内下载地址（自费购买`OSS`节省使用）

- [jar-analyzer-2.12.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.12/jar-analyzer-2.12.zip)
- [jar-analyzer-2.12-windows-full.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.12/jar-analyzer-2.12-windows-full.zip)
- [jar-analyzer-2.12-windows-system.zip](https://jar-analyzer.oss-cn-hangzhou.aliyuncs.com/2.12/jar-analyzer-2.12-windows-system.zip)

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)
- Gauss Zhou (https://github.com/gausszhou)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- `windows-full` 版内置 `JRE 8` 环境 (**完整功能版本**)
- `windows-system` 版使用系统 `JRE` 的启动脚本 (推荐安装 `JRE/JDK 8`)
- `zip` 版仅包含简单的 `jar` 文件 (使用 `java -jar` 启动)

推荐使用 `windows-full` 版，包含完整功能，其他版本某些功能**可能**不可用

如果你是 `ubuntu/mac` 用户请使用 `zip` 版本（**核心功能**可以正常使用）

## 2.11

更新日志：

- [功能] 支持中英文界面切换
- [优化] 非`windows`打开`tomcat-analyzer`给出警告
- [优化] 所有代码使用统一的`ASM9`版本
- [优化] 所有代码使用统一的`ASN Flags`设置
- [其他] 升级`rsyntaxtextarea`和`slf4j`版本
- [其他] 升级`flatlaf`到`3.4`版本
- [其他] 升级`actions/cache`到`v4`版本
- [其他] 界面一处拼写错误

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- 推荐 `embed` 版内置 `JRE 8` 的启动脚本 (**无需另外安装一键启动**)
- `system` 使用系统 `JDK` 的启动脚本 (需要自行安装 `JDK`)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JDK/JRE`)
- 简单的 `jar` 文件，不提供启动脚本 (使用 `java -jar` 启动)

推荐使用 `embed` 版，经过了较多测试，完善支持 `tomcat-analyzer` 模块

## 2.10

更新日志：

- [功能] 分析`Spring`时根据`Controller`注解显示完整路径
- [功能] 支持在`config`菜单配置按方法名/类名对结果进行排序
- [优化] 删除`JNA`库自行实现具体的功能大幅减小体积
- [优化] 搜索结果为空时不应该跳转页面
- [优化] 搜索到结果时应该提示搜索到结果数量
- [优化] 不显示临时文件里的`dll`本地库文件
- [其他] 升级`flatlaf`到`3.3`版本
- [其他] 升级`maven.compiler`插件到`3.12.1`版本
- [其他] 优化代码`import`并完全格式化

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)
- veo (https://github.com/veo)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- 推荐 `embed` 版内置 `JRE 8` 的启动脚本 (**无需另外安装一键启动**)
- `system` 使用系统 `JDK` 的启动脚本 (需要自行安装 `JDK`)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JDK/JRE`)
- 简单的 `jar` 文件，不提供启动脚本 (使用 `java -jar` 启动)

推荐使用 `embed` 版，经过了较多测试，完善支持 `tomcat-analyzer` 模块

## 2.9

安全更新，修复了一处比较严重的拒绝服务漏洞

安全公告：[GHSA-43rf-3hm4-hv5f](https://github.com/jar-analyzer/jar-analyzer/security/advisories/GHSA-43rf-3hm4-hv5f)

更新日志：

- [安全] 修复`HackingFernFlower`提出的拒绝服务漏洞
- [功能] 支持`Hessian`反序列化一键搜索
- [功能] 支持可能的解压操作一键搜索(`Zip Slip`相关)
- [BUG] 解决不可读字符串导致的`NullPointerException`
- [BUG] 使用`JNA`解决部分`Windows`颜色显示问题
- [优化] 使用`Github Actions`构建
- [优化] 整体优化错误日志的输出
- [Y4-HTTP] 修复响应不含`reason`时解析报错
- [Y4-LANG] 暂时删除`java`版的`y4-lang`
- [其他] 升级第三方组件版本到最新稳定

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)
- Y4tacker (https://github.com/Y4tacker)

所有可供下载的文件都由 `Github Actions` 构建，提供以下四种:

- 推荐 `embed` 版内置 `JRE 8` 的启动脚本 (**无需另外安装一键启动**)
- `system` 使用系统 `JDK` 的启动脚本 (需要自行安装 `JDK`)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JDK/JRE`)
- 简单的 `jar` 文件，不提供启动脚本 (使用 `java -jar` 启动)

推荐使用 `embed` 版，经过了较多测试，完善支持 `tomcat-analyzer` 模块

## 2.8

更新日志：

- [重要] 集成`tomcat-analyzer`项目
- [重要] 支持执行`Y4Lang`脚本语言
- [安全] 阻止`SQLITE`执行`ATTACH`语句
- [BUG] 修复某些情况下程序无法启动的问题
- [BUG] 修复手动查看最新版空指针问题
- [优化] 每次新执行`SQL`语句应该清空报错
- [优化] 解决每次启动多次初始化`UI`的问题
- [优化] 编译去除调试信息缩小`JAR`文件
- [优化] 将`System.err`报错也记录在日志
- [Y4-LOG] 修复无调试信息时行号记录问题
- [Y4-LANG] 修复临时目录不存在时的报错
- [其他] 升级第三方组件版本到最新稳定

感谢以下用户的贡献:

- 4ra1n (https://github.com/4ra1n)

在 `release` 中提供四种下载:

- 推荐 `embed` 版内置 `JRE 8` 的启动脚本 (**无需另外安装一键启动**)
- `system` 使用系统 `JDK/JRE` 的启动脚本 (需要自行安装 `JDK/JRE`)
- `linux` 内置 `shell` 启动脚本 (需要自行安装 `JDK/JRE`)
- 简单的 `jar` 文件，不提供启动脚本 (使用 `java -jar` 启动)

推荐使用 `embed` 版，经过了较多测试，完善支持 `tomcat-analyzer` 模块

## 2.7

正式版本 `2.7` 发布

更新日志:

- [功能] 支持多种`FASTJSON`调用一键搜索 #17
- [功能] 支持命令行方式构建数据库 #13
- [功能] 添加快捷键`Ctrl+X`跳转方法调用界面 #16
- [功能] 新增显示内部类和自动修复类路径的配置
- [功能] 支持修改方法名称并修改对应的引用
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
