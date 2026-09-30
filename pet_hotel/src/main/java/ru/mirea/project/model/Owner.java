package ru.mirea.project.model;

public class Owner {
    private Long id;
    private String login;
    private String fullName;
    private String phone;


    public Owner(String login, String fullName, String phone) {
        this(null, login, fullName, phone);
    }

    public Owner(Long id, String login, String fullName, String phone) {
        this.id = id;
        this.login = login;
        this.fullName = fullName;
        this.phone = phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "[ ID: " + id + ", login: " + login + ", имя: " + fullName + ", телефон: " + phone + "]";
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }
}
