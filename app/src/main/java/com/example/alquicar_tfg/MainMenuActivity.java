package com.example.alquicar_tfg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 1. Buscamos los textos de arriba del todo en el XML
        TextView tvSaludo = findViewById(R.id.tvSaludo);
        TextView tvMinutos = findViewById(R.id.tvMinutos);

        // Buscamos los botones
        Button btnAlquilarMinutos = findViewById(R.id.btnAlquilarMinutos);
        TextView tvCerrarSesion = findViewById(R.id.tvCerrarSesion);

        // 2. NUEVO: Abrimos la "mochila" del Intent y sacamos los datos
        String nombre = getIntent().getStringExtra("NOMBRE_USUARIO");
        String minutos = getIntent().getStringExtra("MINUTOS_USUARIO");

        // 3. NUEVO: Si los datos no están vacíos, los pintamos en la pantalla
        if (nombre != null) {
            tvSaludo.setText("Hola " + nombre);
        }
        if (minutos != null) {
            tvMinutos.setText("Tienes disponibles " + minutos + " minutos");
        }

        // --- FUNCIONES DE LOS BOTONES ---

        //btnAlquilarMinutos.setOnClickListener(v -> {
            //Intent intent = new Intent(MainMenuActivity.this, MapActivity.class);
            //startActivity(intent);
        //});

        tvCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}