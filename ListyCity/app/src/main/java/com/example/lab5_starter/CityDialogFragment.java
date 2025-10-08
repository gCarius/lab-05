package com.example.lab5_starter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class CityDialogFragment extends DialogFragment {
    interface CityDialogListener {
        void updateCity(City city, String title, String year);
        void addCity(City city);
        void deleteCity(City city);
    }
    private CityDialogListener listener;

    public static CityDialogFragment newInstance(City city){
        Bundle args = new Bundle();
        args.putSerializable("City", city);

        CityDialogFragment fragment = new CityDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CityDialogListener){
            listener = (CityDialogListener) context;
        }
        else {
            throw new RuntimeException("Implement listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_city_details, null);
        EditText editMovieName = view.findViewById(R.id.edit_city_name);
        EditText editMovieYear = view.findViewById(R.id.edit_province);

        String tag = getTag();
        Bundle bundle = getArguments();
        City city;

        if (Objects.equals(tag, "City Details") && bundle != null){
            city = (City) bundle.getSerializable("City");
            assert city != null;
            editMovieName.setText(city.getName());
            editMovieYear.setText(city.getProvince());
        } else {
            city = null;}

        if (Objects.equals(tag, "Delete City") && bundle != null) {
            city = (City) bundle.getSerializable("City");
            assert city != null;
            editMovieName.setText(city.getName());
            editMovieYear.setText(city.getProvince());
            editMovieName.setEnabled(false);
            editMovieYear.setEnabled(false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);

        if (Objects.equals(tag, "Delete City")) {
            builder.setTitle("Delete City");
            City cityTemp = city;
            builder.setPositiveButton("Delete", (dialog, which) -> listener.deleteCity(cityTemp));
            builder.setNegativeButton("Cancel", null);
        }
        else if (Objects.equals(tag, "City Details")) {
            builder.setTitle("Edit City");
            City cityTemp = city;
            builder.setPositiveButton("Continue", (dialog, which) -> {
                String title = editMovieName.getText().toString();
                String year = editMovieYear.getText().toString();
                listener.updateCity(cityTemp, title, year);
            });
            builder.setNegativeButton("Cancel", null);
        }
        else {
            builder.setTitle("Add City");
            builder.setPositiveButton("Add", (dialog, which) -> {
                String title = editMovieName.getText().toString();
                String year = editMovieYear.getText().toString();
                listener.addCity(new City(title, year));
            });
            builder.setNegativeButton("Cancel", null);
        }

        return builder.create();
    }
}
