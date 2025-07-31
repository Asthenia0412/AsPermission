package org.Asthenia.asPermission.Service;

import org.Asthenia.asPermission.AsPermission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class UserManager {
    private final AsPermission plugin;
    // uuid->对应的权限组列表 ['vip','admin']
    private final Map<UUID, Set<String>> userGroups = new ConcurrentHashMap<>();
    // uuid->对应的权限附件 存储玩家实时的权限附件（用于动态权限控制）
    private final Map<UUID, PermissionAttachment> permissionAttachments = new ConcurrentHashMap<>();

    // 权限组->角色名的映射->角色名和uuid可以是关联的
    private final Map<String,List<String>> groupPlayer= new ConcurrentHashMap<>();

    private final File dataFile;
    private final Set<String> defaultGroups = ConcurrentHashMap.newKeySet();

    public UserManager(AsPermission plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "users.yml");
        updateDefaultGroups();
    }

    public Map<String,List<String>> getGroupPlayers(){
        return groupPlayer;
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

        // 1. 加载用户-组数据
        for (String key : config.getKeys(false)) {
            if (key.equals("group-members")) {
                continue; // 跳过组-玩家映射部分
            }

            try {
                UUID uuid = UUID.fromString(key);
                List<String> groups = config.getStringList(key);

                if (!groups.isEmpty()) {
                    userGroups.put(uuid, ConcurrentHashMap.newKeySet());
                    userGroups.get(uuid).addAll(groups);
                    loadedCount++;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的UUID格式: " + key);
            }
        }

        // 2. 加载组-玩家映射
        ConfigurationSection groupSection = config.getConfigurationSection("group-members");
        if (groupSection != null) {
            for (String groupName : groupSection.getKeys(false)) {
                List<String> players = groupSection.getStringList(groupName);
                groupPlayer.put(groupName, new CopyOnWriteArrayList<>(players));
            }
        }

        plugin.getLogger().info(String.format(
                "已加载 %d 个用户和 %d 个组的成员数据 (耗时: %dms)",
                loadedCount,
                groupPlayer.size(),
                System.currentTimeMillis() - startTime
        ));
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        // 1. 保存用户-组数据
        userGroups.forEach((uuid, groups) ->
                config.set(uuid.toString(), new ArrayList<>(groups))
        );

        // 2. 保存组-玩家映射
        ConfigurationSection groupSection = config.createSection("group-members");
        groupPlayer.forEach((groupName, players) -> {
            // 去重后保存
            List<String> distinctPlayers = new ArrayList<>(new LinkedHashSet<>(players));
            groupSection.set(groupName, distinctPlayers);
        });

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存用户数据时发生错误", e);
        }
    }

    public boolean addUserToGroup(String userName, UUID user, String group) {
        Objects.requireNonNull(user, "用户UUID不能为null");
        Objects.requireNonNull(group, "组名不能为null");

        if (plugin.getGroup(group) == null) {
            plugin.getLogger().warning("尝试添加用户到不存在的组: " + group);
            return false;
        }

        // 添加到用户-组映射
        boolean added = userGroups.computeIfAbsent(user, k -> ConcurrentHashMap.newKeySet()).add(group);

        // 添加到组-玩家映射
        groupPlayer.computeIfAbsent(group, k -> new CopyOnWriteArrayList<>()).add(userName);

        if (added) {
            updatePlayerPermissions(user);
        }
        return added;
    }

    public boolean removeUserFromGroup(String userName, UUID user, String group) {
        Objects.requireNonNull(user, "用户UUID不能为null");
        Objects.requireNonNull(group, "组名不能为null");

        Set<String> groups = userGroups.get(user);
        if (groups == null) {
            return false;
        }

        // 从组-玩家映射移除
        List<String> players = groupPlayer.get(group);
        if (players != null) {
            players.remove(userName);
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