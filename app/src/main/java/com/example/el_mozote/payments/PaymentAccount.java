package com.example.el_mozote.payments;

import java.io.Serializable;
import java.util.Locale;

public class PaymentAccount implements Serializable {

    public static final String STATUS_PENDING = "Pendiente";
    public static final String STATUS_PARTIAL = "Pago parcial";
    public static final String STATUS_PAID = "Pagado";

    private long id;
    private long reservationId;
    private String contractNumber;
    private String customerName;
    private String customerDui;
    private String customerPhone;
    private String vehicleTitle;
    private String vehiclePlate;
    private double totalRentCost;
    private double totalPaid;
    private double pendingBalance;
    private String paymentStatus;
    private long createdAt;
    private long updatedAt;

    public PaymentAccount() {
        this.paymentStatus = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public PaymentAccount(long id, long reservationId, String contractNumber, String customerName,
                          String customerDui, String customerPhone, String vehicleTitle,
                          String vehiclePlate, double totalRentCost, double totalPaid,
                          double pendingBalance, String paymentStatus, long createdAt, long updatedAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.contractNumber = contractNumber;
        this.customerName = customerName;
        this.customerDui = customerDui;
        this.customerPhone = customerPhone;
        this.vehicleTitle = vehicleTitle;
        this.vehiclePlate = vehiclePlate;
        this.totalRentCost = totalRentCost;
        this.totalPaid = totalPaid;
        this.pendingBalance = pendingBalance;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getContractNumber() {
        return contractNumber != null ? contractNumber : "";
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
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

    public String getCustomerPhone() {
        return customerPhone != null ? customerPhone : "";
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
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

    public double getTotalRentCost() {
        return totalRentCost;
    }

    public void setTotalRentCost(double totalRentCost) {
        this.totalRentCost = totalRentCost;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(double pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : STATUS_PENDING;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedTotalRentCost() {
        return String.format(Locale.US, "$%.2f", totalRentCost);
    }

    public String getFormattedTotalPaid() {
        return String.format(Locale.US, "$%.2f", totalPaid);
    }

    public String getFormattedPendingBalance() {
        return String.format(Locale.US, "$%.2f", pendingBalance);
    }

    public int getPaymentPercentage() {
        if (totalRentCost <= 0) return 100;
        int pct = (int) Math.round((totalPaid / totalRentCost) * 100);
        return Math.min(100, Math.max(0, pct));
    }

    public boolean isFullyPaid() {
        return pendingBalance <= 0.009;
    }
}
