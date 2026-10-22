package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.HashMap;
import java.util.Map;

public class HistorialPacientesActivity extends AppCompatActivity {

    private LinearLayout containerUsuarios;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_pacientes);

        containerUsuarios = findViewById(R.id.containerUsuarios);
        databaseRef = FirebaseDatabase.getInstance().getReference("UsuariosMedicos");

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                containerUsuarios.removeAllViews();
                if (!snapshot.exists()) {
                    Toast.makeText(HistorialPacientesActivity.this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String username = ds.getKey();
                    Map<String, String> info = new HashMap<>();
                    if (ds.exists()) {
                        info.put("tipoSangre", ds.child("tipoSangre").getValue(String.class));
                        info.put("alergias", ds.child("alergias").getValue(String.class));
                        info.put("enfermedades", ds.child("enfermedades").getValue(String.class));
                        info.put("peso", ds.child("peso").getValue(String.class));
                        info.put("altura", ds.child("altura").getValue(String.class));
                    }
                    agregarUsuario(username, info);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialPacientesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarUsuario(String username, Map<String, String> info) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_usuario, containerUsuarios, false);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView textUsername = itemView.findViewById(R.id.textUsername);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout infoContainer = itemView.findViewById(R.id.infoContainer);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView textInfo = itemView.findViewById(R.id.textInfo);

        textUsername.setText(username);

        // Construir información
        StringBuilder sb = new StringBuilder();
        sb.append("Tipo de sangre: ").append(info.getOrDefault("tipoSangre", "-")).append("\n");
        sb.append("Alergias: ").append(info.getOrDefault("alergias", "-")).append("\n");
        sb.append("Enfermedades: ").append(info.getOrDefault("enfermedades", "-")).append("\n");
        sb.append("Peso: ").append(info.getOrDefault("peso", "-")).append("\n");
        sb.append("Altura: ").append(info.getOrDefault("altura", "-"));
        textInfo.setText(sb.toString());

        // Ocultar info al inicio
        infoContainer.setVisibility(View.GONE);

        // Expandir / colapsar al presionar el username
        textUsername.setOnClickListener(v -> {
            if (infoContainer.getVisibility() == View.GONE) {
                infoContainer.setVisibility(View.VISIBLE);
            } else {
                infoContainer.setVisibility(View.GONE);
            }
        });

        containerUsuarios.addView(itemView);
    }
}
