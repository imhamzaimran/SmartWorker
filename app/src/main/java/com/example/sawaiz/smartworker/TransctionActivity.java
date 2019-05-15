package com.example.sawaiz.smartworker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sawaiz.smartworker.Utils.SendNotification;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TransctionActivity extends AppCompatActivity {

    private AppCompatRatingBar ratingBar;
    private DatabaseReference submitRatingRef,customerRating,myRef,customerReview,submitReviewRef;
    Context context = this;
    private String Key,customerId,handymanId;
    private float rate;
    private TextView customername, appoid, handyswork, handyscost, jobduration, totalamount;
    private Button billpaid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transction);

        Intent i = getIntent();
        Key = i.getStringExtra("key");

        customername =(TextView)(findViewById(R.id.completecustomername));
        appoid =(TextView)(findViewById(R.id.completeappointmentid));
        handyswork =(TextView)(findViewById(R.id.completeskill));
        handyscost =(TextView)(findViewById(R.id.completecostperhour));
        jobduration =(TextView)(findViewById(R.id.completejobduration));
        totalamount =(TextView)(findViewById(R.id.completebill));
        billpaid =(Button)(findViewById(R.id.completeBTPaid));

        myRef = FirebaseDatabase.getInstance().getReference().child("CurrentAppointments").child(Key);
        getInfo();

        customername.setText(currentSingleShow.getcompletecustomername());
        appoid.setText(currentSingleShow.getcompletehandyappoint());
        handyswork.setText(currentSingleShow.getcompletehandyskill());
        handyscost.setText(currentSingleShow.getcompletehandycost());
        jobduration.setText(currentSingleShow.getcompletehandyduration());
        totalamount.setText(currentSingleShow.getcompletehandybill());



        submitRatingRef = FirebaseDatabase.getInstance().getReference().child("CurrentAppointments").child(Key);
        submitReviewRef = FirebaseDatabase.getInstance().getReference().child("CurrentAppointments").child(Key);

        billpaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayReviewDialog();
            }
        });



        //for calculating average rating for customer
        /*
for (DataSnapshot child : dataSnapshot.child("CustomerRating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }

*/

    }

    private void displayReviewDialog() {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.reason_dialog,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle("Review Customer");
        alertDialogBuilder.setMessage("Provide a review to help other handymen");
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.reason_txt);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String review = userInput.getText().toString();
                                submitReviewRef.child("HandymenReview").setValue(review);
                                customerReview = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child("Customer").child(customerId).child("HandymenReview");
                                customerReview.child(Key).setValue(review);
                                displayRatingBarDialog();
                            }
                        })
                .setNeutralButton("Skip",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                displayRatingBarDialog();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }



    private void getInfo() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){
                        if (child.getKey().equals("CustomerId")){
                            customerId = child.getValue().toString();
                            //getUserInformation("Customer", customerId);
                        }
                        if (child.getKey().equals("HandymanId")){
                            handymanId = child.getValue().toString();
                            //gethandyInformation("Handyman", handymanId);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void displayRatingBarDialog() {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.rating_bar_dialog, null);
        ratingBar = (AppCompatRatingBar)promptsView.findViewById(R.id.customerRatingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rate = rating;
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context,R.style.AlertDialogTheme);
        alertDialogBuilder.setTitle("Rate Customer");
        alertDialogBuilder.setView(promptsView);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Submit",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                submitRatingRef.child("CustomerRating").setValue(rate);
                                customerRating = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child("Customer").child(customerId).child("CustomerRating");
                                customerRating.child(Key).setValue(rate);
                                finish();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        //finish();
    }
}
