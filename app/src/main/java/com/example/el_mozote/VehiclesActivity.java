package com.example.el_mozote;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class VehiclesActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_vehicles);
        MaterialToolbar toolbar = findViewById(R.id.screenToolbar);
        toolbar.setTitle(R.string.nav_vehicles);
        AppNavigation.configure(this, R.id.nav_vehicles);
        ((TextView) findViewById(R.id.screenDescription)).setText("Inventario, disponibilidad, mantenimiento y precios de vehículos.");
    }
}




