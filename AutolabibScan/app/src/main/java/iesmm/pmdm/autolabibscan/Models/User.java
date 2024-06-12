package iesmm.pmdm.autolabibscan.Models;

public class User {
    public String name;
    public String lastName;
    public String email;
    public String role;
    public String profileImageUrl;

    public User() {
        // Constructor vacío requerido para Firebase
    }

    public User(String Name, String lastName, String email, String role, String profileImageUrl) {
        this.name = Name;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String Name, String lastName, String email, String role) {
        this.name = Name;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
