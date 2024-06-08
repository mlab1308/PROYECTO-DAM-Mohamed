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

    private List<AccessItem> accessItems;

    public AccessAdapter(List<AccessItem> accessItems) {
        this.accessItems = accessItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_access, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccessItem item = accessItems.get(position);
        holder.accessTitle.setText("Access");
        holder.accessDetails.setText("User: " + item.getEmail() + "\n" + formatDate(item.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return accessItems.size();
    }

    private String formatDate(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView accessTitle;
        public TextView accessDetails;
        public ImageView userIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            accessTitle = itemView.findViewById(R.id.accessTitle);
            accessDetails = itemView.findViewById(R.id.accessDetails);
            userIcon = itemView.findViewById(R.id.userIcon);
        }
    }
}
