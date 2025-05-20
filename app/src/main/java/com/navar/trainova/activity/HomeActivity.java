
package com.navar.trainova.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.navar.trainova.Evento;
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
            .requestIdToken(getString(R.string.client_id))
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
                mostrarBottomSheetEventos(date);
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

            // Crear adaptador con la lista completa de eventos
            EventoAdapter adapter = new EventoAdapter(nombreEventos, eventosDelDia);

            // Configurar el listener para los clics en eventos
            adapter.setOnEventoClickListener((evento, position) -> {
                mostrarDetallesEvento(evento, date);
            });

            recyclerView.setAdapter(adapter);
        } else {
            // Si no hay eventos, crear un adaptador vacío
            EventoAdapter adapter = new EventoAdapter(nombreEventos, new ArrayList<>());
            recyclerView.setAdapter(adapter);
        }

        // Configurar botón para añadir actividad
        Button btnAddActivity = bottomSheetView.findViewById(R.id.btnAddActivity);
        btnAddActivity.setOnClickListener(v -> {
            // Cerrar el bottom sheet actual
            bottomSheetDialog.dismiss();
            // Abrir diálogo para crear nueva actividad
            mostrarDialogoCrearActividad(date);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
    private void mostrarDetallesEvento(Evento evento, CalendarDay date) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.item_evento, null);

        if (dialogView == null) {
            Toast.makeText(this, "Error al cargar la vista de detalles.", Toast.LENGTH_SHORT).show();
            return; // Salir si no se puede inflar el layout esperado
        }
        dialog.setContentView(dialogView);

        // Como sabemos que es item_evento.xml, solo buscamos IDs de ese layout
        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloActividad);
        if (tvTitulo != null) {
            tvTitulo.setText(evento.getNombre());
        } else {
            Log.e("DetallesEvento", "tvTituloActividad no encontrado en R.layout.item_evento");
        }

        // Los botones sí están en item_evento.xml
        Button btnCerrar = dialogView.findViewById(R.id.btnCerrar);
        Button btnEditar = dialogView.findViewById(R.id.btnEditar);

        if (btnCerrar != null) {
            btnCerrar.setText("Cerrar");
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnEditar != null) {
            btnEditar.setText("Editar");
            btnEditar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarDialogoEditarActividad(evento, date);
            });
        }

        dialog.show();
    }
    private void mostrarDialogoEditarActividad(Evento evento, CalendarDay date) {
        // Este método puede ser similar a mostrarDialogoCrearActividad
        // pero pre-poblando los campos con los datos del evento

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_crear_actividad, null);
        dialog.setContentView(dialogView);

        // Obtener referencias a los campos
        final EditText inputNombre = dialogView.findViewById(R.id.inputNombreActividad);
        // ... otros campos

        // Pre-poblar con datos del evento
        inputNombre.setText(evento.getNombre());
        // ... pre-poblar otros campos

        // Botones de acción
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            // Obtener los valores editados
            String nombre = inputNombre.getText().toString().trim();
            // ... otros valores

            // Validaciones
            if (nombre.isEmpty()) {
                inputNombre.setError("Este campo es obligatorio");
                return;
            }

            // Eliminar el evento anterior
            List<Evento> eventosDelDia = eventosMap.get(date);
            if (eventosDelDia != null) {
                eventosDelDia.remove(evento);
            }

            // Crear y añadir el evento actualizado
            Evento eventoActualizado = new Evento(nombre, evento.getColor());
            // TODO: Expandir con más campos si es necesario
            agregarEvento(date, eventoActualizado);

            // Actualizar la vista
            calendarView.invalidateDecorators();
            agregarDecoradores(calendarView.getCurrentDate() != null ?
                calendarView.getCurrentDate() : CalendarDay.today());

            Toast.makeText(HomeActivity.this, "Actividad actualizada correctamente",
                Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogoCrearActividad(CalendarDay date) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_crear_actividad, null);
        dialog.setContentView(dialogView);

        final EditText inputNombre = dialogView.findViewById(R.id.inputNombreActividad);
        final Spinner spinnerTipoActividad = dialogView.findViewById(R.id.spinnerTipoActividad);
        final Spinner spinnerColor = dialogView.findViewById(R.id.spinnerColor);
        final Spinner spinnerEstado = dialogView.findViewById(R.id.spinnerEstado);
        final Button btnHoraInicio = dialogView.findViewById(R.id.btnHoraInicio);
        final Button btnHoraFin = dialogView.findViewById(R.id.btnHoraFin);
        final EditText inputDescripcion = dialogView.findViewById(R.id.inputDescripcion);

        final int[] horaInicio = {0, 0};
        final int[] horaFin = {0, 0};

        setupTipoActividadSpinner(spinnerTipoActividad);
        setupColorSpinner(spinnerColor);
        setupEstadoSpinner(spinnerEstado);

        btnHoraInicio.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                HomeActivity.this,
                (view, hourOfDay, minute) -> {
                    horaInicio[0] = hourOfDay;
                    horaInicio[1] = minute;
                    btnHoraInicio.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                },
                horaInicio[0], horaInicio[1], true);
            timePickerDialog.show();
        });

        btnHoraFin.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                HomeActivity.this,
                (view, hourOfDay, minute) -> {
                    horaFin[0] = hourOfDay;
                    horaFin[1] = minute;
                    btnHoraFin.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                },
                horaFin[0], horaFin[1], true);
            timePickerDialog.show();
        });

        // Botones de acción
        Button btnCancelar;
        Button btnGuardar;

        btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        btnGuardar = dialogView.findViewById(R.id.btnGuardar);


        if (btnCancelar == null || btnGuardar == null) {
            LinearLayout buttonContainer = new LinearLayout(this);
            buttonContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
            buttonContainer.setPadding(16, 16, 16, 16);

            btnCancelar = new Button(this);
            btnCancelar.setText("Cancelar");
            btnCancelar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            btnGuardar = new Button(this);
            btnGuardar.setText("Guardar");
            btnGuardar.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            buttonContainer.addView(btnCancelar);
            buttonContainer.addView(btnGuardar);

            // Obtener el LinearLayout principal dentro del ScrollView
            LinearLayout mainLayout = (LinearLayout) ((ScrollView) dialogView).getChildAt(0);
            mainLayout.addView(buttonContainer);
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = inputNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                inputNombre.setError("Este campo es obligatorio");
                return;
            }

            boolean horaValida = (horaFin[0] > horaInicio[0]) ||
                (horaFin[0] == horaInicio[0] && horaFin[1] > horaInicio[1]);
            if (!horaValida) {
                Toast.makeText(HomeActivity.this, "La hora de fin debe ser posterior a la hora de inicio", Toast.LENGTH_SHORT).show();
                return;
            }

            String tipoActividad = spinnerTipoActividad.getSelectedItem().toString();
            int colorSeleccionado = getSelectedColor(spinnerColor.getSelectedItemPosition());
            String estado = spinnerEstado.getSelectedItem().toString();
            String descripcion = inputDescripcion.getText().toString().trim();

            String nombreCompleto = tipoActividad.equals("General") ? nombre : tipoActividad + ": " + nombre;

            Evento nuevoEvento = new Evento(nombreCompleto, colorSeleccionado);
            // TODO: Expandir Evento para más campos si es necesario
            agregarEvento(date, nuevoEvento);

            calendarView.invalidateDecorators();
            agregarDecoradores(calendarView.getCurrentDate() != null ? calendarView.getCurrentDate() : CalendarDay.today());

            Toast.makeText(HomeActivity.this, "Actividad añadida correctamente", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    // Configurar spinner de tipo de actividad
    private void setupTipoActividadSpinner(Spinner spinner) {
        //Lista de tipos de actividad (ej. cardio, fuerza, resistencia,etc.).
        String[] tiposActividad = getResources().getStringArray(R.array.tipos_actividad);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, tiposActividad);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    // Configurar spinner de estado
    private void setupEstadoSpinner(Spinner spinner) {
        String[] estados = {"Pendiente", "En progreso", "Completado", "Cancelado"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupColorSpinner(Spinner spinner) {
        // Crear un adaptador para el spinner con los colores disponibles
        ArrayList<ColorOption> colorOptions = new ArrayList<>();

        colorOptions.add(new ColorOption("Naranja", Color.parseColor("#FFA000")));
        colorOptions.add(new ColorOption("Rojo", Color.parseColor("#F44336")));
        colorOptions.add(new ColorOption("Verde", Color.parseColor("#4CAF50")));
        colorOptions.add(new ColorOption("Azul", Color.parseColor("#2196F3")));
        colorOptions.add(new ColorOption("Morado", Color.parseColor("#9C27B0")));

        ColorSpinnerAdapter colorAdapter = new ColorSpinnerAdapter(this, colorOptions);
        spinner.setAdapter(colorAdapter);
    }

    private int getSelectedColor(int position) {
        // Devolver el color según la posición seleccionada
        switch (position) {
            case 0:
                return Color.parseColor("#FFA000"); // Naranja
            case 1:
                return Color.parseColor("#F44336"); // Rojo
            case 2:
                return Color.parseColor("#4CAF50"); // Verde
            case 3:
                return Color.parseColor("#2196F3"); // Azul
            case 4:
                return Color.parseColor("#9C27B0"); // Morado
            default:
                return ContextCompat.getColor(this, R.color.colorPrimario);
        }
    }

    // Clase para representar una opción de color en el spinner
    private static class ColorOption {
        String name;
        int colorValue;

        ColorOption(String name, int colorValue) {
            this.name = name;
            this.colorValue = colorValue;
        }
    }

    // Adaptador para el spinner de colores
    private class ColorSpinnerAdapter extends ArrayAdapter<ColorOption> {
        private final ArrayList<ColorOption> colorOptions;

        ColorSpinnerAdapter(Context context, ArrayList<ColorOption> colorOptions) {
            super(context, android.R.layout.simple_spinner_item, colorOptions);
            this.colorOptions = colorOptions;
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        private View getCustomView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            TextView textView = (TextView) convertView;
            ColorOption item = colorOptions.get(position);

            // Configurar texto
            textView.setText(item.name);

            // Añadir un indicador de color
            Drawable colorIndicator = new ColorDrawable(item.colorValue);
            textView.setCompoundDrawablesWithIntrinsicBounds(colorIndicator, null, null, null);
            textView.setCompoundDrawablePadding(16);

            return convertView;
        }
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