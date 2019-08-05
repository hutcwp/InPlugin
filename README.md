# InPlugin

插件化学习demo，尚未完善

大致流程为：

1. 从配置文件plugin.json读取出插件列表
2. 从服务器或者本地读取插件，并copy到内存（asset目录）
3. 加载并启动插件


插件化实现方式：参考Small和《Android插件化开发指南》

1. hook Instrumentation,AMN.getDefault , ActivityThreadHandlerCallback
2. merge dex和资源到host
3. 插件资源id分段
4. 采取占位的方式绕过AndroidManifest检查

运行方式：

先执行plugina 和 pluginb的assemble task.生成plugina.apk和pluginb.apk到手机外存的mplugins目录。
然后运行run app安装宿主包。启动宿组，自动加载插件包。

