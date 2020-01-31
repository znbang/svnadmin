package controllers;

import play.mvc.Before;

public class RequireLogin extends Controller {
    @Before
    static void requireLogin() {
        if (getCurrentUser() == null) {
            Authenticate.login();
        }
    }
}
