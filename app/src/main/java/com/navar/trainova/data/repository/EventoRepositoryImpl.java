package com.navar.trainova.data.repository;

import android.content.Context;
import android.graphics.Color;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.navar.trainova.data.model.Evento;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación en memoria del EventoRepository.
 * Utiliza estructuras de datos locales (HashMaps) para almacenar y gestionar los eventos.
 * Los cambios en los eventos se reflejan en un {@link LiveData} para que la UI pueda observarlos.
 * Esta implementación es útil para pruebas, demos o cuando no se necesita persistencia de datos.
 * Las operaciones que modifican datos están sincronizadas para la seguridad en hilos.
 */
public class EventoRepositoryImpl implements EventoRepository {

    private final Map<String, Evento> allEventosById = new HashMap<>(); // ID -> Evento
    private final MutableLiveData<Map<CalendarDay, List<Evento>>> eventosGroupedByDayLiveData = new MutableLiveData<>(new HashMap<>());

    private boolean initialDataLoaded = false;

    @Override
    public synchronized void loadInitialData(Context context) {
        if (initialDataLoaded) return;
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1;

        int colorNaranja = Color.parseColor("#FFA000");
        int colorRojo = Color.parseColor("#F44336");

        CalendarDay day1 = CalendarDay.from(year, currentMonth, 12);
        addEventoInternal(new Evento(day1, "Convocatoria", "Trabajo", colorNaranja, "Pendiente", "10:00", "11:00", "Reunión importante"));

        CalendarDay day2 = CalendarDay.from(year, currentMonth, 14);
        addEventoInternal(new Evento(day2, "Entrega Interfaz", "Proyecto", colorRojo, "Pendiente", "14:00", "15:00", "Entrega final"));
        addEventoInternal(new Evento(day2, "Prueba", "Test", colorRojo, "Completado", "16:00", "17:00", "Test de unidad"));

        initialDataLoaded = true;
        updateLiveData();
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

    private void addEventoInternal(Evento evento) {
        allEventosById.put(evento.getId(), evento);
    }

    @Override
    public synchronized void addEvento(Evento evento) {
        addEventoInternal(evento);
        updateLiveData();
    }

    @Override
    public synchronized void updateEvento(Evento evento) {
        if (allEventosById.containsKey(evento.getId())) {
            allEventosById.put(evento.getId(), evento); // Replace existing
            updateLiveData();
        }
    }

    @Override
    public synchronized void deleteEvento(String eventoId) {
        if (allEventosById.containsKey(eventoId)) {
            allEventosById.remove(eventoId);
            updateLiveData();
        }
    }

    @Override
    public Evento findEventoById(String eventoId) {
        return allEventosById.get(eventoId);
    }

    private synchronized void updateLiveData() {
        Map<CalendarDay, List<Evento>> newGroupedMap = new HashMap<>();
        for (Evento evento : allEventosById.values()) {
            newGroupedMap.computeIfAbsent(evento.getCalendarDay(), k -> new ArrayList<>()).add(evento);
        }
        eventosGroupedByDayLiveData.postValue(newGroupedMap);
    }
}