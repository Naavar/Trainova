package com.navar.trainova.util;

import com.navar.trainova.data.model.Evento;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import java.util.List;

/**
 * Interfaz base para eventos de UI que el ViewModel envía a la View.
 * La View puede usar 'instanceof' para determinar el tipo de evento.
 */
public interface UiEvent {

    // Clase para mostrar un mensaje Toast
    class ShowToast implements UiEvent {
        private final String message;

        public ShowToast(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // Clase para navegar a la pantalla de Login
    class NavigateToLogin implements UiEvent {
        private final boolean closeCurrent;

        public NavigateToLogin(boolean closeCurrent) {
            this.closeCurrent = closeCurrent;
        }

        public boolean shouldCloseCurrent() {
            return closeCurrent;
        }
    }

    // Clase para mostrar el BottomSheet con eventos de un día
    class ShowBottomSheetForDay implements UiEvent {
        private final CalendarDay day;
        private final List<Evento> events;

        public ShowBottomSheetForDay(CalendarDay day, List<Evento> events) {
            this.day = day;
            this.events = events;
        }

        public CalendarDay getDay() {
            return day;
        }

        public List<Evento> getEvents() {
            return events;
        }
    }

    // Clase para mostrar el diálogo de creación de evento
    class ShowCreateEventDialog implements UiEvent {
        private final CalendarDay day;

        public ShowCreateEventDialog(CalendarDay day) {
            this.day = day;
        }

        public CalendarDay getDay() {
            return day;
        }
    }

    // Clase para mostrar el diálogo de edición de evento
    class ShowEditEventDialog implements UiEvent {
        private final Evento event;

        public ShowEditEventDialog(Evento event) {
            this.event = event;
        }

        public Evento getEvent() {
            return event;
        }
    }

    // Clase para mostrar el diálogo de detalles del evento
    class ShowEventDetailsDialog implements UiEvent {
        private final Evento event;

        public ShowEventDetailsDialog(Evento event) {
            this.event = event;
        }

        public Evento getEvent() {
            return event;
        }
    }

    // Puedes añadir más clases de eventos aquí según sea necesario
}