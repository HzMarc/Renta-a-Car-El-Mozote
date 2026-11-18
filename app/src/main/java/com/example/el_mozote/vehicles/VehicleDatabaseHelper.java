package com.example.el_mozote.vehicles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class VehicleDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "el_mozote_vehicles.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_VEHICLES = "vehicles";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_PLATE = "plate";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_PRICE_PER_DAY = "price_per_day";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_PHOTO_PATHS = "photo_paths";
    public static final String COLUMN_CREATED_AT = "created_at";

    private static VehicleDatabaseHelper instance;

    public static synchronized VehicleDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VehicleDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private VehicleDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_VEHICLES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_BRAND + " TEXT NOT NULL, "
                + COLUMN_MODEL + " TEXT NOT NULL, "
                + COLUMN_YEAR + " INTEGER NOT NULL, "
                + COLUMN_PLATE + " TEXT NOT NULL UNIQUE, "
                + COLUMN_COLOR + " TEXT NOT NULL, "
                + COLUMN_PRICE_PER_DAY + " REAL NOT NULL, "
                + COLUMN_STATUS + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_PHOTO_PATHS + " TEXT, "
                + COLUMN_CREATED_AT + " INTEGER"
                + ")";
        db.execSQL(createTable);

        // Pre-poblar con vehículos de demostración
        insertSampleVehicles(db);
    }

    private void insertSampleVehicles(SQLiteDatabase db) {
        insertSample(db, "Toyota", "Hilux 4x4", 2023, "P 892-104", "Gris Metálico", 65.0,
                Vehicle.STATUS_AVAILABLE,
                "Pickup diésel 4x4 en excelente estado, doble cabina, aire acondicionado, pantalla táctil y gran capacidad de carga.");

        insertSample(db, "Hyundai", "Elantra", 2022, "P 451-230", "Blanco Polar", 40.0,
                Vehicle.STATUS_RENTED,
                "Sedán automático, muy eficiente en combustible, cámara de retroceso, asientos cómodos, ideal para la ciudad.");

        insertSample(db, "Nissan", "Kicks", 2021, "P 710-985", "Rojo Escarlata", 48.0,
                Vehicle.STATUS_MAINTENANCE,
                "SUV compacta en revisión preventiva programada de frenos, alineación y cambio de aceite.");
    }

    private void insertSample(SQLiteDatabase db, String brand, String model, int year,
                              String plate, String color, double price, String status, String desc) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BRAND, brand);
        cv.put(COLUMN_MODEL, model);
        cv.put(COLUMN_YEAR, year);
        cv.put(COLUMN_PLATE, plate);
        cv.put(COLUMN_COLOR, color);
        cv.put(COLUMN_PRICE_PER_DAY, price);
        cv.put(COLUMN_STATUS, status);
        cv.put(COLUMN_DESCRIPTION, desc);
        cv.put(COLUMN_PHOTO_PATHS, "[]");
        cv.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        db.insert(TABLE_VEHICLES, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        onCreate(db);
    }

    public long insertVehicle(Vehicle vehicle) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BRAND, vehicle.getBrand());
        cv.put(COLUMN_MODEL, vehicle.getModel());
        cv.put(COLUMN_YEAR, vehicle.getYear());
        cv.put(COLUMN_PLATE, vehicle.getPlate());
        cv.put(COLUMN_COLOR, vehicle.getColor());
        cv.put(COLUMN_PRICE_PER_DAY, vehicle.getPricePerDay());
        cv.put(COLUMN_STATUS, vehicle.getStatus());
        cv.put(COLUMN_DESCRIPTION, vehicle.getDescription());
        cv.put(COLUMN_PHOTO_PATHS, serializePhotoPaths(vehicle.getPhotoPaths()));
        cv.put(COLUMN_CREATED_AT, vehicle.getCreatedAt() > 0 ? vehicle.getCreatedAt() : System.currentTimeMillis());

        long id = db.insert(TABLE_VEHICLES, null, cv);
        if (id != -1) {
            vehicle.setId(id);
        }
        return id;
    }

    public int updateVehicle(Vehicle vehicle) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_BRAND, vehicle.getBrand());
        cv.put(COLUMN_MODEL, vehicle.getModel());
        cv.put(COLUMN_YEAR, vehicle.getYear());
        cv.put(COLUMN_PLATE, vehicle.getPlate());
        cv.put(COLUMN_COLOR, vehicle.getColor());
        cv.put(COLUMN_PRICE_PER_DAY, vehicle.getPricePerDay());
        cv.put(COLUMN_STATUS, vehicle.getStatus());
        cv.put(COLUMN_DESCRIPTION, vehicle.getDescription());
        cv.put(COLUMN_PHOTO_PATHS, serializePhotoPaths(vehicle.getPhotoPaths()));

        return db.update(TABLE_VEHICLES, cv, COLUMN_ID + " = ?",
                new String[]{String.valueOf(vehicle.getId())});
    }

    public int deleteVehicle(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_VEHICLES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Vehicle getVehicleById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_VEHICLES, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Vehicle vehicle = null;
        if (cursor != null && cursor.moveToFirst()) {
            vehicle = cursorToVehicle(cursor);
            cursor.close();
        }
        return vehicle;
    }

    public List<Vehicle> getAllVehicles() {
        return searchVehicles(null, null);
    }

    public List<Vehicle> searchVehicles(String query, String statusFilter) {
        List<Vehicle> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (statusFilter != null && !statusFilter.isEmpty() && !"Todos".equalsIgnoreCase(statusFilter)) {
            selection.append(COLUMN_STATUS).append(" = ?");
            selectionArgs.add(statusFilter);
        }

        if (query != null && !query.trim().isEmpty()) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append("(")
                    .append(COLUMN_BRAND).append(" LIKE ? OR ")
                    .append(COLUMN_MODEL).append(" LIKE ? OR ")
                    .append(COLUMN_PLATE).append(" LIKE ?")
                    .append(")");
            String pattern = "%" + query.trim() + "%";
            selectionArgs.add(pattern);
            selectionArgs.add(pattern);
            selectionArgs.add(pattern);
        }

        String whereClause = selection.length() > 0 ? selection.toString() : null;
        String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);

        Cursor cursor = db.query(TABLE_VEHICLES, null, whereClause, args, null, null,
                COLUMN_ID + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToVehicle(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public int getCountTotal() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VEHICLES, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public int getCountByStatus(String status) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VEHICLES + " WHERE " + COLUMN_STATUS + " = ?",
                new String[]{status});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public boolean isPlateExists(String plate, long excludeId) {
        if (plate == null) return false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_VEHICLES, new String[]{COLUMN_ID},
                COLUMN_PLATE + " = ? AND " + COLUMN_ID + " != ?",
                new String[]{plate.trim(), String.valueOf(excludeId)}, null, null, null);
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToFirst();
            cursor.close();
        }
        return exists;
    }

    private Vehicle cursorToVehicle(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND));
        String model = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR));
        String plate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLATE));
        String color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE_PER_DAY));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
        String photoJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_PATHS));
        long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));

        List<String> photos = deserializePhotoPaths(photoJson);
        return new Vehicle(id, brand, model, year, plate, color, price, status, desc, photos, createdAt);
    }

    private String serializePhotoPaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return "[]";
        }
        JSONArray jsonArray = new JSONArray();
        for (String path : paths) {
            jsonArray.put(path);
        }
        return jsonArray.toString();
    }

    private List<String> deserializePhotoPaths(String json) {
        List<String> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return list;
        }
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
