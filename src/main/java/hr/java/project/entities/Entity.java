package hr.java.project.entities;

import hr.java.project.user.User;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Entity implements Serializable {

    private User user;

    private LocalDate dateOfCreation;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDateOfCreation() {
        return dateOfCreation;
    }

    public void setDateOfCreation(LocalDate dateOfCreation) {
        this.dateOfCreation = dateOfCreation;
    }

}
