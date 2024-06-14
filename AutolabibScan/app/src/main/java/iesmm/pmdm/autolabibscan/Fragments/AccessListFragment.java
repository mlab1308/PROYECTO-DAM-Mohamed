package iesmm.pmdm.autolabibscan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import iesmm.pmdm.autolabibscan.Adapters.AccessAdapter;
import iesmm.pmdm.autolabibscan.Models.AccessItem;
import iesmm.pmdm.autolabibscan.R;

public class AccessListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AccessAdapter adapter;
    private List<AccessItem> accessItemList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_access_list, container, false);

        // Inicializar el RecyclerView y configurarlo con un LinearLayoutManager
        recyclerView = view.findViewById(R.id.accessList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar la lista de elementos de acceso y el adaptador
        accessItemList = new ArrayList<>();
        adapter = new AccessAdapter(accessItemList);
        recyclerView.setAdapter(adapter);

        // Cargar los datos de acceso desde la base de datos
        loadAccessData();

        return view;
    }

    // Método para cargar los datos de acceso desde la base de datos Firebase
    private void loadAccessData() {
        // Referencia a la ubicación "access_logs" en la base de datos de Firebase
        DatabaseReference accessRef = FirebaseDatabase.getInstance().getReference().child("access_logs");

        // Añadir un ValueEventListener para escuchar los cambios en los datos
        accessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpiar la lista de elementos de acceso antes de actualizarla
                accessItemList.clear();

                // Recorrer todos los elementos en la instantánea de datos
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Obtener el objeto AccessItem de cada DataSnapshot
                    AccessItem accessItem = dataSnapshot.getValue(AccessItem.class);

                    // Añadir el AccessItem a la lista
                    accessItemList.add(accessItem);
                }

                // Notificar al adaptador que los datos han cambiado para actualizar la vista
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
