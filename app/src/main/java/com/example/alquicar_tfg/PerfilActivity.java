package com.example.alquicar_tfg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    private TextView tvNombre, tvMinutos, tvViajes, tvDistancia, tvAhorro;
    private String idClienteFinal; 

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tvNombre = findViewById(R.id.tvPerfilNombre);
        tvMinutos = findViewById(R.id.tvPerfilMinutos);
        tvViajes = findViewById(R.id.tvViajesNum);
        tvDistancia = findViewById(R.id.tvDistanciaNum);
        tvAhorro = findViewById(R.id.tvAhorroNum);

        findViewById(R.id.tvVolverLabel).setOnClickListener(v -> finish());


        if (getIntent().hasExtra("ID_CLIENTE")) {
            Object idObject = getIntent().getExtras().get("ID_CLIENTE");
            if (idObject != null) {
                idClienteFinal = String.valueOf(idObject);
            }
        }

        if (idClienteFinal != null && !idClienteFinal.equals("-1")) {
            cargarDatosServidor(idClienteFinal);
        } else {
            Toast.makeText(this, "Error: El ID llegó vacío al Perfil", Toast.LENGTH_LONG).show();
        }

        Button btnEditar = findViewById(R.id.btnEditarDatos);
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, EditarPerfilActivity.class);

            intent.putExtra("ID_CLIENTE", idClienteFinal);

            startActivity(intent);
        });
    }

    private void cargarDatosServidor(String id) {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);
        api.obtenerPerfil(id).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();
                    if (data.get("status").getAsString().equals("success")) {
                        tvNombre.setText(data.get("nombre").getAsString());
                        tvMinutos.setText(data.get("minutos_disponibles").getAsString() + " min disponibles");
                        tvViajes.setText(data.get("viajes_realizados").getAsString());
                        tvDistancia.setText(data.get("distancia_total").getAsString() + "km");
                        tvAhorro.setText(data.get("ahorro_co2").getAsString());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
