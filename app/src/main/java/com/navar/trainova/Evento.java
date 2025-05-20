package com.navar.trainova;

public class Evento {
    private final String nombre;
    private final int color;
    private String tipo = "General";
    private String estado = "Pendiente";
    private String horaInicio = "00:00";
    private String horaFin = "00:00";
    private String descripcion = "";

    public Evento(String nombre, int color) {
        this.nombre = nombre;
        this.color = color;
    }

    public Evento(String nombre, int color, String tipo, String estado,
                  String horaInicio, String horaFin, String descripcion) {
        this.nombre = nombre;
        this.color = color;
        this.tipo = tipo;
        this.estado = estado;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getColor() {
        return color;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}