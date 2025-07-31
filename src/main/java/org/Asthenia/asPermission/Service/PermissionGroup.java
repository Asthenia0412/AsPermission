package org.Asthenia.asPermission.Service;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PermissionGroup {
    private String name;
    private Set<String> permissions = new HashSet<>();
    private Map<String,Boolean> settings = new HashMap<>();

    // 保存到yml
    public void save(File file) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        config.set("permissions",new ArrayList<>(permissions));
        config.set("settings",settings);
        config.save(file);
    }

    // 从yml加载
    public static PermissionGroup load(File file){
        //  实现加载逻辑
    }
}
