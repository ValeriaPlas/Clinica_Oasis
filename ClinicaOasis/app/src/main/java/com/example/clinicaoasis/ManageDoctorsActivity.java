package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageDoctorsActivity extends AppCompatActivity {

    private EditText edtDoctorName, edtConsultorio, edtHorarios, edtDias;
    private Button btnAddOrUpdateDoctor;
    private ListView listDoctors;

    private DatabaseReference doctorsRef;
    private ArrayList<String> doctorsList;
    private ArrayList<Doctor> doctorObjects;
    private ArrayAdapter<String> adapter;

    private String doctorSeleccionadoId = null; // para saber si estamos editando

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_doctors);

        // Referencias UI
        edtDoctorName = findViewById(R.id.edtDoctorName);
        edtConsultorio = findViewById(R.id.edtConsultorio);
        edtHorarios = findViewById(R.id.edtHorarios);
        edtDias = findViewById(R.id.edtDias);
        btnAddOrUpdateDoctor = findViewById(R.id.btnAddDoctor);
        listDoctors = findViewById(R.id.listDoctors);

        doctorsRef = FirebaseDatabase.getInstance().getReference("doctores");

        doctorsList = new ArrayList<>();
        doctorObjects = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorsList);
        listDoctors.setAdapter(adapter);

        // Crear datos de ejemplo si la base está vacía
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    llenarDoctoresEjemplo();
                    Toast.makeText(ManageDoctorsActivity.this, "Datos de ejemplo creados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDoctorsActivity.this, "Error verificando base de datos", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón agregar o guardar cambios
        btnAddOrUpdateDoctor.setOnClickListener(v -> {
            String nombre = edtDoctorName.getText().toString().trim();
            String consultorio = edtConsultorio.getText().toString().trim();
            String[] horariosArray = edtHorarios.getText().toString().trim().split(",");
            String[] diasArray = edtDias.getText().toString().trim().split(",");

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(consultorio)
                    || horariosArray.length == 0 || diasArray.length == 0) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> horarios = Arrays.asList(horariosArray);
            List<String> dias = Arrays.asList(diasArray);

            if (doctorSeleccionadoId == null) {
                // Agregar nuevo
                String id = doctorsRef.push().getKey();
                if (id != null) {
                    Doctor doctor = new Doctor(id, nombre, consultorio, horarios, dias);
                    doctorsRef.child(id).setValue(doctor);
                    Toast.makeText(this, "Doctor agregado correctamente", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Editar existente
                Doctor doctorActualizado = new Doctor(doctorSeleccionadoId, nombre, consultorio, horarios, dias);
                doctorsRef.child(doctorSeleccionadoId).setValue(doctorActualizado);
                Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show();
                doctorSeleccionadoId = null;
                btnAddOrUpdateDoctor.setText("Agregar Doctor");
            }

            // Limpiar campos
            edtDoctorName.setText("");
            edtConsultorio.setText("");
            edtHorarios.setText("");
            edtDias.setText("");
        });

        // Mostrar lista de doctores
        doctorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsList.clear();
                doctorObjects.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot doctorSnap : snapshot.getChildren()) {
                        Doctor doctor = doctorSnap.getValue(Doctor.class);
                        if (doctor != null) {
                            doctorObjects.add(doctor);
                            doctorsList.add(doctor.nombre + " - " + doctor.consultorio);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDoctorsActivity.this, "Error al cargar doctores", Toast.LENGTH_SHORT).show();
            }
        });

        // Tocar un doctor para editarlo
        listDoctors.setOnItemClickListener((parent, view, position, id) -> {
            Doctor doctor = doctorObjects.get(position);
            doctorSeleccionadoId = doctor.id;

            edtDoctorName.setText(doctor.nombre);
            edtConsultorio.setText(doctor.consultorio);
            edtHorarios.setText(TextUtils.join(",", doctor.horarios));
            edtDias.setText(TextUtils.join(",", doctor.dias));

            btnAddOrUpdateDoctor.setText("Guardar cambios");
        });

        // 🚫 No hay opción de eliminar
        listDoctors.setOnItemLongClickListener(null);
    }

    // Cargar doctores de ejemplo
    private void llenarDoctoresEjemplo() {
        String[][] ejemplo = {
                {"Juan Pérez", "Consultorio 1", "08:00-09:00,09:00-10:00,10:00-11:00", "Lunes,Martes,Miércoles"},
                {"Ana López", "Consultorio 1", "14:00-15:00,15:00-16:00,16:00-17:00", "Lunes,Martes,Miércoles"},
                {"Carlos Ruiz", "Consultorio 2", "08:00-09:00,09:00-10:00,10:00-11:00", "Martes,Miércoles,Jueves"},
                {"Laura Gómez", "Consultorio 2", "14:00-15:00,15:00-16:00,16:00-17:00", "Martes,Miércoles,Jueves"},
                {"Miguel Torres", "Consultorio 3", "08:00-09:00,09:00-10:00,10:00-11:00", "Miércoles,Jueves,Viernes"},
                {"Isabel Díaz", "Consultorio 3", "14:00-15:00,15:00-16:00,16:00-17:00", "Miércoles,Jueves,Viernes"}
        };

        for (String[] d : ejemplo) {
            String id = doctorsRef.push().getKey();
            if (id != null) {
                Doctor doctor = new Doctor(
                        id,
                        d[0],
                        d[1],
                        Arrays.asList(d[2].split(",")),
                        Arrays.asList(d[3].split(","))
                );
                doctorsRef.child(id).setValue(doctor);
            }
        }
    }

    // Clase Doctor
    public static class Doctor {
        public String id;
        public String nombre;
        public String consultorio;
        public List<String> horarios;
        public List<String> dias;

        public Doctor() {} // Requerido por Firebase

        public Doctor(String id, String nombre, String consultorio, List<String> horarios, List<String> dias) {
            this.id = id;
            this.nombre = nombre;
            this.consultorio = consultorio;
            this.horarios = horarios;
            this.dias = dias;
        }
    }
}
