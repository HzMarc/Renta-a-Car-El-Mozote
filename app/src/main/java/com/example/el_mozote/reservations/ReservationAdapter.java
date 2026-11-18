package com.example.el_mozote.reservations;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
    }

    private final List<Reservation> reservations = new ArrayList<>();
    private final OnReservationClickListener listener;

    public ReservationAdapter(List<Reservation> initialData, OnReservationClickListener listener) {
        if (initialData != null) {
            this.reservations.addAll(initialData);
        }
        this.listener = listener;
    }

    public void setReservations(List<Reservation> newData) {
        this.reservations.clear();
        if (newData != null) {
            this.reservations.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation_card, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation res = reservations.get(position);
        Context context = holder.itemView.getContext();

        holder.txtVehicleTitle.setText(res.getVehicleTitle());
        holder.txtVehiclePlate.setText(res.getVehiclePlate());
        holder.txtCustomerName.setText(res.getCustomerName());
        holder.txtCustomerDui.setText("DUI: " + res.getCustomerDui());
        holder.txtDates.setText(res.getStartDate() + "  ➔  " + res.getEndDate());
        holder.txtDaysCount.setText(res.getTotalDays() + (res.getTotalDays() == 1 ? " día" : " días"));
        holder.txtTotalCost.setText(res.getFormattedTotalCost());

        // Status badge styling
        String status = res.getStatus();
        holder.txtStatusBadge.setText(status);

        if (Reservation.STATUS_RESERVED.equalsIgnoreCase(status)) {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.badge_status_reserved);
            holder.txtStatusBadge.setTextColor(Color.parseColor("#6D28D9"));
        } else if (Reservation.STATUS_ACTIVE.equalsIgnoreCase(status)) {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.bg_status_rented);
            holder.txtStatusBadge.setTextColor(Color.parseColor("#0369A1"));
        } else if (Reservation.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.bg_status_available);
            holder.txtStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_on_container_light));
        } else {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.badge_status_cancelled);
            holder.txtStatusBadge.setTextColor(Color.parseColor("#B91C1C"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReservationClick(res);
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onReservationClick(res);
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView txtVehicleTitle;
        TextView txtVehiclePlate;
        TextView txtStatusBadge;
        TextView txtCustomerName;
        TextView txtCustomerDui;
        TextView txtDates;
        TextView txtDaysCount;
        TextView txtTotalCost;
        MaterialButton btnViewDetails;

        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVehicleTitle = itemView.findViewById(R.id.txtResVehicleTitle);
            txtVehiclePlate = itemView.findViewById(R.id.txtResVehiclePlate);
            txtStatusBadge = itemView.findViewById(R.id.txtResStatusBadge);
            txtCustomerName = itemView.findViewById(R.id.txtResCustomerName);
            txtCustomerDui = itemView.findViewById(R.id.txtResCustomerDui);
            txtDates = itemView.findViewById(R.id.txtResDates);
            txtDaysCount = itemView.findViewById(R.id.txtResDaysCount);
            txtTotalCost = itemView.findViewById(R.id.txtResTotalCost);
            btnViewDetails = itemView.findViewById(R.id.btnResViewDetails);
        }
    }
}
