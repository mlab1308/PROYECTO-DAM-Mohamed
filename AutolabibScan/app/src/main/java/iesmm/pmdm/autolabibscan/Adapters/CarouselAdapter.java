package iesmm.pmdm.autolabibscan.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import iesmm.pmdm.autolabibscan.Activities.AdminDashboardActivity;
import iesmm.pmdm.autolabibscan.Activities.UserDashboardActivity;
import iesmm.pmdm.autolabibscan.Fragments.ResultFragment;
import iesmm.pmdm.autolabibscan.Models.Vehicle;
import iesmm.pmdm.autolabibscan.R;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {

    // Contexto de la actividad o fragmento que usa el adaptador
    private Context context;
    // Lista de vehículos a mostrar en el carrusel
    private List<Vehicle> vehicleList;

    // Constructor del adaptador que recibe el contexto y la lista de vehículos
    public CarouselAdapter(Context context, List<Vehicle> vehicleList) {
        this.context = context;
        this.vehicleList = vehicleList;
    }

    // Inflar el layout para cada ítem del carrusel y crear el ViewHolder
    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    // Vincular los datos del vehículo con las vistas del ViewHolder
    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.bind(vehicle);
    }

    // Obtener el número total de ítems en la lista de vehículos
    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    // Clase ViewHolder para representar los elementos del RecyclerView
    class CarouselViewHolder extends RecyclerView.ViewHolder {
        TextView brandTextView;
        TextView plateTextView;
        TextView powerTextView;
        TextView fuelTypeTextView;
        ImageView vehicleImageView;

        // Constructor del ViewHolder
        CarouselViewHolder(View itemView) {
            super(itemView);
            // Referenciar las vistas del layout
            brandTextView = itemView.findViewById(R.id.brandTextView);
            plateTextView = itemView.findViewById(R.id.plateTextView);
            powerTextView = itemView.findViewById(R.id.powerTextView);
            fuelTypeTextView = itemView.findViewById(R.id.fuelTypeTextView);
            vehicleImageView = itemView.findViewById(R.id.vehicleImageView);

            // Configurar el listener de clic en el ítem del carrusel
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Vehicle vehicle = vehicleList.get(position);
                navigateToResultFragment(vehicle.getPlateText());
            });
        }

        // Método para vincular los datos del vehículo a las vistas
        void bind(final Vehicle vehicle) {
            brandTextView.setText(vehicle.getBrand());
            plateTextView.setText(vehicle.getPlateText());
            powerTextView.setText(vehicle.getPower());
            fuelTypeTextView.setText(vehicle.getFuel());

            String imageUrl = vehicle.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                // Cargar imagen por defecto
                Glide.with(itemView.getContext())
                        .load(R.drawable.default_vehicle_carousel)
                        .apply(new RequestOptions().centerCrop())
                        .into(vehicleImageView);
            } else {
                // Cargar imagen desde la URL
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .apply(new RequestOptions().centerCrop())
                        .into(vehicleImageView);
            }
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

                        // Crear un Bundle para pasar los datos al fragmento de resultados
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

                        // Crear una instancia del fragmento de resultados y establecer los argumentos
                        ResultFragment resultFragment = new ResultFragment();
                        resultFragment.setArguments(bundle);

                        // Iniciar una transacción de fragmento
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
                    // Mostrar un mensaje si no se encuentran datos
                    Toast.makeText(context, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Mostrar un mensaje de error si la consulta a la base de datos falla
                Toast.makeText(context, R.string.db_query_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
