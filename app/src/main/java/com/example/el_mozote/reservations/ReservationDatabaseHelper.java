package com.example.el_mozote.reservations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "el_mozote_reservations.db";
    private static final int DATABASE_VERSION = 1;

    // Table Reservations
    public static final String TABLE_RESERVATIONS = "reservations";
    public static final String COL_RES_ID = "id";
    public static final String COL_RES_VEHICLE_ID = "vehicle_id";
    public static final String COL_RES_VEHICLE_TITLE = "vehicle_title";
    public static final String COL_RES_VEHICLE_PLATE = "vehicle_plate";
    public static final String COL_RES_VEHICLE_PRICE = "vehicle_price";
    public static final String COL_RES_CUST_NAME = "customer_name";
    public static final String COL_RES_CUST_DUI = "customer_dui";
    public static final String COL_RES_CUST_LICENSE = "customer_license";
    public static final String COL_RES_CUST_PHONE = "customer_phone";
    public static final String COL_RES_CUST_ADDRESS = "customer_address";
    public static final String COL_RES_START_DATE = "start_date";
    public static final String COL_RES_END_DATE = "end_date";
    public static final String COL_RES_DAILY_RATE = "daily_rate";
    public static final String COL_RES_TOTAL_DAYS = "total_days";
    public static final String COL_RES_TOTAL_COST = "total_cost";
    public static final String COL_RES_OBSERVATIONS = "observations";
    public static final String COL_RES_STATUS = "status";
    public static final String COL_RES_CREATED_AT = "created_at";

    // Table Inspections
    public static final String TABLE_INSPECTIONS = "inspections";
    public static final String COL_INSP_ID = "id";
    public static final String COL_INSP_RES_ID = "reservation_id";
    public static final String COL_INSP_TYPE = "type";
    public static final String COL_INSP_MILEAGE = "mileage";
    public static final String COL_INSP_FUEL = "fuel_level";
    public static final String COL_INSP_EXTERIOR = "exterior_state";
    public static final String COL_INSP_INTERIOR = "interior_state";
    public static final String COL_INSP_SPARE_TIRE = "spare_tire";
    public static final String COL_INSP_TOOLS = "tools";
    public static final String COL_INSP_DAMAGES = "damages";
    public static final String COL_INSP_OBS = "observations";
    public static final String COL_INSP_PHOTOS = "photo_paths";
    public static final String COL_INSP_CREATED_AT = "created_at";

    private static ReservationDatabaseHelper instance;

    public static synchronized ReservationDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ReservationDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ReservationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createResTable = "CREATE TABLE " + TABLE_RESERVATIONS + " ("
                + COL_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RES_VEHICLE_ID + " INTEGER NOT NULL, "
                + COL_RES_VEHICLE_TITLE + " TEXT, "
                + COL_RES_VEHICLE_PLATE + " TEXT, "
                + COL_RES_VEHICLE_PRICE + " REAL, "
                + COL_RES_CUST_NAME + " TEXT NOT NULL, "
                + COL_RES_CUST_DUI + " TEXT NOT NULL, "
                + COL_RES_CUST_LICENSE + " TEXT NOT NULL, "
                + COL_RES_CUST_PHONE + " TEXT NOT NULL, "
                + COL_RES_CUST_ADDRESS + " TEXT NOT NULL, "
                + COL_RES_START_DATE + " TEXT NOT NULL, "
                + COL_RES_END_DATE + " TEXT NOT NULL, "
                + COL_RES_DAILY_RATE + " REAL NOT NULL, "
                + COL_RES_TOTAL_DAYS + " INTEGER NOT NULL, "
                + COL_RES_TOTAL_COST + " REAL NOT NULL, "
                + COL_RES_OBSERVATIONS + " TEXT, "
                + COL_RES_STATUS + " TEXT NOT NULL, "
                + COL_RES_CREATED_AT + " INTEGER"
                + ")";
        db.execSQL(createResTable);

        String createInspTable = "CREATE TABLE " + TABLE_INSPECTIONS + " ("
                + COL_INSP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_INSP_RES_ID + " INTEGER NOT NULL, "
                + COL_INSP_TYPE + " TEXT NOT NULL, "
                + COL_INSP_MILEAGE + " INTEGER NOT NULL, "
                + COL_INSP_FUEL + " TEXT NOT NULL, "
                + COL_INSP_EXTERIOR + " TEXT NOT NULL, "
                + COL_INSP_INTERIOR + " TEXT NOT NULL, "
                + COL_INSP_SPARE_TIRE + " INTEGER NOT NULL, "
                + COL_INSP_TOOLS + " INTEGER NOT NULL, "
                + COL_INSP_DAMAGES + " TEXT, "
                + COL_INSP_OBS + " TEXT, "
                + COL_INSP_PHOTOS + " TEXT, "
                + COL_INSP_CREATED_AT + " INTEGER"
                + ")";
        db.execSQL(createInspTable);

        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long now = System.currentTimeMillis();
        long dayMillis = 86400000L;

        String today = sdf.format(new Date(now));
        String in3Days = sdf.format(new Date(now + dayMillis * 3));
        String in5Days = sdf.format(new Date(now + dayMillis * 5));
        String past3Days = sdf.format(new Date(now - dayMillis * 3));
        String yesterday = sdf.format(new Date(now - dayMillis));

        // 1. Renta Activa (Hyundai Elantra P 451-230)
        ContentValues cv1 = new ContentValues();
        cv1.put(COL_RES_VEHICLE_ID, 2);
        cv1.put(COL_RES_VEHICLE_TITLE, "Hyundai Elantra");
        cv1.put(COL_RES_VEHICLE_PLATE, "P 451-230");
        cv1.put(COL_RES_VEHICLE_PRICE, 40.0);
        cv1.put(COL_RES_CUST_NAME, "Carlos Alberto Martínez");
        cv1.put(COL_RES_CUST_DUI, "04829103-5");
        cv1.put(COL_RES_CUST_LICENSE, "0614-230990-102-1");
        cv1.put(COL_RES_CUST_PHONE, "+503 7890-1234");
        cv1.put(COL_RES_CUST_ADDRESS, "Col. San Benito, Pasaje 3, Casa #14, San Salvador");
        cv1.put(COL_RES_START_DATE, today);
        cv1.put(COL_RES_END_DATE, in3Days);
        cv1.put(COL_RES_DAILY_RATE, 40.0);
        cv1.put(COL_RES_TOTAL_DAYS, 3);
        cv1.put(COL_RES_TOTAL_COST, 120.0);
        cv1.put(COL_RES_OBSERVATIONS, "Viaje de negocios a San Miguel.");
        cv1.put(COL_RES_STATUS, Reservation.STATUS_ACTIVE);
        cv1.put(COL_RES_CREATED_AT, now);
        long r1Id = db.insert(TABLE_RESERVATIONS, null, cv1);

        // Inspección de salida para la renta activa
        if (r1Id != -1) {
            ContentValues cvInsp = new ContentValues();
            cvInsp.put(COL_INSP_RES_ID, r1Id);
            cvInsp.put(COL_INSP_TYPE, Inspection.TYPE_DEPARTURE);
            cvInsp.put(COL_INSP_MILEAGE, 45200);
            cvInsp.put(COL_INSP_FUEL, "Lleno (4/4)");
            cvInsp.put(COL_INSP_EXTERIOR, "Buen estado, sin rayones");
            cvInsp.put(COL_INSP_INTERIOR, "Limpio, alfombras completas");
            cvInsp.put(COL_INSP_SPARE_TIRE, 1);
            cvInsp.put(COL_INSP_TOOLS, 1);
            cvInsp.put(COL_INSP_DAMAGES, "Ninguno");
            cvInsp.put(COL_INSP_OBS, "Vehículo entregado con tanque lleno y documentos al día.");
            cvInsp.put(COL_INSP_PHOTOS, "[]");
            cvInsp.put(COL_INSP_CREATED_AT, now);
            db.insert(TABLE_INSPECTIONS, null, cvInsp);
        }

        // 2. Reserva Pendiente (Toyota Hilux 4x4)
        ContentValues cv2 = new ContentValues();
        cv2.put(COL_RES_VEHICLE_ID, 1);
        cv2.put(COL_RES_VEHICLE_TITLE, "Toyota Hilux 4x4");
        cv2.put(COL_RES_VEHICLE_PLATE, "P 892-104");
        cv2.put(COL_RES_VEHICLE_PRICE, 65.0);
        cv2.put(COL_RES_CUST_NAME, "Mariana Elizabeth Reyes");
        cv2.put(COL_RES_CUST_DUI, "03918274-1");
        cv2.put(COL_RES_CUST_LICENSE, "0614-150294-101-9");
        cv2.put(COL_RES_CUST_PHONE, "+503 7123-4567");
        cv2.put(COL_RES_CUST_ADDRESS, "Residencial Santa Teresa, Senda Los Pinos #8, Santa Tecla");
        cv2.put(COL_RES_START_DATE, in3Days);
        cv2.put(COL_RES_END_DATE, in5Days);
        cv2.put(COL_RES_DAILY_RATE, 65.0);
        cv2.put(COL_RES_TOTAL_DAYS, 2);
        cv2.put(COL_RES_TOTAL_COST, 130.0);
        cv2.put(COL_RES_OBSERVATIONS, "Requiere barras portaequipaje.");
        cv2.put(COL_RES_STATUS, Reservation.STATUS_RESERVED);
        cv2.put(COL_RES_CREATED_AT, now);
        db.insert(TABLE_RESERVATIONS, null, cv2);

        // 3. Renta en Historial (Finalizada)
        ContentValues cv3 = new ContentValues();
        cv3.put(COL_RES_VEHICLE_ID, 1);
        cv3.put(COL_RES_VEHICLE_TITLE, "Toyota Hilux 4x4");
        cv3.put(COL_RES_VEHICLE_PLATE, "P 892-104");
        cv3.put(COL_RES_VEHICLE_PRICE, 65.0);
        cv3.put(COL_RES_CUST_NAME, "Fernando José Gómez");
        cv3.put(COL_RES_CUST_DUI, "02394851-9");
        cv3.put(COL_RES_CUST_LICENSE, "0614-010188-103-2");
        cv3.put(COL_RES_CUST_PHONE, "+503 7654-3210");
        cv3.put(COL_RES_CUST_ADDRESS, "Barrio El Centro, 2a Calle Poniente #12, Morazán");
        cv3.put(COL_RES_START_DATE, past3Days);
        cv3.put(COL_RES_END_DATE, yesterday);
        cv3.put(COL_RES_DAILY_RATE, 65.0);
        cv3.put(COL_RES_TOTAL_DAYS, 2);
        cv3.put(COL_RES_TOTAL_COST, 130.0);
        cv3.put(COL_RES_OBSERVATIONS, "Renta de fin de semana para ruta turística.");
        cv3.put(COL_RES_STATUS, Reservation.STATUS_COMPLETED);
        cv3.put(COL_RES_CREATED_AT, now - dayMillis * 4);
        long r3Id = db.insert(TABLE_RESERVATIONS, null, cv3);

        if (r3Id != -1) {
            // Inspección salida
            ContentValues iSal = new ContentValues();
            iSal.put(COL_INSP_RES_ID, r3Id);
            iSal.put(COL_INSP_TYPE, Inspection.TYPE_DEPARTURE);
            iSal.put(COL_INSP_MILEAGE, 18200);
            iSal.put(COL_INSP_FUEL, "Lleno (4/4)");
            iSal.put(COL_INSP_EXTERIOR, "Excelente");
            iSal.put(COL_INSP_INTERIOR, "Excelente");
            iSal.put(COL_INSP_SPARE_TIRE, 1);
            iSal.put(COL_INSP_TOOLS, 1);
            iSal.put(COL_INSP_DAMAGES, "Sin daños");
            iSal.put(COL_INSP_OBS, "Entrega impecable.");
            iSal.put(COL_INSP_PHOTOS, "[]");
            iSal.put(COL_INSP_CREATED_AT, now - dayMillis * 3);
            db.insert(TABLE_INSPECTIONS, null, iSal);

            // Inspección regreso
            ContentValues iReg = new ContentValues();
            iReg.put(COL_INSP_RES_ID, r3Id);
            iReg.put(COL_INSP_TYPE, Inspection.TYPE_RETURN);
            iReg.put(COL_INSP_MILEAGE, 18550);
            iReg.put(COL_INSP_FUEL, "Lleno (4/4)");
            iReg.put(COL_INSP_EXTERIOR, "Buen estado");
            iReg.put(COL_INSP_INTERIOR, "Limpio");
            iReg.put(COL_INSP_SPARE_TIRE, 1);
            iReg.put(COL_INSP_TOOLS, 1);
            iReg.put(COL_INSP_DAMAGES, "Ninguno");
            iReg.put(COL_INSP_OBS, "Vehículo devuelto en perfecto estado, kilometraje recorrido: 350 km.");
            iReg.put(COL_INSP_PHOTOS, "[]");
            iReg.put(COL_INSP_CREATED_AT, now - dayMillis);
            db.insert(TABLE_INSPECTIONS, null, iReg);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INSPECTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        onCreate(db);
    }

    // =========================================================================
    // RESERVATIONS CRUD
    // =========================================================================
    public long insertReservation(Reservation reservation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = reservationToContentValues(reservation);
        long id = db.insert(TABLE_RESERVATIONS, null, cv);
        if (id != -1) {
            reservation.setId(id);
        }
        return id;
    }

    public int updateReservation(Reservation reservation) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = reservationToContentValues(reservation);
        return db.update(TABLE_RESERVATIONS, cv, COL_RES_ID + " = ?",
                new String[]{String.valueOf(reservation.getId())});
    }

    public int updateReservationStatus(long reservationId, String newStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RES_STATUS, newStatus);
        return db.update(TABLE_RESERVATIONS, cv, COL_RES_ID + " = ?",
                new String[]{String.valueOf(reservationId)});
    }

    public int deleteReservation(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_INSPECTIONS, COL_INSP_RES_ID + " = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_RESERVATIONS, COL_RES_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Reservation getReservationById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RESERVATIONS, null, COL_RES_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Reservation res = null;
        if (cursor != null && cursor.moveToFirst()) {
            res = cursorToReservation(cursor);
            cursor.close();
        }
        return res;
    }

    public List<Reservation> searchReservations(String query, String statusFilter) {
        List<Reservation> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (statusFilter != null && !statusFilter.isEmpty() && !"Todas".equalsIgnoreCase(statusFilter)) {
            if ("Historial".equalsIgnoreCase(statusFilter)) {
                selection.append(COL_RES_STATUS).append(" = ?");
                selectionArgs.add(Reservation.STATUS_COMPLETED);
            } else if ("En curso".equalsIgnoreCase(statusFilter)) {
                selection.append(COL_RES_STATUS).append(" = ?");
                selectionArgs.add(Reservation.STATUS_ACTIVE);
            } else if ("Pendientes".equalsIgnoreCase(statusFilter)) {
                selection.append(COL_RES_STATUS).append(" = ?");
                selectionArgs.add(Reservation.STATUS_RESERVED);
            } else {
                selection.append(COL_RES_STATUS).append(" = ?");
                selectionArgs.add(statusFilter);
            }
        }

        if (query != null && !query.trim().isEmpty()) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append("(")
                    .append(COL_RES_CUST_NAME).append(" LIKE ? OR ")
                    .append(COL_RES_CUST_DUI).append(" LIKE ? OR ")
                    .append(COL_RES_VEHICLE_TITLE).append(" LIKE ? OR ")
                    .append(COL_RES_VEHICLE_PLATE).append(" LIKE ?")
                    .append(")");
            String pattern = "%" + query.trim() + "%";
            selectionArgs.add(pattern);
            selectionArgs.add(pattern);
            selectionArgs.add(pattern);
            selectionArgs.add(pattern);
        }

        String whereClause = selection.length() > 0 ? selection.toString() : null;
        String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);

        Cursor cursor = db.query(TABLE_RESERVATIONS, null, whereClause, args, null, null,
                COL_RES_ID + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToReservation(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public int getCountTotal() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RESERVATIONS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getCountByStatus(String status) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RESERVATIONS + " WHERE " + COL_RES_STATUS + " = ?",
                new String[]{status});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * Valida si existe una reserva activa/reservada que se superponga con las fechas para el mismo vehículo.
     * Retorna la reserva en conflicto o null si está libre.
     */
    public Reservation checkDateConflict(long vehicleId, String newStart, String newEnd, long excludeResId) {
        SQLiteDatabase db = getReadableDatabase();
        // Superposición: (start_date <= newEnd) AND (end_date >= newStart)
        String query = "SELECT * FROM " + TABLE_RESERVATIONS
                + " WHERE " + COL_RES_VEHICLE_ID + " = ?"
                + " AND " + COL_RES_STATUS + " IN (?, ?)"
                + " AND " + COL_RES_ID + " != ?"
                + " AND (" + COL_RES_START_DATE + " <= ? AND " + COL_RES_END_DATE + " >= ?)";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(vehicleId),
                Reservation.STATUS_RESERVED,
                Reservation.STATUS_ACTIVE,
                String.valueOf(excludeResId),
                newEnd,
                newStart
        });

        Reservation conflict = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                conflict = cursorToReservation(cursor);
            }
            cursor.close();
        }
        return conflict;
    }

    // =========================================================================
    // INSPECTIONS CRUD
    // =========================================================================
    public long insertInspection(Inspection inspection) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_INSP_RES_ID, inspection.getReservationId());
        cv.put(COL_INSP_TYPE, inspection.getType());
        cv.put(COL_INSP_MILEAGE, inspection.getMileage());
        cv.put(COL_INSP_FUEL, inspection.getFuelLevel());
        cv.put(COL_INSP_EXTERIOR, inspection.getExteriorState());
        cv.put(COL_INSP_INTERIOR, inspection.getInteriorState());
        cv.put(COL_INSP_SPARE_TIRE, inspection.isSpareTire() ? 1 : 0);
        cv.put(COL_INSP_TOOLS, inspection.isTools() ? 1 : 0);
        cv.put(COL_INSP_DAMAGES, inspection.getDamages());
        cv.put(COL_INSP_OBS, inspection.getObservations());
        cv.put(COL_INSP_PHOTOS, serializePhotos(inspection.getPhotoPaths()));
        cv.put(COL_INSP_CREATED_AT, inspection.getCreatedAt() > 0 ? inspection.getCreatedAt() : System.currentTimeMillis());

        long id = db.insert(TABLE_INSPECTIONS, null, cv);
        if (id != -1) {
            inspection.setId(id);
        }
        return id;
    }

    public Inspection getInspectionByReservationAndType(long resId, String type) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_INSPECTIONS, null,
                COL_INSP_RES_ID + " = ? AND " + COL_INSP_TYPE + " = ?",
                new String[]{String.valueOf(resId), type}, null, null, null);
        Inspection insp = null;
        if (cursor != null && cursor.moveToFirst()) {
            insp = cursorToInspection(cursor);
            cursor.close();
        }
        return insp;
    }

    public boolean hasInspection(long resId, String type) {
        return getInspectionByReservationAndType(resId, type) != null;
    }

    // =========================================================================
    // MAPPERS
    // =========================================================================
    private ContentValues reservationToContentValues(Reservation res) {
        ContentValues cv = new ContentValues();
        cv.put(COL_RES_VEHICLE_ID, res.getVehicleId());
        cv.put(COL_RES_VEHICLE_TITLE, res.getVehicleTitle());
        cv.put(COL_RES_VEHICLE_PLATE, res.getVehiclePlate());
        cv.put(COL_RES_VEHICLE_PRICE, res.getVehiclePricePerDay());
        cv.put(COL_RES_CUST_NAME, res.getCustomerName());
        cv.put(COL_RES_CUST_DUI, res.getCustomerDui());
        cv.put(COL_RES_CUST_LICENSE, res.getCustomerLicense());
        cv.put(COL_RES_CUST_PHONE, res.getCustomerPhone());
        cv.put(COL_RES_CUST_ADDRESS, res.getCustomerAddress());
        cv.put(COL_RES_START_DATE, res.getStartDate());
        cv.put(COL_RES_END_DATE, res.getEndDate());
        cv.put(COL_RES_DAILY_RATE, res.getDailyRate());
        cv.put(COL_RES_TOTAL_DAYS, res.getTotalDays());
        cv.put(COL_RES_TOTAL_COST, res.getTotalCost());
        cv.put(COL_RES_OBSERVATIONS, res.getObservations());
        cv.put(COL_RES_STATUS, res.getStatus());
        cv.put(COL_RES_CREATED_AT, res.getCreatedAt() > 0 ? res.getCreatedAt() : System.currentTimeMillis());
        return cv;
    }

    private Reservation cursorToReservation(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RES_ID));
        long vId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RES_VEHICLE_ID));
        String vTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_VEHICLE_TITLE));
        String vPlate = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_VEHICLE_PLATE));
        double vPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RES_VEHICLE_PRICE));
        String cName = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_CUST_NAME));
        String cDui = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_CUST_DUI));
        String cLicense = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_CUST_LICENSE));
        String cPhone = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_CUST_PHONE));
        String cAddress = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_CUST_ADDRESS));
        String start = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_START_DATE));
        String end = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_END_DATE));
        double rate = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RES_DAILY_RATE));
        int days = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RES_TOTAL_DAYS));
        double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RES_TOTAL_COST));
        String obs = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_OBSERVATIONS));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_RES_STATUS));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RES_CREATED_AT));

        return new Reservation(id, vId, vTitle, vPlate, vPrice, cName, cDui, cLicense, cPhone,
                cAddress, start, end, rate, days, cost, obs, status, createdAt);
    }

    private Inspection cursorToInspection(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INSP_ID));
        long resId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INSP_RES_ID));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_TYPE));
        int mileage = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INSP_MILEAGE));
        String fuel = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_FUEL));
        String exterior = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_EXTERIOR));
        String interior = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_INTERIOR));
        boolean spare = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INSP_SPARE_TIRE)) == 1;
        boolean tools = cursor.getInt(cursor.getColumnIndexOrThrow(COL_INSP_TOOLS)) == 1;
        String damages = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_DAMAGES));
        String obs = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_OBS));
        String photosJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSP_PHOTOS));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_INSP_CREATED_AT));

        List<String> photos = deserializePhotos(photosJson);
        return new Inspection(id, resId, type, mileage, fuel, exterior, interior, spare, tools,
                damages, obs, photos, createdAt);
    }

    private String serializePhotos(List<String> paths) {
        if (paths == null || paths.isEmpty()) return "[]";
        JSONArray arr = new JSONArray();
        for (String p : paths) arr.put(p);
        return arr.toString();
    }

    private List<String> deserializePhotos(String json) {
        List<String> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
