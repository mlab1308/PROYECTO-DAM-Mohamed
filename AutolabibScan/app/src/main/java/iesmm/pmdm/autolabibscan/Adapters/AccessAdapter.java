package iesmm.pmdm.autolabibscan.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import iesmm.pmdm.autolabibscan.Models.AccessItem;
import iesmm.pmdm.autolabibscan.R;

public class AccessAdapter extends RecyclerView.Adapter<AccessAdapter.ViewHolder> {

    private List<AccessItem> accessItems;

    public AccessAdapter(List<AccessItem> accessItems) {
        this.accessItems = accessItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_access, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AccessItem item = accessItems.get(position);
        holder.accessTitle.setText("Access");
        holder.accessDetails.setText("User: " + item.getEmail() + "\n" + item.getDate());
    }

    @Override
    public int getItemCount() {
        return accessItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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
