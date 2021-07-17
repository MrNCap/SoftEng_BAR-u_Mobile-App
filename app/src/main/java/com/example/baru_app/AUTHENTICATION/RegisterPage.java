package com.example.baru_app.AUTHENTICATION;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baru_app.DATABASE_SQL.BarangayUserModel;
import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterPage extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[~!@#$%^&*()_+{}|;''.,/?])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    EditText fill_Email, fill_FirstName, fill_LastName, fill_Password, fill_PasswordConfirmation, fill_MiddleName, fill_DetailedAddress, fill_ContactNumber;
    Button Btn_Register,accept_terms,decline_terms,verification_btn;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    ProgressBar progressBar;
    String userID, email, password, firstName, lastName, passwordConfirm, middleName, address, barangay_selected, contactNo;
    DocumentReference documentReference;
    Dialog terms_dialog,verification_sent_dialog;
    Spinner spinner;
    DatabaseHelper databaseHelper;
    BarangayUserModel newUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_pagea_ctivity);
        //INSTANCE
        {
            fill_Email = findViewById(R.id.signinput_email);
            fill_FirstName = findViewById(R.id.signinput_firstname);
            fill_MiddleName = findViewById(R.id.signinput_middlename);
            fill_LastName = findViewById(R.id.signinput_lastname);
            fill_Password = findViewById(R.id.signinput_password);
            fill_PasswordConfirmation = findViewById(R.id.signinput_confirmationPassword);
            fill_DetailedAddress = findViewById(R.id.signinput_detailedAddress);
            fill_ContactNumber = findViewById(R.id.signinput_contanctNumber);
            Btn_Register = findViewById(R.id.Btn_register);
            progressBar = findViewById(R.id.progressBar_register);
        }
        //TERMS AND CONDITION DIALOG
        terms_dialog = new Dialog(this);
        terms_dialog.setContentView(R.layout.terms_and_condition_dialog);
        terms_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        accept_terms = terms_dialog.findViewById(R.id.accept_terms_btn);
        decline_terms = terms_dialog.findViewById(R.id.decline_terms_btn);


        //EMAIL SEND DIALOG
        verification_sent_dialog = new Dialog(this);
        verification_sent_dialog.setContentView(R.layout.email_sent);
        verification_sent_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        verification_btn = verification_sent_dialog.findViewById(R.id.okay_btn);



        //FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();


        ////

        //SPINNER BARANGAY
        {
            spinner = (Spinner) findViewById(R.id.brgy_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.barangay_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

        }

        Btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = fill_Email.getText().toString().trim();
                password = fill_Password.getText().toString();
                passwordConfirm = fill_PasswordConfirmation.getText().toString();
                firstName = fill_FirstName.getText().toString();
                middleName = fill_MiddleName.getText().toString();
                lastName = fill_LastName.getText().toString();
                address = fill_DetailedAddress.getText().toString();

                contactNo = fill_ContactNumber.getText().toString();

                barangay_selected = spinner.getSelectedItem().toString();
                String[] split_brgy = barangay_selected.split(" ");
                if(split_brgy.length == 1){
                    barangay_selected = split_brgy[0].toLowerCase();
                }else if(split_brgy.length == 2){
                    barangay_selected = split_brgy[0].toLowerCase()+split_brgy[1];
                }else{
                    barangay_selected = split_brgy[0].toLowerCase()+split_brgy[1]+split_brgy[2];
                }


                // INPUT CHECKER
                {
                    if (TextUtils.isEmpty(email)) {
                        fill_Email.setError("Email is Required");
                        return;
                    }
                    if (TextUtils.isEmpty(firstName)) {
                        fill_FirstName.setError("First Name is Required");
                        return;
                    }
                    if (TextUtils.isEmpty(middleName)) {
                        fill_MiddleName.setError("Middle Name is Required");
                        return;
                    }
                    if (TextUtils.isEmpty(lastName)) {
                        fill_LastName.setError("Last Name is Required");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        fill_Password.setError("Password is Required");
                        return;
                    }
////                if(TextUtils.isEmpty(passwordConfirm)) {
////                    fill_PasswordConfirmation.setError("Password Confirmation is Required");
////                    return;
////                }
////                if(!PASSWORD_PATTERN.matcher(password).matches()){
////                    fill_Password.setError("Password must have at least 8 characters, Should contain a number, symbol, lower case and upper case letters");
////                    return;
////                }
////                if(!password.equals(passwordConfirm)){
////                    fill_PasswordConfirmation.setError("Password entered is not the same.");
////                    return;
////                }
                    if (TextUtils.isEmpty(address)) {
                        fill_DetailedAddress.setError("Detailed Address is Required");
                        return;
                    }
                    if (TextUtils.isEmpty(address)) {
                        fill_ContactNumber.setError("Contact Number is Required");
                        return;
                    }
                }
                terms_dialog.show();



            }
        });

        accept_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //CREATE ACC
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
//
                        if (task.isSuccessful()) {

                            userID = firebaseAuth.getCurrentUser().getUid();
                            documentReference = firestoreDB.collection("barangays").document(barangay_selected).collection("users").document(userID);

                            Map<String, Object> user = new HashMap<>();
                            user.put("firstName", firstName);
                            user.put("middleName", middleName);
                            user.put("lastName", lastName);
                            user.put("email", email);
                            user.put("barangay", spinner.getSelectedItem().toString());
                            user.put("detailedAddress", address);
                            user.put("number", contactNo);
                            user.put("verified", false);
                            user.put("proofOfAddress", "Pending");
                            user.put("idVerification", "Pending");
                            user.put("credential", false);
                            user.put("admin", false);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    databaseHelper = new DatabaseHelper(RegisterPage.this);
                                    firebaseAuth = FirebaseAuth.getInstance();
                                    firestoreDB = FirebaseFirestore.getInstance();
                                    userID = firebaseAuth.getCurrentUser().getUid();

                                    newUser = new BarangayUserModel(-1,userID, barangay_selected);
                                    boolean success = databaseHelper.addOneUser(newUser);
                                    databaseHelper.close();
                                    terms_dialog.dismiss();

                                    Intent intent = new Intent(RegisterPage.this, SendCredential.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            Toast.makeText(RegisterPage.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });//END NEW ACC
            }
        });
        decline_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terms_dialog.dismiss();
            }
        });

    }

}


