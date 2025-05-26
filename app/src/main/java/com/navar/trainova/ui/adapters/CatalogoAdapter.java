package com.navar.trainova.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.navar.trainova.R;
import com.navar.trainova.data.model.CatalogoEvento;
import java.util.ArrayList;
import java.util.List;

public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoAdapter.CatalogoViewHolder> {

    private List<CatalogoEvento> catalogoList = new ArrayList<>();
    private final OnCatalogoActionsListener listener;
    private final String currentUserUid;

    public interface OnCatalogoActionsListener {
        void onAddItemClick(CatalogoEvento plantilla);
        void onItemClick(CatalogoEvento plantilla);
        void onEditPersonalTemplate(CatalogoEvento plantilla);
        void onCopyFromGeneralTemplate(CatalogoEvento plantilla);
        void onDeleteTemplateClick(CatalogoEvento plantilla);
    }

    public CatalogoAdapter(@NonNull OnCatalogoActionsListener listener) {
        this.listener = listener;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            this.currentUserUid = null;
        }
    }

    @NonNull
    @Override
    public CatalogoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_catalogo_evento, parent, false);
        return new CatalogoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogoViewHolder holder, int position) {
        CatalogoEvento plantilla = catalogoList.get(position);
        holder.bind(plantilla, listener, currentUserUid);
    }

    @Override
    public int getItemCount() {
        return catalogoList.size();
    }

    public void submitList(List<CatalogoEvento> newList) {
        this.catalogoList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class CatalogoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombre;
        private final TextView tvTipo;
        private final View colorIndicator;
        private final ImageButton btnAdd;
        private final ImageButton btnDelete;

        public CatalogoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreActividadCatalogo);
            tvTipo = itemView.findViewById(R.id.tvTipoActividadCatalogo);
            colorIndicator = itemView.findViewById(R.id.viewColorIndicatorCatalogo);
            btnAdd = itemView.findViewById(R.id.btnAddDesdeCatalogo);
            btnDelete = itemView.findViewById(R.id.btnDeleteTemplate);
        }

        public void bind(final CatalogoEvento plantilla, final OnCatalogoActionsListener listener, final String currentUserUid) {
            tvNombre.setText(plantilla.getNombreEvento());
            tvTipo.setText(plantilla.getTipoEvento());
            colorIndicator.setBackgroundColor(plantilla.getColorEvento());

            boolean isPersonal = currentUserUid != null && currentUserUid.equals(plantilla.getUidCreador());

            if (isPersonal) {
                // Es una plantilla personal: El clic principal edita, el botón '+' añade, y el botón de borrar es visible.
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> listener.onDeleteTemplateClick(plantilla));

                itemView.setOnClickListener(v -> listener.onEditPersonalTemplate(plantilla));
                btnAdd.setOnClickListener(v -> listener.onAddItemClick(plantilla));
            } else {
                // Es una plantilla general: El botón de borrar está oculto.
                btnDelete.setVisibility(View.GONE);

                itemView.setOnClickListener(v -> listener.onCopyFromGeneralTemplate(plantilla));
                btnAdd.setOnClickListener(v -> listener.onCopyFromGeneralTemplate(plantilla));
            }
        }
    }
}