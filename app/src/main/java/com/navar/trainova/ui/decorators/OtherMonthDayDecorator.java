package com.navar.trainova.ui.decorators;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.navar.trainova.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

/**
 * Decorador para los días del calendario que no pertenecen al mes actualmente visible.
 * Este decorador cambia el color del texto de los días de "otros meses" y los deshabilita,
 * impidiendo su selección.
 */
public class OtherMonthDayDecorator implements DayViewDecorator {
    /** Color a aplicar a los días de otros meses */
    private final int otherMonthColor;
    /** El mes que este decorador considera el "mes actual" (1-12) */
    private final int displayMonth;

    /**
     * @param colourContext El contexto de la aplicación, necesario para obtener el color.
     * @param displayMonth  El número del mes (1-12) que se considera el mes principal
     *                      y cuyos días NO deben ser decorados por este decorador.
     */
    public OtherMonthDayDecorator(@NonNull Context colourContext, int displayMonth) {
        this.otherMonthColor = ContextCompat.getColor(colourContext, R.color.colorOtherMonthDayText);
        this.displayMonth = displayMonth; // Guarda el mes "actual" o de visualización.
    }

    /**
     * Determina si este decorador debe aplicarse a un día específico.
     *
     * @param day El día del calendario a evaluar.
     * @return true si el día pertenece a un mes diferente al displayMonth, false en caso contrario.
     */
    @Override
    public boolean shouldDecorate(@NonNull CalendarDay day) {
        // El decorador se aplica si el mes del día no coincide con el mes de visualización.
        return day.getMonth() != displayMonth;
    }

    /**
     * Aplica la decoración visual al {@link DayViewFacade} de un día.
     * Cambia el color del texto del día y lo deshabilita.
     *
     * @param view La fachada de la vista del día a decorar.
     */
    @Override
    public void decorate(@NonNull DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(otherMonthColor));
        view.setDaysDisabled(true);
    }
}