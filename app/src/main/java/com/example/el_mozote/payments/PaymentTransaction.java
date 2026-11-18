package com.example.el_mozote.payments;

import java.io.Serializable;
import java.util.Locale;

public class PaymentTransaction implements Serializable {

    public static final String METHOD_CASH = "Efectivo";
    public static final String METHOD_CARD = "Tarjeta de Crédito / Débito";
    public static final String METHOD_TRANSFER = "Transferencia Bancaria";
    public static final String METHOD_CHECK = "Cheque";

    public static final String TYPE_FULL = "PAGO COMPLETO";
    public static final String TYPE_PARTIAL = "ABONO";

    private long id;
    private long paymentId;
    private long reservationId;
    private String receiptNumber;
    private double amount;
    private String paymentMethod;
    private String paymentType;
    private double previousBalance;
    private double newBalance;
    private String notes;
    private String transactionDate;
    private long timestamp;

    // Campos enriquecidos para visualización rápida de comprobante
    private String customerName;
    private String contractNumber;
    private String vehicleTitle;

    public PaymentTransaction() {
        this.timestamp = System.currentTimeMillis();
    }

    public PaymentTransaction(long id, long paymentId, long reservationId, String receiptNumber,
                              double amount, String paymentMethod, String paymentType,
                              double previousBalance, double newBalance, String notes,
                              String transactionDate, long timestamp, String customerName,
                              String contractNumber, String vehicleTitle) {
        this.id = id;
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.receiptNumber = receiptNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentType = paymentType;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.notes = notes;
        this.transactionDate = transactionDate;
        this.timestamp = timestamp;
        this.customerName = customerName;
        this.contractNumber = contractNumber;
        this.vehicleTitle = vehicleTitle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getReceiptNumber() {
        return receiptNumber != null ? receiptNumber : "";
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : METHOD_CASH;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentType() {
        return paymentType != null ? paymentType : TYPE_PARTIAL;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(double previousBalance) {
        this.previousBalance = previousBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(double newBalance) {
        this.newBalance = newBalance;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTransactionDate() {
        return transactionDate != null ? transactionDate : "";
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCustomerName() {
        return customerName != null ? customerName : "";
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContractNumber() {
        return contractNumber != null ? contractNumber : "";
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getVehicleTitle() {
        return vehicleTitle != null ? vehicleTitle : "";
    }

    public void setVehicleTitle(String vehicleTitle) {
        this.vehicleTitle = vehicleTitle;
    }

    public String getFormattedAmount() {
        return String.format(Locale.US, "$%.2f", amount);
    }

    public String getFormattedNewBalance() {
        return String.format(Locale.US, "$%.2f", newBalance);
    }

    public String getFormattedPreviousBalance() {
        return String.format(Locale.US, "$%.2f", previousBalance);
    }
}
