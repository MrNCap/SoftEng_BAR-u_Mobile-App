package com.example.baru_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baru_app.AUTHENTICATION.Login;
import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.HISTORY_TRANSACTION.History_Adapter;
import com.example.baru_app.HISTORY_TRANSACTION.Histoy_Model;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[~!@#$%^&*()_+{}|;''.,/?])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 8 characters
                    "$");
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDB;
    DocumentReference ProfileUserRef;
    FirebaseStorage firebaseStorage;
    String userID,sql_return_barangay;
    DatabaseHelper databasehelper;
    TextView userEmail,userName,userContactNo;
    Dialog userSettings_menu,changepassword_user,edit_profile_user,loading_layout;
    Button update_profile_btn,update_pass_btn;
    EditText edit_fn,edit_mn,edit_ln,edit_cont,currentpass,newpass,newpass_con;
    ConstraintLayout     linearLayoutCompat;
    FirebaseUser user,userAuth,userEmailCheck;
    CircleImageView profile;
    StorageReference Address_storageRef;
    CollectionReference transactionList_ref;
    History_Adapter History_Adapter_transaction,History_Adapter_history;
    Switch aswitch;
    RecyclerView post_View;
    Uri selectedPic;
    Query query_onGoing;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ///FIREBASE
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        user =firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getCurrentUser().getUid();
        userEmailCheck = firebaseAuth.getCurrentUser();
        userAuth = FirebaseAuth.getInstance().getCurrentUser();

        //DB SQL
        databasehelper = new DatabaseHelper(Profile.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();
        ProfileUserRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(userID);
        transactionList_ref = firestoreDB.collection("barangays").document(sql_return_barangay).collection("requests");

        //WIDGETS
        userEmail = findViewById(R.id.user_email);
        userContactNo = findViewById(R.id.user_contactno);
        userName = findViewById(R.id.user_name);
        linearLayoutCompat = findViewById(R.id.linearLayoutCompat);
        profile = findViewById(R.id.home_brgy_profile);
        aswitch = findViewById(R.id.switch1);

        Query query = transactionList_ref.whereEqualTo("author_id",userID).whereNotEqualTo("status","Completed");
        FirestoreRecyclerOptions<Histoy_Model> History_Option_Current = new FirestoreRecyclerOptions.Builder<Histoy_Model>()
                .setQuery(query, Histoy_Model.class)
                .build();
        History_Adapter_transaction = new History_Adapter(History_Option_Current);

        Query query_history = transactionList_ref.whereEqualTo("author_id",userID).whereEqualTo("status","Completed").orderBy("timestamp", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Histoy_Model> History_Option_History = new FirestoreRecyclerOptions.Builder<Histoy_Model>()
                .setQuery(query_history, Histoy_Model.class)
                .build();
        History_Adapter_history = new History_Adapter(History_Option_History);



        //LOADING DIALOG
        loading_layout = new Dialog(this);
        loading_layout.setContentView(R.layout.loading_layout);
        loading_layout.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading_layout.setCanceledOnTouchOutside(false);
        loading_layout.setCancelable(false);


        //SETTINGS DIALOG
        userSettings_menu = new Dialog(this);
        userSettings_menu.setContentView(R.layout.settings_popout);
        userSettings_menu.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //EDIT PROFILE
        edit_profile_user = new Dialog(this);
        edit_profile_user.setContentView(R.layout.edit_pro);
        edit_profile_user.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        edit_fn = edit_profile_user.findViewById(R.id.edit_fn);
        edit_mn = edit_profile_user.findViewById(R.id.edit_mn);
        edit_ln = edit_profile_user.findViewById(R.id.edit_ln);
        edit_cont = edit_profile_user.findViewById(R.id.edit_cont);
        update_profile_btn = edit_profile_user.findViewById(R.id.update_profile_btn);

        //CHANGE PASSWORD
        changepassword_user = new Dialog(this);
        changepassword_user.setContentView(R.layout.edit_password);
        changepassword_user.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        currentpass = changepassword_user.findViewById(R.id.currentpass);
        newpass = changepassword_user.findViewById(R.id.newpass);
        newpass_con= changepassword_user.findViewById(R.id.newpass_con);
        update_pass_btn = changepassword_user.findViewById(R.id.update_pass_btn);


        //USER DATA

        Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  userID + "/user_profile");
        Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile);
            }
        });



            ProfileUserRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    userName.setText(value.getString("firstName")+" "+ value.get("lastName"));
                    userEmail.setText(value.getString("email"));
                    userContactNo.setText(value.getString("number"));

                }
            });



        transaction_list();
        aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked == true){
                    //SHOW HISTORY
                    aswitch.setText("History");
                    history_list();

                }else{
                    //CURRENT TRANSACTIONS
                    aswitch.setText("Active");
                    transaction_list();
                }
            }
        });

                    History_Adapter_transaction.setOnItemClickListner(new History_Adapter.OnItemClicklistener() {
                        @Override
                        public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                            String id = documentSnapshot.getId();
                            Intent OpenPost = new Intent(Profile.this, com.example.baru_app.OpenTransaction.class);
                            OpenPost.putExtra("pass_ID",id);
                            startActivity(OpenPost);
                        }
                    });

                    History_Adapter_history.setOnItemClickListner(new History_Adapter.OnItemClicklistener() {
                        @Override
                        public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                            String id = documentSnapshot.getId();
                            Intent OpenPost = new Intent(Profile.this, com.example.baru_app.OpenTransaction.class);
                            OpenPost.putExtra("pass_ID",id);
                            startActivity(OpenPost);
                        }
                    });


    }
    public void transaction_list(){
        post_View = findViewById(R.id.transaction_recycleView);
        post_View.setHasFixedSize(true);
        post_View.setLayoutManager(new LinearLayoutManager(Profile.this));
        post_View.setAdapter(History_Adapter_transaction);
    }

    public void history_list(){
        post_View = findViewById(R.id.transaction_recycleView);
        post_View.setHasFixedSize(true);
        post_View.setLayoutManager(new LinearLayoutManager(Profile.this));
        post_View.setAdapter(History_Adapter_history);
    }

    @Override
    public void onStart() {
        super.onStart();
        History_Adapter_transaction.startListening();
        History_Adapter_history.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        History_Adapter_transaction.stopListening();
        History_Adapter_history.stopListening();
    }


    public void OpenUserSettings(View view){
        userSettings_menu.show();

    }
    public void ChangePassword(View view){
        userSettings_menu.dismiss();
        changepassword_user.show();
        update_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(currentpass.getText().toString().trim())) {
                    currentpass.setError("Current Password is Required");
                    return;
                }
                if (TextUtils.isEmpty(newpass.getText().toString().trim())) {
                    newpass.setError("New Password is Required");
                    return;
                }
                if (TextUtils.isEmpty(newpass_con.getText().toString().trim())) {
                    newpass_con.setError("New Password Confirmation is Required");
                    return;
                }
                if(!PASSWORD_PATTERN.matcher(newpass.getText().toString().trim()).matches()){
                    newpass.setError("Password must have at least 6 characters, Should contain a number, symbol, lower case and upper case letters");
                    return;
                }
                if(!newpass.getText().toString().equals(newpass_con.getText().toString())){
                    newpass_con.setError("Password entered is not the same.");
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(userEmailCheck.getEmail(), currentpass.getText().toString());
                userAuth.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        update_pass_btn.setEnabled(false);
                        user.updatePassword(newpass.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Snackbar snackbar = Snackbar.make(linearLayoutCompat, "Password Updated", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                                newpass.setText("");
                                newpass_con.setText("");
                                currentpass.setText("");
                                changepassword_user.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar snackbar = Snackbar.make(linearLayoutCompat, "Failed, Try again", Snackbar.LENGTH_SHORT);
                                Toast.makeText(Profile.this, e.toString(), Toast.LENGTH_SHORT).show();
                                snackbar.show();
                                newpass.setText("");
                                newpass_con.setText("");
                                currentpass.setText("");
                                changepassword_user.dismiss();
                            }
                        });
                    }
                });
            }
        });

    }
    public void EditProfile(View view){
        Map<String, Object> userdata = new HashMap<>();
        userSettings_menu.dismiss();
        update_profile_btn.setEnabled(true);
        ProfileUserRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                edit_fn.setText(value.getString("firstName"));
                edit_mn.setText(value.getString("middleName"));
                edit_ln.setText(value.getString("lastName"));
                edit_cont.setText(value.getString("number"));

            }
        });
        edit_profile_user.show();
        update_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edit_fn.getText().toString().trim())) {
                    edit_fn.setError("First Name is Required");
                    return;
                }
                if (TextUtils.isEmpty(edit_mn.getText().toString().trim())) {
                    edit_mn.setError("Last Name is Required");
                    return;
                }
                if (TextUtils.isEmpty(edit_ln.getText().toString().trim())) {
                    edit_ln.setError("Last Name is Required");
                    return;
                }
                if (TextUtils.isEmpty(edit_cont.getText().toString().trim())) {
                    edit_cont.setError("Last Name is Required");
                    return;
                }
                userdata.put("firstName", edit_fn.getText().toString().trim());
                userdata.put("middleName", edit_mn.getText().toString().trim());
                userdata.put("lastName", edit_ln.getText().toString().trim());
                userdata.put("number", edit_cont.getText().toString().trim());

                ProfileUserRef.update(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar snackbar = Snackbar.make(linearLayoutCompat, "Profile Updated", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        edit_profile_user.dismiss();
                        update_profile_btn.setEnabled(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar snackbar = Snackbar.make(linearLayoutCompat, "Failed, Try again", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        edit_profile_user.dismiss();
                    }
                });
            }

        });
    }
    public void LogOutUser(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Login.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }
    public void ChangeProfile(View view){
        attach_profile_pic.launch("image/*");
        UploadProfile(selectedPic);

    }
    public void UploadProfile(Uri selectedPic){
        if(selectedPic != null){

            StorageReference profile_ref = firebaseStorage.getReference().child("PROFILE/users/" +  userID + "/user_profile");
            profile_ref.putFile(selectedPic).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        profile.setImageURI(selectedPic);
                        loading_layout.dismiss();
                        Snackbar snackbar = Snackbar.make(linearLayoutCompat, "Profile Updated", Snackbar.LENGTH_SHORT);
                    }else {
                        loading_layout.dismiss();
                        Toast.makeText(Profile.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull  UploadTask.TaskSnapshot snapshot) {
                    loading_layout.show();
                }
            });
        }
    }
    public void GoTo_Home(View view){
        startActivity(new Intent(getApplicationContext(),HomePage.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }
    public void GoTo_Services(View view){
        startActivity(new Intent(getApplicationContext(),Services.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }

    ActivityResultLauncher<String> attach_profile_pic = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        selectedPic = result;
                    }

                }
            });
}