package com.example.el_mozote.statistics;

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
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class VehicleStatAdapter extends RecyclerView.Adapter<VehicleStatAdapter.StatViewHolder> {

    public interface OnVehicleStatClickListener {
        void onVehicleStatClick(VehicleStat stat);
    }

    private final List<VehicleStat> items = new ArrayList<>();
    private final OnVehicleStatClickListener listener;

    public VehicleStatAdapter(List<VehicleStat> initialData, OnVehicleStatClickListener listener) {
        if (initialData != null) items.addAll(initialData);
        this.listener = listener;
    }

    public void submitList(List<VehicleStat> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_stat_card, parent, false);
        return new StatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        VehicleStat stat = items.get(position);
        Context context = holder.itemView.getContext();

        holder.txtRank.setText("#" + (position + 1));
        holder.txtTitle.setText(stat.getTitle());
        holder.txtPlateAndRate.setText("Placa: " + stat.getPlate() + "  •  " + stat.getFormattedDailyRate());

        holder.txtRevenue.setText(stat.getFormattedRevenue());
        holder.txtRentals.setText(stat.getTotalRentals() + (stat.getTotalRentals() == 1 ? " viaje" : " viajes"));
        holder.txtDays.setText(stat.getTotalDaysRented() + " días");
        holder.txtKm.setText(stat.getFormattedKm());
        holder.txtUtilization.setText("Utilización: " + stat.getFormattedUtilization());

        holder.progressBar.setProgress((int) Math.round(stat.getRoiIndex()));

        // Badge styling
        String badge = stat.getFeasibilityBadge();
        holder.txtBadge.setText(badge);

        if ("Alta Rentabilidad".equalsIgnoreCase(badge)) {
            holder.txtBadge.setBackgroundResource(R.drawable.badge_feasibility_high);
            holder.txtBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_on_container_light));
        } else if ("Rendimiento Moderado".equalsIgnoreCase(badge)) {
            holder.txtBadge.setBackgroundResource(R.drawable.badge_feasibility_medium);
            holder.txtBadge.setTextColor(Color.parseColor("#0369A1"));
        } else if ("Mantenimiento".equalsIgnoreCase(badge)) {
            holder.txtBadge.setBackgroundResource(R.drawable.badge_status_unpaid);
            holder.txtBadge.setTextColor(Color.parseColor("#B45309"));
        } else {
            holder.txtBadge.setBackgroundResource(R.drawable.badge_feasibility_low);
            holder.txtBadge.setTextColor(Color.parseColor("#B91C1C"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVehicleStatClick(stat);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class StatViewHolder extends RecyclerView.ViewHolder {
        TextView txtRank, txtTitle, txtPlateAndRate, txtBadge;
        TextView txtRevenue, txtRentals, txtDays, txtKm, txtUtilization;
        LinearProgressIndicator progressBar;

        StatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRank = itemView.findViewById(R.id.txtStatRank);
            txtTitle = itemView.findViewById(R.id.txtStatVehicleTitle);
            txtPlateAndRate = itemView.findViewById(R.id.txtStatPlateAndRate);
            txtBadge = itemView.findViewById(R.id.txtStatFeasibilityBadge);
            txtRevenue = itemView.findViewById(R.id.txtStatRevenue);
            txtRentals = itemView.findViewById(R.id.txtStatRentalsCount);
            txtDays = itemView.findViewById(R.id.txtStatDays);
            txtKm = itemView.findViewById(R.id.txtStatKm);
            txtUtilization = itemView.findViewById(R.id.txtStatUtilization);
            progressBar = itemView.findViewById(R.id.progressStatScore);
        }
    }
}
