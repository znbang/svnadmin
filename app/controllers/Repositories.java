package controllers;

import models.Directory;
import models.Permission;
import models.Repository;
import models.User;
import play.mvc.With;
import play.mvc.results.NotFound;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@With(RequireLogin.class)
public class Repositories extends Controller {
    public static void index() {
        User user = getCurrentUser();
        List<Repository> repositories;
        if (user.isAdmin()) {
            repositories = Repository.findAll();
        } else {
            repositories = Repository.listByManager(user.getId());
        }

        render(repositories);
    }

    static Repository getRepository(Integer id) {
        User user = getCurrentUser();
        Repository repos = user.isAdmin() ? Repository.findById(id) : Repository.findByManager(id, user.getId());
        if (repos != null) {
            return repos;
        }
        throw new NotFound("Repository not found: " + id);
    }

    public static void show(Integer id) {
        Repository repos = getRepository(id);
        List<Directory> dirs = repos.directories.stream()
                .sorted(Comparator.comparing(a -> a.name))
                .collect(Collectors.toList());
        List<User> users = repos.users.stream()
                .sorted(Comparator.comparing(a -> a.displayName))
                .collect(Collectors.toList());
        List<User> others = User.<User>findAll().stream()
                .sorted(Comparator.comparing(a -> a.displayName))
                .collect(Collectors.toList());
        others.removeIf(a -> users.contains(a));
        render(repos, dirs, users, others);
    }

    public static void delete(Integer id) {
        if (!getCurrentUser().isAdmin()) {
            forbidden("Only administrator can delete the repository.");
        }

        Repository repos = Repository.findById(id);
        repos.delete();
        index();
    }

    public static void addDir(Integer id, String name) {
        name = name.trim();

        validation.required("name", name).message("dir_name_is_required");
        if (validation.hasErrors()) {
            show(id);
        }

        Repository repos = getRepository(id);
        repos.addDirectory(name);
        repos.save();

        show(repos.getId());
    }

    public static void editDir(Integer id, Integer dirId) {
        Repository repos = getRepository(id);
        Directory dir = Directory.findById(dirId);
        if (dir == null) {
            notFound();
        }
        Map<String, Permission> currentPerms = dir.permissions.stream()
                .collect(Collectors.toMap(a -> a.userName, a -> a));
        List<Permission> perms = repos.users.stream()
                .sorted(Comparator.comparing(a -> a.userName))
                .map(u -> {
                    Permission p = currentPerms.get(u.userName);
                    if (p == null) {
                        p = new Permission();
                        p.id = 0;
                        p.userName = u.userName;
                        p.permission = "";
                    }
                    p.userId = u.id;
                    p.displayName = u.displayName;
                    return p;
                })
                .collect(Collectors.toList());

        render(repos, dir, perms);
    }

    public static void updateDir(Integer id, Integer dirId, String name) {
        name = name.trim();

        validation.required("name", name).message("dir_name_is_required");
        if (validation.hasErrors()) {
            editDir(id, dirId);
        }

        Repository repos = getRepository(id);
        Directory dir = Directory.findById(dirId);
        if (dir == null) {
            notFound();
        }

        dir.name = name;
        dir.save();

        editDir(id, dirId);
    }

    public static void deleteDir(Integer id, Integer dirId) {
        Repository repos = getRepository(id);
        repos.deleteDirectory(dirId);
        repos.save();

        show(id);
    }

    public static void addUser(Integer id, Integer userId) {
        Repository repos = getRepository(id);
        User user = User.findById(userId);
        if (user == null) {
            notFound();
        }

        repos.addUser(user);
        repos.save();

        show(id);
    }

    public static void removeUser(Integer id, Integer userId) {
        Repository repos = getRepository(id);
        repos.removeUser(userId);
        repos.save();

        show(id);
    }

    public static void updatePermission(Integer id, Integer dirId, Integer userId, String permission) {
        Repository repos = getRepository(id);
        Directory dir = Directory.findById(dirId);
        if (dir == null) {
            notFound("Directory not found: " + dirId);
        }

        User user = User.findById(userId);
        if (user == null) {
            notFound("User not found: " + userId);
        }

        Permission perm = Permission.find("directory.id=:dirId and userName=:userName")
                .setParameter("dirId", dirId)
                .setParameter("userName", user.userName)
                .first();
        if (perm == null) {
            perm = new Permission();
            perm.directory = dir;
            perm.userName = user.userName;
        }
        perm.permission = permission;
        perm.save();

        editDir(id, dirId);
    }

    public static void deletePermission(Integer id, Integer dirId, Integer permId) {
        Repository repos = getRepository(id);
        Directory dir = Directory.findById(dirId);
        if (dir == null) {
            notFound();
        }

        dir.deletePermission(permId);
        dir.save();

        editDir(id, dirId);
    }
}
