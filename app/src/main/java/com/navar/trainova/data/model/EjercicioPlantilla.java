package com.navar.trainova.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

/**
 * Representa un ejercicio específico dentro de una plantilla de catálogo de eventos.
 * Contiene detalles como el nombre, series, repeticiones, descanso y notas adicionales.
 */
public class EjercicioPlantilla implements Parcelable {

    @SerializedName("nombreEjercicio")
    private String nombreEjercicio;

    @SerializedName("series")
    private int series;

    @SerializedName("repeticiones")
    private String repeticiones;

    @SerializedName("descanso")
    private String descanso;

    @SerializedName("notas")
    private String notas;

    /**
     * Constructor vacío requerido por Firestore y Gson para la deserialización.
     */
    public EjercicioPlantilla() {
    }

    /**
     * Constructor para crear una instancia de EjercicioPlantilla con todos sus campos.
     * @param nombreEjercicio Nombre del ejercicio.
     * @param series Número de series.
     * @param repeticiones Rango o cantidad de repeticiones (puede ser texto como "Al fallo").
     * @param descanso Tiempo de descanso entre series (puede ser texto como "60s").
     * @param notas Notas o consejos adicionales para el ejercicio.
     */
    public EjercicioPlantilla(String nombreEjercicio, int series, String repeticiones,
                              String descanso, String notas) {
        this.nombreEjercicio = nombreEjercicio;
        this.series = series;
        this.repeticiones = repeticiones;
        this.descanso = descanso;
        this.notas = notas;
    }

    /**
     * Obtiene el nombre del ejercicio.
     * @return El nombre del ejercicio.
     */
    public String getNombreEjercicio() { return nombreEjercicio; }

    /**
     * Establece el nombre del ejercicio.
     * @param nombreEjercicio El nuevo nombre para el ejercicio.
     */
    public void setNombreEjercicio(String nombreEjercicio) { this.nombreEjercicio = nombreEjercicio; }

    /**
     * Obtiene el número de series para el ejercicio.
     * @return El número de series.
     */
    public int getSeries() { return series; }

    /**
     * Establece el número de series para el ejercicio.
     * @param series El nuevo número de series.
     */
    public void setSeries(int series) { this.series = series; }

    /**
     * Obtiene la descripción de las repeticiones (ej: "10-12", "Al fallo").
     * @return La descripción de las repeticiones.
     */
    public String getRepeticiones() { return repeticiones; }

    /**
     * Establece la descripción de las repeticiones.
     * @param repeticiones La nueva descripción para las repeticiones.
     */
    public void setRepeticiones(String repeticiones) { this.repeticiones = repeticiones; }

    /**
     * Obtiene el tiempo de descanso (ej: "60s", "1-2 min").
     * @return El tiempo de descanso.
     */
    public String getDescanso() { return descanso; }

    /**
     * Establece el tiempo de descanso.
     * @param descanso El nuevo tiempo de descanso.
     */
    public void setDescanso(String descanso) { this.descanso = descanso; }

    /**
     * Obtiene las notas o consejos adicionales para el ejercicio.
     * @return Las notas adicionales.
     */
    public String getNotas() { return notas; }

    /**
     * Establece las notas o consejos adicionales para el ejercicio.
     * @param notas Las nuevas notas.
     */
    public void setNotas(String notas) { this.notas = notas; }


    // Implementación de Parcelable
    protected EjercicioPlantilla(Parcel in) {
        nombreEjercicio = in.readString();
        series = in.readInt();
        repeticiones = in.readString();
        descanso = in.readString();
        notas = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(nombreEjercicio);
        dest.writeInt(series);
        dest.writeString(repeticiones);
        dest.writeString(descanso);
        dest.writeString(notas);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EjercicioPlantilla> CREATOR = new Creator<EjercicioPlantilla>() {
        @Override
        public EjercicioPlantilla createFromParcel(Parcel in) {
            return new EjercicioPlantilla(in);
        }

        @Override
        public EjercicioPlantilla[] newArray(int size) {
            return new EjercicioPlantilla[size];
        }
    };
}