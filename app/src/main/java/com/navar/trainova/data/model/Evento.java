package com.navar.trainova.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.prolificinteractive.materialcalendarview.CalendarDay;

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

    // Constructor sin ID (genera automáticamente)
    public Evento(CalendarDay calendarDay, String nombre, String tipoActividad, int color,
                  String estado, String horaInicio, String horaFin, String descripcion, String userId) { // userId añadido
        this(UUID.randomUUID().toString(), calendarDay, nombre, tipoActividad, color,
            estado, horaInicio, horaFin, descripcion, userId); // userId pasado
    }

    // Constructor con ID (útil para actualizar/cargar de BD)
    public Evento(String id, CalendarDay calendarDay, String nombre, String tipoActividad, int color,
                  String estado, String horaInicio, String horaFin, String descripcion, String userId) { // userId añadido
        this.idEvento = (id != null) ? id : UUID.randomUUID().toString();
        this.calendarDay = (calendarDay != null) ? calendarDay : CalendarDay.today();
        this.nombre = (nombre != null) ? nombre : "";
        this.tipoActividad = (tipoActividad != null) ? tipoActividad : "General";
        this.color = color;
        this.estado = (estado != null) ? estado : "Pendiente";
        this.horaInicio = (horaInicio != null) ? horaInicio : "00:00";
        this.horaFin = (horaFin != null) ? horaFin : "00:00";
        this.descripcion = (descripcion != null) ? descripcion : "";
        this.uid = userId; // Asignar userId
    }

    // Getters
    public String getIdEvento() {
        return idEvento;
    }

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

    /**
     * Obtiene el identificador único del usuario propietario de este evento.
     * @return El ID del usuario.
     */
    public String getUid() {
        return uid;
    }


    public String getNombreMostrado() {
        if (tipoActividad == null || tipoActividad.isEmpty() || "General".equalsIgnoreCase(tipoActividad)) {
            return nombre;
        }
        return tipoActividad + ": " + nombre;
    }

    // Parcelable implementation
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
        uid = in.readString(); // Leer userId
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
        dest.writeString(uid); // Escribir userId
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
            Objects.equals(uid, evento.uid); // Incluir userId en la comparación
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEvento, uid); // Incluir userId en el hash
    }
}