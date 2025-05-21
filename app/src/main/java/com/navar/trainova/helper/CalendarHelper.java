package com.navar.trainova.helper;

import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Clase de utilidad para operaciones y configuraciones relacionadas con el calendario.
 * Proporciona métodos estáticos para ayudar en la personalización y formato
 * de componentes de calendario.
 * No está diseñada para ser instanciada.
 */
public class CalendarHelper {
    /**
     * Devuelve un TitleFormatter para la librería MaterialCalendarView
     * que formatea el título del mes y año en español y mayúsculas.
     * Por ejemplo, para mayo de 2025, mostraría "MAYO DE 2025".
     * Maneja el caso de que el día de entrada sea nulo devolviendo una cadena vacía.
     *
     * @return Un TitleFormatter configurado para el formato "MES de AÑO" en español.
     */
    public static TitleFormatter getSpanishTitleFormatter() {
        return day -> {
            if (day == null) return "";
            Calendar cal = Calendar.getInstance();
            // CalendarDay month is 1-12, Calendar month is 0-11
            cal.set(day.getYear(), day.getMonth() - 1, day.getDay());
            Locale localeSpanish = new Locale("es", "ES");
            DateFormat dateFormat = new SimpleDateFormat("MMMM 'de' yyyy", localeSpanish);
            return dateFormat.format(cal.getTime()).toUpperCase();
        };
    }
}