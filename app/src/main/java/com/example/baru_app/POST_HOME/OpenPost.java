package com.example.baru_app.POST_HOME;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class OpenPost extends AppCompatActivity {
    FirebaseFirestore firestoreDB;
    DocumentReference PostListRef,UserRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay,pass_ID;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;

    TextView Post_Title,Post_Date,Post_Description,Post_NumComment,Post_Time,Post_Author;
    ImageView Post_Profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_post_activity);

        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(OpenPost.this);
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();

        Post_Author = findViewById(R.id.tvPostAuthor);
        Post_Title = findViewById(R.id.tv_postTitle);
        Post_Date = findViewById(R.id. tvPostDate);
        Post_Time = findViewById(R.id. tvPostTime);
        Post_Description = findViewById(R.id. tvPostContent);
        Post_Profile = findViewById(R.id. profileImage);
        pass_ID = getIntent().getStringExtra("pass_ID");


        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("post").document(pass_ID);
        PostListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable  DocumentSnapshot value_post, @Nullable  FirebaseFirestoreException error) {
                        String Author_ID = value_post.getString("author_id");
                        UserRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(Author_ID);
                        UserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable  DocumentSnapshot value, @Nullable  FirebaseFirestoreException error) {
                                if(value.getBoolean("admin") == false){
                                    Post_Title.setText(value.getString("firstName") +" " + value.getString("lastName") );
                                    Post_Author.setText("Brgy. "+value.getString("barangay"));
                                    Post_Date.setText(value_post.getString("date"));
                                    Post_Time.setText(value_post.getString("time"));
                                    //USER PIC
                                    Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  userID + "/user_profile");
                                    Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(Post_Profile);
                                        }
                                    });
                                }else{
                                    Post_Title.setText(value_post.getString("title"));
                                    Post_Author.setText("Brgy. "+value.getString("barangay"));
                                    Post_Description.setText(value_post.getString("description"));
                                    Post_Date.setText(value_post.getString("date"));
                                    Post_Time.setText(value_post.getString("time"));
                                    BarangayProfile = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
                                    BarangayProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Picasso.get().load(uri).into(Post_Profile);
                                        }
                                    });
                                }

                            }
                        });

            }
        });



    }
}