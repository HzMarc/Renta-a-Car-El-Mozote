package com.example.el_mozote.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;
import com.example.el_mozote.reservations.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleRentalHistoryAdapter extends RecyclerView.Adapter<VehicleRentalHistoryAdapter.HistoryViewHolder> {

    private final List<Reservation> items = new ArrayList<>();

    public VehicleRentalHistoryAdapter(List<Reservation> initialData) {
        if (initialData != null) items.addAll(initialData);
    }

    public void submitList(List<Reservation> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_history_card, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Reservation res = items.get(position);

        holder.txtCustName.setText(res.getCustomerName());
        holder.txtCost.setText(String.format(Locale.US, "$%.2f", res.getTotalCost()));
        holder.txtDates.setText(res.getStartDate() + " al " + res.getEndDate() + " (" + res.getTotalDays() + " días)");
        holder.txtStatus.setText(res.getStatus());

        if (Reservation.STATUS_COMPLETED.equalsIgnoreCase(res.getStatus())) {
            holder.txtStatus.setBackgroundResource(R.drawable.badge_status_paid);
            holder.txtStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.emerald_on_container_light, holder.itemView.getContext().getTheme()));
        } else if (Reservation.STATUS_ACTIVE.equalsIgnoreCase(res.getStatus())) {
            holder.txtStatus.setBackgroundResource(R.drawable.badge_status_partial);
            holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#0369A1"));
        } else {
            holder.txtStatus.setBackgroundResource(R.drawable.badge_status_unpaid);
            holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#B45309"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCustName, txtCost, txtDates, txtStatus;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCustName = itemView.findViewById(R.id.txtHistoryCustName);
            txtCost = itemView.findViewById(R.id.txtHistoryCost);
            txtDates = itemView.findViewById(R.id.txtHistoryDates);
            txtStatus = itemView.findViewById(R.id.txtHistoryStatus);
        }
    }
}
