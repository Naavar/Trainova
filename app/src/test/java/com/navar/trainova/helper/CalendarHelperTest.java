package com.navar.trainova.helper;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests unitarios para la clase CalendarHelper.
 * Verifica la correcta funcionalidad del formateador de títulos.
 */
public class CalendarHelperTest {

    @Test
    public void getSpanishTitleFormatter_withValidDate_returnsFormattedStringInUpperCase() {
        // Given: Una fecha específica,(ej. 27 de mayo de 2025).
        CalendarDay day = CalendarDay.from(2025, 5, 27);
        String expectedTitle = "MAYO DE 2025";

        // When: Obtenemos el formateador y lo usamos con la fecha dada.
        TitleFormatter formatter = CalendarHelper.getSpanishTitleFormatter();
        String actualTitle = formatter.format(day).toString();

        // Then: Verificamos que el formateador no sea nulo y que el título sea el esperado.
        assertNotNull(formatter);
        assertEquals(expectedTitle, actualTitle);
    }

    @Test
    public void getSpanishTitleFormatter_withAnotherValidDate_returnsCorrectFormattedString() {
        // Given: Otra fecha para asegurar que no está hardcodeado, por ejemplo, 15 de diciembre de 2024.
        CalendarDay day = CalendarDay.from(2024, 12, 15);
        String expectedTitle = "DICIEMBRE DE 2024";

        // When: Obtenemos el formateador y lo usamos con la fecha.
        TitleFormatter formatter = CalendarHelper.getSpanishTitleFormatter();
        String actualTitle = formatter.format(day).toString();

        // Then: Verificamos que el resultado es el correcto para esta otra fecha.
        assertNotNull(formatter);
        assertEquals(expectedTitle, actualTitle);
    }

    @Test
    public void getSpanishTitleFormatter_withNullDate_returnsEmptyString() {
        // Given: Un objeto CalendarDay nulo, que es un caso límite que la función debe manejar.
        CalendarDay day = null;
        String expectedTitle = "";

        // When: Obtenemos el formateador y lo usamos con el valor nulo.
        TitleFormatter formatter = CalendarHelper.getSpanishTitleFormatter();
        String actualTitle = formatter.format(day).toString();

        // Then: Verificamos que la función maneja el caso nulo correctamente devolviendo una cadena vacía.
        assertNotNull(formatter);
        assertEquals(expectedTitle, actualTitle);
    }
}