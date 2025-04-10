package com.navar.trainova; // O el paquete donde deba ir

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// Decorador para aplicar UN estilo específico (un color) a un CONJUNTO de días
public class SingleEventDecorator implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> days;

    public SingleEventDecorator(int eventColor, Collection<CalendarDay> daysToDecorate) {
        this.color = eventColor;
        this.days = new HashSet<>(daysToDecorate);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        // Verifica si este decorador (con su color específico) debe aplicarse a este día
        return days.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Aplica la decoración (color de fondo y texto blanco)
        // Ya sabemos qué color usar porque está almacenado en this.color
        view.setBackgroundDrawable(new ColorDrawable(color));
        view.addSpan(new ForegroundColorSpan(Color.WHITE));
        // Opcional: podrías añadir un punto en lugar del fondo completo
        // import com.prolificinteractive.materialcalendarview.spans.DotSpan;
        // view.addSpan(new DotSpan(8, color)); // Radio 8px, con el color del evento
    }
}