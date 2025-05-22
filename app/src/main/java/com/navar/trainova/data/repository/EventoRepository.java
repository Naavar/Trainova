package com.navar.trainova.data.repository;

import androidx.lifecycle.LiveData;
import com.navar.trainova.data.model.Evento;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import java.util.List;
import java.util.Map;

/**
 * Define cómo se manejan los datos de los eventos.
 * Esta interfaz obtiene, guarda, actualiza y elimina eventos.
 * Permite que la forma de almacenar los datos (base de datos, API, etc.)
 * pueda cambiar sin afectar al resto de la aplicación, especialmente a los ViewModels
 * que observan estos datos mediante {@link LiveData}.
 */
public interface EventoRepository {

    /** Obtiene todos los eventos, agrupados por día, y permite observar cambios. */
    LiveData<Map<CalendarDay, List<Evento>>> getAllEventosGroupedByDay();

    /** Obtiene los eventos de un día específico y permite observar cambios. */
    LiveData<List<Evento>> getEventosForDay(CalendarDay day);

    /** Guarda un nuevo {@link Evento}. */
    void addEvento(Evento evento);

    /** Actualiza la información de un {@link Evento} existente. */
    void updateEvento(Evento evento);

    /** Elimina un {@link Evento} usando su identificador único. */
    void deleteEvento(String eventoId);

    /** Busca y devuelve un EVENTO específico por su ID. */
    Evento findEventoById(String eventoId);

    /** Carga un conjunto inicial de eventos (ej. para demostración o desarrollo). */
    void loadInitialData(android.content.Context context);
}