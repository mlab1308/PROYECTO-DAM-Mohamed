package iesmm.pmdm.autolabibscan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import iesmm.pmdm.autolabibscan.Models.AccessItem;
import iesmm.pmdm.autolabibscan.R;

public class AccessAdapter extends RecyclerView.Adapter<AccessAdapter.ViewHolder> {

    // Lista de elementos de acceso
    private List<AccessItem> accessItems;

    // Constructor del adaptador
    public AccessAdapter(List<AccessItem> accessItems) {
        this.accessItems = accessItems;
    }

    // Inflar el layout del ítem de la lista y crear el ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout del ítem de acceso
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_access, parent, false);
        return new ViewHolder(view);
    }

    // Vincular los datos del ítem de la lista con las vistas del ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener el ítem de acceso en la posición actual
        AccessItem item = accessItems.get(position);

        // Establecer el título del acceso
        holder.accessTitle.setText("Access");

        // Establecer los detalles del acceso (correo del usuario y la fecha/hora del acceso)
        holder.accessDetails.setText("User: " + item.getEmail() + "\n" + formatDate(item.getTimestamp()));
    }

    // Obtener el número total de ítems en la lista
    @Override
    public int getItemCount() {
        return accessItems.size();
    }

    // Formatear la fecha a una cadena legible
    private String formatDate(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    // Clase ViewHolder para representar los elementos del RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView accessTitle; // Título del acceso
        public TextView accessDetails; // Detalles del acceso (correo y fecha)
        public ImageView userIcon; // Icono del usuario

        // Constructor del ViewHolder
        public ViewHolder(View itemView) {
            super(itemView);
            // Referenciar las vistas del layout
            accessTitle = itemView.findViewById(R.id.accessTitle);
            accessDetails = itemView.findViewById(R.id.accessDetails);
            userIcon = itemView.findViewById(R.id.userIcon);
        }
    }
}
