package com.example.clinicaoasis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrudUsuariosActivity extends AppCompatActivity {

    // Campos de la interfaz
    EditText editTextAlias, editTextEmail, editTextPassword;
    Spinner spinnerRol;
    Button btnAgregar, btnActualizar, btnEliminar;
    ListView listViewUsuarios;
    ImageView imageViewPreview; // vista previa de perfil seleccionada

    // Firebase
    DatabaseReference usuariosRef;

    // Listas para mostrar los usuarios (cada Map representa un usuario)
    ArrayList<Map<String, String>> listaUsuarios = new ArrayList<>();
    UsuarioAdapter adaptadorLista;

    // ID del usuario seleccionado (clave en la DB: puede ser push key o username)
    String idSeleccionado = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_usuarios);

        // Referencias UI
        editTextAlias = findViewById(R.id.editTextAlias);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        spinnerRol = findViewById(R.id.spinnerRol);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);
        listViewUsuarios = findViewById(R.id.listViewUsuarios);
        imageViewPreview = findViewById(R.id.imageViewPreview);

        // Configurar spinner de roles
        String[] roles = {"usuario", "doctor", "admin"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapterSpinner);

        // Referencia al nodo "Usuarios"
        usuariosRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Configurar adaptador personalizado
        adaptadorLista = new UsuarioAdapter();
        listViewUsuarios.setAdapter(adaptadorLista);

        // Cargar usuarios
        cargarUsuarios();

        // Selección de usuario de la lista
        listViewUsuarios.setOnItemClickListener((adapterView, view, position, id) -> {
            Map<String, String> usuario = listaUsuarios.get(position);
            idSeleccionado = usuario.get("id"); // key en Firebase (puede ser push key o username)
            editTextAlias.setText(nonNull(usuario.get("alias")));
            editTextEmail.setText(nonNull(usuario.get("email")));
            editTextPassword.setText(nonNull(usuario.get("password")));

            // Seleccionar rol en spinner (si no existe, selecciona 0)
            String rol = usuario.get("rol");
            if (rol != null) {
                int pos = adapterSpinner.getPosition(rol);
                spinnerRol.setSelection(pos >= 0 ? pos : 0);
            } else {
                spinnerRol.setSelection(0);
            }

            // Cargar imagen preview
            String imageName = usuario.get("imageName");
            int imageResId = getResources().getIdentifier(
                    imageName != null ? imageName : "perfil_default",
                    "drawable",
                    getPackageName());
            imageViewPreview.setImageResource(imageResId != 0 ? imageResId : R.drawable.perfil_default);
        });

        // Eventos de botones
        btnAgregar.setOnClickListener(v -> agregarUsuario());
        btnActualizar.setOnClickListener(v -> actualizarUsuario());
        btnEliminar.setOnClickListener(v -> eliminarUsuario());
    }

    /** Carga todos los usuarios de Firebase */
    private void cargarUsuarios() {
        usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaUsuarios.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Map<String, String> usuario = new HashMap<>();

                    // Guardamos la key real como idSeleccionado potencial
                    String key = ds.getKey();
                    usuario.put("id", key);

                    // Intentamos mapear varios esquemas posibles: alias/username/email/imageName/rol/password
                    // ds.child(...).getValue(String.class) puede devolver null si no existe la propiedad
                    String alias = ds.child("alias").getValue(String.class);
                    String username = ds.child("username").getValue(String.class); // otro posible campo
                    String email = ds.child("email").getValue(String.class);
                    String password = ds.child("password").getValue(String.class);
                    String rol = ds.child("rol").getValue(String.class);
                    String imageName = ds.child("imageName").getValue(String.class);

                    // Si no hay alias pero sí username, usar username como alias mostrado
                    if (alias == null && username != null) alias = username;

                    usuario.put("alias", alias != null ? alias : "Sin nombre");
                    usuario.put("username", username != null ? username : "");
                    usuario.put("email", email != null ? email : "");
                    usuario.put("password", password != null ? password : "");
                    usuario.put("rol", rol != null ? rol : "usuario");
                    usuario.put("imageName", imageName != null ? imageName : "perfil_default");

                    listaUsuarios.add(usuario);
                }

                adaptadorLista.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CrudUsuariosActivity.this,
                        "Error al cargar usuarios: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Agregar nuevo usuario: guarda username = alias para evitar inconsistencias */
    private void agregarUsuario() {
        String alias = editTextAlias.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String rol = spinnerRol.getSelectedItem().toString();

        if (alias.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar id (push key) para mantener compatibilidad con datos existentes
        String id = usuariosRef.push().getKey();

        Map<String, Object> nuevoUsuario = new HashMap<>();
        nuevoUsuario.put("alias", alias);
        nuevoUsuario.put("username", alias); // guardamos username para búsquedas consistentes
        nuevoUsuario.put("email", email);
        nuevoUsuario.put("password", password);
        nuevoUsuario.put("rol", rol);
        nuevoUsuario.put("imageName", "perfil_default"); // puedes cambiar o exponer selector

        usuariosRef.child(id).setValue(nuevoUsuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario agregado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Actualizar usuario seleccionado */
    private void actualizarUsuario() {
        if (idSeleccionado == null || idSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un usuario de la lista", Toast.LENGTH_SHORT).show();
            return;
        }

        String alias = editTextAlias.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String rol = spinnerRol.getSelectedItem().toString();

        if (alias.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("alias", alias);
        datosActualizados.put("username", alias); // mantener username sincronizado con alias
        datosActualizados.put("email", email);
        datosActualizados.put("password", password);
        datosActualizados.put("rol", rol);
        // imageName no se modifica aquí (si quieres un selector, lo agregamos)

        usuariosRef.child(idSeleccionado).updateChildren(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Eliminar usuario seleccionado */
    private void eliminarUsuario() {
        if (idSeleccionado == null || idSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un usuario para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        usuariosRef.child(idSeleccionado).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /** Limpia los campos del formulario */
    private void limpiarCampos() {
        editTextAlias.setText("");
        editTextEmail.setText("");
        editTextPassword.setText("");
        spinnerRol.setSelection(0);
        imageViewPreview.setImageResource(R.drawable.perfil_default);
        idSeleccionado = "";
    }

    private String nonNull(String s) {
        return s != null ? s : "";
    }

    /**
     * Adaptador interno para mostrar imagen + alias (rol) en la lista.
     * Usa listaUsuarios (ArrayList<Map<String,String>>).
     */
    private class UsuarioAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listaUsuarios.size();
        }

        @Override
        public Object getItem(int position) {
            return listaUsuarios.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(CrudUsuariosActivity.this);
                row = inflater.inflate(R.layout.row_usuario, parent, false);
            }

            ImageView img = row.findViewById(R.id.rowImage);
            TextView tvAlias = row.findViewById(R.id.rowAlias);
            TextView tvEmail = row.findViewById(R.id.rowEmail);

            Map<String, String> usuario = listaUsuarios.get(position);
            String alias = usuario.get("alias");
            String rol = usuario.get("rol");
            String email = usuario.get("email");
            String imageName = usuario.get("imageName");

            // Mostrar alias (con rol)
            tvAlias.setText(alias + " (" + (rol != null ? rol : "usuario") + ")");
            tvEmail.setText(email != null && !email.isEmpty() ? email : "");

            // Cargar drawable por nombre (si existe)
            int res = getResources().getIdentifier(
                    imageName != null ? imageName : "perfil_default",
                    "drawable",
                    getPackageName());
            img.setImageResource(res != 0 ? res : R.drawable.perfil_default);

            return row;
        }
    }
}
