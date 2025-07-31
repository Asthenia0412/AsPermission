package org.Asthenia.asPermission.Service;

import org.Asthenia.asPermission.AsPermission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class UserManager {
    private final AsPermission plugin;
    private final Map<UUID, Set<String>> userGroups = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> permissionAttachments = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Set<String> defaultGroups = ConcurrentHashMap.newKeySet();

    public UserManager(AsPermission plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "users.yml");
        updateDefaultGroups();
    }

    public void updateDefaultGroups() {
        defaultGroups.clear();
        defaultGroups.add(plugin.getDefaultGroup());
    }

    public void load() {
        long startTime = System.currentTimeMillis();

        if (!dataFile.exists()) {
            plugin.getLogger().info("未找到用户数据文件，将创建新文件");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        int loadedCount = 0;

        for (String uuidStr : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> groups = config.getStringList(uuidStr);

                if (!groups.isEmpty()) {
                    userGroups.put(uuid, ConcurrentHashMap.newKeySet());
                    userGroups.get(uuid).addAll(groups);
                    loadedCount++;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的UUID格式: " + uuidStr);
            }
        }

        plugin.getLogger().info(String.format(
                "已加载 %d 个用户的权限组数据 (耗时: %dms)",
                loadedCount,
                System.currentTimeMillis() - startTime
        ));
    }

    public void save() {
        long startTime = System.currentTimeMillis();
        YamlConfiguration config = new YamlConfiguration();

        try {
            userGroups.entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .forEach(entry ->
                            config.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()))
                    );

            config.save(dataFile);

            plugin.getLogger().info(String.format(
                    "已保存 %d 个用户的权限组数据 (耗时: %dms)",
                    userGroups.size(),
                    System.currentTimeMillis() - startTime
            ));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存用户数据时发生错误", e);
        }
    }

    public boolean addUserToGroup(UUID user, String group) {
        Objects.requireNonNull(user, "用户UUID不能为null");
        Objects.requireNonNull(group, "组名不能为null");

        if (plugin.getGroup(group) == null) {
            plugin.getLogger().warning("尝试添加用户到不存在的组: " + group);
            return false;
        }

        boolean added = userGroups.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(group);
        if (added) {
            updatePlayerPermissions(user);
        }
        return added;
    }

    public boolean removeUserFromGroup(UUID user, String group) {
        Objects.requireNonNull(user, "用户UUID不能为null");
        Objects.requireNonNull(group, "组名不能为null");

        Set<String> groups = userGroups.get(user);
        if (groups == null) {
            return false;
        }

        boolean removed = groups.remove(group);
        if (removed) {
            if (groups.isEmpty()) {
                userGroups.remove(user);
            }
            updatePlayerPermissions(user);
        }
        return removed;
    }

    public Set<String> getUserGroups(UUID user) {
        Set<String> groups = userGroups.get(user);
        if (groups == null || groups.isEmpty()) {
            return Collections.unmodifiableSet(defaultGroups);
        }
        return Collections.unmodifiableSet(groups);
    }

    public boolean isUserInGroup(UUID user, String group) {
        Set<String> groups = getUserGroups(user);
        return groups.contains(group);
    }

    public boolean hasPermission(UUID user, String permission) {
        if (user == null || permission == null) {
            return false;
        }

        for (String groupName : getUserGroups(user)) {
            PermissionGroup group = plugin.getGroup(groupName);
            if (group != null && group.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public void updatePlayerPermissions(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        PermissionAttachment oldAttachment = permissionAttachments.remove(uuid);
        if (oldAttachment != null) {
            oldAttachment.remove();
        }

        PermissionAttachment attachment = player.addAttachment(plugin);
        permissionAttachments.put(uuid, attachment);

        for (String groupName : getUserGroups(uuid)) {
            PermissionGroup group = plugin.getGroup(groupName);
            if (group != null) {
                group.getPermissions().forEach(perm -> {
                    if (perm != null && !perm.trim().isEmpty()) {
                        attachment.setPermission(perm, true);
                    }
                });
            }
        }
    }

    public void cleanupPlayer(UUID uuid) {
        PermissionAttachment attachment = permissionAttachments.remove(uuid);
        if (attachment != null) {
            attachment.remove();
        }
    }

    public Set<UUID> getAllUsersWithGroups() {
        return Collections.unmodifiableSet(userGroups.keySet());
    }

    public void reload() {
        userGroups.clear();
        permissionAttachments.values().forEach(PermissionAttachment::remove);
        permissionAttachments.clear();
        load();
    }
}