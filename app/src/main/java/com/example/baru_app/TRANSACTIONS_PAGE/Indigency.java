package com.example.baru_app.TRANSACTIONS_PAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.example.baru_app.Services;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Indigency extends AppCompatActivity {
    RadioGroup purpose_radg;
    RadioButton purpose_1,purpose_2,purpose_3,purpose_4;
    EditText showOther;
    String purpose,uuid;
    Button send_request,okay_btn,okay_btn_success,send_request_overview,decline_request_overview,file_upload_id;
    CollectionReference RequestListRef;
    FirebaseFirestore firestoreDB;
    DocumentReference UserListRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,price;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    Dialog transaction_found,success_transaction,transaction_overview;
    TextView brgy_name,final_price,doc_name,doc_copy,doc_purpose,doc_price;
    Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indigency);

        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(Indigency.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();



        RequestListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("requests");
        UserListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);

        Date timestamp = new Date();
        String date_string = DateFormat.getDateInstance().format(timestamp);
        String time_string = DateFormat.getTimeInstance().format(timestamp);



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
        purpose_radg = findViewById(R.id.purpose_radg);
        purpose_1 = findViewById(R.id.purpose_1);
        purpose_2 = findViewById(R.id.purpose_2);
        purpose_3 = findViewById(R.id.purpose_3);
        purpose_4 = findViewById(R.id.purpose_4);
        showOther = findViewById(R.id.others_specify);
        final_price = findViewById(R.id.final_price);

        //SPINNER BARANGAY
            spinner = (Spinner) findViewById(R.id.num_copies);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.num_copies, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

        //USER PIC
        UserListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
        UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                brgy_name.setText("Brgy. " + value.getString("barangay"));
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position){
                    case 0:
                        final_price.setText("₱ 15.00");
                        break;
                    case 1:
                        final_price.setText("₱ 30.00");
                        break;
                    case 2:
                        final_price.setText("₱ 45.00");
                        break;
                    case 3:
                        final_price.setText("₱ 60.00");
                        break;
                    case 4:
                        final_price.setText("₱ 75.00");
                        break;
                }
                price = final_price.getText().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        purpose_radg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int isChecked) {
                showOther.setVisibility(View.INVISIBLE);
                showOther.setText("");
                switch (isChecked){
                    case R.id.purpose_1:
                        purpose = purpose_1.getText().toString();
                        break;
                    case R.id.purpose_2:
                        purpose = purpose_2.getText().toString();
                        break;
                    case R.id.purpose_3:
                        purpose = purpose_3.getText().toString();
                        break;
                    case R.id.purpose_4:
                        showOther.setVisibility(View.VISIBLE);
                        break;
                }

            }
        });



        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UserListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(purpose_4.isChecked()){
                                if (TextUtils.isEmpty(showOther.getText().toString().trim())) {
                                    showOther.setError("Purpose is Required");
                                    return;
                                }
                                    transaction_overview.show();
                                    doc_copy.setText(spinner.getSelectedItem().toString());
                                    doc_name.setText("Certificate of Indigency");
                                    doc_purpose.setText(showOther.getText().toString());
                                    doc_price.setText(price);

                                    send_request_overview.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if(value.getBoolean("certificate_indigency").equals(false)){
                                                Map<String, Object> user_request = new HashMap<>();
                                                user_request.put("author_id", userID);
                                                user_request.put("date_request", date_string);
                                                user_request.put("time_request", time_string);
                                                user_request.put("status", "Pending");
                                                user_request.put("date_complete", "");
                                                user_request.put("purpose", showOther.getText().toString());
                                                user_request.put("timestamp", timestamp);
                                                user_request.put("type", "certificate_indigency");
                                                user_request.put("price", price);
                                                user_request.put("copies", spinner.getSelectedItem().toString());
                                                user_request.put("fullName", value.getString("firstName")+ " " + value.getString("lastName"));
                                                user_request.put("address", value.getString("detailedAddress"));
                                                RequestListRef.document().set(user_request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        transaction_overview.dismiss();
                                                        success_transaction.show();
                                                        okay_btn_success.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Intent BackToServices = new Intent(Indigency.this,Services.class);
                                                                BackToServices.putExtra("transaction_update",true);
                                                                BackToServices.putExtra("type","certificate_indigency");
                                                                startActivity(BackToServices);
                                                            }
                                                        });

                                                    }
                                                });
                                            }else{
                                                transaction_found.show();
                                                okay_btn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        transaction_found.dismiss();
                                                        transaction_overview.dismiss();
                                                    }
                                                });

                                            }

                                        }
                                    });
                                     decline_request_overview.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                             transaction_overview.dismiss();
                                         }
                                     });


                            }else if(purpose_1.isChecked() || purpose_2.isChecked() || purpose_3.isChecked()){
                                transaction_overview.show();
                                doc_copy.setText(spinner.getSelectedItem().toString());
                                doc_name.setText("Certificate of Indigency");
                                doc_purpose.setText(purpose);
                                doc_price.setText(price);

                                    send_request_overview.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(value.getBoolean("certificate_indigency").equals(false)){
                                                Map<String, Object> user_request = new HashMap<>();
                                                user_request.put("author_id",userID);
                                                user_request.put("date_request",date_string);
                                                user_request.put("time_request", time_string);
                                                user_request.put("status","Pending");
                                                user_request.put("date_complete","");
                                                user_request.put("purpose",purpose);
                                                user_request.put("timestamp", timestamp);
                                                user_request.put("type", "certificate_indigency");
                                                user_request.put("price", price);
                                                user_request.put("copies", spinner.getSelectedItem().toString());
                                                user_request.put("fullName", value.getString("firstName")+ " " + value.getString("lastName"));
                                                user_request.put("address", value.getString("detailedAddress"));
                                                RequestListRef.document().set(user_request).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        transaction_overview.dismiss();
                                                        success_transaction.show();
                                                        okay_btn_success.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Intent BackToServices = new Intent(Indigency.this,Services.class);
                                                                BackToServices.putExtra("transaction_update",true);
                                                                BackToServices.putExtra("type","certificate_indigency");
                                                                startActivity(BackToServices);
                                                            }
                                                        });
                                                    }
                                                });
                                            }else{
                                                transaction_found.show();
                                                okay_btn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        transaction_found.dismiss();
                                                        transaction_overview.dismiss();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                decline_request_overview.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        transaction_overview.dismiss();
                                    }
                                });

                            }else{
                                Toast.makeText(Indigency.this, "Please pick a purpose", Toast.LENGTH_SHORT).show();
                            }

                    }
                });
            }
        });







    }
    public void GoBack_service(View view){
        startActivity(new Intent(getApplicationContext(), Services.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
}