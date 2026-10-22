package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserActivity extends AppCompatActivity {

    private TextView textUsername;
    private ImageView imageProfile;
    private LinearLayout menuOptions;
    private Button btnLogout, btnSelectAct;
    private Button btnDatosMedicos, btnCita, btnComunicados, btnHistorial;
    private Animation fadeIn;

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- VERIFICAR SESIÓN ---
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String rol = sharedPreferences.getString("rol", null);

        // Si no hay usuario logueado o el rol no es user, redirigir a Login
        if (username == null || rol == null || !rol.equals("user")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_user);

        // --- INICIALIZAR VIEWS ---
        textUsername = findViewById(R.id.textUsername);
        imageProfile = findViewById(R.id.imageProfile);
        menuOptions = findViewById(R.id.menuOptions);
        btnLogout = findViewById(R.id.btnLogout);
        btnSelectAct = findViewById(R.id.btnSelectAct);
        btnDatosMedicos = findViewById(R.id.btnDatosMedicos);
        btnCita = findViewById(R.id.btnCita);
        btnComunicados = findViewById(R.id.btnComunicados);
        btnHistorial = findViewById(R.id.btnHistorial);

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // --- NOMBRE DEL USUARIO ---
        textUsername.setText("Hola, " + username);

        // --- CARGAR IMAGEN DE PERFIL DESDE FIREBASE ---
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Usuarios").child(username);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String imageName = task.getResult().child("imageName").getValue(String.class);
                setProfileImage(imageName != null ? imageName : "perfil_default");
            } else {
                setProfileImage("perfil_default");
            }
        });

        // --- MENÚ OPCIONES ---
        imageProfile.setOnClickListener(v -> toggleMenuOptions());

        btnLogout.setOnClickListener(v -> {
            sharedPreferences.edit().remove("username").remove("rol").apply();
            startActivity(new Intent(UserActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        btnSelectAct.setOnClickListener(v -> toggleActivityOptions());

        // --- BOTONES DE ACTIVIDAD ---
        btnDatosMedicos.setOnClickListener(v ->
                startActivity(new Intent(UserActivity.this, DatosMedicosActivity.class)));

        btnComunicados.setOnClickListener(v ->
                startActivity(new Intent(UserActivity.this, ComunicadosActivity.class)));

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, HistorialActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnCita.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, CitaActivity.class);
            intent.putExtra("usuarioActual", username);
            startActivity(intent);
        });

        // Opciones del menú
        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(UserActivity.this, EditProfileActivity.class)));

        findViewById(R.id.btnAbout).setOnClickListener(v ->
                startActivity(new Intent(UserActivity.this, AboutActivity.class)));
    }

    // --- MÉTODOS AUXILIARES ---
    private void setProfileImage(String imageName) {
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        imageProfile.setImageResource(resId != 0 ? resId : R.drawable.perfil_default);
    }

    private void toggleMenuOptions() {
        if (menuOptions.getVisibility() == View.GONE) {
            menuOptions.setVisibility(View.VISIBLE);
            menuOptions.startAnimation(fadeIn);
        } else {
            menuOptions.setVisibility(View.GONE);
        }
    }

    private void toggleActivityOptions() {
        if (btnDatosMedicos.getVisibility() == View.GONE) {
            btnDatosMedicos.setVisibility(View.VISIBLE);
            btnCita.setVisibility(View.VISIBLE);
            btnComunicados.setVisibility(View.VISIBLE);
            btnHistorial.setVisibility(View.VISIBLE);
            btnDatosMedicos.startAnimation(fadeIn);
            btnCita.startAnimation(fadeIn);
            btnComunicados.startAnimation(fadeIn);
            btnHistorial.startAnimation(fadeIn);
        } else {
            btnDatosMedicos.setVisibility(View.GONE);
            btnCita.setVisibility(View.GONE);
            btnComunicados.setVisibility(View.GONE);
            btnHistorial.setVisibility(View.GONE);
        }
    }
}
