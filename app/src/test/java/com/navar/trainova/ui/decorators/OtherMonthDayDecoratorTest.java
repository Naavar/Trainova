package com.navar.trainova.ui.decorators;

import android.content.Context;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.navar.trainova.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

/**
 * Test unitario para la clase OtherMonthDayDecorator SIN MODIFICAR EL CÓDIGO ORIGINAL.
 * Usa mockito-inline para interceptar la llamada estática a ContextCompat.getColor().
 */
@RunWith(MockitoJUnitRunner.class)
public class OtherMonthDayDecoratorTest {

    private static final int CURRENT_DISPLAY_MONTH = 5; // Mayo
    private static final int TEST_COLOR = Color.LTGRAY;

    @Mock
    private Context mockContext;

    @Mock
    private DayViewFacade mockViewFacade;

    @Captor
    private ArgumentCaptor<ForegroundColorSpan> spanCaptor;

    @Test
    public void shouldDecorate_cuandoElDiaEsDeOtroMes_devuelveTrue() {
        // Usamos un bloque try-with-resources para asegurar que el mock estático se cierre solo
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class)) {
            // Given: Le decimos a nuestro mock estático qué devolver cuando se le llame.
            mockedContextCompat.when(() -> ContextCompat.getColor(any(Context.class), anyInt()))
                .thenReturn(TEST_COLOR);

            OtherMonthDayDecorator decorator = new OtherMonthDayDecorator(mockContext, CURRENT_DISPLAY_MONTH);
            CalendarDay dayInDifferentMonth = CalendarDay.from(2025, 6, 1);

            // When
            boolean result = decorator.shouldDecorate(dayInDifferentMonth);

            // Then
            assertTrue("Debe decorar días de un mes diferente", result);
        }
    }

    @Test
    public void shouldDecorate_cuandoElDiaEsDelMismoMes_devuelveFalse() {
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class)) {
            // Given
            mockedContextCompat.when(() -> ContextCompat.getColor(any(Context.class), anyInt()))
                .thenReturn(TEST_COLOR);

            OtherMonthDayDecorator decorator = new OtherMonthDayDecorator(mockContext, CURRENT_DISPLAY_MONTH);
            CalendarDay dayInSameMonth = CalendarDay.from(2025, 5, 15);

            // When
            boolean result = decorator.shouldDecorate(dayInSameMonth);

            // Then
            assertFalse("NO debe decorar días del mismo mes", result);
        }
    }

    @Test
    public void decorate_aplicaColorYDeshabilitaElDia() {
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class)) {
            // Given
            mockedContextCompat.when(() -> ContextCompat.getColor(mockContext, R.color.colorOtherMonthDayText))
                .thenReturn(TEST_COLOR);

            OtherMonthDayDecorator decorator = new OtherMonthDayDecorator(mockContext, CURRENT_DISPLAY_MONTH);

            // When
            decorator.decorate(mockViewFacade);

            // Then
            verify(mockViewFacade).setDaysDisabled(true);
            verify(mockViewFacade).addSpan(any(ForegroundColorSpan.class));
        }
    }
}