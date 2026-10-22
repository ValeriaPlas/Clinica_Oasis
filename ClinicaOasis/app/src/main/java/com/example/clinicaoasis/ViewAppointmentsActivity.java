package com.example.clinicaoasis;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewAppointmentsActivity extends AppCompatActivity {

    private ListView listFutureAppointments, listPastAppointments;
    private ArrayList<String> futureList, pastList;
    private ArrayAdapter<String> adapterFuture, adapterPast;

    private DatabaseReference appointmentsRef;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm"); // formato día + horario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        listFutureAppointments = findViewById(R.id.listFutureAppointments);
        listPastAppointments = findViewById(R.id.listPastAppointments);

        futureList = new ArrayList<>();
        pastList = new ArrayList<>();

        adapterFuture = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, futureList);
        adapterPast = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pastList);

        listFutureAppointments.setAdapter(adapterFuture);
        listPastAppointments.setAdapter(adapterPast);

        // Firebase
        appointmentsRef = FirebaseDatabase.getInstance().getReference("citas");

        // Cargar citas en tiempo real
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                futureList.clear();
                pastList.clear();

                Date now = new Date();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String patientName = snap.child("usuarioCreador").getValue(String.class); // usuario que creó la cita
                    String doctorName = snap.child("doctor").getValue(String.class);
                    String dia = snap.child("dia").getValue(String.class);
                    String horario = snap.child("horario").getValue(String.class);

                    if (patientName == null) patientName = "Desconocido";
                    if (doctorName == null) doctorName = "Desconocido";
                    if (dia == null) dia = "Fecha desconocida";
                    if (horario == null) horario = "Horario desconocido";

                    String displayText = patientName + " - " + doctorName + " - " + dia + " " + horario;

                    try {
                        Date appointmentDate = sdf.parse(dia + " " + horario);
                        if (appointmentDate != null && appointmentDate.after(now)) {
                            futureList.add(displayText);
                        } else {
                            pastList.add(displayText);
                        }
                    } catch (ParseException e) {
                        pastList.add(displayText + " (fecha inválida)");
                    }
                }

                adapterFuture.notifyDataSetChanged();
                adapterPast.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAppointmentsActivity.this, "Error al cargar citas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
