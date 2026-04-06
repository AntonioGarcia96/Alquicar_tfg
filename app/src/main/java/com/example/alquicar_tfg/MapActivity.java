package com.example.alquicar_tfg;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;
    private boolean mLocationPermissionGranted;
    //en caso de que no se conceda el permiso de ubi debe haber una por defecto (Sevilla)
    private final LatLng mDefaultLocation = new LatLng(37.38961262805748, -5.982849049283779);
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //atributos usados
    private GoogleMap mapa;
    private LocationManager locationManager;
    private Location currentLocation;

    TextView tvMatricula;
    TextView tvAutonomia;
    TextView tvPrecio;
    TextView tvBateria;
    Button btnReservar;
    ImageButton btMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        btMenu= findViewById(R.id.btMenu);
        btMenu.setOnClickListener(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapaCoches);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void mostrarInfoCoche(String matricula, int bateria, int autonomia, double precio) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View vista = LayoutInflater.from(this).inflate(R.layout.coche_info, null);

        tvMatricula = vista.findViewById(R.id.tvMatricula);
        tvBateria = vista.findViewById(R.id.tvBateria);
        tvAutonomia = vista.findViewById(R.id.tvAutonomia);
        tvPrecio = vista.findViewById(R.id.tvTarifa);
        btnReservar = vista.findViewById(R.id.btnReservarCoche);

        tvMatricula.setText(matricula);
        tvBateria.setText("Batería: " + bateria + "%");
        tvAutonomia.setText("Autonomía: " + autonomia + "km");
        tvPrecio.setText("Tarifa: " + precio + "€");

        btnReservar.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Reserva iniciada para " + matricula,
                    Toast.LENGTH_SHORT).show();

            //dialog.dismiss();
        });

        dialog.setContentView(vista);
        dialog.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;

        getLocationPermission();

        LatLng sevilla = new LatLng(37.3891, -5.9845);
        LatLng coche1 = new LatLng(37.401066213722494, -5.975189283196233);

        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(sevilla, 14f));

        mapa.addMarker(new MarkerOptions()
                .position(coche1)
                .title("Seat Mii").snippet("1234TFG"));

        mapa.setOnMarkerClickListener(this);

        //mapa.animateCamera();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String matricula = marker.getSnippet();
        int autonomia = 200;
        int bateria = 80;
        double precio = 0.70;

        mostrarInfoCoche(matricula, bateria, autonomia, precio);
        return true;
        //Toast.makeText(this, marker.getTitle() + " - " + marker.getSnippet(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent= null;

        if(id == btMenu.getId()){
            intent = new Intent(this, MainMenuActivity.class);
            startActivity(intent);
        }
    }

    //solicitar permiso al usuario para acceder a la ubicación del dispositivo
    private void getLocationPermission(){
        String[] permisos= {android.Manifest.permission.ACCESS_FINE_LOCATION};

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
        } else{
            ActivityCompat.requestPermissions(this, permisos, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //Se ejecuta automáticamente cuando el usuario ha aceptado o denegado el permiso de ubicación
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
            //getDeviceLocation();
        }
    }

    //modifica la interfaz del mapa después de onRequestPermissionsResult()
    private void updateLocationUI(){
        if(mapa != null){
            try{
                if(mLocationPermissionGranted){
                    mapa.setMyLocationEnabled(true);
                    mapa.getUiSettings().setMyLocationButtonEnabled(true);
                } else{
                    mapa.setMyLocationEnabled(false);
                    mapa.getUiSettings().setMyLocationButtonEnabled(false);
                    //mLastKnownLocation= null;
                }

            } catch(SecurityException e){
                Log.e("Permiso de ubicación no concedido. ", e.getMessage());
            }
        }
    }

//    private void getDeviceLocation(){
//        try{
//            if(mLocationPermissionGranted){
//                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
//                locationResult.addOnCompleteListener(this, ev ->{
//                   if(ev.isSuccessful() && ev.getResult() != null){
//                       mLastKnownLocation = ev.getResult();
//                       Log.e("MAPA", "Lat: " + mLastKnownLocation.getLatitude());
//                       Log.e("MAPA", "Lng: " + mLastKnownLocation.getLongitude());
//
//                       LatLng miPosicion = new LatLng(
//                               mLastKnownLocation.getLatitude(),
//                               mLastKnownLocation.getLongitude()
//                       );
//
//                       mapa.addMarker(new MarkerOptions()
//                               .position(miPosicion)
//                               .title("Mi ubicación"));
//
//                       mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15f));
//
////                       mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
////                               new LatLng(mLastKnownLocation.getLatitude(),
////                               mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                   } else{
//                       Log.e(this.getLocalClassName(), "La ubicación actual es null. Usando valor por defecto.", ev.getException());
//                       mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//                       mapa.getUiSettings().setMyLocationButtonEnabled(false);
//                   }
//
//                });
//            }
//
//        } catch(SecurityException e){
//            Log.e("Error al acceder a la ubicación actual del dispositivo. ", e.getMessage());
//        }
//    }
}