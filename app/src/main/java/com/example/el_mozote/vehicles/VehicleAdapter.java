package com.example.el_mozote.vehicles;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    public interface OnVehicleActionListener {
        void onVehicleClick(Vehicle vehicle);
        void onVehicleEdit(Vehicle vehicle);
        void onVehicleDelete(Vehicle vehicle);
        void onVehicleStatusChange(Vehicle vehicle, String newStatus);
    }

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final OnVehicleActionListener listener;

    public VehicleAdapter(List<Vehicle> initialVehicles, OnVehicleActionListener listener) {
        if (initialVehicles != null) {
            this.vehicles.addAll(initialVehicles);
        }
        this.listener = listener;
    }

    public void setVehicles(List<Vehicle> newVehicles) {
        this.vehicles.clear();
        if (newVehicles != null) {
            this.vehicles.addAll(newVehicles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_card, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        Context context = holder.itemView.getContext();

        holder.txtTitle.setText(vehicle.getTitle());
        holder.txtPlate.setText(vehicle.getPlate());
        holder.txtYear.setText(String.valueOf(vehicle.getYear()));
        holder.txtColor.setText(vehicle.getColor());
        holder.txtPrice.setText(vehicle.getFormattedPrice());

        // Status badge
        String status = vehicle.getStatus();
        holder.txtStatus.setText(status);
        if (Vehicle.STATUS_AVAILABLE.equalsIgnoreCase(status)) {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_available);
            holder.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.emerald_on_container_light));
        } else if (Vehicle.STATUS_RENTED.equalsIgnoreCase(status)) {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_rented);
            holder.txtStatus.setTextColor(Color.parseColor("#0369A1"));
        } else {
            holder.txtStatus.setBackgroundResource(R.drawable.bg_status_maintenance);
            holder.txtStatus.setTextColor(Color.parseColor("#B45309"));
        }

        // Photo & Multiple Photos Badge
        if (vehicle.hasPhotos()) {
            ImageStorageHelper.loadScaledImage(holder.imgVehicle, vehicle.getFirstPhotoPath(),
                    400, 260, R.drawable.ic_car_placeholder);
            if (vehicle.getPhotoPaths().size() > 1) {
                holder.txtPhotoCountBadge.setVisibility(View.VISIBLE);
                holder.txtPhotoCountBadge.setText("📷 " + vehicle.getPhotoPaths().size());
            } else {
                holder.txtPhotoCountBadge.setVisibility(View.GONE);
            }
        } else {
            holder.imgVehicle.setImageResource(R.drawable.ic_car_placeholder);
            holder.txtPhotoCountBadge.setVisibility(View.GONE);
        }

        // Clicks
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVehicleClick(vehicle);
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onVehicleClick(vehicle);
        });

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add("Ver detalles");
            popup.getMenu().add("Editar");
            if (!Vehicle.STATUS_AVAILABLE.equals(vehicle.getStatus())) {
                popup.getMenu().add("Marcar como Disponible");
            }
            if (!Vehicle.STATUS_RENTED.equals(vehicle.getStatus())) {
                popup.getMenu().add("Marcar como Rentado");
            }
            if (!Vehicle.STATUS_MAINTENANCE.equals(vehicle.getStatus())) {
                popup.getMenu().add("Marcar en Mantenimiento");
            }
            popup.getMenu().add("Eliminar");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (listener == null) return false;

                if ("Ver detalles".equals(title)) {
                    listener.onVehicleClick(vehicle);
                } else if ("Editar".equals(title)) {
                    listener.onVehicleEdit(vehicle);
                } else if ("Marcar como Disponible".equals(title)) {
                    listener.onVehicleStatusChange(vehicle, Vehicle.STATUS_AVAILABLE);
                } else if ("Marcar como Rentado".equals(title)) {
                    listener.onVehicleStatusChange(vehicle, Vehicle.STATUS_RENTED);
                } else if ("Marcar en Mantenimiento".equals(title)) {
                    listener.onVehicleStatusChange(vehicle, Vehicle.STATUS_MAINTENANCE);
                } else if ("Eliminar".equals(title)) {
                    listener.onVehicleDelete(vehicle);
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVehicle;
        TextView txtPhotoCountBadge;
        TextView txtStatus;
        TextView txtTitle;
        TextView txtPlate;
        TextView txtYear;
        TextView txtColor;
        TextView txtPrice;
        ImageButton btnMenu;
        MaterialButton btnViewDetails;

        VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVehicle = itemView.findViewById(R.id.imgVehicleCard);
            txtPhotoCountBadge = itemView.findViewById(R.id.txtPhotoCountBadge);
            txtStatus = itemView.findViewById(R.id.txtStatusBadge);
            txtTitle = itemView.findViewById(R.id.txtVehicleTitle);
            txtPlate = itemView.findViewById(R.id.txtVehiclePlate);
            txtYear = itemView.findViewById(R.id.txtVehicleYear);
            txtColor = itemView.findViewById(R.id.txtVehicleColor);
            txtPrice = itemView.findViewById(R.id.txtVehiclePrice);
            btnMenu = itemView.findViewById(R.id.btnVehicleMenu);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
