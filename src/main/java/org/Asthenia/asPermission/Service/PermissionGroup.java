package org.Asthenia.asPermission.Service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PermissionGroup {
    private String name;
    private Set<String> permissions = new HashSet<>();
    private Map<String,Boolean> settings = new HashMap<>();
    private File dataFolder;

    public PermissionGroup(String name,File dataFolder){
        this.name = name;
        this.dataFolder = dataFolder;
    }

    // 保存到yml
    public void save() throws IOException {
       File file = new File(dataFolder,"groups/"+name+".yml");
       YamlConfiguration config = new YamlConfiguration();

       // 保存权限列表
        config.set("permissions",new ArrayList<>(permissions));

        //保存设置
        ConfigurationSection settingsSection = config.createSection("settings");
        settings.forEach(settingsSection::set);

        // 确保目录存在
        file.getParentFile().mkdirs();
        config.save(file);
    }

    // 从yml加载
    public static PermissionGroup load(File file,File dataFolder){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String groupName = file.getName().replace(".yml","");
        PermissionGroup group = new PermissionGroup(groupName,dataFolder);

        // 加载数据
        List<String> permissionList = config.getStringList("permissions");
        group.permissions.addAll(permissionList);

        // 加载设置
        ConfigurationSection settingsSection = config.getConfigurationSection("setting");
        if(settingsSection!=null){
            for(String key : settingsSection.getKeys(false)){
                group.settings.put(key,settingsSection.getBoolean(key));
            }
        }
        return group;
    }

    public void addPermission(String permission){
        permissions.add(permission);
    }

    public void removePermission(String permission){
        permissions.remove(permission);
    }

    public boolean hasPermission(String permission){
        return permissions.contains(permission);
    }

    public String getName() { return name; }
    public Set<String> getPermissions() { return Collections.unmodifiableSet(permissions); }
    public Map<String, Boolean> getSettings() { return Collections.unmodifiableMap(settings); }


}
