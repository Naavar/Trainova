package com.navar.trainova.data.model;

/**
 * Representa una opción de color para mostrar en un spinner.
 * Cada opción tiene un nombre (por ejemplo, "Rojo") y un valor entero
 * que representa el color.
 */
public class ColorOption {
    public String name;
    public int colorValue;

    public ColorOption(String name, int colorValue) {
        this.name = name;
        this.colorValue = colorValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColorValue() {
        return colorValue;
    }

    public void setColorValue(int colorValue) {
        this.colorValue = colorValue;
    }
}
