package com.navar.trainova.ui.decorators;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MultipleEventDecorator implements DayViewDecorator {

    private final Set<CalendarDay> days;
    private final Set<Integer> dotColors;
    private final Integer backgroundColor;
    private final boolean showWhiteText;

    /**
     * Constructor completo.
     * @param daysToDecorate Días a decorar.
     * @param dotColors Colores de los puntos (se pueden mostrar varios).
     * @param backgroundColor Color de fondo, o null si no se quiere aplicar.
     * @param showWhiteText Si se quiere texto blanco sobre fondo.
     */
    public MultipleEventDecorator(Collection<CalendarDay> daysToDecorate,
                                  Set<Integer> dotColors,
                                  Integer backgroundColor,
                                  boolean showWhiteText) {
        this.days = new HashSet<>(daysToDecorate);
        this.dotColors = dotColors;
        this.backgroundColor = backgroundColor;
        this.showWhiteText = showWhiteText;
    }

    @Override
    public boolean shouldDecorate(@NonNull CalendarDay day) {
        return days.contains(day);
    }

    @Override
    public void decorate(@NonNull DayViewFacade view) {
        // Aplica fondo si se especificó
        if (backgroundColor != null) {
            view.setBackgroundDrawable(new ColorDrawable(backgroundColor));
        }

        // Aplica texto blanco si se indicó
        if (showWhiteText) {
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }

        // Aplica puntos de colores
        if (dotColors != null) {
            float dotRadius = 8;
            for (int color : dotColors) {
                view.addSpan(new DotSpan(dotRadius, color));
            }
        }
    }
}
