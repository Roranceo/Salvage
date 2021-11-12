package com.example.salvage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private boolean mLocationGrantPermissionGranted =false;
    private static final String TYPE_RECYCABLE = "Recyclable";
    private static final String TYPE_NON_RECYCABLE = "Non-Recyclable";
    private String mCategoryType;
    private ImageButton info;

    private TextView formatTxt, contentTxt;
    private DatabaseReference product;

    /*
    * 2 ArrayLists to store both category's*/
    ArrayList<BarcodeDataModel> recycableCodes = new ArrayList<>();
    ArrayList<BarcodeDataModel> nonRecycableCodes = new ArrayList<>();

    private BarcodeDataModel barcodeDataModel;





    /*
    * // start the activity with saved instances
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        final ActionBar actionBar =getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        /*
        * data members with ids to identify these will be viewed by the user */
        formatTxt = findViewById(R.id.scan_format);
        contentTxt = findViewById(R.id.scan_content);
        info = (ImageButton)findViewById(R.id.info);
        /*
        * OnClickListener used when user interacts with info button*/
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openInfo();
            }
        });

        //creates an Firebase Instance//
        product = FirebaseDatabase.getInstance().getReference().child("product");
        product.addValueEventListener(new ValueEventListener() {




            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {

                    if (dataSnapshot.getChildrenCount() == 0) {
                        Toast.makeText(MainActivity.this, "No data available!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (dataSnapshot.child("product_recyclable").exists()) {
                        for (DataSnapshot item : dataSnapshot.child("product_recyclable").getChildren()) {
                            recycableCodes.add(item.getValue(BarcodeDataModel.class));
                        }
                    }
                    if (dataSnapshot.child("product_non_recyclable").exists()) {
                        for (DataSnapshot item : dataSnapshot.child("product_non_recyclable").getChildren()) {
                            nonRecycableCodes.add(item.getValue(BarcodeDataModel.class));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
    /*
    * open the info activity on click*/
    public void openInfo() {
        Intent intent = new Intent(this, Info.class);
        startActivity(intent);
    }



    // method to scan
    public void scanNow(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan Barcode");
        integrator.setCameraId(0);  // Identifying user's primary camera on device
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
        integrator.setOrientationLocked(false);
    }

     /*
     * User is given the choice in a prompt if data is not present*/
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Is this Packaging Recyclable?")
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCategoryType = TYPE_NON_RECYCABLE;
                        pushDataToFirebase(barcodeDataModel);

                    }
                })
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        mCategoryType = TYPE_RECYCABLE;
                        pushDataToFirebase(barcodeDataModel);

                    }
                });
        //an alert prompt if no values exist//
        AlertDialog d = builder.create();
        d.setTitle("Select category");
        d.show();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            Toast toast = Toast.makeText(getApplicationContext(), "No scan data received! Please try again", Toast.LENGTH_SHORT);
            toast.show();
            //result is displayed as string

            String imagePath = scanningResult.getBarcodeImagePath();
            String content = scanningResult.getContents();
            String format = scanningResult.getFormatName();

            if (imagePath == null) imagePath = "";
            if (content == null) content = "";
            if (format == null) format = "";

            barcodeDataModel = new BarcodeDataModel(imagePath, content, format);

            // display it on screen
            formatTxt.setText("Barcode Type: " + format);
            contentTxt.setText("Barcode ID: " + content);

            /*
            * if else statement for adding to firebase. if result doesn't exist execute the prompt*/
            if (itemNotExist(barcodeDataModel)) {
                showDialog();
                /*
                * if we do have a result then we get the toast message "This packaging is'*/
            } else {
                Toast.makeText(MainActivity.this, "This packaging is " + mCategoryType, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void pushDataToFirebase(BarcodeDataModel scanningResult) {
        DatabaseReference selectedProductReference = null;

        switch (mCategoryType) {
            case TYPE_RECYCABLE:
                selectedProductReference = product.child("product_recyclable").push();
                break;

            case TYPE_NON_RECYCABLE:
                selectedProductReference = product.child("product_non_recyclable").push();

                break;
        }

        selectedProductReference.setValue(scanningResult).addOnSuccessListener(aVoid -> {
            Toast.makeText(MainActivity.this, "Entry added successfully, Thank you!", Toast.LENGTH_LONG).show();

        });
    }

    /*
    * this method below comapares the values scanned*/
    private boolean itemNotExist(BarcodeDataModel scanningResult) {
        for (BarcodeDataModel result : recycableCodes) {
            if (scanningResult.contents.equals(result.contents)) {
                mCategoryType = TYPE_RECYCABLE;
                return false;
            }
        }

        for (BarcodeDataModel result : nonRecycableCodes) {
            if (scanningResult.contents.equals(result.contents)) {
                mCategoryType = TYPE_NON_RECYCABLE;
                return false;
            }
        }

        return true;

    }
}