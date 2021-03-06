package com.example.car_rental;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.car_rental.Common.Common;
import com.example.car_rental.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText editUsername,editPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPassword = (MaterialEditText)findViewById(R.id.editPassword);
        editUsername = (MaterialEditText)findViewById(R.id.editUsername);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        ckbRemember = (CheckBox)findViewById(R.id.ckbRemember);

        //Init Paper
        Paper.init(this);

        //Initialize firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    //Save user and password
                    if(ckbRemember.isChecked())
                    {
                        Paper.book().write(Common.USER_KEY,editUsername.getText().toString());
                        Paper.book().write(Common.PWD_KEY,editPassword.getText().toString());
                    }




                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Please wating......");
                    mDialog.show();
                    table_user.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //check if user exists in database
                            if (dataSnapshot.child(editUsername.getText().toString()).exists()) {
                                //Get USer Information
                                mDialog.dismiss();
                                User user = dataSnapshot.child(editUsername.getText().toString()).getValue(User.class);
                                user.setPhone(editUsername.getText().toString());
                                if (user.getPassword().equals(editPassword.getText().toString())) {
                                    {
                                        Intent homeIntent = new Intent(SignIn.this, Home.class);
                                        Common.currentUser = user;
                                        startActivity(homeIntent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(SignIn.this, "Sign In not Succsesfull !", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "User doesen't exist in database !", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else
                {
                    Toast.makeText(SignIn.this,"Please check you internet connection!",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        });
    }
}