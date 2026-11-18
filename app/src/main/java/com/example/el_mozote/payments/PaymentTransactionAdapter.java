package com.example.el_mozote.payments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionAdapter extends RecyclerView.Adapter<PaymentTransactionAdapter.TransactionViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(PaymentTransaction transaction);
    }

    private final List<PaymentTransaction> transactions = new ArrayList<>();
    private final OnTransactionClickListener listener;

    public PaymentTransactionAdapter(List<PaymentTransaction> initialData, OnTransactionClickListener listener) {
        if (initialData != null) {
            this.transactions.addAll(initialData);
        }
        this.listener = listener;
    }

    public void setTransactions(List<PaymentTransaction> newData) {
        this.transactions.clear();
        if (newData != null) {
            this.transactions.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_card, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        PaymentTransaction t = transactions.get(position);

        holder.txtReceipt.setText(t.getReceiptNumber());
        holder.txtDate.setText(t.getTransactionDate());
        holder.txtAmount.setText("+" + t.getFormattedAmount());
        holder.txtTypeBadge.setText(t.getPaymentType());
        holder.txtMethod.setText(t.getPaymentMethod());
        holder.txtNewBal.setText("Saldo rest: " + t.getFormattedNewBalance());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(t);
        });

        holder.btnReceipt.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtReceipt;
        TextView txtDate;
        TextView txtAmount;
        TextView txtTypeBadge;
        TextView txtMethod;
        TextView txtNewBal;
        MaterialButton btnReceipt;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReceipt = itemView.findViewById(R.id.txtTrxReceipt);
            txtDate = itemView.findViewById(R.id.txtTrxDate);
            txtAmount = itemView.findViewById(R.id.txtTrxAmount);
            txtTypeBadge = itemView.findViewById(R.id.txtTrxTypeBadge);
            txtMethod = itemView.findViewById(R.id.txtTrxMethod);
            txtNewBal = itemView.findViewById(R.id.txtTrxNewBal);
            btnReceipt = itemView.findViewById(R.id.btnTrxViewReceipt);
        }
    }
}
