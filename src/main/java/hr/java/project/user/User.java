package hr.java.project.user;

import hr.java.project.enumerations.UserRoles;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record User(Integer id, String name, String email, String userName, UserRoles role) implements Serializable{

    public User(Integer id, String name, String email, String userName, UserRoles role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.userName = userName;
        this.role = role;
    }
}
