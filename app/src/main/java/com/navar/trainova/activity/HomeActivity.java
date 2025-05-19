package com.navar.trainova.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.navar.trainova.EventoAdapter;
import com.navar.trainova.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private CalendarDay fechaSeleccionada; // Para guardar la fecha seleccionada
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private MaterialCalendarView calendarView;

    // Mapa que almacena una lista de eventos para cada día
    private final Map<CalendarDay, List<Evento>> eventosMap = new HashMap<>();

    // Guardar referencia al decorador de selección para quitarlo específicamente
    private SelectedDayDecorator selectedDayDecoratorInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Inicialización Auth y UI ---
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            irALogin();
            return;
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)) // Asegúrate que R.string.client_id existe
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button cerrarSesionButton = findViewById(R.id.cerrarSesionButton);
        cerrarSesionButton.setOnClickListener(v -> cerrarSesion());

        calendarView = findViewById(R.id.calendarView);

        // --- Configuración Inicial Calendario ---
        configurarCalendario(); // Configura formato título, selección transparente, etc.
        cargarEventosSimulados(); // Carga TODOS los eventos en el mapa `eventosMap`

        // Aplicar decoradores por primera vez para el mes inicial
        // Pasa la fecha actual para que filtre eventos relevantes
        if (calendarView.getCurrentDate() != null) { // Añadir null check por seguridad
            agregarDecoradores(calendarView.getCurrentDate());
        } else {
            // Fallback si getCurrentDate es null al inicio (raro)
            agregarDecoradores(CalendarDay.today());
        }

        calendarView.setOnDateChangedListener((@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) -> {
            // Actualizar fecha seleccionada
            fechaSeleccionada = date;

            // Obtener eventos del día
            List<Evento> eventosDelDia = eventosMap.get(date);

            if (widget.getCurrentDate() != null && date.getMonth() == widget.getCurrentDate().getMonth()) {
                if (eventosDelDia == null || eventosDelDia.isEmpty()) {
                    // Mostrar Toast solo si NO hay eventos
                    Toast.makeText(HomeActivity.this, "No hay evento registrado", Toast.LENGTH_SHORT).show();
                } else {
                    // Mostrar BottomSheetDialog con los eventos del día
                    mostrarBottomSheetEventos(date);
                }
            }

            // --- Gestión eficiente del decorador de selección ---
            if (selectedDayDecoratorInstance != null) {
                calendarView.removeDecorator(selectedDayDecoratorInstance);
            }

            if (selected) {
                selectedDayDecoratorInstance = new SelectedDayDecorator(this, fechaSeleccionada);
                calendarView.addDecorator(selectedDayDecoratorInstance);
            } else {
                selectedDayDecoratorInstance = null;
            }
        });

        calendarView.setOnMonthChangedListener((widget, date) -> {
            // Al cambiar de mes, 'date' es el primer día del nuevo mes visible
            // Volver a aplicar decoradores FILTRANDO para el nuevo mes
            agregarDecoradores(date); // <--- Se pasa la fecha del nuevo mes

            // Volver a aplicar el decorador de selección si había uno y si sigue visible
            if (selectedDayDecoratorInstance != null && fechaSeleccionada != null && fechaSeleccionada.getMonth() == date.getMonth()) {
                // Re-añadir la instancia existente si el mes coincide
                calendarView.addDecorator(selectedDayDecoratorInstance);
            }
        });
    }

    private void configurarCalendario() {
        calendarView.setTitleFormatter((day) -> {
            Calendar cal = Calendar.getInstance();
            // Comprobación defensiva
            if (day == null) return "";
            cal.set(day.getYear(), day.getMonth() - 1, day.getDay());
            Locale localeSpanish = new Locale("es", "ES");
            // Formato correcto
            java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("MMMM 'de' yyyy", localeSpanish);
            return dateFormat.format(cal.getTime()).toUpperCase();
        });
    }

    private void cargarEventosSimulados() {
        // Carga TODOS los eventos en la variable `eventosMap`
        eventosMap.clear();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        int nextMonthYear = currentMonth == 12 ? year + 1 : year;

        int colorNaranja = Color.parseColor("#FFA000");
        int colorRojo = Color.parseColor("#F44336");
        int colorVerde = Color.parseColor("#4CAF50");
        int colorDescanso = Color.parseColor("#80CBC4");
        int colorPrimario = ContextCompat.getColor(this, R.color.colorPrimario);

        // Método helper para añadir eventos
        agregarEvento(CalendarDay.from(year, currentMonth, 12), new Evento("Convocatoria", colorNaranja));
        agregarEvento(CalendarDay.from(year, currentMonth, 14), new Evento("Entrega Interfaz", colorRojo));
        agregarEvento(CalendarDay.from(year, currentMonth, 14), new Evento("Prueba", colorRojo));
        agregarEvento(CalendarDay.from(year, currentMonth, 14), new Evento("Prueba2", colorRojo));
        agregarEvento(CalendarDay.from(year, currentMonth, 20), new Evento("Curso Leng. Marcas", colorVerde));
        agregarEvento(CalendarDay.from(year, currentMonth, 15), new Evento("Reunión Proyecto", colorPrimario));
        agregarEvento(CalendarDay.from(year, currentMonth, 22), new Evento("Demo Cliente", colorVerde));

        // Eventos Mes Siguiente (ej. Mayo 2025)
        agregarEvento(CalendarDay.from(nextMonthYear, nextMonth, 5), new Evento("Inicio Sprint", colorNaranja));
        agregarEvento(CalendarDay.from(nextMonthYear, nextMonth, 10), new Evento("Planning", colorPrimario));

        // Descansos Mes Actual (ej. Abril 2025)
        int[] sabadosAbr = {5, 12, 19, 26}; // Ajusta a los sábados reales
        int[] domingosAbr = {6, 13, 20, 27}; // Ajusta a los domingos reales
        for (int dia : sabadosAbr)
            agregarEvento(CalendarDay.from(year, currentMonth, dia), new Evento("Descanso", colorDescanso));
        for (int dia : domingosAbr)
            agregarEvento(CalendarDay.from(year, currentMonth, dia), new Evento("Descanso", colorDescanso));
    }

    // Método helper para añadir un evento a la lista de eventos de un día
    private void agregarEvento(CalendarDay day, Evento evento) {
        if (!eventosMap.containsKey(day)) {
            eventosMap.put(day, new ArrayList<>());
        }
        eventosMap.get(day).add(evento);
    }

    private void mostrarBottomSheetEventos(CalendarDay date) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_eventos, null);

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.recyclerEventos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener la lista de eventos del día
        List<Evento> eventosDelDia = eventosMap.get(date);
        List<String> nombreEventos = new ArrayList<>();

        if (eventosDelDia != null && !eventosDelDia.isEmpty()) {
            for (Evento evento : eventosDelDia) {
                nombreEventos.add(evento.getNombre());
            }
        }

        EventoAdapter adapter = new EventoAdapter(nombreEventos);
        recyclerView.setAdapter(adapter);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // --- MÉTODO agregarDecoradores (actualizado para múltiples eventos) ---
    private final Set<DayViewDecorator> decoradoresActuales = new HashSet<>();

    private void agregarDecoradores(@NonNull CalendarDay currentMonthDate) {
        // Elimina únicamente los decoradores personalizados previos (no todos)
        for (DayViewDecorator decorador : decoradoresActuales) {
            calendarView.removeDecorator(decorador);
        }
        decoradoresActuales.clear();

        // --- Añadir Decoradores Base (siempre presentes) ---
        DayViewDecorator baseDecorator = new BaseDayDecorator(this);
        DayViewDecorator otherMonthDecorator = new OtherMonthDayDecorator(this, calendarView);
        calendarView.addDecorator(baseDecorator);
        calendarView.addDecorator(otherMonthDecorator);
        decoradoresActuales.add(baseDecorator);
        decoradoresActuales.add(otherMonthDecorator);

        // --- Agrupar eventos por día y color (solo para mes actual) ---
        if (eventosMap == null || eventosMap.isEmpty()) return;

        int anyo = currentMonthDate.getYear();
        int mes = currentMonthDate.getMonth();

        Map<CalendarDay, Set<Integer>> coloresPorDia = new HashMap<>();

        for (Map.Entry<CalendarDay, List<Evento>> entry : eventosMap.entrySet()) {
            CalendarDay dia = entry.getKey();
            List<Evento> eventosDelDia = entry.getValue();

            if (dia == null || eventosDelDia == null || eventosDelDia.isEmpty()) continue;

            if (dia.getYear() == anyo && dia.getMonth() == mes) {
                Set<Integer> colores = new HashSet<>();
                for (Evento evento : eventosDelDia) {
                    colores.add(evento.getColor());
                }
                coloresPorDia.put(dia, colores);
            }
        }

        // Crear y añadir decoradores solo para los días con eventos
        for (Map.Entry<CalendarDay, Set<Integer>> entry : coloresPorDia.entrySet()) {
            DayViewDecorator decorador = new SingleEventDecorator(entry.getValue(), entry.getKey());
            calendarView.addDecorator(decorador);
            decoradoresActuales.add(decorador);
        }
    }

    // --- Métodos de Sesión ---
    private void cerrarSesion() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(HomeActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irALogin();
        });
    }

    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- CLASES INTERNAS / DECORADORES ---

    public static class Evento {
        private final String nombre;
        private final int color;

        public Evento(String nombre, int color) {
            this.nombre = nombre;
            this.color = color;
        }

        public String getNombre() {
            return nombre;
        }

        public int getColor() {
            return color;
        }
    }

    // Decorador Base (con null check y clonado)
    public static class BaseDayDecorator implements DayViewDecorator {
        private final Drawable baseDrawable;

        public BaseDayDecorator(@NonNull Context context) {
            baseDrawable = ContextCompat.getDrawable(context, R.drawable.drawable_day_cell_base);
            if (baseDrawable == null) {
                Log.e("HomeActivity", "Drawable R.drawable.drawable_day_cell_base no encontrado!");
            }
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            return true;
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            if (baseDrawable != null && baseDrawable.getConstantState() != null) {
                try {
                    view.setBackgroundDrawable(baseDrawable.getConstantState().newDrawable().mutate());
                } catch (Exception e) {
                    Log.e("HomeActivity", "Error clonando baseDrawable", e);
                }
            }
        }
    }

    // Decorador Otros Meses (sin cambios, usa el mes actual de la vista)
    public static class OtherMonthDayDecorator implements DayViewDecorator {
        private final int otherMonthColor;
        private final int currentMonth;

        public OtherMonthDayDecorator(@NonNull Context context, @NonNull MaterialCalendarView calendarView) {
            this.otherMonthColor = ContextCompat.getColor(context, R.color.colorOtherMonthDayText);
            // Obtiene el mes que la vista está mostrando AHORA mismo
            CalendarDay currentDate = calendarView.getCurrentDate();
            this.currentMonth = (currentDate != null) ? currentDate.getMonth() : CalendarDay.today().getMonth(); // Fallback
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            return day.getMonth() != currentMonth;
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(otherMonthColor));
            view.setDaysDisabled(true);
        }
    }

    // Decorador de Eventos
    public static class SingleEventDecorator implements DayViewDecorator {
        private final Set<Integer> colors;
        private final CalendarDay day;

        public SingleEventDecorator(Set<Integer> eventColors, CalendarDay day) {
            this.colors = eventColors;
            this.day = day;
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            // Comprobar nulls por si acaso
            return this.day != null && this.day.equals(day);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            float dotRadius = 4;
            if (colors != null) {
                for (int color : colors) {
                    view.addSpan(new DotSpan(dotRadius, color));
                }
            }
        }
    }

    // Decorador Selección (con null check y clonado)
    public static class SelectedDayDecorator implements DayViewDecorator {
        private final CalendarDay selectedDay;
        private final Drawable selectionDrawable;

        public SelectedDayDecorator(@NonNull Context context, CalendarDay day) {
            this.selectedDay = day;
            this.selectionDrawable = ContextCompat.getDrawable(context, R.drawable.drawable_day_selection);
            if (selectionDrawable == null) {
                Log.e("HomeActivity", "Drawable R.drawable.drawable_day_selection no encontrado!");
            }
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            return day.equals(selectedDay);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            if (selectionDrawable != null && selectionDrawable.getConstantState() != null) {
                try {
                    view.setBackgroundDrawable(selectionDrawable.getConstantState().newDrawable().mutate());
                } catch (Exception e) {
                    Log.e("HomeActivity", "Error clonando selectionDrawable", e);
                }
            }
        }
    }
}
