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

        // Recogemos el ID
        if (getIntent().hasExtra("ID_CLIENTE")) {
            Object idObject = getIntent().getExtras().get("ID_CLIENTE");
            if (idObject != null) {
                idCliente = String.valueOf(idObject);
            }
        }

        // Enlazamos la tarjeta flotante del XML
        cardViajeActivo = findViewById(R.id.cardViajeActivo);
        tvTiempoCronometro = findViewById(R.id.tvTiempoCronometro);
        tvPrecioCronometro = findViewById(R.id.tvPrecioCronometro);
        btnTerminarViajeFlotante = findViewById(R.id.btnTerminarViajeFlotante);

        // Si pulsamos el botón rojo de la tarjeta flotante, terminamos el viaje
        if (btnTerminarViajeFlotante != null) {
            btnTerminarViajeFlotante.setOnClickListener(v -> terminarViaje());
        }

        // Recuperamos el viaje de la memoria interna
        if (idCliente != null) {
            SharedPreferences prefs = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
            tiempoInicioMilisegundos = prefs.getLong("VIAJE_ACTIVO_" + idCliente, 0);

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
        // Si dejas pulsado el botón de menú 2 segundos, se resetea el viaje
        btMenu.setOnLongClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
            prefs.edit().putLong("VIAJE_ACTIVO_" + idCliente, 0).apply();

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

        tvMatricula.setText(matricula);
        tvBateria.setText("Batería: " + bateria + "%");
        tvAutonomia.setText("Autonomía: " + autonomia + "km");
        tvPrecio.setText("Tarifa: " + precio + "€ / min");

        // Si ya hay un viaje activo, no dejamos reservar otro coche
        if (tiempoInicioMilisegundos > 0) {
            btnReservar.setText("Ya tienes un viaje en curso");
            btnReservar.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnReservar.setEnabled(false); // Desactivamos el botón
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
            cardViajeActivo.setVisibility(View.VISIBLE); // Mostramos la tarjeta
        }

        runnableCronometro = new Runnable() {
            @Override
            public void run() {
                long tiempoActual = System.currentTimeMillis();
                long diferencia = tiempoActual - tiempoInicioMilisegundos;

                int minutos = (int) (diferencia / 60000);
                int segundos = (int) ((diferencia % 60000) / 1000);

                // Calculamos el coste progresivo
                double minutosExactos = diferencia / 60000.0;
                double costeActual = minutosExactos * PRECIO_POR_MINUTO;

                // Formateamos los textos
                String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);
                String costeFormateado = String.format("%.2f €", costeActual);

                if (tvTiempoCronometro != null) tvTiempoCronometro.setText(tiempoFormateado);
                if (tvPrecioCronometro != null) tvPrecioCronometro.setText(costeFormateado);

                // Se repite cada segundo
                handlerCronometro.postDelayed(this, 1000);
            }
        };
        handlerCronometro.post(runnableCronometro);
    }

    private void iniciarViaje() {
        tiempoInicioMilisegundos = System.currentTimeMillis();

        SharedPreferences prefs = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("VIAJE_ACTIVO_" + idCliente, tiempoInicioMilisegundos);
        editor.apply();

        Toast.makeText(this, "¡Viaje iniciado! Conduce con cuidado.", Toast.LENGTH_SHORT).show();
        if(dialogActual != null) dialogActual.dismiss();

        // Arrancamos el cronómetro de la tarjeta
        arrancarRelojVisual();
    }

    private void terminarViaje() {
        if(dialogActual != null) dialogActual.dismiss();

        // Apagamos el motor del cronómetro y ocultamos la tarjeta
        if (handlerCronometro != null && runnableCronometro != null) {
            handlerCronometro.removeCallbacks(runnableCronometro);
        }
        if (cardViajeActivo != null) {
            cardViajeActivo.setVisibility(View.GONE);
        }

        // Calculamos los totales finales
        long tiempoFinMilisegundos = System.currentTimeMillis();
        long diferenciaMilisegundos = tiempoFinMilisegundos - tiempoInicioMilisegundos;

        int minutosGastados = (int) (diferenciaMilisegundos / 60000);
        if (minutosGastados == 0) minutosGastados = 1;

        double distanciaFalsa = minutosGastados * 0.5;
        int co2Falso = minutosGastados * 120;

        Toast.makeText(this, "Procesando pago de " + minutosGastados + " min...", Toast.LENGTH_SHORT).show();

        // Enviamos los datos a la base de datos
        enviarDatosViaje(minutosGastados, distanciaFalsa, co2Falso);

        // Borramos el viaje de la memoria interna
        tiempoInicioMilisegundos = 0;
        SharedPreferences prefs = getSharedPreferences("MisViajesAlquiCar", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("VIAJE_ACTIVO_" + idCliente, 0);
        editor.apply();
    }

    private void enviarDatosViaje(int minutos, double distancia, int co2) {
        if (idCliente == null) return;

        double costeFinal = minutos * 0.70;

        int idVehiculoFalso = 1;
        String modalidad = "MINUTOS";

        AlquicarApi api = RetrofitClient.getClient().create(AlquicarApi.class);

        api.registrarViaje(idCliente, idVehiculoFalso, modalidad, distancia, costeFinal, co2).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject result = response.body();
                    if (result.get("status").getAsString().equals("success")) {
                        Toast.makeText(MapActivity.this, "¡Viaje finalizado con éxito!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(MapActivity.this, "Error: " + result.get("message").getAsString(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(MapActivity.this, "Error de red al guardar el viaje", Toast.LENGTH_SHORT).show();
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
        mostrarInfoCoche(matricula, 80, 200, PRECIO_POR_MINUTO);
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
