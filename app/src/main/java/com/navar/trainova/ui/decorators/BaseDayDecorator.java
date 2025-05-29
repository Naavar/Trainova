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
 * Decorador base para las vistas de día en un calendario MaterialCalendarView.
 * Este decorador aplica un drawable de fondo predefinido a cada día del calendario.
 * Está diseñado para ser una base sobre la cual otros decoradores pueden añadir estilos adicionales.
 */
public class BaseDayDecorator implements DayViewDecorator {
    private final Drawable baseDrawable; // Drawable que se aplicará como fondo

    /**
     * Constructor para BaseDayDecorator.
     * Inicializa el decorador cargando el drawable base desde los recursos de la aplicación.
     * @param context El contexto de la aplicación, necesario para acceder a los recursos.
     */
    public BaseDayDecorator(@NonNull Context context) {
        // Carga el drawable 'drawable_day_cell_base' que servirá como fondo para las celdas del día.
        baseDrawable = ContextCompat.getDrawable(context, R.drawable.drawable_day_cell_base);
        // Registra un error si el drawable no se encuentra, lo cual es crítico para el funcionamiento.
        if (baseDrawable == null) {
            Log.e("HomeActivity", "Drawable R.drawable.drawable_day_cell_base no encontrado!");
        }
    }

    /**
     * Determina si este decorador debe aplicarse a un día específico.
     * En este caso, se aplica a todos los días, ya que es un decorador base.
     * @param day El día del calendario a evaluar.
     * @return Siempre true, indicando que el decorador debe aplicarse a todos los días.
     */
    @Override
    public boolean shouldDecorate(@NonNull CalendarDay day) {
        return true;
    }

    /**
     * Aplica la decoración visual al DayViewFacade de un día.
     * Esto establece el drawable de fondo para la celda del día.
     * Se realiza una comprobación de nulidad y se clona el drawable para evitar problemas de estado compartido.
     * @param view La fachada de la vista del día a decorar.
     */
    @Override
    public void decorate(@NonNull DayViewFacade view) {
        // Comprueba que el drawable no sea nulo y que tenga un estado constante para poder clonarlo.
        if (baseDrawable != null && baseDrawable.getConstantState() != null) {
            try {
                // Clona el drawable y lo muta para asegurar que cada celda tenga su propia instancia.
                // Esto previene que los cambios en un drawable afecten a otras celdas.
                view.setBackgroundDrawable(baseDrawable.getConstantState().newDrawable().mutate());
            } catch (Exception e) {
                // Registra un error si ocurre un problema al clonar el drawable.
                Log.e("HomeActivity", "Error clonando baseDrawable", e);
            }
        }
    }
}