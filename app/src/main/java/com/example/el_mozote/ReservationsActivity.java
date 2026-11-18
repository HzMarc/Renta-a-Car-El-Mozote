package com.example.el_mozote;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class ReservationsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_reservations);
        MaterialToolbar toolbar = findViewById(R.id.screenToolbar);
        toolbar.setTitle(R.string.nav_reservations);
        AppNavigation.configure(this, R.id.nav_reservations);
        ((TextView) findViewById(R.id.screenDescription)).setText("Reservas pendientes, activas, completadas y canceladas.");
    }
}




