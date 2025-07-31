package org.Asthenia.asPermission.OpenAPI;

import org.Asthenia.asPermission.AsPermission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AsPermissionAPI {
    private static volatile AsPermission plugin;
    private static final Object lock = new Object();

    public static void initialize(@NotNull JavaPlugin plugin) throws IllegalStateException {
        Objects.requireNonNull(plugin, "插件实例不能为 null");

        synchronized (lock) {
            if (AsPermissionAPI.plugin != null) {
                throw new IllegalStateException("API 已被初始化");
            }
            if (!(plugin instanceof AsPermission)) {
                throw new IllegalArgumentException("必须传入 AsPermission 实例");
            }
            AsPermissionAPI.plugin = (AsPermission) plugin;
        }
    }

    public static boolean isInitialized() {
        return plugin != null;
    }

    // 其他方法添加状态检查
    public static boolean hasPermission(Player player, String permission) {
        requireInitialized();
        return plugin.getUserManager().hasPermission(player.getUniqueId(), permission);
    }

    private static void requireInitialized() {
        if (plugin == null) {
            throw new IllegalStateException("API 未初始化. 请确保: " +
                    "1. AsPermission 插件已加载\n" +
                    "2. 调用 AsPermissionAPI.initialize()");
        }
    }
}