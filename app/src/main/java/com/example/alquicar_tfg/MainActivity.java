package com.example.alquicar_tfg;

// Los "import" son las herramientas que traemos de fuera para usar en esta clase
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

    // 1. DECLARACIÓN DE VARIABLES:
    // Creamos "cajas" vacías para guardar el botón y el campo de texto que hizo Bryan en el XML.
    private EditText etCorreo;
    private Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aquí le decimos a Java: "Tu aspecto visual es el archivo activity_main.xml"
        setContentView(R.layout.activity_main);

        // 2. VINCULACIÓN (El puente entre XML y Java):
        // Buscamos los elementos por su ID y los metemos en las variables que creamos arriba.
        etCorreo = findViewById(R.id.etCorreo);
        btnContinuar = findViewById(R.id.btContinuar);

        // 3. EL "ESCUCHADOR" DE CLICS:
        // Le ponemos un vigilante al botón. Todo lo que esté dentro de las llaves {} se ejecutará SOLO cuando el usuario pulse el botón.
        btnContinuar.setOnClickListener(v -> {
            // Cogemos el texto que ha escrito el usuario, lo pasamos a String y le quitamos los espacios en blanco de los bordes con trim()
            String correo = etCorreo.getText().toString().trim();

            // Comprobamos si está vacío. Si lo está, mostramos un mensajito emergente (Toast).
            if (correo.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, introduce tu correo", Toast.LENGTH_SHORT).show();
            } else {
                // Si ha escrito algo, llamamos al método que se comunica con el XAMPP de Fernando.
                hacerLogin(correo);
            }
        });
        // Buscamos el botón de ir al registro en el XML
        Button btnIrRegistro = findViewById(R.id.btnIrRegistro);

// Le ponemos el "escuchador" de clics
        btnIrRegistro.setOnClickListener(v -> {
            // Creamos el "viaje" desde esta pantalla (MainActivity) a la nueva (RegisterActivity)
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // 4. LA LLAMADA AL SERVIDOR (Retrofit):
    private void hacerLogin(String correo) {
        // Preparamos la "llamada telefónica" usando las clases que creaste antes.
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);
        Call<JsonObject> call = api.loginUsuario(correo);

        // enqueue() significa "haz la llamada en segundo plano".
        // Esto es vital: Android no permite hacer consultas a internet en el "hilo principal" porque congelaría la pantalla del móvil.
        call.enqueue(new Callback<JsonObject>() {

            // onResponse: XAMPP ha contestado (puede ser un OK o un error de PHP, pero ha contestado).
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extraemos la variable "status" del JSON que Fernando imprimió en su PHP
                    String status = response.body().get("status").getAsString();

                    if (status.equals("success")) {
                        // ¡El correo existe en la base de datos!
                        Toast.makeText(MainActivity.this, "¡Login Correcto!", Toast.LENGTH_SHORT).show();
                    } else {
                        // El correo no existe, mostramos el mensaje de error de Fernando
                        String mensaje = response.body().get("message").getAsString();
                        Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    }
                }
            }

            // onFailure: El servidor de XAMPP está apagado, la IP está mal o no hay internet.
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_LONG).show();
            }
        });
    }
}