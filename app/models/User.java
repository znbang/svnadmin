package models;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Entity
@Table(name = "users")
public class User extends Model {
    public String userName;
    public String displayName;
    @Enumerated(EnumType.STRING)
    public UserRole role;
    @ManyToMany(mappedBy = "managers")
    public Set<Repository> managedRepositories;
    @ManyToMany(mappedBy = "users")
    public Set<Repository> repositories;

    public User() {}

    public User(String userName, String displayName, UserRole role) {
        this.userName = userName.toLowerCase();
        this.displayName = displayName;
        this.role = role;
    }

    public boolean isAdmin() {
        return role == UserRole.admin;
    }

    public static User findByUserName(String userName) {
        return find("userName=:userName")
                .setParameter("userName", userName)
                .first();
    }

    public static List<User> listByRole(UserRole role) {
        return find("role=:role")
                .setParameter("role", role)
                .<User>fetch()
                .stream()
                .sorted(Comparator.comparing(a -> a.displayName))
                .collect(toList());
    }

    public static List<User> listUser() {
        return listByRole(UserRole.user);
    }

    public static List<User> listManager() {
        return listByRole(UserRole.cm);
    }

    public static List<User> listAdmin() {
        return listByRole(UserRole.admin);
    }
}
