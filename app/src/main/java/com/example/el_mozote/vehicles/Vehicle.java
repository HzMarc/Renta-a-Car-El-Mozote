package com.example.el_mozote.vehicles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Vehicle implements Serializable {

    public static final String STATUS_AVAILABLE = "Disponible";
    public static final String STATUS_RENTED = "Rentado";
    public static final String STATUS_MAINTENANCE = "En mantenimiento";

    private long id;
    private String brand;
    private String model;
    private int year;
    private String plate;
    private String color;
    private double pricePerDay;
    private String status;
    private String description;
    private List<String> photoPaths;
    private long createdAt;

    public Vehicle() {
        this.photoPaths = new ArrayList<>();
        this.status = STATUS_AVAILABLE;
        this.createdAt = System.currentTimeMillis();
    }

    public Vehicle(long id, String brand, String model, int year, String plate,
                   String color, double pricePerDay, String status, String description,
                   List<String> photoPaths, long createdAt) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.plate = plate;
        this.color = color;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.description = description;
        this.photoPaths = (photoPaths != null) ? new ArrayList<>(photoPaths) : new ArrayList<>();
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand != null ? brand : "";
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model != null ? model : "";
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPlate() {
        return plate != null ? plate : "";
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getColor() {
        return color != null ? color : "";
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getStatus() {
        return status != null ? status : STATUS_AVAILABLE;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPhotoPaths() {
        if (photoPaths == null) {
            photoPaths = new ArrayList<>();
        }
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = (photoPaths != null) ? new ArrayList<>(photoPaths) : new ArrayList<>();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean hasPhotos() {
        return photoPaths != null && !photoPaths.isEmpty();
    }

    public String getFirstPhotoPath() {
        if (hasPhotos()) {
            return photoPaths.get(0);
        }
        return null;
    }

    public String getTitle() {
        return getBrand() + " " + getModel();
    }

    public String getFormattedPrice() {
        return String.format(Locale.US, "$%.2f / día", pricePerDay);
    }
}
