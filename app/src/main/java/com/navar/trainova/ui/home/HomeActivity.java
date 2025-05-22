package com.navar.trainova.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.navar.trainova.R;
import com.navar.trainova.ui.auth.AuthActivity;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.ui.adapters.EventoAdapter;
import com.navar.trainova.helper.CalendarHelper;
import com.navar.trainova.ui.decorators.BaseDayDecorator;
import com.navar.trainova.ui.decorators.MultipleEventDecorator;
import com.navar.trainova.ui.decorators.OtherMonthDayDecorator;
import com.navar.trainova.ui.decorators.SelectedDayDecorator;
import com.navar.trainova.util.UiEvent;
import com.navar.trainova.ui.dialogs.EventoCreateEditDialogFragment;
import com.navar.trainova.ui.dialogs.EventoDetailsDialogFragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Actividad principal de la aplicación que muestra el calendario y una lista de eventos/actividades.
 * Permite a los usuarios visualizar eventos en el calendario, seleccionar días,
 * y gestionar eventos (crear, editar, eliminar) a través de diálogos y una hoja inferior.
 * También maneja el cierre de sesión del usuario.
 */
public class HomeActivity extends AppCompatActivity {

    /** ViewModel para gestionar la lógica de negocio y datos */
    private HomeViewModel viewModel;
    /** Vista del calendario */
    private MaterialCalendarView calendarView;
    /** Adaptador para la lista de eventos en el bottom sheet */
    private EventoAdapter eventoAdapter;

    /** Decoradores para personalizar la apariencia del calendario */
    private final Set<DayViewDecorator> activeEventDecorators = new HashSet<>();
    private SelectedDayDecorator currentSelectedDayDecorator = null;
    private BaseDayDecorator baseDayDecorator;
    private OtherMonthDayDecorator otherMonthDayDecorator;


    /**
     * Se llama cuando la actividad es creada por primera vez.
     * Aquí se inicializan las vistas, el ViewModel, se configura el calendario
     * y se establecen los observadores y listeners.
     * @param savedInstanceState Si la actividad se está recreando después de un
     * cambio de configuración, este Bundle contiene los datos más recientes de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Enlaza la vista del calendario y el botón de cerrar sesión
        calendarView = findViewById(R.id.calendarView);
        Button cerrarSesionButton = findViewById(R.id.cerrarSesionButton);
        // Configura el listener para cerrar sesión
        cerrarSesionButton.setOnClickListener(v -> viewModel.cerrarSesion());

        // Configuración inicial del calendario, observadores y listeners
        setupCalendar();
        setupObservers();
        setupListeners();

        // Inicializa el calendario en el ViewModel con la fecha actual
        CalendarDay initialCalendarDate = calendarView.getCurrentDate() != null ? calendarView
            .getCurrentDate() : CalendarDay.today();
        viewModel.initializeCalendar(initialCalendarDate);
    }

    /**
     * Configura el MaterialCalendarView, incluyendo el formato del título
     * y la adición del decorador base para los días.
     */
    private void setupCalendar() {
        // Establece el formato del título del calendario en español
        calendarView.setTitleFormatter(CalendarHelper.getSpanishTitleFormatter());
        // Añade un decorador base para todos los días del calendario
        baseDayDecorator = new BaseDayDecorator(this);
        calendarView.addDecorator(baseDayDecorator);
    }

    /**
     * Configura los listeners para los eventos de cambio de fecha y mes en el calendario.
     */
    private void setupListeners() {
        // Listener para cuando cambia la fecha seleccionada en el calendario
        calendarView.setOnDateChangedListener((@NonNull MaterialCalendarView widget,
                                               @NonNull CalendarDay date, boolean selected) -> {
            CalendarDay currentVisibleMonth = widget.getCurrentDate();
            if (currentVisibleMonth == null) currentVisibleMonth = CalendarDay.today();
            viewModel.handleDateChanged(date, selected, currentVisibleMonth);
        });

        // Listener para cuando el mes visible en el calendario cambia
        calendarView.setOnMonthChangedListener((widget, date) -> viewModel.handleMonthChanged(date));
    }

    /**
     * Configura los observadores para los LiveData del ViewModel.
     * Responde a eventos de la UI, actualizaciones de eventos del calendario
     * y cambios en el mes o día seleccionado.
     */
    private void setupObservers() {
        // Observador para eventos de la UI (mostrar Toast, navegar, etc.)
        viewModel.uiEvent.observe(this, uiEvent -> {
            if (uiEvent == null) return; // No hacer nada si el evento es nulo

            if (uiEvent instanceof UiEvent.ShowToast) {
                Toast.makeText(this, ((UiEvent.ShowToast) uiEvent).getMessage(),
                    Toast.LENGTH_SHORT).show();
            } else if (uiEvent instanceof UiEvent.NavigateToLogin) {
                irALogin();
            } else if (uiEvent instanceof UiEvent.ShowBottomSheetForDay) {
                UiEvent.ShowBottomSheetForDay event = (UiEvent.ShowBottomSheetForDay) uiEvent;
                mostrarBottomSheetEventos(event.getDay(), event.getEvents());
            } else if (uiEvent instanceof UiEvent.ShowCreateEventDialog) {
                // Muestra el diálogo para crear un nuevo evento
                EventoCreateEditDialogFragment.newInstanceForCreate
                        (((UiEvent.ShowCreateEventDialog) uiEvent).getDay())
                    .show(getSupportFragmentManager(), EventoCreateEditDialogFragment.TAG_CREATE);
            } else if (uiEvent instanceof UiEvent.ShowEditEventDialog) {
                // Muestra el diálogo para editar un evento existente
                EventoCreateEditDialogFragment.newInstanceForEdit
                        (((UiEvent.ShowEditEventDialog) uiEvent).getEvent())
                    .show(getSupportFragmentManager(), EventoCreateEditDialogFragment.TAG_EDIT);
            } else if (uiEvent instanceof UiEvent.ShowEventDetailsDialog) {
                // Muestra el diálogo con los detalles de un evento
                EventoDetailsDialogFragment.newInstance(((UiEvent.ShowEventDetailsDialog) uiEvent)
                        .getEvent())
                    .show(getSupportFragmentManager(), EventoDetailsDialogFragment.TAG);
            }

            viewModel.onUiEventHandled(); // Indica al ViewModel que el evento ha sido manejado
        });

        // Observador para la lista de eventos que se mostrarán en el calendario
        viewModel.eventosForCalendarView.observe(this, eventosMap -> {
            CalendarDay currentMonthToDecorate = viewModel.currentDisplayMonth.getValue();
            if (currentMonthToDecorate != null && eventosMap != null) {
                applyEventDecorators(currentMonthToDecorate, eventosMap); // Aplica los decoradores de eventos
            }
        });

        // Observador para el mes actualmente visible en el calendario
        viewModel.currentDisplayMonth.observe(this, displayMonth -> {
            if (displayMonth != null) {
                // Remueve el decorador del mes anterior si existe
                if (otherMonthDayDecorator != null) {
                    calendarView.removeDecorator(otherMonthDayDecorator);
                }
                // Añade un nuevo decorador para los días de otros meses del mes actual
                otherMonthDayDecorator = new OtherMonthDayDecorator(this,
                    displayMonth.getMonth());
                calendarView.addDecorator(otherMonthDayDecorator);

                // Vuelve a aplicar los decoradores de eventos para el nuevo mes
                Map<CalendarDay, List<Evento>> currentEvents = viewModel.eventosForCalendarView.getValue();
                applyEventDecorators(displayMonth, currentEvents != null ? currentEvents : new HashMap<>());

                // Actualiza el decorador del día seleccionado si el día seleccionado está en el mes actual
                CalendarDay selectedDay = viewModel.selectedCalendarDay.getValue();
                updateSelectionDecorator(selectedDay != null && selectedDay.getMonth() == displayMonth.getMonth() ? selectedDay : null);
            }
        });

        // Observador para el día actualmente seleccionado en el calendario
        viewModel.selectedCalendarDay.observe(this, this::updateSelectionDecorator);
    }

    /**
     * Actualiza el decorador para el día seleccionado en el calendario.
     * Remueve cualquier selección previa y aplica una nueva si se especifica un día.
     * @param dayToSelect El CalendarDay que debe ser marcado como seleccionado, o null para deseleccionar.
     */
    private void updateSelectionDecorator(CalendarDay dayToSelect) {
        // Remueve el decorador de selección anterior si existe
        if (currentSelectedDayDecorator != null) {
            calendarView.removeDecorator(currentSelectedDayDecorator);
            currentSelectedDayDecorator = null;
        }
        // Si hay un día para seleccionar y está en el mes actual del calendario, aplica el decorador
        if (dayToSelect != null) {
            CalendarDay currentCalendarMonth = calendarView.getCurrentDate();
            if (currentCalendarMonth != null && dayToSelect.getMonth() == currentCalendarMonth.getMonth()) {
                currentSelectedDayDecorator = new SelectedDayDecorator(this, dayToSelect);
                calendarView.addDecorator(currentSelectedDayDecorator);
            }
        }
        calendarView.invalidateDecorators(); // Refresca los decoradores del calendario
    }

    /**
     * Aplica los decoradores de eventos a los días que tienen eventos.
     * Remueve los decoradores anteriores y añade nuevos basados en los eventos proporcionados
     * para el mes actualmente visible.
     * @param currentMonthDate El CalendarDay que representa el mes actualmente visible.
     * @param allEvents Un mapa de CalendarDay a una lista de Eventos para ese día.
     */
    private void applyEventDecorators(@NonNull CalendarDay currentMonthDate,
                                      @NonNull Map<CalendarDay, List<Evento>> allEvents) {
        // Remueve todos los decoradores de eventos activos previamente
        for (DayViewDecorator decorator : activeEventDecorators) {
            calendarView.removeDecorator(decorator);
        }
        activeEventDecorators.clear();

        // Si no hay eventos, invalida los decoradores y sale
        if (allEvents.isEmpty()) {
            calendarView.invalidateDecorators();
            return;
        }

        int year = currentMonthDate.getYear();
        int month = currentMonthDate.getMonth();

        // Itera sobre todos los eventos para aplicar decoradores
        for (Map.Entry<CalendarDay, List<Evento>> entry : allEvents.entrySet()) {
            CalendarDay day = entry.getKey();
            List<Evento> eventosDelDia = entry.getValue();

            // Continúa si el día o la lista de eventos es nula o vacía
            if (day == null || eventosDelDia == null || eventosDelDia.isEmpty()) continue;

            // Si el día del evento pertenece al mes y año actuales
            if (day.getYear() == year && day.getMonth() == month) {
                Set<Integer> colores = new HashSet<>();
                for (Evento evento : eventosDelDia) {
                    colores.add(evento.getColor());
                }

                if (!colores.isEmpty()) {
                    Set<CalendarDay> daysToDecorate = new HashSet<>();
                    daysToDecorate.add(day); // Solo el día actual

                    Integer backgroundColor = null; // Color de fondo del día
                    // Lógica para determinar si el día tiene un evento de "Descanso" con un color específico
                    for (Evento evento : eventosDelDia) {
                        if ("Descanso".equals(evento.getNombre()) && evento.getColor() == Color.parseColor("#80CBC4")) {
                            backgroundColor = Color.parseColor("#E0F2F1"); // Un verde muy claro
                            break;
                        }
                    }

                    boolean showWhiteText = false;

                    MultipleEventDecorator decorador = new MultipleEventDecorator(
                        daysToDecorate,     // Colección de CalendarDay
                        colores,            // Set de colores para los puntos
                        backgroundColor,    // Color de fondo (Integer, puede ser null)
                        showWhiteText       // Boolean para texto blanco
                    );

                    calendarView.addDecorator(decorador); // Añade el decorador al calendario
                    activeEventDecorators.add(decorador); // Guarda el decorador para futuras remociones
                }
            }
        }
        calendarView.invalidateDecorators(); // Refresca los decoradores del calendario
    }

    /**
     * Muestra un BottomSheetDialog que contiene la lista de eventos para un día específico.
     * Permite ver los eventos, añadir una nueva actividad o ver los detalles de un evento existente.
     * @param date El CalendarDay para el cual se mostrarán los eventos.
     * @param eventosDelDia La lista de Eventos para el día especificado.
     */
    private void mostrarBottomSheetEventos(CalendarDay date, List<Evento> eventosDelDia) {
        try {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            @SuppressLint("InflateParams")
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_eventos, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            RecyclerView recyclerView = bottomSheetView.findViewById(R.id.recyclerEventos);
            Button btnAddActivity = bottomSheetView.findViewById(R.id.btnAddActivity);

            // Verificación de que las vistas se hayan encontrado correctamente
            if (recyclerView == null || btnAddActivity == null) {
                Toast.makeText(this, "Error al cargar la vista del bottom sheet",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Asegura que la lista de eventos no sea nula
            List<Evento> eventos = eventosDelDia != null ? eventosDelDia : new ArrayList<>();

            // Inicializa el adaptador de eventos para el RecyclerView
            eventoAdapter = new EventoAdapter(new ArrayList<>(eventos), evento -> {
                if (evento != null && evento.getIdEvento() != null) {
                    viewModel.requestEventDetailsDialog(evento.getIdEvento());
                    bottomSheetDialog.dismiss();
                }
            });

            recyclerView.setAdapter(eventoAdapter);

            // Configura el listener para el botón de añadir actividad
            btnAddActivity.setOnClickListener(v -> {
                if (date != null) {
                    viewModel.requestCreateEventDialog(date);
                    // Solicita crear un evento para la fecha seleccionada
                } else {
                    viewModel.requestCreateEventDialog(CalendarDay.today());
                    // Si la fecha es nula, usa la fecha actual
                }
                bottomSheetDialog.dismiss(); // Cierra el bottom sheet
            });

            bottomSheetDialog.show(); // Muestra el bottom sheet
        } catch (Exception e) {
            // Para el desarrollador: Imprime la traza de la pila completa en Logcat.
            Log.e("HomeActivity", "Error al mostrar eventos: " + e.getMessage(), e);

            // Para el usuario: Muestra un mensaje amigable y no técnico.
            Toast.makeText(this, "Ocurrió un error inesperado al cargar los eventos. " +
                    "Inténtelo de nuevo.",
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navega a la actividad de inicio de sesión (AuthActivity) y finaliza la actividad actual.
     * Se usa para cerrar la sesión del usuario.
     */
    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
        // Limpia la pila de actividades para que el usuario no pueda volver a HomeActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}