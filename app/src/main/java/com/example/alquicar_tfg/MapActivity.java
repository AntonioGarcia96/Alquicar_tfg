package com.example.alquicar_tfg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MapActivity extends AppCompatActivity {

    private BottomSheetDialog bottomSheetDialog;
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
}