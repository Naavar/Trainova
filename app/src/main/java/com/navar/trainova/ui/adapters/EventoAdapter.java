package com.navar.trainova.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.navar.trainova.data.model.Evento;

import java.util.List;

/**
 * Adaptador para mostrar una lista de objetos Evento en un RecyclerView.
 * Este adaptador se encarga de inflar las vistas de los elementos de la lista y de vincular
 * los datos de cada Evento con su correspondiente vista.
 * También gestiona los clics en los elementos de la lista a través de un listener.
 */
public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.ViewHolder> {

    private List<Evento> eventosList;
    private final OnEventoClickListener listener;

    /**
     * Interfaz para el callback de clic en un evento.
     * Permite que otras clases reaccionen cuando un usuario hace clic en un elemento de la lista.
     */
    public interface OnEventoClickListener {
        /**
         * Se invoca cuando se hace clic en un elemento de la lista.
         * @param evento El objeto {@link Evento} que fue clicado.
         */
        void onEventoClick(Evento evento);
    }

    /**
     * Constructor del adaptador.
     * @param eventos La lista de objetos Evento a mostrar.
     * @param listener El listener para manejar los clics en los eventos.
     */
    public EventoAdapter(@NonNull List<Evento> eventos, @NonNull OnEventoClickListener listener) {
        this.eventosList = eventos;
        this.listener = listener;
    }

    /**
     * Crea nuevas vistas (invocado por el layout manager).
     * @param parent El ViewGroup en el que se añadirá la nueva View una vez esté adjunta a una posición de adaptador.
     * @param viewType El tipo de vista de la nueva View.
     * @return Un nuevo {@link ViewHolder} que contiene la View para el elemento de la lista.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Se infla el layout para cada elemento de la lista.
        View view = LayoutInflater.from(parent.getContext())
            .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Reemplaza el contenido de una vista (invocado por el layout manager).
     * Este método asocia los datos de un {@link Evento} específico con la vista de un elemento.
     * @param holder El {@link ViewHolder} que debe ser actualizado para representar el contenido del elemento en la posición dada.
     * @param position La posición del elemento dentro del conjunto de datos del adaptador.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Evento currentEvento = eventosList.get(position);

        // Se establece el texto y el color del texto del TextView.
        holder.textView.setText(currentEvento.getNombreMostrado());
        holder.textView.setTextColor(currentEvento.getColor());

        // Se configura el click listener para el elemento completo.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventoClick(currentEvento);
            }
        });
    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos que el adaptador representa.
     * @return El número total de eventos en la lista.
     */
    @Override
    public int getItemCount() {
        return eventosList != null ? eventosList.size() : 0;
    }

    /**
     * Actualiza la lista de eventos que el adaptador está mostrando y notifica al RecyclerView
     * que los datos han cambiado.
     * nuevosEventos es la nueva lista de los eventos a mostrar.
     */
    public void submitList(List<Evento> nuevosEventos) {
        this.eventosList = nuevosEventos;
        notifyDataSetChanged(); // Notifica al RecyclerView que los datos han cambiado.
    }

    /**
     * Proporciona una referencia a las vistas para cada elemento de datos.
     * Un {@link ViewHolder} describe la vista de un elemento y los metadatos sobre su lugar
     * dentro del {@link RecyclerView}.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}