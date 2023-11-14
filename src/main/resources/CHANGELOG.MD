# CHANGELOG

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
