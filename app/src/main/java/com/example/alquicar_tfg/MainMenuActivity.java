package com.example.alquicar_tfg;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private String idClienteFinal; // Guardaremos el ID aquí bien seguro

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Buscamos los elementos del XML
        TextView tvSaludo = findViewById(R.id.tvSaludo);
        TextView tvMinutos = findViewById(R.id.tvMinutos);
        Button btnAlquilarMinutos = findViewById(R.id.btnAlquilarMinutos);
        Button btnPerfil = findViewById(R.id.btnPerfil);
        TextView tvCerrarSesion = findViewById(R.id.tvCerrarSesion);
        Button btnHistorial = findViewById(R.id.btnHistorial);


        //  EL TRUCO SALVAVIDAS PARA EL ID
        if (getIntent().hasExtra("ID_CLIENTE")) {
            Object idObject = getIntent().getExtras().get("ID_CLIENTE");
            if (idObject != null) {
                idClienteFinal = String.valueOf(idObject);
            }
        }

        // Sacamos nombre y minutos (esto sí viaja bien como texto)
        String nombre = getIntent().getStringExtra("NOMBRE_USUARIO");
        String minutos = getIntent().getStringExtra("MINUTOS_USUARIO");

        // Pintamos los datos en la pantalla principal
        if (nombre != null) {
            tvSaludo.setText("Hola " + nombre);
        }
        if (minutos != null) {
            tvMinutos.setText("Tienes disponibles " + minutos + " minutos");
        }

        // --- FUNCIONES DE LOS BOTONES ---

        // Botón para ir al PERFIL
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, PerfilActivity.class);
            intent.putExtra("ID_CLIENTE", idClienteFinal); // Pasamos el ID seguro
            startActivity(intent);
        });

        // Botón para Alquilar (Mapa) CORREGIDO
        btnAlquilarMinutos.setOnClickListener(ev ->{
            Intent intent = new Intent(MainMenuActivity.this, MapActivity.class);
            intent.putExtra("ID_CLIENTE", idClienteFinal); // ¡Súper importante para guardar el viaje luego!
            startActivity(intent);
        });

        // Cerrar sesión
        tvCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Suponiendo que tu botón se llama btnMenuHistorial
        btnHistorial.setOnClickListener(v -> {
            // El Intent es el "billete de tren" para ir a otra pantalla
            Intent intent = new Intent(MainMenuActivity.this, HistorialActivity.class);

            // Si necesitas pasarle el ID del cliente para luego buscar sus viajes reales:
            // intent.putExtra("ID_CLIENTE", idCliente);

            startActivity(intent);
        });
    }
}