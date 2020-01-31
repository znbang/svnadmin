package controllers;

import models.Repository;

import java.util.List;

public class Application extends Controller {
    public static void index() {
        List<Repository> repositories = Repository.findAll();
        render(repositories);
    }
}