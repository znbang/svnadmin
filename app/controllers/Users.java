package controllers;

import models.Repository;
import models.User;
import models.UserRole;
import play.mvc.With;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@With(RequireLogin.class)
public class Users extends Controller {
    public static void index() {
        List<User> users = User.listUser();
        List<User> managers = User.listManager();
        List<User> admins = User.listAdmin();
        render(users, managers, admins);
    }

    public static void importUsers() {
        userService.importUsers();

        redirect(request.headers.get("referer").value());
    }

    public static void edit(Integer id) {
        User user = User.findById(id);
        if (user == null) {
            notFound();
        }
        List<Repository> managed = user.managedRepositories.stream()
                .sorted(Comparator.comparing(a -> a.name))
                .collect(Collectors.toList());
        List<Repository> others = Repository.findAll();
        others.removeAll(managed);
        others = others.stream()
                .sorted(Comparator.comparing(a -> a.name))
                .collect(Collectors.toList());
        render(user, managed, others);
    }

    public static void update(Integer id, String role) {
        checkAuthenticity();
        User user = User.findById(id);
        if (user == null) {
            notFound();;
        }
        user.role = UserRole.valueOf(role);
        user.save();

        edit(id);
    }

    public static void addRepository(Integer id, Integer reposId) {
        checkAuthenticity();
        User user = User.findById(id);
        if (user == null) {
            notFound();
        }
        Repository repos = Repository.findById(reposId);
        if (repos == null) {
            notFound();
        }
        repos.addManager(user);
        repos.save();

        edit(id);
    }

    public static void removeRepository(Integer id, Integer reposId) {
        checkAuthenticity();
        User user = User.findById(id);
        if (user == null) {
            notFound();
        }
        Repository repos = Repository.findById(reposId);
        if (repos == null) {
            notFound();
        }
        repos.removeManager(user);
        repos.save();

        edit(id);
    }
}
