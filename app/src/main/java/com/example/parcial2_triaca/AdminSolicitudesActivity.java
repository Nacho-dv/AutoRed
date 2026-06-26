package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminSolicitudesActivity extends AppCompatActivity {

    private RecyclerView rvSolicitudes;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private List<Map<String, Object>> listaSolicitudes = new ArrayList<>();
    private List<String> listaIds = new ArrayList<>();
    private SolicitudesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_solicitudes);

        db = FirebaseFirestore.getInstance();

        rvSolicitudes = findViewById(R.id.rvSolicitudes);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        adapter = new SolicitudesAdapter();
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));
        rvSolicitudes.setAdapter(adapter);

        cargarSolicitudes();
    }

    private void cargarSolicitudes() {
        db.collection("concesionarias")
                .whereEqualTo("estado", "pendiente")
                .get()
                .addOnSuccessListener(query -> {
                    listaSolicitudes.clear();
                    listaIds.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        listaSolicitudes.add(doc.getData());
                        listaIds.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    private void aprobarConcesionaria(int position) {
        String uid = listaIds.get(position);

        // Actualizar estado en concesionarias
        db.collection("concesionarias").document(uid)
                .update("estado", "activo")
                .addOnSuccessListener(unused -> {
                    // Actualizar estado en usuarios
                    db.collection("usuarios").document(uid)
                            .update("estado", "activo")
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this, "Concesionaria aprobada", Toast.LENGTH_SHORT).show();
                                listaSolicitudes.remove(position);
                                listaIds.remove(position);
                                adapter.notifyItemRemoved(position);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    private void rechazarConcesionaria(int position) {
        String uid = listaIds.get(position);

        // Eliminar concesionaria
        db.collection("concesionarias").document(uid)
                .delete()
                .addOnSuccessListener(unused -> {
                    // Eliminar usuario
                    db.collection("usuarios").document(uid)
                            .delete()
                            .addOnSuccessListener(unused2 -> {
                                Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show();
                                listaSolicitudes.remove(position);
                                listaIds.remove(position);
                                adapter.notifyItemRemoved(position);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    // Adapter interno
    class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombreConcesionaria, tvEmailContacto, tvFechaEnvio;
            com.google.android.material.button.MaterialButton btnAprobar, btnRechazar;

            ViewHolder(View itemView) {
                super(itemView);
                tvNombreConcesionaria = itemView.findViewById(R.id.tvNombreConcesionaria);
                tvEmailContacto = itemView.findViewById(R.id.tvEmailContacto);
                tvFechaEnvio = itemView.findViewById(R.id.tvFechaEnvio);
                btnAprobar = itemView.findViewById(R.id.btnAprobar);
                btnRechazar = itemView.findViewById(R.id.btnRechazar);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_solicitud, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, Object> solicitud = listaSolicitudes.get(position);

            String nombre = solicitud.get("nombre") != null ? (String) solicitud.get("nombre") : "Sin nombre";
            String email = solicitud.get("emailContacto") != null ? (String) solicitud.get("emailContacto") : "Sin email";

            holder.tvNombreConcesionaria.setText(nombre);
            holder.tvEmailContacto.setText(email);
            holder.tvFechaEnvio.setText("Solicitud pendiente");

            holder.btnAprobar.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID) aprobarConcesionaria(pos);
            });

            holder.btnRechazar.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_ID) rechazarConcesionaria(pos);
            });
        }

        @Override
        public int getItemCount() {
            return listaSolicitudes.size();
        }
    }
}