package com.example.el_mozote;

import android.content.res.ColorStateList;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private BottomSheetDialog authSheet;
    private View appShell;
    private View welcomeScreen;
    private FrameLayout mainContent;
    private MaterialToolbar mainToolbar;
    private int selectedNavId = R.id.nav_home;
    private View userManagementView;
    private final List<User> users = new ArrayList<>();
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        welcomeScreen = findViewById(R.id.welcomeScreen);
        appShell = findViewById(R.id.appShell);
        mainContent = findViewById(R.id.mainContent);
        mainToolbar = findViewById(R.id.mainToolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(appShell, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int navigationHeight = (int) (56 * getResources().getDisplayMetrics().density);
            View navigation = findViewById(R.id.bottomNavigation);
            navigation.getLayoutParams().height = navigationHeight;
            navigation.requestLayout();
            mainToolbar.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        setupBottomNavigation();
        ensureStaticData();
        MaterialButton startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> openAuthBottomSheet());
    }

    private void ensureStaticData() {
        SharedPreferences preferences = getSharedPreferences("el_mozote", MODE_PRIVATE);
        UserCredentials.ensureUnifiedAdminRecord(this, preferences.getString("users", null));
    }

    private void openAuthBottomSheet() {
        if (authSheet != null && authSheet.isShowing()) return;

        authSheet = new BottomSheetDialog(this, R.style.ThemeOverlay_ElMozote_BottomSheet);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_auth, null);
        authSheet.setContentView(sheetView);
        authSheet.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        authSheet.getWindow().setDimAmount(0f);

        View welcomeContent = findViewById(R.id.welcomeContent);
        View welcomeBrand = findViewById(R.id.welcomeBrand);
        View topBrand = findViewById(R.id.topBrand);

        welcomeContent.animate().alpha(0f).translationY(24f).setDuration(220).start();
        welcomeBrand.animate().scaleX(0.82f).scaleY(0.82f).setDuration(180).start();
        topBrand.setVisibility(View.VISIBLE);
        topBrand.animate().alpha(1f).translationY(0f).setStartDelay(140).setDuration(220).start();

        authSheet.setOnDismissListener(dialog -> {
            if (appShell.getVisibility() == View.VISIBLE) return;
            welcomeContent.animate().alpha(1f).translationY(0f).setDuration(220).start();
            welcomeBrand.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
            topBrand.animate().alpha(0f).translationY(-12f).setDuration(180)
                    .withEndAction(() -> topBrand.setVisibility(View.GONE)).start();
        });

        ViewSwitcher authViewSwitcher = sheetView.findViewById(R.id.authViewSwitcher);
        Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_slide_in);
        Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_slide_out);
        authViewSwitcher.setInAnimation(inAnimation);
        authViewSwitcher.setOutAnimation(outAnimation);

        TextInputLayout loginEmailLayout = sheetView.findViewById(R.id.loginEmailLayout);
        TextInputLayout loginPasswordLayout = sheetView.findViewById(R.id.loginPasswordLayout);
        TextInputEditText loginEmail = sheetView.findViewById(R.id.loginEmail);
        TextInputEditText loginPassword = sheetView.findViewById(R.id.loginPassword);

        sheetView.findViewById(R.id.loginButton).setOnClickListener(v -> {
            loginEmailLayout.setError(null);
            loginPasswordLayout.setError(null);
            String identifier = text(loginEmail);
            String password = text(loginPassword);
            if (TextUtils.isEmpty(identifier)) {
                loginEmailLayout.setError("Ingresa usuario o correo");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                loginPasswordLayout.setError("Ingresa una contraseña");
                return;
            }
            if (UserCredentials.authenticate(this, identifier, password)) {
                enterApp();
            } else {
                loginPasswordLayout.setError("Usuario o contraseña incorrectos");
            }
        });

        sheetView.findViewById(R.id.googleButton).setOnClickListener(v ->
                Toast.makeText(this, R.string.demo_google, Toast.LENGTH_SHORT).show());

        sheetView.findViewById(R.id.openRegisterButton).setOnClickListener(v -> {
            if (authViewSwitcher.getDisplayedChild() == 0) authViewSwitcher.showNext();
        });

        TextInputEditText registerName = sheetView.findViewById(R.id.registerName);
        TextInputEditText registerEmail = sheetView.findViewById(R.id.registerEmail);
        TextInputEditText registerPassword = sheetView.findViewById(R.id.registerPassword);
        TextInputEditText registerConfirm = sheetView.findViewById(R.id.registerConfirm);

        sheetView.findViewById(R.id.backToLoginButton).setOnClickListener(v -> {
            if (authViewSwitcher.getDisplayedChild() == 1) authViewSwitcher.showPrevious();
        });

        sheetView.findViewById(R.id.registerButton).setOnClickListener(v -> {
            if (TextUtils.isEmpty(text(registerName))) {
                registerName.setError("Ingresa tu nombre");
                return;
            }
            if (TextUtils.isEmpty(text(registerEmail))) {
                registerEmail.setError("Ingresa un correo");
                return;
            }
            if (TextUtils.isEmpty(text(registerPassword))) {
                registerPassword.setError("Ingresa una contraseña");
                return;
            }
            if (text(registerPassword).length() < 6) {
                registerPassword.setError("La contraseña debe tener al menos 6 caracteres");
                return;
            }
            if (!text(registerPassword).equals(text(registerConfirm))) {
                registerConfirm.setError("Las contraseñas no coinciden");
                return;
            }
            if (registerUser(text(registerName), text(registerEmail),
                    text(registerPassword))) enterApp();
        });

        authSheet.show();
    }

    private void enterApp() {
        if (authSheet != null) authSheet.dismiss();
        welcomeScreen.animate().alpha(0f).setDuration(180).withEndAction(() -> welcomeScreen.setVisibility(View.GONE)).start();
        View nav = findViewById(R.id.bottomNavigation);
        nav.setVisibility(View.VISIBLE);
        nav.requestLayout();
        appShell.setAlpha(0f);
        appShell.setVisibility(View.VISIBLE);
        appShell.animate().alpha(1f).setDuration(260).start();
        selectNavItem(R.id.nav_home);
        showDestination(R.id.nav_home);
    }

    private boolean registerUser(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        SharedPreferences preferences = getSharedPreferences("el_mozote", MODE_PRIVATE);

        try {
            JSONArray users = new JSONArray(preferences.getString("users", "[]"));
            String username = normalizedEmail.contains("@")
                    ? normalizedEmail.substring(0, normalizedEmail.indexOf('@'))
                    : normalizedEmail;

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                if (normalizedEmail.equalsIgnoreCase(user.optString("email", ""))
                        || username.equalsIgnoreCase(user.optString("username", ""))) {
                    Toast.makeText(this, "Ese correo o usuario ya está registrado", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            int nextId = 1;
            for (int i = 0; i < users.length(); i++) {
                nextId = Math.max(nextId, users.getJSONObject(i).optInt("id", 0) + 1);
            }

            JSONObject user = new JSONObject();
            user.put("id", nextId);
            user.put("name", name.trim());
            user.put("username", username);
            user.put("email", normalizedEmail);
            user.put("phone", "");
            user.put("role", "Cliente");
            user.put("status", "Activo");
            user.put("passwordHash", UserCredentials.hash(password));
            users.put(user);

            preferences.edit()
                    .putString("users", users.toString())
                    .apply();
            UserCredentials.saveSession(this, username, normalizedEmail, name.trim(), "Cliente");
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo crear la cuenta", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void setupBottomNavigation() {
        int[] ids = {R.id.nav_home, R.id.nav_vehicles, R.id.nav_reservations, R.id.nav_users, R.id.nav_profile};
        for (int id : ids) {
            View item = findViewById(id);
            item.setOnClickListener(v -> {
                selectNavItem(id);
                showDestination(id);
            });
        }
        selectNavItem(R.id.nav_home);
    }

    private void selectNavItem(int itemId) {
        selectedNavId = itemId;
        int[] ids = {R.id.nav_home, R.id.nav_vehicles, R.id.nav_reservations, R.id.nav_users, R.id.nav_profile};
        for (int id : ids) {
            LinearLayout item = findViewById(id);
            item.setBackgroundResource(id == itemId
                    ? R.drawable.nav_item_selected
                    : android.R.color.transparent);
            TextView label = null;
            for (int child = 0; child < item.getChildCount(); child++) {
                View view = item.getChildAt(child);
                if (view instanceof TextView) label = (TextView) view;
            }
            if (label != null) {
                label.setTextColor(getResources().getColor(
                        id == itemId ? R.color.emerald_on_container_light : R.color.emerald_primary,
                        getTheme()));
            }
        }
    }

    private void showDestination(int itemId) {
        mainContent.removeAllViews();
        findViewById(R.id.appBar).setVisibility(View.VISIBLE);
        if (itemId == R.id.nav_home) {
            mainToolbar.setTitle(R.string.nav_home);
            addPlaceholder(
                    getString(R.string.nav_home),
                    "Resumen de reservas, vehículos disponibles y actividad reciente.");
        } else if (itemId == R.id.nav_vehicles) {
            mainToolbar.setTitle(R.string.nav_vehicles);
            addPlaceholder(
                    getString(R.string.nav_vehicles),
                    "Inventario, disponibilidad, mantenimiento y precios de vehículos.");
        } else if (itemId == R.id.nav_reservations) {
            mainToolbar.setTitle(R.string.nav_reservations);
            addPlaceholder(
                    getString(R.string.nav_reservations),
                    "Reservas pendientes, activas, completadas y canceladas.");
        } else if (itemId == R.id.nav_users) {
            startActivity(new Intent(this, UserManagementActivity.class));
            overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit);
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.nav_enter, R.anim.nav_exit);
        }
    }

    private void addPlaceholder(String title, String description) {
        LinearLayout placeholder = new LinearLayout(this);
        placeholder.setOrientation(LinearLayout.VERTICAL);
        placeholder.setGravity(android.view.Gravity.CENTER);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        placeholder.setPadding(padding, padding, padding, padding);
        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextAppearance(this, android.R.style.TextAppearance_Material_Headline);
        TextView body = new TextView(this);
        body.setText(description);
        body.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        body.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
        placeholder.addView(heading);
        placeholder.addView(body);
        mainContent.addView(placeholder);
    }

    @Override
    public void onBackPressed() {
        if (appShell != null && appShell.getVisibility() == View.VISIBLE
                && selectedNavId != R.id.nav_home) {
            selectNavItem(R.id.nav_home);
            showDestination(R.id.nav_home);
            return;
        }
        super.onBackPressed();
    }

    private void setupSampleUsers() {
        users.clear();
    }

    private void showUserManagement() {
        View view = getLayoutInflater().inflate(R.layout.fragment_user_management, mainContent, false);
        userManagementView = view;
        mainContent.addView(view);
    }

    private void showUserForm(User existing) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText(existing == null ? "Agregar usuario" : "Editar usuario");
        title.setTextAppearance(this, android.R.style.TextAppearance_Material_Headline);
        root.addView(title);

        TextInputLayout nameLayout = addField(root, "Nombre completo");
        TextInputLayout emailLayout = addField(root, "Correo electrónico");
        TextInputLayout phoneLayout = addField(root, "Teléfono");
        TextInputLayout roleLayout = addField(root, "Rol");
        TextInputLayout statusLayout = addField(root, "Estado");

        if (existing != null) {
            nameLayout.getEditText().setText(existing.name);
            emailLayout.getEditText().setText(existing.email);
            phoneLayout.getEditText().setText(existing.phone);
            roleLayout.getEditText().setText(existing.role);
            statusLayout.getEditText().setText(existing.status);
        }

        MaterialButton save = new MaterialButton(this);
        save.setText(existing == null ? "Agregar" : "Guardar");
        save.setOnClickListener(v -> {
            String name = nameLayout.getEditText().getText().toString().trim();
            String email = emailLayout.getEditText().getText().toString().trim();
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Completa nombre y correo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (existing == null) {
                users.add(new User(users.size() + 1, name, email,
                        phoneLayout.getEditText().getText().toString().trim(),
                        roleLayout.getEditText().getText().toString().trim(),
                        statusLayout.getEditText().getText().toString().trim()));
            } else {
                existing.name = name;
                existing.email = email;
                existing.phone = phoneLayout.getEditText().getText().toString().trim();
                existing.role = roleLayout.getEditText().getText().toString().trim();
                existing.status = statusLayout.getEditText().getText().toString().trim();
            }
            sheet.dismiss();
            if (userAdapter != null) userAdapter.submit(new ArrayList<>(users));
            Snackbar.make(findViewById(R.id.main), "Usuario guardado", Snackbar.LENGTH_SHORT).show();
        });
        root.addView(save);
        sheet.setContentView(root);
        sheet.show();
    }

    private TextInputLayout addField(LinearLayout root, String hint) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setHint(hint);
        TextInputEditText editText = new TextInputEditText(layout.getContext());
        layout.addView(editText);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        root.addView(layout, params);
        return layout;
    }

    private void showUserDetail(User user) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        TextView name = new TextView(this);
        name.setText(user.name);
        name.setTextAppearance(this, android.R.style.TextAppearance_Material_Headline);
        root.addView(name);
        root.addView(detailLine("Correo", user.email));
        root.addView(detailLine("Teléfono", user.phone));
        root.addView(detailLine("Rol", user.role));
        root.addView(detailLine("Estado", user.status));
        MaterialButton edit = new MaterialButton(this);
        edit.setText("Editar");
        edit.setOnClickListener(v -> {
            sheet.dismiss();
            showUserForm(user);
        });
        root.addView(edit);
        sheet.setContentView(root);
        sheet.show();
    }

    private TextView detailLine(String label, String value) {
        TextView view = new TextView(this);
        view.setText(label + ": " + value);
        view.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);
        return view;
    }

    private void confirmDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Eliminar a " + user.name + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int index = users.indexOf(user);
                    users.remove(user);
                    if (userAdapter != null) userAdapter.submit(new ArrayList<>(users));
                    Snackbar.make(findViewById(R.id.main), "Usuario eliminado", Snackbar.LENGTH_LONG)
                            .setAction("Deshacer", v -> {
                                users.add(Math.max(0, index), user);
                                if (userAdapter != null) userAdapter.submit(new ArrayList<>(users));
                            })
                            .show();
                })
                .show();
    }

    private String text(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "U";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private static class User {
        final int id;
        String name;
        String email;
        String phone;
        String role;
        String status;

        User(int id, String name, String email, String phone, String role, String status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
        }
    }

    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {
        interface Listener {
            void onOpen(User user);
            void onEdit(User user);
            void onDelete(User user);
            void onStatus(User user, String status);
        }

        private final List<User> data = new ArrayList<>();
        private final Listener listener;

        UserAdapter(List<User> data, Listener listener) {
            this.data.addAll(data);
            this.listener = listener;
        }

        void submit(List<User> users) {
            data.clear();
            data.addAll(users);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            User user = data.get(position);
            holder.initial.setText(user.name.isEmpty()
                    ? "?"
                    : String.valueOf(user.name.charAt(0)).toUpperCase());
            holder.name.setText(user.name);
            holder.email.setText(user.email);
            holder.role.setText(user.role);
            holder.status.setText(user.status);
            holder.itemView.setOnClickListener(v -> listener.onOpen(user));
            holder.menu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add("Ver detalles");
                popup.getMenu().add("Editar");
                popup.getMenu().add("Activar");
                popup.getMenu().add("Suspender");
                popup.getMenu().add("Eliminar");
                popup.setOnMenuItemClickListener(item -> {
                    String action = item.getTitle().toString();
                    if ("Ver detalles".equals(action)) listener.onOpen(user);
                    else if ("Editar".equals(action)) listener.onEdit(user);
                    else if ("Activar".equals(action)) listener.onStatus(user, "Activo");
                    else if ("Suspender".equals(action)) listener.onStatus(user, "Suspendido");
                    else listener.onDelete(user);
                    return true;
                });
                popup.show();
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView initial, name, email, role, status;
            ImageButton menu;

            Holder(@NonNull View itemView) {
                super(itemView);
                initial = itemView.findViewById(R.id.userInitial);
                name = itemView.findViewById(R.id.userName);
                email = itemView.findViewById(R.id.userEmail);
                role = itemView.findViewById(R.id.userRole);
                status = itemView.findViewById(R.id.userStatus);
                menu = itemView.findViewById(R.id.userMenu);
            }
        }
    }
}
