package com.navar.trainova.ui.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.navar.trainova.data.model.ColorOption;

import java.util.List;

/**
 * Adaptador personalizado para un Spinner que muestra una lista de ColorOptions.
 * Cada ítem en el spinner presenta el nombre del color junto con un pequeño indicador visual
 * (cuadro de color) del color correspondiente.
 * Utiliza simple_spinner_dropdown_item como base para las vistas de los ítems.
 */
public class ColorSpinnerAdapter extends ArrayAdapter<ColorOption> {
    private final List<ColorOption> colorOptionsList;
    private final LayoutInflater inflater;

    public ColorSpinnerAdapter(@NonNull Context context, @NonNull List<ColorOption> options) {
        super(context, android.R.layout.simple_spinner_item, options);
        this.inflater = LayoutInflater.from(context);
        this.colorOptionsList = options;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    /**
     * Crea o reutiliza una vista personalizada para un ítem del spinner.
     * Muestra el nombre del color y un indicador visual (cuadro de color) al lado.
     * Esta vista se utiliza tanto para el ítem seleccionado en el spinner ({@code getView})
     * como para los ítems en la lista desplegable ({@code getDropDownView}).
     *
     * @param position    La posición del ítem dentro del conjunto de datos del adaptador.
     * @param convertView La vista antigua a reutilizar, si es posible.
     * @param parent      El grupo padre al que esta vista será eventualmente adjuntada.
     * @return La {@link View} para el ítem en la posición especificada.
     */
    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent, boolean isDropDownView) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = (TextView) view; // El layout es un TextView
        ColorOption item = colorOptionsList.get(position);

        if (item != null) {
            textView.setText(item.getName());

            Drawable colorIndicator = new ColorDrawable(item.getColorValue());
            // Define un tamaño para el indicador de color para que sea consistente
            int indicatorSize = (int) (textView.getTextSize() * 1.2);
            colorIndicator.setBounds(0, 0, indicatorSize, indicatorSize);

            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(colorIndicator, null, null, null); // Mejor para RTL
            textView.setCompoundDrawablePadding(16);
        }
        return view;
    }

    @Nullable
    @Override
    public ColorOption getItem(int position) {
        if (position >= 0 && position < colorOptionsList.size()) {
            return colorOptionsList.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return colorOptionsList.size();
    }
}