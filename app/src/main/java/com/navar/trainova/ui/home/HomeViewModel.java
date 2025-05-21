package com.navar.trainova.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.navar.trainova.R;
import com.navar.trainova.data.model.ColorOption;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.data.repository.EventoRepository;
import com.navar.trainova.data.repository.EventoRepositoryImpl;
import com.navar.trainova.util.UiEvent;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.res.TypedArray;

/**
 * ViewModel para la HomeActivity.
 * Gestiona la lógica de negocio y los datos relacionados con la pantalla principal
 * de la aplicación, incluyendo la autenticación del usuario, la gestión de eventos
 * y la interacción con el calendario.
 */
public class HomeViewModel extends AndroidViewModel {

    private final FirebaseAuth mAuth; // Instancia de Firebase Authentication
    private final GoogleSignInClient mGoogleSignInClient; // Cliente para Google Sign-In
    private final EventoRepository eventoRepository; // Repositorio para la gestión de eventos

    /** LiveData privado y público para el usuario actual */
    private final MutableLiveData<FirebaseUser> _currentUser = new MutableLiveData<>();

    /**  LiveData para el día del calendario actualmente seleccionado */
    private final MutableLiveData<CalendarDay> _selectedCalendarDay = new MutableLiveData<>();
    public LiveData<CalendarDay> selectedCalendarDay = _selectedCalendarDay;

    /** LiveData para el mes del calendario actualmente visible */
    private final MutableLiveData<CalendarDay> _currentDisplayMonth = new MutableLiveData<>();
    public LiveData<CalendarDay> currentDisplayMonth = _currentDisplayMonth;

    /** LiveData para los eventos agrupados por día, utilizados por la vista del calendario */
    public LiveData<Map<CalendarDay, List<Evento>>> eventosForCalendarView;

    /** LiveData para eventos de la UI que la actividad debe observar y reaccionar */
    private final MutableLiveData<UiEvent> _uiEvent = new MutableLiveData<>();
    public LiveData<UiEvent> uiEvent = _uiEvent;

    /** LiveData para las opciones de color disponibles para los eventos */
    private final MutableLiveData<List<ColorOption>> _colorOptions = new MutableLiveData<>();
    public LiveData<List<ColorOption>> colorOptions = _colorOptions;


    /**
     * Constructor para HomeViewModel.
     * @param application La instancia de Application.
     */
    public HomeViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        eventoRepository = new EventoRepositoryImpl();
        // Carga datos iniciales o sincroniza el repositorio
        eventoRepository.loadInitialData(application.getApplicationContext());

        // Configura Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.client_id)) // Solicita el token de ID para Firebase
            .requestEmail() // Solicita el correo electrónico del usuario
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(application, gso);

        // Establece el usuario actual y navega al login si no hay sesión iniciada
        _currentUser.setValue(mAuth.getCurrentUser());
        if (mAuth.getCurrentUser() == null) {
            _uiEvent.setValue(new UiEvent.NavigateToLogin(true));
        }

        // Obtiene todos los eventos agrupados por día desde el repositorio para la vista del calendario
        eventosForCalendarView = eventoRepository.getAllEventosGroupedByDay();
        loadColorOptions();
    }

    /**
     * Carga una lista predefinida de opciones de color para que el usuario las elija
     * al crear o editar eventos.
     */
    private void loadColorOptions() {
        ArrayList<ColorOption> options = new ArrayList<>();
        String[] colorNames = getApplication().getResources().getStringArray(R.array.color_names);
        // TypedArray para obtener colores
        TypedArray colorValues = getApplication().getResources().obtainTypedArray(R.array.color_values);

        for (int i = 0; i < colorNames.length; i++) {

            if (i < colorValues.length()) {
                int color = colorValues.getColor(i, 0);
                options.add(new ColorOption(colorNames[i], color));
            }
        }
        colorValues.recycle();
        _colorOptions.setValue(options);
    }

    /**
     * Inicializa el estado del calendario en el ViewModel.
     * @param initialMonth El CalendarDay que representa el mes inicial a mostrar.
     */
    public void initializeCalendar(CalendarDay initialMonth) {
        _currentDisplayMonth.setValue(initialMonth != null ? initialMonth : CalendarDay.today());
    }

    /**
     * Maneja el cambio de fecha en el calendario.
     * Actualiza el día seleccionado y, si un día es seleccionado en el mes visible,
     * solicita mostrar el BottomSheet de eventos para ese día.
     * @param date El CalendarDay que ha sido seleccionado o deseleccionado.
     * @param selected true si el día fue seleccionado, false si fue deseleccionado.
     * @param currentCalendarViewMonth El mes actualmente visible en el calendario.
     */
    public void handleDateChanged(CalendarDay date, boolean selected, CalendarDay currentCalendarViewMonth) {
        _selectedCalendarDay.setValue(selected ? date : null);

        // Si el día seleccionado está en el mes actualmente visible
        if (date.getMonth() == currentCalendarViewMonth.getMonth()) {
            List<Evento> eventosDelDia = new ArrayList<>();
            Map<CalendarDay, List<Evento>> allEvents = eventosForCalendarView.getValue();
            if (allEvents != null && allEvents.get(date) != null) {
                eventosDelDia.addAll(allEvents.get(date));
            }

            // Si el día fue seleccionado, solicita mostrar el BottomSheet
            if (selected) {
                _uiEvent.setValue(new UiEvent.ShowBottomSheetForDay(date, eventosDelDia));
            }
        }
    }

    /**
     * Maneja el cambio de mes en el calendario.
     * Actualiza el mes actualmente visible y deselecciona el día si no está en el nuevo mes.
     * @param newMonthDate El CalendarDay que representa el nuevo mes visible.
     */
    public void handleMonthChanged(CalendarDay newMonthDate) {
        _currentDisplayMonth.setValue(newMonthDate);
        // Deselecciona el día si el día seleccionado ya no está en el mes actual
        if (_selectedCalendarDay.getValue() != null && _selectedCalendarDay.getValue().getMonth() != newMonthDate.getMonth()) {
            _selectedCalendarDay.setValue(null);
        }
    }

    /**
     * Solicita a la UI que muestre el diálogo para crear un nuevo evento para un día específico.
     * @param day El CalendarDay para el cual se creará el evento.
     */
    public void requestCreateEventDialog(CalendarDay day) {
        _uiEvent.setValue(new UiEvent.ShowCreateEventDialog(day));
    }

    /**
     * Solicita a la UI que muestre el diálogo para editar un evento existente.
     * Busca el evento por su ID y notifica a la UI.
     * @param eventoId El ID del evento a editar.
     */
    public void requestEditEventDialog(String eventoId) {
        Evento evento = eventoRepository.findEventoById(eventoId);
        if (evento != null) {
            _uiEvent.setValue(new UiEvent.ShowEditEventDialog(evento));
        } else {
            _uiEvent.setValue(new UiEvent.ShowToast("Evento no encontrado."));
        }
    }

    /**
     * Solicita a la UI que muestre el diálogo con los detalles de un evento.
     * Busca el evento por su ID y notifica a la UI.
     * @param eventoId El ID del evento cuyos detalles se mostrarán.
     */
    public void requestEventDetailsDialog(String eventoId) {
        Evento evento = eventoRepository.findEventoById(eventoId);
        if (evento != null) {
            _uiEvent.setValue(new UiEvent.ShowEventDetailsDialog(evento));
        } else {
            _uiEvent.setValue(new UiEvent.ShowToast("Evento no encontrado."));
        }
    }


    /**
     * Guarda un nuevo evento en el repositorio.
     * Realiza validaciones básicas y notifica a la UI sobre el resultado.
     * @param day El CalendarDay al que pertenece el evento.
     * @param nombre El nombre del evento.
     * @param tipo El tipo de actividad del evento.
     * @param color El color asociado al evento.
     * @param estado El estado del evento.
     * @param horaInicio La hora de inicio del evento (formato HH:MM).
     * @param horaFin La hora de fin del evento (formato HH:MM).
     * @param descripcion La descripción del evento.
     */
    public void saveEvento(
        CalendarDay day,
        String nombre,
        String tipo,
        int color,
        String estado,
        String horaInicio,
        String horaFin,
        String descripcion) {
        if (nombre.trim().isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("El nombre es obligatorio."));
            return;
        }
        // Valida que la hora de fin sea posterior a la de inicio, a menos que ambas estén vacías.
        boolean horaValida = isHoraFinAfterHoraInicio(horaInicio, horaFin);
        if (!horaValida && !(horaInicio.isEmpty() && horaFin.isEmpty())) {
            _uiEvent.setValue(new UiEvent.ShowToast("La hora de fin debe ser posterior a la de inicio."));
            return;
        }

        Evento nuevoEvento = new Evento(day, nombre, tipo, color, estado, horaInicio, horaFin, descripcion);
        eventoRepository.addEvento(nuevoEvento); // Añade el evento al repositorio
        _uiEvent.setValue(new UiEvent.ShowToast("Actividad añadida.")); // Notifica a la UI
    }

    /**
     * Actualiza un evento existente en el repositorio.
     * Realiza validaciones básicas y notifica a la UI sobre el resultado.
     * @param id El ID del evento a actualizar.
     * @param day El CalendarDay al que pertenece el evento.
     * @param nombre El nuevo nombre del evento.
     * @param tipo El nuevo tipo de actividad del evento.
     * @param color El nuevo color asociado al evento.
     * @param estado El nuevo estado del evento.
     * @param horaInicio La nueva hora de inicio del evento.
     * @param horaFin La nueva hora de fin del evento.
     * @param descripcion La nueva descripción del evento.
     */
    public void updateEvento(String id, CalendarDay day, String nombre, String tipo, int color, String estado, String horaInicio, String horaFin, String descripcion) {
        if (nombre.trim().isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("El nombre es obligatorio."));
            return;
        }
        // Valida que la hora de fin sea posterior a la de inicio, a menos que ambas estén vacías.
        boolean horaValida = isHoraFinAfterHoraInicio(horaInicio, horaFin);
        if (!horaValida && !(horaInicio.isEmpty() && horaFin.isEmpty())) {
            _uiEvent.setValue(new UiEvent.ShowToast("La hora de fin debe ser posterior a la de inicio."));
            return;
        }
        Evento eventoActualizado = new Evento(id, day, nombre, tipo, color, estado, horaInicio, horaFin, descripcion);
        eventoRepository.updateEvento(eventoActualizado); // Actualiza el evento en el repositorio
        _uiEvent.setValue(new UiEvent.ShowToast("Actividad actualizada.")); // Notifica a la UI
    }

    /**
     * Elimina un evento del repositorio.
     * @param eventoId El ID del evento a eliminar.
     */
    public void deleteEvento(String eventoId) {
        eventoRepository.deleteEvento(eventoId); // Elimina el evento del repositorio
        _uiEvent.setValue(new UiEvent.ShowToast("Actividad eliminada.")); // Notifica a la UI
    }

    /**
     * Verifica si la hora de fin es posterior a la hora de inicio.
     * Permite que ambas horas estén vacías. Si una está vacía y la otra no, o si el formato es incorrecto,
     * se considera inválido.
     * @param inicio La hora de inicio en formato HH:MM.
     * @param fin La hora de fin en formato HH:MM.
     * @return true si la hora de fin es posterior a la de inicio, o si ambas están vacías; false en caso contrario.
     */
    private boolean isHoraFinAfterHoraInicio(String inicio, String fin) {
        // Si ambas son nulas o vacías, se considera válido (no hay conflicto de horas)
        if ((inicio == null || inicio.isEmpty()) && (fin == null || fin.isEmpty())) {
            return true;
        }
        // Si una es nula/vacía y la otra no, o si el formato no es HH:MM
        if (inicio == null || fin == null || inicio.isEmpty() || fin.isEmpty() || !inicio.contains(":") || !fin.contains(":")) {
            return false; // Se considera inválido porque no se pueden comparar
        }
        try {
            String[] inicioParts = inicio.split(":");
            String[] finParts = fin.split(":");
            int hInicio = Integer.parseInt(inicioParts[0]);
            int mInicio = Integer.parseInt(inicioParts[1]);
            int hFin = Integer.parseInt(finParts[0]);
            int mFin = Integer.parseInt(finParts[1]);

            if (hFin > hInicio) return true; // La hora de fin es posterior
            return hFin == hInicio && mFin > mInicio; // Misma hora, pero minuto de fin posterior
// La hora de fin no es posterior a la de inicio
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Error en el parseo de las horas, se considera inválido
            return false;
        }
    }


    /**
     * Cierra la sesión actual del usuario en Firebase y Google Sign-In.
     * Notifica a la UI para navegar a la pantalla de login.
     */
    public void cerrarSesion() {
        mAuth.signOut(); // Cierra sesión de Firebase
        // Cierra sesión de Google y notifica a la UI
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            _currentUser.setValue(null); // Borra el usuario actual del LiveData
            _uiEvent.setValue(new UiEvent.NavigateToLogin(true)); // Solicita navegar al login
            _uiEvent.setValue(new UiEvent.ShowToast("Sesión cerrada")); // Muestra un mensaje de sesión cerrada
        });
    }

    /**
     * Resetea el evento de la UI a nulo.
     * Debe llamarse después de que la actividad haya manejado un UiEvent para evitar que se
     * dispare de nuevo en cambios de configuración o recreación de la actividad.
     */
    public void onUiEventHandled() {
        _uiEvent.setValue(null);
    }
}