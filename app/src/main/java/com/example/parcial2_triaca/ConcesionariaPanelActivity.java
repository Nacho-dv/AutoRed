package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConcesionariaPanelActivity extends AppCompatActivity {

    private TextView tvNombreConcesionaria, tvDatoNombre, tvDatoDireccion, tvDatoTelefono, tvVerTodos;
    private MaterialButton btnEditar;
    private ImageButton btnLogout;
    private FloatingActionButton fabAgregarAuto;
    private RecyclerView rvAutos;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    private List<Map<String, Object>> listaAutos = new ArrayList<>();
    private List<String> listaAutosIds = new ArrayList<>();
    private AutosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concesionaria_panel);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        tvNombreConcesionaria = findViewById(R.id.tvNombreConcesionaria);
        tvDatoNombre = findViewById(R.id.tvDatoNombre);
        tvDatoDireccion = findViewById(R.id.tvDatoDireccion);
        tvDatoTelefono = findViewById(R.id.tvDatoTelefono);
        tvVerTodos = findViewById(R.id.tvVerTodos);
        btnEditar = findViewById(R.id.btnEditar);
        btnEditar.setOnClickListener(v ->
                startActivity(new Intent(this, EditarConcesionariaActivity.class)));

        btnLogout = findViewById(R.id.btnLogout);
        fabAgregarAuto = findViewById(R.id.fabAgregarAuto);
        rvAutos = findViewById(R.id.rvAutos);

        adapter = new AutosAdapter();
        rvAutos.setLayoutManager(new LinearLayoutManager(this));
        rvAutos.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> cerrarSesion());
        fabAgregarAuto.setOnClickListener(v ->
                startActivity(new Intent(this, AgregarAutoActivity.class)));

        cargarDatosConcesionaria();
        cargarAutos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarAutos();
    }

    private void cargarDatosConcesionaria() {
        db.collection("concesionarias").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nombre = document.getString("nombre");
                        String direccion = document.getString("direccion");
                        String telefono = document.getString("telefono");

                        tvNombreConcesionaria.setText(nombre);
                        tvDatoNombre.setText(nombre);
                        tvDatoDireccion.setText(direccion);
                        tvDatoTelefono.setText(telefono);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    private void cargarAutos() {
        db.collection("concesionarias").document(uid)
                .collection("autos").get()
                .addOnSuccessListener(query -> {
                    listaAutos.clear();
                    listaAutosIds.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        listaAutos.add(doc.getData());
                        listaAutosIds.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
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
    class AutosAdapter extends RecyclerView.Adapter<AutosAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAutoNombre, tvAutoAnio, tvAutoPrecio, tvAutoStock;
            ImageButton btnOpciones;

            ViewHolder(View itemView) {
                super(itemView);
                tvAutoNombre = itemView.findViewById(R.id.tvAutoNombre);
                tvAutoAnio = itemView.findViewById(R.id.tvAutoAnio);
                tvAutoPrecio = itemView.findViewById(R.id.tvAutoPrecio);
                tvAutoStock = itemView.findViewById(R.id.tvAutoStock);
                btnOpciones = itemView.findViewById(R.id.btnOpciones);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_auto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, Object> auto = listaAutos.get(position);
            String autoId = listaAutosIds.get(position);

            String marca = auto.get("marca") != null ? (String) auto.get("marca") : "";
            String modelo = auto.get("modelo") != null ? (String) auto.get("modelo") : "";
            String anio = auto.get("anio") != null ? auto.get("anio").toString() : "";
            String precio = auto.get("precio") != null ? "$ " + auto.get("precio").toString() : "$ 0";
            String stock = auto.get("stock") != null ? auto.get("stock").toString() + " disponibles" : "0 disponibles";
            String tipo = auto.get("tipo") != null ? (String) auto.get("tipo") : "";

            holder.tvAutoNombre.setText(marca + " " + modelo);
            holder.tvAutoAnio.setText(anio + " • " + tipo);
            holder.tvAutoPrecio.setText(precio);
            holder.tvAutoStock.setText(stock);

            holder.btnOpciones.setOnClickListener(v -> {
                // Menú editar / eliminar
                android.widget.PopupMenu popup = new android.widget.PopupMenu(ConcesionariaPanelActivity.this, v);
                popup.getMenu().add(0, 0, 0, "Editar");
                popup.getMenu().add(0, 1, 1, "Eliminar");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) {
                        Intent intent = new Intent(ConcesionariaPanelActivity.this, AgregarAutoActivity.class);
                        intent.putExtra("autoId", autoId);
                        startActivity(intent);
                    } else {
                        eliminarAuto(position, autoId);
                    }
                    return true;
                });
                popup.show();
            });
        }

        @Override
        public int getItemCount() {
            return listaAutos.size();
        }
    }

    private void eliminarAuto(int position, String autoId) {
        db.collection("concesionarias").document(uid)
                .collection("autos").document(autoId)
                .delete()
                .addOnSuccessListener(unused -> {
                    listaAutos.remove(position);
                    listaAutosIds.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Auto eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }
}