package com.example.clinicaoasis;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextAlias, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonAccept, buttonSelectImage;
    private CheckBox checkBoxShowPassword;
    private ImageView imageViewProfile;
    private HorizontalScrollView scrollImageSelector;

    private String selectedImageName = null;

    private DatabaseReference usersRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextAlias = findViewById(R.id.editTextAlias);
        editTextEmail = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonAccept = findViewById(R.id.buttonAccept);
        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        imageViewProfile = findViewById(R.id.imageProfile);
        scrollImageSelector = findViewById(R.id.scrollImageSelector);

        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        scrollImageSelector.setVisibility(HorizontalScrollView.GONE);

        // Inicializar imágenes del selector
        int[] perfilIds = {
                R.id.option1, R.id.option2, R.id.option3, R.id.option4, R.id.option5,
                R.id.option6, R.id.option7, R.id.option8, R.id.option9, R.id.option10, R.id.option11
        };
        String[] perfilNames = {
                "perfil1","perfil2","perfil3","perfil4","perfil5",
                "perfil6","perfil7","perfil8","perfil9","perfil10","perfil11"
        };

        for (int i = 0; i < perfilIds.length; i++) {
            ImageView img = findViewById(perfilIds[i]);
            String nombre = perfilNames[i];
            int resId = getResources().getIdentifier(nombre,"drawable",getPackageName());
            img.setImageResource(resId);
            img.setOnClickListener(v -> {
                imageViewProfile.setImageResource(resId);
                selectedImageName = nombre;
                scrollImageSelector.setVisibility(HorizontalScrollView.GONE);
            });
        }

        buttonSelectImage.setOnClickListener(v -> {
            if(scrollImageSelector.getVisibility() == HorizontalScrollView.GONE){
                scrollImageSelector.setVisibility(HorizontalScrollView.VISIBLE);
            } else {
                scrollImageSelector.setVisibility(HorizontalScrollView.GONE);
            }
        });

        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editTextPassword.setTransformationMethod(isChecked ?
                    HideReturnsTransformationMethod.getInstance() :
                    PasswordTransformationMethod.getInstance());
            editTextConfirmPassword.setTransformationMethod(isChecked ?
                    HideReturnsTransformationMethod.getInstance() :
                    PasswordTransformationMethod.getInstance());
        });

        buttonAccept.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String alias = editTextAlias.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if(alias.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Toast.makeText(this,"Todos los campos son obligatorios",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(confirmPassword)){
            Toast.makeText(this,"Las contraseñas no coinciden",Toast.LENGTH_SHORT).show();
            return;
        }

        if(!esContrasenaSegura(password)){
            Toast.makeText(this,"Contraseña débil: mínimo 8 caracteres, mayúscula, minúscula, número y símbolo",Toast.LENGTH_LONG).show();
            return;
        }

        if(selectedImageName == null){
            Toast.makeText(this,"Selecciona una imagen de perfil",Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar alias en ambos campos alias y username
        Usuario nuevoUsuario = new Usuario(alias, email, password, selectedImageName, "usuario");

        usersRef.child(alias).setValue(nuevoUsuario)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,"Usuario registrado correctamente ✅",Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,"Error al registrar usuario: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }

    private boolean esContrasenaSegura(String password){
        String patron = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[.@#$%^&+=!])(?=\\S+$).{8,}$";
        return password.matches(patron);
    }

    public static class Usuario{
        public String alias;      // <-- se guarda igual que username
        public String email;
        public String password;
        public String imageName;
        public String rol;
        public String username;   // <-- se guarda igual que alias

        public Usuario() {}

        public Usuario(String alias, String email, String password, String imageName, String rol){
            this.alias = alias;
            this.username = alias;  // <-- clave para que se vea igual
            this.email = email;
            this.password = password;
            this.imageName = imageName;
            this.rol = rol;
        }
    }
}
