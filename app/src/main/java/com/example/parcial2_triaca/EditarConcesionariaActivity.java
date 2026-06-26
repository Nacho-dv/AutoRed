package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
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

public class EditarConcesionariaActivity extends AppCompatActivity {

    private TextInputLayout tilNombreConcesionaria, tilDireccion, tilTelefono, tilEmailContacto, tilDescripcion;
    private TextInputEditText etNombreConcesionaria, etDireccion, etTelefono, etEmailContacto, etDescripcion;
    private MaterialButton btnGuardar;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_concesionaria);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = mAuth.getCurrentUser().getUid();

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
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarCambios());

        cargarDatos();
    }

    private void cargarDatos() {
        db.collection("concesionarias").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etNombreConcesionaria.setText(document.getString("nombre"));
                        etDireccion.setText(document.getString("direccion"));
                        etTelefono.setText(document.getString("telefono"));
                        etEmailContacto.setText(document.getString("emailContacto"));
                        etDescripcion.setText(document.getString("descripcion"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    private void guardarCambios() {
        String nombre = etNombreConcesionaria.getText() != null ? etNombreConcesionaria.getText().toString().trim() : "";
        String direccion = etDireccion.getText() != null ? etDireccion.getText().toString().trim() : "";
        String telefono = etTelefono.getText() != null ? etTelefono.getText().toString().trim() : "";
        String emailContacto = etEmailContacto.getText() != null ? etEmailContacto.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        tilNombreConcesionaria.setError(null);
        tilDireccion.setError(null);
        tilTelefono.setError(null);
        tilEmailContacto.setError(null);

        if (TextUtils.isEmpty(nombre)) { tilNombreConcesionaria.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(direccion)) { tilDireccion.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(telefono)) { tilTelefono.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(emailContacto)) { tilEmailContacto.setError(getString(R.string.error_campo_vacio)); return; }

        btnGuardar.setEnabled(false);

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("direccion", direccion);
        datos.put("telefono", telefono);
        datos.put("emailContacto", emailContacto);
        datos.put("descripcion", descripcion);

        db.collection("concesionarias").document(uid)
                .update(datos)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                });
    }
}