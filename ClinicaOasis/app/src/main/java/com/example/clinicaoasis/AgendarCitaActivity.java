package com.example.clinicaoasis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class AgendarCitaActivity extends AppCompatActivity {

    private Spinner spinnerDoctor, spinnerConsultorio, spinnerHorario;
    private Button btnConfirmarCita;
    private TextView txtResumenCita, txtDiaSeleccionado;
    private CheckBox checkboxNotificarCorreo;
    private EditText editTextCorreo;

    private final Map<String, Doctor> doctoresMap = new HashMap<>();
    private String diaSeleccionado;
    private String usuarioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);

        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        spinnerConsultorio = findViewById(R.id.spinnerConsultorio);
        spinnerHorario = findViewById(R.id.spinnerHorario);
        btnConfirmarCita = findViewById(R.id.btnConfirmarCita);
        txtResumenCita = findViewById(R.id.txtResumenCita);
        txtDiaSeleccionado = findViewById(R.id.txtDiaSeleccionado);
        checkboxNotificarCorreo = findViewById(R.id.checkboxNotificarCorreo);
        editTextCorreo = findViewById(R.id.editTextCorreo);

        editTextCorreo.setVisibility(View.GONE);

        diaSeleccionado = getIntent().getStringExtra("fechaSeleccionada");
        usuarioActual = getIntent().getStringExtra("usuarioActual");

        if (diaSeleccionado != null && !diaSeleccionado.isEmpty()) {
            txtDiaSeleccionado.setText("Día seleccionado: " + diaSeleccionado);
        } else {
            txtDiaSeleccionado.setText("Selecciona un día en el calendario");
        }

        DatabaseReference doctorsRef = FirebaseDatabase.getInstance().getReference("doctores");
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> nombresDoctores = new ArrayList<>();
                doctoresMap.clear();

                for (DataSnapshot doctorSnap : snapshot.getChildren()) {
                    Doctor doctor = doctorSnap.getValue(Doctor.class);
                    if (doctor == null || doctor.nombre == null) continue;

                    doctoresMap.put(doctor.nombre, doctor);
                    nombresDoctores.add(doctor.nombre);
                }

                if (nombresDoctores.isEmpty()) {
                    Toast.makeText(AgendarCitaActivity.this, "No hay doctores registrados", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayAdapter<String> adapterDoctores = new ArrayAdapter<>(AgendarCitaActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, nombresDoctores);
                spinnerDoctor.setAdapter(adapterDoctores);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AgendarCitaActivity.this, "Error al cargar doctores", Toast.LENGTH_SHORT).show();
            }
        });

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String doctorNombre = (String) spinnerDoctor.getSelectedItem();
                if (doctorNombre == null) return;

                Doctor doctor = doctoresMap.get(doctorNombre);
                if (doctor == null) return;

                String consultorio = (doctor.consultorio != null) ? doctor.consultorio : "No asignado";
                ArrayAdapter<String> adapterConsultorio = new ArrayAdapter<>(AgendarCitaActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        new String[]{consultorio});
                spinnerConsultorio.setAdapter(adapterConsultorio);

                List<String> horariosDisponibles = new ArrayList<>();
                if (doctor.dias != null && doctor.horarios != null) {
                    for (int i = 0; i < doctor.dias.size() && i < doctor.horarios.size(); i++) {
                        String dia = doctor.dias.get(i);
                        if (dia != null && dia.equalsIgnoreCase(diaSeleccionado)) {
                            horariosDisponibles.add(doctor.horarios.get(i));
                        }
                    }
                }

                if (horariosDisponibles.isEmpty() && doctor.horarios != null) {
                    horariosDisponibles.addAll(doctor.horarios);
                }

                if (horariosDisponibles.isEmpty()) {
                    horariosDisponibles.add("No hay horarios disponibles");
                }

                ArrayAdapter<String> adapterHorarios = new ArrayAdapter<>(AgendarCitaActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, horariosDisponibles);
                spinnerHorario.setAdapter(adapterHorarios);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        checkboxNotificarCorreo.setOnCheckedChangeListener((buttonView, isChecked) ->
                editTextCorreo.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        btnConfirmarCita.setOnClickListener(v -> {
            String doctor = (String) spinnerDoctor.getSelectedItem();
            String consultorio = (String) spinnerConsultorio.getSelectedItem();
            String horario = (String) spinnerHorario.getSelectedItem();

            if (doctor == null || consultorio == null || horario == null ||
                    doctor.equals("No hay doctores disponibles") ||
                    horario.equals("No hay horarios disponibles")) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String resumen = "Cita agendada:\n" +
                    "Día: " + diaSeleccionado + "\n" +
                    "Doctor: " + doctor + "\n" +
                    "Consultorio: " + consultorio + "\n" +
                    "Horario: " + horario + "\n" +
                    "Usuario: " + usuarioActual;

            if (checkboxNotificarCorreo.isChecked()) {
                String correo = editTextCorreo.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                    Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
                enviarCorreo(correo, "Confirmación de Cita", resumen);
                resumen += "\n\nNotificación enviada a: " + correo;
            }

            txtResumenCita.setText(resumen);

            verificarYGuardarCita(doctor, consultorio, horario, usuarioActual);
        });
    }

    private void enviarCorreo(String correo, String asunto, String mensaje) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + correo));
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void verificarYGuardarCita(String doctor, String consultorio, String horario, String usuario) {
        DatabaseReference citasRef = FirebaseDatabase.getInstance().getReference("citas");

        citasRef.orderByChild("doctor").equalTo(doctor).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean citaDuplicada = false;

                for (DataSnapshot citaSnap : snapshot.getChildren()) {
                    String dia = citaSnap.child("dia").getValue(String.class);
                    String horarioExistente = citaSnap.child("horario").getValue(String.class);

                    if (dia != null && horarioExistente != null &&
                            dia.equalsIgnoreCase(diaSeleccionado) &&
                            horarioExistente.equalsIgnoreCase(horario)) {
                        citaDuplicada = true;
                        break;
                    }
                }

                if (citaDuplicada) {
                    Toast.makeText(AgendarCitaActivity.this,
                            "❌ Ya existe una cita con este doctor, día y horario.", Toast.LENGTH_LONG).show();
                } else {
                    guardarCita(doctor, consultorio, horario, usuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AgendarCitaActivity.this, "Error al verificar citas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCita(String doctor, String consultorio, String horario, String usuario) {
        DatabaseReference citasRef = FirebaseDatabase.getInstance().getReference("citas");
        String id = citasRef.push().getKey();

        Map<String, Object> cita = new HashMap<>();
        cita.put("id", id);
        cita.put("doctor", doctor);
        cita.put("consultorio", consultorio);
        cita.put("dia", diaSeleccionado);
        cita.put("horario", horario);
        cita.put("usuarioCreador", usuario);

        citasRef.child(id).setValue(cita)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Cita guardada correctamente", Toast.LENGTH_SHORT).show();

                    // 📢 NUEVA FUNCION: Notificación al doctor
                    String mensaje = "Tienes una nueva cita el " + diaSeleccionado + " a las " + horario;
                    enviarNotificacionPushAlDoctor(doctor, "Nueva Cita", mensaje);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar cita", Toast.LENGTH_SHORT).show());
    }

    // 📢 NUEVO: Enviar notificación push mediante el token del doctor
    private void enviarNotificacionPushAlDoctor(String doctorNombre, String titulo, String mensaje) {
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("tokensDoctores");

        tokensRef.child(doctorNombre).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.getValue(String.class);

                if (token == null) {
                    Toast.makeText(AgendarCitaActivity.this,
                            "El doctor no tiene token FCM registrado", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("notificacionesPendientes");

                Map<String, Object> notificacion = new HashMap<>();
                notificacion.put("token", token);
                notificacion.put("titulo", titulo);
                notificacion.put("mensaje", mensaje);

                notiRef.push().setValue(notificacion);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static class Doctor {
        public String id;
        public String nombre;
        public String consultorio;
        public List<String> horarios;
        public List<String> dias;

        public Doctor() {}
    }
}
