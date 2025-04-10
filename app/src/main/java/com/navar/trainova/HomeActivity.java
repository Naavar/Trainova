package com.navar.trainova;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan; // Necesario para SingleEventDecorator
import android.widget.Button;
import android.widget.Toast;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Collection; // Necesario para SingleEventDecorator
import java.util.HashMap;
import java.util.HashSet;  // Necesario para SingleEventDecorator y agregarDecoradores
import java.util.Locale;
import java.util.Map;
import java.util.Set;    // Necesario para agregarDecoradores

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private MaterialCalendarView calendarView;
    // Mapa para almacenar los eventos por día
    private final Map<CalendarDay, Evento> eventos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Autenticación Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            irALogin();
            return; // Importante salir si no hay usuario
        }

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id)) // Asegúrate que R.string.client_id existe
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Botón de cerrar sesión
        Button cerrarSesionButton = findViewById(R.id.cerrarSesionButton);
        cerrarSesionButton.setOnClickListener(v -> cerrarSesion());

        // Inicializar calendario
        calendarView = findViewById(R.id.calendarView);
        configurarCalendario();

        // Cargar eventos (simulados o reales)
        cargarEventosSimulados();

        // Añadir los decoradores al calendario
        agregarDecoradores(); // Llama al método modificado

        // Evento de selección de fecha
        calendarView.setOnDateChangedListener((@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) -> {
            // Muestra la fecha seleccionada (puedes cambiar esto)
            // SimpleDateFormat para formato más legible si quieres:
            // SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            // Toast.makeText(HomeActivity.this, "Fecha: " + sdf.format(date.getDate()), Toast.LENGTH_SHORT).show();

            // Comprobar si hay evento en la fecha seleccionada
            Evento eventoDelDia = eventos.get(date);
            if (eventoDelDia != null) {
                Toast.makeText(HomeActivity.this,
                    "Evento: " + eventoDelDia.getNombre(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this,
                    "No hay evento registrado para este día", Toast.LENGTH_SHORT).show();
            }
            // Aquí puedes añadir lógica para mostrar más detalles o abrir otra pantalla
        });
    }

    private void configurarCalendario() {
        // Configura el formato del título (Mes Año) en español
        calendarView.setTitleFormatter((day) -> {
            // Nota: CalendarDay.getDate() devuelve java.util.Date, pero puede ser confuso.
            // Es más seguro construir la fecha a partir de los componentes de CalendarDay.
            Calendar cal = Calendar.getInstance();
            cal.set(day.getYear(), day.getMonth() - 1, day.getDay()); // Mes es 0-based en Calendar
            java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("MMMM 'de' yyyy", new Locale("es", "ES"));
            return dateFormat.format(cal.getTime());
        });
        // Puedes añadir otras configuraciones aquí (ej. primer día de la semana)
        // calendarView.state().edit().setFirstDayOfWeek(Calendar.MONDAY).commit();
    }

    private void cargarEventosSimulados() {
        // Limpia eventos anteriores si vas a recargar
        eventos.clear();
        Calendar calendar = Calendar.getInstance();

        // Añadir eventos de ejemplo
        // FEBRERO 2025 (Mes 1 en CalendarDay, pero Calendar.FEBRUARY es 1)
        // OJO: Calendar.FEBRUARY es 1, pero los meses en set() son 0-based (Enero=0, Febrero=1)
        // CalendarDay usa meses 1-based (Enero=1, Febrero=2)
        // ¡Cuidado con la confusión entre Calendar (0-11) y CalendarDay (1-12)!

        int year = 2025; // Año de ejemplo

        // Evento 12 Feb 2025
        calendar.set(year, Calendar.FEBRUARY, 12); // Mes 1 (Feb) en Calendar es correcto
        eventos.put(CalendarDay.from(year, 2, 12), // Mes 2 (Feb) en CalendarDay
            new Evento("Convocatoria", Color.parseColor("#FFA000"))); // Naranja

        // Evento 14 Feb 2025
        calendar.set(year, Calendar.FEBRUARY, 14);
        eventos.put(CalendarDay.from(year, 2, 14),
            new Evento("Entrega Interfaz", Color.parseColor("#F44336"))); // Rojo

        // Evento 20 Feb 2025
        calendar.set(year, Calendar.FEBRUARY, 20);
        eventos.put(CalendarDay.from(year, 2, 20),
            new Evento("Curso Leng. Marcas", Color.parseColor("#4CAF50"))); // Verde (cambiado de amarillo)

        // Fines de semana de Febrero 2025 con evento "Descanso" (color diferente al WeekendDecorator)
        int descansoColor = Color.parseColor("#80CBC4"); // Verde azulado claro
        int[] sabadosFeb = {1, 8, 15, 22};
        int[] domingosFeb = {2, 9, 16, 23};

        for (int dia : sabadosFeb) {
            eventos.put(CalendarDay.from(year, 2, dia), new Evento("Descanso", descansoColor));
        }
        for (int dia : domingosFeb) {
            eventos.put(CalendarDay.from(year, 2, dia), new Evento("Descanso", descansoColor));
        }

        // Puedes añadir más eventos aquí...
    }

    // --- MÉTODO MODIFICADO PARA USAR SingleEventDecorator ---
    private void agregarDecoradores() {
        // 1. Limpiar decoradores anteriores para evitar duplicados si se llama de nuevo
        calendarView.removeDecorators();

        // 2. Añadir decorador para fines de semana (se aplicará primero)
        calendarView.addDecorator(new WeekendDecorator());

        // 3. Agrupar los días de eventos por el color del evento
        Map<Integer, Set<CalendarDay>> diasPorColor = new HashMap<>();

        if (eventos != null) {
            for (Map.Entry<CalendarDay, Evento> entry : eventos.entrySet()) {
                CalendarDay dia = entry.getKey();
                Evento evento = entry.getValue();
                if (evento != null) {
                    int colorEvento = evento.getColor();
                    // Obtiene el set de días para este color, o crea uno nuevo si no existe
                    Set<CalendarDay> diasConEsteColor = diasPorColor.computeIfAbsent(colorEvento, k -> new HashSet<>());
                    diasConEsteColor.add(dia);
                    // Alternativa con computeIfAbsent (más conciso):
                    // diasPorColor.computeIfAbsent(colorEvento, k -> new HashSet<>()).add(dia);
                }
            }
        }

        // 4. Crear y añadir un SingleEventDecorator para cada grupo de color
        for (Map.Entry<Integer, Set<CalendarDay>> entry : diasPorColor.entrySet()) {
            int colorGrupo = entry.getKey();
            Set<CalendarDay> diasDelGrupo = entry.getValue();

            // Crear el decorador específico para este color y este conjunto de días
            SingleEventDecorator decorator = new SingleEventDecorator(colorGrupo, diasDelGrupo);
            calendarView.addDecorator(decorator);
            // Nota: Este decorador se añadirá DESPUÉS del WeekendDecorator.
            // Si un día es fin de semana Y tiene evento, el estilo del SingleEventDecorator
            // (color de fondo del evento) sobrescribirá al del WeekendDecorator (fondo gris).
            // Si quisieras lo contrario, añade WeekendDecorator al final.
        }

        // 5. Opcional: Invalidar decoradores si la actualización no es automática
        // calendarView.invalidateDecorators();
    }

    // --- Métodos de Sesión ---
    private void cerrarSesion() {
        mAuth.signOut();
        // También cerrar sesión de Google para que pida elegir cuenta la próxima vez
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(HomeActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irALogin();
        });
    }

    private void irALogin() {
        Intent intent = new Intent(HomeActivity.this, AuthActivity.class); // Asume que tienes AuthActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Cierra HomeActivity
    }

    // --- CLASES INTERNAS ESTÁTICAS ---

    // Clase simple para representar un Evento
    public static class Evento {
        private final String nombre;
        private final int color;

        public Evento(String nombre, int color) {
            this.nombre = nombre;
            this.color = color;
        }
        public String getNombre() { return nombre; }
        public int getColor() { return color; }
    }

    // Decorador para fines de semana (fondo gris claro)
    private static class WeekendDecorator implements DayViewDecorator {
        private final ColorDrawable weekendBackground;
        private final Calendar calendar = Calendar.getInstance(); // Reutilizar instancia

        public WeekendDecorator() {
            weekendBackground = new ColorDrawable(Color.parseColor("#F0F0F0"));
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            // Usar set() para configurar el día en el Calendar reutilizado
            calendar.set(day.getYear(), day.getMonth() - 1, day.getDay()); // Mes - 1 !!
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.setBackgroundDrawable(weekendBackground);
        }
    }

    // Decorador para aplicar UN estilo (color) a un CONJUNTO de días
    public static class SingleEventDecorator implements DayViewDecorator {
        private final int color;
        private final HashSet<CalendarDay> days;

        public SingleEventDecorator(int eventColor, @NonNull Collection<CalendarDay> daysToDecorate) {
            this.color = eventColor;
            this.days = new HashSet<>(daysToDecorate);
        }

        @Override
        public boolean shouldDecorate(@NonNull CalendarDay day) {
            return days.contains(day);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            // Aplica color de fondo y texto blanco
            view.setBackgroundDrawable(new ColorDrawable(color));
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
            // Opcional: Añadir un punto indicador en lugar del fondo (comenta lo anterior si usas esto)
            // import com.prolificinteractive.materialcalendarview.spans.DotSpan;
            // view.addSpan(new DotSpan(8, color)); // Radio 8px, con el color del evento
        }
    }

} // Fin de HomeActivity