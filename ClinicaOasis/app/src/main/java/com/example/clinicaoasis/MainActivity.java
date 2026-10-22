package com.example.clinicaoasis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageView logoImage;
    private TextView welcomeText;
    private Button btnWelcome, btnRetry;
    private LinearLayout loadingLayout;

    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private DatabaseReference database, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Inicializa Firebase
        FirebaseApp.initializeApp(this);

        logoImage = findViewById(R.id.logoApp);
        welcomeText = findViewById(R.id.welcomeText);
        btnWelcome = findViewById(R.id.btnWelcome);
        btnRetry = findViewById(R.id.btnRetry);
        loadingLayout = findViewById(R.id.loadingLayout);

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        btnWelcome.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        // Inicializar Firebase Auth y DB
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(
                "https://clinica-oasis-2025-default-rtdb.firebaseio.com"
        );
        database = firebaseDatabase.getReference();
        usersRef = database.child("Usuarios");

        // 🔹 Comprobar sesión guardada antes de cualquier cosa
        String savedUsername = sharedPreferences.getString("username", null);
        if (savedUsername != null) {
            Intent intent;
            if (savedUsername.equals("admin")) {
                intent = new Intent(MainActivity.this, AdminActivity.class);
            } else {
                intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("username", savedUsername);
            }
            startActivity(intent);
            finish();
            return;
        }

        // Intento de conexión inicial
        conectarFirebase();


        // Botón Reintentar
        btnRetry.setOnClickListener(v -> {
            btnRetry.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
            conectarFirebase();
        });

        // Botón Iniciar manual
        btnWelcome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void conectarFirebase() {
        probarEscritura();
    }

    private void probarEscritura() {
        DatabaseReference ref = database.child("test");
        ref.setValue("¡Hola mundo!")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this,
                            "✅ Conectado ", Toast.LENGTH_SHORT).show();
                    btnWelcome.setVisibility(View.VISIBLE);
                    loadingLayout.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this,
                            "❌ Error al Conectar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadingLayout.setVisibility(View.GONE);
                    btnRetry.setVisibility(View.VISIBLE);
                });
    }

}
