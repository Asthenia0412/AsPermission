package org.Asthenia.asPermission.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UserManager {
    private Map<UUID, Set<String>> userGroups = new HashMap<>();

    public void addUserToGroup(UUID user,String group){
        // 实现添加逻辑
    }

    public boolean hasPermission(UUID user,String permission){
        // 实现权限检查
        return true;
    }
}
