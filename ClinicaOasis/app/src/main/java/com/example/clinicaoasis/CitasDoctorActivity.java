package com.example.clinicaoasis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CitasDoctorActivity extends AppCompatActivity {

    private LinearLayout containerCitas;
    private DatabaseReference citasRef;
    private String doctorUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas_doctor);

        containerCitas = findViewById(R.id.containerCitas);

        // Obtener doctor actual de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        doctorUsername = prefs.getString("username", null);
        String rol = prefs.getString("rol", null);

        if (doctorUsername == null || !rol.equalsIgnoreCase("doctor")) {
            Toast.makeText(this, "Acceso no autorizado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Referencia a Firebase
        citasRef = FirebaseDatabase.getInstance().getReference("Citas");

        cargarCitasDoctor();
    }

    private void cargarCitasDoctor() {
        citasRef.orderByChild("doctor").equalTo(doctorUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        containerCitas.removeAllViews();
                        if (!snapshot.exists()) {
                            Toast.makeText(CitasDoctorActivity.this, "No hay citas asignadas", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String paciente = ds.child("paciente").getValue(String.class);
                            String fecha = ds.child("fecha").getValue(String.class);
                            String hora = ds.child("hora").getValue(String.class);
                            agregarCita(paciente, fecha, hora);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CitasDoctorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void agregarCita(String paciente, String fecha, String hora) {
        // Crear un contenedor para la cita
        LinearLayout citaLayout = new LinearLayout(this);
        citaLayout.setOrientation(LinearLayout.VERTICAL);
        citaLayout.setPadding(24, 24, 24, 24);
        citaLayout.setBackgroundResource(android.R.color.white);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        citaLayout.setLayoutParams(params);

        // Texto del paciente
        TextView textPaciente = new TextView(this);
        textPaciente.setText("Paciente: " + paciente);
        textPaciente.setTextSize(16);
        textPaciente.setPadding(0, 0, 0, 8);
        textPaciente.setTextColor(getResources().getColor(android.R.color.black));

        // Texto de la fecha
        TextView textFecha = new TextView(this);
        textFecha.setText("Fecha: " + fecha);
        textFecha.setTextSize(14);
        textFecha.setPadding(0, 0, 0, 4);
        textFecha.setTextColor(getResources().getColor(android.R.color.black));

        // Texto de la hora
        TextView textHora = new TextView(this);
        textHora.setText("Hora: " + hora);
        textHora.setTextSize(14);
        textHora.setTextColor(getResources().getColor(android.R.color.black));

        // Agregar TextViews al contenedor
        citaLayout.addView(textPaciente);
        citaLayout.addView(textFecha);
        citaLayout.addView(textHora);

        // Agregar al contenedor principal
        containerCitas.addView(citaLayout);
    }
}
