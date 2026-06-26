package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UsuarioHomeActivity extends AppCompatActivity {

    private RecyclerView rvConcesionarias;
    private TextInputEditText etBuscar;
    private ImageButton btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Map<String, Object>> listaConcesionarias = new ArrayList<>();
    private List<String> listaIds = new ArrayList<>();
    private List<Map<String, Object>> listaFiltrada = new ArrayList<>();
    private List<String> listaIdsFiltrada = new ArrayList<>();
    private ConcesionariasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvConcesionarias = findViewById(R.id.rvConcesionarias);
        etBuscar = findViewById(R.id.etBuscar);
        btnLogout = findViewById(R.id.btnLogout);

        adapter = new ConcesionariasAdapter();
        rvConcesionarias.setLayoutManager(new LinearLayoutManager(this));
        rvConcesionarias.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> cerrarSesion());

        // Búsqueda en tiempo real
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cargarConcesionarias();
    }

    private void cargarConcesionarias() {
        db.collection("concesionarias")
                .whereEqualTo("estado", "activo")
                .get()
                .addOnSuccessListener(query -> {
                    listaConcesionarias.clear();
                    listaIds.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        listaConcesionarias.add(doc.getData());
                        listaIds.add(doc.getId());
                    }
                    listaFiltrada.clear();
                    listaFiltrada.addAll(listaConcesionarias);
                    listaIdsFiltrada.clear();
                    listaIdsFiltrada.addAll(listaIds);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    private void filtrar(String texto) {
        listaFiltrada.clear();
        listaIdsFiltrada.clear();
        String textoBajo = texto.toLowerCase().trim();

        for (int i = 0; i < listaConcesionarias.size(); i++) {
            Map<String, Object> c = listaConcesionarias.get(i);
            String nombre = c.get("nombre") != null ? ((String) c.get("nombre")).toLowerCase() : "";
            String direccion = c.get("direccion") != null ? ((String) c.get("direccion")).toLowerCase() : "";

            if (nombre.contains(textoBajo) || direccion.contains(textoBajo)) {
                listaFiltrada.add(c);
                listaIdsFiltrada.add(listaIds.get(i));
            }
        }
        adapter.notifyDataSetChanged();
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
        // Deshabilitar back
    }

    // Adapter interno
    class ConcesionariasAdapter extends RecyclerView.Adapter<ConcesionariasAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvDireccion, tvTelefono;
            MaterialButton btnVerAutos;

            ViewHolder(View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombre);
                tvDireccion = itemView.findViewById(R.id.tvDireccion);
                tvTelefono = itemView.findViewById(R.id.tvTelefono);
                btnVerAutos = itemView.findViewById(R.id.btnVerAutos);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_concesionaria, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, Object> concesionaria = listaFiltrada.get(position);
            String id = listaIdsFiltrada.get(position);

            String nombre = concesionaria.get("nombre") != null ? (String) concesionaria.get("nombre") : "Sin nombre";
            String direccion = concesionaria.get("direccion") != null ? (String) concesionaria.get("direccion") : "Sin dirección";
            String telefono = concesionaria.get("telefono") != null ? (String) concesionaria.get("telefono") : "Sin teléfono";

            holder.tvNombre.setText(nombre);
            holder.tvDireccion.setText(direccion);
            holder.tvTelefono.setText(telefono);

            holder.btnVerAutos.setOnClickListener(v -> {
                Intent intent = new Intent(UsuarioHomeActivity.this, DetalleConcesionariaActivity.class);
                intent.putExtra("concesionariaId", id);
                intent.putExtra("concesionariaNombre", nombre);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return listaFiltrada.size();
        }
    }
}