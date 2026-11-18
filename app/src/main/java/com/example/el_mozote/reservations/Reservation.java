package com.example.el_mozote.reservations;

import java.io.Serializable;
import java.util.Locale;

public class Reservation implements Serializable {

    public static final String STATUS_RESERVED = "Reservada";
    public static final String STATUS_ACTIVE = "Activa";
    public static final String STATUS_COMPLETED = "Finalizada";
    public static final String STATUS_CANCELLED = "Cancelada";

    private long id;
    private long vehicleId;
    private String vehicleTitle;
    private String vehiclePlate;
    private double vehiclePricePerDay;
    private String customerName;
    private String customerDui;
    private String customerLicense;
    private String customerPhone;
    private String customerAddress;
    private String startDate;
    private String endDate;
    private double dailyRate;
    private int totalDays;
    private double totalCost;
    private String observations;
    private String status;
    private long createdAt;
    private int initialMileage;
    private String initialFuelLevel;

    public Reservation() {
        this.status = STATUS_RESERVED;
        this.createdAt = System.currentTimeMillis();
        this.initialMileage = 0;
        this.initialFuelLevel = "Lleno (4/4)";
    }

    public Reservation(long id, long vehicleId, String vehicleTitle, String vehiclePlate,
                       double vehiclePricePerDay, String customerName, String customerDui,
                       String customerLicense, String customerPhone, String customerAddress,
                       String startDate, String endDate, double dailyRate, int totalDays,
                       double totalCost, String observations, String status, long createdAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.vehicleTitle = vehicleTitle;
        this.vehiclePlate = vehiclePlate;
        this.vehiclePricePerDay = vehiclePricePerDay;
        this.customerName = customerName;
        this.customerDui = customerDui;
        this.customerLicense = customerLicense;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyRate = dailyRate;
        this.totalDays = totalDays;
        this.totalCost = totalCost;
        this.observations = observations;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleTitle() {
        return vehicleTitle != null ? vehicleTitle : "";
    }

    public void setVehicleTitle(String vehicleTitle) {
        this.vehicleTitle = vehicleTitle;
    }

    public String getVehiclePlate() {
        return vehiclePlate != null ? vehiclePlate : "";
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public double getVehiclePricePerDay() {
        return vehiclePricePerDay;
    }

    public void setVehiclePricePerDay(double vehiclePricePerDay) {
        this.vehiclePricePerDay = vehiclePricePerDay;
    }

    public String getCustomerName() {
        return customerName != null ? customerName : "";
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerDui() {
        return customerDui != null ? customerDui : "";
    }

    public void setCustomerDui(String customerDui) {
        this.customerDui = customerDui;
    }

    public String getCustomerLicense() {
        return customerLicense != null ? customerLicense : "";
    }

    public void setCustomerLicense(String customerLicense) {
        this.customerLicense = customerLicense;
    }

    public String getCustomerPhone() {
        return customerPhone != null ? customerPhone : "";
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return customerAddress != null ? customerAddress : "";
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getStartDate() {
        return startDate != null ? startDate : "";
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate != null ? endDate : "";
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public int getTotalDays() {
        return totalDays > 0 ? totalDays : 1;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getObservations() {
        return observations != null ? observations : "";
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getStatus() {
        return status != null ? status : STATUS_RESERVED;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getInitialMileage() {
        return initialMileage;
    }

    public void setInitialMileage(int initialMileage) {
        this.initialMileage = Math.max(0, initialMileage);
    }

    public String getInitialFuelLevel() {
        return initialFuelLevel != null ? initialFuelLevel : "Lleno (4/4)";
    }

    public void setInitialFuelLevel(String initialFuelLevel) {
        this.initialFuelLevel = initialFuelLevel;
    }

    public String getFormattedTotalCost() {
        return String.format(Locale.US, "$%.2f", totalCost);
    }

    public String getFormattedDailyRate() {
        return String.format(Locale.US, "$%.2f / día", dailyRate);
    }
}
