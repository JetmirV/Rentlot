package com.example.car_rental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.car_rental.Common.Common;
import com.example.car_rental.Model.Request;
import com.example.car_rental.Model.Reservation;
import com.example.car_rental.ViewHolder.CartAdapter;
import com.example.car_rental.Database.Database;
//import com.firebase.ui.database.paging.FirebaseDataSource;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Cart extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    Button btnBook;

    List<Reservation> cart = new ArrayList<>();
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnBook = (Button)findViewById(R.id.btnBook);

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this,  "Your cart is empty!", Toast.LENGTH_SHORT).show();

            }
        });

        loadListCar();

    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Confirmation!");
        alertDialog.setMessage("Are you sure you want to make a reservation");



        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Create new request
                Request request = new Request(
                        Common.currentUser.getUsername(),
                        Common.currentUser.getName(),
                        txtTotalPrice.getText().toString(),
                        cart

                );
                //Submit to Firebase
                //System.CurrentMilli to key
                requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);
                // Delete cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you, Reservation processed! ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        alertDialog.show();

    }

    private void loadListCar()
    {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);


        //Calculate total price
        int total = 0;
        for(Reservation reservation:cart) {
            if(reservation.getTimeType().toString().equals("Hours"))
                total += (Integer.parseInt((reservation.getPrice()).split(" ", 3)[0])) * (Integer.parseInt(reservation.getReservationTime()));
            else
                total += (Integer.parseInt((reservation.getPrice()).split(" ", 3)[0])) * 24 *(Integer.parseInt(reservation.getReservationTime()));
        }

        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
        {
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {
        cart.remove(position);
        //Delete all old data from SQLite
        new Database(this).cleanCart();

        for(Reservation item:cart)
            new Database(this).addToCart(item);

        loadListCar();
    }
}