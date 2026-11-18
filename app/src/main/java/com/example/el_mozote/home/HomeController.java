package com.example.el_mozote.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.el_mozote.MainActivity;
import com.example.el_mozote.R;
import com.example.el_mozote.payments.PaymentDatabaseHelper;
import com.example.el_mozote.reservations.Reservation;
import com.example.el_mozote.reservations.ReservationDatabaseHelper;
import com.example.el_mozote.vehicles.Vehicle;
import com.example.el_mozote.vehicles.VehicleDatabaseHelper;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeController {

    private final MainActivity activity;
    private final FrameLayout mainContent;
    private final VehicleDatabaseHelper vehicleDb;
    private final ReservationDatabaseHelper reservationDb;
    private final PaymentDatabaseHelper paymentDb;

    public HomeController(MainActivity activity, FrameLayout mainContent) {
        this.activity = activity;
        this.mainContent = mainContent;
        this.vehicleDb = VehicleDatabaseHelper.getInstance(activity);
        this.reservationDb = ReservationDatabaseHelper.getInstance(activity);
        this.paymentDb = PaymentDatabaseHelper.getInstance(activity);
    }

    public void showHome() {
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_home, mainContent, false);
        mainContent.addView(view);

        // Date Banner
        TextView txtDate = view.findViewById(R.id.txtHomeDateBanner);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));
        txtDate.setText(sdf.format(new Date()));

        // Sincronizar reservas con cobros para métricas exactas
        List<Reservation> allRes = reservationDb.searchReservations(null, null);
        paymentDb.syncAllReservations(allRes);

        // 1. KPI Flota Counters
        int totalVeh = vehicleDb.getCountTotal();
        int availVeh = vehicleDb.getCountByStatus(Vehicle.STATUS_AVAILABLE);
        int rentedVeh = vehicleDb.getCountByStatus(Vehicle.STATUS_RENTED);
        int maintVeh = vehicleDb.getCountByStatus(Vehicle.STATUS_MAINTENANCE);

        TextView txtTotal = view.findViewById(R.id.txtHomeTotalVehicles);
        TextView txtAvail = view.findViewById(R.id.txtHomeAvailableVehicles);
        TextView txtRented = view.findViewById(R.id.txtHomeRentedVehicles);
        TextView txtMaint = view.findViewById(R.id.txtHomeMaintenanceVehicles);

        txtTotal.setText(String.valueOf(totalVeh));
        txtAvail.setText(String.valueOf(availVeh));
        txtRented.setText(String.valueOf(rentedVeh));
        txtMaint.setText(String.valueOf(maintVeh));

        // 2. Acciones Rápidas Listeners
        view.findViewById(R.id.cardActionVehicles).setOnClickListener(v -> activity.navigateToTab(R.id.nav_vehicles));
        view.findViewById(R.id.cardActionNewRental).setOnClickListener(v -> activity.navigateToNewRental());
        view.findViewById(R.id.cardActionPayments).setOnClickListener(v -> activity.showPaymentsModule());
        view.findViewById(R.id.cardActionStats).setOnClickListener(v -> activity.showStatisticsModule());

        view.findViewById(R.id.btnHomeViewAllVehicles).setOnClickListener(v -> activity.navigateToTab(R.id.nav_vehicles));
        view.findViewById(R.id.btnHomeViewAllRentals).setOnClickListener(v -> activity.navigateToTab(R.id.nav_reservations));

        // 3. Vehículos Disponibles para Entrega
        LinearLayout containerAvail = view.findViewById(R.id.layoutHomeAvailableContainer);
        TextView txtNoAvail = view.findViewById(R.id.txtHomeNoAvailable);
        containerAvail.removeAllViews();

        List<Vehicle> availableList = vehicleDb.searchVehicles(null, Vehicle.STATUS_AVAILABLE);
        if (availableList.isEmpty()) {
            txtNoAvail.setVisibility(View.VISIBLE);
        } else {
            txtNoAvail.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(activity);

            for (Vehicle veh : availableList) {
                View card = inflater.inflate(R.layout.item_home_available_card, containerAvail, false);

                TextView title = card.findViewById(R.id.txtHomeVehicleTitle);
                TextView plate = card.findViewById(R.id.txtHomeVehiclePlate);
                TextView price = card.findViewById(R.id.txtHomeVehiclePrice);
                ImageView img = card.findViewById(R.id.imgHomeVehicle);
                MaterialButton btnRent = card.findViewById(R.id.btnHomeRentVehicle);

                title.setText(veh.getTitle());
                plate.setText(veh.getPlate() + "  •  " + veh.getYear());
                price.setText(String.format(Locale.US, "$%.2f/día", veh.getPricePerDay()));

                // Cargar fotografía si existe
                if (veh.getPhotoPaths() != null && !veh.getPhotoPaths().isEmpty()) {
                    String path = veh.getPhotoPaths().get(0);
                    File file = new File(path);
                    if (file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        if (bitmap != null) {
                            img.setImageBitmap(bitmap);
                        }
                    }
                }

                // Clicks
                card.setOnClickListener(v -> activity.openVehicleDetail(veh));
                btnRent.setOnClickListener(v -> activity.navigateToNewRental());

                containerAvail.addView(card);
            }
        }

        // 4. Rentas Activas y Recientes
        LinearLayout containerRentals = view.findViewById(R.id.layoutHomeRentalsContainer);
        TextView txtNoRentals = view.findViewById(R.id.txtHomeNoRentals);
        containerRentals.removeAllViews();

        if (allRes.isEmpty()) {
            txtNoRentals.setVisibility(View.VISIBLE);
        } else {
            txtNoRentals.setVisibility(View.GONE);
            LayoutInflater inflater = LayoutInflater.from(activity);
            int count = Math.min(allRes.size(), 4);

            for (int i = 0; i < count; i++) {
                Reservation res = allRes.get(i);
                View card = inflater.inflate(R.layout.item_home_rental_card, containerRentals, false);

                TextView txtCust = card.findViewById(R.id.txtHomeRentalCustomer);
                TextView txtBadge = card.findViewById(R.id.txtHomeRentalBadge);
                TextView txtVeh = card.findViewById(R.id.txtHomeRentalVehicle);
                TextView txtDates = card.findViewById(R.id.txtHomeRentalDates);
                TextView txtCost = card.findViewById(R.id.txtHomeRentalTotal);

                txtCust.setText(res.getCustomerName());
                txtVeh.setText(res.getVehicleTitle() + " (" + res.getVehiclePlate() + ")");
                txtDates.setText(res.getStartDate() + " al " + res.getEndDate() + " (" + res.getTotalDays() + " días)");
                txtCost.setText(String.format(Locale.US, "$%.2f", res.getTotalCost()));

                String status = res.getStatus();
                txtBadge.setText(status);
                if (Reservation.STATUS_ACTIVE.equalsIgnoreCase(status)) {
                    txtBadge.setBackgroundResource(R.drawable.badge_status_rented);
                    txtBadge.setTextColor(Color.parseColor("#0369A1"));
                } else if (Reservation.STATUS_COMPLETED.equalsIgnoreCase(status)) {
                    txtBadge.setBackgroundResource(R.drawable.badge_status_available);
                    txtBadge.setTextColor(activity.getResources().getColor(R.color.emerald_on_container_light, activity.getTheme()));
                } else {
                    txtBadge.setBackgroundResource(R.drawable.badge_status_unpaid);
                    txtBadge.setTextColor(Color.parseColor("#B45309"));
                }

                final Reservation targetRes = res;
                card.setOnClickListener(v -> activity.openReservationDetail(targetRes));
                containerRentals.addView(card);
            }
        }

        // 5. Resumen Financiero Rápido
        TextView txtBilled = view.findViewById(R.id.txtHomeFinanceBilled);
        TextView txtCollected = view.findViewById(R.id.txtHomeFinanceCollected);
        TextView txtPending = view.findViewById(R.id.txtHomeFinancePending);

        txtBilled.setText(String.format(Locale.US, "$%.2f", paymentDb.getTotalBilled()));
        txtCollected.setText(String.format(Locale.US, "$%.2f", paymentDb.getTotalCollected()));
        txtPending.setText(String.format(Locale.US, "$%.2f", paymentDb.getTotalPending()));

        view.findViewById(R.id.btnHomeGoToPayments).setOnClickListener(v -> activity.showPaymentsModule());
    }
}
