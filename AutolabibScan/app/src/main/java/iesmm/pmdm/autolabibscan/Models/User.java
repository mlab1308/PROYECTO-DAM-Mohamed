package iesmm.pmdm.autolabibscan.Models;


public class User {
    public String name;
    public String email;
    public String role;

    public User() {
        // Constructor vacío requerido para Firebase
    }

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
    public User(String email, String role) {

        this.email = email;
        this.role = role;
    }
}