package edu.pmdm.turbogranny;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TiendaAdapter extends RecyclerView.Adapter<TiendaAdapter.ViewHolder> {

    private Context context;
    private List<ShopItem> items;
    private final OnItemClickListener listener;
    private int monedasActuales;  // Monedas disponibles para comprar

    public interface OnItemClickListener {
        void onItemClick(ShopItem item, int position);
    }


    public TiendaAdapter(Context context, List<ShopItem> items, OnItemClickListener listener, int monedasActuales) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.monedasActuales = monedasActuales;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tienda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopItem item = items.get(position);

        // Configurar la vista
        holder.imgCoche.setImageResource(item.getImagenRes());
        holder.txtNombre.setText(item.getNombre());

        if (item.isComprado()) {
            // Si ya está comprado, mostramos "Seleccionar"
            holder.btnAccion.setText("Seleccionar");
            holder.txtPrecio.setVisibility(View.GONE);
            holder.btnAccion.setBackgroundColor(ContextCompat.getColor(context, R.color.verde));
        } else {
            // Si no está comprado, mostramos el precio y el botón "Comprar"
            holder.txtPrecio.setText(String.format("%d monedas", item.getPrecio()));
            holder.btnAccion.setText("Comprar");
            holder.txtPrecio.setVisibility(View.VISIBLE);
            holder.btnAccion.setEnabled(monedasActuales >= item.getPrecio());
            holder.btnAccion.setBackgroundColor(ContextCompat.getColor(
                    context,
                    (monedasActuales >= item.getPrecio()) ? R.color.azul : R.color.gris_deshabilitado
            ));
        }

        // Manejar clic en el botón
        holder.btnAccion.setOnClickListener(v -> {
            // Si no está comprado y no hay suficientes monedas, no hacemos nada
            if (!item.isComprado() && monedasActuales < item.getPrecio()) return;
            // En caso contrario, se notifica al listener
            listener.onItemClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Permite actualizar las monedas actuales y refrescar la lista.
     * Útil si cambias las monedas desde fuera y quieres que se refleje en los ítems.
     */
    public void setMonedasActuales(int nuevasMonedas) {
        this.monedasActuales = nuevasMonedas;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCoche;
        TextView txtNombre, txtPrecio;
        Button btnAccion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCoche = itemView.findViewById(R.id.imgCoche);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            btnAccion = itemView.findViewById(R.id.btnComprar);
        }
    }
}
