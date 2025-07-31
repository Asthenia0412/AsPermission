package org.Asthenia.asPermission;

import org.Asthenia.asPermission.Commands.PermissionCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public final class AsPermission extends JavaPlugin {
    private PermissionCommand permissionCommand;

    @Override
    public void onEnable() {
        this.permissionCommand = new PermissionCommand(this);
        registerCommands();
    }

    private void registerCommands() {
        try {
            // 通过反射获取Bukkit的CommandMap
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // 注册/perm命令
            registerCommand(commandMap, "perm", (sender, command, label, args) -> {
                return permissionCommand.execute(sender, args);
            });

        } catch (Exception e) {
            getLogger().severe("命令注册失败: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommand(CommandMap commandMap, String name, CommandExecutor executor) {
        Command command = new Command(name) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
                return executor.onCommand(sender, this, label, args);
            }
        };
        commandMap.register(name, "asperm", command);
    }

    @FunctionalInterface
    private interface CommandExecutor {
        boolean onCommand(CommandSender sender, Command command, String label, String[] args);
    }

    @Override
    public void onDisable() {
        // 清理逻辑
    }
}