package com.example.el_mozote.statistics;

import com.example.el_mozote.vehicles.Vehicle;

import java.io.Serializable;
import java.util.Locale;

public class VehicleStat implements Serializable {

    private long vehicleId;
    private String brand;
    private String model;
    private int year;
    private String plate;
    private String color;
    private double pricePerDay;
    private String currentStatus;
    private String firstPhotoPath;

    // Métricas operativas y económicas
    private int totalRentals;
    private int completedRentals;
    private int activeRentals;
    private int reservedRentals;

    private double totalRevenue; // Total cobrado / recaudado
    private double totalBilled;  // Total facturado por contratos
    private double pendingBalance; // Saldo pendiente

    private int totalDaysRented;
    private int totalKmDriven;

    private double avgRevenuePerRental;
    private double rentalFrequency; // rentas por mes o por período
    private double utilizationRate; // % de días en renta

    // Factibilidad económica y diagnóstico
    private String feasibilityBadge; // "Alta Rentabilidad", "Rendimiento Moderado", "Bajo Rendimiento", "Subutilizado"
    private String feasibilityVerdict;
    private String feasibilityRecommendation;
    private double roiIndex; // Índice de rendimiento relativo

    public VehicleStat(Vehicle vehicle) {
        if (vehicle != null) {
            this.vehicleId = vehicle.getId();
            this.brand = vehicle.getBrand();
            this.model = vehicle.getModel();
            this.year = vehicle.getYear();
            this.plate = vehicle.getPlate();
            this.color = vehicle.getColor();
            this.pricePerDay = vehicle.getPricePerDay();
            this.currentStatus = vehicle.getStatus();
            if (vehicle.getPhotoPaths() != null && !vehicle.getPhotoPaths().isEmpty()) {
                this.firstPhotoPath = vehicle.getPhotoPaths().get(0);
            }
        }
    }

    public long getVehicleId() {
        return vehicleId;
    }

    public String getBrand() {
        return brand != null ? brand : "";
    }

    public String getModel() {
        return model != null ? model : "";
    }

    public String getTitle() {
        return getBrand() + " " + getModel() + " (" + year + ")";
    }

    public int getYear() {
        return year;
    }

    public String getPlate() {
        return plate != null ? plate : "";
    }

    public String getColor() {
        return color != null ? color : "";
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public String getCurrentStatus() {
        return currentStatus != null ? currentStatus : Vehicle.STATUS_AVAILABLE;
    }

    public String getFirstPhotoPath() {
        return firstPhotoPath;
    }

    public int getTotalRentals() {
        return totalRentals;
    }

    public void setTotalRentals(int totalRentals) {
        this.totalRentals = totalRentals;
    }

    public int getCompletedRentals() {
        return completedRentals;
    }

    public void setCompletedRentals(int completedRentals) {
        this.completedRentals = completedRentals;
    }

    public int getActiveRentals() {
        return activeRentals;
    }

    public void setActiveRentals(int activeRentals) {
        this.activeRentals = activeRentals;
    }

    public int getReservedRentals() {
        return reservedRentals;
    }

    public void setReservedRentals(int reservedRentals) {
        this.reservedRentals = reservedRentals;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalBilled() {
        return totalBilled;
    }

    public void setTotalBilled(double totalBilled) {
        this.totalBilled = totalBilled;
    }

    public double getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(double pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public int getTotalDaysRented() {
        return totalDaysRented;
    }

    public void setTotalDaysRented(int totalDaysRented) {
        this.totalDaysRented = totalDaysRented;
    }

    public int getTotalKmDriven() {
        return totalKmDriven;
    }

    public void setTotalKmDriven(int totalKmDriven) {
        this.totalKmDriven = totalKmDriven;
    }

    public double getAvgRevenuePerRental() {
        return avgRevenuePerRental;
    }

    public void setAvgRevenuePerRental(double avgRevenuePerRental) {
        this.avgRevenuePerRental = avgRevenuePerRental;
    }

    public double getRentalFrequency() {
        return rentalFrequency;
    }

    public void setRentalFrequency(double rentalFrequency) {
        this.rentalFrequency = rentalFrequency;
    }

    public double getUtilizationRate() {
        return utilizationRate;
    }

    public void setUtilizationRate(double utilizationRate) {
        this.utilizationRate = utilizationRate;
    }

    public String getFeasibilityBadge() {
        return feasibilityBadge != null ? feasibilityBadge : "Rendimiento Moderado";
    }

    public void setFeasibilityBadge(String feasibilityBadge) {
        this.feasibilityBadge = feasibilityBadge;
    }

    public String getFeasibilityVerdict() {
        return feasibilityVerdict != null ? feasibilityVerdict : "";
    }

    public void setFeasibilityVerdict(String feasibilityVerdict) {
        this.feasibilityVerdict = feasibilityVerdict;
    }

    public String getFeasibilityRecommendation() {
        return feasibilityRecommendation != null ? feasibilityRecommendation : "";
    }

    public void setFeasibilityRecommendation(String feasibilityRecommendation) {
        this.feasibilityRecommendation = feasibilityRecommendation;
    }

    public double getRoiIndex() {
        return roiIndex;
    }

    public void setRoiIndex(double roiIndex) {
        this.roiIndex = roiIndex;
    }

    // Formatted strings
    public String getFormattedRevenue() {
        return String.format(Locale.US, "$%.2f", totalRevenue);
    }

    public String getFormattedBilled() {
        return String.format(Locale.US, "$%.2f", totalBilled);
    }

    public String getFormattedPending() {
        return String.format(Locale.US, "$%.2f", pendingBalance);
    }

    public String getFormattedAvgRevenue() {
        return String.format(Locale.US, "$%.2f", avgRevenuePerRental);
    }

    public String getFormattedDailyRate() {
        return String.format(Locale.US, "$%.2f/día", pricePerDay);
    }

    public String getFormattedUtilization() {
        return String.format(Locale.US, "%.1f%%", utilizationRate);
    }

    public String getFormattedKm() {
        return String.format(Locale.US, "%,d km", totalKmDriven);
    }
}
