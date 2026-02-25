package com.example.alquicar_tfg;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Importamos las herramientas de nuestra API
import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // 1. Declaramos todas las "cajas" de texto
    private EditText etDni, etNombre, etApellidos, etDireccion, etEmail, etPassword, etIban, etTelefono, etFecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // El botón de volver que ya teníais
        TextView tvVolver = findViewById(R.id.tvVolver);
        tvVolver.setOnClickListener(v -> finish());

        etDni = findViewById(R.id.etDniReg);
        etNombre = findViewById(R.id.etNombreReg);
        etApellidos = findViewById(R.id.etApellidosReg);
        etDireccion = findViewById(R.id.etDireccionReg);
        etEmail = findViewById(R.id.etEmailReg);
        etPassword = findViewById(R.id.etPasswordReg);
        etIban = findViewById(R.id.etIbanReg);
        etTelefono= findViewById(R.id.etTelefono);
        etFecha= findViewById(R.id.etFecha);

        Button btnRegistrar = findViewById(R.id.btnRegistrarUsuario);

        btnRegistrar.setOnClickListener(v -> realizarRegistro());
    }

    private void realizarRegistro() {
        // Extraemos el texto que ha escrito el usuario y le quitamos los espacios en blanco
        String dni = etDni.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String iban = etIban.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String fecha_nacimiento = etFecha.getText().toString().trim();

        // 4. Validación básica: Comprobamos que lo más importante no esté vacío
        if (dni.isEmpty() || email.isEmpty() || password.isEmpty() || nombre.isEmpty() || apellidos.isEmpty() || direccion.isEmpty() || iban.isEmpty() || fecha_nacimiento.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();

        }else{
            // 5. Llamamos a Fernando (API) usando Retrofit
            AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);
            Call<JsonObject> call = api.registrarUsuario(dni, nombre, apellidos, fecha_nacimiento, direccion, telefono, email, iban, password);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Leemos lo que nos contesta el XAMPP
                        String status = response.body().get("status").getAsString();
                        String mensaje = response.body().get("message").getAsString();

                        if (status.equals("success")) {
                            // ¡Se guardó bien!
                            Toast.makeText(RegisterActivity.this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show();
                            finish(); // Cerramos esta pantalla y le devolvemos al Login para que entre
                        } else {
                            // Error desde el servidor (ej: "El DNI ya existe")
                            Toast.makeText(RegisterActivity.this, "Aviso: " + mensaje, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Error de conexión (XAMPP apagado, sin internet...)
                    Toast.makeText(RegisterActivity.this, "Error de conexión con el servidor" + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        }
    }
}