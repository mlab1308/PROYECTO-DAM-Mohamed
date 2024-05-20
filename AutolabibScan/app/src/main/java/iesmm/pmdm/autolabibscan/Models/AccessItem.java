package iesmm.pmdm.autolabibscan.Models;

public class AccessItem {
    private String email;
    private String date;

    public AccessItem(String email, String date) {
        this.email = email;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }
}
