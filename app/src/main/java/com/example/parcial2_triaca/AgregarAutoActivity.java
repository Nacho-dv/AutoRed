package com.example.parcial2_triaca;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AgregarAutoActivity extends AppCompatActivity {

    private TextInputLayout tilMarca, tilModelo, tilAnio, tilStock, tilPrecio, tilTipo, tilDescripcion;
    private TextInputEditText etMarca, etModelo, etAnio, etStock, etPrecio, etDescripcion;
    private AutoCompleteTextView spinnerTipo;
    private MaterialButton btnGuardar;
    private ImageButton btnBack;
    private MaterialCardView cardFoto;
    private ImageView ivFotoPreview;
    private LinearLayout layoutPlaceholder;
    private TextView tvTitulo;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String uid;
    private String autoId = null;
    private Uri fotoUri = null;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_auto);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        // Verificar si es edición
        autoId = getIntent().getStringExtra("autoId");

        tilMarca = findViewById(R.id.tilMarca);
        tilModelo = findViewById(R.id.tilModelo);
        tilAnio = findViewById(R.id.tilAnio);
        tilStock = findViewById(R.id.tilStock);
        tilPrecio = findViewById(R.id.tilPrecio);
        tilTipo = findViewById(R.id.tilTipo);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        etMarca = findViewById(R.id.etMarca);
        etModelo = findViewById(R.id.etModelo);
        etAnio = findViewById(R.id.etAnio);
        etStock = findViewById(R.id.etStock);
        etPrecio = findViewById(R.id.etPrecio);
        etDescripcion = findViewById(R.id.etDescripcion);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnBack = findViewById(R.id.btnBack);
        cardFoto = findViewById(R.id.cardFoto);
        ivFotoPreview = findViewById(R.id.ivFotoPreview);
        layoutPlaceholder = findViewById(R.id.layoutPlaceholder);
        tvTitulo = findViewById(R.id.tvTitulo);

        // Configurar spinner de tipos
        List<String> tipos = Arrays.asList("Sedán", "SUV", "Pick-up", "Hatchback", "Coupé", "Otro");
        ArrayAdapter<String> tiposAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos);
        spinnerTipo.setAdapter(tiposAdapter);

        // Launcher para elegir foto de galería
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoUri = uri;
                        ivFotoPreview.setImageURI(uri);
                        ivFotoPreview.setVisibility(View.VISIBLE);
                        layoutPlaceholder.setVisibility(View.GONE);
                    }
                }
        );

        cardFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnBack.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarAuto());

        // Si es edición, cargar datos existentes
        if (autoId != null) {
            tvTitulo.setText(getString(R.string.title_editar_auto));
            cargarDatosAuto();
        }
    }

    private void cargarDatosAuto() {
        db.collection("concesionarias").document(uid)
                .collection("autos").document(autoId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etMarca.setText(document.getString("marca"));
                        etModelo.setText(document.getString("modelo"));
                        etAnio.setText(document.getString("anio"));
                        etPrecio.setText(document.getString("precio"));
                        etStock.setText(document.getString("stock"));
                        etDescripcion.setText(document.getString("descripcion"));
                        if (document.getString("tipo") != null) {
                            spinnerTipo.setText(document.getString("tipo"), false);
                        }
                    }
                });
    }

    private void guardarAuto() {
        String marca = etMarca.getText() != null ? etMarca.getText().toString().trim() : "";
        String modelo = etModelo.getText() != null ? etModelo.getText().toString().trim() : "";
        String anio = etAnio.getText() != null ? etAnio.getText().toString().trim() : "";
        String precio = etPrecio.getText() != null ? etPrecio.getText().toString().trim() : "";
        String stock = etStock.getText() != null ? etStock.getText().toString().trim() : "";
        String tipo = spinnerTipo.getText() != null ? spinnerTipo.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        // Limpiar errores
        tilMarca.setError(null);
        tilModelo.setError(null);
        tilAnio.setError(null);
        tilPrecio.setError(null);
        tilStock.setError(null);

        // Validaciones
        if (TextUtils.isEmpty(marca)) { tilMarca.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(modelo)) { tilModelo.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(anio)) { tilAnio.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(precio)) { tilPrecio.setError(getString(R.string.error_campo_vacio)); return; }
        if (TextUtils.isEmpty(stock)) { tilStock.setError(getString(R.string.error_campo_vacio)); return; }

        btnGuardar.setEnabled(false);

        if (fotoUri != null) {
            subirFotoYGuardar(marca, modelo, anio, precio, stock, tipo, descripcion);
        } else {
            guardarEnFirestore(marca, modelo, anio, precio, stock, tipo, descripcion, null);
        }
    }

    private void subirFotoYGuardar(String marca, String modelo, String anio,
                                   String precio, String stock, String tipo, String descripcion) {
        String nombreArchivo = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child("autos/" + uid + "/" + nombreArchivo);

        ref.putFile(fotoUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                guardarEnFirestore(marca, modelo, anio, precio, stock, tipo, descripcion, uri.toString())
                        )
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                });
    }

    private void guardarEnFirestore(String marca, String modelo, String anio,
                                    String precio, String stock, String tipo,
                                    String descripcion, String fotoUrl) {
        Map<String, Object> auto = new HashMap<>();
        auto.put("marca", marca);
        auto.put("modelo", modelo);
        auto.put("anio", anio);
        auto.put("precio", precio);
        auto.put("stock", stock);
        auto.put("tipo", tipo);
        auto.put("descripcion", descripcion);
        if (fotoUrl != null) auto.put("fotoUrl", fotoUrl);

        if (autoId != null) {
            // Editar auto existente
            db.collection("concesionarias").document(uid)
                    .collection("autos").document(autoId)
                    .update(auto)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Auto actualizado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                        btnGuardar.setEnabled(true);
                    });
        } else {
            // Agregar auto nuevo
            db.collection("concesionarias").document(uid)
                    .collection("autos")
                    .add(auto)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Auto agregado", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show();
                        btnGuardar.setEnabled(true);
                    });
        }
    }
}