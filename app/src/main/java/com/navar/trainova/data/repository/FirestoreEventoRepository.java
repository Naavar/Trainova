package com.navar.trainova.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.navar.trainova.data.model.Evento;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreEventoRepository implements EventoRepository {
    private static final String TAG = "FirestoreEventoRepo";
    private static final String COLLECTION_EVENTOS = "Evento"; // Nombre de tu colección de eventos
    private static final String FIELD_USER_ID = "uid";     // Nombre del campo para el ID de usuario en Firestore

    private final FirebaseFirestore db;
    private final MutableLiveData<Map<CalendarDay, List<Evento>>> eventosGroupedByDayLiveData = new MutableLiveData<>(new HashMap<>());
    private ListenerRegistration eventosListenerRegistration; // Para poder remover el listener
    private String currentUid; // Para almacenar el UID del usuario actual

    public FirestoreEventoRepository() {
        db = FirebaseFirestore.getInstance();
        // La carga de eventos ya no se hace automáticamente en el constructor.
        // Se llamará a loadAndObserveEventsForUser(uid) desde el ViewModel.
    }

    /**
     * Establece el usuario actual y comienza a observar/cargar sus eventos desde Firestore.
     * Si uid es nulo o vacío, se dejan de observar eventos y se limpia el LiveData.
     * @param uid El ID del usuario cuyos eventos se cargarán.
     */
    public void loadAndObserveEventsForUser(@Nullable String uid) {
        // Remover el listener anterior si existe, para evitar múltiples listeners o fugas de memoria
        if (eventosListenerRegistration != null) {
            eventosListenerRegistration.remove();
            eventosListenerRegistration = null;
        }

        this.currentUid = uid; // Actualiza el uid actual

        if (uid == null || uid.isEmpty()) {
            Log.w(TAG, "UID de usuario es nulo o vacío. Limpiando eventos y deteniendo escucha.");
            eventosGroupedByDayLiveData.postValue(new HashMap<>()); // Limpiar LiveData
            return;
        }

        Log.d(TAG, "Iniciando escucha de eventos para el usuario: " + uid);
        Query query = db.collection(COLLECTION_EVENTOS).whereEqualTo(FIELD_USER_ID, uid);

        eventosListenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Error escuchando los cambios en Firestore.", e);
                    // Podrías querer postear un mapa vacío o un estado de error al LiveData aquí
                    eventosGroupedByDayLiveData.postValue(new HashMap<>());
                    return;
                }

                Map<CalendarDay, List<Evento>> eventosMap = new HashMap<>();
                if (snapshots != null) {
                    for (QueryDocumentSnapshot document : snapshots) {
                        Evento evento = documentToEvento(document);
                        if (evento != null) {
                            CalendarDay day = evento.getCalendarDay();
                            eventosMap.computeIfAbsent(day, k -> new ArrayList<>()).add(evento);
                        }
                    }
                }
                eventosGroupedByDayLiveData.postValue(eventosMap);
                Log.d(TAG, "Eventos actualizados desde Firestore para el usuario: " + currentUid +
                    ". Días con eventos: " + eventosMap.size());
            }
        });
    }

    private Evento documentToEvento(DocumentSnapshot document) {
        try {
            String id = document.getId();
            String nombre = document.getString("nombre");
            String tipo = document.getString("tipoActividad");
            String descripcion = document.getString("descripcion");
            String horaInicio = document.getString("horaInicio");
            String horaFin = document.getString("horaFin");
            String estado = document.getString("estado");
            String uid = document.getString(FIELD_USER_ID); // OBTENER uid del documento

            if (uid == null) { // Un evento siempre debe tener un uid
                Log.e(TAG, "Documento de Evento sin uid. ID del documento: " + id);
                return null;
            }

            Long colorLong = document.getLong("color");
            int color = colorLong != null ? colorLong.intValue() : 0;

            Map<String, Object> calendarDayMap = (Map<String, Object>) document.get("calendarDay");
            if (calendarDayMap == null) {
                Log.e(TAG, "Documento de Evento sin calendarDayMap. ID: " + id);
                return null;
            }

            Long year = (Long) calendarDayMap.get("year");
            Long month = (Long) calendarDayMap.get("month");
            Long day = (Long) calendarDayMap.get("day");

            if (year == null || month == null || day == null) {
                Log.e(TAG, "Documento de Evento con datos de fecha incompletos en calendarDayMap. ID: " + id);
                return null;
            }

            CalendarDay calendarDay = CalendarDay.from(year.intValue(), month.intValue(), day.intValue());

            // Usar el constructor de Evento que ahora incluye uid
            return new Evento(id, calendarDay, nombre, tipo, color, estado, horaInicio, horaFin, descripcion, uid);
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a Evento. ID: " + document.getId(), e);
            return null;
        }
    }

    private Map<String, Object> eventoToMap(Evento evento) {
        Map<String, Object> eventoMap = new HashMap<>();
        // No es necesario guardar idEvento aquí si es el ID del documento.
        // Firestore lo maneja. Si quieres guardar tu UUID interno como campo aparte, puedes hacerlo.
        eventoMap.put("nombre", evento.getNombre());
        eventoMap.put("tipoActividad", evento.getTipoActividad());
        eventoMap.put("descripcion", evento.getDescripcion());
        eventoMap.put("horaInicio", evento.getHoraInicio());
        eventoMap.put("horaFin", evento.getHoraFin());
        eventoMap.put("estado", evento.getEstado());
        eventoMap.put("color", evento.getColor());
        eventoMap.put(FIELD_USER_ID, evento.getUid()); // GUARDAR uid del objeto Evento

        CalendarDay day = evento.getCalendarDay();
        Map<String, Integer> calendarDayMap = new HashMap<>();
        calendarDayMap.put("year", day.getYear());
        calendarDayMap.put("month", day.getMonth());
        calendarDayMap.put("day", day.getDay());
        eventoMap.put("calendarDay", calendarDayMap);

        return eventoMap;
    }

    @Override
    public LiveData<Map<CalendarDay, List<Evento>>> getAllEventosGroupedByDay() {
        return eventosGroupedByDayLiveData;
    }

    @Override
    public LiveData<List<Evento>> getEventosForDay(CalendarDay day) {
        MutableLiveData<List<Evento>> liveData = new MutableLiveData<>();
        Map<CalendarDay, List<Evento>> currentMap = eventosGroupedByDayLiveData.getValue();
        if (currentMap != null && currentMap.containsKey(day)) {
            liveData.setValue(new ArrayList<>(currentMap.get(day)));
        } else {
            liveData.setValue(new ArrayList<>());
        }
        return liveData;
    }

    @Override
    public void addEvento(Evento evento) {
        // Se asume que el objeto 'evento' que llega ya tiene el 'uid' correcto.
        // El ViewModel es responsable de asignar el uid del usuario logueado al crear el Evento.
        if (evento.getUid() == null || evento.getUid().isEmpty()) {
            Log.e(TAG, "Error: El evento que se intenta añadir no tiene un Uid. Evento: " + evento.getNombre());
            // Considera no continuar si el Uid es inválido.
            return;
        }
        // No es necesario asignar this.currentUid aquí si el objeto Evento ya lo tiene.
        // Solo asegúrate de que el evento que se pasa al `eventoToMap` tenga el Uid.

        Map<String, Object> eventoMap = eventoToMap(evento);

        db.collection(COLLECTION_EVENTOS)
            .add(eventoMap) // Firestore genera el ID del documento
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Evento añadido con ID: " + documentReference.getId() + " para usuario: " + evento.getUid());
                // No es necesario llamar a loadEventosFromFirestore() aquí
                // si loadAndObserveEventsForUser está usando addSnapshotListener,
                // ya que el listener detectará automáticamente el nuevo evento.
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error añadiendo evento: " + evento.getNombre(), e));
    }

    @Override
    public void updateEvento(Evento evento) {
        // Se asume que 'evento' tiene el idEvento (ID del documento) y el Uid correctos.
        // Las reglas de seguridad de Firestore deben verificar la propiedad.
        if (evento.getIdEvento() == null || evento.getIdEvento().isEmpty()) {
            Log.e(TAG, "Error: El evento que se intenta actualizar no tiene un idEvento. Evento: " + evento.getNombre());
            return;
        }
        if (evento.getUid() == null || evento.getUid().isEmpty()) {
            Log.e(TAG, "Error: El evento que se intenta actualizar no tiene un Uid. Evento: " + evento.getNombre());
            return;
        }

        DocumentReference documentRef = db.collection(COLLECTION_EVENTOS).document(evento.getIdEvento());
        documentRef.set(eventoToMap(evento)) // Usar set para sobrescribir el documento completo.
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Evento actualizado: " + evento.getIdEvento() + " para usuario: " + evento.getUid());
                // addSnapshotListener se encargará de actualizar el LiveData.
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error actualizando evento: " + evento.getIdEvento(), e));
    }

    @Override
    public void deleteEvento(String eventoId) {
        if (eventoId == null || eventoId.isEmpty()) {
            Log.e(TAG, "Error: Se intentó borrar un evento con ID nulo o vacío.");
            return;
        }
        // Las reglas de seguridad de Firestore deben asegurar que solo el propietario puede borrar.
        db.collection(COLLECTION_EVENTOS).document(eventoId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Evento borrado: " + eventoId);
                // addSnapshotListener se encargará de actualizar el LiveData.
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error borrando evento: " + eventoId, e));
    }

    @Override
    public Evento findEventoById(String eventoId) {
        // Esta implementación busca en los datos locales que ya están (o deberían estar)
        // filtrados para el usuario actual debido a loadAndObserveEventsForUser.
        if (eventoId == null) return null;
        Map<CalendarDay, List<Evento>> currentMap = eventosGroupedByDayLiveData.getValue();
        if (currentMap != null) {
            for (List<Evento> eventosDelDia : currentMap.values()) {
                for (Evento evento : eventosDelDia) {
                    if (eventoId.equals(evento.getIdEvento())) {
                        // Verificación adicional (opcional aquí, ya que los datos deberían estar filtrados)
                        if (this.currentUid != null && this.currentUid.equals(evento.getUid())) {
                            return evento;
                        } else {
                            Log.w(TAG, "findEventoById encontró un evento ("+eventoId+") pero su Uid ("+evento.getUid()+") no coincide con el usuario actual ("+this.currentUid+").");
                            return null; // O manejar como un error/no encontrado.
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void loadInitialData(Context context) {
        // Este método ahora solo debería cargar datos si el usuario actual no tiene ninguno.
        if (this.currentUid == null || this.currentUid.isEmpty()) {
            Log.w(TAG, "No se puede cargar datos iniciales: currentUid no está establecido.");
            return;
        }

        // Comprueba si el usuario actual ya tiene eventos.
        db.collection(COLLECTION_EVENTOS)
            .whereEqualTo(FIELD_USER_ID, this.currentUid)
            .limit(1) // Solo necesitamos saber si la colección está vacía para este usuario.
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().isEmpty()) {
                        // Si el usuario no tiene eventos, carga algunos de muestra para él.
                        Log.d(TAG, "No hay eventos para el usuario: " + this.currentUid + ". Cargando datos de muestra.");
                        loadSampleDataForCurrentUser();
                    } else {
                        Log.d(TAG, "El usuario " + this.currentUid + " ya tiene eventos o la tarea no devolvió resultado. No se cargan datos de muestra.");
                    }
                } else {
                    Log.w(TAG, "Error al comprobar datos iniciales para el usuario: " + this.currentUid, task.getException());
                }
            });
    }

    private void loadSampleDataForCurrentUser() {
        // Este método ahora usa this.currentUid
        if (this.currentUid == null || this.currentUid.isEmpty()) {
            Log.e(TAG, "No se pueden cargar datos de muestra, currentUid es nulo o vacío.");
            return;
        }

        // Crear algunos eventos de muestra si no hay ninguno en Firestore para el usuario actual
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1; // Calendar.MONTH es 0-indexado

        // Asegúrate de que el constructor de Evento acepte Uid como último parámetro
        CalendarDay day1 = CalendarDay.from(year, currentMonth, 12);
        addEvento(new Evento(day1, "Reunión de Equipo", "Trabajo", 0xFF00796B, "Pendiente", "10:00", "11:00", "Discutir avances del proyecto.", this.currentUid));

        CalendarDay day2 = CalendarDay.from(year, currentMonth, 14);
        addEvento(new Evento(day2, "Entrenamiento Pierna", "Gym", 0xFFD32F2F, "Pendiente", "18:00", "19:00", "Sentadillas, prensa, extensiones.", this.currentUid));
        addEvento(new Evento(day2, "Estudiar Firestore", "Personal", 0xFF512DA8, "Completado", "20:00", "21:00", "Revisar documentación.", this.currentUid));
        Log.d(TAG, "Datos de muestra cargados para el usuario: " + this.currentUid);
    }
}