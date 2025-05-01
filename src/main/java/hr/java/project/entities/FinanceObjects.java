package hr.java.project.entities;

import hr.java.project.LoginStartApplication;

import java.io.Serializable;
import java.time.LocalDate;

public abstract non-sealed class FinanceObjects<T extends Number> extends Entity implements Financial, Serializable {

    private String categoryName;
    private T amount;

    public FinanceObjects(String categoryName, T amount) {
        this.categoryName = categoryName;
        this.amount = amount;
        super.setDateOfCreation(LocalDate.now());
        if(LoginStartApplication.currentUser.isPresent()){
            super.setUser(LoginStartApplication.currentUser.get());
        }
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public T getAmount() {
        return amount;
    }

    public void setAmount(T amount) {
        this.amount = amount;
    }
}
