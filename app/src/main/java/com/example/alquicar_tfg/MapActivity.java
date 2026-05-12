package com.example.alquicar_tfg;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.alquicar_tfg.api.AlquicarApi;
import com.example.alquicar_tfg.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranted;
    private final LatLng mDefaultLocation = new LatLng(37.38961262805748, -5.982849049283779);
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GoogleMap mapa;
    private LocationManager locationManager;
    private Location currentLocation;

    // Variables para cronómetro flotante
    private View cardViajeActivo;
    private TextView tvTiempoCronometro, tvPrecioCronometro;
    private Button btnTerminarViajeFlotante;
    private android.os.Handler handlerCronometro = new android.os.Handler();
    private Runnable runnableCronometro;
    private final double PRECIO_POR_MINUTO = 0.70;

    // NUEVAS VARIABLES PARA EL SISTEMA DE BONOS
    private int minutosBonoIniciales = 0;

    TextView tvMatricula, tvAutonomia, tvPrecio, tvBateria;
    Button btnReservar;
    ImageButton btMenu;

    private String idCliente;
    private long tiempoInicioMilisegundos = 0;
    private BottomSheetDialog dialogActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        // 1. Recogemos el ID
        if (getIntent().hasExtra("ID_CLIENTE")) {
            Object idObject = getIntent().getExtras().get("ID_CLIENTE");
            if (idObject != null) {
                idCliente = String.valueOf(idObject);
            }
        }

        // LEEMOS LOS MINUTOS DISPONIBLES AL ABRIR EL MAPA
        SharedPreferences prefs = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
        String minutosGuardados = prefs.getString("MINUTOS_USUARIO", "0");
        minutosBonoIniciales = Integer.parseInt(minutosGuardados);

        // 2. Enlazamos la tarjeta flotante del XML
        cardViajeActivo = findViewById(R.id.cardViajeActivo);
        tvTiempoCronometro = findViewById(R.id.tvTiempoCronometro);
        tvPrecioCronometro = findViewById(R.id.tvPrecioCronometro);
        btnTerminarViajeFlotante = findViewById(R.id.btnTerminarViajeFlotante);

        // Si pulsamos el botón rojo de la tarjeta flotante, terminamos el viaje
        if (btnTerminarViajeFlotante != null) {
            btnTerminarViajeFlotante.setOnClickListener(v -> terminarViaje());
        }

        // 3. Recuperamos el viaje de la memoria interna
        if (idCliente != null) {
            SharedPreferences prefsViajes = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
            tiempoInicioMilisegundos = prefsViajes.getLong("VIAJE_ACTIVO_" + idCliente, 0);

            // Si ya había un viaje guardado, arrancamos la tarjeta visual directamente
            if (tiempoInicioMilisegundos > 0) {
                arrancarRelojVisual();
            }
        }

        btMenu = findViewById(R.id.btMenu);
        btMenu.setOnClickListener(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapaCoches);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btMenu.setOnLongClickListener(v -> {
            SharedPreferences prefsViajes = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
            prefsViajes.edit().putLong("VIAJE_ACTIVO_" + idCliente, 0).apply();

            tiempoInicioMilisegundos = 0;
            if (cardViajeActivo != null) cardViajeActivo.setVisibility(View.GONE);
            if (handlerCronometro != null && runnableCronometro != null) {
                handlerCronometro.removeCallbacks(runnableCronometro);
            }

            Toast.makeText(this, "Debug: Viaje reseteado manualmente", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    public void mostrarInfoCoche(String matricula, int bateria, int autonomia, double precio) {
        dialogActual = new BottomSheetDialog(this);
        View vista = LayoutInflater.from(this).inflate(R.layout.coche_info, null);

        tvMatricula = vista.findViewById(R.id.tvMatricula);
        tvBateria = vista.findViewById(R.id.tvBateria);
        tvAutonomia = vista.findViewById(R.id.tvAutonomia);
        tvPrecio = vista.findViewById(R.id.tvTarifa);
        btnReservar = vista.findViewById(R.id.btnReservarCoche);

        tvMatricula.setText("Matrícula " + matricula);
        tvBateria.setText("Batería: " + bateria + "%");
        tvAutonomia.setText("Autonomía: " + autonomia + "km");
        tvPrecio.setText("Tarifa: " + precio + "€ / min");

        if (tiempoInicioMilisegundos > 0) {
            btnReservar.setText("Ya tienes un viaje en curso");
            btnReservar.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnReservar.setEnabled(false);
        } else {
            btnReservar.setText("Reservar y Empezar Viaje");
            btnReservar.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            btnReservar.setEnabled(true);
            btnReservar.setOnClickListener(v -> iniciarViaje());
        }

        dialogActual.setContentView(vista);
        dialogActual.show();
    }

    private void arrancarRelojVisual() {
        if (cardViajeActivo != null) {
            cardViajeActivo.setVisibility(View.VISIBLE);
        }

        runnableCronometro = new Runnable() {
            @Override
            public void run() {
                long tiempoActual = System.currentTimeMillis();
                long diferencia = tiempoActual - tiempoInicioMilisegundos;

                int minutosGastados = (int) (diferencia / 60000);
                int segundosGastados = (int) ((diferencia % 60000) / 1000);

                // Mostramos el tiempo exacto en formato MM:SS
                String tiempoFormateado = String.format("%02d:%02d", minutosGastados, segundosGastados);
                if (tvTiempoCronometro != null) tvTiempoCronometro.setText(tiempoFormateado);

                // --- LÓGICA HÍBRIDA DE PAGO ---
                if (tvPrecioCronometro != null) {
                    if (minutosGastados < minutosBonoIniciales) {
                        // AÚN TIENE SALDO DEL BONO
                        int minutosRestantes = minutosBonoIniciales - minutosGastados;
                        tvPrecioCronometro.setText("Quedan: " + minutosRestantes + " min restantes");
                        tvPrecioCronometro.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // Verde para "gratis"
                    } else {
                        // SE QUEDÓ SIN BONO, EMPIEZA A COBRAR
                        int minutosFacturables = minutosGastados - minutosBonoIniciales;

                        // Calculamos el coste basándonos solo en los minutos fuera del bono
                        // Usamos double para tener céntimos (ej. 1 min extra * 0.70€ = 0.70€)
                        double costeReal = minutosFacturables * PRECIO_POR_MINUTO;

                        tvPrecioCronometro.setText(String.format("Precio: %.2f €", costeReal));
                        tvPrecioCronometro.setTextColor(getResources().getColor(android.R.color.black)); // Negro para cobrar
                    }
                }

                handlerCronometro.postDelayed(this, 1000);
            }
        };
        handlerCronometro.post(runnableCronometro);
    }

    private void iniciarViaje() {
        tiempoInicioMilisegundos = System.currentTimeMillis();

        SharedPreferences prefsViajes = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefsViajes.edit();
        editor.putLong("VIAJE_ACTIVO_" + idCliente, tiempoInicioMilisegundos);
        editor.apply();

        // Refrescamos los minutos justo antes de arrancar por si compró un bono
        SharedPreferences prefsUsuario = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
        minutosBonoIniciales = Integer.parseInt(prefsUsuario.getString("MINUTOS_USUARIO", "0"));

        Toast.makeText(this, "¡Viaje iniciado! Conduce con cuidado.", Toast.LENGTH_SHORT).show();
        if(dialogActual != null) dialogActual.dismiss();

        arrancarRelojVisual();
    }

    private void terminarViaje() {
        if(dialogActual != null) dialogActual.dismiss();

        if (handlerCronometro != null && runnableCronometro != null) {
            handlerCronometro.removeCallbacks(runnableCronometro);
        }
        if (cardViajeActivo != null) {
            cardViajeActivo.setVisibility(View.GONE);
        }

        long tiempoFinMilisegundos = System.currentTimeMillis();
        long diferenciaMilisegundos = tiempoFinMilisegundos - tiempoInicioMilisegundos;

        int minutosGastados = (int) (diferenciaMilisegundos / 60000);
        if (minutosGastados == 0) minutosGastados = 1; // Mínimo cobramos 1 minuto

        // CÁLCULO FINAL DE PAGO Y BONO
        int minutosBonoConsumidos = Math.min(minutosGastados, minutosBonoIniciales);
        int nuevosMinutosDisponibles = minutosBonoIniciales - minutosBonoConsumidos;

        int minutosParaPagar = minutosGastados - minutosBonoConsumidos;
        double costeFinalEuros = minutosParaPagar * PRECIO_POR_MINUTO;

        // Datos falsos para rellenar BD
        double distanciaFalsa = minutosGastados * 0.5;
        int co2Falso = minutosGastados * 120;

        Toast.makeText(this, "Terminando... Has usado " + minutosBonoConsumidos + " min de bono. A pagar: " + String.format("%.2f", costeFinalEuros) + "€", Toast.LENGTH_LONG).show();

        // 1. Guardamos el nuevo saldo de minutos en el teléfono
        SharedPreferences prefsUsuario = getSharedPreferences("UsuarioAlquiCar", MODE_PRIVATE);
        prefsUsuario.edit().putString("MINUTOS_USUARIO", String.valueOf(nuevosMinutosDisponibles)).apply();

        // 2. Enviamos el viaje al servidor (PHP)
        enviarDatosViaje(minutosGastados, distanciaFalsa, costeFinalEuros, co2Falso, nuevosMinutosDisponibles);

        // 3. Limpiamos el viaje
        tiempoInicioMilisegundos = 0;
        SharedPreferences prefsViajes = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
        prefsViajes.edit().putLong("VIAJE_ACTIVO_" + idCliente, 0).apply();
    }

    private void enviarDatosViaje(int minutosTotales, double distancia, double costeFinalEuros, int co2, int nuevosMinutosDisponibles) {
        if (idCliente == null) return;

        int idVehiculoFalso = 1;
        String modalidad = "MINUTOS";

        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        // REGISTRAMOS EL VIAJE CON EL COSTE FINAL REAL
        api.registrarViaje(idCliente, idVehiculoFalso, modalidad, distancia, costeFinalEuros, co2).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject result = response.body();
                    if (result.get("status").getAsString().equals("success")) {

                        // Si el viaje se guardó bien, ACTUALIZAMOS LOS MINUTOS EN EL SERVIDOR
                        actualizarMinutosServidor(nuevosMinutosDisponibles);

                    } else {
                        Toast.makeText(MapActivity.this, "Error BD Viaje: " + result.get("message").getAsString(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Error de red al guardar el viaje", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void actualizarMinutosServidor(int nuevosMinutos) {
        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        // Llamamos al servidor para que guarde el nuevo saldo permanentemente
        api.actualizarMinutos(idCliente, nuevosMinutos).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Si la base de datos ya está actualizada, ya podemos cerrar con seguridad
                    Toast.makeText(MapActivity.this, "¡Viaje finalizado y minutos guardados!", Toast.LENGTH_LONG).show();
                    finish(); // Volvemos al menú principal
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Si falla el internet, avisamos, pero al menos lo hemos guardado en SharedPreferences
                Log.e("API_ERROR", "No se pudo sincronizar el saldo: " + t.getMessage());
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        getLocationPermission();

        LatLng sevilla = new LatLng(37.3891, -5.9845);
        LatLng coche1 = new LatLng(37.39879385580851, -5.972624036123549);

        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(sevilla, 14f));
        mapa.setOnMarkerClickListener(this);

        mapa.addMarker(new MarkerOptions()
                .position(coche1)
                .icon(obtenerIconoCoche(R.drawable.icono_coche_mapa))
                .anchor(0.5f, 0.5f));
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String matricula = marker.getSnippet();
        mostrarInfoCoche("2323FAB", 80, 200, PRECIO_POR_MINUTO);
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btMenu.getId()){
            finish();
        }
    }

    private void getLocationPermission(){
        String[] permisos= {android.Manifest.permission.ACCESS_FINE_LOCATION};
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
        } else{
            ActivityCompat.requestPermissions(this, permisos, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else{
                mLocationPermissionGranted = false;
                Toast.makeText(this, "Ve a ajustes para conceder el permiso de ubicación.", Toast.LENGTH_LONG).show();
            }
            updateLocationUI();
        }
    }

    private void updateLocationUI(){
        if(mapa != null){
            try{
                if(mLocationPermissionGranted){
                    mapa.setMyLocationEnabled(true);
                    mapa.getUiSettings().setMyLocationButtonEnabled(true);
                } else{
                    mapa.setMyLocationEnabled(false);
                    mapa.getUiSettings().setMyLocationButtonEnabled(false);
                }
            } catch(SecurityException e){
                Log.e("Permiso no concedido. ", e.getMessage());
            }
        }
    }

    private com.google.android.gms.maps.model.BitmapDescriptor obtenerIconoCoche(int idImagen) {
        android.graphics.drawable.Drawable vectorDrawable = androidx.core.content.ContextCompat.getDrawable(this, idImagen);
        if (vectorDrawable == null) return null;

        int anchoOriginal = vectorDrawable.getIntrinsicWidth();
        int altoOriginal = vectorDrawable.getIntrinsicHeight();

        if (anchoOriginal <= 0 || altoOriginal <= 0) {
            anchoOriginal = 100;
            altoOriginal = 100;
        }

        int anchoDeseado = 300;
        int altoCalculado = (altoOriginal * anchoDeseado) / anchoOriginal;

        vectorDrawable.setBounds(0, 0, anchoDeseado, altoCalculado);
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(anchoDeseado, altoCalculado, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}