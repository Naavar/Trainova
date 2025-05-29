package com.navar.trainova.ui.decorators;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SelectedDayDecoratorTest {

    @Mock
    private Context mockContext;
    @Mock
    private DayViewFacade mockViewFacade;
    @Mock
    private Drawable mockDrawable;
    @Mock
    private Drawable.ConstantState mockConstantState;

    private CalendarDay selectedDay;

    @Before
    public void setUp() {
        selectedDay = CalendarDay.from(2025, 5, 28);
    }

    @Test
    public void shouldDecorate_cuandoElDiaEsElSeleccionado_devuelveTrue() {
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class)) {
            mockedContextCompat.when(() -> ContextCompat.getDrawable(any(), anyInt())).thenReturn(mockDrawable);

            // Given
            SelectedDayDecorator decorator = new SelectedDayDecorator(mockContext, selectedDay);

            // When
            boolean result = decorator.shouldDecorate(selectedDay);

            // Then
            assertTrue("Debe decorar el día que es igual al seleccionado", result);
        }
    }

    @Test
    public void shouldDecorate_cuandoElDiaEsDiferente_devuelveFalse() {
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class)) {
            mockedContextCompat.when(() -> ContextCompat.getDrawable(any(), anyInt())).thenReturn(mockDrawable);

            // Given
            SelectedDayDecorator decorator = new SelectedDayDecorator(mockContext, selectedDay);
            CalendarDay otherDay = CalendarDay.from(2025, 5, 29);

            // When
            boolean result = decorator.shouldDecorate(otherDay);

            // Then
            assertFalse("NO debe decorar un día diferente al seleccionado", result);
        }
    }

    @Test
    public void decorate_cuandoElDrawableExiste_aplicaElFondo() {
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class)) {
            // Given
            mockedContextCompat.when(() -> ContextCompat.getDrawable(any(), anyInt())).thenReturn(mockDrawable);
            when(mockDrawable.getConstantState()).thenReturn(mockConstantState);
            when(mockConstantState.newDrawable()).thenReturn(mockDrawable);
            when(mockDrawable.mutate()).thenReturn(mockDrawable);
            SelectedDayDecorator decorator = new SelectedDayDecorator(mockContext, selectedDay);

            // When
            decorator.decorate(mockViewFacade);

            // Then
            verify(mockViewFacade).setBackgroundDrawable(mockDrawable);
        }
    }

    @Test
    public void decorate_cuandoElDrawableEsNulo_noHaceNada() {
        try (MockedStatic<ContextCompat> mockedContextCompat = Mockito.mockStatic(ContextCompat.class);
             MockedStatic<Log> mockedLog = Mockito.mockStatic(Log.class)) {

            // Given
            mockedContextCompat.when(() -> ContextCompat.getDrawable(any(), anyInt())).thenReturn(null);
            SelectedDayDecorator decorator = new SelectedDayDecorator(mockContext, selectedDay);

            // When
            decorator.decorate(mockViewFacade);

            // Then
            verify(mockViewFacade, never()).setBackgroundDrawable(any());
            mockedLog.verify(() -> Log.e(anyString(), anyString()));
        }
    }
}