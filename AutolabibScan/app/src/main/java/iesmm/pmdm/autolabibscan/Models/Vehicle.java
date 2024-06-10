package iesmm.pmdm.autolabibscan.Models;

public class Vehicle {
    private String brand;
    private String plateText;
    private int owners;
    private String power;
    private String fuel;
    private String bastidor;
    private String registeringAuthority;
    private String fuelType;
    private String emissionNorm;
    private String manufacturingDate;
    private boolean vehicleStatus;
    private String imageUrl;

    public Vehicle() {
        // Constructor vacío necesario para Firebase
    }

    // Getters and setters

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPlateText() {
        return plateText;
    }

    public void setPlateText(String plateText) {
        this.plateText = plateText;
    }

    public int getOwners() {
        return owners;
    }

    public void setOwners(int owners) {
        this.owners = owners;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public String getBastidor() {
        return bastidor;
    }

    public void setBastidor(String bastidor) {
        this.bastidor = bastidor;
    }

    public String getRegisteringAuthority() {
        return registeringAuthority;
    }

    public void setRegisteringAuthority(String registeringAuthority) {
        this.registeringAuthority = registeringAuthority;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getEmissionNorm() {
        return emissionNorm;
    }

    public void setEmissionNorm(String emissionNorm) {
        this.emissionNorm = emissionNorm;
    }

    public String getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(String manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public boolean isVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(boolean vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
