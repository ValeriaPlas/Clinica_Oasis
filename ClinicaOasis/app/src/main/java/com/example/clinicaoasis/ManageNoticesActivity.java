package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ManageNoticesActivity extends AppCompatActivity {

    private EditText edtNoticeText;
    private Button btnAddNotice;
    private ListView listNotices;

    private DatabaseReference noticesRef;
    private ArrayList<String> noticesList;
    private ArrayAdapter<String> adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_notices);

        // Referencias UI
        edtNoticeText = findViewById(R.id.edtNoticeText);
        btnAddNotice = findViewById(R.id.btnAddNotice);
        listNotices = findViewById(R.id.listNotices);

        // Firebase referencia -> nodo "avisos"
        noticesRef = FirebaseDatabase.getInstance().getReference("avisos");

        // Adaptador de la lista
        noticesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, noticesList);
        listNotices.setAdapter(adapter);

        // Agregar aviso
        btnAddNotice.setOnClickListener(v -> {
            String aviso = edtNoticeText.getText().toString().trim();
            if (TextUtils.isEmpty(aviso)) {
                Toast.makeText(this, "Ingresa un aviso", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = noticesRef.push().getKey(); // Generar ID único
            noticesRef.child(id).setValue(aviso);

            edtNoticeText.setText("");
            Toast.makeText(this, "Aviso agregado", Toast.LENGTH_SHORT).show();
        });

        // Cargar avisos en tiempo real
        noticesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noticesList.clear();
                for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                    String aviso = noticeSnap.getValue(String.class);
                    noticesList.add(aviso);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageNoticesActivity.this, "Error al cargar avisos", Toast.LENGTH_SHORT).show();
            }
        });

        // Eliminar aviso manteniendo presionado
        listNotices.setOnItemLongClickListener((parent, view, position, id) -> {
            String aviso = noticesList.get(position);

            noticesRef.orderByValue().equalTo(aviso).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                        noticeSnap.getRef().removeValue();
                    }
                    Toast.makeText(ManageNoticesActivity.this, "Aviso eliminado", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            return true;
        });

        // Editar aviso con un simple clic (reemplaza el texto en el EditText para actualizar)
        listNotices.setOnItemClickListener((parent, view, position, id) -> {
            String aviso = noticesList.get(position);
            edtNoticeText.setText(aviso);

            btnAddNotice.setText("Actualizar Aviso");

            btnAddNotice.setOnClickListener(v -> {
                String nuevoAviso = edtNoticeText.getText().toString().trim();
                if (!TextUtils.isEmpty(nuevoAviso)) {
                    noticesRef.orderByValue().equalTo(aviso).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                                noticeSnap.getRef().setValue(nuevoAviso);
                            }
                            Toast.makeText(ManageNoticesActivity.this, "Aviso actualizado", Toast.LENGTH_SHORT).show();
                            edtNoticeText.setText("");
                            btnAddNotice.setText("Agregar Aviso");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            });
        });
    }
}