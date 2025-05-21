package com.navar.trainova.ui.decorators;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.navar.trainova.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

/**
 * Decorador para resaltar el día seleccionado, aplicando un drawable
 */
public class SelectedDayDecorator implements DayViewDecorator {
    /** El día del calendario que está seleccionado */
    private final CalendarDay selectedDay;
    /** El día del calendario que está seleccionado */
    private final Drawable selectionDrawable;

    /**
     * @param context El contexto de la aplicación, necesario para cargar el drawable.
     * @param day El CalendarDay que se considera actualmente seleccionado y que debe ser decorado.
     */
    public SelectedDayDecorator(@NonNull Context context, CalendarDay day) {
        this.selectedDay = day; // Guarda el día que se ha seleccionado.
        // Carga el drawable 'drawable_day_selection' para usarlo como fondo del día seleccionado.
        this.selectionDrawable = ContextCompat.getDrawable(context, R.drawable.drawable_day_selection);
        // Registra un error si el drawable no se encuentra.
        if (selectionDrawable == null) {
            Log.e("HomeActivity", "Drawable R.drawable.drawable_day_selection no encontrado!");
        }
    }

    /**
     * Determina si este decorador debe aplicarse a un día específico.
     * @param day El día del calendario a evaluar.
     * @return true si el día del calendario es igual al día seleccionado por este decorador, false en caso contrario.
     */
    @Override
    public boolean shouldDecorate(@NonNull CalendarDay day) {
        // El decorador se aplica solo si el día actual es el mismo que el día seleccionado.
        return day.equals(selectedDay);
    }

    /**
     * Aplica la decoración visual al DayViewFacade del día seleccionado.
     * Establece el drawable de selección como fondo del día, asegurando que cada celda
     * tenga su propia instancia del drawable para evitar problemas de estado compartido.
     * @param view La fachada de la vista del día a decorar.
     */
    @Override
    public void decorate(@NonNull DayViewFacade view) {
        // Verifica que el drawable no sea nulo y que se pueda clonar de forma segura.
        if (selectionDrawable != null && selectionDrawable.getConstantState() != null) {
            try {
                view.setBackgroundDrawable(selectionDrawable.getConstantState().newDrawable().mutate());
            } catch (Exception e) {
                Log.e("HomeActivity", "Error clonando selectionDrawable", e);
            }
        }
    }
}