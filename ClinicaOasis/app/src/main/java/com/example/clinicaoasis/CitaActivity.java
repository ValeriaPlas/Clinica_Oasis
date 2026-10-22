package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CitaActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button btnAgregarCita;
    private long fechaSeleccionada;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);

        calendarView = findViewById(R.id.calendarView);
        btnAgregarCita = findViewById(R.id.btnAgregarCita);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Inicializamos con la fecha actual del calendario
        fechaSeleccionada = calendarView.getDate();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada = new GregorianCalendar(year, month, dayOfMonth).getTimeInMillis();
        });

        btnAgregarCita.setOnClickListener(v -> {
            long hoy = System.currentTimeMillis();
            String username = sharedPreferences.getString("username", null);

            if (username == null) {
                Toast.makeText(this, "No se encontró el usuario, inicia sesión de nuevo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (fechaSeleccionada <= hoy) {
                Toast.makeText(this, "No se puede agendar en el mismo día ni días anteriores", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convertir la fecha a formato dd/MM/yyyy
            Date date = new Date(fechaSeleccionada);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaFormateada = sdf.format(date);

            // Pasar la fecha y usuario al AgendarCitaActivity
            Intent intent = new Intent(CitaActivity.this, AgendarCitaActivity.class);
            intent.putExtra("fechaSeleccionada", fechaFormateada);
            intent.putExtra("usuarioActual", username);
            startActivity(intent);
        });
    }
}
