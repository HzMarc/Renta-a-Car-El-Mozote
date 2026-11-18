package com.example.el_mozote;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserManagementActivity extends AppCompatActivity {
    private FrameLayout mainContent;
    private final List<User> users = new ArrayList<>();
    private UserAdapter userAdapter;
    private TextInputEditText userSearch;
    private TextView usersCount;
    private TextView usersEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        mainContent = findViewById(R.id.userContent);
        MaterialToolbar toolbar = findViewById(R.id.userToolbar);
        AppNavigation.configure(this, R.id.nav_users);
        setupSampleUsers();
        showUserManagement();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.nav_exit, R.anim.nav_enter);
    }

    private void setupSampleUsers() {
        users.clear();
        SharedPreferences preferences = getSharedPreferences("el_mozote", MODE_PRIVATE);
        String saved = preferences.getString("users", null);
        UserCredentials.ensureUnifiedAdminRecord(this, saved);
        saved = preferences.getString("users", null);
        if (saved != null) {
            try {
                JSONArray array = new JSONArray(saved);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String email = item.optString("email", "");
                    String username = item.optString("username", "");
                    if (username.isEmpty()) {
                        username = "admin".equalsIgnoreCase(email) ? "admin"
                                : (email.contains("@") ? email.substring(0, email.indexOf('@')) : email);
                    }
                    String role = item.optString("role", "Cliente");
                    if (UserCredentials.isAdminIdentifier(email) || UserCredentials.isAdminIdentifier(username)) {
                        role = "Administrador";
                    }
                    users.add(new User(item.getInt("id"), item.getString("name"),
                            username, email, item.optString("phone"),
                            role, item.optString("status", "Activo"),
                            item.optString("passwordHash", "")));
                }
            } catch (Exception ignored) {
                users.clear();
            }
        }
    }

    private void saveUsers() {
        JSONArray array = new JSONArray();
        try {
            for (User user : users) {
                JSONObject item = new JSONObject();
                item.put("id", user.id);
                item.put("name", user.name);
                item.put("username", user.username);
                item.put("email", user.email);
                item.put("phone", user.phone);
                item.put("role", user.role);
                item.put("status", user.status);
                item.put("passwordHash", user.passwordHash);
                array.put(item);
            }
            getSharedPreferences("el_mozote", MODE_PRIVATE).edit()
                    .putString("users", array.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void showUserManagement() {
        View view = getLayoutInflater().inflate(R.layout.view_user_management, mainContent, false);
        RecyclerView recycler = view.findViewById(R.id.usersRecycler);
        usersEmpty = view.findViewById(R.id.usersEmpty);
        usersCount = view.findViewById(R.id.usersCount);
        userSearch = view.findViewById(R.id.userSearch);

        userAdapter = new UserAdapter(new ArrayList<>(), new UserAdapter.Listener() {
            @Override public void onOpen(User user) { showUserDetail(user); }
            @Override public void onEdit(User user) { showUserForm(user); }
            @Override public void onStatus(User user, String status) {
                if ("Desactivado".equalsIgnoreCase(status) && isAdministrator(user) && !hasAnotherActiveAdministrator(user)) {
                    Toast.makeText(UserManagementActivity.this, "Debe quedar al menos un administrador activo", Toast.LENGTH_SHORT).show();
                    return;
                }
                user.status = status;
                saveUsers();
                applyUserFilters();
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(userAdapter);
        view.findViewById(R.id.addUserButton).setOnClickListener(v -> showUserForm(null));
        if (userSearch != null) {
            userSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyUserFilters(); }
                @Override public void afterTextChanged(Editable s) { }
            });
        }
        applyUserFilters();
        mainContent.addView(view);
    }

    private void applyUserFilters() {
        if (userAdapter == null) return;
        String query = userSearch == null || userSearch.getText() == null
                ? "" : userSearch.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        List<User> filtered = new ArrayList<>();
        for (User user : users) {
            String name = user.name.toLowerCase(java.util.Locale.ROOT);
            String username = user.username.toLowerCase(java.util.Locale.ROOT);
            String email = user.email.toLowerCase(java.util.Locale.ROOT);
            boolean matchesQuery = query.isEmpty() || name.contains(query) || username.contains(query) || email.contains(query);
            if (matchesQuery) filtered.add(user);
        }
        userAdapter.submit(filtered);
        if (usersCount != null) {
            usersCount.setText(users.size() + (users.size() == 1 ? " usuario registrado" : " usuarios registrados"));
        }
        if (usersEmpty != null) {
            if (filtered.isEmpty()) {
                usersEmpty.setText(users.isEmpty()
                        ? "Aún no hay usuarios. Agrega una cuenta para empezar."
                        : "No encontramos usuarios con esos criterios.");
                usersEmpty.setVisibility(View.VISIBLE);
            } else {
                usersEmpty.setVisibility(View.GONE);
            }
        }
    }

    private boolean isAdministrator(User user) {
        return user.id == 1 || UserCredentials.isAdminIdentifier(user.username)
                || UserCredentials.isAdminIdentifier(user.email)
                || user.role.toLowerCase(java.util.Locale.ROOT).contains("admin");
    }

    private boolean hasAnotherActiveAdministrator(User current) {
        for (User user : users) {
            if (user.id != current.id && isAdministrator(user) && "Activo".equalsIgnoreCase(user.status)) return true;
        }
        return false;
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
        TextInputLayout usernameLayout = addField(root, "Nombre de usuario");
        TextInputLayout emailLayout = addField(root, "Correo electrónico");
        TextInputLayout phoneLayout = addField(root, "Teléfono");
        TextInputLayout roleLayout = new TextInputLayout(this);
        roleLayout.setHint("Rol de acceso");
        roleLayout.setEndIconMode(TextInputLayout.END_ICON_DROPDOWN_MENU);
        MaterialAutoCompleteTextView roleInput = new MaterialAutoCompleteTextView(roleLayout.getContext());
        roleInput.setInputType(InputType.TYPE_NULL);
        roleInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new String[]{"Cliente", "Administrador"}));
        roleLayout.addView(roleInput);
        LinearLayout.LayoutParams roleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        roleParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
        root.addView(roleLayout, roleParams);

        SwitchMaterial activeSwitch = new SwitchMaterial(this);
        activeSwitch.setText("Cuenta activa para iniciar sesión");
        activeSwitch.setTextColor(getColor(R.color.obsidian_navy));
        LinearLayout.LayoutParams activeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        activeParams.topMargin = (int) (12 * getResources().getDisplayMetrics().density);
        root.addView(activeSwitch, activeParams);
        TextInputLayout passwordLayout = addField(root, existing == null
                ? "Contraseña de acceso" : "Nueva contraseña (opcional)");
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        if (passwordLayout.getEditText() != null) {
            passwordLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        if (existing != null) {
            if (nameLayout.getEditText() != null) nameLayout.getEditText().setText(existing.name);
            if (usernameLayout.getEditText() != null) usernameLayout.getEditText().setText(existing.username);
            if (emailLayout.getEditText() != null) emailLayout.getEditText().setText(existing.email);
            if (phoneLayout.getEditText() != null) phoneLayout.getEditText().setText(existing.phone);
            roleInput.setText(isAdministrator(existing) ? "Administrador" : "Cliente", false);
            activeSwitch.setChecked("Activo".equalsIgnoreCase(existing.status));
        } else {
            roleInput.setText("Cliente", false);
            activeSwitch.setChecked(true);
        }

        MaterialButton save = new MaterialButton(this);
        save.setText(existing == null ? "Agregar" : "Guardar");
        save.setOnClickListener(v -> {
            String name = nameLayout.getEditText() != null ? nameLayout.getEditText().getText().toString().trim() : "";
            String username = usernameLayout.getEditText() != null ? usernameLayout.getEditText().getText().toString().trim() : "";
            String email = emailLayout.getEditText() != null ? emailLayout.getEditText().getText().toString().trim() : "";
            String password = passwordLayout.getEditText() != null ? passwordLayout.getEditText().getText().toString().trim() : "";
            String selectedRole = roleInput.getText() == null ? "" : roleInput.getText().toString().trim();
            String selectedStatus = activeSwitch.isChecked() ? "Activo" : "Desactivado";

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || (existing == null && password.isEmpty())) {
                Toast.makeText(this, existing == null
                        ? "Completa nombre, usuario, correo y contraseña"
                        : "Completa nombre, usuario y correo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!"Cliente".equals(selectedRole) && !"Administrador".equals(selectedRole)) {
                roleLayout.setError("Selecciona Cliente o Administrador");
                return;
            }
            roleLayout.setError(null);
            if ((existing == null || !password.isEmpty()) && password.length() < 8) {
                Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            for (User user : users) {
                if ((existing == null || user.id != existing.id)
                        && (user.username.equalsIgnoreCase(username) || user.email.equalsIgnoreCase(email))) {
                    Toast.makeText(this, "Ya existe un usuario con ese nombre de usuario o correo", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            boolean wasActiveAdministrator = existing != null && isAdministrator(existing)
                    && "Activo".equalsIgnoreCase(existing.status);
            boolean willBeActiveAdministrator = (existing != null && existing.id == 1
                    || "Administrador".equals(selectedRole)) && "Activo".equals(selectedStatus);
            if (wasActiveAdministrator && !willBeActiveAdministrator && !hasAnotherActiveAdministrator(existing)) {
                Toast.makeText(this, "Debe quedar al menos un administrador activo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (existing == null) {
                int nextId = 1;
                for (User user : users) nextId = Math.max(nextId, user.id + 1);
                String phoneStr = phoneLayout.getEditText() != null ? phoneLayout.getEditText().getText().toString().trim() : "";
                users.add(new User(nextId, name, username, email,
                        phoneStr,
                        selectedRole,
                        selectedStatus,
                        UserCredentials.hash(password)));
            } else {
                existing.name = name;
                existing.username = username;
                existing.email = email;
                if (phoneLayout.getEditText() != null) {
                    existing.phone = phoneLayout.getEditText().getText().toString().trim();
                }
                existing.role = existing.id == 1 ? "Administrador" : selectedRole;
                existing.status = selectedStatus;
                if (!password.isEmpty()) existing.passwordHash = UserCredentials.hash(password);
            }
            saveUsers();
            sheet.dismiss();
            applyUserFilters();
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
        root.addView(detailLine("Usuario", user.username));
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

    private static class User {
        final int id;
        String name;
        String username;
        String email;
        String phone;
        String role;
        String status;
        String passwordHash;

        User(int id, String name, String username, String email, String phone, String role, String status, String passwordHash) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
            this.passwordHash = passwordHash;
        }
    }

    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.Holder> {
        interface Listener {
            void onOpen(User user);
            void onEdit(User user);
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
            if (holder.username != null) {
                holder.username.setText("Usuario: " + user.username);
            }
            if (holder.email != null) {
                holder.email.setText("Correo: " + user.email);
            }
            holder.role.setText(user.role);
            holder.status.setText("Suspendido".equalsIgnoreCase(user.status) ? "Desactivado" : user.status);
            boolean active = "Activo".equalsIgnoreCase(user.status);
            holder.status.setBackgroundResource(active
                    ? R.drawable.status_badge_active
                    : R.drawable.status_badge_inactive);
            holder.status.setTextColor(holder.status.getContext().getColor(active
                    ? R.color.emerald_dark : R.color.slate_muted));
            holder.itemView.setOnClickListener(v -> listener.onOpen(user));
            holder.menu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(new androidx.appcompat.view.ContextThemeWrapper(v.getContext(), R.style.Theme_El_Mozote), v);
                popup.getMenu().add("Ver detalles");
                popup.getMenu().add("Editar");
                boolean userIsActive = "Activo".equalsIgnoreCase(user.status);
                popup.getMenu().add(userIsActive ? "Desactivar" : "Activar");
                popup.setOnMenuItemClickListener(item -> {
                    String action = item.getTitle().toString();
                    if ("Ver detalles".equals(action)) listener.onOpen(user);
                    else if ("Editar".equals(action)) listener.onEdit(user);
                    else if ("Activar".equals(action)) listener.onStatus(user, "Activo");
                    else if ("Desactivar".equals(action)) listener.onStatus(user, "Desactivado");
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
            TextView initial, name, username, email, role, status;
            ImageButton menu;

            Holder(@NonNull View itemView) {
                super(itemView);
                initial = itemView.findViewById(R.id.userInitial);
                name = itemView.findViewById(R.id.userName);
                username = itemView.findViewById(R.id.userUsername);
                email = itemView.findViewById(R.id.userEmail);
                role = itemView.findViewById(R.id.userRole);
                status = itemView.findViewById(R.id.userStatus);
                menu = itemView.findViewById(R.id.userMenu);
            }
        }
    }
}
