package com.example.lab5_starter;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button delCityButton;
    private ListView cityListView;



    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private City selectedCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        delCityButton = findViewById(R.id.buttonDelCity);
        delCityButton.setBackgroundColor(Color.parseColor("#FFCDD2"));
        delCityButton.setTextColor(Color.BLACK); // optional for better contrast
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // set listeners
        // 1. Track the currently selected city
        selectedCity = null;
        // 2. When a city is clicked, set it as selected (and optionally edit)
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedCity = cityArrayAdapter.getItem(i);
            ((CityArrayAdapter) cityListView.getAdapter()).setSelectedCity(selectedCity); // highlight
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(selectedCity);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });

        // 3. Add city button
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        // 4. Delete button — only works if a city is selected
        delCityButton.setOnClickListener(view -> {
            if (selectedCity != null) {
                CityDialogFragment deleteDialog = CityDialogFragment.newInstance(selectedCity);
                deleteDialog.show(getSupportFragmentManager(), "Delete City");
            } else {
                Toast.makeText(this, "Please select a city first", Toast.LENGTH_SHORT).show();
            }
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()){
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        String oldName = city.getName(); // store old doc ID

        // Update locally
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Delete the old doc, then add the new one
        citiesRef.document(oldName).delete()
                .addOnSuccessListener(aVoid -> {
                    citiesRef.document(title).set(city)
                            .addOnSuccessListener(aVoid2 -> Log.d("Firestore", "City renamed successfully"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error adding new city", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting old city", e));
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    @Override
    public void deleteCity(City city) {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "City deleted successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error deleting city", e));
        // Clear the selected city so you don't delete it again by accident
        selectedCity = null;
        ((CityArrayAdapter) cityListView.getAdapter()).setSelectedCity(null);
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}