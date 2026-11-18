package com.example.el_mozote.vehicles;

import android.app.AlertDialog;
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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.MainActivity;
import com.example.el_mozote.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleController {

    public enum ScreenState {
        CATALOG,
        REGISTER,
        DETAIL,
        EDIT
    }

    private final MainActivity activity;
    private final FrameLayout mainContent;
    private final VehicleDatabaseHelper dbHelper;

    private ScreenState currentScreen = ScreenState.CATALOG;
    private Vehicle currentVehicle = null;

    // Catalog state
    private VehicleAdapter catalogAdapter;
    private String currentSearchQuery = "";
    private String currentFilterStatus = "Todos";
    private TextView txtStatTotal;
    private TextView txtStatAvailable;
    private TextView txtStatRented;
    private TextView txtStatMaintenance;
    private LinearLayout layoutEmptyVehicles;

    // Active Form state (Register / Edit)
    private final List<String> activeFormPhotos = new ArrayList<>();
    private VehiclePhotoThumbnailAdapter formThumbnailAdapter;
    private TextView txtFormPhotoCount;
    private TextView txtFormPhotosEmpty;
    private RecyclerView recyclerFormPhotos;

    public VehicleController(MainActivity activity, FrameLayout mainContent) {
        this.activity = activity;
        this.mainContent = mainContent;
        this.dbHelper = VehicleDatabaseHelper.getInstance(activity);
    }

    public ScreenState getCurrentScreen() {
        return currentScreen;
    }

    public boolean handleBackPressed() {
        if (currentScreen == ScreenState.REGISTER) {
            showCatalog();
            return true;
        } else if (currentScreen == ScreenState.EDIT) {
            if (currentVehicle != null) {
                showDetail(currentVehicle);
            } else {
                showCatalog();
            }
            return true;
        } else if (currentScreen == ScreenState.DETAIL) {
            showCatalog();
            return true;
        }
        return false;
    }

    // =========================================================================
    // 1. VISTA DE CATÁLOGO
    // =========================================================================
    public void showCatalog() {
        currentScreen = ScreenState.CATALOG;
        currentVehicle = null;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_vehicle_catalog, mainContent, false);
        mainContent.addView(view);

        // Stats
        txtStatTotal = view.findViewById(R.id.txtStatTotal);
        txtStatAvailable = view.findViewById(R.id.txtStatAvailable);
        txtStatRented = view.findViewById(R.id.txtStatRented);
        txtStatMaintenance = view.findViewById(R.id.txtStatMaintenance);
        layoutEmptyVehicles = view.findViewById(R.id.layoutEmptyVehicles);

        // RecyclerView
        RecyclerView recycler = view.findViewById(R.id.recyclerVehicles);
        recycler.setLayoutManager(new LinearLayoutManager(activity));

        catalogAdapter = new VehicleAdapter(new ArrayList<>(), new VehicleAdapter.OnVehicleActionListener() {
            @Override
            public void onVehicleClick(Vehicle vehicle) {
                showDetail(vehicle);
            }

            @Override
            public void onVehicleEdit(Vehicle vehicle) {
                showEditForm(vehicle);
            }

            @Override
            public void onVehicleDelete(Vehicle vehicle) {
                confirmDeleteVehicle(vehicle);
            }

            @Override
            public void onVehicleStatusChange(Vehicle vehicle, String newStatus) {
                vehicle.setStatus(newStatus);
                dbHelper.updateVehicle(vehicle);
                refreshCatalogData();
                Snackbar.make(mainContent, "Estado actualizado a: " + newStatus, Snackbar.LENGTH_SHORT).show();
            }
        });
        recycler.setAdapter(catalogAdapter);

        // Search Input
        TextInputEditText editSearch = view.findViewById(R.id.editVehicleSearch);
        editSearch.setText(currentSearchQuery);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = (s != null) ? s.toString().trim() : "";
                refreshCatalogData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupStatus);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipFilterAvailable) {
                currentFilterStatus = Vehicle.STATUS_AVAILABLE;
            } else if (checkedId == R.id.chipFilterRented) {
                currentFilterStatus = Vehicle.STATUS_RENTED;
            } else if (checkedId == R.id.chipFilterMaintenance) {
                currentFilterStatus = Vehicle.STATUS_MAINTENANCE;
            } else {
                currentFilterStatus = "Todos";
            }
            refreshCatalogData();
        });

        // FAB & Empty button -> Register
        ExtendedFloatingActionButton fabAdd = view.findViewById(R.id.fabAddVehicle);
        fabAdd.setOnClickListener(v -> showRegisterForm());

        MaterialButton btnEmptyAdd = view.findViewById(R.id.btnEmptyAddVehicle);
        btnEmptyAdd.setOnClickListener(v -> showRegisterForm());

        refreshCatalogData();
    }

    private void refreshCatalogData() {
        if (txtStatTotal != null) {
            txtStatTotal.setText(String.valueOf(dbHelper.getCountTotal()));
            txtStatAvailable.setText(String.valueOf(dbHelper.getCountByStatus(Vehicle.STATUS_AVAILABLE)));
            txtStatRented.setText(String.valueOf(dbHelper.getCountByStatus(Vehicle.STATUS_RENTED)));
            txtStatMaintenance.setText(String.valueOf(dbHelper.getCountByStatus(Vehicle.STATUS_MAINTENANCE)));
        }

        List<Vehicle> list = dbHelper.searchVehicles(currentSearchQuery, currentFilterStatus);
        if (catalogAdapter != null) {
            catalogAdapter.setVehicles(list);
        }

        if (layoutEmptyVehicles != null) {
            layoutEmptyVehicles.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // =========================================================================
    // 2. VISTA DE REGISTRO
    // =========================================================================
    public void showRegisterForm() {
        currentScreen = ScreenState.REGISTER;
        currentVehicle = null;
        activeFormPhotos.clear();
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_vehicle_form, mainContent, false);
        mainContent.addView(view);

        TextView txtTitle = view.findViewById(R.id.txtFormTitle);
        txtTitle.setText(R.string.vehicle_form_add_title);

        setupFormFields(view, null);
    }

    // =========================================================================
    // 3. VISTA DE EDICIÓN
    // =========================================================================
    public void showEditForm(Vehicle vehicle) {
        currentScreen = ScreenState.EDIT;
        currentVehicle = vehicle;
        activeFormPhotos.clear();
        if (vehicle.getPhotoPaths() != null) {
            activeFormPhotos.addAll(vehicle.getPhotoPaths());
        }
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_vehicle_form, mainContent, false);
        mainContent.addView(view);

        TextView txtTitle = view.findViewById(R.id.txtFormTitle);
        txtTitle.setText(R.string.vehicle_form_edit_title);

        MaterialButton btnSave = view.findViewById(R.id.btnFormSave);
        btnSave.setText("Guardar cambios");

        setupFormFields(view, vehicle);
    }

    private void setupFormFields(View view, Vehicle existing) {
        ImageButton btnBack = view.findViewById(R.id.btnFormBack);
        btnBack.setOnClickListener(v -> handleBackPressed());

        MaterialButton btnCancel = view.findViewById(R.id.btnFormCancel);
        btnCancel.setOnClickListener(v -> handleBackPressed());

        TextInputLayout layoutBrand = view.findViewById(R.id.layoutFormBrand);
        TextInputLayout layoutModel = view.findViewById(R.id.layoutFormModel);
        TextInputLayout layoutYear = view.findViewById(R.id.layoutFormYear);
        TextInputLayout layoutPlate = view.findViewById(R.id.layoutFormPlate);
        TextInputLayout layoutColor = view.findViewById(R.id.layoutFormColor);
        TextInputLayout layoutPrice = view.findViewById(R.id.layoutFormPrice);
        TextInputLayout layoutDesc = view.findViewById(R.id.layoutFormDescription);

        TextInputEditText editBrand = view.findViewById(R.id.editFormBrand);
        TextInputEditText editModel = view.findViewById(R.id.editFormModel);
        TextInputEditText editYear = view.findViewById(R.id.editFormYear);
        TextInputEditText editPlate = view.findViewById(R.id.editFormPlate);
        TextInputEditText editColor = view.findViewById(R.id.editFormColor);
        TextInputEditText editPrice = view.findViewById(R.id.editFormPrice);
        TextInputEditText editDesc = view.findViewById(R.id.editFormDescription);

        ChipGroup chipGroupStatus = view.findViewById(R.id.chipGroupFormStatus);
        Chip chipAvailable = view.findViewById(R.id.chipStatusAvailable);
        Chip chipRented = view.findViewById(R.id.chipStatusRented);
        Chip chipMaintenance = view.findViewById(R.id.chipStatusMaintenance);

        // Prepopulate if editing
        if (existing != null) {
            editBrand.setText(existing.getBrand());
            editModel.setText(existing.getModel());
            editYear.setText(String.valueOf(existing.getYear()));
            editPlate.setText(existing.getPlate());
            editColor.setText(existing.getColor());
            editPrice.setText(String.format(Locale.US, "%.2f", existing.getPricePerDay()));
            editDesc.setText(existing.getDescription());

            if (Vehicle.STATUS_RENTED.equalsIgnoreCase(existing.getStatus())) {
                chipRented.setChecked(true);
            } else if (Vehicle.STATUS_MAINTENANCE.equalsIgnoreCase(existing.getStatus())) {
                chipMaintenance.setChecked(true);
            } else {
                chipAvailable.setChecked(true);
            }
        }

        // Photos UI
        txtFormPhotoCount = view.findViewById(R.id.txtFormPhotoCount);
        txtFormPhotosEmpty = view.findViewById(R.id.txtFormPhotosEmpty);
        recyclerFormPhotos = view.findViewById(R.id.recyclerFormPhotos);
        recyclerFormPhotos.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));

        formThumbnailAdapter = new VehiclePhotoThumbnailAdapter(activeFormPhotos, (position, photoPath) -> {
            updateFormPhotosUI();
        });
        recyclerFormPhotos.setAdapter(formThumbnailAdapter);

        updateFormPhotosUI();

        MaterialButton btnAddPhotos = view.findViewById(R.id.btnFormAddPhotos);
        btnAddPhotos.setOnClickListener(v -> activity.launchVehiclePhotoPicker());

        // Save Button Logic
        MaterialButton btnSave = view.findViewById(R.id.btnFormSave);
        btnSave.setOnClickListener(v -> {
            layoutBrand.setError(null);
            layoutModel.setError(null);
            layoutYear.setError(null);
            layoutPlate.setError(null);
            layoutColor.setError(null);
            layoutPrice.setError(null);

            String brand = text(editBrand);
            String model = text(editModel);
            String yearStr = text(editYear);
            String plate = text(editPlate).toUpperCase(Locale.US);
            String color = text(editColor);
            String priceStr = text(editPrice);
            String desc = text(editDesc);

            boolean valid = true;
            if (TextUtils.isEmpty(brand)) {
                layoutBrand.setError("Ingresa la marca del vehículo");
                valid = false;
            }
            if (TextUtils.isEmpty(model)) {
                layoutModel.setError("Ingresa el modelo del vehículo");
                valid = false;
            }

            int year = 0;
            if (TextUtils.isEmpty(yearStr)) {
                layoutYear.setError("Ingresa el año");
                valid = false;
            } else {
                try {
                    year = Integer.parseInt(yearStr);
                    if (year < 1980 || year > 2035) {
                        layoutYear.setError("Año fuera de rango válido");
                        valid = false;
                    }
                } catch (NumberFormatException e) {
                    layoutYear.setError("Año no válido");
                    valid = false;
                }
            }

            if (TextUtils.isEmpty(plate)) {
                layoutPlate.setError("Ingresa la placa del vehículo");
                valid = false;
            } else {
                long excludeId = (existing != null) ? existing.getId() : -1;
                if (dbHelper.isPlateExists(plate, excludeId)) {
                    layoutPlate.setError("Esta placa ya está registrada");
                    valid = false;
                }
            }

            if (TextUtils.isEmpty(color)) {
                layoutColor.setError("Ingresa el color del vehículo");
                valid = false;
            }

            double price = 0.0;
            if (TextUtils.isEmpty(priceStr)) {
                layoutPrice.setError("Ingresa el precio por día");
                valid = false;
            } else {
                try {
                    price = Double.parseDouble(priceStr);
                    if (price <= 0) {
                        layoutPrice.setError("El precio debe ser mayor a 0");
                        valid = false;
                    }
                } catch (NumberFormatException e) {
                    layoutPrice.setError("Precio no válido");
                    valid = false;
                }
            }

            if (!valid) return;

            // Status from ChipGroup
            String status = Vehicle.STATUS_AVAILABLE;
            int checkedChipId = chipGroupStatus.getCheckedChipId();
            if (checkedChipId == R.id.chipStatusRented) {
                status = Vehicle.STATUS_RENTED;
            } else if (checkedChipId == R.id.chipStatusMaintenance) {
                status = Vehicle.STATUS_MAINTENANCE;
            }

            List<String> finalPhotos = (formThumbnailAdapter != null)
                    ? formThumbnailAdapter.getPhotos()
                    : new ArrayList<>(activeFormPhotos);

            if (existing == null) {
                Vehicle newVehicle = new Vehicle(0, brand, model, year, plate, color, price,
                        status, desc, finalPhotos, System.currentTimeMillis());
                long newId = dbHelper.insertVehicle(newVehicle);
                if (newId != -1) {
                    Snackbar.make(mainContent, R.string.vehicle_saved_success, Snackbar.LENGTH_SHORT).show();
                    showCatalog();
                } else {
                    Snackbar.make(mainContent, "Error al guardar vehículo", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                existing.setBrand(brand);
                existing.setModel(model);
                existing.setYear(year);
                existing.setPlate(plate);
                existing.setColor(color);
                existing.setPricePerDay(price);
                existing.setStatus(status);
                existing.setDescription(desc);
                existing.setPhotoPaths(finalPhotos);

                dbHelper.updateVehicle(existing);
                Snackbar.make(mainContent, "Vehículo actualizado correctamente", Snackbar.LENGTH_SHORT).show();
                showDetail(existing);
            }
        });
    }

    private void updateFormPhotosUI() {
        if (txtFormPhotoCount == null) return;
        int count = activeFormPhotos.size();
        txtFormPhotoCount.setText(count + (count == 1 ? " foto seleccionada" : " fotos seleccionadas"));

        if (count > 0) {
            recyclerFormPhotos.setVisibility(View.VISIBLE);
            txtFormPhotosEmpty.setVisibility(View.GONE);
        } else {
            recyclerFormPhotos.setVisibility(View.GONE);
            txtFormPhotosEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void onPhotosSelected(List<Uri> uris) {
        if (uris == null || uris.isEmpty()) return;
        List<String> savedPaths = ImageStorageHelper.saveUrisToInternalStorage(activity, uris);
        if (savedPaths != null && !savedPaths.isEmpty()) {
            activeFormPhotos.addAll(savedPaths);
            if (formThumbnailAdapter != null) {
                formThumbnailAdapter.addPhotos(savedPaths);
            }
            updateFormPhotosUI();
        }
    }

    // =========================================================================
    // 4. VISTA DE DETALLES
    // =========================================================================
    public void showDetail(Vehicle vehicle) {
        currentScreen = ScreenState.DETAIL;
        currentVehicle = vehicle;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_vehicle_detail, mainContent, false);
        mainContent.addView(view);

        ImageButton btnBack = view.findViewById(R.id.btnDetailBack);
        btnBack.setOnClickListener(v -> showCatalog());

        ImageButton btnMenu = view.findViewById(R.id.btnDetailMenu);
        btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(activity, v);
            popup.getMenu().add("Editar vehículo");
            popup.getMenu().add("Eliminar vehículo");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Editar vehículo".equals(title)) {
                    showEditForm(vehicle);
                } else if ("Eliminar vehículo".equals(title)) {
                    confirmDeleteVehicle(vehicle);
                }
                return true;
            });
            popup.show();
        });

        // Photo Gallery
        RecyclerView recyclerGallery = view.findViewById(R.id.recyclerDetailGallery);
        View layoutSingle = view.findViewById(R.id.layoutDetailSinglePhoto);
        ImageView imgSingle = view.findViewById(R.id.imgDetailSinglePhoto);

        if (vehicle.hasPhotos()) {
            if (vehicle.getPhotoPaths().size() > 1) {
                recyclerGallery.setVisibility(View.VISIBLE);
                layoutSingle.setVisibility(View.GONE);
                recyclerGallery.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
                recyclerGallery.setAdapter(new VehiclePhotoGalleryAdapter(vehicle.getPhotoPaths()));
            } else {
                recyclerGallery.setVisibility(View.GONE);
                layoutSingle.setVisibility(View.VISIBLE);
                ImageStorageHelper.loadScaledImage(imgSingle, vehicle.getFirstPhotoPath(), 600, 400, R.drawable.ic_car_placeholder);
            }
        } else {
            recyclerGallery.setVisibility(View.GONE);
            layoutSingle.setVisibility(View.VISIBLE);
            imgSingle.setImageResource(R.drawable.ic_car_placeholder);
        }

        // Header info
        TextView txtTitle = view.findViewById(R.id.txtDetailTitle);
        txtTitle.setText(vehicle.getTitle());

        TextView txtStatus = view.findViewById(R.id.txtDetailStatus);
        txtStatus.setText(vehicle.getStatus());
        if (Vehicle.STATUS_AVAILABLE.equalsIgnoreCase(vehicle.getStatus())) {
            txtStatus.setBackgroundResource(R.drawable.bg_status_available);
            txtStatus.setTextColor(activity.getResources().getColor(R.color.emerald_on_container_light, activity.getTheme()));
        } else if (Vehicle.STATUS_RENTED.equalsIgnoreCase(vehicle.getStatus())) {
            txtStatus.setBackgroundResource(R.drawable.bg_status_rented);
            txtStatus.setTextColor(android.graphics.Color.parseColor("#0369A1"));
        } else {
            txtStatus.setBackgroundResource(R.drawable.bg_status_maintenance);
            txtStatus.setTextColor(android.graphics.Color.parseColor("#B45309"));
        }

        TextView txtPlate = view.findViewById(R.id.txtDetailPlate);
        txtPlate.setText(vehicle.getPlate());

        TextView txtColor = view.findViewById(R.id.txtDetailColor);
        txtColor.setText(vehicle.getColor());

        TextView txtYear = view.findViewById(R.id.txtDetailYear);
        txtYear.setText("Año " + vehicle.getYear());

        TextView txtPrice = view.findViewById(R.id.txtDetailPrice);
        txtPrice.setText(String.format(Locale.US, "$%.2f", vehicle.getPricePerDay()));

        // Specs card
        TextView txtSpecBrand = view.findViewById(R.id.txtDetailSpecBrand);
        txtSpecBrand.setText(vehicle.getBrand());

        TextView txtSpecModel = view.findViewById(R.id.txtDetailSpecModel);
        txtSpecModel.setText(vehicle.getModel());

        TextView txtSpecYear = view.findViewById(R.id.txtDetailSpecYear);
        txtSpecYear.setText(String.valueOf(vehicle.getYear()));

        TextView txtSpecPlate = view.findViewById(R.id.txtDetailSpecPlate);
        txtSpecPlate.setText(vehicle.getPlate());

        TextView txtSpecColor = view.findViewById(R.id.txtDetailSpecColor);
        txtSpecColor.setText(vehicle.getColor());

        // Description
        TextView txtDesc = view.findViewById(R.id.txtDetailDescription);
        if (TextUtils.isEmpty(vehicle.getDescription())) {
            txtDesc.setText("Sin descripción adicional registrada.");
            txtDesc.setTextColor(activity.getResources().getColor(R.color.slate_muted, activity.getTheme()));
        } else {
            txtDesc.setText(vehicle.getDescription());
        }

        // Action Buttons: Edit & Delete
        MaterialButton btnEdit = view.findViewById(R.id.btnDetailEdit);
        btnEdit.setOnClickListener(v -> showEditForm(vehicle));

        MaterialButton btnDelete = view.findViewById(R.id.btnDetailDelete);
        btnDelete.setOnClickListener(v -> confirmDeleteVehicle(vehicle));
    }

    private void confirmDeleteVehicle(Vehicle vehicle) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.vehicle_delete_confirm_title)
                .setMessage("¿Deseas eliminar el vehículo " + vehicle.getTitle() + " con placa " + vehicle.getPlate() + "? "
                        + activity.getString(R.string.vehicle_delete_confirm_msg))
                .setNegativeButton(R.string.vehicle_btn_cancel, null)
                .setPositiveButton(R.string.vehicle_btn_delete, (dialog, which) -> {
                    dbHelper.deleteVehicle(vehicle.getId());
                    ImageStorageHelper.deletePhotoFiles(vehicle.getPhotoPaths());
                    Snackbar.make(mainContent, R.string.vehicle_deleted_success, Snackbar.LENGTH_SHORT).show();
                    showCatalog();
                })
                .show();
    }

    private String text(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
