package com.navar.trainova.data.repository;

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
    /** Nombre de tu colección de eventos  */
    private static final String COLLECTION_EVENTOS = "Evento";
    /**Nombre del campo para el ID de usuario en Firestore*/
    private static final String FIELD_USER_ID = "uid";     //

    private final FirebaseFirestore db;
    private final MutableLiveData<Map<CalendarDay, List<Evento>>> eventosGroupedByDayLiveData =
        new MutableLiveData<>(new HashMap<>());
    /** Para poder remover el listener */
    private ListenerRegistration eventosListenerRegistration;
    /** Para almacenar el UID del usuario actual */
    private String currentUid;

    public FirestoreEventoRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Establece el usuario actual y comienza a observar/cargar sus eventos desde Firestore.
     * Si uid es nulo o vacío, se dejan de observar eventos y se limpia el LiveData.
     * @param uid El ID del usuario cuyos eventos se cargarán.
     */
    public void loadAndObserveEventsForUser(@Nullable String uid) {
        // Remover el listener anterior si existe, para evitar múltiples listeners
        if (eventosListenerRegistration != null) {
            eventosListenerRegistration.remove();
            eventosListenerRegistration = null;
        }

        this.currentUid = uid;

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
            String uid = document.getString(FIELD_USER_ID);

            if (uid == null) {
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
                Log.e(TAG, "Documento de Evento con datos de fecha incompletos " +
                    "en calendarDayMap. ID: " + id);
                return null;
            }

            CalendarDay calendarDay = CalendarDay.from(year.intValue(), month.intValue(), day.intValue());

            return new Evento(id, calendarDay, nombre, tipo, color, estado, horaInicio, horaFin,
                descripcion, uid);
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento a Evento. ID: " + document.getId(), e);
            return null;
        }
    }

    private Map<String, Object> eventoToMap(Evento evento) {
        Map<String, Object> eventoMap = new HashMap<>();
        eventoMap.put("nombre", evento.getNombre());
        eventoMap.put("tipoActividad", evento.getTipoActividad());
        eventoMap.put("descripcion", evento.getDescripcion());
        eventoMap.put("horaInicio", evento.getHoraInicio());
        eventoMap.put("horaFin", evento.getHoraFin());
        eventoMap.put("estado", evento.getEstado());
        eventoMap.put("color", evento.getColor());
        eventoMap.put(FIELD_USER_ID, evento.getUid());

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
        if (evento.getUid() == null || evento.getUid().isEmpty()) {
            Log.e(TAG, "Error: El evento que se intenta añadir no tiene un Uid. Evento: " +
                evento.getNombre());
            return;
        }

        Map<String, Object> eventoMap = eventoToMap(evento);

        db.collection(COLLECTION_EVENTOS)
            .add(eventoMap)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Evento añadido con ID: " + documentReference.getId() +
                    " para usuario: " + evento.getUid());
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error añadiendo evento: " + evento.getNombre(), e));
    }

    @Override
    public void updateEvento(Evento evento) {
        if (evento.getIdEvento() == null || evento.getIdEvento().isEmpty()) {
            Log.e(TAG, "Error: El evento que se intenta actualizar no tiene un idEvento. Evento: " +
                evento.getNombre());
            return;
        }
        if (evento.getUid() == null || evento.getUid().isEmpty()) {
            Log.e(TAG, "Error: El evento que se intenta actualizar no tiene un Uid. Evento: " +
                evento.getNombre());
            return;
        }

        DocumentReference documentRef = db.collection(COLLECTION_EVENTOS).document(evento.getIdEvento());
        documentRef.set(eventoToMap(evento))
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Evento actualizado: " + evento.getIdEvento() + " para usuario: " +
                    evento.getUid());
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error actualizando evento: " +
                evento.getIdEvento(), e));
    }

    @Override
    public void deleteEvento(String eventoId) {
        if (eventoId == null || eventoId.isEmpty()) {
            Log.e(TAG, "Error: Se intentó borrar un evento con ID nulo o vacío.");
            return;
        }
        db.collection(COLLECTION_EVENTOS).document(eventoId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Evento borrado: " + eventoId);
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error borrando evento: " + eventoId, e));
    }

    @Override
    public Evento findEventoById(String eventoId) {
        if (eventoId == null) return null;
        Map<CalendarDay, List<Evento>> currentMap = eventosGroupedByDayLiveData.getValue();
        if (currentMap != null) {
            for (List<Evento> eventosDelDia : currentMap.values()) {
                for (Evento evento : eventosDelDia) {
                    if (eventoId.equals(evento.getIdEvento())) {
                        // Verificación adicional
                        if (this.currentUid != null && this.currentUid.equals(evento.getUid())) {
                            return evento;
                        } else {
                            Log.w(TAG, "findEventoById encontró un evento ("+eventoId+")" +
                                " pero su Uid ("+evento.getUid()+") no coincide con el usuario actual" +
                                " ("+this.currentUid+").");
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void removeListeners() {
        if (eventosListenerRegistration != null) {
            eventosListenerRegistration.remove();
            eventosListenerRegistration = null;
            Log.d(TAG, "Listener de eventos removido.");
        }
    }
}