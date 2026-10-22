package com.example.clinicaoasis;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class NewUserActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword, editTextRole;
    private Button btnSave;
    private FirebaseRealtimeHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextRole = findViewById(R.id.editTextRole);
        btnSave = findViewById(R.id.btnSave);

        firebaseHelper = new FirebaseRealtimeHelper();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String role = editTextRole.getText().toString().trim();

                if (!username.isEmpty() && !password.isEmpty() && !role.isEmpty()) {
                    // Generamos un ID único para el usuario (puede ser username si quieres que sea único)
                    String userId = FirebaseDatabase.getInstance().getReference("Users").push().getKey();

                    // Guardamos en Firebase


                    Toast.makeText(NewUserActivity.this, "Usuario guardado en Firebase", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(NewUserActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
