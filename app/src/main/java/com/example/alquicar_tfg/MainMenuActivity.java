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

        // 1. Buscamos los elementos del XML
        TextView tvSaludo = findViewById(R.id.tvSaludo);
        TextView tvMinutos = findViewById(R.id.tvMinutos);
        Button btnAlquilarMinutos = findViewById(R.id.btnAlquilarMinutos);

        // --- AQUÍ BUSCAMOS EL BOTÓN DE PERFIL ---
        Button btnPerfil = findViewById(R.id.btnPerfil);

        TextView tvCerrarSesion = findViewById(R.id.tvCerrarSesion);

        // 2. Abrimos la "mochila" del Intent y sacamos los datos que vienen del LOGIN
        String idCliente = getIntent().getStringExtra("ID_CLIENTE"); // IMPORTANTE
        String nombre = getIntent().getStringExtra("NOMBRE_USUARIO");
        String minutos = getIntent().getStringExtra("MINUTOS_USUARIO");

        // 3. Pintamos los datos en la pantalla principal
        if (nombre != null) {
            tvSaludo.setText("Hola " + nombre);
        }
        if (minutos != null) {
            tvMinutos.setText("Tienes disponibles " + minutos + " minutos");
        }

        // --- FUNCIONES DE LOS BOTONES ---

        // Botón para ir al PERFIL (Le pasamos el ID)
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, PerfilActivity.class);
            intent.putExtra("ID_CLIENTE", idCliente); // Le pasamos el ID para que PerfilActivity sepa quién eres
            startActivity(intent);
        });

        // Botón para Alquilar (Mapa)
        /*
        btnAlquilarMinutos.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MapActivity.class);
            startActivity(intent);
        });
        */

        // Cerrar sesión
        tvCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnAlquilarMinutos.setOnClickListener(ev ->{
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        });
    }
}