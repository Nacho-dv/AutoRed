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

public class AdminUsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private List<Map<String, Object>> listaUsuarios = new ArrayList<>();
    private List<String> listaIds = new ArrayList<>();
    private UsuariosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = FirebaseFirestore.getInstance();

        rvUsuarios = findViewById(R.id.rvUsuarios);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        adapter = new UsuariosAdapter();
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setAdapter(adapter);

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        db.collection("usuarios").get()
                .addOnSuccessListener(query -> {
                    listaUsuarios.clear();
                    listaIds.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        listaUsuarios.add(doc.getData());
                        listaIds.add(doc.getId());
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_generico), Toast.LENGTH_SHORT).show());
    }

    // Adapter interno
    class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNombre, tvEmail, tvRol, tvEstado;

            ViewHolder(View itemView) {
                super(itemView);
                tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
                tvEmail = itemView.findViewById(R.id.tvEmailUsuario);
                tvRol = itemView.findViewById(R.id.tvRolBadge);
                tvEstado = itemView.findViewById(R.id.tvEstadoBadge);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_usuario, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, Object> usuario = listaUsuarios.get(position);

            String nombre = usuario.get("nombre") != null ? (String) usuario.get("nombre") : "Sin nombre";
            String email = usuario.get("email") != null ? (String) usuario.get("email") : "Sin email";
            String rol = usuario.get("rol") != null ? (String) usuario.get("rol") : "usuario";
            String estado = usuario.get("estado") != null ? (String) usuario.get("estado") : "activo";

            holder.tvNombre.setText(nombre);
            holder.tvEmail.setText(email);
            holder.tvRol.setText(rol.toUpperCase());
            holder.tvEstado.setText(estado.toUpperCase());

            // Color del badge de estado
            if (estado.equals("pendiente")) {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_badge_pending);
                holder.tvEstado.setTextColor(getColor(R.color.badge_pending_text));
            } else {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_badge_active);
                holder.tvEstado.setTextColor(getColor(R.color.badge_active_text));
            }

            // Color del badge de rol
            switch (rol) {
                case "admin":
                    holder.tvRol.setBackgroundResource(R.drawable.bg_badge_orange);
                    holder.tvRol.setTextColor(getColor(R.color.on_secondary));
                    break;
                case "concesionaria":
                    holder.tvRol.setBackgroundResource(R.drawable.bg_icon_primary);
                    holder.tvRol.setTextColor(getColor(R.color.on_primary_container));
                    break;
                default:
                    holder.tvRol.setBackgroundResource(R.drawable.bg_icon_circle);
                    holder.tvRol.setTextColor(getColor(R.color.on_surface_variant));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return listaUsuarios.size();
        }
    }
}