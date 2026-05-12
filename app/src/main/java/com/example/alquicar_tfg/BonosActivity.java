package com.example.alquicar_tfg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BonosActivity extends AppCompatActivity {

    private String idClienteSesion;
    private TextView tvMinutosActuales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonos);

        // 1. Recuperamos el ID del usuario y sus minutos actuales de la sesión (SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
        idClienteSesion = prefs.getString("ID_SESION", "0");
        String minutosGuardados = prefs.getString("MINUTOS_USUARIO", "0"); // Asumiendo que guardaste esto en el Login

        // 2. Enlazamos las vistas
        tvMinutosActuales = findViewById(R.id.tvMinutosActuales);
        tvMinutosActuales.setText(minutosGuardados); // Mostramos el 35 de tu diseño inicial

        LinearLayout btnVolver = findViewById(R.id.btnVolverBonos);
        LinearLayout btnBono30 = findViewById(R.id.btnBono30);
        LinearLayout btnBono60 = findViewById(R.id.btnBono60);
        LinearLayout btnBono120 = findViewById(R.id.btnBono120); // Asegúrate de tener estos IDs en tu XML
        LinearLayout btnBono180 = findViewById(R.id.btnBono180);

        // 3. Programamos los clics
        btnVolver.setOnClickListener(v -> finish());

        btnBono30.setOnClickListener(v -> mostrarDialogoConfirmacion(30, "20€"));
        btnBono60.setOnClickListener(v -> mostrarDialogoConfirmacion(60, "35€"));

        // Si tienes los otros en el XML, descomenta esto:
        // btnBono120.setOnClickListener(v -> mostrarDialogoConfirmacion(120, "65€"));
        // btnBono180.setOnClickListener(v -> mostrarDialogoConfirmacion(180, "90€"));
    }

    // Método para mostrar el Pop-up de confirmación
    private void mostrarDialogoConfirmacion(int minutos, String precio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar compra");
        builder.setMessage("¿Estás seguro de que deseas comprar un bono de " + minutos + " minutos por " + precio);

        // Si le da a Aceptar, llamamos al servidor
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            sumarMinutosEnServidor(minutos);
        });

        // Si le da a Cancelar, no hacemos nada y cerramos la ventana
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para conectar con PHP y actualizar la Base de Datos
    private void sumarMinutosEnServidor(int minutosComprados) {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        api.comprarBono(idClienteSesion, minutosComprados).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().get("status").getAsString();

                    if (status.equals("success")) {
                        // El servidor nos devuelve la nueva suma total de minutos
                        String nuevoTotal = response.body().get("nuevos_minutos").getAsString();

                        // 1. Actualizamos el número gigante de la pantalla
                        tvMinutosActuales.setText(nuevoTotal);

                        // 2. Guardamos el nuevo total en SharedPreferences para que el Perfil y el Menú Principal también se actualicen
                        SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
                        prefs.edit().putString("MINUTOS_USUARIO", nuevoTotal).apply();

                        // 3. Mostramos el Toast de éxito
                        Toast.makeText(BonosActivity.this, "Minutos añadidos correctamente", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(BonosActivity.this, "Error al comprar bono", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(BonosActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}