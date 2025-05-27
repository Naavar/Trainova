package com.navar.trainova.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.navar.trainova.R;
import com.navar.trainova.data.model.EjercicioPlantilla;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para el RecyclerView que muestra una lista de ejercicios dentro de una plantilla.
 * Permite visualizar los detalles de cada ejercicio y ofrece acciones como editar o eliminar.
 */
public class EjerciciosPlantillaAdapter extends RecyclerView.Adapter<EjerciciosPlantillaAdapter.EjercicioViewHolder> {

    private List<EjercicioPlantilla> ejercicioPlantillaList;
    private final OnEjercicioActionListener listener;

    /**
     * Interfaz para manejar las acciones realizadas sobre un ítem de ejercicio
     * (editar o borrar) y comunicarlas al fragmento o actividad contenedora.
     */
    public interface OnEjercicioActionListener {
        /**
         * Se llama cuando el usuario quiere editar un ejercicio.
         * @param ejercicio El objeto EjercicioPlantilla a editar.
         * @param position La posición del ejercicio en la lista.
         */
        void onEditEjercicio(EjercicioPlantilla ejercicio, int position);

        /**
         * Se llama cuando el usuario quiere borrar un ejercicio.
         * @param position La posición del ejercicio en la lista a borrar.
         */
        void onDeleteEjercicio(int position);
    }

    /**
     * Constructor del adapter.
     * @param ejercicios La lista inicial de ejercicios a mostrar.
     * @param listener El listener para las acciones de edición y borrado.
     */
    public EjerciciosPlantillaAdapter(List<EjercicioPlantilla> ejercicios,
                                      @NonNull OnEjercicioActionListener listener) {
        this.ejercicioPlantillaList = ejercicios != null ? new ArrayList<>(ejercicios) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public EjercicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_ejercicio_plantilla, parent, false);
        return new EjercicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EjercicioViewHolder holder, int position) {
        EjercicioPlantilla ejercicio = ejercicioPlantillaList.get(position);
        holder.bind(ejercicio, listener);
    }

    @Override
    public int getItemCount() {
        return ejercicioPlantillaList.size();
    }

    /**
     * Actualiza la lista de ejercicios que muestra el adapter.
     * Notifica al RecyclerView para que se redibuje.
     * @param nuevosEjercicios La nueva lista de ejercicios.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<EjercicioPlantilla> nuevosEjercicios) {
        this.ejercicioPlantillaList = new ArrayList<>(nuevosEjercicios);
        notifyDataSetChanged();
    }

    /**
     * Añade un nuevo ejercicio a la lista y notifica al adapter.
     * @param ejercicio El EjercicioPlantilla a añadir.
     */
    public void addEjercicio(EjercicioPlantilla ejercicio) {
        this.ejercicioPlantillaList.add(ejercicio);
        notifyItemInserted(this.ejercicioPlantillaList.size() - 1);
    }

    /**
     * Actualiza un ejercicio existente en la lista y notifica al adapter.
     * @param position La posición del ejercicio a actualizar.
     * @param ejercicio El EjercicioPlantilla con los datos actualizados.
     */
    public void updateEjercicio(int position, EjercicioPlantilla ejercicio) {
        if (position >= 0 && position < this.ejercicioPlantillaList.size()) {
            this.ejercicioPlantillaList.set(position, ejercicio);
            notifyItemChanged(position);
        }
    }


    /**
     * ViewHolder que representa la vista de un solo ítem de ejercicio en la lista.
     * Contiene las referencias a los elementos de la UI y asigna los listeners de los botones.
     */
    static class EjercicioViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombreEjercicio;
        private final TextView tvSeriesReps;
        private final TextView tvDescanso;
        private final TextView tvNotas;
        private final ImageButton btnEditarEjercicio;
        private final ImageButton btnBorrarEjercicio;

        EjercicioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreEjercicio = itemView.findViewById(R.id.tvEjercicioNombreItem);
            tvSeriesReps = itemView.findViewById(R.id.tvEjercicioSeriesRepsItem);
            tvDescanso = itemView.findViewById(R.id.tvEjercicioDescansoItem);
            tvNotas = itemView.findViewById(R.id.tvEjercicioNotasItem);
            btnEditarEjercicio = itemView.findViewById(R.id.btnEditarEjercicioItem);
            btnBorrarEjercicio = itemView.findViewById(R.id.btnBorrarEjercicioItem);
        }

        /**
         * Vincula los datos de un objeto EjercicioPlantilla a los elementos de la UI de este ViewHolder.
         * @param ejercicio El objeto EjercicioPlantilla a mostrar.
         * @param listener El listener para las acciones de editar y borrar.
         */
        void bind(final EjercicioPlantilla ejercicio, final OnEjercicioActionListener listener) {
            tvNombreEjercicio.setText(ejercicio.getNombreEjercicio());
            tvSeriesReps.setText(String.format("Series: %d, Reps: %s", ejercicio.getSeries(),
                ejercicio.getRepeticiones()));
            tvDescanso.setText(String.format("Descanso: %s", ejercicio.getDescanso()));

            if (ejercicio.getNotas() != null && !ejercicio.getNotas().isEmpty()) {
                tvNotas.setText(String.format("Notas: %s", ejercicio.getNotas()));
                tvNotas.setVisibility(View.VISIBLE);
            } else {
                tvNotas.setVisibility(View.GONE);
            }

            btnEditarEjercicio.setOnClickListener(v -> listener.onEditEjercicio(ejercicio,
                getAdapterPosition()));
            btnBorrarEjercicio.setOnClickListener(v -> listener.onDeleteEjercicio(getAdapterPosition()));
        }
    }
}