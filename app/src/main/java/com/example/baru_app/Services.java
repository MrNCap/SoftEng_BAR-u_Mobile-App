package com.example.baru_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.baru_app.AUTHENTICATION.New_Device;
import com.example.baru_app.AUTHENTICATION.SendCredential;
import com.example.baru_app.AUTHENTICATION.UserNotVerified;
import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;


public class Services extends AppCompatActivity {
    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    DocumentReference ProfileVerificationChecker;
    String userID,sql_return_barangay;
    DatabaseHelper databasehelper;
    ConstraintLayout linearLayoutCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.services_page);
        linearLayoutCompat = findViewById(R.id.linearLayoutCompat);


        /// FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance();

        try{
        databasehelper = new DatabaseHelper(Services.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        ProfileVerificationChecker = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);


        //USER CHECKER IF VERIFIED
        ProfileVerificationChecker.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Boolean userVerification = value.getBoolean("verified");
                Boolean userCredential = value.getBoolean("credential");

                if(userCredential.equals(false)&&userVerification.equals(false)){
                        Intent intent = new Intent(Services.this, SendCredential.class);
                        startActivity(intent);
                        finish();
                }
                if(userCredential.equals(true)&&userVerification.equals(false)){
                    Intent intent = new Intent(Services.this, UserNotVerified.class);
                    startActivity(intent);
                    finish();
                }


            }
        });
        }catch (Exception e){
            // DETECT NEW DEVICE XD
            Intent intent = new Intent(Services.this, New_Device.class);
            startActivity(intent);
            finish();

        }








    }

    public void GoTo_Home(View view){
        startActivity(new Intent(getApplicationContext(),HomePage.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

    }
    public void GoTo_Profile(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

    }
}