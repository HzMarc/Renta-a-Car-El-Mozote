package com.example.el_mozote;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class UserCredentials {
    private UserCredentials() { }

    public static String hash(String password) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) result.append(String.format(java.util.Locale.ROOT, "%02x", value));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo proteger la contraseña", e);
        }
    }

    public static boolean isAdminIdentifier(String identifier) {
        if (identifier == null) return false;
        String id = identifier.trim().toLowerCase(java.util.Locale.ROOT);
        return id.equals("admin") || id.contains("yisset");
    }

    public static boolean authenticate(Context context, String identifier, String password) {
        if (identifier == null || password == null || identifier.trim().isEmpty() || password.isEmpty()) return false;
        String enteredIdentifier = identifier.trim();
        String enteredHash = hash(password);
        boolean isAdmin = isAdminIdentifier(enteredIdentifier);

        SharedPreferences preferences = context.getSharedPreferences("el_mozote", Context.MODE_PRIVATE);
        String saved = preferences.getString("users", null);

        if (isAdmin) {
            boolean validPassword = "1234".equals(password);
            if (!validPassword && saved != null) {
                try {
                    JSONArray users = new JSONArray(saved);
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        String email = user.optString("email", "");
                        String username = user.optString("username", "");
                        if (isAdminIdentifier(email) || isAdminIdentifier(username)) {
                            String pHash = user.optString("passwordHash", "");
                            if (!pHash.isEmpty() && pHash.equals(enteredHash)) {
                                validPassword = true;
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) { }
            }

            if (validPassword) {
                ensureUnifiedAdminRecord(context, saved);
                saveSession(context, "admin", "yisset0203@gmail.com", "Administrador", "Administrador");
                return true;
            }
            return false;
        }

        if (saved != null) {
            try {
                JSONArray users = new JSONArray(saved);
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    String account = user.optString("email", "");
                    String username = user.optString("username", "");
                    String status = user.optString("status", "Activo");
                    String passwordHash = user.optString("passwordHash", "");
                    boolean active = "Activo".equalsIgnoreCase(status);
                    String role = user.optString("role", "Cliente");

                    boolean matches = account.equalsIgnoreCase(enteredIdentifier)
                            || username.equalsIgnoreCase(enteredIdentifier);

                    if (matches && active && !passwordHash.isEmpty() && passwordHash.equals(enteredHash)) {
                        saveSession(context, username.isEmpty() ? account : username, account, user.optString("name", account), role);
                        return true;
                    }
                }
            } catch (Exception ignored) { }
        }

        return false;
    }

    public static void ensureUnifiedAdminRecord(Context context, String saved) {
        try {
            JSONArray users = saved == null ? new JSONArray() : new JSONArray(saved);
            JSONArray cleanedUsers = new JSONArray();
            JSONObject adminUser = null;
            int nextId = 1;

            for (int i = 0; i < users.length(); i++) {
                JSONObject item = users.getJSONObject(i);
                int id = item.optInt("id", 0);
                nextId = Math.max(nextId, id + 1);
                String email = item.optString("email", "");
                String username = item.optString("username", "");
                if (isAdminIdentifier(email) || isAdminIdentifier(username)) {
                    if (adminUser == null) {
                        adminUser = item;
                    }
                } else {
                    cleanedUsers.put(item);
                }
            }

            if (adminUser == null) {
                adminUser = new JSONObject();
                adminUser.put("id", 1);
                adminUser.put("name", "Administrador");
                adminUser.put("username", "admin");
                adminUser.put("email", "yisset0203@gmail.com");
                adminUser.put("phone", "");
                adminUser.put("role", "Administrador");
                adminUser.put("status", "Activo");
                adminUser.put("passwordHash", hash("1234"));
            } else {
                adminUser.put("name", "Administrador");
                adminUser.put("username", "admin");
                adminUser.put("email", "yisset0203@gmail.com");
                adminUser.put("role", "Administrador");
                adminUser.put("status", "Activo");
                if (adminUser.optString("passwordHash", "").isEmpty()) {
                    adminUser.put("passwordHash", hash("1234"));
                }
            }

            cleanedUsers.put(adminUser);
            context.getSharedPreferences("el_mozote", Context.MODE_PRIVATE).edit()
                    .putString("users", cleanedUsers.toString()).apply();
        } catch (Exception ignored) { }
    }

    public static void saveSession(Context context, String username, String email, String name, String role) {
        context.getSharedPreferences("el_mozote", Context.MODE_PRIVATE).edit()
                .putString("currentUsername", username)
                .putString("currentEmail", email)
                .putString("currentName", name)
                .putString("currentRole", role)
                .apply();
    }
}
