package com.navar.trainova.ui.home;

import android.app.Application;
import android.util.Log;

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
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.data.model.ColorOption;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.data.repository.CatalogoRepository;
import com.navar.trainova.data.repository.EventoRepository;
import com.navar.trainova.data.repository.FirestoreCatalogoRepository;
import com.navar.trainova.data.repository.FirestoreEventoRepository;
import com.navar.trainova.util.UiEvent;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.res.TypedArray;

public class HomeViewModel extends AndroidViewModel {

    private final FirebaseAuth mAuth;
    private final GoogleSignInClient mGoogleSignInClient;
    private final EventoRepository eventoRepository;
    private final CatalogoRepository catalogoRepository;

    private final MutableLiveData<FirebaseUser> _currentUser = new MutableLiveData<>();
    private final MutableLiveData<CalendarDay> _selectedCalendarDay = new MutableLiveData<>();
    public LiveData<CalendarDay> selectedCalendarDay = _selectedCalendarDay;
    private final MutableLiveData<CalendarDay> _currentDisplayMonth = new MutableLiveData<>();
    public LiveData<CalendarDay> currentDisplayMonth = _currentDisplayMonth;
    public LiveData<Map<CalendarDay, List<Evento>>> eventosForCalendarView;
    private final MutableLiveData<UiEvent> _uiEvent = new MutableLiveData<>();
    public LiveData<UiEvent> uiEvent = _uiEvent;
    private final MutableLiveData<List<ColorOption>> _colorOptions = new MutableLiveData<>();
    public LiveData<List<ColorOption>> colorOptions = _colorOptions;
    private final LiveData<List<CatalogoEvento>> catalogoLiveData;

    private String currentUserId;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        eventoRepository = new FirestoreEventoRepository();
        catalogoRepository = new FirestoreCatalogoRepository();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(application, gso);

        eventosForCalendarView = eventoRepository.getAllEventosGroupedByDay();
        loadColorOptions();

        catalogoLiveData = catalogoRepository.getCatalogLiveData();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        _currentUser.setValue(firebaseUser);

        if (firebaseUser != null) {
            this.currentUserId = firebaseUser.getUid();
            Log.d("HomeViewModel", "Usuario ViewModel inicializado con UID: " + this.currentUserId);

            ((FirestoreEventoRepository) eventoRepository).loadAndObserveEventsForUser(this.currentUserId);
            catalogoRepository.loadAndObserveCombinedCatalog(this.currentUserId);

        } else {
            Log.w("HomeViewModel", "No hay usuario autenticado. Se emitirá NavigateToLogin.");
            _uiEvent.setValue(new UiEvent.NavigateToLogin(true));
            ((FirestoreEventoRepository) eventoRepository).loadAndObserveEventsForUser(null);
            catalogoRepository.loadAndObserveCombinedCatalog(null);
        }
    }

    public LiveData<List<CatalogoEvento>> getCatalogo() {
        return catalogoLiveData;
    }

    public void createPersonalTemplate(String nombre, String descripcion, String duracion,
                                       String tipo, int color) {
        if (nombre.trim().isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("El nombre de la plantilla es obligatorio."));
            return;
        }

        CatalogoEvento nuevaPlantilla = new CatalogoEvento(nombre, descripcion, duracion,
            tipo, color, null);

        catalogoRepository.createPersonalTemplate(nuevaPlantilla, (success, message) -> {
            if (success) {
                _uiEvent.setValue(new UiEvent.ShowToast("Plantilla creada con éxito."));
            } else {
                _uiEvent.setValue(new UiEvent.ShowToast("Error: " + message));
            }
        });
    }

    public void deletePersonalTemplate(CatalogoEvento plantilla) {
        if (plantilla == null || plantilla.getId() == null) {
            _uiEvent.setValue(new UiEvent.ShowToast("Error: Plantilla no válida."));
            return;
        }

        if (plantilla.getUidCreador() == null || !plantilla.getUidCreador().equals(this.currentUserId)) {
            _uiEvent.setValue(new UiEvent.ShowToast("No puedes borrar una plantilla general."));
            return;
        }

        catalogoRepository.deletePersonalTemplate(plantilla.getId(), (success, message) -> {
            if (success) {
                _uiEvent.setValue(new UiEvent.ShowToast("Plantilla borrada."));
            } else {
                _uiEvent.setValue(new UiEvent.ShowToast("Error: " + message));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ((FirestoreEventoRepository) eventoRepository).removeListeners();
        catalogoRepository.removeListeners();
        Log.d("HomeViewModel", "ViewModel cleared, listeners removidos.");
    }

    private void loadColorOptions() {
        ArrayList<ColorOption> options = new ArrayList<>();
        String[] colorNames = getApplication().getResources().getStringArray(R.array.color_names);
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

    public void initializeCalendar(CalendarDay initialMonth) {
        _currentDisplayMonth.setValue(initialMonth != null ? initialMonth : CalendarDay.today());
    }

    public void handleDateChanged(CalendarDay date, boolean selected, CalendarDay currentCalendarViewMonth) {
        _selectedCalendarDay.setValue(selected ? date : null);

        if (date.getMonth() == currentCalendarViewMonth.getMonth()) {
            List<Evento> eventosDelDia = new ArrayList<>();
            Map<CalendarDay, List<Evento>> allEvents = eventosForCalendarView.getValue();
            if (allEvents != null && allEvents.get(date) != null) {
                eventosDelDia.addAll(allEvents.get(date));
            }

            if (selected) {
                _uiEvent.setValue(new UiEvent.ShowBottomSheetForDay(date, eventosDelDia));
            }
        }
    }

    public void handleMonthChanged(CalendarDay newMonthDate) {
        _currentDisplayMonth.setValue(newMonthDate);
        if (_selectedCalendarDay.getValue() != null && _selectedCalendarDay.getValue()
            .getMonth() != newMonthDate.getMonth()) {
            _selectedCalendarDay.setValue(null);
        }
    }

    public void requestCreateEventDialog(CalendarDay day) {
        _uiEvent.setValue(new UiEvent.ShowCreateEventDialog(day));
    }

    public void requestEditEventDialog(String eventoId) {
        Evento evento = eventoRepository.findEventoById(eventoId);
        if (evento != null) {
            _uiEvent.setValue(new UiEvent.ShowEditEventDialog(evento));
        } else {
            _uiEvent.setValue(new UiEvent.ShowToast("Evento no encontrado."));
        }
    }

    public void requestEventDetailsDialog(String eventoId) {
        Evento evento = eventoRepository.findEventoById(eventoId);
        if (evento != null) {
            _uiEvent.setValue(new UiEvent.ShowEventDetailsDialog(evento));
        } else {
            _uiEvent.setValue(new UiEvent.ShowToast("Evento no encontrado."));
        }
    }

    public void saveEvento(
        CalendarDay day,
        String nombre,
        String tipo,
        int color,
        String estado,
        String horaInicio,
        String horaFin,
        String descripcion) {

        if (this.currentUserId == null || this.currentUserId.isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("Error: No se puede guardar evento, usuario no identificado."));
            Log.e("HomeViewModel", "Intento de guardar evento sin currentUserId.");
            return;
        }

        if (nombre.trim().isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("El nombre es obligatorio."));
            return;
        }
        boolean horaValida = isHoraFinAfterHoraInicio(horaInicio, horaFin);
        if (!horaValida && !(horaInicio.isEmpty() && horaFin.isEmpty())) {
            _uiEvent.setValue(new UiEvent.ShowToast("La hora de fin debe ser posterior a la de inicio."));
            return;
        }

        Evento nuevoEvento = new Evento(day, nombre, tipo, color, estado, horaInicio, horaFin,
            descripcion, this.currentUserId);
        eventoRepository.addEvento(nuevoEvento);
        _uiEvent.setValue(new UiEvent.ShowToast("Actividad añadida."));
    }

    public void updateEvento(String id, CalendarDay day, String nombre, String tipo, int color,
                             String estado, String horaInicio, String horaFin, String descripcion) {
        if (this.currentUserId == null || this.currentUserId.isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("Error: No se puede actualizar evento, usuario no identificado."));
            Log.e("HomeViewModel", "Intento de actualizar evento sin currentUserId.");
            return;
        }
        if (nombre.trim().isEmpty()) {
            _uiEvent.setValue(new UiEvent.ShowToast("El nombre es obligatorio."));
            return;
        }
        boolean horaValida = isHoraFinAfterHoraInicio(horaInicio, horaFin);
        if (!horaValida && !(horaInicio.isEmpty() && horaFin.isEmpty())) {
            _uiEvent.setValue(new UiEvent.ShowToast("La hora de fin debe ser posterior a la de inicio."));
            return;
        }
        Evento eventoActualizado = new Evento(id, day, nombre, tipo, color, estado, horaInicio,
            horaFin, descripcion, this.currentUserId);
        eventoRepository.updateEvento(eventoActualizado);
        _uiEvent.setValue(new UiEvent.ShowToast("Actividad actualizada."));
    }

    public void deleteEvento(String eventoId) {
        eventoRepository.deleteEvento(eventoId);
        _uiEvent.setValue(new UiEvent.ShowToast("Actividad eliminada."));
    }

    private boolean isHoraFinAfterHoraInicio(String inicio, String fin) {
        if ((inicio == null || inicio.isEmpty()) && (fin == null || fin.isEmpty())) {
            return true;
        }
        if (inicio == null || fin == null || inicio.isEmpty() || fin.isEmpty() ||
            !inicio.contains(":") || !fin.contains(":")) {
            return false;
        }
        try {
            String[] inicioParts = inicio.split(":");
            String[] finParts = fin.split(":");
            int hInicio = Integer.parseInt(inicioParts[0]);
            int mInicio = Integer.parseInt(inicioParts[1]);
            int hFin = Integer.parseInt(finParts[0]);
            int mFin = Integer.parseInt(finParts[1]);

            if (hFin > hInicio) return true;
            return hFin == hInicio && mFin > mInicio;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public void cerrarSesion() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            _currentUser.setValue(null);
            this.currentUserId = null;

            ((FirestoreEventoRepository) eventoRepository).loadAndObserveEventsForUser(null);
            catalogoRepository.removeListeners();

            _uiEvent.setValue(new UiEvent.NavigateToLogin(true));
            _uiEvent.setValue(new UiEvent.ShowToast("Sesión cerrada"));
        });
    }

    public void onUiEventHandled() {
        _uiEvent.setValue(null);
    }
}