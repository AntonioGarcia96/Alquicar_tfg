package com.example.alquicar_tfg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ViajeAdapter extends RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder> {

    private List<Viaje> listaViajes;

    public ViajeAdapter(List<Viaje> listaViajes) {
        this.listaViajes = listaViajes;
    }

    @NonNull
    @Override
    public ViajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_viaje, parent, false);
        return new ViajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViajeViewHolder holder, int position) {
        Viaje viaje = listaViajes.get(position);

        holder.tvFecha.setText(viaje.getFecha());
        holder.tvDuracion.setText("⏱ Duración: " + viaje.getDuracionMinutos() + " min");
        holder.tvKm.setText("KM recorridos: " + viaje.getKmRecorridos() + "km");
        holder.tvHoraInicio.setText("Hora de inicio: " + viaje.getHoraInicio());
        holder.tvHoraFin.setText("Hora de fin: " + viaje.getHoraFin());
        holder.tvTotal.setText(String.format("Total: %.2f€", viaje.getCosteTotal()));
    }

    @Override
    public int getItemCount() {
        return listaViajes.size();
    }

    public static class ViajeViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvDuracion, tvKm, tvHoraInicio, tvHoraFin, tvTotal;

        public ViajeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaViaje);
            tvDuracion = itemView.findViewById(R.id.tvDuracionViaje);
            tvKm = itemView.findViewById(R.id.tvKmViaje);
            tvHoraInicio = itemView.findViewById(R.id.tvHoraInicioViaje);
            tvHoraFin = itemView.findViewById(R.id.tvHoraFinViaje);
            tvTotal = itemView.findViewById(R.id.tvTotalViaje);
        }
    }
}