package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminPanelActivity extends AppCompatActivity {

    private MaterialCardView cardGestionarUsuarios, cardConcesionariasPendientes;
    private TextView tvUsuariosBadge, tvPendientesBadge;
    private MaterialButton btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cardGestionarUsuarios = findViewById(R.id.cardGestionarUsuarios);
        cardConcesionariasPendientes = findViewById(R.id.cardConcesionariasPendientes);
        tvUsuariosBadge = findViewById(R.id.tvUsuariosBadge);
        tvPendientesBadge = findViewById(R.id.tvPendientesBadge);
        btnLogout = findViewById(R.id.btnLogout);

        cardGestionarUsuarios.setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsuariosActivity.class)));

        cardConcesionariasPendientes.setOnClickListener(v ->
                startActivity(new Intent(this, AdminSolicitudesActivity.class)));

        btnLogout.setOnClickListener(v -> cerrarSesion());

        cargarContadores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar contadores cada vez que se vuelve a esta pantalla
        cargarContadores();
    }

    private void cargarContadores() {
        // Total de usuarios
        db.collection("usuarios").get()
                .addOnSuccessListener(query ->
                        tvUsuariosBadge.setText(String.valueOf(query.size())));

        // Concesionarias pendientes
        db.collection("concesionarias")
                .whereEqualTo("estado", "pendiente")
                .get()
                .addOnSuccessListener(query ->
                        tvPendientesBadge.setText(String.valueOf(query.size())));
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Deshabilitar back para que no pueda salir del panel
    }
}