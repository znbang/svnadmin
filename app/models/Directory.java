package models;

import javax.persistence.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "directories")
public class Directory extends Model {
    public String name;
    @ManyToOne
    public Repository repository;
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<Permission> permissions;

    public Directory() {}

    public Directory(Repository repository, String name) {
        this.repository = repository;
        this.name = name;
    }

    public void deletePermission(Integer permId) {
        this.permissions.removeIf(a -> a.id.equals(permId));
    }

    public String toRule() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(repository.name).append(":").append(name).append("]\n");
        Optional<Permission> all = permissions.stream()
                .filter(a -> a.userName.equals("all"))
                .findFirst();
        if (all.isPresent()) {
            sb.append(all.get().toRule()).append("\n");
        } else {
            sb.append("* =\n");
        }
        permissions.stream()
                .filter(a -> !a.userName.equals("all"))
                .sorted(Comparator.comparing(a -> a.userName))
                .forEach(a -> sb.append(a.toRule()).append("\n"));
        return sb.toString();
    }
}
