ahpackage com.example.baru_app.TRANSACTIONS_PAGE;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baru_app.AUTHENTICATION.New_Device;
import com.example.baru_app.DATABASE_SQL.BarangayUserModel;
import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.example.baru_app.Services;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BarangayID extends AppCompatActivity {
    RadioGroup verify_radg,reason_radg;
    RadioButton type_verify_1,type_verify_2,reason_1,reason_2,reason_3;
    EditText verify_num,reason_other;
    Button send_request,okay_btn,okay_btn_success,next_replacement,cancel_replacement,send_request_overview,decline_request_overview,file_upload_1x1,date_picker,next_btn_comelec,next_btn_national_id,try_again_api;
    Button next_replacement_btn,cancel_replacement_btn,replacement_upload;
    CollectionReference RequestListRef,api_checker;
    FirebaseFirestore firestoreDB;
    DocumentReference UserListRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,birthdate,date_string,time_string;
    String reason_replacement;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    Dialog transaction_found,success_transaction,transaction_overview,comelec_api,national_id_api,no_user_found_api,already_user,id_replacement,vin,psn;
    TextView brgy_name,doc_name,doc_copy,doc_purpose,doc_price,verfiy_type,showDate,show_filename,doc_purpose_title,upload_showname;
    ImageView verify_icon;
    DatePickerDialog.OnDateSetListener datePickerListener;
    Uri selected_pic,selected_pic_replacement;
    Boolean checker_api;
    Date timestamp;
    String methodVerify,uuid;
    Button okay_vin,okay_psn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barangay_id);
        birthdate = null;
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        uuid = UUID.randomUUID().toString();

        //DB SQL
        databasehelper = new DatabaseHelper(BarangayID.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();


        RequestListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("requests");
        UserListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);


        timestamp = new Date();
        date_string = DateFormat.getDateInstance().format(timestamp);
        time_string = DateFormat.getTimeInstance().format(timestamp);

        //COMELEC API  DIALOG
        vin = new Dialog(this);
        vin.setContentView(R.layout.vin);
        vin.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        okay_vin = vin.findViewById(R.id.accept_terms_btn);

        psn = new Dialog(this);
        psn.setContentView(R.layout.psn);
        psn.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        okay_psn = psn.findViewById(R.id.accept_terms_btn);


        //COMELEC API  DIALOG
        comelec_api = new Dialog(this);
        comelec_api.setContentView(R.layout.comelec_api_found);
        comelec_api.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        next_btn_comelec = comelec_api.findViewById(R.id.next_btn);

        national_id_api = new Dialog(this);
        national_id_api.setContentView(R.layout.national_id_api_found);
        national_id_api.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        next_btn_national_id = national_id_api.findViewById(R.id.next_btn);

        no_user_found_api = new Dialog(this);
        no_user_found_api.setContentView(R.layout.no_user_found_api);
        no_user_found_api.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        try_again_api = no_user_found_api.findViewById(R.id.try_again_btn);

        //ALREADY HAVE AN ID  DIALOG
        already_user = new Dialog(this);
        already_user.setContentView(R.layout.already_have_id);
        already_user.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        next_replacement = already_user.findViewById(R.id.accept_terms_btn);
        cancel_replacement = already_user.findViewById(R.id.decline_terms_btn);

        //REPLACEMENT ID
        id_replacement = new Dialog(this);
        id_replacement.setContentView(R.layout.replacement_id);
        id_replacement.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        next_replacement_btn = id_replacement.findViewById(R.id.next_replacement_btn);
        cancel_replacement_btn = id_replacement.findViewById(R.id.cancel_replacement_btn);
        reason_radg = id_replacement.findViewById(R.id.reason_radg);
        reason_1= id_replacement.findViewById(R.id.reason_1);
        reason_2= id_replacement.findViewById(R.id.reason_2);
        reason_3= id_replacement.findViewById(R.id.reason_3);
        reason_other = id_replacement.findViewById(R.id.others_specify4);

        //OVERVIEW  DIALOG
        transaction_overview = new Dialog(this);
        transaction_overview.setContentView(R.layout.overview_transaction);
        transaction_overview.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        send_request_overview = transaction_overview.findViewById(R.id.accept_transaction_btn);
        decline_request_overview = transaction_overview.findViewById(R.id.decline_transaction_btn);
        doc_copy = transaction_overview.findViewById(R.id.doc_copy);
        doc_name = transaction_overview.findViewById(R.id.doc_name);
        doc_price = transaction_overview.findViewById(R.id.doc_price);
        doc_purpose = transaction_overview.findViewById(R.id.doc_purpose);
        doc_purpose_title = transaction_overview.findViewById(R.id.purpost_title);


        //REQUEST FOUND DIALOG
        transaction_found = new Dialog(this);
        transaction_found.setContentView(R.layout.transaction_checker);
        transaction_found.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        transaction_found.setCanceledOnTouchOutside(false);
        transaction_found.setCancelable(false);
        okay_btn = transaction_found.findViewById(R.id.okay_btn);

        success_transaction = new Dialog(this);
        success_transaction.setContentView(R.layout.succes_transaction_checker);
        success_transaction.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        success_transaction.setCanceledOnTouchOutside(false);
        success_transaction.setCancelable(false);
        okay_btn_success = success_transaction.findViewById(R.id.okay_btn);

        /// Widgets
        brgy_name = findViewById(R.id.brgy_name2);
        send_request = findViewById(R.id.send_request);
        verify_radg = findViewById(R.id.radioG_verify);
        type_verify_1 = findViewById(R.id.rad1);
        type_verify_2 = findViewById(R.id.rad2);
        verfiy_type = findViewById(R.id.verfiy_type);
        verify_icon = findViewById(R.id.verify_icon);
        verify_num = findViewById(R.id.verify_num);
        date_picker = findViewById(R.id.date_picker);
        showDate = findViewById(R.id.show_pickdate);
        file_upload_1x1 = findViewById(R.id.file_upload_id3);
        show_filename = findViewById(R.id.id_show_text_fileUpload4);

        //USER PIC
        UserListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
        UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                brgy_name.setText("Brgy. " + value.getString("barangay"));
            }
        });
        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int yr = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(BarangayID.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,datePickerListener,yr,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month +1;
                date_picker.setBackgroundColor(Color.parseColor("#31a35d"));
                date_picker.setText(month+"/"+day+"/"+year);
                birthdate = month+"/"+day+"/"+year;
            }
        };
        file_upload_1x1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attach_File.launch("image/*");

            }
        });
        verify_radg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int isChecked) {
                switch (isChecked){
                    case R.id.rad1:
                        verify_num.setVisibility(View.VISIBLE);
                        verfiy_type.setVisibility(View.VISIBLE);
                        verify_icon.setVisibility(View.VISIBLE);
                        verfiy_type.setText("Voter's ID: VIN Number");
                        verify_num.setHint("VIN Number");
                        verify_icon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                vin.show();
                                okay_vin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        vin.dismiss();
                                    }
                                });
                            }
                        });
                        break;
                    case R.id.rad2:
                        verify_num.setVisibility(View.VISIBLE);
                        verfiy_type.setVisibility(View.VISIBLE);
                        verify_icon.setVisibility(View.VISIBLE);
                        verfiy_type.setText("National ID: PSN Number");
                        verify_num.setHint("PSN Number");
                        verify_icon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                psn.show();
                                okay_psn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        psn.dismiss();
                                    }
                                });
                            }
                        });
                        break;
                }
            }
        });



        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selected_pic != null){
                    if(birthdate != null){
                        if(type_verify_1.isChecked()){
                            ///
                            if (TextUtils.isEmpty(verify_num.getText().toString().trim())) {
                                verify_num.setError("VIN Number is Required");
                                return;
                            }
                            UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable  DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(value.getBoolean("barangay_id_replacement").equals(true)){
                                        transaction_found.show();
                                        okay_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                transaction_found.dismiss();
                                            }
                                        });
                                        return;
                                    }
                                    if(value.getBoolean("barangay_id").equals(false)){
                                        api_checker = firestoreDB.collection("comelec_api_mock");
                                        api_checker.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                // FOR EACH
                                                for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    if (verify_num.getText().toString().equals(documentSnapshot.getId())) {
                                                        comelec_api.show();
                                                        next_btn_comelec.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                comelec_api.dismiss();
                                                                transaction_overview.show();
                                                                doc_name.setText("Barangay ID");
                                                                doc_price.setText("₱ 150.00");
                                                                doc_purpose_title.setText("Method:");
                                                                doc_purpose.setText("Voter's ID");
                                                                send_request_overview.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        ProgressDialog progressDialog_1x1 = new ProgressDialog(BarangayID.this);
                                                                        progressDialog_1x1.setTitle("File Attachment Uploading (1/1)");
                                                                        progressDialog_1x1.show();
                                                                        StorageReference Address_storageRef = firebaseStorage.getReference().child("REQUEST/" +  uuid + "/1x1");
                                                                        Address_storageRef.putFile(selected_pic).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                Map<String, Object> user_request = new HashMap<>();
                                                                                user_request.put("author_id", userID);
                                                                                user_request.put("date_request", date_string);
                                                                                user_request.put("time_request", time_string);
                                                                                user_request.put("status", "Pending");
                                                                                user_request.put("date_complete", "");
                                                                                user_request.put("method", "Voter's ID");
                                                                                user_request.put("method_id", verify_num.getText().toString());
                                                                                user_request.put("timestamp", timestamp);
                                                                                user_request.put("type", "barangay_id");
                                                                                user_request.put("price", "₱ 150.00");
                                                                                user_request.put("copies", "1");
                                                                                user_request.put("fullName", value.getString("firstName")+ " " + value.getString("lastName"));
                                                                                user_request.put("address", value.getString("detailedAddress"));
                                                                                user_request.put("storage_id", uuid);
                                                                                RequestListRef.document().set(user_request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        transaction_overview.dismiss();
                                                                                        success_transaction.show();
                                                                                        okay_btn_success.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View view) {
                                                                                                Intent BackToServices = new Intent(BarangayID.this,Services.class);
                                                                                                BackToServices.putExtra("transaction_update",true);
                                                                                                BackToServices.putExtra("type","barangay_id");
                                                                                                startActivity(BackToServices);
                                                                                            }
                                                                                        });

                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                                                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                                                                progressDialog_1x1.setMessage("Progress: " + (int) progress + "%");

                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Toast.makeText(BarangayID.this, "Transaction Failed: ERROR["+e.toString()+"]", Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                                decline_request_overview.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        transaction_overview.dismiss();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        checker_api = true;
                                                        break;
                                                    }
                                                    checker_api = false;
                                                }
                                                if (checker_api == false) {
                                                    no_user_found_api.show();
                                                    try_again_api.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            no_user_found_api.dismiss();
                                                        }
                                                    });

                                                }


                                            }
                                        });//END FAIL-SUCCESS LISTENER
                                    }else{
                                        //already have id
                                        api_checker = firestoreDB.collection("comelec_api_mock");
                                        api_checker.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    if (verify_num.getText().toString().equals(documentSnapshot.getId())) {
                                                        comelec_api.show();
                                                        next_btn_comelec.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                already_user.show();
                                                                next_replacement.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        comelec_api.dismiss();
                                                                        id_replacement.show();
                                                                        replace_id();
                                                                    }
                                                                });
                                                                cancel_replacement.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        already_user.dismiss();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        checker_api = true;
                                                        break;
                                                    }
                                                    checker_api = false;
                                                } if (checker_api == false) {
                                                    no_user_found_api.show();
                                                    try_again_api.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            no_user_found_api.dismiss();
                                                        }
                                                    });

                                                }

                                            }
                                        });



                                    }
                                }
                            });



                        } else if(type_verify_2.isChecked()){
                            ///
                            if (TextUtils.isEmpty(verify_num.getText().toString().trim())) {
                                verify_num.setError("PSN Number is Required");
                                return;
                            }
                                UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable  DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(value.getBoolean("barangay_id_replacement").equals(true)){
                                        transaction_found.show();
                                        okay_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                transaction_found.dismiss();
                                            }
                                        });
                                        return;
                                    }
                                    if(value.getBoolean("barangay_id").equals(false)){
                                        api_checker = firestoreDB.collection("national_id_api_mock");
                                        api_checker.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                // FOR EACH
                                                for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    if (verify_num.getText().toString().equals(documentSnapshot.getId())) {
                                                        national_id_api.show();
                                                        next_btn_national_id.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                national_id_api.dismiss();
                                                                transaction_overview.show();
                                                                doc_name.setText("Barangay ID");
                                                                doc_price.setText("₱ 150.00");
                                                                doc_purpose_title.setText("Method:");
                                                                doc_purpose.setText("National ID");
                                                                send_request_overview.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        ProgressDialog progressDialog_1x1 = new ProgressDialog(BarangayID.this);
                                                                        progressDialog_1x1.setTitle("File Attachment Uploading (1/1)");
                                                                        progressDialog_1x1.show();
                                                                        StorageReference Address_storageRef = firebaseStorage.getReference().child("REQUEST/" +  uuid + "/1x1");
                                                                        Address_storageRef.putFile(selected_pic).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                Map<String, Object> user_request = new HashMap<>();
                                                                                user_request.put("author_id", userID);
                                                                                user_request.put("date_request", date_string);
                                                                                user_request.put("time_request", time_string);
                                                                                user_request.put("status", "Pending");
                                                                                user_request.put("date_complete", "");
                                                                                user_request.put("method", "National ID");
                                                                                user_request.put("method_id", verify_num.getText().toString());
                                                                                user_request.put("timestamp", timestamp);
                                                                                user_request.put("type", "barangay_id");
                                                                                user_request.put("price", "₱ 150.00");
                                                                                user_request.put("copies", "1");
                                                                                user_request.put("fullName", value.getString("firstName")+ " " + value.getString("lastName"));
                                                                                user_request.put("address", value.getString("detailedAddress"));
                                                                                user_request.put("storage_id", uuid);
                                                                                RequestListRef.document().set(user_request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        transaction_overview.dismiss();
                                                                                        success_transaction.show();
                                                                                        okay_btn_success.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View view) {
                                                                                                Intent BackToServices = new Intent(BarangayID.this,Services.class);
                                                                                                BackToServices.putExtra("transaction_update",true);
                                                                                                BackToServices.putExtra("type","barangay_id");
                                                                                                startActivity(BackToServices);
                                                                                            }
                                                                                        });
                                                                                        decline_request_overview.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View view) {
                                                                                                transaction_overview.dismiss();

                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                            @Override
                                                                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                                                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                                                                progressDialog_1x1.setMessage("Progress: " + (int) progress + "%");

                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Toast.makeText(BarangayID.this, "Transaction Failed: ERROR["+e.toString()+"]", Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                            }
                                                        });



                                                        checker_api = true;
                                                        break;
                                                    }
                                                    checker_api = false;
                                                }
                                                if (checker_api == false) {
                                                    no_user_found_api.show();
                                                    try_again_api.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            no_user_found_api.dismiss();
                                                        }
                                                    });
                                                }


                                            }
                                        });//END FAIL-SUCCESS LISTENER
                                    }else{
                                        //already have id
                                        api_checker = firestoreDB.collection("national_id_api_mock");
                                        api_checker.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                for (final QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    if (verify_num.getText().toString().equals(documentSnapshot.getId())) {
                                                        national_id_api.show();
                                                        next_btn_national_id.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                already_user.show();
                                                                next_replacement.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        national_id_api.dismiss();
                                                                        id_replacement.show();
                                                                        replace_id();
                                                                    }
                                                                });
                                                                cancel_replacement.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        already_user.dismiss();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        checker_api = true;
                                                        break;
                                                    }
                                                    checker_api = false;
                                                } if (checker_api == false) {
                                                    no_user_found_api.show();
                                                    try_again_api.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            no_user_found_api.dismiss();
                                                        }
                                                    });

                                                }

                                            }
                                        });


                                    }
                                }
                            });

                        }else{
                            Toast.makeText(BarangayID.this, "Please select a verification method", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        date_picker.setBackgroundColor(Color.RED);
                        Toast.makeText(BarangayID.this, "Please put birthdate", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    file_upload_1x1.setBackgroundColor(Color.RED);
                    Toast.makeText(BarangayID.this, "Please attach a file", Toast.LENGTH_SHORT).show();
                }
            }
        });





    }
    public void replace_id(){
        reason_radg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int isChecked) {
                reason_other.setVisibility(View.INVISIBLE);
                reason_other.setText("");
                switch (isChecked){
                    case R.id.reason_1:
                        reason_replacement = "Lost or Stolen";
                        break;
                    case R.id.reason_2:
                        reason_replacement = "Damaged";
                        break;
                    case R.id.reason_3:
                        reason_replacement = "Needs Amendment";
                        reason_other.setVisibility(View.VISIBLE);
                        break;
                }

            }
        });

        next_replacement_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if(reason_1.isChecked() || reason_2.isChecked()){
                        id_replacement.dismiss();
                        if(type_verify_1.isChecked()){
                            methodVerify = "Voter's ID";
                        }else if(type_verify_2.isChecked()){
                            methodVerify = "National ID";
                        }
                        already_user.dismiss();
                        transaction_overview.show();
                        doc_name.setText("Barangay ID Replacement");
                        doc_price.setText("₱ 150.00");
                        doc_purpose_title.setText("Reason:");
                        doc_purpose.setText(reason_replacement);
                        send_request_overview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                        ProgressDialog progressDialog_1x1 = new ProgressDialog(BarangayID.this);
                                        progressDialog_1x1.setTitle("File Attachment Uploading (1/1)");
                                        progressDialog_1x1.show();
                                        StorageReference Address_storageRef = firebaseStorage.getReference().child("REQUEST/" +  uuid + "/1x1");
                                        Address_storageRef.putFile(selected_pic).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                Map<String, Object> user_request = new HashMap<>();
                                                user_request.put("author_id", userID);
                                                user_request.put("date_request", date_string);
                                                user_request.put("time_request", time_string);
                                                user_request.put("status", "Pending");
                                                user_request.put("date_complete", "");
                                                user_request.put("method", methodVerify);
                                                user_request.put("method_id",  verify_num.getText().toString());
                                                user_request.put("reason", reason_replacement);
                                                user_request.put("reason_other", "");
                                                user_request.put("timestamp", timestamp);
                                                user_request.put("type", "barangay_id_replacement");
                                                user_request.put("price", "₱ 150.00");
                                                user_request.put("copies", "1");
                                                user_request.put("fullName", value.getString("firstName")+ " " + value.getString("lastName"));
                                                user_request.put("storage_id", uuid);
                                                RequestListRef.document().set(user_request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        transaction_overview.dismiss();
                                                        success_transaction.show();
                                                        okay_btn_success.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Intent BackToServices = new Intent(BarangayID.this,Services.class);
                                                                BackToServices.putExtra("transaction_update",true);
                                                                BackToServices.putExtra("type","barangay_id_replacement");
                                                                startActivity(BackToServices);
                                                            }
                                                        });


                                                    }
                                                });
                                            }
                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                                progressDialog_1x1.setMessage("Progress: " + (int) progress + "%");

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(BarangayID.this, "Transaction Failed: ERROR["+e.toString()+"]", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                });
                            }
                        });
                        decline_request_overview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                transaction_overview.dismiss();
                            }
                        });
                    }else if(reason_3.isChecked()){
                        if (TextUtils.isEmpty(reason_other.getText().toString().trim())) {
                            reason_other.setError("Amendment is Required");
                            return;
                        }
                        already_user.dismiss();
                        id_replacement.dismiss();
                        transaction_overview.show();
                        doc_name.setText("Barangay ID");
                        doc_price.setText("₱ 150.00");
                        doc_purpose_title.setText("Reason:");
                        doc_purpose.setText(reason_other.getText().toString());
                        send_request_overview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                        ProgressDialog progressDialog_1x1 = new ProgressDialog(BarangayID.this);
                                        progressDialog_1x1.setTitle("File Attachment Uploading (1/1)");
                                        progressDialog_1x1.show();
                                        StorageReference Address_storageRef = firebaseStorage.getReference().child("REQUEST/" +  uuid + "/1x1");
                                        Address_storageRef.putFile(selected_pic).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                Map<String, Object> user_request = new HashMap<>();
                                                user_request.put("author_id", userID);
                                                user_request.put("date_request", date_string);
                                                user_request.put("time_request", time_string);
                                                user_request.put("status", "Pending");
                                                user_request.put("date_complete", "");
                                                user_request.put("method", methodVerify);
                                                user_request.put("method_id",  verify_num.getText().toString());
                                                user_request.put("reason",reason_replacement );
                                                user_request.put("reason_other", reason_other.getText().toString());
                                                user_request.put("timestamp", timestamp);
                                                user_request.put("type", "barangay_id_replacement");
                                                user_request.put("price", "₱ 150.00");
                                                user_request.put("copies", "1");
                                                user_request.put("fullName", value.getString("firstName")+ " " + value.getString("lastName"));
                                                RequestListRef.document().set(user_request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        transaction_overview.dismiss();
                                                        success_transaction.show();
                                                        okay_btn_success.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Intent BackToServices = new Intent(BarangayID.this,Services.class);
                                                                BackToServices.putExtra("transaction_update",true);
                                                                BackToServices.putExtra("type","barangay_id_replacement");
                                                                startActivity(BackToServices);
                                                            }
                                                        });


                                                    }
                                                });
                                            }
                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                                double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                                                progressDialog_1x1.setMessage("Progress: " + (int) progress + "%");

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(BarangayID.this, "Transaction Failed: ERROR["+e.toString()+"]", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                });
                            }
                        });
                        decline_request_overview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                transaction_overview.dismiss();
                            }
                        });
                    }else{
                        Toast.makeText(BarangayID.this, "Please select select a reason", Toast.LENGTH_SHORT).show();
                    }


            }




        });
        cancel_replacement_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id_replacement.dismiss();
            }
        });

    }
    public void GoBack_service(View view){
        startActivity(new Intent(getApplicationContext(), Services.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    ActivityResultLauncher<String> attach_File = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        selected_pic = result;
                        file_upload_1x1.setBackgroundColor(Color.BLUE);
                        show_filename.setVisibility(View.VISIBLE);
                        show_filename.setText(selected_pic.getLastPathSegment());
                    }
                }
            });
}