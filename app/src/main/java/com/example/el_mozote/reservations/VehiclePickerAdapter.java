package com.example.el_mozote.reservations;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;
import com.example.el_mozote.vehicles.ImageStorageHelper;
import com.example.el_mozote.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehiclePickerAdapter extends RecyclerView.Adapter<VehiclePickerAdapter.PickerViewHolder> {

    public interface OnVehicleSelectedListener {
        void onSelected(Vehicle vehicle);
    }

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final OnVehicleSelectedListener listener;

    public VehiclePickerAdapter(List<Vehicle> availableVehicles, OnVehicleSelectedListener listener) {
        if (availableVehicles != null) {
            this.vehicles.addAll(availableVehicles);
        }
        this.listener = listener;
    }

    @NonNull
    @Override
    public PickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_picker, parent, false);
        return new PickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickerViewHolder holder, int position) {
        Vehicle v = vehicles.get(position);

        holder.txtTitle.setText(v.getTitle());
        holder.txtPlate.setText(v.getPlate());
        holder.txtYear.setText("Año " + v.getYear());
        holder.txtRate.setText(v.getFormattedPrice());

        if (v.hasPhotos()) {
            ImageStorageHelper.loadScaledImage(holder.imgVehicle, v.getFirstPhotoPath(),
                    120, 100, R.drawable.ic_car_placeholder);
        } else {
            holder.imgVehicle.setImageResource(R.drawable.ic_car_placeholder);
        }

        holder.itemView.setOnClickListener(view -> {
            if (listener != null) listener.onSelected(v);
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class PickerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVehicle;
        TextView txtTitle;
        TextView txtPlate;
        TextView txtYear;
        TextView txtRate;

        PickerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgVehicle = itemView.findViewById(R.id.imgPickerVehicle);
            txtTitle = itemView.findViewById(R.id.txtPickerTitle);
            txtPlate = itemView.findViewById(R.id.txtPickerPlate);
            txtYear = itemView.findViewById(R.id.txtPickerYear);
            txtRate = itemView.findViewById(R.id.txtPickerRate);
        }
    }
}
