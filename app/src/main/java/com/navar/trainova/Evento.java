package com.navar.trainova;

public class Evento {
    public String etiqueta;
    public int color;

    public Evento(String etiqueta, int color) {
        this.etiqueta = etiqueta;
        this.color = color;
    }
    public Evento() {
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getColor() {
        return color;
    }
}

