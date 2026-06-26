package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetalleConcesionariaActivity extends AppCompatActivity {

    private TextView tvTituloBar, tvNombre, tvDireccion, tvTelefono, tvEmail, tvDescripcion;
    private ImageButton btnBack;
    private RecyclerView rvAutosDetalle;

    private FirebaseFirestore db;
    private String concesionariaId;

    private List<Map<String, Object>> listaAutos = new ArrayList<>();
    private AutosDetalleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_concesionaria);

        db = FirebaseFirestore.getInstance();

        // Recibir datos del Intent
        concesionariaId = getIntent().getStringExtra("concesionariaId");
        String nombre = getIntent().getStringExtra("concesionariaNombre");

        tvTituloBar = findViewById(R.id.tvTituloBar);
        tvNombre = findViewById(R.id.tvNombre);
        tvDireccion = findViewById(R.id.tvDireccion);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvEmail = findViewById(R.id.tvEmail);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        btnBack = findViewById(R.id.btnBack);
        rvAutosDetalle = findViewById(R.id.rvAutosDetalle);

        if (nombre != null) tvTituloBar.setText(nombre);

        btnBack.setOnClickListener(v -> finish());

        adapter = new AutosDetalleAdapter();
        rvAutosDetalle.setLayoutManager(new LinearLayoutManager(this));
        rvAutosDetalle.setAdapter(adapter);

        cargarDatosConcesionaria();
        cargarAutos();
    }

    private void cargarDatosConcesionaria() {
        db.collection("concesionarias").document(concesionariaId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        tvNombre.setText(document.getString("nombre"));
                        tvDireccion.setText(document.getString("direccion"));
                        tvTelefono.setText(document.getString("telefono"));
                        tvEmail.setText(document.getString("emailContacto"));
                        tvDescripcion.setText(document.getString("descripcion"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    private void cargarAutos() {
        db.collection("concesionarias").document(concesionariaId)
                .collection("autos").get()
                .addOnSuccessListener(query -> {
                    listaAutos.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        listaAutos.add(doc.getData());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    // Adapter interno
    class AutosDetalleAdapter extends RecyclerView.Adapter<AutosDetalleAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAutoNombre, tvAutoTipo, tvAutoPrecio, tvAutoAnio, tvAutoStock, tvAutoDescripcion;
            ImageView ivFotoAuto;

            ViewHolder(View itemView) {
                super(itemView);
                tvAutoNombre = itemView.findViewById(R.id.tvAutoNombre);
                tvAutoTipo = itemView.findViewById(R.id.tvAutoTipo);
                tvAutoPrecio = itemView.findViewById(R.id.tvAutoPrecio);
                tvAutoAnio = itemView.findViewById(R.id.tvAutoAnio);
                tvAutoStock = itemView.findViewById(R.id.tvAutoStock);
                tvAutoDescripcion = itemView.findViewById(R.id.tvAutoDescripcion);
                ivFotoAuto = itemView.findViewById(R.id.ivFotoAuto);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_auto_detalle, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, Object> auto = listaAutos.get(position);

            String marca = auto.get("marca") != null ? (String) auto.get("marca") : "";
            String modelo = auto.get("modelo") != null ? (String) auto.get("modelo") : "";
            String tipo = auto.get("tipo") != null ? (String) auto.get("tipo") : "";
            String anio = auto.get("anio") != null ? auto.get("anio").toString() : "";
            String precio = auto.get("precio") != null ? "$ " + auto.get("precio").toString() : "$ 0";
            String stock = auto.get("stock") != null ? auto.get("stock").toString() + " disponibles" : "0 disponibles";
            String descripcion = auto.get("descripcion") != null ? (String) auto.get("descripcion") : "";
            String fotoUrl = auto.get("fotoUrl") != null ? (String) auto.get("fotoUrl") : null;

            holder.tvAutoNombre.setText(marca + " " + modelo);
            holder.tvAutoTipo.setText(tipo + " • " + anio);
            holder.tvAutoPrecio.setText(precio);
            holder.tvAutoAnio.setText(anio);
            holder.tvAutoStock.setText(stock);
            holder.tvAutoDescripcion.setText(descripcion);

            // Cargar foto si existe
            if (fotoUrl != null) {
                Glide.with(holder.ivFotoAuto.getContext()).load(fotoUrl).into(holder.ivFotoAuto);
            }
        }

        @Override
        public int getItemCount() {
            return listaAutos.size();
        }
    }
}