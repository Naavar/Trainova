package com.navar.trainova.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.navar.trainova.R;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.helper.CalendarHelper;
import com.navar.trainova.ui.adapters.EventoAdapter;
import com.navar.trainova.ui.auth.AuthActivity;
import com.navar.trainova.ui.catalogo.CatalogActivity;
import com.navar.trainova.ui.decorators.BaseDayDecorator;
import com.navar.trainova.ui.decorators.MultipleEventDecorator;
import com.navar.trainova.ui.decorators.OtherMonthDayDecorator;
import com.navar.trainova.ui.decorators.SelectedDayDecorator;
import com.navar.trainova.ui.dialogs.EventoCreateEditDialogFragment;
import com.navar.trainova.ui.dialogs.EventoDetailsDialogFragment;
import com.navar.trainova.ui.ia.IaActivity;
import com.navar.trainova.ui.settings.SettingsActivity;
import com.navar.trainova.util.UiEvent;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;
    private MaterialCalendarView calendarView;
    private EventoAdapter eventoAdapter;

    private final Set<DayViewDecorator> activeEventDecorators = new HashSet<>();
    private SelectedDayDecorator currentSelectedDayDecorator = null;
    private OtherMonthDayDecorator otherMonthDayDecorator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        calendarView = findViewById(R.id.calendarView);

        Button cerrarSesionButton = findViewById(R.id.cerrarSesionButton);
        ImageButton homeButton = findViewById(R.id.homeButton);
        ImageButton iaButton = findViewById(R.id.iaButton);
        ImageButton eventButton = findViewById(R.id.eventButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        cerrarSesionButton.setOnClickListener(v -> viewModel.cerrarSesion());

        homeButton.setOnClickListener(v -> {
        });

        iaButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, IaActivity.class);
            startActivity(intent);
        });

        eventButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CatalogActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        setupCalendar();
        setupCalendarListeners();
        setupViewModelObservers();

        viewModel.initializeCalendar(calendarView.getCurrentDate() != null ? calendarView
            .getCurrentDate() : CalendarDay.today());
    }

    private void setupCalendar() {
        calendarView.setTitleFormatter(CalendarHelper.getSpanishTitleFormatter());
        calendarView.addDecorator(new BaseDayDecorator(this));
    }

    private void setupCalendarListeners() {
        calendarView.setOnDateChangedListener((@NonNull MaterialCalendarView widget,
                                               @NonNull CalendarDay date, boolean selected) -> {
            CalendarDay currentVisibleMonth = widget.getCurrentDate();
            if (currentVisibleMonth == null) currentVisibleMonth = CalendarDay.today();
            viewModel.handleDateChanged(date, selected, currentVisibleMonth);
        });

        calendarView.setOnMonthChangedListener((widget, date) -> viewModel.handleMonthChanged(date));
    }

    private void setupViewModelObservers() {
        viewModel.uiEvent.observe(this, uiEvent -> {
            if (uiEvent == null) return;

            if (uiEvent instanceof UiEvent.ShowToast) {
                Toast.makeText(this, ((UiEvent.ShowToast) uiEvent).getMessage(),
                    Toast.LENGTH_SHORT).show();
            } else if (uiEvent instanceof UiEvent.NavigateToLogin) {
                irALogin();
            } else if (uiEvent instanceof UiEvent.ShowBottomSheetForDay) {
                UiEvent.ShowBottomSheetForDay event = (UiEvent.ShowBottomSheetForDay) uiEvent;
                mostrarBottomSheetEventos(event.getDay(), event.getEvents());
            } else if (uiEvent instanceof UiEvent.ShowCreateEventDialog) {
                EventoCreateEditDialogFragment.newInstanceForCreate
                        (((UiEvent.ShowCreateEventDialog) uiEvent).getDay())
                    .show(getSupportFragmentManager(), EventoCreateEditDialogFragment.TAG_CREATE);
            } else if (uiEvent instanceof UiEvent.ShowEditEventDialog) {
                EventoCreateEditDialogFragment.newInstanceForEdit
                        (((UiEvent.ShowEditEventDialog) uiEvent).getEvent())
                    .show(getSupportFragmentManager(), EventoCreateEditDialogFragment.TAG_EDIT);
            } else if (uiEvent instanceof UiEvent.ShowEventDetailsDialog) {
                EventoDetailsDialogFragment.newInstance(((UiEvent.ShowEventDetailsDialog) uiEvent)
                        .getEvent())
                    .show(getSupportFragmentManager(), EventoDetailsDialogFragment.TAG);
            }
            viewModel.onUiEventHandled();
        });

        viewModel.eventosForCalendarView.observe(this, eventosMap -> {
            CalendarDay currentMonthToDecorate = viewModel.currentDisplayMonth.getValue();
            if (currentMonthToDecorate != null && eventosMap != null) {
                applyEventDecorators(currentMonthToDecorate, eventosMap);
            }
        });

        viewModel.currentDisplayMonth.observe(this, displayMonth -> {
            if (displayMonth != null) {
                if (otherMonthDayDecorator != null) {
                    calendarView.removeDecorator(otherMonthDayDecorator);
                }
                otherMonthDayDecorator = new OtherMonthDayDecorator(this,
                    displayMonth.getMonth());
                calendarView.addDecorator(otherMonthDayDecorator);

                Map<CalendarDay, List<Evento>> currentEvents = viewModel.eventosForCalendarView.getValue();
                applyEventDecorators(displayMonth, currentEvents != null ? currentEvents : new HashMap<>());

                CalendarDay selectedDay = viewModel.selectedCalendarDay.getValue();
                updateSelectionDecorator(selectedDay != null && selectedDay
                    .getMonth() == displayMonth.getMonth() ? selectedDay : null);
            }
        });

        viewModel.selectedCalendarDay.observe(this, this::updateSelectionDecorator);
    }

    private void updateSelectionDecorator(CalendarDay dayToSelect) {
        if (currentSelectedDayDecorator != null) {
            calendarView.removeDecorator(currentSelectedDayDecorator);
            currentSelectedDayDecorator = null;
        }
        if (dayToSelect != null) {
            CalendarDay currentCalendarMonth = calendarView.getCurrentDate();
            if (currentCalendarMonth != null && dayToSelect.getMonth() == currentCalendarMonth.getMonth()) {
                currentSelectedDayDecorator = new SelectedDayDecorator(this, dayToSelect);
                calendarView.addDecorator(currentSelectedDayDecorator);
            }
        }
        calendarView.invalidateDecorators();
    }

    private void applyEventDecorators(@NonNull CalendarDay currentMonthDate,
                                      @NonNull Map<CalendarDay, List<Evento>> allEvents) {
        for (DayViewDecorator decorator : activeEventDecorators) {
            calendarView.removeDecorator(decorator);
        }
        activeEventDecorators.clear();

        if (allEvents.isEmpty()) {
            calendarView.invalidateDecorators();
            return;
        }

        int year = currentMonthDate.getYear();
        int month = currentMonthDate.getMonth();

        for (Map.Entry<CalendarDay, List<Evento>> entry : allEvents.entrySet()) {
            CalendarDay day = entry.getKey();
            List<Evento> eventosDelDia = entry.getValue();

            if (day == null || eventosDelDia == null || eventosDelDia.isEmpty()) continue;

            if (day.getYear() == year && day.getMonth() == month) {
                Set<Integer> colores = new HashSet<>();
                for (Evento evento : eventosDelDia) {
                    colores.add(evento.getColor());
                }

                if (!colores.isEmpty()) {
                    Integer backgroundColor = null;
                    for (Evento evento : eventosDelDia) {
                        if ("Descanso".equals(evento.getNombre()) && evento.getColor() == Color
                            .parseColor("#80CBC4")) {
                            backgroundColor = Color.parseColor("#E0F2F1");
                            break;
                        }
                    }
                    MultipleEventDecorator decorador = new MultipleEventDecorator(
                        Set.of(day),
                        colores,
                        backgroundColor,
                        false
                    );
                    calendarView.addDecorator(decorador);
                    activeEventDecorators.add(decorador);
                }
            }
        }
        calendarView.invalidateDecorators();
    }

    @SuppressLint("InflateParams")
    private void mostrarBottomSheetEventos(CalendarDay date, List<Evento> eventosDelDia) {
        try {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_eventos, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            RecyclerView recyclerView = bottomSheetView.findViewById(R.id.recyclerEventos);
            Button btnAddActivity = bottomSheetView.findViewById(R.id.btnAddActivity);

            if (recyclerView == null || btnAddActivity == null) {
                Toast.makeText(this, "Error al cargar la vista del bottom sheet",
                    Toast.LENGTH_SHORT).show();
                return;
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            List<Evento> eventos = eventosDelDia != null ? eventosDelDia : new ArrayList<>();

            eventoAdapter = new EventoAdapter(new ArrayList<>(eventos), evento -> {
                if (evento != null && evento.getIdEvento() != null) {
                    viewModel.requestEventDetailsDialog(evento.getIdEvento());
                    bottomSheetDialog.dismiss();
                }
            });
            recyclerView.setAdapter(eventoAdapter);

            btnAddActivity.setOnClickListener(v -> {
                viewModel.requestCreateEventDialog(date != null ? date : CalendarDay.today());
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        } catch (Exception e) {
            Log.e("HomeActivity", "Error al mostrar eventos: " + e.getMessage(), e);
            Toast.makeText(this, "Ocurrió un error inesperado al cargar los eventos.",
                Toast.LENGTH_SHORT).show();
        }
    }

    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}