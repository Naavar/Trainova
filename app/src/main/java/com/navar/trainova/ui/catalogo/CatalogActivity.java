package com.navar.trainova.ui.catalogo;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.data.model.EjercicioPlantilla;
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.data.repository.EventoRepository;
import com.navar.trainova.data.repository.FirestoreEventoRepository;
import com.navar.trainova.ui.adapters.CatalogoAdapter;
import com.navar.trainova.ui.dialogs.MultiDatePickerDialogFragment;
import com.navar.trainova.ui.dialogs.TemplateCreateEditDialogFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

/**
 * Actividad que muestra el catálogo de plantillas de eventos, tanto generales como personales.
 * Permite al usuario crear nuevas plantillas, editar las suyas, borrar plantillas personales,
 * y añadir eventos al calendario basados en estas plantillas.
 */
public class CatalogActivity extends AppCompatActivity implements TemplateCreateEditDialogFragment.OnTemplateSaveListener, MultiDatePickerDialogFragment.OnMultiDateSetListener {

    private CatalogoViewModel catalogoViewModel;
    private EventoRepository eventoRepository;
    private RecyclerView recyclerViewCatalogo;
    private CatalogoAdapter catalogoAdapter;
    private FloatingActionButton fabAgregarPlantilla;

    /**
     * Se llama cuando la actividad es creada por primera vez.
     * Aquí se inicializan las vistas, el ViewModel, el RecyclerView y los observadores.
     * @param savedInstanceState Si la actividad se está re-inicializando después de haber sido
     * previamente cerrada, este Bundle contiene los datos que más
     * recientemente suministró en onSaveInstanceState(Bundle).
     * Nota: De lo contrario es nulo.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        catalogoViewModel = new ViewModelProvider(this).get(CatalogoViewModel.class);

        eventoRepository = new FirestoreEventoRepository();

        recyclerViewCatalogo = findViewById(R.id.recyclerViewCatalogo);
        fabAgregarPlantilla = findViewById(R.id.fabAgregarPlantillaCatalogo);

        setupRecyclerView();
        setupObservers();

        fabAgregarPlantilla.setOnClickListener(view -> {
            TemplateCreateEditDialogFragment dialog = TemplateCreateEditDialogFragment.newInstance();
            dialog.setOnTemplateSaveListener(this);
            dialog.show(getSupportFragmentManager(), "CreateTemplateDialog");
        });
    }

    /**
     * Configura el RecyclerView, su LayoutManager y el CatalogoAdapter
     * con el listener para las acciones sobre los ítems.
     */
    private void setupRecyclerView() {
        CatalogoAdapter.OnCatalogoActionsListener listener = new CatalogoAdapter
            .OnCatalogoActionsListener() {
            @Override
            public void onAddItemClick(CatalogoEvento plantilla) {
                fabAgregarPlantilla.setTag(plantilla);
                MultiDatePickerDialogFragment dialog = MultiDatePickerDialogFragment.newInstance();
                dialog.setOnMultiDateSetListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "MultiDatePickerDialog");
            }

            @Override
            public void onItemClick(CatalogoEvento plantilla) {
                // Si la plantilla es personal, permitir editar, sino, ofrecer copiar.
                String currentUserId = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                if (currentUserId != null && currentUserId.equals(plantilla.getUid())) {
                    onEditPersonalTemplate(plantilla);
                } else {
                    onCopyFromGeneralTemplate(plantilla);
                }
            }

            @Override
            public void onEditPersonalTemplate(CatalogoEvento plantilla) {
                TemplateCreateEditDialogFragment dialog = TemplateCreateEditDialogFragment
                    .newInstance(plantilla);
                dialog.setOnTemplateSaveListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "EditTemplateDialog");
            }

            @Override
            public void onCopyFromGeneralTemplate(CatalogoEvento plantilla) {
                List<EjercicioPlantilla> ejerciciosCopiados = plantilla.getEjercicios() != null ?
                    new ArrayList<>(plantilla.getEjercicios()) : new ArrayList<>();
                String currentUserId = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                    FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

                CatalogoEvento copiaParaCrear = new CatalogoEvento(
                    plantilla.getNombreEvento(),
                    plantilla.getDescripcion(),
                    plantilla.getDuracion(),
                    plantilla.getTipoEvento(),
                    plantilla.getColorEvento(),
                    currentUserId, // Asignar el UID del usuario actual a la copia
                    ejerciciosCopiados
                );

                TemplateCreateEditDialogFragment dialog = TemplateCreateEditDialogFragment.newInstance(copiaParaCrear);
                dialog.setOnTemplateSaveListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "CreateFromCopiedTemplateDialog");
            }

            @Override
            public void onDeleteTemplateClick(CatalogoEvento plantilla) {
                new AlertDialog.Builder(CatalogActivity.this)
                    .setTitle(getString(R.string.confirmar_borrado_title))
                    .setMessage(getString(R.string.confirmar_borrado_plantilla_message,
                        plantilla.getNombreEvento()))
                    .setPositiveButton(getString(R.string.btn_borrar), (dialog, which) -> {
                        catalogoViewModel.deletePersonalTemplate(plantilla.getId());
                        Toast.makeText(CatalogActivity.this, getString(R.string
                            .plantilla_borrada_toast), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.btn_cancelar), null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        };

        catalogoAdapter = new CatalogoAdapter(listener);
        recyclerViewCatalogo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCatalogo.setAdapter(catalogoAdapter);
    }

    /**
     * Se llama cuando el usuario selecciona una o varias fechas en el MultiDatePickerDialogFragment.
     * Crea objetos Evento basados en la plantilla seleccionada y los añade al calendario.
     * @param dates Lista de CalendarDay seleccionados por el usuario.
     */
    @Override
    public void onDatesSelected(List<CalendarDay> dates) {
        Object tag = fabAgregarPlantilla.getTag();
        if (!(tag instanceof CatalogoEvento)) {
            Toast.makeText(this, getString(R.string.error_plantilla_no_seleccionada),
                Toast.LENGTH_SHORT).show();
            return;
        }
        CatalogoEvento plantilla = (CatalogoEvento) tag;
        fabAgregarPlantilla.setTag(null);

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (ownerUid == null) {
            Toast.makeText(this, getString(R.string.error_usuario_no_identificado),
                Toast.LENGTH_SHORT).show();
            return;
        }

        int eventosCreados = 0;
        for (CalendarDay dia : dates) {
            Evento nuevoEvento = new Evento(
                dia,
                plantilla.getNombreEvento(),
                plantilla.getTipoEvento(),
                plantilla.getColorEvento(),
                getString(R.string.estado_evento_pendiente),
                "09:00",
                "10:00",
                plantilla.getDescripcion(),
                ownerUid
            );
            if (eventoRepository != null) {
                eventoRepository.addEvento(nuevoEvento);
            }
            eventosCreados++;
        }

        if (eventosCreados > 0) {
            Toast.makeText(this, getString(R.string.eventos_anadidos_calendario,
                eventosCreados), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Configura los observadores para el LiveData del ViewModel.
     * Actualiza el adapter del catálogo cuando cambian las plantillas.
     */
    private void setupObservers() {
        catalogoViewModel.getCatalogLiveData().observe(this, plantillas -> {
            if (plantillas != null) {
                catalogoAdapter.submitList(plantillas);
            }
        });
    }

    /**
     * Se llama cuando el TemplateCreateEditDialogFragment guarda una plantilla (nueva o editada).
     * @param plantilla La plantilla que ha sido guardada (ahora con su lista de ejercicios).
     */
    @Override
    public void onTemplateSave(CatalogoEvento plantilla) {
        if (plantilla.getId() != null && !plantilla.getId().isEmpty()) {
            catalogoViewModel.updatePersonalTemplate(plantilla);
            Toast.makeText(this, getString(R.string.plantilla_actualizada_toast),
                Toast.LENGTH_SHORT).show();
        } else {
            if (plantilla.getUid() == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
                plantilla.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
            }
            catalogoViewModel.createPersonalTemplate(plantilla);
            Toast.makeText(this, getString(R.string.plantilla_creada_toast),
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Se llama cuando la actividad está siendo destruida.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}