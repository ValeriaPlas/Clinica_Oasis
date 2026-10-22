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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DoctorActivity extends AppCompatActivity {

    private TextView textDoctorName;
    private ImageView imageProfile;
    private LinearLayout menuOptions;
    private Button btnLogout, btnSelectAct;
    private Button btnHistorialPacientes, btnCitas, btnAvisos;
    private Animation fadeIn;

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- VERIFICAR SESIÓN DEL DOCTOR ---
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String rol = sharedPreferences.getString("rol", null);

        if (username == null || rol == null || !rol.equals("doctor")) {
            if ("admin".equals(rol)) {
                startActivity(new Intent(this, AdminActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            } else if ("user".equals(rol)) {
                startActivity(new Intent(this, UserActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            } else {
                startActivity(new Intent(this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
            finish();
            return;
        }

        setContentView(R.layout.activity_doctor);

        textDoctorName = findViewById(R.id.textDoctorName);
        imageProfile = findViewById(R.id.imageProfile);
        menuOptions = findViewById(R.id.menuOptions);
        btnLogout = findViewById(R.id.btnLogout);
        btnSelectAct = findViewById(R.id.btnSelectAct);
        btnHistorialPacientes = findViewById(R.id.btnHistorialPacientes);
        btnCitas = findViewById(R.id.btnCitas);
        btnAvisos = findViewById(R.id.btnAvisos);

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        textDoctorName.setText("Dr. " + username);

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

        imageProfile.setOnClickListener(v -> toggleMenuOptions());

        btnLogout.setOnClickListener(v -> {
            sharedPreferences.edit().remove("username").remove("rol").apply();
            startActivity(new Intent(DoctorActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        btnSelectAct.setOnClickListener(v -> toggleActivityOptions());

        btnHistorialPacientes.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorActivity.this, HistorialPacientesActivity.class);
            intent.putExtra("usuarioActual", username);
            startActivity(intent);
        });
        btnCitas.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorActivity.this, CitasDoctorActivity.class);
            intent.putExtra("usuarioActual", username);
            startActivity(intent);
        });

        btnAvisos.setOnClickListener(v ->
                startActivity(new Intent(DoctorActivity.this, ComunicadosActivity.class)));

        findViewById(R.id.btnAbout).setOnClickListener(v ->
                startActivity(new Intent(DoctorActivity.this, AboutActivity.class)));
    }

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
        if (btnHistorialPacientes.getVisibility() == View.GONE) {
            btnHistorialPacientes.setVisibility(View.VISIBLE);
            btnCitas.setVisibility(View.VISIBLE);
            btnAvisos.setVisibility(View.VISIBLE);
            btnHistorialPacientes.startAnimation(fadeIn);
            btnCitas.startAnimation(fadeIn);
            btnAvisos.startAnimation(fadeIn);
        } else {
            btnHistorialPacientes.setVisibility(View.GONE);
            btnCitas.setVisibility(View.GONE);
            btnAvisos.setVisibility(View.GONE);
        }
    }
}
