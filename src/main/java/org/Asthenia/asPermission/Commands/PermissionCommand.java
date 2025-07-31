package org.Asthenia.asPermission.Commands;

import org.Asthenia.asPermission.AsPermission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PermissionCommand {
    private final AsPermission plugin;

    public PermissionCommand(AsPermission plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0];
        switch (subCommand) {
            case "AddPlayerToGroup":
                return handleAddPlayerToGroup(sender, args);
            case "DeletePlayerFromGroup":
                return handleDeletePlayerFromGroup(sender, args);
            case "createGroup":
                return handleCreateGroup(sender, args);
            case "DeleteGroup":
                return handleDeleteGroup(sender, args);
            case "AddPermission":
                return handleAddPermission(sender, args);
            case "DeletePermission":
                return handleDeletePermission(sender, args);
            default:
                sender.sendMessage("§c未知命令。输入 /perm help 查看帮助");
                return false;
        }
    }

    private boolean handleAddPlayerToGroup(CommandSender sender, String[] args) {
        // 实现添加玩家到组的逻辑
        return true;
    }

    private boolean handleDeletePlayerFromGroup(CommandSender sender, String[] args) {
        // 实现从组移除玩家的逻辑
        return true;
    }

    private boolean handleCreateGroup(CommandSender sender, String[] args) {
        // 实现创建组的逻辑
        return true;
    }

    private boolean handleDeleteGroup(CommandSender sender, String[] args) {
        // 实现删除组的逻辑
        return true;
    }

    private boolean handleAddPermission(CommandSender sender, String[] args) {
        // 实现添加权限的逻辑
        return true;
    }

    private boolean handleDeletePermission(CommandSender sender, String[] args) {
        // 实现删除权限的逻辑
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l» §e§lRBAC 权限管理系统帮助 §6§l«");
        sender.sendMessage("§7输入 §b/perm <命令> §7获取详细用法");
        sender.sendMessage("");

        sender.sendMessage("§a§l用户组管理命令:");
        sender.sendMessage("§e/perm createGroup <组名> §7- 创建新权限组");
        sender.sendMessage("§e/perm DeleteGroup <组名> §7- 删除权限组");
        sender.sendMessage("");

        sender.sendMessage("§a§l权限管理命令:");
        sender.sendMessage("§e/perm AddPermission <组名> <权限> §7- 向组添加权限");
        sender.sendMessage("§e/perm DeletePermission <组名> <权限> §7- 从组移除权限");
        sender.sendMessage("");

        sender.sendMessage("§a§l用户管理命令:");
        sender.sendMessage("§e/perm AddPlayerToGroup <玩家> <组名> §7- 分配玩家到权限组");
        sender.sendMessage("§e/perm DeletePlayerFromGroup <玩家> <组名> §7- 从组移除玩家");
        sender.sendMessage("");

        sender.sendMessage("§7提示: 使用Tab键自动补全命令");
        sender.sendMessage("§6§l» §e§l输入具体命令查看详细用法 §6§l«");
    }
}