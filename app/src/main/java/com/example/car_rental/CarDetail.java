package com.example.car_rental;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.car_rental.Common.Common;
import com.example.car_rental.Model.Car;
import com.example.car_rental.Model.Reservation;
import com.example.car_rental.Database.Database;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CarDetail extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView car_name,car_price,car_description;
    ImageView car_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;

    String carId="";

    FirebaseDatabase database;
    DatabaseReference cars;

    Car currentCar;

    Spinner spiner;

    private String spinerValue;

    public String getSpinerValue() {
        return spinerValue;
    }



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        //Firebase

        database = FirebaseDatabase.getInstance();
        cars = database.getReference("Cars");

        //Init view

        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Reservation(
                        carId,
                        currentCar.getName(),
                        numberButton.getNumber(),
                        currentCar.getPrice(),
                        spinerValue
                ));

                Toast.makeText(CarDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();
            }
        });

        car_description = (TextView)findViewById(R.id.car_description);
        car_name = (TextView)findViewById(R.id.car_name);
        car_price = (TextView)findViewById(R.id.car_price);
        car_image = (ImageView)findViewById(R.id.img_car);

        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);


        //Get food Id from Intent

        if(getIntent()!=null)
            carId = getIntent().getStringExtra("CarId");

        if(!carId.isEmpty()){
            if(Common.isConnectedToInternet(getBaseContext()))
                getDetailCar(carId);
            else
            {
                Toast.makeText(CarDetail.this,"Please check you internet connection!",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        spiner = (Spinner)findViewById(R.id.spiner);
        ArrayAdapter<CharSequence> spinerAdapter = ArrayAdapter.createFromResource(this,R.array.time_selector,android.R.layout.simple_spinner_item);
        spinerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiner.setAdapter(spinerAdapter);
        spiner.setOnItemSelectedListener(this);
        
    }

    private void getDetailCar(String carId) {
        cars.child(carId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            currentCar = dataSnapshot.getValue(Car.class);

            //set image

                Picasso.with(getBaseContext()).load(currentCar.getImage())
                        .into(car_image);

                collapsingToolbarLayout.setTitle(currentCar.getName());

                car_price.setText(currentCar.getPrice());

                car_name.setText(currentCar.getName());

                car_description.setText(currentCar.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinerValue = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}