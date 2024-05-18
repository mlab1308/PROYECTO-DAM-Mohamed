package iesmm.pmdm.autolabibscan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iesmm.pmdm.autolabibscan.R;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private List<String> favoritePlates;

    public FavoritesAdapter(List<String> favoritePlates) {
        this.favoritePlates = favoritePlates;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_plate, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        String plate = favoritePlates.get(position);
        holder.txtPlate.setText(plate);
        // Aquí puedes agregar lógica adicional si es necesario
    }

    @Override
    public int getItemCount() {
        return favoritePlates.size();
    }

    public static class FavoritesViewHolder extends RecyclerView.ViewHolder {

        TextView txtPlate;
        ImageView imgPlateIcon;

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlate = itemView.findViewById(R.id.txtPlate);
            imgPlateIcon = itemView.findViewById(R.id.imgPlateIcon);
        }
    }
}
