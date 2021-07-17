package com.example.baru_app.AUTHENTICATION;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.example.baru_app.Services;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class SendCredential extends AppCompatActivity {
    Uri selectedFile_ID, selectedFile_Address;
    TextView id_show_text_fileUpload, address_show_text_fileUpload;
    Button send_verification_btn, file_upload_id, file_upload_address,verification_btn;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    DocumentReference documentReference;
    FirebaseStorage firebaseStorage;
    String userID,sql_return_barangay;
    Dialog verification_sent_dialog;
    DatabaseHelper databasehelper;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_credential);
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //EMAIL SEND DIALOG
        verification_sent_dialog = new Dialog(this);
        verification_sent_dialog.setContentView(R.layout.email_sent);
        verification_sent_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        verification_btn = verification_sent_dialog.findViewById(R.id.okay_btn);

        file_upload_id = findViewById(R.id.file_upload_id);
        file_upload_address = findViewById(R.id.file_upload_address);
        send_verification_btn = findViewById(R.id.send_verification);
        address_show_text_fileUpload = findViewById(R.id.address_show_text_fileUpload);
        id_show_text_fileUpload = findViewById(R.id.id_show_text_fileUpload);

        verification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verification_sent_dialog.dismiss();
            }
        });

        file_upload_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attach_File_ID.launch("image/*");

            }
        });

        file_upload_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attach_File_Address.launch("image/*");
            }
        });


        send_verification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedFile_ID != null && selectedFile_Address != null) {
                    StorageReference Id_storageRef = firebaseStorage.getReference().child("VALIDATION_FILES/users/ " +  userID + "/ID_VALIDATION");
                    Id_storageRef.putFile(selectedFile_ID).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        }

                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        }
                    });

                    ProgressDialog progressDialog_2 = new ProgressDialog(SendCredential.this);
                    progressDialog_2.setTitle("File Attachment Uploading (2/2)");
                    progressDialog_2.show();
                    StorageReference Address_storageRef = firebaseStorage.getReference().child("VALIDATION_FILES/users/ " +  userID + "/ADDRESS_PROOF");
                    Address_storageRef.putFile(selectedFile_ID).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {

                                //sendverification
                                FirebaseUser userEmail = firebaseAuth.getCurrentUser();
                                userEmail.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SendCredential.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                databasehelper = new DatabaseHelper(SendCredential.this);
                                sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
                                databasehelper.close();

                                documentReference = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
                                Map<String, Object> user = new HashMap<>();
                                user.put("credential", true);
                                documentReference.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Snackbar.make(view, "Verification Uploaded", Snackbar.LENGTH_LONG).show();
                                        verification_sent_dialog.show();
                                        verification_sent_dialog.setCanceledOnTouchOutside(false);
                                        verification_sent_dialog.setCancelable(false);
                                        verification_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(SendCredential.this, UserNotVerified.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                    }
                                });
                                progressDialog_2.dismiss();
//                                Toast.makeText(SendCredential.this, selectedFile_Address.getLastPathSegment(), Toast.LENGTH_SHORT).show();
                            } else {

                            }
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                            progressDialog_2.setMessage("Progress: " + (int) progress + "%");

                        }
                    });



                        }else{
                            Toast.makeText(SendCredential.this, "File Attachment is needed", Toast.LENGTH_SHORT).show();
                            file_upload_id.setBackgroundColor(Color.RED);
                            file_upload_address.setBackgroundColor(Color.RED);
                        }
                    }
                });




    }
        ActivityResultLauncher<String> attach_File_ID = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            selectedFile_ID = result;
                            file_upload_id.setBackgroundColor(Color.BLUE);
                            id_show_text_fileUpload.setVisibility(View.VISIBLE);
                        }

                    }
                });
        ActivityResultLauncher<String> attach_File_Address = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            selectedFile_Address = result;
                            file_upload_address.setBackgroundColor(Color.BLUE);
                            address_show_text_fileUpload.setVisibility(View.VISIBLE);
                        }

                    }
                });

}