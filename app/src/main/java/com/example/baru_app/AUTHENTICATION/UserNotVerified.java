package com.example.baru_app.AUTHENTICATION;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.example.baru_app.Services;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class UserNotVerified extends AppCompatActivity {
    DatabaseHelper databasehelper;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    DocumentReference ProfileVerificationChecker;
    String userID,sql_return_barangay;
    ImageView icon1,icon2,icon3;
    TextView resend_btn;
    FirebaseUser userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_not_verified_activity);
        resend_btn = findViewById(R.id.resend_btn);
        icon1 = findViewById(R.id.icon_checker_1);
        icon2 = findViewById(R.id.icon_checker_2);
        icon3 = findViewById(R.id.icon_checker_3);
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        databasehelper = new DatabaseHelper(UserNotVerified.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        ProfileVerificationChecker = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);


        //USER CHECKER IF VERIFIED
        ProfileVerificationChecker.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                Boolean userVerification = value.getBoolean("Verified");
                Boolean userCredential = value.getBoolean("credential");
                String userIDVerification = value.get("idVerification").toString();
                String userProofAddress = value.get("proofOfAddress").toString();


                    if (userIDVerification.equals("Pending")) {
                        icon1.setImageResource(R.drawable.search_icon);
                    } else if (userIDVerification.equals("Verified")) {
                        icon1.setImageResource(R.drawable.check_icon);
                    } else {
                        icon1.setImageResource(R.drawable.x_icon);
                    }

                    if (userProofAddress.equals("Pending")) {
                        icon2.setImageResource(R.drawable.search_icon);
                    } else if (userProofAddress.equals("Verified")) {
                        icon2.setImageResource(R.drawable.check_icon);
                    } else {
                        icon2.setImageResource(R.drawable.x_icon);
                    }

                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        icon3.setImageResource(R.drawable.check_icon);
                    } else {
                        icon3.setImageResource(R.drawable.x_icon);

                    }
                    if(userCredential.equals(true) && userProofAddress.equals("Verified") && userIDVerification.equals("Verified") && firebaseAuth.getCurrentUser().isEmailVerified()){
                        ProfileVerificationChecker = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);

                        Map<String, Object> user = new HashMap<>();
                        user.put("verified", true);
                        ProfileVerificationChecker.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent intent = new Intent(UserNotVerified.this, Services.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }

                resend_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        userEmail = firebaseAuth.getCurrentUser();
                        userEmail.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar.make(view, "Email Verification Sent", Snackbar.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserNotVerified.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });

        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            private long time = 0;
            @Override
            public void run()
            {
                firebaseAuth.getCurrentUser().reload();
                time += 1000;
                h.postDelayed(this, 1000);
            }
        }, 1000); // 1 second delay (takes millis)
    }
}