package com.rishabh.covidprotect.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rishabh.covidprotect.Adapters.QurantineAdapter;
import com.rishabh.covidprotect.Models.ReportModel;
import com.rishabh.covidprotect.R;




import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntToDoubleFunction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dmax.dialog.SpotsDialog;


public class ReportActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 5;
    private static final int GPS_REQUEST_CODE = 10;
    private Toolbar toolbar;
    private AppBarLayout toolbar_layout;
    String uid;
    AlertDialog alertDialog;

    private int location_error = 0;
    private static final int THRESHOLD_DIST = 200;
    private double lat, lon;
    private CardView reportBtn;
    private RecyclerView rview;
    private QurantineAdapter mAdapter;
    private RelativeLayout pFrame;
    private List<ReportModel> data = new ArrayList<>();
    static int MINUTES = 120;
    static int LAST_UPDATE = 0;
    Uri pickedImage;
    private int error = 0;
    private TextView dayleftcount, daysleftmessege;
    public static final String MY_PREFS_NAME = "Last_Update";
    int LOCATION_PERMISSION_CODE=22,CAMERA_PERMISSION_CODE=11;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        uid= FirebaseAuth.getInstance().getUid();
        init();
        getLocation();
        getLocationUpdates();
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false)
                .setMessage("Checking Face in Image....Please Wait.....")
                .build();

        setRecyclerView();

        getReports();
//        lat = getIntent().getDoubleExtra("lat", 0);
//        lon = getIntent().getDoubleExtra("lon", 0);

        /*if(currentLocation == null){
            pFrame.setVisibility(View.VISIBLE);
            reportBtn.setEnabled(false);
            reportBtn.setText(getResources().getString(R.string.please_wait));
        }*/
//        calDiffDist();



    }

    private void getLocation() {
        Intent intent = getIntent();
        lat = intent.getFloatExtra("lat", 0.0f);
        lon = intent.getFloatExtra("lon", 0.0f);


        reportBtn.setEnabled(true);



    }


    private void getLocationUpdates() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        if (checkLocationPermission()) {
            //first get current location as quarantine location
            //then open dialog
            if (canGetLocation()) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 20, locationListener);
            } else {

            }

        } else {
            requestLocationPermissions();
//                    Toast.makeText(getContexgetLocationUpdatest(), "You don't have location permission", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_CODE
        );
    }

    public boolean canGetLocation() {
        boolean result = true;
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }


    private void getReports() {
        final DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("report"))
                {
                    reference.child(uid) .child("report").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            data.clear();
                            pFrame.setVisibility(View.VISIBLE);
                            for(DataSnapshot ds:dataSnapshot.getChildren())
                            {   String id=ds.child("id").getValue(String.class);
                                String img=ds.child("img").getValue(String.class);
                                float lat=ds.child("location_lat").getValue(Float.class);
                                float lon=ds.child("location_lon").getValue(Float.class);
                                String time=ds.child("report_time").getValue(String.class);
                                ReportModel reportModel=new ReportModel(id,img,lat,lon,time);
                                data.add(reportModel);
                            }
                            pFrame.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                            if(data.size()==0)
                                Toast.makeText(ReportActivity.this,"You have report history.Please make your first report!",Toast.LENGTH_LONG).show();


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    pFrame.setVisibility(View.GONE);

                        Toast.makeText(ReportActivity.this,"You have no report history.Please make your first report!",Toast.LENGTH_LONG).show();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    private void setRecyclerView() {
        rview.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new QurantineAdapter(this, data);
        rview.setAdapter(mAdapter);



    }


    private void init() {

        rview = findViewById(R.id.quarantine_rview);
        pFrame = findViewById(R.id.progress_frame);


        reportBtn = findViewById(R.id.report_btn);
        reportBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkCameraPermission()) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra("android.intent.extras.CAMERA_FACING", Camera.CameraInfo.CAMERA_FACING_FRONT);
                        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                        startActivityForResult(intent, CAMERA_REQUEST);
                    } else {
                        requestCameraPermission();
                    }
                }
            }
        });
    }
    private void processFaceDetection(Bitmap bitmap, final Intent data) {
        FirebaseVisionImage firebaseVisionImage  = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions = new FirebaseVisionFaceDetectorOptions.Builder().build();

        FirebaseVisionFaceDetector firebaseVisionFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(firebaseVisionFaceDetectorOptions);

        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                getFaceResults(firebaseVisionFaces,data);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getFaceResults(List<FirebaseVisionFace> firebaseVisionFaces,Intent data) {
        int counter = 0;
        for( FirebaseVisionFace face : firebaseVisionFaces)
        {
            counter = counter + 1;
        }
        alertDialog.dismiss();
        if(counter==0)
        {
            error = 1;
            Toast.makeText(this, "Face Not Detected Take a clear pic", Toast.LENGTH_SHORT).show();
        }
        else if(counter>1)
        {
            error = 1;
            Toast.makeText(this, "More Than One Face" +
                    " Detected !!", Toast.LENGTH_SHORT).show();
        }
        else{
                upload(data);
        }
    }
    public void upload(Intent data)

    {  pickedImage=data.getData();
        Bundle extras = data.getExtras();
        // get the cropped bitmap
       // Bitmap selectedBitmap = extras.getParcelable("data");
        if(pickedImage!=null) {
        final long ts = (long) System.currentTimeMillis();
        final FirebaseDatabase[] database1 = {FirebaseDatabase.getInstance()};
        final DatabaseReference databaseReference1 = database1[0].getReference();
        final String mediaId = String.valueOf(ts);
        final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("ReportImage/").child(String.valueOf(ts));
        final UploadTask uploadTask;
        Bitmap bitmap = null;
        try {


            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] dat = baos.toByteArray();
        Log.d("kamwa kiya", "true");
        uploadTask = filePath.putBytes(dat);

        // uploadTask = filePath.putFile(Uri.parse(selectedImage));
        Log.d("sender", String.valueOf(pickedImage));
        final ProgressDialog mProgress = new ProgressDialog(ReportActivity .this);
        mProgress.setTitle("Uploading...");


        mProgress.setCancelable(true);

        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadTask.cancel();
            }
        });

        mProgress.show();


        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String url = String.valueOf(uri);
                        final String ts = String.valueOf(System.currentTimeMillis());
                        ReportModel reportModel=new ReportModel(uid,url,(float)lat,(float) lon,ts);
                        // post.add(map);
                        //  map.clear();

                        // databaseReference1.child("Feeds").child(String.valueOf(ts)).child("subEvent").setValue("");
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        reference.child(uid).child("report").child(ts).setValue(reportModel);
                        reference.child("report").child(ts).setValue(reportModel);
                        mProgress.dismiss();



                    }
                });


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                mProgress.setMessage("Uploaded: " + (int) progress + "%");
                mProgress.setProgress((int) progress);

            }
        });
    }


        }



    void sendDataToServer(Intent data) {

    }


    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE
        );
    }


    public boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                lat = loc.getLatitude();
                lon = loc.getLongitude();

            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            try {
                if (checkLocationPermission()) {
                    //first get current location as quarantine location
                    //then open dialog
                    if (canGetLocation()) {

                    } else {
                        // showSettingsAlert();
                    }

                } else {
                    requestLocationPermissions();
//                    Toast.makeText(getContext(), "You don't have location permission", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {

            }

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                startActivityForResult(intent, CAMERA_REQUEST);

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();

            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
                if (canGetLocation()) {
                    getLocationUpdates();
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                } else {
                    // showSettingsAlert();
                }

            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                pickedImage=data.getData();
                Bundle extras = data.getExtras();
                // get the cropped bitmap
                alertDialog.show();
                Bitmap selectedBitmap = extras.getParcelable("data");
                processFaceDetection(selectedBitmap,data);

            } else {
                Toast.makeText(this, "try again", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GPS_REQUEST_CODE) {
            if (resultCode == 1) {
                getLocationUpdates();
            } else {
                finish();
            }
        }
    }


}


