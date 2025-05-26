package com.navar.trainova.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.firestore.Exclude;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un evento que ocurre en una fecha concreta del calendario.
 * Esta clase se usa para gestionar actividades dentro de una aplicación de entrenamiento
 * o planificación.
 * Incluye información como tipo de actividad, hora, estado, color, descripción y el
 * identificador del usuario propietario del evento.
 * También implementa Parceable para poder pasar objetos entre actividades o fragmentos en Android.
 */
public class Evento implements Parcelable {
    /** Identificador único del evento. Se genera automáticamente mediante UUID si no se especifica. */
    private final String idEvento;
    /** Fecha del calendario en la que se produce el evento.*/
    private final CalendarDay calendarDay;
    /** Nombre del evento/actividad. */
    private final String nombre;
    /** Para clasificar el evento (ej. "Cardio", "Resistencia", etc.). */
    private final String tipoActividad;
    /** El color asignado para el dia del evento */
    private int color;
    /** Estado actual del evento (ej. "Pendiente", "Completado", "Cancelado"). */
    private final String estado;
    /** Hora de inicio del evento, en formato de cadena (ej. "09:00"). */
    private final String horaInicio;
    /** Hora de finalización del evento, en formato de cadena (ej. "10:30"). */
    private final String horaFin;
    /** Detalles adicionales o notas sobre el evento. */
    private final String descripcion;
    /** Identificador único del usuario al que pertenece este evento. */
    private final String uid;
    /** fecha usada para Firestore */
    private Date fechaFirestore;

    public Evento(CalendarDay calendarDay, String nombre, String tipoActividad, int color,
                  String estado, String horaInicio, String horaFin, String descripcion, String userId) {
        this(UUID.randomUUID().toString(), calendarDay, nombre, tipoActividad, color,
            estado, horaInicio, horaFin, descripcion, userId);
    }

    public Evento(String id, CalendarDay calendarDay, String nombre, String tipoActividad, int color,
                  String estado, String horaInicio, String horaFin, String descripcion, String userId) {
        this.idEvento = (id != null) ? id : UUID.randomUUID().toString();
        this.calendarDay = (calendarDay != null) ? calendarDay : CalendarDay.today();
        this.nombre = (nombre != null) ? nombre : "";
        this.tipoActividad = (tipoActividad != null) ? tipoActividad : "General";
        this.color = color;
        this.estado = (estado != null) ? estado : "Pendiente";
        this.horaInicio = (horaInicio != null) ? horaInicio : "00:00";
        this.horaFin = (horaFin != null) ? horaFin : "00:00";
        this.descripcion = (descripcion != null) ? descripcion : "";
        this.uid = userId;

        if (this.calendarDay != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(this.calendarDay.getYear(), this.calendarDay.getMonth() - 1, this.calendarDay.getDay());
            this.fechaFirestore = cal.getTime();
        }
    }

    public String getIdEvento() {
        return idEvento;
    }

    @Exclude
    public CalendarDay getCalendarDay() {
        return calendarDay;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getEstado() {
        return estado;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUid() {
        return uid;
    }

    public Date getFechaFirestore() {
        return fechaFirestore;
    }

    public void setFechaFirestore(Date fechaFirestore) {
        this.fechaFirestore = fechaFirestore;
    }

    public String getNombreMostrado() {
        if (tipoActividad == null || tipoActividad.isEmpty() || "General".equalsIgnoreCase(tipoActividad)) {
            return nombre;
        }
        return tipoActividad + ": " + nombre;
    }

    protected Evento(Parcel in) {
        idEvento = in.readString();
        calendarDay = in.readParcelable(CalendarDay.class.getClassLoader());
        nombre = in.readString();
        tipoActividad = in.readString();
        color = in.readInt();
        estado = in.readString();
        horaInicio = in.readString();
        horaFin = in.readString();
        descripcion = in.readString();
        uid = in.readString();
        long tmpDate = in.readLong();
        this.fechaFirestore = tmpDate == -1 ? null : new Date(tmpDate);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idEvento);
        dest.writeParcelable(calendarDay, flags);
        dest.writeString(nombre);
        dest.writeString(tipoActividad);
        dest.writeInt(color);
        dest.writeString(estado);
        dest.writeString(horaInicio);
        dest.writeString(horaFin);
        dest.writeString(descripcion);
        dest.writeString(uid);
        dest.writeLong(fechaFirestore != null ? fechaFirestore.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Evento> CREATOR = new Creator<Evento>() {
        @Override
        public Evento createFromParcel(Parcel in) {
            return new Evento(in);
        }

        @Override
        public Evento[] newArray(int size) {
            return new Evento[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evento)) return false;
        Evento evento = (Evento) o;
        return idEvento.equals(evento.idEvento) &&
            Objects.equals(uid, evento.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEvento, uid);
    }
}