package com.example.el_mozote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private String currentUsername;
    private String currentEmail;
    private String currentRole;
    private TextInputEditText nameInput;
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_profile);
        preferences = getSharedPreferences("el_mozote", MODE_PRIVATE);
        currentUsername = preferences.getString("currentUsername", "admin");
        currentEmail = preferences.getString("currentEmail", "yisset0203@gmail.com");
        String name = preferences.getString("currentName", "Administrador");
        currentRole = preferences.getString("currentRole", "Administrador");

        AppNavigation.configure(this, R.id.nav_profile);

        nameInput = findViewById(R.id.profileNameInput);
        usernameInput = findViewById(R.id.profileUsernameInput);
        emailInput = findViewById(R.id.profileEmailInput);
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        currentPasswordLayout = findViewById(R.id.currentPasswordLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);

        if (nameInput != null) nameInput.setText(name);
        if (usernameInput != null) usernameInput.setText(currentUsername);
        if (emailInput != null) emailInput.setText(currentEmail);
        refreshHeader(name, currentUsername, currentEmail, currentRole);

        MaterialButton saveButton = findViewById(R.id.saveProfileButton);
        if (saveButton != null) saveButton.setOnClickListener(v -> saveProfile());

        MaterialButton updatePasswordButton = findViewById(R.id.updatePasswordButton);
        if (updatePasswordButton != null) updatePasswordButton.setOnClickListener(v -> updatePassword());

        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) logoutButton.setOnClickListener(v -> logout());
    }

    private void saveProfile() {
        String name = value(nameInput);
        String newUsername = value(usernameInput);
        String newEmail = value(emailInput);
        if (name.isEmpty() || newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Completa el nombre, usuario y correo", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONArray users = new JSONArray(preferences.getString("users", "[]"));
            JSONObject currentUser = null;
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String accountEmail = user.optString("email", "");
                String accountUsername = user.optString("username", "");

                boolean isSameUser = accountEmail.equalsIgnoreCase(currentEmail)
                        || accountUsername.equalsIgnoreCase(currentUsername)
                        || UserCredentials.isAdminIdentifier(accountEmail)
                        || UserCredentials.isAdminIdentifier(accountUsername);

                if (!isSameUser) {
                    if (accountEmail.equalsIgnoreCase(newEmail) || accountUsername.equalsIgnoreCase(newUsername)) {
                        Toast.makeText(this, "Ese usuario o correo ya está en uso por otra cuenta", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    currentUser = user;
                }
            }

            if (currentUser == null) {
                currentUser = new JSONObject();
                currentUser.put("id", 1);
                users.put(currentUser);
            }

            currentUser.put("name", name);
            currentUser.put("username", newUsername);
            currentUser.put("email", newEmail);
            currentRole = currentUser.optString("role", currentRole);

            preferences.edit()
                    .putString("users", users.toString())
                    .putString("currentUsername", newUsername)
                    .putString("currentEmail", newEmail)
                    .putString("currentName", name)
                    .putString("currentRole", currentRole)
                    .apply();

            currentUsername = newUsername;
            currentEmail = newEmail;
            refreshHeader(name, newUsername, newEmail, currentRole);
            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudieron guardar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        if (currentPasswordLayout != null) currentPasswordLayout.setError(null);
        if (newPasswordLayout != null) newPasswordLayout.setError(null);
        String currentPassword = value(currentPasswordInput);
        String newPassword = value(newPasswordInput);
        if (currentPassword.isEmpty()) {
            if (currentPasswordLayout != null) currentPasswordLayout.setError("Ingresa tu contraseña actual");
            return;
        }
        if (newPassword.length() < 8) {
            if (newPasswordLayout != null) newPasswordLayout.setError("Usa al menos 8 caracteres");
            return;
        }
        if (!UserCredentials.authenticate(this, currentUsername, currentPassword)
                && !UserCredentials.authenticate(this, currentEmail, currentPassword)) {
            if (currentPasswordLayout != null) currentPasswordLayout.setError("La contraseña actual no es correcta");
            return;
        }
        try {
            JSONArray users = new JSONArray(preferences.getString("users", "[]"));
            boolean updated = false;
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String accountEmail = user.optString("email", "");
                String accountUsername = user.optString("username", "");
                if (accountEmail.equalsIgnoreCase(currentEmail) || accountUsername.equalsIgnoreCase(currentUsername)
                        || UserCredentials.isAdminIdentifier(accountEmail)) {
                    user.put("passwordHash", UserCredentials.hash(newPassword));
                    users.put(i, user);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                Toast.makeText(this, "No se encontró la cuenta activa", Toast.LENGTH_SHORT).show();
                return;
            }
            preferences.edit().putString("users", users.toString()).apply();
            if (currentPasswordInput != null) currentPasswordInput.setText("");
            if (newPasswordInput != null) newPasswordInput.setText("");
            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo actualizar la contraseña", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshHeader(String name, String username, String email, String role) {
        TextView displayName = findViewById(R.id.profileDisplayName);
        TextView displayUsername = findViewById(R.id.profileDisplayUsername);
        TextView displayEmail = findViewById(R.id.profileDisplayEmail);
        TextView roleBadge = findViewById(R.id.profileRoleBadge);
        TextView initialsView = findViewById(R.id.profileInitials);

        if (displayName != null) displayName.setText(name);
        if (displayUsername != null) displayUsername.setText("Usuario: " + username);
        if (displayEmail != null) displayEmail.setText("Correo: " + email);
        if (roleBadge != null) roleBadge.setText(role);
        if (initialsView != null) initialsView.setText(initials(name));
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "U";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String value(TextInputEditText input) {
        return input == null || input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void logout() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}
