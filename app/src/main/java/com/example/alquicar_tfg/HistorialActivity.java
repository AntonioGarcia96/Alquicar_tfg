package com.example.alquicar_tfg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ViajeAdapter adaptador;
    private List<Viaje> listaViajes = new ArrayList<>();
    private String idCliente = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // 1. Abrimos la caja fuerte para sacar el ID del usuario
        SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
        idCliente = prefs.getString("ID_SESION", "0");

        // Si por algún motivo no hay sesión, avisamos
        if (idCliente.equals("0")) {
            Toast.makeText(this, "Error: No se encontró la sesión del usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Configurar el botón Volver
        LinearLayout btnVolver = findViewById(R.id.layoutVolver);
        btnVolver.setOnClickListener(v -> finish());

        // 3. Preparar el RecyclerView (Lista VACÍA al principio, sin datos falsos)
        recyclerView = findViewById(R.id.recyclerViewHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptador = new ViajeAdapter(listaViajes);
        recyclerView.setAdapter(adaptador);

        // 4. Pedir los datos reales al PHP
        cargarHistorialDesdeServidor();
    }

    private void cargarHistorialDesdeServidor() {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        api.obtenerHistorial(idCliente).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaViajes.clear(); // Limpiamos por si acaso

                    // Llenamos la lista con lo que diga MySQL
                    for (JsonObject obj : response.body()) {
                        listaViajes.add(new Viaje(
                                obj.get("fecha_hora_inicio").getAsString(),
                                obj.get("duracion").getAsInt(),
                                obj.get("recorrido").getAsDouble(),
                                obj.get("hora_inicio").getAsString(),
                                obj.get("hora_fin").getAsString(),
                                obj.get("coste").getAsDouble()
                        ));
                    }

                    // Si el usuario no tiene viajes, se lo decimos
                    if(listaViajes.isEmpty()){
                        Toast.makeText(HistorialActivity.this, "Aún no tienes viajes registrados", Toast.LENGTH_SHORT).show();
                    }

                    // Actualizamos la pantalla
                    adaptador.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                Toast.makeText(HistorialActivity.this, "Error de red al cargar el historial", Toast.LENGTH_SHORT).show();
            }
        });
    }
}