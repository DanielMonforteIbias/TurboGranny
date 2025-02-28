package edu.pmdm.turbogranny;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PuntuacionAdapter extends RecyclerView.Adapter<PuntuacionAdapter.ViewHolder>{
    private ArrayList<Puntuacion> puntuaciones;

    public PuntuacionAdapter(ArrayList<Puntuacion> puntuaciones) {
        this.puntuaciones = puntuaciones;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtNickname;
        private final TextView txtPuntos;
        public ViewHolder(View itemView) {
            super(itemView);
            txtNickname=itemView.findViewById(R.id.txtNicknameLeader);
            txtPuntos=itemView.findViewById(R.id.txtPoints);

        }
        public TextView getTxtNombre() {return txtNickname;}
        public TextView getTxtDescripcion() {return txtPuntos;}
    }
    @NonNull
    @Override
    public PuntuacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboards_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PuntuacionAdapter.ViewHolder holder, int position) {
        Context context=holder.itemView.getContext();
        Puntuacion puntuacion=puntuaciones.get(position);
        holder.txtNickname.setText(puntuacion.getNickname());
        holder.txtPuntos.setText(String.valueOf(puntuacion.getPuntos()));
        if(position==0){ //El primero es amarillo
            holder.txtNickname.setTextColor(context.getResources().getColor(R.color.car2yellow));
            holder.txtPuntos.setTextColor(context.getResources().getColor(R.color.car2yellow));
        }
    }

    @Override
    public int getItemCount() {
        return puntuaciones.size();
    }
}
