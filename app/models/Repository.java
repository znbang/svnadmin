package models;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "repositories")
public class Repository extends Model {
    public String name;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Directory> directories;
    @ManyToMany
    @JoinTable(
            name = "repository_users",
            joinColumns = @JoinColumn(name = "repository_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> users;
    @ManyToMany
    @JoinTable(
            name = "managerships",
            joinColumns = @JoinColumn(name = "repository_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public Set<User> managers;

    public void addUser(User user) {
        this.users.add(user);
        user.repositories.add(this);
    }

    public void removeUser(Integer userId) {
        this.users.removeIf(u -> u.id.equals(userId));
    }

    public void addManager(User user) {
        this.managers.add(user);
        user.managedRepositories.add(this);
    }

    public void removeManager(User user) {
        this.managers.remove(user);
        user.managedRepositories.remove(this);
    }

    public void addDirectory(String name) {
        for (Directory dir : this.directories) {
            if (dir.name.equals(name)) {
                return;
            }
        }
        this.directories.add(new Directory(this, name));
    }

    public void deleteDirectory(Integer dirId) {
        this.directories.removeIf(dir ->
                dir.id.equals(dirId)
        );
    }

    public static List<Repository> listByManager(Integer userId) {
        return find("users.id=:userId")
                .setParameter("userId", userId)
                .fetch();
    }

    public static Repository findByManager(Integer id, Integer userId) {
        return find("id=:id and users.id=:userId")
                .setParameter("id", id)
                .setParameter("userId", userId)
                .first();
    }

    public String toRule() {
        StringBuilder sb = new StringBuilder();
        directories.stream().sorted(Comparator.comparing(a -> a.name)).forEach(a -> sb.append(a.toRule()).append("\n"));
        return sb.toString();
    }
}
