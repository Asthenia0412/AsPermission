package org.Asthenia.asPermission.Commands;

import org.Asthenia.asPermission.AsPermission;
import org.Asthenia.asPermission.Service.PermissionGroup;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionCommand {
    private final AsPermission plugin;
    private static final String NO_PERMISSION_MSG = "§c你没有权限执行此命令!";
    private static final String PLAYER_NOT_FOUND = "§c玩家不存在或从未登录过服务器!";
    private static final String GROUP_NOT_FOUND = "§c权限组 '%s' 不存在!";
    private static final String PERMISSION_NOT_FOUND = "§c权限 '%s' 不在组中!";
    private static final String PERMISSION_ALREADY_EXISTS = "§c权限 '%s' 已在组中!";
    private static final String PLAYER_NOT_IN_GROUP = "§c玩家 %s 不在组 %s 中";
    private static final String GROUP_ALREADY_EXISTS = "§c权限组 '%s' 已存在!";
    private static final String DATA_SAVE_ERROR = "§c保存数据失败: %s";
    private static final String RELOAD_SUCCESS = "§a权限系统重载成功!";
    private static final String RELOAD_FAILED = "§c重载失败: %s";

    public PermissionCommand(AsPermission plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        try {
            switch (subCommand) {
                case "addplayertogroup":
                    return checkAdminPermission(sender) && handleAddPlayerToGroup(sender, args);
                case "deleteplayerfromgroup":
                    return checkAdminPermission(sender) && handleDeletePlayerFromGroup(sender, args);
                case "creategroup":
                    return checkAdminPermission(sender) && handleCreateGroup(sender, args);
                case "deletegroup":
                    return checkAdminPermission(sender) && handleDeleteGroup(sender, args);
                case "addpermission":
                    return checkAdminPermission(sender) && handleAddPermission(sender, args);
                case "deletepermission":
                    return checkAdminPermission(sender) && handleDeletePermission(sender, args);
                case "listgroups":
                    return handleListGroups(sender);
                case "listpermissions":
                    return handleListPermissions(sender, args);
                case "checkpermission":
                    return handleCheckPermission(sender, args);
                case "reload":
                    return checkAdminPermission(sender) && handleReload(sender);
                case "help":
                    sendHelp(sender);
                    return true;
                default:
                    sender.sendMessage("§c未知命令。输入 /perm help 查看帮助");
                    return false;
            }
        } catch (Exception e) {
            sender.sendMessage("§c执行命令时出错: " + e.getMessage());
            plugin.getLogger().warning("执行命令出错: " + e.getMessage());
            return false;
        }
    }

    private boolean checkAdminPermission(CommandSender sender) {
        if (!sender.hasPermission("asperm.admin")) {
            sender.sendMessage(NO_PERMISSION_MSG);
            return false;
        }
        return true;
    }

    private boolean handleAddPlayerToGroup(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 3, "/perm AddPlayerToGroup <玩家> <组名>")) {
            return false;
        }

        String playerName = args[1];
        String groupName = args[2];

        UUID playerUUID = getPlayerUUID(playerName);
        if (playerUUID == null) {
            sender.sendMessage(PLAYER_NOT_FOUND);
            return false;
        }

        if (plugin.getGroup(groupName) == null) {
            sender.sendMessage(String.format(GROUP_NOT_FOUND, groupName));
            return false;
        }

        plugin.getUserManager().addUserToGroup(playerUUID, groupName);
        sender.sendMessage(String.format("§a成功将玩家 %s 添加到组 %s", playerName, groupName));

        try {
            plugin.saveAll();
        } catch (IOException e) {
            sender.sendMessage(String.format(DATA_SAVE_ERROR, e.getMessage()));
        }

        return true;
    }

    private boolean handleDeletePlayerFromGroup(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 3, "/perm DeletePlayerFromGroup <玩家> <组名>")) {
            return false;
        }

        String playerName = args[1];
        String groupName = args[2];

        UUID playerUUID = getPlayerUUID(playerName);
        if (playerUUID == null) {
            sender.sendMessage(PLAYER_NOT_FOUND);
            return false;
        }

        if (!plugin.getUserManager().isUserInGroup(playerUUID, groupName)) {
            sender.sendMessage(String.format(PLAYER_NOT_IN_GROUP, playerName, groupName));
            return false;
        }

        plugin.getUserManager().removeUserFromGroup(playerUUID, groupName);
        sender.sendMessage(String.format("§a成功将玩家 %s 从组 %s 移除", playerName, groupName));

        try {
            plugin.saveAll();
        } catch (IOException e) {
            sender.sendMessage(String.format(DATA_SAVE_ERROR, e.getMessage()));
        }

        return true;
    }

    private boolean handleCreateGroup(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 2, "/perm createGroup <组名>")) {
            return false;
        }

        String groupName = args[1];

        if (plugin.getGroup(groupName) != null) {
            sender.sendMessage(String.format(GROUP_ALREADY_EXISTS, groupName));
            return false;
        }

        try {
            PermissionGroup group = plugin.getOrCreateGroup(groupName);
            sender.sendMessage("§a成功创建权限组: " + groupName);
            return true;
        } catch (IOException e) {
            sender.sendMessage("§c创建组失败: " + e.getMessage());
            return false;
        }
    }

    private boolean handleDeleteGroup(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 2, "/perm DeleteGroup <组名>")) {
            return false;
        }

        String groupName = args[1];

        if (plugin.getGroup(groupName) == null) {
            sender.sendMessage(String.format(GROUP_NOT_FOUND, groupName));
            return false;
        }

        boolean success = plugin.deleteGroup(groupName);
        if (success) {
            sender.sendMessage("§a成功删除权限组: " + groupName);
            return true;
        } else {
            sender.sendMessage("§c删除权限组失败!");
            return false;
        }
    }

    private boolean handleAddPermission(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 3, "/perm AddPermission <组名> <权限>")) {
            return false;
        }

        String groupName = args[1];
        String permission = args[2];

        PermissionGroup group = plugin.getGroup(groupName);
        if (group == null) {
            sender.sendMessage(String.format(GROUP_NOT_FOUND, groupName));
            return false;
        }

        if (group.hasPermission(permission)) {
            sender.sendMessage(String.format(PERMISSION_ALREADY_EXISTS, permission));
            return false;
        }

        group.addPermission(permission);
        sender.sendMessage(String.format("§a成功将权限 %s 添加到组 %s", permission, groupName));

        try {
            group.save();
        } catch (IOException e) {
            sender.sendMessage(String.format(DATA_SAVE_ERROR, e.getMessage()));
        }

        return true;
    }

    private boolean handleDeletePermission(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 3, "/perm DeletePermission <组名> <权限>")) {
            return false;
        }

        String groupName = args[1];
        String permission = args[2];

        PermissionGroup group = plugin.getGroup(groupName);
        if (group == null) {
            sender.sendMessage(String.format(GROUP_NOT_FOUND, groupName));
            return false;
        }

        if (!group.hasPermission(permission)) {
            sender.sendMessage(String.format(PERMISSION_NOT_FOUND, permission));
            return false;
        }

        group.removePermission(permission);
        sender.sendMessage(String.format("§a成功从组 %s 移除权限 %s", groupName, permission));

        try {
            group.save();
        } catch (IOException e) {
            sender.sendMessage(String.format(DATA_SAVE_ERROR, e.getMessage()));
        }

        return true;
    }

    private boolean handleListGroups(CommandSender sender) {
        Map<String, PermissionGroup> groups = plugin.getGroups();
        if (groups.isEmpty()) {
            sender.sendMessage("§c当前没有权限组");
            return true;
        }

        sender.sendMessage("§a§l现有权限组列表 (§e总计: " + groups.size() + "§a§l):");
        groups.forEach((name, group) -> {
            int permissionCount = group.getPermissions().size();
            sender.sendMessage(String.format("§e- %s §7(权限数: %d)", name, permissionCount));
        });
        return true;
    }

    private boolean handleListPermissions(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 2, "/perm listPermissions <组名>")) {
            return false;
        }

        String groupName = args[1];
        PermissionGroup group = plugin.getGroup(groupName);
        if (group == null) {
            sender.sendMessage(String.format(GROUP_NOT_FOUND, groupName));
            return false;
        }

        Set<String> permissions = group.getPermissions();
        if (permissions.isEmpty()) {
            sender.sendMessage("§c组 '" + groupName + "' 没有任何权限");
            return true;
        }

        sender.sendMessage(String.format("§a§l组 %s 的权限列表 (§e总计: %d§a§l):", groupName, permissions.size()));
        permissions.forEach(perm -> sender.sendMessage("§e- " + perm));
        return true;
    }

    private boolean handleCheckPermission(CommandSender sender, String[] args) {
        if (!checkArgs(sender, args, 3, "/perm checkPermission <玩家> <权限>")) {
            return false;
        }

        String playerName = args[1];
        String permission = args[2];

        UUID playerUUID = getPlayerUUID(playerName);
        if (playerUUID == null) {
            sender.sendMessage(PLAYER_NOT_FOUND);
            return false;
        }

        boolean hasPerm = plugin.getUserManager().hasPermission(playerUUID, permission);
        if (hasPerm) {
            sender.sendMessage(String.format("§a玩家 %s 拥有权限 %s", playerName, permission));
        } else {
            sender.sendMessage(String.format("§c玩家 %s 没有权限 %s", playerName, permission));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        try {
            plugin.reloadAllData();
            sender.sendMessage(RELOAD_SUCCESS);
            return true;
        } catch (IOException e) {
            sender.sendMessage(String.format(RELOAD_FAILED, e.getMessage()));
            return false;
        }
    }

    private UUID getPlayerUUID(String playerName) {
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // 检查离线玩家
        OfflinePlayer offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);

        return offlinePlayer != null ? offlinePlayer.getUniqueId() : null;
    }

    private boolean checkArgs(CommandSender sender, String[] args, int required, String usage) {
        if (args.length < required) {
            sender.sendMessage("§c用法: " + usage);
            return false;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> helpMessages = Arrays.asList(
                "§6§l» §e§lRBAC 权限管理系统帮助 §6§l«",
                "§7输入 §b/perm <命令> §7获取详细用法",
                "",
                "§a§l用户组管理命令:",
                "§e/perm createGroup <组名> §7- 创建新权限组",
                "§e/perm DeleteGroup <组名> §7- 删除权限组",
                "§e/perm listGroups §7- 列出所有权限组",
                "",
                "§a§l权限管理命令:",
                "§e/perm AddPermission <组名> <权限> §7- 向组添加权限",
                "§e/perm DeletePermission <组名> <权限> §7- 从组移除权限",
                "§e/perm listPermissions <组名> §7- 列出组的所有权限",
                "",
                "§a§l用户管理命令:",
                "§e/perm AddPlayerToGroup <玩家> <组名> §7- 分配玩家到权限组",
                "§e/perm DeletePlayerFromGroup <玩家> <组名> §7- 从组移除玩家",
                "§e/perm checkPermission <玩家> <权限> §7- 检查玩家是否有权限",
                "",
                "§a§l系统命令:",
                "§e/perm reload §7- 重载权限系统 (需要 asperm.admin 权限)",
                "§e/perm help §7- 显示帮助信息",
                "",
                "§7提示: 使用Tab键自动补全命令",
                "§6§l» §e§l输入具体命令查看详细用法 §6§l«"
        );

        helpMessages.forEach(sender::sendMessage);
    }
}