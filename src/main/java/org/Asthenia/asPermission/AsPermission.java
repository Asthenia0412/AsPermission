package org.Asthenia.asPermission;

import org.Asthenia.asPermission.Commands.PermissionCommand;
import org.Asthenia.asPermission.OpenAPI.AsPermissionAPI;
import org.Asthenia.asPermission.Service.PermissionGroup;
import org.Asthenia.asPermission.Service.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class AsPermission extends JavaPlugin {
    private PermissionCommand permissionCommand;
    private UserManager userManager;
    private Map<String, PermissionGroup> groups = new HashMap<>();
    private File groupsFolder;
    private File configFile;

    @Override
    public void onEnable() {
        // 0. 先公开API
        AsPermissionAPI.initialize(this);
        // 1. 先初始化数据目录
        if (!initializeDataFolders()) {
            getLogger().severe("无法初始化数据目录，插件已禁用!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // 2. 加载配置（必须在其他初始化之前）
        loadConfig();

        // 3. 初始化用户管理器
        this.userManager = new UserManager(this); // 注意这里传参应该是 this 而不是 AsPermission

        try {
            // 4. 加载数据
            reloadAllData();

            // 5. 注册命令
            this.permissionCommand = new PermissionCommand(this);
            registerCommands();

            getLogger().info("插件加载完成! 版本: " + getDescription().getVersion());
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "数据加载失败", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private boolean initializeDataFolders() {
        try {
            // 主数据目录
            File dataFolder = getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                getLogger().severe("无法创建插件数据目录!");
                return false;
            }

            // 权限组目录
            groupsFolder = new File(dataFolder, "groups");
            if (!groupsFolder.exists() && !groupsFolder.mkdirs()) {
                getLogger().severe("无法创建权限组目录!");
                return false;
            }

            // 配置文件
            configFile = new File(dataFolder, "config.yml");
            return true;
        } catch (SecurityException e) {
            getLogger().log(Level.SEVERE, "无法创建必要的目录结构", e);
            return false;
        }
    }

    private void loadConfig() {
        // 如果配置文件不存在，从JAR中保存默认配置

        // 相当于一层保险 假设数据目录中没有config.yml文件，为了避免插件出错 我需要额外创建config.yml
        if (!configFile.exists()) {
            saveResource("config.yml", false); // 第二个参数表示不覆盖已有文件
        }
        reloadConfig();
    }

    public void reloadAllData() throws IOException {
        groups.clear();
        loadAllGroups();
        userManager.load();
    }

    private void registerCommands() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // 注册主命令
            registerCommand(commandMap, "perm", (sender, command, label, args) ->
                    permissionCommand.execute(sender, args));

            // 注册快捷命令别名
            registerCommand(commandMap, "asperm", (sender, command, label, args) ->
                    permissionCommand.execute(sender, args));

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "命令注册失败", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommand(CommandMap commandMap, String name, CommandExecutor executor) {
        Command command = new Command(name) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                if (!sender.hasPermission("asperm.use")) {
                    sender.sendMessage("§c你没有权限使用此命令!");
                    return false;
                }
                return executor.onCommand(sender, this, label, args);
            }
        };
        commandMap.register(name, "asperm", command);
    }

    // 加载所有权限组
    private void loadAllGroups() throws IOException {
        File[] groupFiles = groupsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (groupFiles != null) {
            for (File file : groupFiles) {
                PermissionGroup group = PermissionGroup.load(file, getDataFolder());
                groups.put(group.getName(), group);
            }
        }
    }

    // 获取或创建权限组
    public PermissionGroup getOrCreateGroup(String name) throws IOException {
        PermissionGroup group = groups.get(name);
        if (group == null) {
            group = new PermissionGroup(name, getDataFolder());
            groups.put(name, group);
            group.save();
        }
        return group;
    }

    // 删除权限组
    public boolean deleteGroup(String name) {
        PermissionGroup group = groups.remove(name);
        if (group != null) {
            File file = new File(groupsFolder, name + ".yml");
            if (file.exists()) {
                return file.delete();
            }
        }
        return false;
    }

    // 获取权限组
    public PermissionGroup getGroup(String name) {
        return groups.get(name);
    }

    // 获取所有权限组(不可修改视图)
    public Map<String, PermissionGroup> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    // 保存所有数据
    public void saveAll() throws IOException {
        for (PermissionGroup group : groups.values()) {
            try {
                group.save();
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "保存权限组失败: " + group.getName(), e);
                throw e;
            }
        }

        userManager.save();
    }

    // 获取用户管理器
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public void onDisable() {
        if (userManager != null) { // 添加null检查
            try {
                saveAll();
                getLogger().info("插件数据已保存");
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "保存数据失败", e);
            }
        } else {
            getLogger().warning("userManager为null，跳过保存");
        }
    }

    public String getDefaultGroup() {
        return "default";
    }
    public Map<String, List<String>> getGroupPlayers(){
        return userManager.getGroupPlayers();
    }

    @FunctionalInterface
    private interface CommandExecutor {
        boolean onCommand(CommandSender sender, Command command, String label, String[] args);
    }
}