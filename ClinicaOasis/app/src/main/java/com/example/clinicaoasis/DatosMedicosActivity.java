package com.example.clinicaoasis;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatosMedicosActivity extends AppCompatActivity {

    private Spinner spinnerTipoSangre;
    private EditText editTextAlergias, editTextEnfermedades, editTextPeso, editTextAltura;
    private Button btnGuardarInfo;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_medicos);

        spinnerTipoSangre = findViewById(R.id.spinnerTipoSangre);
        editTextAlergias = findViewById(R.id.editTextAlergias);
        editTextEnfermedades = findViewById(R.id.editTextEnfermedades);
        editTextPeso = findViewById(R.id.editTextPeso);
        editTextAltura = findViewById(R.id.editTextAltura);
        btnGuardarInfo = findViewById(R.id.btnGuardarInfo);

        // Spinner de tipos de sangre
        String[] tiposSangre = {"Selecciona tipo de sangre", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tiposSangre);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoSangre.setAdapter(adapter);

        // Inicializar Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("UsuariosMedicos");

        // Guardar información al presionar el botón
        btnGuardarInfo.setOnClickListener(v -> guardarInformacion());

        // Cargar datos si existen
        cargarInformacion();
    }

    private void guardarInformacion() {
        String tipoSangre = spinnerTipoSangre.getSelectedItem().toString();
        String alergias = editTextAlergias.getText().toString().trim();
        String enfermedades = editTextEnfermedades.getText().toString().trim();
        String peso = editTextPeso.getText().toString().trim();
        String altura = editTextAltura.getText().toString().trim();

        if (tipoSangre.equals("Selecciona tipo de sangre")) {
            Toast.makeText(this, "Selecciona tu tipo de sangre", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el username del usuario logueado
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) {
            Toast.makeText(this, "Error: usuario no logueado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar en Firebase bajo el nodo de este usuario
        databaseRef.child(username).setValue(new InfoMedica(tipoSangre, alergias, enfermedades, peso, altura))
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Guardar también en SharedPreferences local por si quieres cargar rápido
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("tipoSangre", tipoSangre);
        editor.putString("alergias", alergias);
        editor.putString("enfermedades", enfermedades);
        editor.putString("peso", peso);
        editor.putString("altura", altura);
        editor.apply();
    }

    private void cargarInformacion() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username == null) return;

        // Cargar datos locales si existen
        String tipoSangre = prefs.getString("tipoSangre", "Selecciona tipo de sangre");
        String alergias = prefs.getString("alergias", "");
        String enfermedades = prefs.getString("enfermedades", "");
        String peso = prefs.getString("peso", "");
        String altura = prefs.getString("altura", "");

        spinnerTipoSangre.setSelection(((ArrayAdapter) spinnerTipoSangre.getAdapter()).getPosition(tipoSangre));
        editTextAlergias.setText(alergias);
        editTextEnfermedades.setText(enfermedades);
        editTextPeso.setText(peso);
        editTextAltura.setText(altura);
    }

    // Clase para enviar a Firebase
    public static class InfoMedica {
        public String tipoSangre;
        public String alergias;
        public String enfermedades;
        public String peso;
        public String altura;

        public InfoMedica() {} // Constructor vacío necesario para Firebase

        public InfoMedica(String tipoSangre, String alergias, String enfermedades, String peso, String altura) {
            this.tipoSangre = tipoSangre;
            this.alergias = alergias;
            this.enfermedades = enfermedades;
            this.peso = peso;
            this.altura = altura;
        }
    }
}
