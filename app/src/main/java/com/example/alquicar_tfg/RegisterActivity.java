package com.example.alquicar_tfg;

import android.os.Bundle;
import android.widget.TextView; // ¡Asegúrate de que esto se importa!
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 1. Enlazamos el texto de "Volver" que Bryan puso en el XML
        TextView tvVolver = findViewById(R.id.tvVolver);

        // 2. Le ponemos el escuchador de clics
        tvVolver.setOnClickListener(v -> {
            // El comando finish() cierra esta pantalla y te devuelve a la anterior
            finish();
        });

        // Aquí debajo programaremos luego el botón verde de "Registrarse"
    }
}