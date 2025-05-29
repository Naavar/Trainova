package com.navar.trainova.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.DocumentId;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa una plantilla de evento o rutina, ya sea general o personal.
 * Puede contener una lista de ejercicios específicos si es una plantilla de rutina.
 * Incluye un campo para el nombre del color (nombreColor) que la IA puede sugerir,
 * y un campo para el entero del color (colorEvento) que usa la app.
 */
public class CatalogoEvento implements Parcelable {

    @DocumentId
    private String id;

    @SerializedName("nombre")
    private String nombreEvento;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("duracion")
    private String duracion;

    @SerializedName("tipo")
    private String tipoEvento;

    /**
     * Almacena el nombre del color como lo podría devolver la IA (ej. "Naranja", "Rojo").
     * Este campo es el que se mapeará desde el JSON con la clave "nombreColor".
     */
    @SerializedName("nombreColor")
    private String nombreColor;

    /**
     * Almacena el valor entero del color ARGB de Android.
     * Este campo se poblará en la lógica de la app convirtiendo el nombreColor,
     * o directamente si la plantilla se crea/edita manualmente con un selector de color.
     * La anotación @SerializedName("color") se mantiene por si en algún contexto
     * se necesitara serializar/deserializar directamente un entero de color.
     */
    @SerializedName("color")
    private int colorEvento;

    private String uid;

    @SerializedName("ejercicios")
    private List<EjercicioPlantilla> ejercicios;

    /**
     * Constructor vacío requerido por Firestore y Gson para la deserialización.
     * Inicializa la lista de ejercicios.
     */
    public CatalogoEvento() {
        this.ejercicios = new ArrayList<>();
    }

    /**
     * Constructor para crear una instancia de CatalogoEvento con campos básicos.
     * Usado principalmente cuando se crea una plantilla manualmente o se copia.
     * El campo nombreColor puede ser nulo si el color se establece directamente como entero.
     * @param nombreEvento Nombre de la plantilla del evento.
     * @param descripcion Descripción detallada de la plantilla.
     * @param duracion Duración estimada del evento o rutina.
     * @param tipoEvento Tipo o categoría del evento.
     * @param colorEvento Valor entero ARGB del color representativo para la UI.
     * @param uid ID del usuario que creó esta plantilla (si es personal).
     * @param ejercicios Lista de ejercicios que componen la plantilla de rutina.
     */
    public CatalogoEvento(String nombreEvento, String descripcion, String duracion,
                          String tipoEvento, int colorEvento, String uid,
                          List<EjercicioPlantilla> ejercicios) {
        this.nombreEvento = nombreEvento;
        this.descripcion = descripcion;
        this.duracion = duracion;
        this.tipoEvento = tipoEvento;
        this.colorEvento = colorEvento;
        this.uid = uid;
        this.ejercicios = (ejercicios != null) ? ejercicios : new ArrayList<>();
    }


    /**
     * Obtiene el ID único del documento de Firestore.
     * @return El ID del documento.
     */
    public String getId() { return id; }

    /**
     * Establece el ID único del documento.
     * @param id El nuevo ID del documento.
     */
    public void setId(String id) { this.id = id; }

    /**
     * Obtiene el nombre de la plantilla del evento.
     * @return El nombre de la plantilla.
     */
    public String getNombreEvento() { return nombreEvento; }

    /**
     * Establece el nombre de la plantilla del evento.
     * @param nombreEvento El nuevo nombre para la plantilla.
     */
    public void setNombreEvento(String nombreEvento) { this.nombreEvento = nombreEvento; }

    /**
     * Obtiene la descripción de la plantilla.
     * @return La descripción.
     */
    public String getDescripcion() { return descripcion; }

    /**
     * Establece la descripción de la plantilla.
     * @param descripcion La nueva descripción.
     */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /**
     * Obtiene la duración estimada del evento.
     * @return La duración como texto (ej: "30 minutos").
     */
    public String getDuracion() { return duracion; }

    /**
     * Establece la duración estimada del evento.
     * @param duracion La nueva duración.
     */
    public void setDuracion(String duracion) { this.duracion = duracion; }

    /**
     * Obtiene el tipo o categoría del evento.
     * @return El tipo de evento.
     */
    public String getTipoEvento() { return tipoEvento; }

    /**
     * Establece el tipo o categoría del evento.
     * @param tipoEvento El nuevo tipo de evento.
     */
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    /**
     * Obtiene el nombre del color (ej. "Naranja", "Rojo") como podría ser devuelto por la IA.
     * Este campo es usado por Gson para mapear la clave "nombreColor" del JSON.
     * @return El nombre del color.
     */
    public String getNombreColor() { return nombreColor; }

    /**
     * Establece el nombre del color.
     * @param nombreColor El nombre del color.
     */
    public void setNombreColor(String nombreColor) { this.nombreColor = nombreColor; }

    /**
     * Obtiene el valor entero ARGB del color asociado a la plantilla para la UI.
     * @return Un entero representando el color ARGB.
     */
    public int getColorEvento() { return colorEvento; }

    /**
     * Establece el valor entero ARGB del color asociado a la plantilla.
     * @param colorEvento El nuevo color como un entero ARGB.
     */
    public void setColorEvento(int colorEvento) { this.colorEvento = colorEvento; }

    /**
     * Obtiene el ID del usuario que creó la plantilla (si aplica).
     * @return El UID del creador.
     */
    public String getUid() { return uid; }

    /**
     * Establece el ID del usuario creador.
     * @param uid El UID del creador.
     */
    public void setUid(String uid) { this.uid = uid; }

    /**
     * Obtiene la lista de ejercicios asociados a esta plantilla de rutina.
     * @return Una lista de objetos EjercicioPlantilla.
     */
    public List<EjercicioPlantilla> getEjercicios() { return ejercicios; }

    /**
     * Establece la lista de ejercicios para esta plantilla de rutina.
     * @param ejercicios La nueva lista de ejercicios.
     */
    public void setEjercicios(List<EjercicioPlantilla> ejercicios) { this.ejercicios = ejercicios; }


    protected CatalogoEvento(Parcel in) {
        id = in.readString();
        nombreEvento = in.readString();
        descripcion = in.readString();
        duracion = in.readString();
        tipoEvento = in.readString();
        nombreColor = in.readString();
        colorEvento = in.readInt();
        uid = in.readString();
        ejercicios = new ArrayList<>();
        in.readTypedList(ejercicios, EjercicioPlantilla.CREATOR);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nombreEvento);
        dest.writeString(descripcion);
        dest.writeString(duracion);
        dest.writeString(tipoEvento);
        dest.writeString(nombreColor);
        dest.writeInt(colorEvento);
        dest.writeString(uid);
        dest.writeTypedList(ejercicios);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CatalogoEvento> CREATOR = new Creator<CatalogoEvento>() {
        @Override
        public CatalogoEvento createFromParcel(Parcel in) {
            return new CatalogoEvento(in);
        }

        @Override
        public CatalogoEvento[] newArray(int size) {
            return new CatalogoEvento[size];
        }
    };
}