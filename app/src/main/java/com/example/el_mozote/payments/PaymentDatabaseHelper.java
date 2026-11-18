package com.example.el_mozote.payments;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.el_mozote.reservations.Reservation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "el_mozote_payments.db";
    private static final int DATABASE_VERSION = 1;

    // Table Accounts
    public static final String TABLE_ACCOUNTS = "payment_accounts";
    public static final String COL_ACC_ID = "id";
    public static final String COL_ACC_RES_ID = "reservation_id";
    public static final String COL_ACC_CONTRACT = "contract_number";
    public static final String COL_ACC_CUST_NAME = "customer_name";
    public static final String COL_ACC_CUST_DUI = "customer_dui";
    public static final String COL_ACC_CUST_PHONE = "customer_phone";
    public static final String COL_ACC_VEH_TITLE = "vehicle_title";
    public static final String COL_ACC_VEH_PLATE = "vehicle_plate";
    public static final String COL_ACC_TOTAL_COST = "total_rent_cost";
    public static final String COL_ACC_TOTAL_PAID = "total_paid";
    public static final String COL_ACC_PENDING = "pending_balance";
    public static final String COL_ACC_STATUS = "payment_status";
    public static final String COL_ACC_CREATED_AT = "created_at";
    public static final String COL_ACC_UPDATED_AT = "updated_at";

    // Table Transactions
    public static final String TABLE_TRANSACTIONS = "payment_transactions";
    public static final String COL_TRX_ID = "id";
    public static final String COL_TRX_PAY_ID = "payment_id";
    public static final String COL_TRX_RES_ID = "reservation_id";
    public static final String COL_TRX_RECEIPT = "receipt_number";
    public static final String COL_TRX_AMOUNT = "amount";
    public static final String COL_TRX_METHOD = "payment_method";
    public static final String COL_TRX_TYPE = "payment_type";
    public static final String COL_TRX_PREV_BAL = "previous_balance";
    public static final String COL_TRX_NEW_BAL = "new_balance";
    public static final String COL_TRX_NOTES = "notes";
    public static final String COL_TRX_DATE = "transaction_date";
    public static final String COL_TRX_TIMESTAMP = "timestamp";
    public static final String COL_TRX_CUST_NAME = "customer_name";
    public static final String COL_TRX_CONTRACT = "contract_number";
    public static final String COL_TRX_VEH_TITLE = "vehicle_title";

    private static PaymentDatabaseHelper instance;

    public static synchronized PaymentDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PaymentDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PaymentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createAccounts = "CREATE TABLE " + TABLE_ACCOUNTS + " ("
                + COL_ACC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACC_RES_ID + " INTEGER NOT NULL UNIQUE, "
                + COL_ACC_CONTRACT + " TEXT NOT NULL UNIQUE, "
                + COL_ACC_CUST_NAME + " TEXT NOT NULL, "
                + COL_ACC_CUST_DUI + " TEXT, "
                + COL_ACC_CUST_PHONE + " TEXT, "
                + COL_ACC_VEH_TITLE + " TEXT NOT NULL, "
                + COL_ACC_VEH_PLATE + " TEXT NOT NULL, "
                + COL_ACC_TOTAL_COST + " REAL NOT NULL, "
                + COL_ACC_TOTAL_PAID + " REAL NOT NULL DEFAULT 0.0, "
                + COL_ACC_PENDING + " REAL NOT NULL, "
                + COL_ACC_STATUS + " TEXT NOT NULL, "
                + COL_ACC_CREATED_AT + " INTEGER, "
                + COL_ACC_UPDATED_AT + " INTEGER"
                + ")";
        db.execSQL(createAccounts);

        String createTrx = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                + COL_TRX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TRX_PAY_ID + " INTEGER NOT NULL, "
                + COL_TRX_RES_ID + " INTEGER NOT NULL, "
                + COL_TRX_RECEIPT + " TEXT NOT NULL UNIQUE, "
                + COL_TRX_AMOUNT + " REAL NOT NULL, "
                + COL_TRX_METHOD + " TEXT NOT NULL, "
                + COL_TRX_TYPE + " TEXT NOT NULL, "
                + COL_TRX_PREV_BAL + " REAL NOT NULL, "
                + COL_TRX_NEW_BAL + " REAL NOT NULL, "
                + COL_TRX_NOTES + " TEXT, "
                + COL_TRX_DATE + " TEXT NOT NULL, "
                + COL_TRX_TIMESTAMP + " INTEGER NOT NULL, "
                + COL_TRX_CUST_NAME + " TEXT, "
                + COL_TRX_CONTRACT + " TEXT, "
                + COL_TRX_VEH_TITLE + " TEXT"
                + ")";
        db.execSQL(createTrx);

        insertSampleFinancials(db);
    }

    private void insertSampleFinancials(SQLiteDatabase db) {
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        // 1. Contrato CTR-0001 (Renta Activa Carlos Martínez - Total $120.00, Abono de $50.00 -> Saldo $70.00)
        ContentValues a1 = new ContentValues();
        a1.put(COL_ACC_RES_ID, 1);
        a1.put(COL_ACC_CONTRACT, "CTR-0001");
        a1.put(COL_ACC_CUST_NAME, "Carlos Alberto Martínez");
        a1.put(COL_ACC_CUST_DUI, "04829103-5");
        a1.put(COL_ACC_CUST_PHONE, "+503 7890-1234");
        a1.put(COL_ACC_VEH_TITLE, "Hyundai Elantra");
        a1.put(COL_ACC_VEH_PLATE, "P 451-230");
        a1.put(COL_ACC_TOTAL_COST, 120.0);
        a1.put(COL_ACC_TOTAL_PAID, 50.0);
        a1.put(COL_ACC_PENDING, 70.0);
        a1.put(COL_ACC_STATUS, PaymentAccount.STATUS_PARTIAL);
        a1.put(COL_ACC_CREATED_AT, now - 86400000L);
        a1.put(COL_ACC_UPDATED_AT, now);
        long acc1Id = db.insert(TABLE_ACCOUNTS, null, a1);

        if (acc1Id != -1) {
            ContentValues t1 = new ContentValues();
            t1.put(COL_TRX_PAY_ID, acc1Id);
            t1.put(COL_TRX_RES_ID, 1);
            t1.put(COL_TRX_RECEIPT, "REC-2026-0001");
            t1.put(COL_TRX_AMOUNT, 50.0);
            t1.put(COL_TRX_METHOD, PaymentTransaction.METHOD_CASH);
            t1.put(COL_TRX_TYPE, PaymentTransaction.TYPE_PARTIAL);
            t1.put(COL_TRX_PREV_BAL, 120.0);
            t1.put(COL_TRX_NEW_BAL, 70.0);
            t1.put(COL_TRX_NOTES, "Abono inicial al momento de retiro.");
            t1.put(COL_TRX_DATE, sdf.format(new Date(now - 86400000L)));
            t1.put(COL_TRX_TIMESTAMP, now - 86400000L);
            t1.put(COL_TRX_CUST_NAME, "Carlos Alberto Martínez");
            t1.put(COL_TRX_CONTRACT, "CTR-0001");
            t1.put(COL_TRX_VEH_TITLE, "Hyundai Elantra");
            db.insert(TABLE_TRANSACTIONS, null, t1);
        }

        // 2. Contrato CTR-0002 (Reserva Pendiente Mariana Reyes - Total $130.00, Pendiente completo $130.00)
        ContentValues a2 = new ContentValues();
        a2.put(COL_ACC_RES_ID, 2);
        a2.put(COL_ACC_CONTRACT, "CTR-0002");
        a2.put(COL_ACC_CUST_NAME, "Mariana Elizabeth Reyes");
        a2.put(COL_ACC_CUST_DUI, "03918274-1");
        a2.put(COL_ACC_CUST_PHONE, "+503 7123-4567");
        a2.put(COL_ACC_VEH_TITLE, "Toyota Hilux 4x4");
        a2.put(COL_ACC_VEH_PLATE, "P 892-104");
        a2.put(COL_ACC_TOTAL_COST, 130.0);
        a2.put(COL_ACC_TOTAL_PAID, 0.0);
        a2.put(COL_ACC_PENDING, 130.0);
        a2.put(COL_ACC_STATUS, PaymentAccount.STATUS_PENDING);
        a2.put(COL_ACC_CREATED_AT, now);
        a2.put(COL_ACC_UPDATED_AT, now);
        db.insert(TABLE_ACCOUNTS, null, a2);

        // 3. Contrato CTR-0003 (Renta Finalizada Fernando Gómez - Total $130.00, Pagado Completo $130.00)
        ContentValues a3 = new ContentValues();
        a3.put(COL_ACC_RES_ID, 3);
        a3.put(COL_ACC_CONTRACT, "CTR-0003");
        a3.put(COL_ACC_CUST_NAME, "Fernando José Gómez");
        a3.put(COL_ACC_CUST_DUI, "02394851-9");
        a3.put(COL_ACC_CUST_PHONE, "+503 7654-3210");
        a3.put(COL_ACC_VEH_TITLE, "Toyota Hilux 4x4");
        a3.put(COL_ACC_VEH_PLATE, "P 892-104");
        a3.put(COL_ACC_TOTAL_COST, 130.0);
        a3.put(COL_ACC_TOTAL_PAID, 130.0);
        a3.put(COL_ACC_PENDING, 0.0);
        a3.put(COL_ACC_STATUS, PaymentAccount.STATUS_PAID);
        a3.put(COL_ACC_CREATED_AT, now - 86400000L * 3);
        a3.put(COL_ACC_UPDATED_AT, now - 86400000L);
        long acc3Id = db.insert(TABLE_ACCOUNTS, null, a3);

        if (acc3Id != -1) {
            ContentValues t2 = new ContentValues();
            t2.put(COL_TRX_PAY_ID, acc3Id);
            t2.put(COL_TRX_RES_ID, 3);
            t2.put(COL_TRX_RECEIPT, "REC-2026-0002");
            t2.put(COL_TRX_AMOUNT, 130.0);
            t2.put(COL_TRX_METHOD, PaymentTransaction.METHOD_CARD);
            t2.put(COL_TRX_TYPE, PaymentTransaction.TYPE_FULL);
            t2.put(COL_TRX_PREV_BAL, 130.0);
            t2.put(COL_TRX_NEW_BAL, 0.0);
            t2.put(COL_TRX_NOTES, "Pago total con tarjeta Visa.");
            t2.put(COL_TRX_DATE, sdf.format(new Date(now - 86400000L)));
            t2.put(COL_TRX_TIMESTAMP, now - 86400000L);
            t2.put(COL_TRX_CUST_NAME, "Fernando José Gómez");
            t2.put(COL_TRX_CONTRACT, "CTR-0003");
            t2.put(COL_TRX_VEH_TITLE, "Toyota Hilux 4x4");
            db.insert(TABLE_TRANSACTIONS, null, t2);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        onCreate(db);
    }

    // =========================================================================
    // SYNC CON RESERVAS EXISTENTES
    // =========================================================================
    public synchronized PaymentAccount syncFromReservation(Reservation res) {
        if (res == null) return null;
        PaymentAccount account = getAccountByReservationId(res.getId());
        if (account == null) {
            String contractNum = "CTR-" + String.format(Locale.US, "%04d", res.getId());
            account = new PaymentAccount(0, res.getId(), contractNum, res.getCustomerName(),
                    res.getCustomerDui(), res.getCustomerPhone(), res.getVehicleTitle(),
                    res.getVehiclePlate(), res.getTotalCost(), 0.0, res.getTotalCost(),
                    PaymentAccount.STATUS_PENDING, System.currentTimeMillis(), System.currentTimeMillis());
            long newId = insertPaymentAccount(account);
            account.setId(newId);
        }
        return account;
    }

    public synchronized void syncAllReservations(List<Reservation> reservations) {
        if (reservations == null) return;
        for (Reservation r : reservations) {
            syncFromReservation(r);
        }
    }

    public long insertPaymentAccount(PaymentAccount account) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ACC_RES_ID, account.getReservationId());
        cv.put(COL_ACC_CONTRACT, account.getContractNumber());
        cv.put(COL_ACC_CUST_NAME, account.getCustomerName());
        cv.put(COL_ACC_CUST_DUI, account.getCustomerDui());
        cv.put(COL_ACC_CUST_PHONE, account.getCustomerPhone());
        cv.put(COL_ACC_VEH_TITLE, account.getVehicleTitle());
        cv.put(COL_ACC_VEH_PLATE, account.getVehiclePlate());
        cv.put(COL_ACC_TOTAL_COST, account.getTotalRentCost());
        cv.put(COL_ACC_TOTAL_PAID, account.getTotalPaid());
        cv.put(COL_ACC_PENDING, account.getPendingBalance());
        cv.put(COL_ACC_STATUS, account.getPaymentStatus());
        cv.put(COL_ACC_CREATED_AT, account.getCreatedAt());
        cv.put(COL_ACC_UPDATED_AT, account.getUpdatedAt());

        long id = db.insert(TABLE_ACCOUNTS, null, cv);
        if (id != -1) account.setId(id);
        return id;
    }

    public int updatePaymentAccount(PaymentAccount account) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ACC_TOTAL_PAID, account.getTotalPaid());
        cv.put(COL_ACC_PENDING, account.getPendingBalance());
        cv.put(COL_ACC_STATUS, account.getPaymentStatus());
        cv.put(COL_ACC_UPDATED_AT, System.currentTimeMillis());

        return db.update(TABLE_ACCOUNTS, cv, COL_ACC_ID + " = ?",
                new String[]{String.valueOf(account.getId())});
    }

    public PaymentAccount getAccountById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ACCOUNTS, null, COL_ACC_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        PaymentAccount account = null;
        if (c != null && c.moveToFirst()) {
            account = cursorToAccount(c);
            c.close();
        }
        return account;
    }

    public PaymentAccount getAccountByReservationId(long resId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ACCOUNTS, null, COL_ACC_RES_ID + " = ?",
                new String[]{String.valueOf(resId)}, null, null, null);
        PaymentAccount account = null;
        if (c != null && c.moveToFirst()) {
            account = cursorToAccount(c);
            c.close();
        }
        return account;
    }

    public List<PaymentAccount> searchPaymentAccounts(String query, String statusFilter) {
        List<PaymentAccount> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder sel = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (statusFilter != null && !statusFilter.isEmpty() && !"Todos".equalsIgnoreCase(statusFilter)) {
            sel.append(COL_ACC_STATUS).append(" = ?");
            args.add(statusFilter);
        }

        if (query != null && !query.trim().isEmpty()) {
            if (sel.length() > 0) sel.append(" AND ");
            sel.append("(")
                    .append(COL_ACC_CUST_NAME).append(" LIKE ? OR ")
                    .append(COL_ACC_CONTRACT).append(" LIKE ? OR ")
                    .append(COL_ACC_VEH_PLATE).append(" LIKE ? OR ")
                    .append(COL_ACC_CUST_DUI).append(" LIKE ?")
                    .append(")");
            String p = "%" + query.trim() + "%";
            args.add(p);
            args.add(p);
            args.add(p);
            args.add(p);
        }

        String where = sel.length() > 0 ? sel.toString() : null;
        String[] selArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        Cursor c = db.query(TABLE_ACCOUNTS, null, where, selArgs, null, null, COL_ACC_ID + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToAccount(c));
            }
            c.close();
        }
        return list;
    }

    // =========================================================================
    // REGISTRO DE TRANSACCIÓN ATÓMICA (PAGO / ABONO)
    // =========================================================================
    public synchronized boolean recordTransaction(PaymentTransaction trx) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            PaymentAccount account = getAccountById(trx.getPaymentId());
            if (account == null) {
                db.endTransaction();
                return false;
            }

            // Validaciones obligatorias de negocio
            if (trx.getAmount() <= 0) {
                db.endTransaction();
                return false;
            }
            if (trx.getAmount() > account.getPendingBalance() + 0.009) {
                db.endTransaction();
                return false;
            }

            double prevBal = account.getPendingBalance();
            double newBal = Math.max(0.0, prevBal - trx.getAmount());
            double newPaid = account.getTotalPaid() + trx.getAmount();
            String newStatus = (newBal <= 0.009) ? PaymentAccount.STATUS_PAID : PaymentAccount.STATUS_PARTIAL;

            trx.setPreviousBalance(prevBal);
            trx.setNewBalance(newBal);
            trx.setCustomerName(account.getCustomerName());
            trx.setContractNumber(account.getContractNumber());
            trx.setVehicleTitle(account.getVehicleTitle());

            // 1. Insert Transaction
            ContentValues cvTrx = new ContentValues();
            cvTrx.put(COL_TRX_PAY_ID, trx.getPaymentId());
            cvTrx.put(COL_TRX_RES_ID, trx.getReservationId());
            cvTrx.put(COL_TRX_RECEIPT, trx.getReceiptNumber());
            cvTrx.put(COL_TRX_AMOUNT, trx.getAmount());
            cvTrx.put(COL_TRX_METHOD, trx.getPaymentMethod());
            cvTrx.put(COL_TRX_TYPE, trx.getPaymentType());
            cvTrx.put(COL_TRX_PREV_BAL, trx.getPreviousBalance());
            cvTrx.put(COL_TRX_NEW_BAL, trx.getNewBalance());
            cvTrx.put(COL_TRX_NOTES, trx.getNotes());
            cvTrx.put(COL_TRX_DATE, trx.getTransactionDate());
            cvTrx.put(COL_TRX_TIMESTAMP, trx.getTimestamp());
            cvTrx.put(COL_TRX_CUST_NAME, trx.getCustomerName());
            cvTrx.put(COL_TRX_CONTRACT, trx.getContractNumber());
            cvTrx.put(COL_TRX_VEH_TITLE, trx.getVehicleTitle());

            long trxId = db.insert(TABLE_TRANSACTIONS, null, cvTrx);
            if (trxId == -1) {
                db.endTransaction();
                return false;
            }
            trx.setId(trxId);

            // 2. Update Account balances
            ContentValues cvAcc = new ContentValues();
            cvAcc.put(COL_ACC_TOTAL_PAID, newPaid);
            cvAcc.put(COL_ACC_PENDING, newBal);
            cvAcc.put(COL_ACC_STATUS, newStatus);
            cvAcc.put(COL_ACC_UPDATED_AT, System.currentTimeMillis());

            int updated = db.update(TABLE_ACCOUNTS, cvAcc, COL_ACC_ID + " = ?",
                    new String[]{String.valueOf(account.getId())});

            if (updated <= 0) {
                db.endTransaction();
                return false;
            }

            account.setTotalPaid(newPaid);
            account.setPendingBalance(newBal);
            account.setPaymentStatus(newStatus);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<PaymentTransaction> getTransactionsByPaymentId(long paymentId) {
        List<PaymentTransaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null, COL_TRX_PAY_ID + " = ?",
                new String[]{String.valueOf(paymentId)}, null, null, COL_TRX_ID + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToTransaction(c));
            }
            c.close();
        }
        return list;
    }

    public List<PaymentTransaction> getAllTransactions() {
        List<PaymentTransaction> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null, null, null, null, null, COL_TRX_ID + " DESC");
        if (c != null) {
            while (c.moveToNext()) {
                list.add(cursorToTransaction(c));
            }
            c.close();
        }
        return list;
    }

    public PaymentTransaction getTransactionById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTIONS, null, COL_TRX_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        PaymentTransaction t = null;
        if (c != null && c.moveToFirst()) {
            t = cursorToTransaction(c);
            c.close();
        }
        return t;
    }

    public double getTotalBilled() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_ACC_TOTAL_COST + ") FROM " + TABLE_ACCOUNTS, null);
        double total = 0.0;
        if (c != null && c.moveToFirst()) {
            total = c.getDouble(0);
            c.close();
        }
        return total;
    }

    public double getTotalCollected() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_ACC_TOTAL_PAID + ") FROM " + TABLE_ACCOUNTS, null);
        double total = 0.0;
        if (c != null && c.moveToFirst()) {
            total = c.getDouble(0);
            c.close();
        }
        return total;
    }

    public double getTotalPending() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_ACC_PENDING + ") FROM " + TABLE_ACCOUNTS, null);
        double total = 0.0;
        if (c != null && c.moveToFirst()) {
            total = c.getDouble(0);
            c.close();
        }
        return total;
    }

    public int getCountPendingAccounts() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ACCOUNTS + " WHERE " + COL_ACC_PENDING + " > 0.009", null);
        int count = 0;
        if (c != null && c.moveToFirst()) {
            count = c.getInt(0);
            c.close();
        }
        return count;
    }

    public String generateNextReceiptNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
        String prefix = "REC-" + sdf.format(new Date()) + "-";
        long count = System.currentTimeMillis() % 10000;
        return prefix + String.format(Locale.US, "%04d", count);
    }

    // =========================================================================
    // MAPPERS
    // =========================================================================
    private PaymentAccount cursorToAccount(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(COL_ACC_ID));
        long resId = c.getLong(c.getColumnIndexOrThrow(COL_ACC_RES_ID));
        String contract = c.getString(c.getColumnIndexOrThrow(COL_ACC_CONTRACT));
        String custName = c.getString(c.getColumnIndexOrThrow(COL_ACC_CUST_NAME));
        String custDui = c.getString(c.getColumnIndexOrThrow(COL_ACC_CUST_DUI));
        String custPhone = c.getString(c.getColumnIndexOrThrow(COL_ACC_CUST_PHONE));
        String vehTitle = c.getString(c.getColumnIndexOrThrow(COL_ACC_VEH_TITLE));
        String vehPlate = c.getString(c.getColumnIndexOrThrow(COL_ACC_VEH_PLATE));
        double cost = c.getDouble(c.getColumnIndexOrThrow(COL_ACC_TOTAL_COST));
        double paid = c.getDouble(c.getColumnIndexOrThrow(COL_ACC_TOTAL_PAID));
        double pending = c.getDouble(c.getColumnIndexOrThrow(COL_ACC_PENDING));
        String status = c.getString(c.getColumnIndexOrThrow(COL_ACC_STATUS));
        long createdAt = c.getLong(c.getColumnIndexOrThrow(COL_ACC_CREATED_AT));
        long updatedAt = c.getLong(c.getColumnIndexOrThrow(COL_ACC_UPDATED_AT));

        return new PaymentAccount(id, resId, contract, custName, custDui, custPhone,
                vehTitle, vehPlate, cost, paid, pending, status, createdAt, updatedAt);
    }

    private PaymentTransaction cursorToTransaction(Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(COL_TRX_ID));
        long payId = c.getLong(c.getColumnIndexOrThrow(COL_TRX_PAY_ID));
        long resId = c.getLong(c.getColumnIndexOrThrow(COL_TRX_RES_ID));
        String receipt = c.getString(c.getColumnIndexOrThrow(COL_TRX_RECEIPT));
        double amount = c.getDouble(c.getColumnIndexOrThrow(COL_TRX_AMOUNT));
        String method = c.getString(c.getColumnIndexOrThrow(COL_TRX_METHOD));
        String type = c.getString(c.getColumnIndexOrThrow(COL_TRX_TYPE));
        double prev = c.getDouble(c.getColumnIndexOrThrow(COL_TRX_PREV_BAL));
        double next = c.getDouble(c.getColumnIndexOrThrow(COL_TRX_NEW_BAL));
        String notes = c.getString(c.getColumnIndexOrThrow(COL_TRX_NOTES));
        String date = c.getString(c.getColumnIndexOrThrow(COL_TRX_DATE));
        long timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TRX_TIMESTAMP));
        String cust = c.getString(c.getColumnIndexOrThrow(COL_TRX_CUST_NAME));
        String contract = c.getString(c.getColumnIndexOrThrow(COL_TRX_CONTRACT));
        String veh = c.getString(c.getColumnIndexOrThrow(COL_TRX_VEH_TITLE));

        return new PaymentTransaction(id, payId, resId, receipt, amount, method, type,
                prev, next, notes, date, timestamp, cust, contract, veh);
    }
}
