package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoToRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> intentarLogin());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, ElegirCuentaActivity.class));
        });
    }

    private void intentarLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Limpiar errores anteriores
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validaciones
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

        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    String rol = document.getString("rol");
                                    String estado = document.getString("estado");
                                    redirigirSegunRol(rol, estado);
                                } else {
                                    Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                                    btnLogin.setEnabled(true);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                                btnLogin.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    tilPassword.setError("Email o contraseña incorrectos");
                    btnLogin.setEnabled(true);
                });
    }

    private void redirigirSegunRol(String rol, String estado) {
        Intent intent;
        if (rol == null) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            switch (rol) {
                case "admin":
                    intent = new Intent(this, AdminPanelActivity.class);
                    break;
                case "concesionaria":
                    if ("pendiente".equals(estado)) {
                        intent = new Intent(this, PendingActivity.class);
                    } else {
                        intent = new Intent(this, ConcesionariaPanelActivity.class);
                    }
                    break;
                case "usuario":
                    intent = new Intent(this, UsuarioHomeActivity.class);
                    break;
                default:
                    intent = new Intent(this, LoginActivity.class);
                    break;
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}