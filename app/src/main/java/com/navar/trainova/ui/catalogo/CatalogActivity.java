package com.navar.trainova.ui.catalogo;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import com.navar.trainova.ui.adapters.CatalogoAdapter;
import com.navar.trainova.ui.dialogs.TemplateCreateEditDialogFragment;

public class CatalogActivity extends AppCompatActivity implements TemplateCreateEditDialogFragment.OnTemplateSaveListener {

    private CatalogoViewModel catalogoViewModel;
    private RecyclerView recyclerViewCatalogo;
    private CatalogoAdapter catalogoAdapter;
    private FloatingActionButton fabAgregarPlantilla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        catalogoViewModel = new ViewModelProvider(this).get(CatalogoViewModel.class);

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
                Toast.makeText(CatalogActivity.this, "Funcionalidad pendiente: Añadir '" + plantilla.getNombreEvento() + "' al calendario", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(CatalogoEvento plantilla) {
                TemplateCreateEditDialogFragment dialog = TemplateCreateEditDialogFragment.newInstance(plantilla);
                dialog.setOnTemplateSaveListener(CatalogActivity.this);
                dialog.show(getSupportFragmentManager(), "EditTemplateDialog");
            }
        };

        catalogoAdapter = new CatalogoAdapter(listener);
        recyclerViewCatalogo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCatalogo.setAdapter(catalogoAdapter);
    }

    private void setupObservers() {
        // Observa la lista de plantillas del ViewModel y la pasa al adaptador
        catalogoViewModel.getCatalogLiveData().observe(this, plantillas -> {
            if (plantillas != null) {
                catalogoAdapter.submitList(plantillas);
            }
        });
    }

    // Este método se llama cuando el diálogo de creación/edición pulsa "Guardar"
    @Override
    public void onTemplateSave(CatalogoEvento plantilla) {
        // Le decimos al ViewModel que cree o actualice la plantilla
        catalogoViewModel.createPersonalTemplate(plantilla);
        Toast.makeText(this, "Plantilla guardada.", Toast.LENGTH_SHORT).show();
    }
}