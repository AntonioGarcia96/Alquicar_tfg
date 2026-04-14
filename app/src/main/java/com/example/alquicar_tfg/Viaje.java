package com.example.alquicar_tfg;

public class Viaje {
    private String fecha;
    private int duracionMinutos;
    private double kmRecorridos;
    private String horaInicio;
    private String horaFin;
    private double costeTotal;

    public Viaje(String fecha, int duracionMinutos, double kmRecorridos, String horaInicio, String horaFin, double costeTotal) {
        this.fecha = fecha;
        this.duracionMinutos = duracionMinutos;
        this.kmRecorridos = kmRecorridos;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.costeTotal = costeTotal;
    }

    // Getters
    public String getFecha() { return fecha; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public double getKmRecorridos() { return kmRecorridos; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
    public double getCosteTotal() { return costeTotal; }
}