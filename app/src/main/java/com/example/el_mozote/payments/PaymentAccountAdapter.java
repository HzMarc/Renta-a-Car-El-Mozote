package com.example.el_mozote.payments;

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
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class PaymentAccountAdapter extends RecyclerView.Adapter<PaymentAccountAdapter.AccountViewHolder> {

    public interface OnAccountClickListener {
        void onAccountClick(PaymentAccount account);
    }

    private final List<PaymentAccount> accounts = new ArrayList<>();
    private final OnAccountClickListener listener;

    public PaymentAccountAdapter(List<PaymentAccount> initialData, OnAccountClickListener listener) {
        if (initialData != null) {
            this.accounts.addAll(initialData);
        }
        this.listener = listener;
    }

    public void setAccounts(List<PaymentAccount> newData) {
        this.accounts.clear();
        if (newData != null) {
            this.accounts.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_card, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        PaymentAccount acc = accounts.get(position);
        Context context = holder.itemView.getContext();

        holder.txtContract.setText(acc.getContractNumber());
        holder.txtCustomer.setText(acc.getCustomerName());
        holder.txtVehicle.setText(acc.getVehicleTitle() + " (" + acc.getVehiclePlate() + ")");
        holder.txtTotalCost.setText(acc.getFormattedTotalRentCost());
        holder.txtTotalPaid.setText(acc.getFormattedTotalPaid());
        holder.txtPendingBalance.setText(acc.getFormattedPendingBalance());

        int pct = acc.getPaymentPercentage();
        holder.progressBar.setProgress(pct);
        holder.txtPercentage.setText(pct + "% cubierto");

        String status = acc.getPaymentStatus();
        holder.txtStatusBadge.setText(status);

        if (PaymentAccount.STATUS_PAID.equalsIgnoreCase(status)) {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.badge_status_paid);
            holder.txtStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.emerald_on_container_light));
            holder.btnAction.setText("Ver recibo");
        } else if (PaymentAccount.STATUS_PARTIAL.equalsIgnoreCase(status)) {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.badge_status_partial);
            holder.txtStatusBadge.setTextColor(Color.parseColor("#0369A1"));
            holder.btnAction.setText("Cobrar saldo");
        } else {
            holder.txtStatusBadge.setBackgroundResource(R.drawable.badge_status_unpaid);
            holder.txtStatusBadge.setTextColor(Color.parseColor("#B45309"));
            holder.btnAction.setText("Procesar cobro");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAccountClick(acc);
        });

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onAccountClick(acc);
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView txtContract;
        TextView txtStatusBadge;
        TextView txtCustomer;
        TextView txtVehicle;
        TextView txtTotalCost;
        TextView txtTotalPaid;
        TextView txtPendingBalance;
        LinearProgressIndicator progressBar;
        TextView txtPercentage;
        MaterialButton btnAction;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            txtContract = itemView.findViewById(R.id.txtPayContractNumber);
            txtStatusBadge = itemView.findViewById(R.id.txtPayStatusBadge);
            txtCustomer = itemView.findViewById(R.id.txtPayCustomerName);
            txtVehicle = itemView.findViewById(R.id.txtPayVehicleInfo);
            txtTotalCost = itemView.findViewById(R.id.txtPayTotalCost);
            txtTotalPaid = itemView.findViewById(R.id.txtPayTotalPaid);
            txtPendingBalance = itemView.findViewById(R.id.txtPayPendingBalance);
            progressBar = itemView.findViewById(R.id.progressPayment);
            txtPercentage = itemView.findViewById(R.id.txtPayPercentage);
            btnAction = itemView.findViewById(R.id.btnPayAction);
        }
    }
}
