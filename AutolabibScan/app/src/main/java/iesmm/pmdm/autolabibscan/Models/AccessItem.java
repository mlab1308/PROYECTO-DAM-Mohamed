package iesmm.pmdm.autolabibscan.Models;

import java.util.Date;

public class AccessItem {
    private String email;
    private Date timestamp;

    // Constructor vacío requerido por Firebase
    public AccessItem() {
    }

    // Constructor con parámetros
    public AccessItem(String email, Date timestamp) {
        this.email = email;
        this.timestamp = timestamp;
    }

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
