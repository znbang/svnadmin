package service;

import models.User;
import play.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserService {
    private final LdapService ldapService = new LdapService();

    public User authenticate(String userName, String password) {
        // 驗證帳號密碼
        User ldapUser = ldapService.authenticate(userName, password);
        if (ldapUser == null) {
            return null;
        }

        // 帳號是否已存入資料庫
        User user = User.findByUserName(userName.toLowerCase());
        if (user != null) {
            return user;
        }

        // 儲存新帳號
        return ldapUser.save();
    }

    public void importUsers() {
        Set<String> ldapUsers = new HashSet<>();
        ldapUsers.add("all");
        for (User user : ldapService.getUsers()) {
            ldapUsers.add(user.userName);
            if (null == User.findByUserName(user.userName)) {
                Logger.info("Created user: %s", user.userName);
                user.save();
            }
        }
        List<User> users = User.findAll();
        users.removeIf(a -> ldapUsers.contains(a.userName));
        users.forEach(a -> {
            Logger.info("Deleted user: %s", a.userName);
            a.delete();
        });
    }
}
