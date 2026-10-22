package com.example.clinicaoasis;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ComunicadosActivity extends AppCompatActivity {

    private RecyclerView recyclerComunicados;
    private Button btnBack;
    private ArrayList<String> comunicadosList;
    private ComunicadosAdapter adapter;

    private DatabaseReference noticesRef; // Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicados);

        recyclerComunicados = findViewById(R.id.recyclerComunicados);
        btnBack = findViewById(R.id.btnBack);

        comunicadosList = new ArrayList<>();
        adapter = new ComunicadosAdapter(comunicadosList);

        recyclerComunicados.setLayoutManager(new LinearLayoutManager(this));
        recyclerComunicados.setAdapter(adapter);

        // Conexión a Firebase (nodo "avisos")
        noticesRef = FirebaseDatabase.getInstance().getReference("avisos");

        // Listener en tiempo real
        noticesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comunicadosList.clear();
                for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                    String aviso = noticeSnap.getValue(String.class);
                    comunicadosList.add(aviso);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Puedes manejar errores aquí
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private class ComunicadosAdapter extends RecyclerView.Adapter<ComunicadosAdapter.ViewHolder> {

        private ArrayList<String> comunicados;

        public ComunicadosAdapter(ArrayList<String> comunicados) {
            this.comunicados = comunicados;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comunicado, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textComunicado.setText(comunicados.get(position));
        }

        @Override
        public int getItemCount() {
            return comunicados.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textComunicado;
            MaterialCardView cardView;

            public ViewHolder(View itemView) {
                super(itemView);
                textComunicado = itemView.findViewById(R.id.textComunicado);
                cardView = itemView.findViewById(R.id.cardComunicado);
            }
        }
    }
}
