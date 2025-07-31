<p align="center"> ❤️ 由 Asthenia  </p>

# AsPermission - Minecraft RBAC 权限管理系统

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-green)
![Java](https://img.shields.io/badge/Java-21-blue)
![License](https://img.shields.io/badge/License-MIT-orange)

## 📖 项目简介

AsPermission 是一个基于 RBAC (基于角色的访问控制) 模型的 Minecraft 权限管理系统插件，专为 Paper 服务端设计。提供细粒度的权限控制和高效的数据持久化存储。

## ✨ 核心特性

### 🛡️ RBAC 权限模型

- **用户→权限组→权限** 三级权限结构
- 支持多级权限继承
- 动态权限实时更新

### 💾 数据持久化

- YAML 格式存储配置
- 自动保存用户权限组关联
- 权限组配置独立存储

### 🔌 开放 API

- 兼容 As 系列插件生态
- 提供完整的 JavaDoc 文档
- 支持事件监听和自定义扩展

## 🚀 快速开始

### 安装要求

- **Java 21+**
- **Paper 1.21.8** 服务端
- 至少 2MB 可用内存

### 安装步骤

1. 下载最新版本插件 JAR 文件
2. 放入服务器的 `plugins/` 目录
3. 重启服务器

## ⚙️ 命令列表

### 用户组管理命令

| 命令                                        | 描述             | 权限节点       |
| ------------------------------------------- | ---------------- | -------------- |
| `/perm addplayertogroup <玩家> <组名>`      | 添加玩家到权限组 | `asperm.admin` |
| `/perm deleteplayerfromgroup <玩家> <组名>` | 从组移除玩家     | `asperm.admin` |
| `/perm creategroup <组名>`                  | 创建新权限组     | `asperm.admin` |
| `/perm deletegroup <组名>`                  | 删除权限组       | `asperm.admin` |
| `/perm listgroups`                          | 列出所有权限组   | `asperm.use`   |
| `/perm listgroupplayers <组名>`             | 列出组内所有玩家 | `asperm.use`   |

### 权限管理命令

| 命令                                   | 描述               | 权限节点       |
| -------------------------------------- | ------------------ | -------------- |
| `/perm addpermission <组名> <权限>`    | 向组添加权限       | `asperm.admin` |
| `/perm deletepermission <组名> <权限>` | 从组移除权限       | `asperm.admin` |
| `/perm listpermissions <组名>`         | 列出组的所有权限   | `asperm.use`   |
| `/perm checkpermission <玩家> <权限>`  | 检查玩家是否有权限 | `asperm.use`   |

### 系统命令

| 命令           | 描述         | 权限节点       |
| -------------- | ------------ | -------------- |
| `/perm reload` | 重载权限系统 | `asperm.admin` |
| `/perm help`   | 显示帮助信息 | `asperm.use`   |

## 📂 文件结构

```
plugins/AsPermission/
├── config.yml       # 主配置文件
├── groups/          # 权限组存储目录
│   ├── admin.yml
│   ├── default.yml
│   └── vip.yml
└── users.yml        # 用户数据文件
```

## 🛠️ 开发者指南

### Maven 配置

```
<repositories>
    <repository>
        <id>papermc</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.Asthenia</groupId>
        <artifactId>AsPermission</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### API 使用示例

```
// 检查玩家权限
boolean hasPerm = AsPermissionAPI.hasPermission(player, "minecraft.command.gamemode");

// 获取玩家所在组
Set<String> groups = AsPermissionAPI.getPlayerGroups(player);

// 检查玩家是否在组中
boolean isVIP = AsPermissionAPI.isPlayerInGroup(player, "vip");
```

## ⚠️ 注意事项

1. 使用阿里云镜像时需在 

   ```
   settings.xml
   ```

    中添加：

   ```
   <mirrorOf>*,!papermc</mirrorOf>
   ```

2. 建议定期备份 `plugins/AsPermission/` 目录

3. 修改权限组后建议执行 `/perm reload`

## 📜 开源协议

本项目采用 **MIT License**，详情请查看 [LICENSE](https://yuanbao.tencent.com/chat/naQivTmsDa/LICENSE) 文件。

------

<p align="center"> ❤️ 由 Asthenia 团队开发</p>
