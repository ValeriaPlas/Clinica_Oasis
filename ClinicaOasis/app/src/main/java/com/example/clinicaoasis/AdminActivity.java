package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private TextView textAdmin;
    private LinearLayout menuOptions;
    private Button btnAbout, btnLogout,btnCrud;
    private Button btnManageDoctors, btnManageNotices, btnViewAppointments;

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener sesión del usuario
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        // Si no hay usuario logueado o no es admin, redirigir a LoginActivity
        if (username == null || !username.equals("admin")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_admin);

        // Inicializar views
        textAdmin = findViewById(R.id.textAdmin);
        menuOptions = findViewById(R.id.menuOptions);

        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);
        btnCrud = findViewById(R.id.btnCrudUsuarios);

        btnManageDoctors = findViewById(R.id.btnManageDoctors);
        btnManageNotices = findViewById(R.id.btnManageNotices);
        btnViewAppointments = findViewById(R.id.btnViewAppointments);

        // Mostrar/ocultar menú al presionar el card de administración
        textAdmin.setOnClickListener(v -> {
            if (menuOptions.getVisibility() == View.GONE) {
                menuOptions.setVisibility(View.VISIBLE);
            } else {
                menuOptions.setVisibility(View.GONE);
            }
        });

        // Botón About → abre AboutActivity
        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        // Botón Logout → limpia sesión y vuelve a LoginActivity
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("username"); // borrar sesión
            editor.apply();

            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Abrir gestión de doctores
        btnManageDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageDoctorsActivity.class);
            startActivity(intent);
        });

        // Abrir gestión de avisos
        btnManageNotices.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ManageNoticesActivity.class);
            startActivity(intent);
        });

        btnCrud.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, CrudUsuariosActivity.class);
            startActivity(intent);
        });

        // Ver citas futuras y pasadas
        btnViewAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ViewAppointmentsActivity.class);
            startActivity(intent);
        });
    }
}
