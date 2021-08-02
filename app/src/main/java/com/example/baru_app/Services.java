package com.example.baru_app;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.baru_app.AUTHENTICATION.New_Device;
import com.example.baru_app.AUTHENTICATION.SendCredential;
import com.example.baru_app.AUTHENTICATION.UserNotVerified;
import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.TRANSACTIONS_PAGE.BarangayID;
import com.example.baru_app.TRANSACTIONS_PAGE.Barangay_Certificate;
import com.example.baru_app.TRANSACTIONS_PAGE.Barangay_Clearance;
import com.example.baru_app.TRANSACTIONS_PAGE.Indigency;
import com.example.baru_app.TRANSACTIONS_PAGE.Residency;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Services extends AppCompatActivity {
    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    DocumentReference ProfileVerificationChecker;
    String userID,sql_return_barangay;
    DatabaseHelper databasehelper;
    ConstraintLayout linearLayoutCompat;
    Boolean transaction_checker;
    String transaction_type;
    Dialog healthCert;
    Button okayBtn;
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

        healthCert = new Dialog(this);
        healthCert.setContentView(R.layout.health_cert);
        healthCert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        okayBtn = healthCert.findViewById(R.id.send_forgotpass_btn);





        try{
            //USER CHECKER IF VERIFIED
            databasehelper = new DatabaseHelper(Services.this);
            sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
            databasehelper.close();
            ProfileVerificationChecker = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
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
            Intent intent = new Intent(Services.this, New_Device.class);
            startActivity(intent);
            finish();
        }



        try {

            transaction_checker = getIntent().getBooleanExtra("transaction_update",false);
            transaction_type = getIntent().getStringExtra("type");

            if(transaction_checker.equals(true)){
                if(transaction_type.equals("certificate_indigency")){
                    Map<String, Object> update_user_transaction = new HashMap<>();
                    update_user_transaction.put("certificate_indigency",true);
                    ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }else if(transaction_type.equals("certificate_residency")){
                    Map<String, Object> update_user_transaction = new HashMap<>();
                    update_user_transaction.put("certificate_residency",true);
                    ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }else if(transaction_type.equals("barangay_id")) {
                    Map<String, Object> update_user_transaction = new HashMap<>();
                    update_user_transaction.put("barangay_id", true);
                    ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                } else if(transaction_type.equals("barangay_id_replacement")) {
                    Map<String, Object> update_user_transaction = new HashMap<>();
                    update_user_transaction.put("barangay_id_replacement", true);
                    ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }else if(transaction_type.equals("barangay_certificate")) {
                    Map<String, Object> update_user_transaction = new HashMap<>();
                    update_user_transaction.put("barangay_certificate", true);
                    ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }else if(transaction_type.equals("barangay_clearance")) {
                    Map<String, Object> update_user_transaction = new HashMap<>();
                    update_user_transaction.put("barangay_clearance", true);
                    ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                }
            }
        }catch (Exception e){

        }

    }
    public void Transaction_1(View view){
        startActivity(new Intent(getApplicationContext(), Indigency.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void Transaction_2(View view){
        startActivity(new Intent(getApplicationContext(), Residency.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void Transaction_3(View view){
        startActivity(new Intent(getApplicationContext(), Barangay_Certificate.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void Transaction_4(View view){
        startActivity(new Intent(getApplicationContext(), Barangay_Clearance.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void Transaction_5(View view){
        startActivity(new Intent(getApplicationContext(), BarangayID.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void Transaction_6(View view){
        healthCert.show();
        okayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                healthCert.dismiss();
            }
        });
    }
    public void GoTo_Home(View view){
        startActivity(new Intent(getApplicationContext(),HomePage.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));

    }
    public void GoTo_Profile(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
}