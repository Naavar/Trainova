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
import com.navar.trainova.data.model.Evento;
import com.navar.trainova.data.repository.EventoRepository;
import com.navar.trainova.data.repository.FirestoreEventoRepository;
import com.navar.trainova.ui.adapters.CatalogoAdapter;
import com.navar.trainova.ui.dialogs.MultiDatePickerDialogFragment;
import com.navar.trainova.ui.dialogs.TemplateCreateEditDialogFragment;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.List;

public class CatalogActivity extends AppCompatActivity implements TemplateCreateEditDialogFragment.OnTemplateSaveListener, MultiDatePickerDialogFragment.OnMultiDateSetListener {

    private CatalogoViewModel catalogoViewModel;
    private EventoRepository eventoRepository;
    private RecyclerView recyclerViewCatalogo;
    private CatalogoAdapter catalogoAdapter;
    private FloatingActionButton fabAgregarPlantilla;

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

    private void setupRecyclerView() {
        CatalogoAdapter.OnCatalogoActionsListener listener = new CatalogoAdapter.OnCatalogoActionsListener() {
            @Override
            public void onAddItemClick(CatalogoEvento plantilla) {
                // Guardamos la plantilla en el tag de un View para poder recuperarla después
                fabAgregarPlantilla.setTag(plantilla);
                MultiDatePickerDialogFragment dialog = MultiDatePickerDialogFragment.newInstance();
                dialog.setOnMultiDateSetListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "MultiDatePickerDialog");
            }

            @Override
            public void onItemClick(CatalogoEvento plantilla) {
                onEditPersonalTemplate(plantilla);
            }

            @Override
            public void onEditPersonalTemplate(CatalogoEvento plantilla) {
                TemplateCreateEditDialogFragment dialog = TemplateCreateEditDialogFragment.newInstance(plantilla);
                dialog.setOnTemplateSaveListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "EditTemplateDialog");
            }

            @Override
            public void onCopyFromGeneralTemplate(CatalogoEvento plantilla) {
                CatalogoEvento copiaParaCrear = new CatalogoEvento(
                    plantilla.getNombreEvento(),
                    plantilla.getDescripcion(),
                    plantilla.getDuracion(),
                    plantilla.getTipoEvento(),
                    plantilla.getColorEvento(),
                    null
                );

                TemplateCreateEditDialogFragment dialog = TemplateCreateEditDialogFragment.newInstance(copiaParaCrear);
                dialog.setOnTemplateSaveListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "CreateFromTemplateDialog");
            }

            @Override
            public void onDeleteTemplateClick(CatalogoEvento plantilla) {
                new AlertDialog.Builder(CatalogActivity.this)
                    .setTitle("Confirmar borrado")
                    .setMessage("¿Estás seguro de que quieres borrar la plantilla '" + plantilla.getNombreEvento() + "'?")
                    .setPositiveButton("Borrar", (dialog, which) -> {
                        catalogoViewModel.deletePersonalTemplate(plantilla.getId());
                        Toast.makeText(CatalogActivity.this, "Plantilla borrada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        };

        catalogoAdapter = new CatalogoAdapter(listener);
        recyclerViewCatalogo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCatalogo.setAdapter(catalogoAdapter);
    }

    @Override
    public void onDatesSelected(List<CalendarDay> dates) {
        Object tag = fabAgregarPlantilla.getTag();
        if (!(tag instanceof CatalogoEvento)) {
            Toast.makeText(this, "Error: no se pudo encontrar la plantilla seleccionada.", Toast.LENGTH_SHORT).show();
            return;
        }
        CatalogoEvento plantilla = (CatalogoEvento) tag;

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        int eventosCreados = 0;
        for (CalendarDay dia : dates) {
            Evento nuevoEvento = new Evento(
                dia,
                plantilla.getNombreEvento(),
                plantilla.getTipoEvento(),
                plantilla.getColorEvento(),
                "Pendiente",
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
            Toast.makeText(this, eventosCreados + " evento(s) añadidos al calendario", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupObservers() {
        catalogoViewModel.getCatalogLiveData().observe(this, plantillas -> {
            if (plantillas != null) {
                catalogoAdapter.submitList(plantillas);
            }
        });
    }

    @Override
    public void onTemplateSave(CatalogoEvento plantilla) {
        if (plantilla.getId() != null && !plantilla.getId().isEmpty()) {
            catalogoViewModel.updatePersonalTemplate(plantilla);
            Toast.makeText(this, "Plantilla actualizada.", Toast.LENGTH_SHORT).show();
        } else {
            catalogoViewModel.createPersonalTemplate(plantilla);
            Toast.makeText(this, "Plantilla creada.", Toast.LENGTH_SHORT).show();
        }
    }
}