package com.example.alquicar_tfg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etPassword;
    private Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCorreo = findViewById(R.id.etCorreo);
        // ¡Aquí está el cambio! Enlazamos con el ID exacto de Bryan: etContrasenna
        etPassword = findViewById(R.id.etContrasenna);
        btnContinuar = findViewById(R.id.btContinuar);

        btnContinuar.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, introduce tu correo y contraseña", Toast.LENGTH_SHORT).show();
            } else {
                hacerLogin(correo, password);
            }
        });

        Button btnIrRegistro = findViewById(R.id.btRegistrarse);
        btnIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void hacerLogin(String correo, String password) {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);
        Call<JsonObject> call = api.loginUsuario(correo, password);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().get("status").getAsString();

                    if (status.equals("success")) {
                        // Extraemos TODO lo que nos manda el PHP
                        String nombreUsuario = response.body().get("nombre").getAsString();
                        String minutosUsuario = response.body().get("minutos_disponibles").getAsString();

                        // NUEVO: Extraemos el ID_CLIENTE
                        String idUsuario = response.body().get("id_cliente").getAsString();

                        // Preparamos el viaje
                        Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);

                        // Metemos los datos en la "mochila" del Intent
                        intent.putExtra("NOMBRE_USUARIO", nombreUsuario);
                        intent.putExtra("MINUTOS_USUARIO", minutosUsuario);

                        // NUEVO: Metemos el ID en la mochila
                        intent.putExtra("ID_CLIENTE", idUsuario);

                        startActivity(intent);
                        finish();

                    } else {
                        String mensaje = response.body().get("message").getAsString();
                        Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}