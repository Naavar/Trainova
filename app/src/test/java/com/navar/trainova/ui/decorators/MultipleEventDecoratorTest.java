package com.navar.trainova.ui.decorators;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test unitario para la clase MultipleEventDecorator.
 * Verifica que el decorador se aplica solo a los días correctos y que cada
 * opción de estilo (fondo, texto, puntos) funciona como se espera.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipleEventDecoratorTest {

    // Mock para la vista del día
    @Mock
    private DayViewFacade mockViewFacade;

    @Captor
    private ArgumentCaptor<Object> spanCaptor;

    @Captor
    private ArgumentCaptor<ColorDrawable> backgroundCaptor;

    @Test
    public void shouldDecorate_cuandoElDiaEstaEnLaLista_devuelveTrue() {
        // Given: Un día específico que queremos decorar.
        CalendarDay dayToDecorate = CalendarDay.from(2025, 5, 28);
        Set<CalendarDay> days = new HashSet<>(Collections.singletonList(dayToDecorate));
        MultipleEventDecorator decorator = new MultipleEventDecorator(days, null, null, false);

        // When: Se pregunta si se debe decorar ese día.
        boolean result = decorator.shouldDecorate(dayToDecorate);

        // Then: El resultado debe ser verdadero.
        assertTrue(result);
    }

    @Test
    public void shouldDecorate_cuandoElDiaNoEstaEnLaLista_devuelveFalse() {
        // Given: Un decorador para un día específico.
        CalendarDay dayToDecorate = CalendarDay.from(2025, 5, 28);
        CalendarDay otherDay = CalendarDay.from(2025, 5, 29);
        Set<CalendarDay> days = new HashSet<>(Collections.singletonList(dayToDecorate));
        MultipleEventDecorator decorator = new MultipleEventDecorator(days, null, null, false);

        // When: Se pregunta si se debe decorar un día diferente.
        boolean result = decorator.shouldDecorate(otherDay);

        // Then: El resultado debe ser falso.
        assertFalse(result);
    }

    @Test
    public void decorate_conTodasLasOpciones_aplicaTodosLosEstilos() {
        // Given: Un decorador con color de fondo, texto blanco y múltiples puntos.
        int backgroundColor = Color.BLUE;
        Set<Integer> dotColors = new HashSet<>(Arrays.asList(Color.RED, Color.GREEN));
        MultipleEventDecorator decorator = new MultipleEventDecorator(Collections.emptySet(),
            dotColors, backgroundColor, true);

        // When: Se decora la vista.
        decorator.decorate(mockViewFacade);

        // Then: Verificamos que se aplicaron todos los estilos correctamente.
        verify(mockViewFacade).setBackgroundDrawable(any(ColorDrawable.class));
        verify(mockViewFacade, times(3)).addSpan(spanCaptor.capture());

        List<Object> capturedSpans = spanCaptor.getAllValues();
        assertTrue("Debería contener un ForegroundColorSpan para el texto blanco",
            capturedSpans.stream().anyMatch(s -> s instanceof ForegroundColorSpan));
        assertEquals("Debería contener 2 DotSpans para los puntos", 2,
            capturedSpans.stream().filter(s -> s instanceof DotSpan).count());
    }

    @Test
    public void decorate_soloConColorDeFondo_aplicaSoloElFondo() {
        // Given: Un decorador solo con color de fondo.
        int backgroundColor = Color.YELLOW;
        MultipleEventDecorator decorator = new MultipleEventDecorator(Collections.emptySet(),
            null, backgroundColor, false);

        // When: Se decora la vista.
        decorator.decorate(mockViewFacade);

        // Then: Solo se debe llamar a setBackgroundDrawable.
        verify(mockViewFacade).setBackgroundDrawable(any(ColorDrawable.class));
        verify(mockViewFacade, never()).addSpan(any());
    }

    @Test
    public void decorate_soloConTextoBlanco_aplicaSoloElSpanDeColor() {
        // Given: Un decorador solo con texto blanco.
        MultipleEventDecorator decorator = new MultipleEventDecorator(Collections.emptySet(),
            null, null, true);

        // When: Se decora la vista.
        decorator.decorate(mockViewFacade);

        // Then: Solo se debe añadir un ForegroundColorSpan.
        verify(mockViewFacade).addSpan(any(ForegroundColorSpan.class));
        verify(mockViewFacade, never()).setBackgroundDrawable(any()); // No se debe cambiar el fondo.
    }

    @Test
    public void decorate_soloConPuntos_aplicaSoloDotSpans() {
        // Given: Un decorador solo con puntos.
        Set<Integer> dotColors = new HashSet<>(Arrays.asList(Color.CYAN, Color.MAGENTA));
        MultipleEventDecorator decorator = new MultipleEventDecorator(Collections.emptySet(),
            dotColors, null, false);

        // When: Se decora la vista.
        decorator.decorate(mockViewFacade);

        // Then: Se deben añadir dos DotSpans y nada más.
        verify(mockViewFacade, times(2)).addSpan(any(DotSpan.class));
        verify(mockViewFacade, never()).setBackgroundDrawable(any());
    }

    @Test
    public void decorate_sinOpcionesDeEstilo_noHaceNada() {
        // Given: Un decorador sin ninguna opción de estilo.
        MultipleEventDecorator decorator = new MultipleEventDecorator(Collections.emptySet(),
            null, null, false);

        // When: Se decora la vista.
        decorator.decorate(mockViewFacade);

        // Then: No se debe llamar a ningún método de la vista.
        verify(mockViewFacade, never()).setBackgroundDrawable(any());
        verify(mockViewFacade, never()).addSpan(any());
    }
}