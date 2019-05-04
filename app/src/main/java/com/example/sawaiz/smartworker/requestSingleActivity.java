package com.example.sawaiz.smartworker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class requestSingleActivity extends AppCompatActivity implements OnMapReadyCallback{

    String userId,customerId,handymanId,name,latlng, lat,lng,date,time;

    private TextView userName;
    private TextView userPhone;

    private TextView userdate;
    private TextView usertime;

    private Button accept;
    private Button reject;

    private ImageView userImage;

    private LatLng locationLatLng;

    private DatabaseReference mydbref,hdb,cdb,db;

    private ProgressDialog progressDialog;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    Location location;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestsingleactivity);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requestSingleActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }else{
            mMapFragment.getMapAsync(this);
        }


        userImage = (ImageView) findViewById(R.id.userImage);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);
        userdate = (TextView) findViewById(R.id.JobDate);
        usertime = (TextView) findViewById(R.id.JobTime);


        Intent i = getIntent();
        final String Key = i.getStringExtra("key");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mydbref = FirebaseDatabase.getInstance().getReference().child("AppointmentRequests").child(Key);

        getUserRequestInfo();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        accept = (Button)(findViewById(R.id.ReqAccept));
        reject = (Button)(findViewById(R.id.ReqReject));

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference handyman = FirebaseDatabase.getInstance().getReference().child("Users").child("Handyman").child(userId).child("FutureAppointments");
                DatabaseReference customerDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(customerId).child("FutureAppointments");
                DatabaseReference myDBref1 = FirebaseDatabase.getInstance().getReference().child("FutureAppointments");
                String FutureAppointmentsId = myDBref1.push().getKey();
                handyman.child(FutureAppointmentsId).setValue(true);
                customerDB.child(FutureAppointmentsId).setValue(true);

                Map data = new HashMap();
                data.put("HandymanId",userId);
                data.put("Date",date);
                data.put("Time",time);
                data.put("CustomerId",customerId);
                data.put("CustomerLocation/lat",lat);
                data.put("CustomerLocation/lat",lng);
                myDBref1.child(FutureAppointmentsId).updateChildren(data);

                db =  FirebaseDatabase.getInstance().getReference().child("AppointmentRequests").child(Key);
                hdb = FirebaseDatabase.getInstance().getReference().child("Users").child("Handyman").child(userId).child("RequestList").child(Key);
                cdb =  FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(customerId).child("RequestList").child(Key);

                hdb.removeValue();
                cdb.removeValue();
                db.removeValue();
                finish();
            }
        });

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db =  FirebaseDatabase.getInstance().getReference().child("AppointmentRequests").child(Key);
                hdb = FirebaseDatabase.getInstance().getReference().child("Users").child("Handyman").child(userId).child("RequestList").child(Key);
                cdb =  FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(customerId).child("RequestList").child(Key);

                hdb.removeValue();
                cdb.removeValue();
                db.removeValue();
                finish();
            }
        });
    }

    private void getUserRequestInfo() {
        mydbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot child:dataSnapshot.getChildren()){
                        if (child.getKey().equals("CustomerId")){
                            customerId = child.getValue().toString();
                                getUserInformation("Customer", customerId);
                        }

                        if (child.getKey().equals("HandymanId")){
                            handymanId = child.getValue().toString();
                        }

                        if (child.getKey().equals("Date")){
                            date = child.getValue().toString();
                            userdate.setText((child.getValue().toString()));
                        }
                        if (child.getKey().equals("Time")){
                            time = child.getValue().toString();
                            usertime.setText(child.getValue().toString());
                        }
                        if (child.getKey().equals("CustomerLocation")){
                            lat = child.child("Latitude").getValue().toString();
                            //Log.e("lat: ",lat);
                            //Toast.makeText(getApplicationContext(),"lat: "+lat,Toast.LENGTH_LONG).show();
                            lng = lat = child.child("Longitude").getValue().toString();
                            locationLatLng = new LatLng(Double.valueOf(child.child("Latitude").getValue().toString()),Double.valueOf(child.child("Longitude").getValue().toString()));
                            if(locationLatLng != new LatLng(0,0)) {
                                MarkeratCustomerLocation();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void MarkeratCustomerLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locationLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        mMap.addMarker(new MarkerOptions().position(locationLatLng).title("Job Location"));
    }


    private void getUserInformation(String customer, String otherUserId) {
        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(customer).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("FirstName") != null){
                        name = map.get("FirstName").toString();
                    }
                    if(map.get("LastName") != null){
                        name = name + " "+map.get("LastName").toString();
                        userName.setText(name);
                    }
                    if(map.get("PhoneNumber") != null){
                        userPhone.setText(map.get("PhoneNumber").toString());
                    }
                    if(map.get("profileImageUrl") != null){
                        String url = map.get("profileImageUrl").toString();
                        Picasso.get().load(url).fit().into(userImage);
                    }
                }
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requestSingleActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        mMap.setMyLocationEnabled(true);
    }

    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mMapFragment.getMapAsync(this);
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


}