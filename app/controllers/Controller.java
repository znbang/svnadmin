package controllers;

import models.User;
import play.cache.Cache;
import play.mvc.Before;
import service.UserService;

public class Controller extends play.mvc.Controller {
    static final UserService userService = new UserService();

    @Before
    static void initCurrentUser() {
        renderArgs.put("currentUser", getCurrentUser());
    }

    static User getCurrentUser() {
        return Cache.get(session.getId(), User.class);
    }

    static void setCurrentUser(User user) {
        Cache.set(session.getId(), user);
    }

    static void resetCurrentUser() {
        Cache.delete(session.getId());
        session.clear();
    }
}
