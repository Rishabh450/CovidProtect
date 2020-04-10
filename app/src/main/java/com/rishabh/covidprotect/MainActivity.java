package com.rishabh.covidprotect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rishabh.covidprotect.Activities.Login;
import com.rishabh.covidprotect.Activities.ReportActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth.AuthStateListener authStateListener;
    String userno;
    FirebaseAuth mAuth;
    String email, name;
    private ProgressDialog mProgress;
    Uri photoUrl;
    TextView name1, email1;
    NavigationView navigationView;
    private Context context = MainActivity.this;
    String currentUser1;
    ImageView profilePhoto;
    DrawerLayout drawer;
    Uri profilePictureUri;
    BottomNavigationView bott;
    TextView userid;
    GoogleSignInClient mGoogleSignInClient;
    String provider;
    AppBarLayout root;
    CardView create;
    CardView join;


    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.e("ak47", "on Start");
        super.onStart();
        Log.e("ak47", "on Start after super");
        mAuth.addAuthStateListener(authStateListener);
        Log.e("ak47", "on Start Ends");
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.e("ak47", "user null");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Log.e("ak47", "user null");
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                } else {


                    Log.e("ak47", "user not null");
                }

            }
        };
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent();
            //name=intent.getStringExtra("avatar");

            //root=findViewById(R.id.root);
            email = user.getEmail();
            for (UserInfo use : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                Log.d("providerAUTH",use.getProviderId());
                if (use.getProviderId().equals("facebook.com")) {
                    provider = "facebook";
                } else if(use.getProviderId().equals("google.com"))
                    provider = "google";
                else
                    provider="phone";
            }
            FirebaseAuth.getInstance().getCurrentUser().getProviderId();
            UserInfo userr = FirebaseAuth.getInstance().getCurrentUser();
            if (provider.equals("facebook")) {
                userno = Profile.getCurrentProfile().getId();

            } else {
                userno = user.getUid();
            }


        }
        init();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, ReportActivity.class);
                intent.putExtra("lat",(float) getLastKnownLocation().getLatitude());
                intent.putExtra("lon",(float)getLastKnownLocation().getLongitude());
                startActivity(intent);
            /*    AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.adddialog, null);
                builder.setView(view);
                final Dialog dialog=builder.create();

                dialog.setContentView(R.layout.adddialog);
                *//*dialog.getWindow().getAttributes().windowAnimations=R.style.MyAnimation_Window;*//*
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                // (0x80000000, PorterDuff.Mode.MULTIPLY);
                dialog.show();

                 final EditText nameEdit = (EditText) dialog.findViewById(R.id.name);
                final EditText password = (EditText) dialog.findViewById(R.id.password);
                final EditText cpassword=dialog.findViewById(R.id.cpassword);
                TextView addButton = (TextView) dialog.findViewById(R.id.create);
                TextView cancelButton = (TextView) dialog.findViewById(R.id.cancel);

                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = nameEdit.getText().toString();
                        String cpass=cpassword.getText().toString();
                        String pass=password.getText().toString();
                        if(name.equals("")) {
                            Toast.makeText(MainActivity.this,"Name Field cannot be empty !",Toast.LENGTH_SHORT).show();


                        }
                        else if(pass.equals("")||cpass.equals(""))
                        {
                            Toast.makeText(MainActivity.this,"Enter password !",Toast.LENGTH_SHORT).show();

                        }
                        else if(!pass.equals(cpass))
                        {
                            Toast.makeText(MainActivity.this,"passwords don't match !",Toast.LENGTH_SHORT).show();

                        }
                        else

                        {

                            DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
                            reference.child(name).child("password").setValue(pass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this,"Created successfully !",Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });


                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
*/


            }
        });
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View view = inflater.inflate(R.layout.openinventorydialog, null);
                builder.setView(view);
                final Dialog dialog=builder.create();

                dialog.setContentView(R.layout.openinventorydialog);
                /*dialog.getWindow().getAttributes().windowAnimations=R.style.MyAnimation_Window;*/
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                // (0x80000000, PorterDuff.Mode.MULTIPLY);
                dialog.show();

                final EditText nameEdit = (EditText) dialog.findViewById(R.id.nameo);
                final EditText password = (EditText) dialog.findViewById(R.id.passwordo);
                TextView addButton = (TextView) dialog.findViewById(R.id.open);
                TextView cancelButton = (TextView) dialog.findViewById(R.id.cancel);

                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String name = nameEdit.getText().toString();
                        final String pass=password.getText().toString();
                        if(name.equals("")) {
                            Toast.makeText(MainActivity.this,"Name Field cannot be empty !",Toast.LENGTH_SHORT).show();


                        }
                        else if(pass.equals(""))
                        {
                            Toast.makeText(MainActivity.this,"Enter password !",Toast.LENGTH_SHORT).show();

                        }

                        else

                        {

                            final DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.hasChild(name))
                                        Toast.makeText(MainActivity.this,"Database does not exist",Toast.LENGTH_SHORT).show();
                                    else
                                    {
                                        reference.child(name).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String actual=dataSnapshot.getValue(String.class);
                                                if(actual.equals(pass))
                                                {

                                                }
                                                else
                                                {
                                                    Toast.makeText(MainActivity.this,"Wrong password",Toast.LENGTH_SHORT).show();
                                                    password.setText("");

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }

            });

    }
    });
}
    private Location getLastKnownLocation() {
        LocationManager mLocationManager;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ( checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.


                }
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


            mAuth.signOut();





        return super.onOptionsItemSelected(item);
    }
    private void init()
    {
        create=findViewById(R.id.create);
        join=findViewById(R.id.join);
    }
}