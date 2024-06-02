package iesmm.pmdm.autolabibscan.Models;

public class ApiResponse {
    private String plate;
    private String error;

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
