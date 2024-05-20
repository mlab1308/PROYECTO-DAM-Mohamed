package iesmm.pmdm.autolabibscan.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import iesmm.pmdm.autolabibscan.Adapters.AccessAdapter;
import iesmm.pmdm.autolabibscan.Models.AccessItem;
import iesmm.pmdm.autolabibscan.R;

public class adminDashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AccessAdapter adapter;
    private List<AccessItem> accessItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        recyclerView = findViewById(R.id.accessList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Crear una lista de ejemplos de AccessItem
        accessItemList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            accessItemList.add(new AccessItem("Moha@gmail.com", "24/04/2024 15:31"));
        }

        adapter = new AccessAdapter(accessItemList);
        recyclerView.setAdapter(adapter);
    }
}