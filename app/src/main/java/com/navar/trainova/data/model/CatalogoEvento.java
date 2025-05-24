package com.navar.trainova.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.DocumentId;

/**
 * Modelo que representa una plantilla de evento, ya sea general o personal.
 */
public class CatalogoEvento implements Parcelable {

    @DocumentId
    private String id;

    private String nombreEvento;
    private String descripcion;
    private String duracion;
    private String tipoEvento;
    private int colorEvento;
    private String uidCreador;

    public CatalogoEvento() {
    }

    public CatalogoEvento(String nombreEvento, String descripcion, String duracion, String tipoEvento, int colorEvento, String uidCreador) {
        this.nombreEvento = nombreEvento;
        this.descripcion = descripcion;
        this.duracion = duracion;
        this.tipoEvento = tipoEvento;
        this.colorEvento = colorEvento;
        this.uidCreador = uidCreador;
    }

    public String getId() { return id; }
    public String getNombreEvento() { return nombreEvento; }
    public String getDescripcion() { return descripcion; }
    public String getDuracion() { return duracion; }
    public String getTipoEvento() { return tipoEvento; }
    public int getColorEvento() { return colorEvento; }
    public String getUidCreador() { return uidCreador; }

    public void setId(String id) { this.id = id; }
    public void setNombreEvento(String nombreEvento) { this.nombreEvento = nombreEvento; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public void setColorEvento(int colorEvento) { this.colorEvento = colorEvento; }
    public void setUidCreador(String uidCreador) { this.uidCreador = uidCreador; }

    protected CatalogoEvento(Parcel in) {
        id = in.readString();
        nombreEvento = in.readString();
        descripcion = in.readString();
        duracion = in.readString();
        tipoEvento = in.readString();
        colorEvento = in.readInt();
        uidCreador = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nombreEvento);
        dest.writeString(descripcion);
        dest.writeString(duracion);
        dest.writeString(tipoEvento);
        dest.writeInt(colorEvento);
        dest.writeString(uidCreador);
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