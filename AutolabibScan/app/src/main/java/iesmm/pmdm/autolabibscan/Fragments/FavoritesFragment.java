package iesmm.pmdm.autolabibscan.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import iesmm.pmdm.autolabibscan.Adapters.FavoritesAdapter;
import iesmm.pmdm.autolabibscan.R;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerViewFavorites;
    private FavoritesAdapter adapter;
    private List<String> favoritePlates;
    private TextView noFavoritesTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerViewFavorites = view.findViewById(R.id.recyclerViewFavorites);
        noFavoritesTextView = view.findViewById(R.id.noFavoritesTextView);

        setupRecyclerView();
        loadFavoritePlates();

        return view;
    }

    private void setupRecyclerView() {
        favoritePlates = new ArrayList<>();
        adapter = new FavoritesAdapter(getContext(), favoritePlates);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFavorites.setAdapter(adapter);
    }

    private void loadFavoritePlates() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favorites").child(userId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    favoritePlates.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String plate = snapshot.getKey();
                        favoritePlates.add(plate);
                    }
                    adapter.notifyDataSetChanged();
                    checkIfNoFavorites();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void checkIfNoFavorites() {
        if (favoritePlates.isEmpty()) {
            recyclerViewFavorites.setVisibility(View.GONE);
            noFavoritesTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewFavorites.setVisibility(View.VISIBLE);
            noFavoritesTextView.setVisibility(View.GONE);
        }
    }
}
