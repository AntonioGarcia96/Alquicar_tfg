package com.example.alquicar_tfg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenuActivity extends AppCompatActivity {

    private String idClienteFinal;
    private TextView tvSaludo;
    private TextView tvMinutos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        tvSaludo = findViewById(R.id.tvSaludo);
        tvMinutos = findViewById(R.id.tvMinutos);
        Button btnAlquilarMinutos = findViewById(R.id.btnAlquilarMinutos);
        Button btnPerfil = findViewById(R.id.btnPerfil);
        TextView tvCerrarSesion = findViewById(R.id.tvCerrarSesion);
        Button btnHistorial = findViewById(R.id.btnHistorial);
        Button btnAtencion = findViewById(R.id.btnAtencion);
        Button btnBonos = findViewById(R.id.btnBonos);

        // Lógica para recuperar el ID del cliente
        if (getIntent().hasExtra("ID_CLIENTE")) {
            Object idObject = getIntent().getExtras().get("ID_CLIENTE");
            if (idObject != null) {
                idClienteFinal = String.valueOf(idObject);
            }
        }

        // --- CONFIGURACIÓN DE BOTONES ---

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, PerfilActivity.class);
            intent.putExtra("ID_CLIENTE", idClienteFinal);
            startActivity(intent);
        });

        btnAlquilarMinutos.setOnClickListener(ev -> {
            Intent intent = new Intent(MainMenuActivity.this, MapActivity.class);
            intent.putExtra("ID_CLIENTE", idClienteFinal);
            startActivity(intent);
        });

        btnHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, HistorialActivity.class);
            intent.putExtra("ID_CLIENTE", idClienteFinal);
            startActivity(intent);
        });

        btnAtencion.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, AtencionClienteActivity.class);
            startActivity(intent);
        });

        btnBonos.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, BonosActivity.class);
            startActivity(intent);
        });

        tvCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (idClienteFinal != null && !idClienteFinal.isEmpty()) {
            actualizarDatosDesdeServidor();
        } else {
            SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
            String nombre = prefs.getString("NOMBRE_USUARIO", "Usuario");
            String minutos = prefs.getString("MINUTOS_USUARIO", "0");
            if (tvSaludo != null) tvSaludo.setText("Hola " + nombre);
            if (tvMinutos != null) tvMinutos.setText("Tienes disponibles " + minutos + " minutos");
        }
    }


    private void actualizarDatosDesdeServidor() {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);


        api.obtenerPerfil(idClienteFinal).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    if (data.has("status") && data.get("status").getAsString().equals("success")) {

                        String minutosReales = data.get("minutos_disponibles").getAsString();
                        String nombreReal = data.get("nombre").getAsString();


                        if (tvSaludo != null) tvSaludo.setText("Hola " + nombreReal);
                        if (tvMinutos != null) tvMinutos.setText("Tienes disponibles " + minutosReales + " minutos");


                        SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
                        prefs.edit()
                                .putString("MINUTOS_USUARIO", minutosReales)
                                .putString("NOMBRE_USUARIO", nombreReal)
                                .apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
                String minutosGuardados = prefs.getString("MINUTOS_USUARIO", "0");
                if (tvMinutos != null) tvMinutos.setText("Tienes disponibles " + minutosGuardados + " minutos");
            }
        });
    }
}
