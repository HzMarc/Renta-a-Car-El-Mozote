package com.example.el_mozote.payments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.el_mozote.MainActivity;
import com.example.el_mozote.R;
import com.example.el_mozote.reservations.Reservation;
import com.example.el_mozote.reservations.ReservationDatabaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentController {

    public enum ScreenState {
        LIST,
        DETAIL,
        FORM,
        RECEIPT
    }

    private final MainActivity activity;
    private final FrameLayout mainContent;
    private final PaymentDatabaseHelper payDbHelper;
    private final ReservationDatabaseHelper resDbHelper;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);

    private ScreenState currentScreen = ScreenState.LIST;
    private PaymentAccount currentAccount = null;
    private PaymentTransaction currentTransaction = null;

    // List view state
    private PaymentAccountAdapter accountAdapter;
    private PaymentTransactionAdapter allTrxAdapter;
    private String currentSearchQuery = "";
    private String currentFilterStatus = "Todos";

    private TextView txtStatBilled, txtStatCollected, txtStatPending, txtStatCountPending;
    private LinearLayout layoutEmpty;
    private RecyclerView recyclerList;

    public PaymentController(MainActivity activity, FrameLayout mainContent) {
        this.activity = activity;
        this.mainContent = mainContent;
        this.payDbHelper = PaymentDatabaseHelper.getInstance(activity);
        this.resDbHelper = ReservationDatabaseHelper.getInstance(activity);
    }

    public ScreenState getCurrentScreen() {
        return currentScreen;
    }

    public boolean handleBackPressed() {
        if (currentScreen == ScreenState.DETAIL) {
            showPaymentList();
            return true;
        } else if (currentScreen == ScreenState.FORM) {
            if (currentAccount != null) {
                showPaymentDetail(currentAccount);
            } else {
                showPaymentList();
            }
            return true;
        } else if (currentScreen == ScreenState.RECEIPT) {
            if (currentAccount != null) {
                showPaymentDetail(currentAccount);
            } else {
                showPaymentList();
            }
            return true;
        }
        return false;
    }

    private void syncReservations() {
        List<Reservation> allRes = resDbHelper.searchReservations(null, null);
        payDbHelper.syncAllReservations(allRes);
    }

    // =========================================================================
    // 1. LISTADO DE COBROS Y RENTAS PENDIENTES
    // =========================================================================
    public void showPaymentList() {
        currentScreen = ScreenState.LIST;
        currentAccount = null;
        currentTransaction = null;
        mainContent.removeAllViews();

        syncReservations();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_payment_list, mainContent, false);
        mainContent.addView(view);

        txtStatBilled = view.findViewById(R.id.txtPayStatBilled);
        txtStatCollected = view.findViewById(R.id.txtPayStatCollected);
        txtStatPending = view.findViewById(R.id.txtPayStatPending);
        txtStatCountPending = view.findViewById(R.id.txtPayStatCountPending);
        layoutEmpty = view.findViewById(R.id.layoutEmptyPayments);

        recyclerList = view.findViewById(R.id.recyclerPayments);
        recyclerList.setLayoutManager(new LinearLayoutManager(activity));

        accountAdapter = new PaymentAccountAdapter(new ArrayList<>(), this::showPaymentDetail);
        allTrxAdapter = new PaymentTransactionAdapter(new ArrayList<>(), trx -> {
            PaymentAccount acc = payDbHelper.getAccountById(trx.getPaymentId());
            showReceipt(trx, acc);
        });

        recyclerList.setAdapter(accountAdapter);

        // Search Box
        TextInputEditText editSearch = view.findViewById(R.id.editPaymentSearch);
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
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupPayStatus);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipPayPending) currentFilterStatus = PaymentAccount.STATUS_PENDING;
            else if (id == R.id.chipPayPartial) currentFilterStatus = PaymentAccount.STATUS_PARTIAL;
            else if (id == R.id.chipPayPaid) currentFilterStatus = PaymentAccount.STATUS_PAID;
            else if (id == R.id.chipPayHistory) currentFilterStatus = "Transacciones";
            else currentFilterStatus = "Todos";
            refreshListData();
        });

        refreshListData();
    }

    private void refreshListData() {
        if (txtStatBilled != null) {
            txtStatBilled.setText(String.format(Locale.US, "$%.2f", payDbHelper.getTotalBilled()));
            txtStatCollected.setText(String.format(Locale.US, "$%.2f", payDbHelper.getTotalCollected()));
            txtStatPending.setText(String.format(Locale.US, "$%.2f", payDbHelper.getTotalPending()));
            txtStatCountPending.setText(String.valueOf(payDbHelper.getCountPendingAccounts()));
        }

        if ("Transacciones".equalsIgnoreCase(currentFilterStatus)) {
            recyclerList.setAdapter(allTrxAdapter);
            List<PaymentTransaction> trxs = payDbHelper.getAllTransactions();
            allTrxAdapter.setTransactions(trxs);
            layoutEmpty.setVisibility(trxs.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            recyclerList.setAdapter(accountAdapter);
            List<PaymentAccount> accounts = payDbHelper.searchPaymentAccounts(currentSearchQuery, currentFilterStatus);
            accountAdapter.setAccounts(accounts);
            layoutEmpty.setVisibility(accounts.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    // =========================================================================
    // 2. DETALLE DEL COBRO (ESTADO DE CUENTA)
    // =========================================================================
    public void showPaymentDetail(PaymentAccount account) {
        currentScreen = ScreenState.DETAIL;
        currentAccount = account;
        mainContent.removeAllViews();

        // Refresh latest data from DB
        PaymentAccount fresh = payDbHelper.getAccountById(account.getId());
        if (fresh != null) {
            currentAccount = fresh;
            account = fresh;
        }

        View view = LayoutInflater.from(activity).inflate(R.layout.view_payment_detail, mainContent, false);
        mainContent.addView(view);

        ImageButton btnBack = view.findViewById(R.id.btnPayDetailBack);
        btnBack.setOnClickListener(v -> showPaymentList());

        MaterialButton btnClose = view.findViewById(R.id.btnPayDetailClose);
        btnClose.setOnClickListener(v -> showPaymentList());

        // Header info
        TextView txtContract = view.findViewById(R.id.txtDetailContractNumber);
        TextView txtBadge = view.findViewById(R.id.txtDetailStatusBadge);
        txtContract.setText("CONTRATO #" + account.getContractNumber());

        String status = account.getPaymentStatus();
        txtBadge.setText(status);
        if (PaymentAccount.STATUS_PAID.equalsIgnoreCase(status)) {
            txtBadge.setBackgroundResource(R.drawable.badge_status_paid);
            txtBadge.setTextColor(activity.getResources().getColor(R.color.emerald_on_container_light, activity.getTheme()));
        } else if (PaymentAccount.STATUS_PARTIAL.equalsIgnoreCase(status)) {
            txtBadge.setBackgroundResource(R.drawable.badge_status_partial);
            txtBadge.setTextColor(android.graphics.Color.parseColor("#0369A1"));
        } else {
            txtBadge.setBackgroundResource(R.drawable.badge_status_unpaid);
            txtBadge.setTextColor(android.graphics.Color.parseColor("#B45309"));
        }

        // Financial balances
        TextView txtPending = view.findViewById(R.id.txtDetailPendingBalance);
        TextView txtTotal = view.findViewById(R.id.txtDetailTotalCost);
        TextView txtPaid = view.findViewById(R.id.txtDetailTotalPaid);
        LinearProgressIndicator progress = view.findViewById(R.id.progressDetailPayment);
        TextView txtPct = view.findViewById(R.id.txtDetailPercentage);

        txtPending.setText(account.getFormattedPendingBalance());
        txtTotal.setText(account.getFormattedTotalRentCost());
        txtPaid.setText(account.getFormattedTotalPaid());

        int pct = account.getPaymentPercentage();
        progress.setProgress(pct);
        txtPct.setText(pct + "% cubierto de la tarifa total");

        // Customer & Vehicle
        TextView txtCust = view.findViewById(R.id.txtDetailCustName);
        TextView txtPhone = view.findViewById(R.id.txtDetailCustPhone);
        TextView txtVeh = view.findViewById(R.id.txtDetailVehicleInfo);

        txtCust.setText(account.getCustomerName());
        txtPhone.setText("DUI: " + account.getCustomerDui() + "  •  Tel: " + account.getCustomerPhone());
        txtVeh.setText(account.getVehicleTitle() + "  •  Placa: " + account.getVehiclePlate());

        // Actions: Pay Full & Pay Abono
        View layoutActions = view.findViewById(R.id.layoutPaymentActions);
        View badgeFullyPaid = view.findViewById(R.id.txtFullyPaidBadge);
        MaterialButton btnPayFull = view.findViewById(R.id.btnPayFull);
        MaterialButton btnPayAbono = view.findViewById(R.id.btnPayAbono);

        if (account.isFullyPaid()) {
            layoutActions.setVisibility(View.GONE);
            badgeFullyPaid.setVisibility(View.VISIBLE);
        } else {
            layoutActions.setVisibility(View.VISIBLE);
            badgeFullyPaid.setVisibility(View.GONE);

            btnPayFull.setText("Pagar Totalidad (" + account.getFormattedPendingBalance() + ")");
            final PaymentAccount targetAcc = account;
            btnPayFull.setOnClickListener(v -> showPaymentForm(targetAcc, true));
            btnPayAbono.setOnClickListener(v -> showPaymentForm(targetAcc, false));
        }

        // Transactions of this contract
        RecyclerView recyclerTrx = view.findViewById(R.id.recyclerContractTransactions);
        TextView txtNoTrx = view.findViewById(R.id.txtNoContractTransactions);
        recyclerTrx.setLayoutManager(new LinearLayoutManager(activity));

        List<PaymentTransaction> trxs = payDbHelper.getTransactionsByPaymentId(account.getId());
        final PaymentAccount finalAcc = account;
        PaymentTransactionAdapter trxAdapter = new PaymentTransactionAdapter(trxs, trx -> {
            showReceipt(trx, finalAcc);
        });
        recyclerTrx.setAdapter(trxAdapter);

        txtNoTrx.setVisibility(trxs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void openPaymentForReservation(long reservationId) {
        Reservation res = resDbHelper.getReservationById(reservationId);
        if (res != null) {
            PaymentAccount account = payDbHelper.syncFromReservation(res);
            if (account != null) {
                showPaymentDetail(account);
            }
        }
    }

    // =========================================================================
    // 3. REGISTRAR PAGO TOTAL / REGISTRAR ABONO
    // =========================================================================
    public void showPaymentForm(PaymentAccount account, boolean isFullPayment) {
        currentScreen = ScreenState.FORM;
        currentAccount = account;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_payment_form, mainContent, false);
        mainContent.addView(view);

        ImageButton btnBack = view.findViewById(R.id.btnPayFormBack);
        btnBack.setOnClickListener(v -> showPaymentDetail(account));

        MaterialButton btnCancel = view.findViewById(R.id.btnPayCancel);
        btnCancel.setOnClickListener(v -> showPaymentDetail(account));

        TextView txtTitle = view.findViewById(R.id.txtPayFormTitle);
        txtTitle.setText(isFullPayment ? "Registrar Pago Total" : "Registrar Abono");

        // Header info
        TextView txtContract = view.findViewById(R.id.txtPayFormContract);
        TextView txtClient = view.findViewById(R.id.txtPayFormClient);
        TextView txtVehicle = view.findViewById(R.id.txtPayFormVehicle);
        TextView txtPendingBal = view.findViewById(R.id.txtPayFormPendingBalance);

        txtContract.setText("CONTRATO #" + account.getContractNumber());
        txtClient.setText("Cliente: " + account.getCustomerName());
        txtVehicle.setText("Vehículo: " + account.getVehicleTitle() + " (" + account.getVehiclePlate() + ")");
        txtPendingBal.setText(account.getFormattedPendingBalance());

        // Amount input & live calculation
        TextInputLayout layoutAmount = view.findViewById(R.id.layoutPayAmount);
        TextInputEditText editAmount = view.findViewById(R.id.editPayAmount);
        TextView txtResultBal = view.findViewById(R.id.txtPayFormResultingBalance);
        ChipGroup chipGroupMethod = view.findViewById(R.id.chipGroupPayMethod);
        TextInputEditText editNotes = view.findViewById(R.id.editPayNotes);

        if (isFullPayment) {
            editAmount.setText(String.format(Locale.US, "%.2f", account.getPendingBalance()));
            txtResultBal.setText("$0.00 (Saldado)");
        } else {
            editAmount.setText("");
            txtResultBal.setText(account.getFormattedPendingBalance());
        }

        editAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double input = Double.parseDouble(s.toString().trim());
                    double rem = Math.max(0.0, account.getPendingBalance() - input);
                    if (rem <= 0.009) {
                        txtResultBal.setText("$0.00 (Quedará Saldado)");
                    } else {
                        txtResultBal.setText(String.format(Locale.US, "$%.2f", rem));
                    }
                } catch (Exception e) {
                    txtResultBal.setText(account.getFormattedPendingBalance());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Submit Button
        MaterialButton btnSubmit = view.findViewById(R.id.btnPaySubmit);
        btnSubmit.setText(isFullPayment
                ? "Confirmar Pago Total de " + account.getFormattedPendingBalance()
                : "Confirmar y Procesar Abono");

        btnSubmit.setOnClickListener(v -> {
            layoutAmount.setError(null);
            String amtStr = editAmount.getText() != null ? editAmount.getText().toString().trim() : "";
            if (TextUtils.isEmpty(amtStr)) {
                layoutAmount.setError("Ingresa el monto a pagar");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr);
            } catch (NumberFormatException e) {
                layoutAmount.setError("Monto inválido");
                return;
            }

            // REGLAS OBLIGATORIAS:
            // 1. No aceptar pagos <= 0
            if (amount <= 0) {
                layoutAmount.setError("El monto debe ser mayor a $0.00");
                return;
            }

            // 2. No permitir abonos superiores al saldo pendiente
            if (amount > account.getPendingBalance() + 0.009) {
                layoutAmount.setError("El monto ($" + String.format(Locale.US, "%.2f", amount)
                        + ") no puede superar el saldo pendiente (" + account.getFormattedPendingBalance() + ")");
                return;
            }

            // Método de pago
            String method = PaymentTransaction.METHOD_CASH;
            int checkedId = chipGroupMethod.getCheckedChipId();
            if (checkedId == R.id.chipMethodCard) method = PaymentTransaction.METHOD_CARD;
            else if (checkedId == R.id.chipMethodTransfer) method = PaymentTransaction.METHOD_TRANSFER;
            else if (checkedId == R.id.chipMethodCheck) method = PaymentTransaction.METHOD_CHECK;

            String type = (isFullPayment || Math.abs(amount - account.getPendingBalance()) < 0.01)
                    ? PaymentTransaction.TYPE_FULL
                    : PaymentTransaction.TYPE_PARTIAL;

            String receiptNum = payDbHelper.generateNextReceiptNumber();
            String dateStr = dateTimeFormat.format(new Date());
            String notes = editNotes.getText() != null ? editNotes.getText().toString().trim() : "";

            PaymentTransaction trx = new PaymentTransaction(0, account.getId(), account.getReservationId(),
                    receiptNum, amount, method, type, account.getPendingBalance(),
                    account.getPendingBalance() - amount, notes, dateStr, System.currentTimeMillis(),
                    account.getCustomerName(), account.getContractNumber(), account.getVehicleTitle());

            boolean success = payDbHelper.recordTransaction(trx);
            if (success) {
                Snackbar.make(mainContent, R.string.payment_saved_success, Snackbar.LENGTH_SHORT).show();
                showReceipt(trx, account);
            } else {
                Snackbar.make(mainContent, "Error al registrar la transacción", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================================
    // 4. COMPROBANTE OFICIAL DE PAGO
    // =========================================================================
    public void showReceipt(PaymentTransaction trx, PaymentAccount account) {
        currentScreen = ScreenState.RECEIPT;
        currentTransaction = trx;
        if (account != null) currentAccount = account;
        mainContent.removeAllViews();

        View view = LayoutInflater.from(activity).inflate(R.layout.view_payment_receipt, mainContent, false);
        mainContent.addView(view);

        ImageButton btnBack = view.findViewById(R.id.btnReceiptBack);
        btnBack.setOnClickListener(v -> {
            if (currentAccount != null) showPaymentDetail(currentAccount);
            else showPaymentList();
        });

        MaterialButton btnReturn = view.findViewById(R.id.btnReceiptViewAccount);
        btnReturn.setOnClickListener(v -> {
            if (currentAccount != null) showPaymentDetail(currentAccount);
            else showPaymentList();
        });

        // Receipt fields
        TextView txtReceipt = view.findViewById(R.id.txtReceiptNumber);
        TextView txtDate = view.findViewById(R.id.txtReceiptDate);
        TextView txtAmount = view.findViewById(R.id.txtReceiptAmount);
        TextView txtTypeBadge = view.findViewById(R.id.txtReceiptTypeBadge);
        TextView txtContract = view.findViewById(R.id.txtReceiptContract);
        TextView txtCustomer = view.findViewById(R.id.txtReceiptCustomer);
        TextView txtVehicle = view.findViewById(R.id.txtReceiptVehicle);
        TextView txtMethod = view.findViewById(R.id.txtReceiptMethod);
        TextView txtPrevBal = view.findViewById(R.id.txtReceiptPrevBalance);
        TextView txtNewBal = view.findViewById(R.id.txtReceiptNewBalance);
        TextView txtNotes = view.findViewById(R.id.txtReceiptNotes);

        txtReceipt.setText(trx.getReceiptNumber());
        txtDate.setText(trx.getTransactionDate());
        txtAmount.setText(trx.getFormattedAmount());
        txtTypeBadge.setText(trx.getPaymentType());
        txtContract.setText(trx.getContractNumber());
        txtCustomer.setText(trx.getCustomerName());
        txtVehicle.setText(trx.getVehicleTitle());
        txtMethod.setText(trx.getPaymentMethod());
        txtPrevBal.setText(trx.getFormattedPreviousBalance());
        txtNewBal.setText(trx.getFormattedNewBalance());

        if (TextUtils.isEmpty(trx.getNotes())) {
            txtNotes.setText("Notas: Ninguna referencia adicional.");
        } else {
            txtNotes.setText("Notas: " + trx.getNotes());
        }

        // Copy button
        MaterialButton btnCopy = view.findViewById(R.id.btnReceiptCopy);
        btnCopy.setOnClickListener(v -> {
            String receiptText = "-------------------------------------\n"
                    + "RENT-A-CAR EL MOZOTE\n"
                    + "COMPROBANTE DE PAGO #" + trx.getReceiptNumber() + "\n"
                    + "Fecha: " + trx.getTransactionDate() + "\n"
                    + "Contrato: " + trx.getContractNumber() + "\n"
                    + "Cliente: " + trx.getCustomerName() + "\n"
                    + "Vehículo: " + trx.getVehicleTitle() + "\n"
                    + "-------------------------------------\n"
                    + "Tipo: " + trx.getPaymentType() + "\n"
                    + "Método: " + trx.getPaymentMethod() + "\n"
                    + "MONTO PAGADO: " + trx.getFormattedAmount() + "\n"
                    + "Saldo anterior: " + trx.getFormattedPreviousBalance() + "\n"
                    + "NUEVO SALDO: " + trx.getFormattedNewBalance() + "\n"
                    + "Notas: " + trx.getNotes() + "\n"
                    + "✓ TRANSACCIÓN VERIFICADA\n"
                    + "-------------------------------------";

            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Comprobante Rent-a-Car El Mozote", receiptText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "¡Comprobante copiado al portapapeles!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
