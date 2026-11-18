package com.example.el_mozote.reservations;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.MainActivity;
import com.example.el_mozote.R;
import com.example.el_mozote.vehicles.ImageStorageHelper;
import com.example.el_mozote.vehicles.Vehicle;
import com.example.el_mozote.vehicles.VehicleDatabaseHelper;
import com.example.el_mozote.vehicles.VehiclePhotoGalleryAdapter;
import com.example.el_mozote.vehicles.VehiclePhotoThumbnailAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationController {

    public enum ScreenState {
        LIST,
        FORM_NEW,
        DETAIL,
        INSPECTION_FORM,
        INSPECTION_VIEW
    }

    private final MainActivity activity;
    private final FrameLayout mainContent;
    private final ReservationDatabaseHelper resDbHelper;
    private final VehicleDatabaseHelper vehicleDbHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private ScreenState currentScreen = ScreenState.LIST;
    private Reservation currentReservation = null;

    // List screen state
    private ReservationAdapter listAdapter;
    private String currentSearchQuery = "";
    private String currentFilterStatus = "Todas";
    private TextView txtStatTotal, txtStatActive, txtStatReserved, txtStatHistory;
    private LinearLayout layoutEmpty;

    // Inspection form state
    private final List<String> activeInspectionPhotos = new ArrayList<>();
    private VehiclePhotoThumbnailAdapter inspectionPhotoAdapter;
    private TextView txtInspPhotoCount;
    private RecyclerView recyclerInspPhotos;

    public ReservationController(MainActivity activity, FrameLayout mainContent) {
        this.activity = activity;
        this.mainContent = mainContent;
        this.resDbHelper = ReservationDatabaseHelper.getInstance(activity);
        this.vehicleDbHelper = VehicleDatabaseHelper.getInstance(activity);
    }

    public ScreenState getCurrentScreen() {
        return currentScreen;
    }

    public boolean handleBackPressed() {
        if (currentScreen == ScreenState.FORM_NEW) {
            showReservationList();
            return true;
        } else if (currentScreen == ScreenState.DETAIL) {
            showReservationList();
            return true;
        } else if (currentScreen == ScreenState.INSPECTION_FORM || currentScreen == ScreenState.INSPECTION_VIEW) {
            if (currentReservation != null) {
                showReservationDetail(currentReservation);
            } else {
                showReservationList();
            }
            return true;
        }
        return false;
    }

    // =========================================================================
    // 1. LISTADO DE RESERVAS Y RENTAS
    // =========================================================================
    public void showReservationList() {
        currentScreen = ScreenState.LIST;
        currentReservation = null;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_reservation_list, mainContent, false);
        mainContent.addView(view);

        // Stats
        txtStatTotal = view.findViewById(R.id.txtResStatTotal);
        txtStatActive = view.findViewById(R.id.txtResStatActive);
        txtStatReserved = view.findViewById(R.id.txtResStatReserved);
        txtStatHistory = view.findViewById(R.id.txtResStatHistory);
        layoutEmpty = view.findViewById(R.id.layoutEmptyReservations);

        // Recycler
        RecyclerView recycler = view.findViewById(R.id.recyclerReservations);
        recycler.setLayoutManager(new LinearLayoutManager(activity));
        listAdapter = new ReservationAdapter(new ArrayList<>(), this::showReservationDetail);
        recycler.setAdapter(listAdapter);

        // Search Box
        TextInputEditText editSearch = view.findViewById(R.id.editResSearch);
        editSearch.setText(currentSearchQuery);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = (s != null) ? s.toString().trim() : "";
                refreshListData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupResStatus);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipResActive) currentFilterStatus = "En curso";
            else if (id == R.id.chipResReserved) currentFilterStatus = "Pendientes";
            else if (id == R.id.chipResHistory) currentFilterStatus = "Historial";
            else if (id == R.id.chipResCancelled) currentFilterStatus = Reservation.STATUS_CANCELLED;
            else currentFilterStatus = "Todas";
            refreshListData();
        });

        // FAB & Empty button
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fabAddReservation);
        fab.setOnClickListener(v -> showNewReservationForm());

        MaterialButton btnEmpty = view.findViewById(R.id.btnEmptyAddReservation);
        btnEmpty.setOnClickListener(v -> showNewReservationForm());

        refreshListData();
    }

    private void refreshListData() {
        if (txtStatTotal != null) {
            txtStatTotal.setText(String.valueOf(resDbHelper.getCountTotal()));
            txtStatActive.setText(String.valueOf(resDbHelper.getCountByStatus(Reservation.STATUS_ACTIVE)));
            txtStatReserved.setText(String.valueOf(resDbHelper.getCountByStatus(Reservation.STATUS_RESERVED)));
            txtStatHistory.setText(String.valueOf(resDbHelper.getCountByStatus(Reservation.STATUS_COMPLETED)));
        }

        List<Reservation> list = resDbHelper.searchReservations(currentSearchQuery, currentFilterStatus);
        if (listAdapter != null) {
            listAdapter.setReservations(list);
        }

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // =========================================================================
    // 2. NUEVA RESERVA
    // =========================================================================
    public void showNewReservationForm() {
        currentScreen = ScreenState.FORM_NEW;
        currentReservation = null;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_reservation_form, mainContent, false);
        mainContent.addView(view);

        ImageButton btnBack = view.findViewById(R.id.btnResFormBack);
        btnBack.setOnClickListener(v -> showReservationList());

        MaterialButton btnCancel = view.findViewById(R.id.btnResCancel);
        btnCancel.setOnClickListener(v -> showReservationList());

        // Vehicle Selection Views
        View cardNoVehicle = view.findViewById(R.id.cardNoVehicleSelected);
        View cardVehicleSelected = view.findViewById(R.id.cardVehicleSelected);
        TextView txtVehTitle = view.findViewById(R.id.txtSelectedVehicleTitle);
        TextView txtVehPlate = view.findViewById(R.id.txtSelectedVehiclePlate);
        TextView txtVehRate = view.findViewById(R.id.txtSelectedVehicleRate);
        MaterialButton btnSelectVeh = view.findViewById(R.id.btnSelectVehicle);
        MaterialButton btnChangeVeh = view.findViewById(R.id.btnChangeVehicle);

        // Inputs
        TextInputLayout layoutCustName = view.findViewById(R.id.layoutResCustName);
        TextInputLayout layoutCustDui = view.findViewById(R.id.layoutResCustDui);
        TextInputLayout layoutCustLicense = view.findViewById(R.id.layoutResCustLicense);
        TextInputLayout layoutCustPhone = view.findViewById(R.id.layoutResCustPhone);
        TextInputLayout layoutCustAddress = view.findViewById(R.id.layoutResCustAddress);

        TextInputEditText editCustName = view.findViewById(R.id.editResCustName);
        TextInputEditText editCustDui = view.findViewById(R.id.editResCustDui);
        TextInputEditText editCustLicense = view.findViewById(R.id.editResCustLicense);
        TextInputEditText editCustPhone = view.findViewById(R.id.editResCustPhone);
        TextInputEditText editCustAddress = view.findViewById(R.id.editResCustAddress);

        TextInputLayout layoutStartDate = view.findViewById(R.id.layoutResStartDate);
        TextInputLayout layoutEndDate = view.findViewById(R.id.layoutResEndDate);
        TextInputEditText editStartDate = view.findViewById(R.id.editResStartDate);
        TextInputEditText editEndDate = view.findViewById(R.id.editResEndDate);
        TextInputEditText editDailyRate = view.findViewById(R.id.editResDailyRate);
        TextInputEditText editObservations = view.findViewById(R.id.editResObservations);

        TextView txtCalcDays = view.findViewById(R.id.txtResCalculatedDays);
        TextView txtCalcTotal = view.findViewById(R.id.txtResCalculatedTotal);

        // Default Dates (Today & Tomorrow)
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        Date twoDaysLater = cal.getTime();

        editStartDate.setText(dateFormat.format(today));
        editEndDate.setText(dateFormat.format(twoDaysLater));

        final Vehicle[] selectedVehicleHolder = new Vehicle[1];

        // Recalculate helper
        Runnable recalculate = () -> {
            try {
                Date d1 = dateFormat.parse(editStartDate.getText().toString().trim());
                Date d2 = dateFormat.parse(editEndDate.getText().toString().trim());
                if (d1 != null && d2 != null) {
                    long diff = d2.getTime() - d1.getTime();
                    long days = Math.max(1, diff / (1000 * 60 * 60 * 24));
                    txtCalcDays.setText(days + (days == 1 ? " día de alquiler" : " días de alquiler"));

                    double rate = 0;
                    try {
                        rate = Double.parseDouble(editDailyRate.getText().toString().trim());
                    } catch (Exception ignored) {}

                    double total = days * rate;
                    txtCalcTotal.setText(String.format(Locale.US, "$%.2f", total));
                }
            } catch (Exception e) {
                txtCalcDays.setText("1 día");
                txtCalcTotal.setText("$0.00");
            }
        };

        // Date pickers
        editStartDate.setOnClickListener(v -> showDatePicker(editStartDate, recalculate));
        editEndDate.setOnClickListener(v -> showDatePicker(editEndDate, recalculate));

        editDailyRate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recalculate.run();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Vehicle Picker BottomSheet
        View.OnClickListener openVehiclePicker = v -> {
            List<Vehicle> available = vehicleDbHelper.searchVehicles(null, Vehicle.STATUS_AVAILABLE);
            if (available.isEmpty()) {
                new AlertDialog.Builder(activity)
                        .setTitle("Sin vehículos disponibles")
                        .setMessage("No hay vehículos con estado 'Disponible' en el inventario actualmente. Todos se encuentran rentados o en mantenimiento.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }

            BottomSheetDialog dialog = new BottomSheetDialog(activity);
            View sheetView = LayoutInflater.from(activity).inflate(R.layout.view_vehicle_catalog, null);
            // Reutilizar layout de catálogo para picker rápido o crear vista limpia
            LinearLayout root = new LinearLayout(activity);
            root.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (16 * activity.getResources().getDisplayMetrics().density);
            root.setPadding(pad, pad, pad, pad);

            TextView title = new TextView(activity);
            title.setText("Selecciona un vehículo disponible");
            title.setTextAppearance(activity, android.R.style.TextAppearance_Material_Headline);
            title.setTextSize(16);
            title.setTextColor(activity.getResources().getColor(R.color.obsidian_navy, activity.getTheme()));
            root.addView(title);

            RecyclerView pickerRecycler = new RecyclerView(activity);
            pickerRecycler.setLayoutManager(new LinearLayoutManager(activity));
            VehiclePickerAdapter pickerAdapter = new VehiclePickerAdapter(available, veh -> {
                selectedVehicleHolder[0] = veh;
                cardNoVehicle.setVisibility(View.GONE);
                cardVehicleSelected.setVisibility(View.VISIBLE);
                txtVehTitle.setText(veh.getTitle());
                txtVehPlate.setText(veh.getPlate());
                txtVehRate.setText(veh.getFormattedPrice());
                editDailyRate.setText(String.format(Locale.US, "%.2f", veh.getPricePerDay()));
                recalculate.run();
                dialog.dismiss();
            });
            pickerRecycler.setAdapter(pickerAdapter);
            root.addView(pickerRecycler);

            dialog.setContentView(root);
            dialog.show();
        };

        btnSelectVeh.setOnClickListener(openVehiclePicker);
        btnChangeVeh.setOnClickListener(openVehiclePicker);

        // Save Reservation
        MaterialButton btnSave = view.findViewById(R.id.btnResSave);
        btnSave.setOnClickListener(v -> {
            if (selectedVehicleHolder[0] == null) {
                Toast.makeText(activity, "Por favor selecciona un vehículo", Toast.LENGTH_SHORT).show();
                return;
            }

            layoutCustName.setError(null);
            layoutCustDui.setError(null);
            layoutCustLicense.setError(null);
            layoutCustPhone.setError(null);
            layoutCustAddress.setError(null);
            layoutStartDate.setError(null);
            layoutEndDate.setError(null);

            String custName = text(editCustName);
            String custDui = text(editCustDui);
            String custLicense = text(editCustLicense);
            String custPhone = text(editCustPhone);
            String custAddress = text(editCustAddress);
            String startDate = text(editStartDate);
            String endDate = text(editEndDate);

            boolean valid = true;
            if (TextUtils.isEmpty(custName)) {
                layoutCustName.setError("Ingresa el nombre del cliente");
                valid = false;
            }
            if (TextUtils.isEmpty(custDui)) {
                layoutCustDui.setError("Ingresa el DUI");
                valid = false;
            }
            if (TextUtils.isEmpty(custLicense)) {
                layoutCustLicense.setError("Ingresa la licencia");
                valid = false;
            }
            if (TextUtils.isEmpty(custPhone)) {
                layoutCustPhone.setError("Ingresa el teléfono");
                valid = false;
            }
            if (TextUtils.isEmpty(custAddress)) {
                layoutCustAddress.setError("Ingresa la dirección");
                valid = false;
            }

            Date d1 = null, d2 = null;
            try {
                d1 = dateFormat.parse(startDate);
                d2 = dateFormat.parse(endDate);
                if (d1 != null && d2 != null && d2.before(d1)) {
                    layoutEndDate.setError("La fecha de devolución no puede ser anterior a la de salida");
                    valid = false;
                }
            } catch (ParseException e) {
                layoutStartDate.setError("Formato de fecha inválido");
                valid = false;
            }

            double rate = 0;
            try {
                rate = Double.parseDouble(text(editDailyRate));
                if (rate <= 0) valid = false;
            } catch (Exception e) {
                valid = false;
            }

            if (!valid) return;

            // Validación de superposición de fechas
            Reservation conflict = resDbHelper.checkDateConflict(selectedVehicleHolder[0].getId(),
                    startDate, endDate, -1);
            if (conflict != null) {
                new AlertDialog.Builder(activity)
                        .setTitle("Conflicto de fechas")
                        .setMessage("El vehículo " + selectedVehicleHolder[0].getTitle() + " ya cuenta con una reserva activa del "
                                + conflict.getStartDate() + " al " + conflict.getEndDate() + " para el cliente " + conflict.getCustomerName()
                                + ".\n\nPor favor selecciona otro rango de fechas o elige otro vehículo disponible.")
                        .setPositiveButton("Aceptar", null)
                        .show();
                return;
            }

            long diff = d2.getTime() - d1.getTime();
            int days = (int) Math.max(1, diff / (1000 * 60 * 60 * 24));
            double totalCost = days * rate;

            Vehicle veh = selectedVehicleHolder[0];
            Reservation newRes = new Reservation(0, veh.getId(), veh.getTitle(), veh.getPlate(),
                    veh.getPricePerDay(), custName, custDui, custLicense, custPhone, custAddress,
                    startDate, endDate, rate, days, totalCost, text(editObservations),
                    Reservation.STATUS_RESERVED, System.currentTimeMillis());

            long id = resDbHelper.insertReservation(newRes);
            if (id != -1) {
                Snackbar.make(mainContent, R.string.reservation_saved_success, Snackbar.LENGTH_SHORT).show();
                showReservationDetail(newRes);
            } else {
                Snackbar.make(mainContent, "Error al guardar la reserva", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(TextInputEditText targetInput, Runnable onDatePicked) {
        Calendar cal = Calendar.getInstance();
        try {
            Date current = dateFormat.parse(targetInput.getText().toString().trim());
            if (current != null) cal.setTime(current);
        } catch (Exception ignored) {}

        new DatePickerDialog(activity, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            targetInput.setText(dateFormat.format(cal.getTime()));
            if (onDatePicked != null) onDatePicked.run();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // =========================================================================
    // 3. DETALLE DE RESERVA / RENTA
    // =========================================================================
    public void showReservationDetail(Reservation res) {
        currentScreen = ScreenState.DETAIL;
        currentReservation = res;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_reservation_detail, mainContent, false);
        mainContent.addView(view);

        ImageButton btnBack = view.findViewById(R.id.btnResDetailBack);
        btnBack.setOnClickListener(v -> showReservationList());

        TextView txtTitle = view.findViewById(R.id.txtResDetailTitle);
        txtTitle.setText("Reserva #" + res.getId());

        // Header Summary
        TextView txtVehTitle = view.findViewById(R.id.txtDetailVehicleTitle);
        TextView txtVehPlate = view.findViewById(R.id.txtDetailVehiclePlate);
        TextView txtStatus = view.findViewById(R.id.txtDetailStatusBadge);
        TextView txtDates = view.findViewById(R.id.txtDetailDates);
        TextView txtDays = view.findViewById(R.id.txtDetailDays);
        TextView txtCost = view.findViewById(R.id.txtDetailTotalCost);

        txtVehTitle.setText(res.getVehicleTitle());
        txtVehPlate.setText(res.getVehiclePlate());
        txtDates.setText(res.getStartDate() + "  ➔  " + res.getEndDate());
        txtDays.setText(res.getTotalDays() + (res.getTotalDays() == 1 ? " día" : " días")
                + " a " + res.getFormattedDailyRate());
        txtCost.setText(res.getFormattedTotalCost());

        String status = res.getStatus();
        txtStatus.setText(status);
        if (Reservation.STATUS_RESERVED.equalsIgnoreCase(status)) {
            txtStatus.setBackgroundResource(R.drawable.badge_status_reserved);
            txtStatus.setTextColor(android.graphics.Color.parseColor("#6D28D9"));
        } else if (Reservation.STATUS_ACTIVE.equalsIgnoreCase(status)) {
            txtStatus.setBackgroundResource(R.drawable.bg_status_rented);
            txtStatus.setTextColor(android.graphics.Color.parseColor("#0369A1"));
        } else if (Reservation.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            txtStatus.setBackgroundResource(R.drawable.bg_status_available);
            txtStatus.setTextColor(ContextCompat.getColor(activity, R.color.emerald_on_container_light));
        } else {
            txtStatus.setBackgroundResource(R.drawable.badge_status_cancelled);
            txtStatus.setTextColor(android.graphics.Color.parseColor("#B91C1C"));
        }

        // Customer Info Card
        TextView txtCustName = view.findViewById(R.id.txtDetailCustName);
        TextView txtCustDui = view.findViewById(R.id.txtDetailCustDui);
        TextView txtCustLicense = view.findViewById(R.id.txtDetailCustLicense);
        TextView txtCustPhone = view.findViewById(R.id.txtDetailCustPhone);
        TextView txtCustAddress = view.findViewById(R.id.txtDetailCustAddress);

        txtCustName.setText(res.getCustomerName());
        txtCustDui.setText(res.getCustomerDui());
        txtCustLicense.setText(res.getCustomerLicense());
        txtCustPhone.setText(res.getCustomerPhone());
        txtCustAddress.setText(res.getCustomerAddress());

        // Inspecciones State
        boolean hasDeparture = resDbHelper.hasInspection(res.getId(), Inspection.TYPE_DEPARTURE);
        boolean hasReturn = resDbHelper.hasInspection(res.getId(), Inspection.TYPE_RETURN);

        TextView badgeSalida = view.findViewById(R.id.txtDepartureInspBadge);
        TextView sumSalida = view.findViewById(R.id.txtDepartureInspSummary);
        MaterialButton btnDoSalida = view.findViewById(R.id.btnDoDepartureInspection);
        MaterialButton btnViewSalida = view.findViewById(R.id.btnViewDepartureInspection);

        if (hasDeparture) {
            badgeSalida.setText("✓ Realizada");
            badgeSalida.setTextColor(ContextCompat.getColor(activity, R.color.emerald_on_container_light));
            badgeSalida.setBackgroundResource(R.drawable.bg_status_available);
            Inspection depInsp = resDbHelper.getInspectionByReservationAndType(res.getId(), Inspection.TYPE_DEPARTURE);
            sumSalida.setText("Kilometraje registrado: " + (depInsp != null ? depInsp.getMileage() + " km" : ""));
            btnDoSalida.setVisibility(View.GONE);
            btnViewSalida.setVisibility(View.VISIBLE);
            btnViewSalida.setOnClickListener(v -> showInspectionDetail(res, Inspection.TYPE_DEPARTURE));
        } else {
            badgeSalida.setText("Pendiente");
            badgeSalida.setBackgroundResource(R.drawable.badge_plate);
            badgeSalida.setTextColor(ContextCompat.getColor(activity, R.color.slate_muted));
            btnViewSalida.setVisibility(View.GONE);
            if (Reservation.STATUS_RESERVED.equals(status)) {
                btnDoSalida.setVisibility(View.VISIBLE);
                btnDoSalida.setOnClickListener(v -> showInspectionForm(res, Inspection.TYPE_DEPARTURE));
            } else {
                btnDoSalida.setVisibility(View.GONE);
            }
        }

        TextView badgeRegreso = view.findViewById(R.id.txtReturnInspBadge);
        TextView sumRegreso = view.findViewById(R.id.txtReturnInspSummary);
        MaterialButton btnDoRegreso = view.findViewById(R.id.btnDoReturnInspection);
        MaterialButton btnViewRegreso = view.findViewById(R.id.btnViewReturnInspection);

        if (hasReturn) {
            badgeRegreso.setText("✓ Realizada");
            badgeRegreso.setTextColor(ContextCompat.getColor(activity, R.color.emerald_on_container_light));
            badgeRegreso.setBackgroundResource(R.drawable.bg_status_available);
            Inspection retInsp = resDbHelper.getInspectionByReservationAndType(res.getId(), Inspection.TYPE_RETURN);
            sumRegreso.setText("Kilometraje final: " + (retInsp != null ? retInsp.getMileage() + " km" : ""));
            btnDoRegreso.setVisibility(View.GONE);
            btnViewRegreso.setVisibility(View.VISIBLE);
            btnViewRegreso.setOnClickListener(v -> showInspectionDetail(res, Inspection.TYPE_RETURN));
        } else {
            badgeRegreso.setText("Pendiente");
            badgeRegreso.setBackgroundResource(R.drawable.badge_plate);
            badgeRegreso.setTextColor(ContextCompat.getColor(activity, R.color.slate_muted));
            btnViewRegreso.setVisibility(View.GONE);
            if (Reservation.STATUS_ACTIVE.equals(status)) {
                btnDoRegreso.setVisibility(View.VISIBLE);
                btnDoRegreso.setOnClickListener(v -> showInspectionForm(res, Inspection.TYPE_RETURN));
            } else {
                btnDoRegreso.setVisibility(View.GONE);
            }
        }

        // Observaciones
        TextView txtObs = view.findViewById(R.id.txtDetailObservations);
        txtObs.setText(TextUtils.isEmpty(res.getObservations()) ? "Sin observaciones registradas." : res.getObservations());

        // Link to Payments Module
        MaterialButton btnPayments = view.findViewById(R.id.btnGoToPayments);
        if (btnPayments != null) {
            btnPayments.setOnClickListener(v -> activity.openPaymentForReservation(res.getId()));
        }

        // Cancel Button
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelReservation);
        if (Reservation.STATUS_RESERVED.equals(status)) {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(v -> confirmCancelReservation(res));
        } else {
            btnCancel.setVisibility(View.GONE);
        }

        ImageButton btnMenu = view.findViewById(R.id.btnResDetailMenu);
        btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            if (Reservation.STATUS_RESERVED.equals(res.getStatus())) {
                popup.getMenu().add("Cancelar reserva");
            }
            popup.getMenu().add("Eliminar registro");
            popup.setOnMenuItemClickListener(item -> {
                String t = item.getTitle().toString();
                if ("Cancelar reserva".equals(t)) confirmCancelReservation(res);
                else confirmDeleteReservation(res);
                return true;
            });
            popup.show();
        });
    }

    private void confirmCancelReservation(Reservation res) {
        new AlertDialog.Builder(activity)
                .setTitle("¿Cancelar reserva?")
                .setMessage("¿Deseas marcar la reserva #" + res.getId() + " como cancelada?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    res.setStatus(Reservation.STATUS_CANCELLED);
                    resDbHelper.updateReservationStatus(res.getId(), Reservation.STATUS_CANCELLED);
                    Snackbar.make(mainContent, R.string.reservation_cancelled_success, Snackbar.LENGTH_SHORT).show();
                    showReservationDetail(res);
                })
                .show();
    }

    private void confirmDeleteReservation(Reservation res) {
        new AlertDialog.Builder(activity)
                .setTitle("Eliminar registro")
                .setMessage("¿Eliminar definitivamente la reserva #" + res.getId() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    resDbHelper.deleteReservation(res.getId());
                    Snackbar.make(mainContent, "Registro eliminado", Snackbar.LENGTH_SHORT).show();
                    showReservationList();
                })
                .show();
    }

    // =========================================================================
    // 4. INSPECCIÓN (SALIDA O REGRESO)
    // =========================================================================
    public void showInspectionForm(Reservation res, String type) {
        currentScreen = ScreenState.INSPECTION_FORM;
        currentReservation = res;
        activeInspectionPhotos.clear();
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_inspection_form, mainContent, false);
        mainContent.addView(view);

        boolean isDeparture = Inspection.TYPE_DEPARTURE.equals(type);

        TextView txtTitle = view.findViewById(R.id.txtInspFormTitle);
        txtTitle.setText(isDeparture
                ? R.string.reservation_inspection_departure_title
                : R.string.reservation_inspection_return_title);

        ImageButton btnBack = view.findViewById(R.id.btnInspFormBack);
        btnBack.setOnClickListener(v -> showReservationDetail(res));

        MaterialButton btnCancel = view.findViewById(R.id.btnInspCancel);
        btnCancel.setOnClickListener(v -> showReservationDetail(res));

        // Vehicle info
        TextView txtVehTitle = view.findViewById(R.id.txtInspVehicleTitle);
        TextView txtVehPlate = view.findViewById(R.id.txtInspVehiclePlate);
        TextView txtClient = view.findViewById(R.id.txtInspClientName);

        txtVehTitle.setText(res.getVehicleTitle());
        txtVehPlate.setText(res.getVehiclePlate());
        txtClient.setText("Cliente: " + res.getCustomerName());

        // Banner de kilometraje si es regreso
        View banner = view.findViewById(R.id.layoutInspReturnBanner);
        TextView txtInitKm = view.findViewById(R.id.txtInitialMileageBanner);
        View cardMaint = view.findViewById(R.id.cardMaintenanceSwitch);
        MaterialSwitch switchMaint = view.findViewById(R.id.switchSendToMaintenance);

        final int[] departureMileageHolder = new int[]{-1};
        if (!isDeparture) {
            banner.setVisibility(View.VISIBLE);
            cardMaint.setVisibility(View.VISIBLE);
            Inspection dep = resDbHelper.getInspectionByReservationAndType(res.getId(), Inspection.TYPE_DEPARTURE);
            if (dep != null) {
                departureMileageHolder[0] = dep.getMileage();
                txtInitKm.setText("Kilometraje inicial registrado: " + dep.getMileage() + " km");
            }
        } else {
            banner.setVisibility(View.GONE);
            cardMaint.setVisibility(View.GONE);
        }

        // Inputs
        TextInputLayout layoutKm = view.findViewById(R.id.layoutInspMileage);
        TextInputEditText editKm = view.findViewById(R.id.editInspMileage);
        TextInputEditText editFuel = view.findViewById(R.id.editInspFuel);
        TextInputEditText editExterior = view.findViewById(R.id.editInspExterior);
        TextInputEditText editInterior = view.findViewById(R.id.editInspInterior);
        MaterialSwitch switchSpare = view.findViewById(R.id.switchSpareTire);
        MaterialSwitch switchTools = view.findViewById(R.id.switchTools);
        TextInputEditText editDamages = view.findViewById(R.id.editInspDamages);
        TextInputEditText editObs = view.findViewById(R.id.editInspObs);

        // Preload mileage hint
        if (!isDeparture && departureMileageHolder[0] > 0) {
            editKm.setText(String.valueOf(departureMileageHolder[0] + 50));
        }

        // Photos
        txtInspPhotoCount = view.findViewById(R.id.txtInspPhotoCount);
        recyclerInspPhotos = view.findViewById(R.id.recyclerInspPhotos);
        recyclerInspPhotos.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        inspectionPhotoAdapter = new VehiclePhotoThumbnailAdapter(activeInspectionPhotos, (pos, path) -> {
            updateInspPhotosCount();
        });
        recyclerInspPhotos.setAdapter(inspectionPhotoAdapter);

        MaterialButton btnAddPhotos = view.findViewById(R.id.btnInspAddPhotos);
        btnAddPhotos.setOnClickListener(v -> activity.launchReservationPhotoPicker());

        // Save Inspection Button
        MaterialButton btnSave = view.findViewById(R.id.btnInspSave);
        btnSave.setText(isDeparture
                ? "Guardar Inspección e Iniciar Renta"
                : "Guardar Inspección y Finalizar Renta");

        btnSave.setOnClickListener(v -> {
            layoutKm.setError(null);
            String kmStr = text(editKm);
            if (TextUtils.isEmpty(kmStr)) {
                layoutKm.setError("Ingresa el kilometraje actual");
                return;
            }

            int mileage;
            try {
                mileage = Integer.parseInt(kmStr);
                if (mileage < 0) {
                    layoutKm.setError("Kilometraje inválido");
                    return;
                }
            } catch (NumberFormatException e) {
                layoutKm.setError("Número inválido");
                return;
            }

            // Validación REGLA OBLIGATORIA: Kilometraje final no puede ser menor al inicial
            if (!isDeparture && departureMileageHolder[0] > 0 && mileage < departureMileageHolder[0]) {
                layoutKm.setError("El kilometraje final (" + mileage + " km) no puede ser menor al inicial (" + departureMileageHolder[0] + " km)");
                return;
            }

            List<String> photos = inspectionPhotoAdapter != null
                    ? inspectionPhotoAdapter.getPhotos()
                    : new ArrayList<>(activeInspectionPhotos);

            Inspection insp = new Inspection(0, res.getId(), type, mileage, text(editFuel),
                    text(editExterior), text(editInterior), switchSpare.isChecked(),
                    switchTools.isChecked(), text(editDamages), text(editObs), photos,
                    System.currentTimeMillis());

            long inspId = resDbHelper.insertInspection(insp);
            if (inspId == -1) {
                Snackbar.make(mainContent, "Error al registrar la inspección", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Conexión y Actualización con el Inventario de Vehículos
            if (isDeparture) {
                // Actualizar reserva a ACTIVA
                res.setStatus(Reservation.STATUS_ACTIVE);
                resDbHelper.updateReservationStatus(res.getId(), Reservation.STATUS_ACTIVE);

                // REGLA: Actualizar el estado del vehículo a Rentado al iniciar el alquiler
                Vehicle veh = vehicleDbHelper.getVehicleById(res.getVehicleId());
                if (veh != null) {
                    veh.setStatus(Vehicle.STATUS_RENTED);
                    vehicleDbHelper.updateVehicle(veh);
                }

                Snackbar.make(mainContent, "¡Renta iniciada! Vehículo actualizado a Rentado.", Snackbar.LENGTH_LONG).show();
            } else {
                // Actualizar reserva a FINALIZADA
                res.setStatus(Reservation.STATUS_COMPLETED);
                resDbHelper.updateReservationStatus(res.getId(), Reservation.STATUS_COMPLETED);

                // REGLA: Actualizar a Disponible al finalizar, siempre que no requiera mantenimiento
                Vehicle veh = vehicleDbHelper.getVehicleById(res.getVehicleId());
                if (veh != null) {
                    boolean sendToMaint = switchMaint.isChecked();
                    veh.setStatus(sendToMaint ? Vehicle.STATUS_MAINTENANCE : Vehicle.STATUS_AVAILABLE);
                    vehicleDbHelper.updateVehicle(veh);
                }

                Snackbar.make(mainContent, "¡Renta finalizada con éxito! Vehículo liberado en inventario.", Snackbar.LENGTH_LONG).show();
            }

            showReservationDetail(res);
        });
    }

    private void updateInspPhotosCount() {
        if (txtInspPhotoCount == null) return;
        int count = activeInspectionPhotos.size();
        txtInspPhotoCount.setText(count + (count == 1 ? " foto seleccionada" : " fotos seleccionadas"));
        recyclerInspPhotos.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    public void onInspectionPhotosSelected(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;
        List<String> paths = ImageStorageHelper.saveUrisToInternalStorage(activity, uris);
        if (paths != null && !paths.isEmpty()) {
            activeInspectionPhotos.addAll(paths);
            if (inspectionPhotoAdapter != null) {
                inspectionPhotoAdapter.addPhotos(paths);
            }
            updateInspPhotosCount();
        }
    }

    // =========================================================================
    // 5. DETALLE DE INSPECCIÓN REGISTRADA (Solo Lectura)
    // =========================================================================
    public void showInspectionDetail(Reservation res, String type) {
        currentScreen = ScreenState.INSPECTION_VIEW;
        currentReservation = res;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_inspection_detail, mainContent, false);
        mainContent.addView(view);

        Inspection insp = resDbHelper.getInspectionByReservationAndType(res.getId(), type);
        if (insp == null) {
            Toast.makeText(activity, "No se encontró registro de inspección", Toast.LENGTH_SHORT).show();
            showReservationDetail(res);
            return;
        }

        ImageButton btnBack = view.findViewById(R.id.btnInspViewBack);
        btnBack.setOnClickListener(v -> showReservationDetail(res));

        MaterialButton btnClose = view.findViewById(R.id.btnInspViewClose);
        btnClose.setOnClickListener(v -> showReservationDetail(res));

        TextView txtTitle = view.findViewById(R.id.txtInspViewTitle);
        txtTitle.setText("Inspección de " + (Inspection.TYPE_DEPARTURE.equals(type) ? "Salida" : "Regreso"));

        TextView badge = view.findViewById(R.id.txtInspViewBadge);
        badge.setText("INSPECCIÓN DE " + type);

        TextView date = view.findViewById(R.id.txtInspViewDate);
        date.setText(dateFormat.format(new Date(insp.getCreatedAt())));

        TextView km = view.findViewById(R.id.txtInspViewMileage);
        km.setText(String.format(Locale.US, "%,d km", insp.getMileage()));

        TextView fuel = view.findViewById(R.id.txtInspViewFuel);
        fuel.setText(insp.getFuelLevel());

        TextView ext = view.findViewById(R.id.txtInspViewExterior);
        ext.setText(insp.getExteriorState());

        TextView in = view.findViewById(R.id.txtInspViewInterior);
        in.setText(insp.getInteriorState());

        TextView spare = view.findViewById(R.id.txtInspViewSpareTire);
        spare.setText(insp.isSpareTire() ? "✓ Llanta de repuesto: Sí" : "✗ Llanta de repuesto: No");

        TextView tools = view.findViewById(R.id.txtInspViewTools);
        tools.setText(insp.isTools() ? "✓ Kit de herramientas: Sí" : "✗ Kit de herramientas: No");

        TextView damages = view.findViewById(R.id.txtInspViewDamages);
        damages.setText(insp.getDamages());

        TextView obs = view.findViewById(R.id.txtInspViewObs);
        obs.setText(TextUtils.isEmpty(insp.getObservations()) ? "Sin observaciones adicionales." : insp.getObservations());

        // Photos Gallery
        RecyclerView galleryRecycler = view.findViewById(R.id.recyclerInspViewPhotos);
        TextView noPhotos = view.findViewById(R.id.txtInspViewNoPhotos);

        if (insp.hasPhotos()) {
            galleryRecycler.setVisibility(View.VISIBLE);
            noPhotos.setVisibility(View.GONE);
            galleryRecycler.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            galleryRecycler.setAdapter(new VehiclePhotoGalleryAdapter(insp.getPhotoPaths()));
        } else {
            galleryRecycler.setVisibility(View.GONE);
            noPhotos.setVisibility(View.VISIBLE);
        }
    }

    private String text(TextInputEditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString().trim();
    }
}
