package com.navar.trainova.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para mostrar una lista de objetos {@link CatalogoEvento} en un RecyclerView.
 * Se encarga de inflar las vistas de los elementos y vincular los datos de cada plantilla.
 * Gestiona las interacciones del usuario con las plantillas a través de un listener.
 */
public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.CatalogoViewHolder> {

    private List<CatalogoEvento> catalogoList = new ArrayList<>();
    private final OnCatalogoActionsListener listener;
    private final String currentUserUid;

    /**
     * Interfaz para manejar las acciones del usuario sobre una plantilla del catálogo.
     */
    public interface OnCatalogoActionsListener {
        /**
         * Se llama cuando el usuario hace clic en el botón de añadir la plantilla como un evento.
         * @param plantilla La plantilla {@link CatalogoEvento} seleccionada.
         */
        void onAddItemClick(CatalogoEvento plantilla);

        /**
         * Se llama cuando el usuario hace clic en un ítem de la plantilla (acción genérica).
         * @param plantilla La plantilla {@link CatalogoEvento} seleccionada.
         */
        void onItemClick(CatalogoEvento plantilla);

        /**
         * Se llama cuando el usuario quiere editar una plantilla personal.
         * @param plantilla La plantilla {@link CatalogoEvento} a editar.
         */
        void onEditPersonalTemplate(CatalogoEvento plantilla);

        /**
         * Se llama cuando el usuario quiere copiar una plantilla general para hacerla personal.
         * @param plantilla La plantilla {@link CatalogoEvento} general a copiar.
         */
        void onCopyFromGeneralTemplate(CatalogoEvento plantilla);

        /**
         * Se llama cuando el usuario quiere borrar una plantilla personal.
         * @param plantilla La plantilla {@link CatalogoEvento} a borrar.
         */
        void onDeleteTemplateClick(CatalogoEvento plantilla);
    }

    /**
     * Constructor del CatalogoAdapter.
     * @param listener El listener para las acciones sobre las plantillas.
     */
    public CatalogoAdapter(@NonNull OnCatalogoActionsListener listener) {
        this.listener = listener;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.currentUserUid = (currentUser != null) ? currentUser.getUid() : null;
    }

    /**
     * Crea nuevas vistas para los ítems del RecyclerView (invocado por el layout manager).
     * @param parent El ViewGroup padre al que se adjuntará la nueva vista.
     * @param viewType El tipo de vista del nuevo ítem.
     * @return Un nuevo {@link CatalogoViewHolder} que contiene la vista para un ítem.
     */
    @NonNull
    @Override
    public CatalogoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_catalogo_evento, parent, false);
        return new CatalogoViewHolder(view);
    }

    /**
     * Vincula los datos de un {@link CatalogoEvento} específico a una vista (ViewHolder).
     * @param holder El {@link CatalogoViewHolder} que debe ser actualizado.
     * @param position La posición del ítem en la lista de datos.
     */
    @Override
    public void onBindViewHolder(@NonNull CatalogoViewHolder holder, int position) {
        CatalogoEvento plantilla = catalogoList.get(position);
        holder.bind(plantilla, listener, currentUserUid);
    }

    /**
     * Devuelve el número total de ítems en la lista de datos.
     * @return El tamaño de la lista de plantillas.
     */
    @Override
    public int getItemCount() {
        return catalogoList.size();
    }

    /**
     * Actualiza la lista de plantillas que muestra el adapter y notifica los cambios.
     * @param newList La nueva lista de {@link CatalogoEvento} a mostrar.
     */
    public void submitList(List<CatalogoEvento> newList) {
        this.catalogoList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para los ítems de {@link CatalogoEvento}.
     * Contiene las referencias a las vistas de la UI para un solo ítem y la lógica de binding.
     */
    static class CatalogoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombre;
        private final TextView tvTipo;
        private final View colorIndicator;
        private final ImageButton btnAdd;
        private final ImageButton btnDelete;

        /**
         * Constructor del ViewHolder.
         * @param itemView La vista raíz del ítem.
         */
        public CatalogoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreActividadCatalogo);
            tvTipo = itemView.findViewById(R.id.tvTipoActividadCatalogo);
            colorIndicator = itemView.findViewById(R.id.viewColorIndicatorCatalogo);
            btnAdd = itemView.findViewById(R.id.btnAddDesdeCatalogo);
            btnDelete = itemView.findViewById(R.id.btnDeleteTemplate);
        }

        /**
         * Vincula los datos de una {@link CatalogoEvento} a las vistas de este ViewHolder.
         * También configura los listeners de los botones y del ítem según si la plantilla
         * es personal o general.
         * @param plantilla El objeto {@link CatalogoEvento} a mostrar.
         * @param listener El listener para las acciones.
         * @param currentUserUid El UID del usuario actual para determinar si la plantilla es personal.
         */
        public void bind(final CatalogoEvento plantilla, final OnCatalogoActionsListener listener,
                         final String currentUserUid) {
            tvNombre.setText(plantilla.getNombreEvento());
            tvTipo.setText(plantilla.getTipoEvento());
            colorIndicator.setBackgroundColor((int) plantilla.getColorEvento());

            boolean isPersonal = currentUserUid != null && currentUserUid.equals(plantilla.getUidCreador());

            if (isPersonal) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> listener.onDeleteTemplateClick(plantilla));
                itemView.setOnClickListener(v -> listener.onEditPersonalTemplate(plantilla));
                btnAdd.setOnClickListener(v -> listener.onAddItemClick(plantilla));
            } else {
                btnDelete.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> listener.onCopyFromGeneralTemplate(plantilla));
                btnAdd.setOnClickListener(v -> listener.onCopyFromGeneralTemplate(plantilla));
            }
        }
    }
}