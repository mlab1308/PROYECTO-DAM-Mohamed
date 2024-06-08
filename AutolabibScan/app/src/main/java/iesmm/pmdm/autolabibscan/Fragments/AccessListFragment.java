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
        View view = inflater.inflate(R.layout.fragment_access_list, container, false);

        recyclerView = view.findViewById(R.id.accessList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        accessItemList = new ArrayList<>();
        adapter = new AccessAdapter(accessItemList);
        recyclerView.setAdapter(adapter);

        loadAccessData();

        return view;
    }

    private void loadAccessData() {
        DatabaseReference accessRef = FirebaseDatabase.getInstance().getReference().child("access_logs");
        accessRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accessItemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AccessItem accessItem = dataSnapshot.getValue(AccessItem.class);
                    accessItemList.add(accessItem);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error
            }
        });
    }
}
