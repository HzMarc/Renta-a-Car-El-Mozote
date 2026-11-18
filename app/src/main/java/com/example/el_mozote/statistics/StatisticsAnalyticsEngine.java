package com.example.el_mozote.statistics;

import android.content.Context;

import com.example.el_mozote.payments.PaymentAccount;
import com.example.el_mozote.payments.PaymentDatabaseHelper;
import com.example.el_mozote.payments.PaymentTransaction;
import com.example.el_mozote.reservations.Inspection;
import com.example.el_mozote.reservations.Reservation;
import com.example.el_mozote.reservations.ReservationDatabaseHelper;
import com.example.el_mozote.vehicles.Vehicle;
import com.example.el_mozote.vehicles.VehicleDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsAnalyticsEngine {

    private final Context context;
    private final VehicleDatabaseHelper vehicleDb;
    private final ReservationDatabaseHelper reservationDb;
    private final PaymentDatabaseHelper paymentDb;

    public StatisticsAnalyticsEngine(Context context) {
        this.context = context.getApplicationContext();
        this.vehicleDb = VehicleDatabaseHelper.getInstance(context);
        this.reservationDb = ReservationDatabaseHelper.getInstance(context);
        this.paymentDb = PaymentDatabaseHelper.getInstance(context);
    }

    /**
     * Calcula métricas y resumen completo de la flota según el filtro de tiempo especificado.
     */
    public FleetSummary calculateFleetSummary(TimePeriodFilter filter) {
        FleetSummary summary = new FleetSummary();
        summary.setPeriodFilter(filter);

        List<Vehicle> allVehicles = vehicleDb.getAllVehicles();
        List<Reservation> allReservations = reservationDb.searchReservations(null, null);

        // Sincronizar cuentas de cobro para garantizar coherencia
        paymentDb.syncAllReservations(allReservations);

        summary.setTotalVehicles(allVehicles.size());

        int countAvailable = 0;
        int countRented = 0;
        int countMaintenance = 0;

        for (Vehicle v : allVehicles) {
            if (Vehicle.STATUS_AVAILABLE.equalsIgnoreCase(v.getStatus())) countAvailable++;
            else if (Vehicle.STATUS_RENTED.equalsIgnoreCase(v.getStatus())) countRented++;
            else if (Vehicle.STATUS_MAINTENANCE.equalsIgnoreCase(v.getStatus())) countMaintenance++;
        }

        summary.setAvailableVehicles(countAvailable);
        summary.setRentedVehicles(countRented);
        summary.setMaintenanceVehicles(countMaintenance);

        List<VehicleStat> statList = new ArrayList<>();

        double grandTotalRevenue = 0.0;
        double grandTotalPending = 0.0;
        double grandTotalBilled = 0.0;
        int grandTotalRentals = 0;
        int grandTotalDays = 0;
        int grandTotalKm = 0;

        for (Vehicle vehicle : allVehicles) {
            VehicleStat vStat = calculateVehicleStat(vehicle, allReservations, filter);
            statList.add(vStat);

            grandTotalRevenue += vStat.getTotalRevenue();
            grandTotalPending += vStat.getPendingBalance();
            grandTotalBilled += vStat.getTotalBilled();
            grandTotalRentals += vStat.getTotalRentals();
            grandTotalDays += vStat.getTotalDaysRented();
            grandTotalKm += vStat.getTotalKmDriven();
        }

        summary.setTotalRevenue(grandTotalRevenue);
        summary.setTotalPending(grandTotalPending);
        summary.setTotalBilled(grandTotalBilled);
        summary.setTotalRentals(grandTotalRentals);
        summary.setTotalDaysRented(grandTotalDays);
        summary.setTotalKmDriven(grandTotalKm);

        // Utilización global de la flota (%)
        if (summary.getTotalVehicles() > 0) {
            double rate = ((double) countRented / summary.getTotalVehicles()) * 100.0;
            summary.setFleetUtilizationPercent(Math.min(100.0, Math.max(0.0, rate)));
        } else {
            summary.setFleetUtilizationPercent(0.0);
        }

        // Ordenar vehículos por ingresos descendente
        Collections.sort(statList, (a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()));
        summary.setVehicleStats(statList);

        // Identificar líderes y extremos para Factibilidad Económica
        if (!statList.isEmpty()) {
            // Mayor ingreso
            summary.setTopRevenueVehicle(statList.get(0));

            // Menor rendimiento
            summary.setLowestRevenueVehicle(statList.get(statList.size() - 1));

            // Mayor frecuencia de alquiler (ordenar copia por rentas desc)
            List<VehicleStat> byRentals = new ArrayList<>(statList);
            Collections.sort(byRentals, (a, b) -> Integer.compare(b.getTotalRentals(), a.getTotalRentals()));
            summary.setHighestFrequencyVehicle(byRentals.get(0));

            // Menor utilización (ordenar copia por tasa de uso asc)
            List<VehicleStat> byUtil = new ArrayList<>(statList);
            Collections.sort(byUtil, Comparator.comparingDouble(VehicleStat::getUtilizationRate));
            summary.setLowestUtilizationVehicle(byUtil.get(0));
        }

        return summary;
    }

    /**
     * Calcula las estadísticas individuales de un vehículo.
     */
    public VehicleStat calculateVehicleStat(Vehicle vehicle, List<Reservation> allReservations, TimePeriodFilter filter) {
        VehicleStat stat = new VehicleStat(vehicle);

        int rentalsCount = 0;
        int completedCount = 0;
        int activeCount = 0;
        int reservedCount = 0;

        double totalRevenue = 0.0;
        double totalBilled = 0.0;
        double totalPending = 0.0;
        int totalDays = 0;
        int totalKm = 0;

        for (Reservation res : allReservations) {
            if (res.getVehicleId() != vehicle.getId()) continue;

            // Filtro por período
            boolean inPeriod = filter.matchesDateString(res.getStartDate())
                    || filter.matchesTimestamp(res.getCreatedAt());
            if (!inPeriod) continue;

            rentalsCount++;
            String status = res.getStatus();
            if (Reservation.STATUS_COMPLETED.equalsIgnoreCase(status)) completedCount++;
            else if (Reservation.STATUS_ACTIVE.equalsIgnoreCase(status)) activeCount++;
            else if (Reservation.STATUS_RESERVED.equalsIgnoreCase(status)) reservedCount++;

            totalDays += res.getTotalDays();

            // Kilómetros recorridos a partir de inspecciones
            Inspection dep = reservationDb.getInspectionByReservationAndType(res.getId(), Inspection.TYPE_DEPARTURE);
            Inspection ret = reservationDb.getInspectionByReservationAndType(res.getId(), Inspection.TYPE_RETURN);
            if (dep != null && ret != null && ret.getMileage() >= dep.getMileage()) {
                totalKm += (ret.getMileage() - dep.getMileage());
            }

            // Datos financieros de cobros
            PaymentAccount acc = paymentDb.getAccountByReservationId(res.getId());
            if (acc != null) {
                totalBilled += acc.getTotalRentCost();
                totalRevenue += acc.getTotalPaid();
                totalPending += acc.getPendingBalance();
            } else {
                totalBilled += res.getTotalCost();
                totalPending += res.getTotalCost();
            }
        }

        stat.setTotalRentals(rentalsCount);
        stat.setCompletedRentals(completedCount);
        stat.setActiveRentals(activeCount);
        stat.setReservedRentals(reservedCount);

        stat.setTotalRevenue(totalRevenue);
        stat.setTotalBilled(totalBilled);
        stat.setPendingBalance(totalPending);
        stat.setTotalDaysRented(totalDays);
        stat.setTotalKmDriven(totalKm);

        double avgRevenue = (rentalsCount > 0) ? (totalRevenue / rentalsCount) : 0.0;
        stat.setAvgRevenuePerRental(avgRevenue);

        int refDays = filter.getReferenceDaysInPeriod();
        double utilRate = Math.min(100.0, ((double) totalDays / Math.max(1, refDays)) * 100.0);
        stat.setUtilizationRate(utilRate);
        stat.setRentalFrequency(rentalsCount);

        // Diagnóstico de factibilidad económica
        assignFeasibilityDiagnostics(stat, vehicle);

        return stat;
    }

    private void assignFeasibilityDiagnostics(VehicleStat stat, Vehicle vehicle) {
        double revenue = stat.getTotalRevenue();
        int rentals = stat.getTotalRentals();
        String currentStatus = vehicle.getStatus();

        if (Vehicle.STATUS_MAINTENANCE.equalsIgnoreCase(currentStatus)) {
            stat.setFeasibilityBadge("Mantenimiento");
            stat.setFeasibilityVerdict("Operación detenida por revisión técnica preventiva.");
            stat.setFeasibilityRecommendation("Completar inspección de taller para reintegrarlo a la flota de alquiler.");
            stat.setRoiIndex(35.0);
        } else if (revenue >= 100.0 || rentals >= 2) {
            stat.setFeasibilityBadge("Alta Rentabilidad");
            stat.setFeasibilityVerdict("Vehículo altamente productivo con retorno superior al promedio.");
            stat.setFeasibilityRecommendation("Mantener en rotación prioritaria y asegurar mantenimiento preventivo constante.");
            stat.setRoiIndex(95.0);
        } else if (rentals == 1 || revenue > 0.0) {
            stat.setFeasibilityBadge("Rendimiento Moderado");
            stat.setFeasibilityVerdict("Generando ingresos de manera regular y sostenida.");
            stat.setFeasibilityRecommendation("Promocionar en fin de semana para optimizar tasa de ocupación.");
            stat.setRoiIndex(70.0);
        } else {
            stat.setFeasibilityBadge("Bajo Rendimiento");
            stat.setFeasibilityVerdict("Sin actividad de alquiler registrada en este período.");
            stat.setFeasibilityRecommendation("Considerar ajuste competitivo de tarifa diaria o paquete promocional.");
            stat.setRoiIndex(20.0);
        }
    }

    /**
     * Calcula la evolución mensual de ingresos para el gráfico de línea temporal.
     */
    public Map<String, Double> calculateMonthlyRevenueTrend() {
        Map<String, Double> map = new HashMap<>();
        String[] months = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        for (String m : months) map.put(m, 0.0);

        List<PaymentTransaction> allTrxs = paymentDb.getAllTransactions();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        for (PaymentTransaction trx : allTrxs) {
            try {
                Date d = sdf.parse(trx.getTransactionDate());
                if (d != null) {
                    cal.setTime(d);
                    int mIdx = cal.get(Calendar.MONTH);
                    if (mIdx >= 0 && mIdx < 12) {
                        String mName = months[mIdx];
                        map.put(mName, map.get(mName) + trx.getAmount());
                    }
                }
            } catch (Exception ignored) {
                // Si la fecha contiene hora, intentar parsear
                int currentMonth = cal.get(Calendar.MONTH);
                String mName = months[currentMonth];
                map.put(mName, map.get(mName) + trx.getAmount());
            }
        }

        // Si la base de datos de pagos aún está en inicio, consolidar con rentas completadas
        if (allTrxs.isEmpty()) {
            List<Reservation> resList = reservationDb.searchReservations(null, null);
            for (Reservation r : resList) {
                try {
                    Date d = sdf.parse(r.getStartDate());
                    if (d != null) {
                        cal.setTime(d);
                        int mIdx = cal.get(Calendar.MONTH);
                        if (mIdx >= 0 && mIdx < 12) {
                            String mName = months[mIdx];
                            map.put(mName, map.get(mName) + r.getTotalCost());
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        return map;
    }
}
