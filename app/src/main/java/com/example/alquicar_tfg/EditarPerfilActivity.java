package com.example.alquicar_tfg;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etApellidos, etDireccion, etEmail, etContrasena, etCuenta;
    private String idCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);

        etNombre = findViewById(R.id.etEditarNombre);
        etApellidos = findViewById(R.id.etEditarApellidos);
        etDireccion = findViewById(R.id.etEditarDireccion);
        etEmail = findViewById(R.id.etEditarEmail);
        etContrasena = findViewById(R.id.etEditarContrasena);
        etCuenta = findViewById(R.id.etEditarCuenta);

        Button btnGuardar = findViewById(R.id.btnGuardarCambios);
        TextView tvVolver = findViewById(R.id.tvVolverEditar);

        tvVolver.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("ID_CLIENTE")) {
            Object idObject = getIntent().getExtras().get("ID_CLIENTE");
            if (idObject != null) {
                idCliente = String.valueOf(idObject);
            }
        }

        if (idCliente != null && !idCliente.equals("-1")) {
            cargarDatosUsuario();
        } else {
            Toast.makeText(this, "Error: No se encontró el ID del usuario", Toast.LENGTH_SHORT).show();
        }

        btnGuardar.setOnClickListener(v -> guardarDatosUsuario());
    }

    private void cargarDatosUsuario() {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        api.obtenerPerfil(idCliente).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    if (data.get("status").getAsString().equals("success")) {
                        if(data.has("nombre") && !data.get("nombre").isJsonNull())
                            etNombre.setText(data.get("nombre").getAsString());

                        if(data.has("apellidos") && !data.get("apellidos").isJsonNull())
                            etApellidos.setText(data.get("apellidos").getAsString());

                        if(data.has("direccion") && !data.get("direccion").isJsonNull())
                            etDireccion.setText(data.get("direccion").getAsString());

                        if(data.has("email") && !data.get("email").isJsonNull())
                            etEmail.setText(data.get("email").getAsString());

                        // CAMBIO CLAVE: No cargamos la contraseña.
                        // Dejamos el campo vacío para que el usuario solo escriba si quiere cambiarla.
                        etContrasena.setText("");

                        if(data.has("cuenta_bancaria") && !data.get("cuenta_bancaria").isJsonNull())
                            etCuenta.setText(data.get("cuenta_bancaria").getAsString());
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(EditarPerfilActivity.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarDatosUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Si el usuario no escribe nada, enviamos una cadena vacía ""
        String contrasena = etContrasena.getText().toString().trim();

        String cuenta = etCuenta.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "El nombre y el correo son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        // Al enviar 'contrasena' (que puede ser ""), el nuevo PHP que hicimos
        // sabrá si debe actualizarla o dejar la que ya estaba.
        api.actualizarPerfil(idCliente, nombre, apellidos, direccion, email, contrasena, cuenta).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject result = response.body();

                    if (result.get("status").getAsString().equals("success")) {
                        Toast.makeText(EditarPerfilActivity.this, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditarPerfilActivity.this, "Error: " + result.get("message").getAsString(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(EditarPerfilActivity.this, "Fallo de conexión al guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
