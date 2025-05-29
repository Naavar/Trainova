package com.navar.trainova.ui.decorators;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class BaseDayDecoratorTest {

    @Mock
    private Context mockContext;
    @Mock
    private Resources mockResources;
    @Mock
    private Drawable mockDrawable;
    @Mock
    private Drawable.ConstantState mockConstantState;
    @Mock
    private DayViewFacade mockViewFacade;

    private BaseDayDecorator baseDayDecorator;

    @Before
    public void setUp() {
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getDrawable(anyInt())).thenReturn(mockDrawable);
        when(mockDrawable.getConstantState()).thenReturn(mockConstantState);
        when(mockConstantState.newDrawable()).thenReturn(mockDrawable);

        when(mockDrawable.mutate()).thenReturn(mockDrawable);

        baseDayDecorator = new BaseDayDecorator(mockContext);
    }

    @Test
    public void shouldDecorate_shouldAlwaysReturnTrue() {
        CalendarDay day = CalendarDay.from(2025, 5, 27);
        boolean result = baseDayDecorator.shouldDecorate(day);
        assertTrue("shouldDecorate() debería devolver siempre true", result);
    }

    @Test
    public void decorate_shouldSetBackgroundDrawable() {
        baseDayDecorator.decorate(mockViewFacade);

        verify(mockViewFacade).setBackgroundDrawable(mockDrawable);
    }

    @Test
    public void decorate_whenDrawableIsNotCloneable_shouldNotSetBackground() {
        // Given: Se configura un mock que no se puede clonar
        when(mockDrawable.getConstantState()).thenReturn(null);

        // Se crea una nueva instancia con el drawable que no se puede clonar
        BaseDayDecorator localDecorator = new BaseDayDecorator(mockContext);

        // When: Se llama al método decorate
        localDecorator.decorate(mockViewFacade);

        // Then: Se verifica que NUNCA se llama a setBackgroundDrawable
        verify(mockViewFacade, never()).setBackgroundDrawable(any(Drawable.class));
    }
}