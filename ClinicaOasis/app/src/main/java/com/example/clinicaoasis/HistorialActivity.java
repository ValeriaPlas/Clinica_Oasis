package com.example.clinicaoasis;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistorial;
    private TextView textViewEmpty;
    private Button btnBack;
    private HistorialAdapter historialAdapter;
    private ArrayList<String> historialList;

    private String usuarioActual; // Usuario para filtrar historial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerViewHistorial = findViewById(R.id.recyclerViewHistorial);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        btnBack = findViewById(R.id.btnBack);

        // Recibir usuario desde Intent
        usuarioActual = getIntent().getStringExtra("username");

        historialList = new ArrayList<>();
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        historialAdapter = new HistorialAdapter(historialList);
        recyclerViewHistorial.setAdapter(historialAdapter);

        cargarHistorial(usuarioActual);

        btnBack.setOnClickListener(v -> finish());
    }

    private void cargarHistorial(String usuario) {
        DatabaseReference citasRef = FirebaseDatabase.getInstance().getReference("citas");

        // Filtrar solo las citas del usuario
        citasRef.orderByChild("usuarioCreador").equalTo(usuario)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historialList.clear();
                        for (DataSnapshot citaSnap : snapshot.getChildren()) {
                            String doctor = citaSnap.child("doctor").getValue(String.class);
                            String consultorio = citaSnap.child("consultorio").getValue(String.class);
                            String dia = citaSnap.child("dia").getValue(String.class);
                            String horario = citaSnap.child("horario").getValue(String.class);

                            if (doctor != null && consultorio != null && dia != null && horario != null) {
                                historialList.add(dia + " - " + doctor + " - " + consultorio + " - " + horario);
                            }
                        }
                        historialAdapter.notifyDataSetChanged();
                        updateEmptyView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textViewEmpty.setText("Error al cargar historial");
                        updateEmptyView();
                    }
                });
    }

    private void updateEmptyView() {
        if (historialList.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewHistorial.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewHistorial.setVisibility(View.VISIBLE);
        }
    }
}
