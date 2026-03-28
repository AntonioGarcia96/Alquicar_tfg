package com.example.alquicar_tfg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //private BottomSheetDialog bottomSheetDialog;
    private GoogleMap mapa;
    TextView tvMatricula;
    TextView tvAutonomia;
    TextView tvPrecio;
    TextView tvBateria;
    Button btnReservar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

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

        LatLng sevilla = new LatLng(37.3891, -5.9845);
        LatLng coche1 = new LatLng(37.401066213722494, -5.975189283196233);

        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(sevilla, 13f));

        mapa.addMarker(new MarkerOptions()
                .position(coche1)
                .title("Seat Mii").snippet("1234TFG"));

        mapa.setOnMarkerClickListener(this);
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
}