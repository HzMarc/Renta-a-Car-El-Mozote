package com.example.el_mozote.statistics;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.MainActivity;
import com.example.el_mozote.R;
import com.example.el_mozote.reservations.Reservation;
import com.example.el_mozote.reservations.ReservationDatabaseHelper;
import com.example.el_mozote.statistics.charts.BarChartView;
import com.example.el_mozote.statistics.charts.DonutChartView;
import com.example.el_mozote.statistics.charts.LineTrendChartView;
import com.example.el_mozote.vehicles.Vehicle;
import com.example.el_mozote.vehicles.VehicleDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsController {

    public enum SubView {
        DASHBOARD,
        VEHICLE_DETAIL,
        COMPARISON,
        FEASIBILITY
    }

    private final MainActivity activity;
    private final FrameLayout mainContent;
    private final StatisticsAnalyticsEngine engine;
    private final VehicleDatabaseHelper vehicleDb;
    private final ReservationDatabaseHelper reservationDb;

    private SubView currentSubView = SubView.DASHBOARD;
    private TimePeriodFilter currentFilter = TimePeriodFilter.ALL;
    private long selectedVehicleId = -1;
    private String currentComparisonSort = "REVENUE"; // REVENUE, RENTALS, UTILIZATION

    // Root views
    private View mainStatsView;
    private FrameLayout containerContent;
    private ChipGroup chipGroupPeriod;
    private ChipGroup chipGroupViews;

    public StatisticsController(MainActivity activity, FrameLayout mainContent) {
        this.activity = activity;
        this.mainContent = mainContent;
        this.engine = new StatisticsAnalyticsEngine(activity);
        this.vehicleDb = VehicleDatabaseHelper.getInstance(activity);
        this.reservationDb = ReservationDatabaseHelper.getInstance(activity);
    }

    public SubView getCurrentSubView() {
        return currentSubView;
    }

    public boolean handleBackPressed() {
        if (currentSubView != SubView.DASHBOARD) {
            showSubView(SubView.DASHBOARD);
            return true;
        }
        return false;
    }

    public void showStatisticsModule() {
        mainContent.removeAllViews();

        mainStatsView = LayoutInflater.from(activity).inflate(R.layout.view_statistics_main, mainContent, false);
        mainContent.addView(mainStatsView);

        containerContent = mainStatsView.findViewById(R.id.containerStatsContent);
        chipGroupPeriod = mainStatsView.findViewById(R.id.chipGroupStatsPeriod);
        chipGroupViews = mainStatsView.findViewById(R.id.chipGroupStatsViews);

        // Period filter listener
        chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipPeriodToday) currentFilter = TimePeriodFilter.TODAY;
            else if (id == R.id.chipPeriodWeek) currentFilter = TimePeriodFilter.THIS_WEEK;
            else if (id == R.id.chipPeriodMonth) currentFilter = TimePeriodFilter.THIS_MONTH;
            else if (id == R.id.chipPeriodYear) currentFilter = TimePeriodFilter.THIS_YEAR;
            else currentFilter = TimePeriodFilter.ALL;

            refreshCurrentSubView();
        });

        // SubView tabs listener
        chipGroupViews.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipViewVehicles) showSubView(SubView.VEHICLE_DETAIL);
            else if (id == R.id.chipViewComparison) showSubView(SubView.COMPARISON);
            else if (id == R.id.chipViewFeasibility) showSubView(SubView.FEASIBILITY);
            else showSubView(SubView.DASHBOARD);
        });

        showSubView(currentSubView);
    }

    public void showVehicleStatistics(long vehicleId) {
        this.selectedVehicleId = vehicleId;
        if (mainStatsView == null) {
            showStatisticsModule();
        }
        showSubView(SubView.VEHICLE_DETAIL);
    }

    private void showSubView(SubView subView) {
        this.currentSubView = subView;
        if (chipGroupViews != null) {
            int targetChipId = R.id.chipViewDashboard;
            if (subView == SubView.VEHICLE_DETAIL) targetChipId = R.id.chipViewVehicles;
            else if (subView == SubView.COMPARISON) targetChipId = R.id.chipViewComparison;
            else if (subView == SubView.FEASIBILITY) targetChipId = R.id.chipViewFeasibility;

            if (chipGroupViews.getCheckedChipId() != targetChipId) {
                chipGroupViews.check(targetChipId);
            }
        }
        refreshCurrentSubView();
    }

    private void refreshCurrentSubView() {
        if (containerContent == null) return;
        containerContent.removeAllViews();

        FleetSummary summary = engine.calculateFleetSummary(currentFilter);

        switch (currentSubView) {
            case DASHBOARD:
                renderDashboard(summary);
                break;
            case VEHICLE_DETAIL:
                renderVehicleDetail(summary);
                break;
            case COMPARISON:
                renderComparison(summary);
                break;
            case FEASIBILITY:
                renderFeasibility(summary);
                break;
        }
    }

    // =========================================================================
    // 1. DASHBOARD GENERAL
    // =========================================================================
    private void renderDashboard(FleetSummary summary) {
        View view = LayoutInflater.from(activity).inflate(R.layout.view_statistics_dashboard, containerContent, false);
        containerContent.addView(view);

        // 6 General KPIs
        TextView txtTotalVeh = view.findViewById(R.id.txtKpiTotalVehicles);
        TextView txtAvail = view.findViewById(R.id.txtKpiAvailable);
        TextView txtRented = view.findViewById(R.id.txtKpiRented);
        TextView txtRentals = view.findViewById(R.id.txtKpiTotalRentals);
        TextView txtRevenue = view.findViewById(R.id.txtKpiRevenue);
        TextView txtPending = view.findViewById(R.id.txtKpiPending);

        txtTotalVeh.setText(String.valueOf(summary.getTotalVehicles()));
        txtAvail.setText(String.valueOf(summary.getAvailableVehicles()));
        txtRented.setText(String.valueOf(summary.getRentedVehicles()));
        txtRentals.setText(String.valueOf(summary.getTotalRentals()));
        txtRevenue.setText(summary.getFormattedRevenue());
        txtPending.setText(summary.getFormattedPending());

        // Donut Chart - Fleet Utilization
        DonutChartView donutChart = view.findViewById(R.id.donutFleetUtilization);
        List<DonutChartView.SliceItem> donutSlices = new ArrayList<>();
        donutSlices.add(new DonutChartView.SliceItem("Disponibles", summary.getAvailableVehicles(), Color.parseColor("#10B981")));
        donutSlices.add(new DonutChartView.SliceItem("Rentados", summary.getRentedVehicles(), Color.parseColor("#0EA5E9")));
        donutSlices.add(new DonutChartView.SliceItem("Taller", summary.getMaintenanceVehicles(), Color.parseColor("#F59E0B")));

        donutChart.setData(donutSlices, summary.getFormattedUtilization(), "En Alquiler");

        TextView txtLegAvail = view.findViewById(R.id.txtLegendAvailable);
        TextView txtLegRented = view.findViewById(R.id.txtLegendRented);
        TextView txtLegMaint = view.findViewById(R.id.txtLegendMaintenance);
        txtLegAvail.setText("Disponibles (" + summary.getAvailableVehicles() + ")");
        txtLegRented.setText("Rentados (" + summary.getRentedVehicles() + ")");
        txtLegMaint.setText("Taller (" + summary.getMaintenanceVehicles() + ")");

        // Bar Chart - Revenue by Vehicle
        BarChartView barChartRevenue = view.findViewById(R.id.barChartRevenue);
        List<BarChartView.BarItem> revenueBars = new ArrayList<>();
        int[] palette = {Color.parseColor("#10B981"), Color.parseColor("#0EA5E9"), Color.parseColor("#F59E0B"), Color.parseColor("#8B5CF6")};
        int idx = 0;
        for (VehicleStat vs : summary.getVehicleStats()) {
            revenueBars.add(new BarChartView.BarItem(
                    vs.getModel(),
                    vs.getTotalRevenue(),
                    vs.getFormattedRevenue(),
                    palette[idx % palette.length]));
            idx++;
        }
        barChartRevenue.setData(revenueBars);

        // Line Chart - Revenue Evolution Trend
        LineTrendChartView lineChartTrend = view.findViewById(R.id.lineChartRevenueTrend);
        Map<String, Double> monthlyTrend = engine.calculateMonthlyRevenueTrend();
        List<LineTrendChartView.PointItem> trendPoints = new ArrayList<>();
        String[] months = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        for (String m : months) {
            double val = monthlyTrend.containsKey(m) ? monthlyTrend.get(m) : 0.0;
            trendPoints.add(new LineTrendChartView.PointItem(m, val));
        }
        lineChartTrend.setData(trendPoints);

        // Bar Chart - Rentals Count by Vehicle
        BarChartView barChartRentals = view.findViewById(R.id.barChartRentalsCount);
        List<BarChartView.BarItem> rentalBars = new ArrayList<>();
        int rIdx = 0;
        for (VehicleStat vs : summary.getVehicleStats()) {
            rentalBars.add(new BarChartView.BarItem(
                    vs.getModel(),
                    vs.getTotalRentals(),
                    vs.getTotalRentals() + " rentas",
                    Color.parseColor("#0EA5E9")));
            rIdx++;
        }
        barChartRentals.setData(rentalBars);

        // Top Performer Banner
        VehicleStat top = summary.getTopRevenueVehicle();
        MaterialCardView topCard = view.findViewById(R.id.cardTopPerformerBanner);
        TextView txtTopRev = view.findViewById(R.id.txtTopBannerRevenue);
        TextView txtTopTitle = view.findViewById(R.id.txtTopBannerTitle);
        TextView txtTopDesc = view.findViewById(R.id.txtTopBannerDesc);
        MaterialButton btnTopView = view.findViewById(R.id.btnViewTopPerformer);

        if (top != null) {
            topCard.setVisibility(View.VISIBLE);
            txtTopRev.setText(top.getFormattedRevenue() + " generados");
            txtTopTitle.setText(top.getTitle() + " (" + top.getPlate() + ")");
            txtTopDesc.setText(top.getFeasibilityVerdict());
            btnTopView.setOnClickListener(v -> showVehicleStatistics(top.getVehicleId()));
        } else {
            topCard.setVisibility(View.GONE);
        }
    }

    // =========================================================================
    // 2. ESTADÍSTICAS INDIVIDUALES POR VEHÍCULO
    // =========================================================================
    private void renderVehicleDetail(FleetSummary summary) {
        View view = LayoutInflater.from(activity).inflate(R.layout.view_statistics_vehicle_detail, containerContent, false);
        containerContent.addView(view);

        List<VehicleStat> stats = summary.getVehicleStats();
        if (stats.isEmpty()) return;

        // Si no hay vehículo seleccionado o no existe, seleccionar el primero
        if (selectedVehicleId == -1) {
            selectedVehicleId = stats.get(0).getVehicleId();
        }

        VehicleStat currentStat = null;
        for (VehicleStat vs : stats) {
            if (vs.getVehicleId() == selectedVehicleId) {
                currentStat = vs;
                break;
            }
        }
        if (currentStat == null) {
            currentStat = stats.get(0);
            selectedVehicleId = currentStat.getVehicleId();
        }

        // Vehicle Selection Chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupSelectVehicle);
        chipGroup.removeAllViews();

        for (VehicleStat vs : stats) {
            Chip chip = new Chip(activity);
            chip.setText(vs.getTitle());
            chip.setCheckable(true);
            chip.setChecked(vs.getVehicleId() == selectedVehicleId);
            final long vId = vs.getVehicleId();
            chip.setOnClickListener(v -> {
                selectedVehicleId = vId;
                refreshCurrentSubView();
            });
            chipGroup.addView(chip);
        }

        // Populate Selected Vehicle Data
        TextView txtTitle = view.findViewById(R.id.txtDetailVehTitle);
        TextView txtPlate = view.findViewById(R.id.txtDetailVehPlate);
        TextView txtStatus = view.findViewById(R.id.txtDetailVehStatusBadge);
        TextView txtRevenue = view.findViewById(R.id.txtDetailVehRevenue);
        TextView txtDailyRate = view.findViewById(R.id.txtDetailVehDailyRate);
        TextView txtFeasBadge = view.findViewById(R.id.txtDetailVehFeasibilityBadge);
        TextView txtVerdict = view.findViewById(R.id.txtDetailVehVerdict);

        txtTitle.setText(currentStat.getTitle());
        txtPlate.setText("Placa: " + currentStat.getPlate() + "  •  Color: " + currentStat.getColor());
        txtStatus.setText(currentStat.getCurrentStatus());
        txtRevenue.setText(currentStat.getFormattedRevenue());
        txtDailyRate.setText(currentStat.getFormattedDailyRate());

        txtFeasBadge.setText(currentStat.getFeasibilityBadge().toUpperCase(Locale.ROOT));
        txtVerdict.setText(currentStat.getFeasibilityVerdict());

        // 6 Detailed Metrics
        TextView txtRentals = view.findViewById(R.id.txtDetailMetricRentals);
        TextView txtAvgRev = view.findViewById(R.id.txtDetailMetricAvgRevenue);
        TextView txtDays = view.findViewById(R.id.txtDetailMetricDays);
        TextView txtKm = view.findViewById(R.id.txtDetailMetricKm);
        TextView txtFreq = view.findViewById(R.id.txtDetailMetricFrequency);
        TextView txtUtilPct = view.findViewById(R.id.txtDetailMetricUtilizationPct);

        txtRentals.setText(currentStat.getTotalRentals() + " contratos");
        txtAvgRev.setText(currentStat.getFormattedAvgRevenue());
        txtDays.setText(currentStat.getTotalDaysRented() + " días");
        txtKm.setText(currentStat.getFormattedKm());
        txtFreq.setText(currentStat.getTotalRentals() + " rentas en el período");
        txtUtilPct.setText(currentStat.getFormattedUtilization());

        // Vehicle Rental History
        RecyclerView recyclerHistory = view.findViewById(R.id.recyclerVehHistory);
        TextView txtEmptyHistory = view.findViewById(R.id.txtEmptyVehHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(activity));

        List<Reservation> allRes = reservationDb.searchReservations(null, null);
        List<Reservation> vehRes = new ArrayList<>();
        for (Reservation r : allRes) {
            if (r.getVehicleId() == currentStat.getVehicleId()) {
                if (currentFilter.matchesDateString(r.getStartDate()) || currentFilter.matchesTimestamp(r.getCreatedAt())) {
                    vehRes.add(r);
                }
            }
        }

        if (vehRes.isEmpty()) {
            recyclerHistory.setVisibility(View.GONE);
            txtEmptyHistory.setVisibility(View.VISIBLE);
        } else {
            recyclerHistory.setVisibility(View.VISIBLE);
            txtEmptyHistory.setVisibility(View.GONE);
            recyclerHistory.setAdapter(new VehicleRentalHistoryAdapter(vehRes));
        }
    }

    // =========================================================================
    // 3. COMPARACIÓN DE VEHÍCULOS
    // =========================================================================
    private void renderComparison(FleetSummary summary) {
        View view = LayoutInflater.from(activity).inflate(R.layout.view_statistics_comparison, containerContent, false);
        containerContent.addView(view);

        List<VehicleStat> list = new ArrayList<>(summary.getVehicleStats());

        // Sort filter chips
        ChipGroup chipSort = view.findViewById(R.id.chipGroupComparisonSort);
        if ("RENTALS".equals(currentComparisonSort)) {
            chipSort.check(R.id.chipSortRentals);
            Collections.sort(list, (a, b) -> Integer.compare(b.getTotalRentals(), a.getTotalRentals()));
        } else if ("UTILIZATION".equals(currentComparisonSort)) {
            chipSort.check(R.id.chipSortUtilization);
            Collections.sort(list, (a, b) -> Double.compare(b.getUtilizationRate(), a.getUtilizationRate()));
        } else {
            chipSort.check(R.id.chipSortRevenue);
            Collections.sort(list, (a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()));
        }

        chipSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipSortRentals) currentComparisonSort = "RENTALS";
            else if (id == R.id.chipSortUtilization) currentComparisonSort = "UTILIZATION";
            else currentComparisonSort = "REVENUE";
            refreshCurrentSubView();
        });

        RecyclerView recycler = view.findViewById(R.id.recyclerComparisonList);
        recycler.setLayoutManager(new LinearLayoutManager(activity));

        VehicleStatAdapter adapter = new VehicleStatAdapter(list, stat -> {
            showVehicleStatistics(stat.getVehicleId());
        });
        recycler.setAdapter(adapter);
    }

    // =========================================================================
    // 4. ANÁLISIS DE FACTIBILIDAD ECONÓMICA
    // =========================================================================
    private void renderFeasibility(FleetSummary summary) {
        View view = LayoutInflater.from(activity).inflate(R.layout.view_statistics_feasibility, containerContent, false);
        containerContent.addView(view);

        // 1. Top Revenue Focus Card
        VehicleStat top = summary.getTopRevenueVehicle();
        if (top != null) {
            ((TextView) view.findViewById(R.id.txtFeasTopRevenueValue)).setText(top.getFormattedRevenue());
            ((TextView) view.findViewById(R.id.txtFeasTopRevenueVehicle)).setText(top.getTitle() + " (" + top.getPlate() + ")");
            ((TextView) view.findViewById(R.id.txtFeasTopRevenueDesc)).setText(top.getFeasibilityVerdict());
            view.findViewById(R.id.cardFeasTopRevenue).setOnClickListener(v -> showVehicleStatistics(top.getVehicleId()));
        }

        // 2. Lowest Revenue Focus Card
        VehicleStat low = summary.getLowestRevenueVehicle();
        if (low != null) {
            ((TextView) view.findViewById(R.id.txtFeasLowestRevenueValue)).setText(low.getFormattedRevenue());
            ((TextView) view.findViewById(R.id.txtFeasLowestRevenueVehicle)).setText(low.getTitle() + " (" + low.getPlate() + ")");
            ((TextView) view.findViewById(R.id.txtFeasLowestRevenueDesc)).setText(low.getFeasibilityVerdict());
            view.findViewById(R.id.cardFeasLowestRevenue).setOnClickListener(v -> showVehicleStatistics(low.getVehicleId()));
        }

        // 3. Highest Frequency Focus Card
        VehicleStat freq = summary.getHighestFrequencyVehicle();
        if (freq != null) {
            ((TextView) view.findViewById(R.id.txtFeasHighestFreqValue)).setText(freq.getTotalRentals() + " contratos");
            ((TextView) view.findViewById(R.id.txtFeasHighestFreqVehicle)).setText(freq.getTitle() + " (" + freq.getPlate() + ")");
            ((TextView) view.findViewById(R.id.txtFeasHighestFreqDesc)).setText("Mayor preferencia de clientes en el catálogo.");
            view.findViewById(R.id.cardFeasHighestFreq).setOnClickListener(v -> showVehicleStatistics(freq.getVehicleId()));
        }

        // 4. Lowest Utilization Focus Card
        VehicleStat util = summary.getLowestUtilizationVehicle();
        if (util != null) {
            ((TextView) view.findViewById(R.id.txtFeasLowestUtilValue)).setText(util.getFormattedUtilization());
            ((TextView) view.findViewById(R.id.txtFeasLowestUtilVehicle)).setText(util.getTitle() + " (" + util.getPlate() + ")");
            ((TextView) view.findViewById(R.id.txtFeasLowestUtilDesc)).setText(util.getFeasibilityVerdict());
            view.findViewById(R.id.cardFeasLowestUtil).setOnClickListener(v -> showVehicleStatistics(util.getVehicleId()));
        }

        // Strategic Recommendations List for ALL Vehicles
        LinearLayout layoutList = view.findViewById(R.id.layoutFeasibilityRecommendationsList);
        layoutList.removeAllViews();

        for (VehicleStat vs : summary.getVehicleStats()) {
            MaterialCardView card = new MaterialCardView(activity);
            card.setRadius(dpToPx(14));
            card.setCardElevation(dpToPx(1));
            card.setStrokeColor(Color.parseColor("#E2E8F0"));
            card.setStrokeWidth((int) dpToPx(1));
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) dpToPx(8);
            card.setLayoutParams(lp);

            LinearLayout inner = new LinearLayout(activity);
            inner.setOrientation(LinearLayout.VERTICAL);
            int p = (int) dpToPx(14);
            inner.setPadding(p, p, p, p);

            // Title + Badge
            LinearLayout topRow = new LinearLayout(activity);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView title = new TextView(activity);
            title.setText(vs.getTitle() + " (" + vs.getPlate() + ")");
            title.setTextAppearance(activity, com.google.android.material.R.style.TextAppearance_Material3_TitleSmall);
            title.setTextColor(Color.parseColor("#334155"));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            topRow.addView(title, tlp);

            TextView badge = new TextView(activity);
            badge.setText(vs.getFeasibilityBadge());
            badge.setTextSize(10f);
            badge.setTypeface(null, android.graphics.Typeface.BOLD);
            badge.setPadding((int) dpToPx(8), (int) dpToPx(3), (int) dpToPx(8), (int) dpToPx(3));

            if ("Alta Rentabilidad".equalsIgnoreCase(vs.getFeasibilityBadge())) {
                badge.setBackgroundResource(R.drawable.badge_feasibility_high);
                badge.setTextColor(Color.parseColor("#065F46"));
            } else if ("Rendimiento Moderado".equalsIgnoreCase(vs.getFeasibilityBadge())) {
                badge.setBackgroundResource(R.drawable.badge_feasibility_medium);
                badge.setTextColor(Color.parseColor("#0369A1"));
            } else if ("Mantenimiento".equalsIgnoreCase(vs.getFeasibilityBadge())) {
                badge.setBackgroundResource(R.drawable.badge_status_unpaid);
                badge.setTextColor(Color.parseColor("#B45309"));
            } else {
                badge.setBackgroundResource(R.drawable.badge_feasibility_low);
                badge.setTextColor(Color.parseColor("#B91C1C"));
            }
            topRow.addView(badge);
            inner.addView(topRow);

            // Verdict
            TextView verdict = new TextView(activity);
            verdict.setText("Diagnóstico: " + vs.getFeasibilityVerdict());
            verdict.setTextColor(Color.parseColor("#475569"));
            verdict.setTextSize(12f);
            verdict.setPadding(0, (int) dpToPx(6), 0, 0);
            inner.addView(verdict);

            // Actionable Recommendation Box
            TextView rec = new TextView(activity);
            rec.setText("Plan de acción: " + vs.getFeasibilityRecommendation());
            rec.setTextColor(Color.parseColor("#0EA5E9"));
            rec.setTextSize(12f);
            rec.setTypeface(null, android.graphics.Typeface.BOLD);
            rec.setPadding(0, (int) dpToPx(4), 0, 0);
            inner.addView(rec);

            card.addView(inner);
            final long vId = vs.getVehicleId();
            card.setOnClickListener(v -> showVehicleStatistics(vId));
            layoutList.addView(card);
        }
    }

    private float dpToPx(float dp) {
        return dp * activity.getResources().getDisplayMetrics().density;
    }
}
