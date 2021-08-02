package com.example.baru_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.POST_HOME.OpenPost;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.HashMap;
import java.util.Map;

public class OpenTransaction extends AppCompatActivity {
    FirebaseFirestore firestoreDB;
    DocumentReference TransactionRef,ProfileVerificationChecker;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,pass_ID;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    TextView open_transaction_name,transaction_ID,date_requested,time_requested,copies,price,time_completed,date_completed,purpose_requested;
    ImageView status_transaction,transaction_barcode;
    Dialog transactionSetting,delete_transaction;
    Button yes_request,no_request,edit,cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_transaction);

        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(OpenTransaction.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();

        //WIDGETS
        pass_ID = getIntent().getStringExtra("pass_ID");
        open_transaction_name = findViewById(R.id.open_transaction_name);
        status_transaction = findViewById(R.id.status_transaction);
        transaction_ID = findViewById(R.id.transaction_ID);
        date_requested = findViewById(R.id.date_requested);
        time_requested = findViewById(R.id.time_requested);
        copies = findViewById(R.id.copies);
        price = findViewById(R.id.price);
        time_completed = findViewById(R.id.time_completed);
        date_completed = findViewById(R.id.date_completed);
        purpose_requested = findViewById(R.id.purpose_requested);
        transaction_barcode = findViewById(R.id.barcode_generate);


        //SETTINGS DIALOG
        transactionSetting = new Dialog(this);
        transactionSetting.setContentView(R.layout.transaction_settings_popout);
        transactionSetting.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cancel = transactionSetting.findViewById(R.id.cancel_transaction);

        //Delete Post User dialog
        delete_transaction = new Dialog(OpenTransaction.this);
        delete_transaction.setContentView(R.layout.cancel_transaction_dialog);
        delete_transaction.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //widgets
        yes_request = delete_transaction.findViewById(R.id.accept_delete_btn);
        no_request = delete_transaction.findViewById(R.id.decline_delete_btn);


        ///PUT TRANSACTION DATA
        ProfileVerificationChecker = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
        TransactionRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("requests").document(pass_ID);
        try {
        TransactionRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                try {
                if(value.getString("type").equals("certificate_residency")){
                    open_transaction_name.setText("Certificate of Residency");
                }else if(value.getString("type").equals("certificate_indigency")){
                    open_transaction_name.setText("Certificate of Indigency");
                }else if(value.getString("type").equals("barangay_id_replacement")){
                    open_transaction_name.setText("Barangay ID [REPLACEMENT]");
                }else if(value.getString("type").equals("barangay_id")){
                    open_transaction_name.setText("Barangay ID");
                }else if(value.getString("type").equals("barangay_clearance")){
                    open_transaction_name.setText("Barangay Clearance");
                }else if(value.getString("type").equals("barangay_certificate")){
                    open_transaction_name.setText("Barangay Certificate");
                }

                transaction_ID.setText(pass_ID);

                if(value.getString("status").equals("Pending")){
                    GenerateBarCode();
                    status_transaction.setBackgroundResource(R.drawable.transaction_1);
                }else if(value.getString("status").equals("In-Progress")){
                    GenerateBarCode();
                    status_transaction.setBackgroundResource(R.drawable.transaction_2);
                }else if(value.getString("status").equals("Pickup")){
                    status_transaction.setBackgroundResource(R.drawable.transaction_3);
                    GenerateBarCode();
                }else if(value.getString("status").equals("Completed")){
                    status_transaction.setBackgroundResource(R.drawable.transaction_4);
                }else{
                    status_transaction.setBackgroundResource(R.drawable.transaction_failed);
                }

                date_requested.setText(value.getString("date_request"));
                time_requested.setText(value.getString("time_request"));
                copies.setText(value.getString("copies"));
                price.setText(value.getString("price"));
                purpose_requested.setText(value.getString("purpose"));
                date_completed.setText(value.getString("date_complete"));
                time_completed.setText(value.getString("time_complete"));


                //DELETE
                yes_request.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(value.getString("status").equals("Pending")){
                            Map<String, Object> update_user_transaction = new HashMap<>();
                            update_user_transaction.put(value.getString("type"),false);
                            ProfileVerificationChecker.update(update_user_transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            });
                            TransactionRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            });
                        }else{
                            delete_transaction.dismiss();
                            Toast.makeText(OpenTransaction.this, "You cannot cancel a request that is not in Pending", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                no_request.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delete_transaction.dismiss();
                    }
                });





                    }catch (Exception e){
                        Toast.makeText(OpenTransaction.this, "Request Deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    }
                }


            });
        }catch (Exception e){
            Toast.makeText(OpenTransaction.this, "Request Deleted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }




    }
    public void GenerateBarCode(){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(pass_ID, BarcodeFormat.CODE_128, 300, 150);
            Bitmap bitmap = Bitmap.createBitmap(300, 150,Bitmap.Config.RGB_565);
            for (int i = 0; i < 300; i++){
                for (int j = 0; j < 150; j++){
                    bitmap.setPixel(i,j,bitMatrix.get(i,j)? Color.BLACK:Color.WHITE);
                }
            }
            transaction_barcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }
    public void Go_Back(View view){
        startActivity(new Intent(getApplicationContext(),Profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void Open_Setting_Transaction(View view){
        transactionSetting.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transactionSetting.dismiss();
                delete_transaction.show();
            }
        });

    }
}