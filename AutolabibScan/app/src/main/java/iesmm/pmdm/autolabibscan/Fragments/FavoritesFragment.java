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

import java.util.Arrays;
import java.util.List;

import iesmm.pmdm.autolabibscan.Adapters.FavoritesAdapter;
import iesmm.pmdm.autolabibscan.R;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerViewFavorites;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerViewFavorites = view.findViewById(R.id.recyclerViewFavorites);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        List<String> favoritePlates = Arrays.asList("1234-FJK", "5678-ABC", "9012-XYZ"); // Ejemplo de datos
        FavoritesAdapter adapter = new FavoritesAdapter(favoritePlates);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFavorites.setAdapter(adapter);
    }
}
