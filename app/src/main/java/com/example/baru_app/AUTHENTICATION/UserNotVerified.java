package com.example.baru_app.AUTHENTICATION;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.POST_HOME.OpenPost;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UserNotVerified extends AppCompatActivity {
    DatabaseHelper databasehelper;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    DocumentReference ProfileVerificationChecker;
    String userID,sql_return_barangay;
    ImageView icon1,icon2,icon3;
    TextView resend_btn,id_title,address_title,resend_btn_id,resend_btn_address,show_file_id,show_file_address;
    FirebaseUser userEmail;
    Uri selectedFile_ID,selectedFile_Address;
    Dialog resendID,resendAddress;
    Button attach_id,attach_address,resend_id,resend_address,cancel_id,cancel_address;
    FirebaseStorage firebaseStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_not_verified_activity);
        id_title = findViewById(R.id.id_title);
        address_title = findViewById(R.id.addres_title);
        resend_btn_id = findViewById(R.id.resend_btn2);
        resend_btn_address = findViewById(R.id.resend_btn3);
        resend_btn = findViewById(R.id.resend_btn);
        icon1 = findViewById(R.id.icon_checker_1);
        icon2 = findViewById(R.id.icon_checker_2);
        icon3 = findViewById(R.id.icon_checker_3);
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        databasehelper = new DatabaseHelper(UserNotVerified.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        ProfileVerificationChecker = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);


        resendID = new Dialog(UserNotVerified.this);
        resendID.setContentView(R.layout.resend_address);
        resendID.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        attach_id = resendID.findViewById(R.id.file_upload_id2);
        resend_id = resendID.findViewById(R.id.next_replacement_btn);
        cancel_id = resendID.findViewById(R.id.cancel_replacement_btn);
        show_file_id = resendID.findViewById(R.id.id_show_text_fileUpload2);

        resendAddress = new Dialog(UserNotVerified.this);
        resendAddress.setContentView(R.layout.resend_id);
        resendAddress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        attach_address = resendAddress.findViewById(R.id.file_upload_id2);
        resend_address = resendAddress.findViewById(R.id.next_replacement_btn);
        cancel_address = resendAddress.findViewById(R.id.cancel_replacement_btn);
        show_file_address = resendAddress.findViewById(R.id.id_show_text_fileUpload2);


        firebaseAuth.getCurrentUser().reload();

        //USER CHECKER IF VERIFIED
        ProfileVerificationChecker.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                Boolean userVerification = value.getBoolean("Verified");
                Boolean userCredential = value.getBoolean("credential");
                String userIDVerification = value.get("idVerification").toString();
                String userProofAddress = value.get("proofOfAddress").toString();

                if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                    icon3.setImageResource(R.drawable.check_icon);
                } else {
                    icon3.setImageResource(R.drawable.x_icon);

                }

                    if (userIDVerification.equals("Pending")) {
                        icon1.setImageResource(R.drawable.search_icon);
                    } else if (userIDVerification.equals("Verified")) {
                        icon1.setImageResource(R.drawable.check_icon);
                    } else {
                        icon1.setImageResource(R.drawable.x_icon);
                        icon1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(UserNotVerified.this, "ID is not valid, please resend", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (userProofAddress.equals("Pending")) {
                        icon2.setImageResource(R.drawable.search_icon);
                    } else if (userProofAddress.equals("Verified")) {
                        icon2.setImageResource(R.drawable.check_icon);
                    } else {
                        icon2.setImageResource(R.drawable.x_icon);
                        icon2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(UserNotVerified.this, "Proof of Address not valid, please resend", Toast.LENGTH_SHORT).show();
                            }
                        });
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
                    //EMAIL RESEND
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


        resend_btn_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reupload_ID();
            }
        });
        resend_btn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reupload_Address();
            }
        });

    }
    public void Reupload_ID(){
        resendID.show();
        attach_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attach_File_ID.launch("image/*");
            }
        });
        cancel_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendID.dismiss();
            }
        });
        resend_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFile_ID != null){
                    ProgressDialog progressDialog_2 = new ProgressDialog(UserNotVerified.this);
                    progressDialog_2.setTitle("File Attachment Uploading (1/1)");
                    progressDialog_2.show();
                    StorageReference Id_storageRef = firebaseStorage.getReference().child("VALIDATION_FILES/users/ " +  userID + "/ID_VALIDATION");
                    Id_storageRef.putFile(selectedFile_ID).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Map<String, Object> user = new HashMap<>();
                                user.put("idVerification", "Pending");
                                ProfileVerificationChecker.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog_2.dismiss();
                                        resendID.dismiss();
                                        Toast.makeText(UserNotVerified.this, "ID Verification Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                });
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
                    Toast.makeText(UserNotVerified.this, "File Attachment is needed", Toast.LENGTH_SHORT).show();
                    attach_id.setBackgroundColor(Color.RED);
                }
            }
        });


    }
    public void Reupload_Address(){
        resendAddress.show();
        attach_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attach_File_Address.launch("image/*");
            }
        });
        cancel_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendAddress.dismiss();
            }
        });
        resend_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFile_Address != null){
                    ProgressDialog progressDialog_2 = new ProgressDialog(UserNotVerified.this);
                    progressDialog_2.setTitle("File Attachment Uploading (1/1)");
                    progressDialog_2.show();
                    StorageReference Id_storageRef = firebaseStorage.getReference().child("VALIDATION_FILES/users/ " +  userID + "/ADDRESS_PROOF");
                    Id_storageRef.putFile(selectedFile_Address).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Map<String, Object> user = new HashMap<>();
                                user.put("proofOfAddress", "Pending");
                                ProfileVerificationChecker.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog_2.dismiss();
                                        resendAddress.dismiss();
                                        Toast.makeText(UserNotVerified.this, "Proof of Address Verification Uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                });
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
                    Toast.makeText(UserNotVerified.this, "File Attachment is needed", Toast.LENGTH_SHORT).show();
                    attach_address.setBackgroundColor(Color.RED);
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
                        attach_id.setBackgroundColor(Color.BLUE);
                        show_file_id.setVisibility(View.VISIBLE);
                        show_file_id.setText(selectedFile_ID.getLastPathSegment());
                    }

                }
    });
    ActivityResultLauncher<String> attach_File_Address = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        selectedFile_Address = result;
                        attach_address.setBackgroundColor(Color.BLUE);
                        show_file_address.setVisibility(View.VISIBLE);
                        show_file_address.setText(selectedFile_Address.getLastPathSegment());
                    }

                }
            });
}