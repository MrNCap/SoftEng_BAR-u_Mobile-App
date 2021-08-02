package com.example.baru_app.AUTHENTICATION;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.baru_app.R;
import com.example.baru_app.Services;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

public class Login extends AppCompatActivity {
    EditText login_email, login_pass;
    FirebaseAuth firebaseAuth;
    Button BtnLog,send_email_btn;
    Dialog forgotpass,loading_layout;
    TextView send_email_input;
    FirebaseFirestore firestoreDB;
    FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page_activity);
        //INSTANCE
        {
            login_email = findViewById(R.id.input_email);
            login_pass = findViewById(R.id.input_password);
            BtnLog = findViewById(R.id.Btn_log);
        }
        //FIREBASE INSTANCE
        {
            firebaseStorage = FirebaseStorage.getInstance();
            firestoreDB = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();

        }
        //DIALOG FORGOTPASS
        {
            forgotpass = new Dialog(this);
            forgotpass.setContentView(R.layout.forgot_pass);
            forgotpass.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            send_email_input = forgotpass.findViewById(R.id.send_email_input);
            send_email_btn = forgotpass.findViewById(R.id.send_forgotpass_btn);
        }

        //DIALOG LOADING LAYOUT
        {
            loading_layout = new Dialog(this);
            loading_layout.setContentView(R.layout.loading_layout);
            loading_layout.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        }


        //USER LOGIN CHECKER
        {
            if (firebaseAuth.getCurrentUser() != null) {
                Intent intent_to_upload = new Intent(Login.this, Services.class);
                startActivity(intent_to_upload);
                finish();
            }
        }











        BtnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = login_email.getText().toString().trim();
                String password = login_pass.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    login_email.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    login_pass.setError("Password is Required");
                    return;
                }

                loading_layout.show();

                //Login Phase
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(), Services.class));
                            finish();
                        }else{
                            Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });





    }


    public void RegisterNewAcc(View view){
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }
    public void ForgotPass(View view){
        send_email_input.setText("");
        forgotpass.show();
        send_email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String emailR = send_email_input.getText().toString();
                if(TextUtils.isEmpty(send_email_input.getText().toString())){
                    send_email_input.setError("Email is Required");
                    return;
                }
                firebaseAuth.sendPasswordResetEmail(emailR).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Login.this, "Reset link sent Succesfully", Toast.LENGTH_SHORT).show();
                        forgotpass.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        forgotpass.dismiss();
                    }
                });
            }
        });

    }
}