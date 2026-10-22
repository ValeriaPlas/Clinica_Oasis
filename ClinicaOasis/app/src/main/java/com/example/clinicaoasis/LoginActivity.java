package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText editTextUsuario, editTextPassword;
    ImageView imageViewPerfil;
    CheckBox checkBoxShowPassword;
    Button btnAceptarUsuario, btnLogin, btnRegistrarse, btnDeleteProfile;
    LinearLayout layoutUsuario, layoutLogin;

    private DatabaseReference usersRef;
    private Usuario selectedUser;

    public static class Usuario {
        public String alias;
        public String username;
        public String email;
        public String password;
        public String rol;
        public String imageName;

        public Usuario() {}
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editTextPassword = findViewById(R.id.editTextPassword);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        btnAceptarUsuario = findViewById(R.id.btnAceptarUsuario);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnDeleteProfile = findViewById(R.id.btnDeleteProfile);
        layoutUsuario = findViewById(R.id.layoutUsuario);
        layoutLogin = findViewById(R.id.layoutLogin);

        layoutLogin.setVisibility(View.GONE);
        imageViewPerfil.setVisibility(View.VISIBLE);

        crearAdminPorDefecto();

        // Revisar sesión guardada
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUser = prefs.getString("username", null);
        String currentRol = prefs.getString("rol", null);
        if (currentUser != null && currentRol != null) {
            redirigirPorRol(currentRol.trim().toLowerCase(), currentUser);
            return;
        }

        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) ->
                editTextPassword.setInputType(isChecked ?
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));

        btnAceptarUsuario.setOnClickListener(v -> {
            String input = editTextUsuario.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Ingrese un nombre de usuario o correo", Toast.LENGTH_SHORT).show();
                return;
            }
            buscarUsuario(input);
        });

        btnLogin.setOnClickListener(v -> {
            if (selectedUser == null) {
                Toast.makeText(this, "Primero selecciona un usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            String password = editTextPassword.getText().toString().trim();
            if (password.equals(selectedUser.password)) {

                // --- GUARDAR SESIÓN CON ROL CORREGIDO ---
                String rolToSave = "user"; // por defecto
                if (selectedUser.rol != null) {
                    String lowerRol = selectedUser.rol.trim().toLowerCase();
                    if (lowerRol.equals("doctor") || lowerRol.equals("admin")) {
                        rolToSave = lowerRol;
                    }
                }

                prefs.edit()
                        .putString("username", selectedUser.username)
                        .putString("rol", rolToSave)
                        .apply();

                redirigirPorRol(rolToSave, selectedUser.username);

            } else {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegistrarse.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        btnDeleteProfile.setOnClickListener(v -> {
            if (selectedUser == null) {
                Toast.makeText(this, "Selecciona un usuario", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("admin".equalsIgnoreCase(selectedUser.rol)) {
                Toast.makeText(this, "No puedes eliminar al admin", Toast.LENGTH_SHORT).show();
                return;
            }
            usersRef.child(selectedUser.alias).removeValue();
            selectedUser = null;
            ocultarSeccionPassword();
            Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void buscarUsuario(String input) {
        usersRef.orderByChild("alias").equalTo(input)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                selectedUser = ds.getValue(Usuario.class);
                                break;
                            }
                            mostrarSeccionPassword();
                        } else {
                            usersRef.orderByChild("email").equalTo(input)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    selectedUser = ds.getValue(Usuario.class);
                                                    break;
                                                }
                                                mostrarSeccionPassword();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearAdminPorDefecto() {
        usersRef.orderByChild("alias").equalTo("admin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            String id = usersRef.push().getKey();
                            Usuario admin = new Usuario();
                            admin.alias = "admin";
                            admin.username = "admin";
                            admin.email = "admin@clinica.com";
                            admin.password = "Admin123!";
                            admin.rol = "admin";
                            admin.imageName = "perfil_default";
                            usersRef.child(id).setValue(admin);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void redirigirPorRol(String rol, String username) {
        Intent intent;
        switch (rol) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "doctor":
                intent = new Intent(this, DoctorActivity.class);
                break;
            default:
                intent = new Intent(this, UserActivity.class);
                break;
        }
        intent.putExtra("username", username);
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void mostrarSeccionPassword() {
        int imageResId = getResources().getIdentifier(
                selectedUser.imageName != null ? selectedUser.imageName : "perfil_default",
                "drawable",
                getPackageName());
        imageViewPerfil.setImageResource(imageResId != 0 ? imageResId : R.drawable.perfil_default);

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(400);
        layoutUsuario.startAnimation(fadeOut);
        layoutUsuario.setVisibility(LinearLayout.GONE);

        layoutLogin.setVisibility(LinearLayout.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(400);
        layoutLogin.startAnimation(fadeIn);
    }

    private void ocultarSeccionPassword() {
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(400);
        layoutLogin.startAnimation(fadeOut);
        layoutLogin.setVisibility(LinearLayout.GONE);

        layoutUsuario.setVisibility(LinearLayout.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(400);
        layoutUsuario.startAnimation(fadeIn);
    }
}
