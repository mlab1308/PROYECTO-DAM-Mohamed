package iesmm.pmdm.autolabibscan.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import iesmm.pmdm.autolabibscan.Activities.AdminDashboardActivity;
import iesmm.pmdm.autolabibscan.Activities.UserDashboardActivity;
import iesmm.pmdm.autolabibscan.Fragments.ResultFragment;
import iesmm.pmdm.autolabibscan.R;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private List<String> favoritePlates;
    private Context context;

    // Constructor del adaptador que recibe la lista de matrículas favoritas
    public FavoritesAdapter(Context context, List<String> favoritePlates) {
        this.context = context;
        this.favoritePlates = favoritePlates;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout para cada elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_plate, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        // Obtener la matrícula en la posición actual
        String plate = favoritePlates.get(position);
        holder.plateTextView.setText(plate);

        // Establecer el listener para el botón de eliminar
        holder.deleteButton.setOnClickListener(v -> {
            // Mostrar un diálogo de confirmación antes de eliminar
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.confirm_deletion_title))
                    .setMessage(context.getString(R.string.confirm_deletion_message, plate))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Eliminar la matrícula de la base de datos y de la lista
                        removeFavorite(plate);
                        favoritePlates.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, favoritePlates.size());
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        // Establecer el listener para navegar al ResultFragment al hacer clic en la matrícula
        holder.itemView.setOnClickListener(v -> {
            navigateToResultFragment(plate);
        });
    }

    @Override
    public int getItemCount() {
        // Retornar el tamaño de la lista de matrículas favoritas
        return favoritePlates.size();
    }

    // Método para eliminar una matrícula de la base de datos
    private void removeFavorite(String plateText) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(userId).child(plateText);
            databaseReference.removeValue();
        }
    }

    // Método para navegar al ResultFragment con los datos de la matrícula seleccionada
    private void navigateToResultFragment(String plateText) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("cars");

        databaseReference.orderByChild("plateText").equalTo(plateText.toUpperCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot vehicleSnapshot : dataSnapshot.getChildren()) {
                        String carBrand = vehicleSnapshot.child("brand").getValue(String.class);
                        int owners = vehicleSnapshot.child("owners").getValue(Integer.class);
                        String powerText = vehicleSnapshot.child("power").getValue(String.class);
                        String fuelText = vehicleSnapshot.child("fuel").getValue(String.class);
                        String bastidor = vehicleSnapshot.child("info").child("Bastidor").getValue(String.class);
                        String emissionNorm = vehicleSnapshot.child("info").child("EmissionNorm").getValue(String.class);
                        String registeringAuthority = vehicleSnapshot.child("info").child("RegisteringAuthority").getValue(String.class);
                        String manufacturingDate = vehicleSnapshot.child("info").child("ManufacturingDate").getValue(String.class);
                        boolean vehicleStatus = vehicleSnapshot.child("vehicleStatus").getValue(Boolean.class);
                        String imageUrl = vehicleSnapshot.child("imageUrl").getValue(String.class);

                        Bundle bundle = new Bundle();
                        bundle.putString("carBrand", carBrand);
                        bundle.putString("plateText", plateText.toUpperCase());
                        bundle.putString("ownersText", owners + " Owners");
                        bundle.putString("powerText", powerText);
                        bundle.putString("fuelText", fuelText);
                        bundle.putString("Bastidor", bastidor);
                        bundle.putString("EmissionNorm", emissionNorm);
                        bundle.putString("RegisteringAuthority", registeringAuthority);
                        bundle.putString("ManufacturingDate", manufacturingDate);
                        bundle.putBoolean("VehicleStatus", vehicleStatus);
                        bundle.putString("ImageUrl", imageUrl);

                        ResultFragment resultFragment = new ResultFragment();
                        resultFragment.setArguments(bundle);

                        FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();

                        // Verificar la instancia de context y usar el contenedor adecuado
                        if (context instanceof AdminDashboardActivity) {
                            transaction.replace(R.id.admin_fragment_container, resultFragment);
                        } else if (context instanceof UserDashboardActivity) {
                            transaction.replace(R.id.fragment_container, resultFragment);
                        }

                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                } else {
                    Toast.makeText(context, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, R.string.db_query_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ViewHolder para cada elemento de la lista
    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView plateTextView;
        ImageView plateIcon;
        ImageButton deleteButton;

        FavoriteViewHolder(View itemView) {
            super(itemView);
            // Referencias a los elementos de la vista
            plateTextView = itemView.findViewById(R.id.txtPlate);
            plateIcon = itemView.findViewById(R.id.imgPlateIcon);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }
}
