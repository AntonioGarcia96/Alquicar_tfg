package com.example.alquicar_tfg;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AtencionClienteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atencion_cliente);

        // 1. Botón de Volver (El del menú de arriba a la izquierda)
        LinearLayout btnVolver = findViewById(R.id.btnVolverSoporte);
        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // 2. Botón de Llamar (El botón azul dentro de la tarjeta blanca)
        Button btnLlamar = findViewById(R.id.btnLlamar);
        if (btnLlamar != null) {
            btnLlamar.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:954402847"));
                startActivity(intent);
            });
        }
    }
}