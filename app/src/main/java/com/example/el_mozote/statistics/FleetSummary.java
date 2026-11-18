package com.example.el_mozote.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FleetSummary implements Serializable {

    private int totalVehicles;
    private int availableVehicles;
    private int rentedVehicles;
    private int maintenanceVehicles;

    private int totalRentals;
    private double totalRevenue;
    private double totalPending;
    private double totalBilled;

    private int totalDaysRented;
    private int totalKmDriven;
    private double fleetUtilizationPercent;

    private VehicleStat topRevenueVehicle;
    private VehicleStat lowestRevenueVehicle;
    private VehicleStat highestFrequencyVehicle;
    private VehicleStat lowestUtilizationVehicle;

    private final List<VehicleStat> vehicleStats = new ArrayList<>();
    private TimePeriodFilter periodFilter = TimePeriodFilter.ALL;

    public int getTotalVehicles() {
        return totalVehicles;
    }

    public void setTotalVehicles(int totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public int getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public int getRentedVehicles() {
        return rentedVehicles;
    }

    public void setRentedVehicles(int rentedVehicles) {
        this.rentedVehicles = rentedVehicles;
    }

    public int getMaintenanceVehicles() {
        return maintenanceVehicles;
    }

    public void setMaintenanceVehicles(int maintenanceVehicles) {
        this.maintenanceVehicles = maintenanceVehicles;
    }

    public int getTotalRentals() {
        return totalRentals;
    }

    public void setTotalRentals(int totalRentals) {
        this.totalRentals = totalRentals;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(double totalPending) {
        this.totalPending = totalPending;
    }

    public double getTotalBilled() {
        return totalBilled;
    }

    public void setTotalBilled(double totalBilled) {
        this.totalBilled = totalBilled;
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

    public double getFleetUtilizationPercent() {
        return fleetUtilizationPercent;
    }

    public void setFleetUtilizationPercent(double fleetUtilizationPercent) {
        this.fleetUtilizationPercent = fleetUtilizationPercent;
    }

    public VehicleStat getTopRevenueVehicle() {
        return topRevenueVehicle;
    }

    public void setTopRevenueVehicle(VehicleStat topRevenueVehicle) {
        this.topRevenueVehicle = topRevenueVehicle;
    }

    public VehicleStat getLowestRevenueVehicle() {
        return lowestRevenueVehicle;
    }

    public void setLowestRevenueVehicle(VehicleStat lowestRevenueVehicle) {
        this.lowestRevenueVehicle = lowestRevenueVehicle;
    }

    public VehicleStat getHighestFrequencyVehicle() {
        return highestFrequencyVehicle;
    }

    public void setHighestFrequencyVehicle(VehicleStat highestFrequencyVehicle) {
        this.highestFrequencyVehicle = highestFrequencyVehicle;
    }

    public VehicleStat getLowestUtilizationVehicle() {
        return lowestUtilizationVehicle;
    }

    public void setLowestUtilizationVehicle(VehicleStat lowestUtilizationVehicle) {
        this.lowestUtilizationVehicle = lowestUtilizationVehicle;
    }

    public List<VehicleStat> getVehicleStats() {
        return vehicleStats;
    }

    public void setVehicleStats(List<VehicleStat> stats) {
        this.vehicleStats.clear();
        if (stats != null) {
            this.vehicleStats.addAll(stats);
        }
    }

    public TimePeriodFilter getPeriodFilter() {
        return periodFilter;
    }

    public void setPeriodFilter(TimePeriodFilter periodFilter) {
        this.periodFilter = periodFilter;
    }

    public String getFormattedRevenue() {
        return String.format(Locale.US, "$%.2f", totalRevenue);
    }

    public String getFormattedPending() {
        return String.format(Locale.US, "$%.2f", totalPending);
    }

    public String getFormattedBilled() {
        return String.format(Locale.US, "$%.2f", totalBilled);
    }

    public String getFormattedUtilization() {
        return String.format(Locale.US, "%.1f%%", fleetUtilizationPercent);
    }

    public String getFormattedTotalKm() {
        return String.format(Locale.US, "%,d km", totalKmDriven);
    }
}
