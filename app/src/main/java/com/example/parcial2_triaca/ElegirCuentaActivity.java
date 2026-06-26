package com.example.parcial2_triaca;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ElegirCuentaActivity extends AppCompatActivity {

    private MaterialCardView cardUsuario, cardConcesionaria;
    private RadioButton rbUsuario, rbConcesionaria;
    private MaterialButton btnContinuar;
    private ImageButton btnBack;

    private String tipoSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_choose);

        cardUsuario = findViewById(R.id.cardUsuario);
        cardConcesionaria = findViewById(R.id.cardConcesionaria);
        rbUsuario = findViewById(R.id.rbUsuario);
        rbConcesionaria = findViewById(R.id.rbConcesionaria);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        cardUsuario.setOnClickListener(v -> seleccionar("usuario"));
        cardConcesionaria.setOnClickListener(v -> seleccionar("concesionaria"));

        btnContinuar.setOnClickListener(v -> {
            if (tipoSeleccionado == null) {
                Toast.makeText(this, "Seleccioná un tipo de cuenta", Toast.LENGTH_SHORT).show();
                return;
            }
            if (tipoSeleccionado.equals("usuario")) {
                startActivity(new Intent(this, RegistroUsuarioActivity.class));
            } else {
                startActivity(new Intent(this, RegistroConcesionariaActivity.class));
            }
        });
    }

    private void seleccionar(String tipo) {
        tipoSeleccionado = tipo;

        if (tipo.equals("usuario")) {
            rbUsuario.setChecked(true);
            rbConcesionaria.setChecked(false);
            cardUsuario.setStrokeColor(getColor(R.color.primary));
            cardConcesionaria.setStrokeColor(getColor(R.color.outline_variant));
        } else {
            rbUsuario.setChecked(false);
            rbConcesionaria.setChecked(true);
            cardUsuario.setStrokeColor(getColor(R.color.outline_variant));
            cardConcesionaria.setStrokeColor(getColor(R.color.primary));
        }
    }
}