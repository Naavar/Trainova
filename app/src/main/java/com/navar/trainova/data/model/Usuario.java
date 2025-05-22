package com.navar.trainova.data.model; // Asumo que quieres mantener el mismo paquete

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa un usuario dentro de la aplicación.
 * Esta clase se utiliza para gestionar la información del perfil del usuario,
 * incluyendo sus datos personales, fecha de registro y preferencias.
 * También implementa Parcelable para poder pasar objetos Usuario entre
 * actividades o fragmentos en Android.
 */
public class Usuario implements Parcelable {

    /** Identificador único del usuario, generalmente proporcionado por el sistema de autenticación (ej. Firebase UID). */
    private String uid;
    /** Nombre completo del usuario. */
    private String nombre;
    /** Dirección de correo electrónico del usuario. */
    private String email;
    /** Ubicación general del usuario (ej. ciudad, país). Podría expandirse a un objeto más detallado. */
    private String ubicacion;
    /** Fecha y hora en la que el usuario se registró en la aplicación. */
    private Date fechaRegistro;
    /**
     * Colección de preferencias del usuario. Las claves son identificadores de preferencia (String)
     * y los valores son booleanos que indican el estado de dicha preferencia
     * (ej. true si una actividad deportiva le gusta, o una configuración está activada).
     */
    private Map<String, Boolean> preferencias;

    /**
     * Constructor por defecto.
     * Requerido para la deserialización de Firebase Firestore (método toObject()) y para ciertas
     * operaciones de instanciación. Inicializa el mapa de preferencias.
     */
    public Usuario() {
        this.preferencias = new HashMap<>();
    }

    /**
     * Constructor para crear una instancia de Usuario con todos sus atributos.
     *
     * @param uid Identificador único del usuario.
     * @param nombre Nombre del usuario.
     * @param email Correo electrónico del usuario.
     * @param ubicacion Ubicación del usuario.
     * @param fechaRegistro Fecha de registro del usuario.
     * @param preferencias Mapa de preferencias del usuario (String -> Boolean). Si es nulo, se inicializará como un mapa vacío.
     */
    public Usuario(String uid, String nombre, String email, String ubicacion, Date fechaRegistro, Map<String, Boolean> preferencias) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.ubicacion = ubicacion;
        this.fechaRegistro = fechaRegistro;
        this.preferencias = (preferencias != null) ? preferencias : new HashMap<>();
    }

    /**
     * Obtiene el identificador único del usuario.
     * @return El UID del usuario.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Establece el identificador único del usuario.
     * @param uid El nuevo UID para el usuario.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Obtiene el nombre del usuario.
     * @return El nombre del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario.
     * @param nombre El nuevo nombre para el usuario.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     * @return El correo electrónico del usuario.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del usuario.
     * @param email El nuevo correo electrónico para el usuario.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la ubicación del usuario.
     * @return La ubicación del usuario.
     */
    public String getUbicacion() {
        return ubicacion;
    }

    /**
     * Establece la ubicación del usuario.
     * @param ubicacion La nueva ubicación para el usuario.
     */
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    /**
     * Obtiene la fecha de registro del usuario.
     * @return La fecha de registro.
     */
    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * Establece la fecha de registro del usuario.
     * @param fechaRegistro La nueva fecha de registro.
     */
    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Obtiene el mapa de preferencias del usuario (String -> Boolean).
     * @return Un mapa con las preferencias del usuario.
     */
    public Map<String, Boolean> getPreferencias() {
        return preferencias;
    }

    /**
     * Establece el mapa de preferencias del usuario (String -> Boolean).
     * @param preferencias El nuevo mapa de preferencias.
     */
    public void setPreferencias(Map<String, Boolean> preferencias) {
        this.preferencias = preferencias;
    }

    /**
     * Constructor para crear un Usuario a partir de un Parcel.
     * Utilizado por el sistema Android para la deserialización de Parcelable.
     * @param in El Parcel del que leer los datos del Usuario.
     */
    protected Usuario(Parcel in) {
        uid = in.readString();
        nombre = in.readString();
        email = in.readString();
        ubicacion = in.readString();
        long tmpFechaRegistro = in.readLong();
        fechaRegistro = tmpFechaRegistro == -1 ? null : new Date(tmpFechaRegistro);

        int preferenciasSize = in.readInt();
        this.preferencias = new HashMap<>(preferenciasSize);
        for (int i = 0; i < preferenciasSize; i++) {
            String key = in.readString();
            // Leemos el Boolean. readValue() devuelve Object, por lo que se necesita un cast.
            // Boolean.class.getClassLoader() ayuda a Parcel a saber qué tipo de objeto esperar.
            Boolean value = (Boolean) in.readValue(Boolean.class.getClassLoader());
            this.preferencias.put(key, value);
        }
    }

    /**
     * Escribe los datos del Usuario a un Parcel.
     * Utilizado por el sistema Android para la serialización de Parcelable.
     * @param dest El Parcel en el que escribir los datos del Usuario.
     * @param flags Banderas adicionales sobre cómo debe escribirse el objeto.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(nombre);
        dest.writeString(email);
        dest.writeString(ubicacion);
        dest.writeLong(fechaRegistro != null ? fechaRegistro.getTime() : -1);

        dest.writeInt(preferencias.size());
        // Iteramos sobre Map.Entry<String, Boolean>
        for (Map.Entry<String, Boolean> entry : preferencias.entrySet()) {
            dest.writeString(entry.getKey());
            // writeValue puede manejar objetos Boolean directamente.
            dest.writeValue(entry.getValue());
        }
    }

    /**
     * Describe los tipos de objetos especiales contenidos en la representación Parcelable.
     * @return Un valor de bits que indica los tipos de objetos especiales.
     */
    @Override
    public int describeContents() {
        return 0; // Normalmente 0 a menos que contenga un FileDescriptor u otro objeto especial.
    }

    /**
     * Objeto CREATOR estático requerido para la interfaz Parcelable.
     * Permite al sistema Android crear nuevas instancias de la clase Parcelable
     * a partir de un Parcel.
     */
    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        /**
         * Crea una nueva instancia de la clase Parcelable, instanciándola desde el Parcel dado.
         * @param in El Parcel del que leer los datos del objeto.
         * @return Una nueva instancia de Usuario.
         */
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        /**
         * Crea un nuevo array de la clase Parcelable.
         * @param size Tamaño del array.
         * @return Un array de objetos Usuario.
         */
        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    /**
     * Compara este Usuario con otro objeto para determinar si son iguales.
     * Dos usuarios se consideran iguales si sus UIDs son iguales.
     * @param o El objeto con el que comparar.
     * @return true si los objetos son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(uid, usuario.uid);
    }

    /**
     * Genera un código hash para este Usuario.
     * El código hash se basa en el UID del usuario.
     * @return El código hash del objeto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }

    /**
     * Devuelve una representación en cadena del objeto Usuario.
     * Útil para la depuración y el logging.
     * @return Una cadena que representa el objeto Usuario.
     */
    @Override
    public String toString() {
        return "Usuario{" +
            "uid='" + uid + '\'' +
            ", nombre='" + nombre + '\'' +
            ", email='" + email + '\'' +
            ", ubicacion='" + ubicacion + '\'' +
            ", fechaRegistro=" + fechaRegistro +
            ", preferencias=" + preferencias + // Esto ahora imprimirá Map<String, Boolean>
            '}';
    }
}