package controllers;

import models.Repository;
import play.mvc.With;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;

import static java.util.stream.Collectors.toList;

@With(RequireLogin.class)
public class Permissions extends Controller {
    public static void export() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Repository repos : Repository.<Repository>findAll().stream().sorted(Comparator.comparing(a -> a.name)).collect(toList())) {
            sb.append(repos.toRule());
        }
        Files.writeString(Paths.get("/home/svn/svn.access"), sb.toString());

        redirect(request.headers.get("referer").value());
    }
}
