1.支持RBAC权限模型：实现 用户->权限组->细分权限的隔离

2.实现数据在磁盘的持久化->yml格式存储（Config配置项+权限组与权限的关联+用户与权限组的关联）

3.支持开放API-兼容其他As系列插件的权限管理

4.开发版本相关：

A.JDK版本：21

B.Minecraft版本：1.21.8

C.服务端类型：Paper

D.注意项：在pom安装依赖时候，如果你默认采取了阿里云镜像加速，且没有设置<mirrorOf>你是无法正确下载到papermc的依赖的，因此你需要在你maven仓库的settings.xml中在阿里云镜像的<mirrorof>栏目中添加!papermc。从而绕开papermc进行加速。