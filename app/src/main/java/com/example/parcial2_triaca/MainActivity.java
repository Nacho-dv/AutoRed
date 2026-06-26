package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // No hay sesión activa, ir al login
            irA(LoginActivity.class);
        } else {
            // Hay sesión activa, consultar rol en Firestore
            db.collection("usuarios")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String rol = document.getString("rol");
                            String estado = document.getString("estado");

                            if (rol == null) {
                                irA(LoginActivity.class);
                                return;
                            }

                            switch (rol) {
                                case "admin":
                                    irA(AdminPanelActivity.class);
                                    break;
                                case "concesionaria":
                                    if ("pendiente".equals(estado)) {
                                        irA(PendingActivity.class);
                                    } else {
                                        irA(ConcesionariaPanelActivity.class);
                                    }
                                    break;
                                case "usuario":
                                    irA(UsuarioHomeActivity.class);
                                    break;
                                default:
                                    irA(LoginActivity.class);
                                    break;
                            }
                        } else {
                            irA(LoginActivity.class);
                        }
                    })
                    .addOnFailureListener(e -> irA(LoginActivity.class));
        }
    }

    private void irA(Class<?> destino) {
        Intent intent = new Intent(this, destino);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}