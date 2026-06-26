package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroConcesionariaActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputLayout tilNombreConcesionaria, tilDireccion, tilTelefono, tilEmailContacto, tilDescripcion;
    private TextInputEditText etNombre, etEmail, etPassword, etConfirmPassword;
    private TextInputEditText etNombreConcesionaria, etDireccion, etTelefono, etEmailContacto, etDescripcion;
    private MaterialButton btnEnviarSolicitud;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_concesionaria);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Info personal
        tilNombre = findViewById(R.id.tilNombre);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Info comercial
        tilNombreConcesionaria = findViewById(R.id.tilNombreConcesionaria);
        tilDireccion = findViewById(R.id.tilDireccion);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilEmailContacto = findViewById(R.id.tilEmailContacto);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        etNombreConcesionaria = findViewById(R.id.etNombreConcesionaria);
        etDireccion = findViewById(R.id.etDireccion);
        etTelefono = findViewById(R.id.etTelefono);
        etEmailContacto = findViewById(R.id.etEmailContacto);
        etDescripcion = findViewById(R.id.etDescripcion);

        btnEnviarSolicitud = findViewById(R.id.btnEnviarSolicitud);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnEnviarSolicitud.setOnClickListener(v -> intentarRegistro());
    }

    private void intentarRegistro() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
        String nombreConcesionaria = etNombreConcesionaria.getText() != null ? etNombreConcesionaria.getText().toString().trim() : "";
        String direccion = etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        String emailContacto = etEmailContacto.getText() != null ? etEmailContacto.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        // Limpiar errores
        tilNombre.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilNombreConcesionaria.setError(null);
        tilDireccion.setError(null);
        tilTelefono.setError(null);
        tilEmailContacto.setError(null);

        // Validaciones info personal
        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalido));
            return;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_passwords_no_coinciden));
            return;
        }

        // Validaciones info comercial
        if (TextUtils.isEmpty(nombreConcesionaria)) {
            tilNombreConcesionaria.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (TextUtils.isEmpty(direccion)) {
            tilDireccion.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (TextUtils.isEmpty(telefono)) {
            tilTelefono.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (TextUtils.isEmpty(emailContacto)) {
            tilEmailContacto.setError(getString(R.string.error_campo_vacio));
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContacto).matches()) {
            tilEmailContacto.setError(getString(R.string.error_email_invalido));
            return;
        }

        btnEnviarSolicitud.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();

                    // Documento del usuario
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("nombre", nombre);
                    usuario.put("email", email);
                    usuario.put("rol", "concesionaria");
                    usuario.put("estado", "pendiente");

                    // Documento de la concesionaria
                    Map<String, Object> concesionaria = new HashMap<>();
                    concesionaria.put("usuarioId", uid);
                    concesionaria.put("nombre", nombreConcesionaria);
                    concesionaria.put("direccion", direccion);
                    concesionaria.put("telefono", telefono);
                    concesionaria.put("emailContacto", emailContacto);
                    concesionaria.put("descripcion", descripcion);
                    concesionaria.put("estado", "pendiente");

                    // Guardar usuario en Firestore
                    db.collection("usuarios").document(uid).set(usuario)
                            .addOnSuccessListener(unused -> {
                                // Guardar concesionaria en Firestore
                                db.collection("concesionarias").document(uid).set(concesionaria)
                                        .addOnSuccessListener(unused2 -> {
                                            Intent intent = new Intent(this, PendingActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                                            btnEnviarSolicitud.setEnabled(true);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                                btnEnviarSolicitud.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnEnviarSolicitud.setEnabled(true);
                });
    }
}