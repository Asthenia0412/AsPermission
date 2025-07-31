package org.Asthenia.asPermission.OpenAPI;

import org.Asthenia.asPermission.AsPermission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;

/**
 * AsPermission 插件对外公开的 API 接口
 */
public class AsPermissionAPI {
    private static AsPermission plugin;

    /**
     * 初始化 API
     * @param plugin 主插件实例
     */
    public static void initialize(JavaPlugin plugin) {
        if (plugin instanceof AsPermission) {
            AsPermissionAPI.plugin = (AsPermission) plugin;
        } else {
            throw new IllegalArgumentException("必须传入 AsPermission 插件实例");
        }
    }

    /**
     * 检查玩家是否拥有指定权限
     * @param player 玩家对象
     * @param permission 要检查的权限节点
     * @return 是否拥有权限
     */
    public static boolean hasPermission(Player player, String permission) {
        if (plugin == null) throw new IllegalStateException("API 未初始化");
        return plugin.getUserManager().hasPermission(player.getUniqueId(), permission);
    }

    /**
     * 检查玩家是否拥有指定权限
     * @param playerUUID 玩家的UUID
     * @param permission 要检查的权限节点
     * @return 是否拥有权限
     */
    public static boolean hasPermission(UUID playerUUID, String permission) {
        if (plugin == null) throw new IllegalStateException("API 未初始化");
        return plugin.getUserManager().hasPermission(playerUUID, permission);
    }

    /**
     * 获取玩家所在的权限组
     * @param player 玩家对象
     * @return 权限组名称集合
     */
    public static Set<String> getPlayerGroups(Player player) {
        if (plugin == null) throw new IllegalStateException("API 未初始化");
        return plugin.getUserManager().getUserGroups(player.getUniqueId());
    }

    /**
     * 获取玩家所在的权限组
     * @param playerUUID 玩家的UUID
     * @return 权限组名称集合
     */
    public static Set<String> getPlayerGroups(UUID playerUUID) {
        if (plugin == null) throw new IllegalStateException("API 未初始化");
        return plugin.getUserManager().getUserGroups(playerUUID);
    }

    /**
     * 检查玩家是否在指定权限组中
     * @param player 玩家对象
     * @param groupName 权限组名称
     * @return 是否在组中
     */
    public static boolean isPlayerInGroup(Player player, String groupName) {
        if (plugin == null) throw new IllegalStateException("API 未初始化");
        return plugin.getUserManager().isUserInGroup(player.getUniqueId(), groupName);
    }

    /**
     * 检查玩家是否在指定权限组中
     * @param playerUUID 玩家的UUID
     * @param groupName 权限组名称
     * @return 是否在组中
     */
    public static boolean isPlayerInGroup(UUID playerUUID, String groupName) {
        if (plugin == null) throw new IllegalStateException("API 未初始化");
        return plugin.getUserManager().isUserInGroup(playerUUID, groupName);
    }

    /**
     * 获取插件实例
     * @return AsPermission 插件实例
     */
    public static AsPermission getPlugin() {
        return plugin;
    }
}