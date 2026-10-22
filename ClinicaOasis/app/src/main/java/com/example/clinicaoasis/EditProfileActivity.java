package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtLoginUsername, edtLoginPassword;
    private EditText edtNewPassword, edtConfirmPassword;
    private ImageView profileImageView;
    private Button btnValidate, btnSave, btnChangeImage;
    private LinearLayout imageSelectionLayout, loginLayout, updateLayout;
    private String currentProfileImage = "perfil_default";
    private String username;
    private String userKey; // ID en Firebase
    private CheckBox showPasswordCheckBox, showLoginPasswordCheckBox;

    private DatabaseReference dbRef;

    @SuppressLint({"MissingInflatedId", "Range"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inicializar Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        // Layouts
        loginLayout = findViewById(R.id.loginLayout);
        updateLayout = findViewById(R.id.userUpdateLayout);

        // Mostrar primero login
        loginLayout.setVisibility(View.VISIBLE);
        updateLayout.setVisibility(View.GONE);

        // Inputs para login manual
        edtLoginUsername = findViewById(R.id.currentUsernameEditText);
        edtLoginPassword = findViewById(R.id.currentPasswordEditText);
        btnValidate = findViewById(R.id.validateUserButton);

        // Inputs para actualización
        edtNewPassword = findViewById(R.id.newPasswordEditText);
        edtConfirmPassword = findViewById(R.id.confirmPasswordEditText);
        profileImageView = findViewById(R.id.profileImageView);
        btnChangeImage = findViewById(R.id.changeImageButton);
        imageSelectionLayout = findViewById(R.id.imageSelectionlayout);
        btnSave = findViewById(R.id.saveButton);

        // Checkbox para mostrar/ocultar contraseñas
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox);
        showLoginPasswordCheckBox = findViewById(R.id.showLoginPasswordCheckBox);

        // Recuperar usuario desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", null);
        if (savedUsername != null) {
            username = savedUsername;
            loadUserData(username);
        }

        // Validar usuario manualmente
        btnValidate.setOnClickListener(v -> {
            String enteredUsername = edtLoginUsername.getText().toString().trim();
            String enteredPassword = edtLoginPassword.getText().toString().trim();

            dbRef.orderByChild("username").equalTo(enteredUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String storedPass = userSnap.child("password").getValue(String.class);
                                    if (storedPass != null && storedPass.equals(enteredPassword)) {
                                        userKey = userSnap.getKey();
                                        username = enteredUsername;
                                        currentProfileImage = userSnap.child("image").getValue(String.class);
                                        switchToUpdateMode();
                                        return;
                                    }
                                }
                                Toast.makeText(EditProfileActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(EditProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Mostrar / ocultar selector de imágenes
        btnChangeImage.setOnClickListener(v -> {
            int visibility = imageSelectionLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            imageSelectionLayout.setVisibility(visibility);
        });

        // Opciones de imagen
        int[] imageIds = {
                R.id.imageOption1, R.id.imageOption2, R.id.imageOption3,
                R.id.imageOption4, R.id.imageOption5, R.id.imageOption6,
                R.id.imageOption7, R.id.imageOption8, R.id.imageOption9,
                R.id.imageOption10, R.id.imageOption11
        };
        String[] imageNames = {
                "perfil1", "perfil2", "perfil3", "perfil4", "perfil5",
                "perfil6", "perfil7", "perfil8", "perfil9", "perfil10", "perfil11"
        };

        for (int i = 0; i < imageIds.length; i++) {
            int index = i;
            ImageView option = findViewById(imageIds[i]);
            option.setOnClickListener(v -> {
                currentProfileImage = imageNames[index];
                profileImageView.setImageResource(
                        getResources().getIdentifier(currentProfileImage, "drawable", getPackageName())
                );
                imageSelectionLayout.setVisibility(View.GONE);
            });
        }

        // Guardar cambios en Firebase
        btnSave.setOnClickListener(v -> {
            String newPass = edtNewPassword.getText().toString().trim();
            String confirmPass = edtConfirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Rellena ambos campos de contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userKey != null) {
                dbRef.child(userKey).child("password").setValue(newPass);
                dbRef.child(userKey).child("image").setValue(currentProfileImage)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(this, "Error: usuario no cargado", Toast.LENGTH_SHORT).show();
            }
        });

        // Mostrar/ocultar contraseña en login
        showLoginPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtLoginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                edtLoginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        // Mostrar/ocultar contraseña en actualización
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                edtConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    // Cargar datos de usuario directamente si estaba guardado en SharedPreferences
    private void loadUserData(String username) {
        dbRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                userKey = userSnap.getKey();
                                currentProfileImage = userSnap.child("image").getValue(String.class);
                                switchToUpdateMode();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(EditProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void switchToUpdateMode() {
        loginLayout.setVisibility(View.GONE);
        updateLayout.setVisibility(View.VISIBLE);
        if (currentProfileImage != null) {
            profileImageView.setImageResource(
                    getResources().getIdentifier(currentProfileImage, "drawable", getPackageName())
            );
        }
    }
}
