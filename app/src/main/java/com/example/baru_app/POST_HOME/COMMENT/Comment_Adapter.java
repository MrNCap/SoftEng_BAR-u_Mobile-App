package com.example.baru_app.POST_HOME.COMMENT;

import android.app.Dialog;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.baru_app.DATABASE_SQL.DatabaseHelper;
import com.example.baru_app.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
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

public class Comment_Adapter extends FirestoreRecyclerAdapter<Comment_Model, Comment_Adapter.PostHolder> {
    FirebaseFirestore firestoreDB;
    DocumentReference PostListRef;
    FirebaseAuth firebaseAuth;
    String userID,sql_return_barangay;
    StorageReference Address_storageRef,BarangayProfile;
    FirebaseStorage firebaseStorage;
    DatabaseHelper databasehelper;
    OnLongClickListener listener_long;
    Dialog openCommentSetting,editComment,deleteComment;
    Button edit_comment_setting,delete_comment_setting,update_comment,delete_comment,cancel_delete_comment;
    TextView editAuthor,editDate,editTime;
    EditText editComment_user;
    String comment_id,post_id;
    DocumentReference CommentListRef;

    public Comment_Adapter(@NonNull FirestoreRecyclerOptions<Comment_Model> options) {
        super(options);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }




    @Override
    protected void onBindViewHolder(@NonNull PostHolder holder, int position, @NonNull Comment_Model model) {
        firebaseStorage = FirebaseStorage.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        //DB SQL
        databasehelper = new DatabaseHelper(holder.itemView.getContext());
        sql_return_barangay = databasehelper.getBarangayCurrentUser(userID);
        databasehelper.close();

        post_id = model.getPostID();
        comment_id = model.getDocID();


        PostListRef = firestoreDB.collection("barangays").document(sql_return_barangay).collection("users").document(model.getAuthor_id());
        PostListRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                holder.User.setVisibility(View.GONE);
                holder.Other.setVisibility(View.GONE);

                if(model.getAuthor_id().equals(userID)){
                    holder.User.setVisibility(View.VISIBLE);
                    if(value.getBoolean("admin") == false){
                        holder.User_Comment_Author.setText(value.getString("firstName") +" " + value.getString("lastName"));
                        holder.User_Comment_Content.setText(model.getComment());
                        holder.User_Comment_Date.setText(model.getDate());
                        holder.User_Comment_Time.setText(model.getTime());
                        //USER PIC
                        Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  model.getAuthor_id() + "/user_profile");
                        Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.User_Comment_Profile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                    }else{

                        holder.User_Comment_Author.setText("Brgy. "+value.getString("barangay"));
                        holder.User_Comment_Content.setText(model.getComment());
                        holder.User_Comment_Date.setText(model.getDate());
                        holder.User_Comment_Time.setText(model.getTime());
                        BarangayProfile = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
                        BarangayProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.User_Comment_Profile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                }else{
                    holder.Other.setVisibility(View.VISIBLE);
                    if(value.getBoolean("admin") == false){
                        holder.Other_Comment_Author.setText(value.getString("firstName") +" " + value.getString("lastName"));
                        holder.Other_Comment_Content.setText(model.getComment());
                        holder.Other_Comment_Date.setText(model.getDate());
                        holder.Other_Comment_Time.setText(model.getTime());
                        //USER PIC
                        Address_storageRef = firebaseStorage.getReference().child("PROFILE/users/" +  model.getAuthor_id() + "/user_profile");
                        Address_storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.Other_Comment_Profile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                    }else{
                        holder.Other_Comment_Author.setText("Brgy. "+value.getString("barangay"));
                        holder.Other_Comment_Content.setText(model.getComment());
                        holder.Other_Comment_Date.setText(model.getDate());
                        holder.Other_Comment_Time.setText(model.getTime());
                        BarangayProfile = firebaseStorage.getReference().child("BARANGAY LOGO/"+sql_return_barangay+"/profile_brgy.jpg");
                        BarangayProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(holder.Other_Comment_Profile);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }

                }




            }
        });



    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_all,parent, false);
        return new PostHolder(view);
    }

    class PostHolder extends RecyclerView.ViewHolder{
        TextView User_Comment_Author,User_Comment_Date,User_Comment_Content,User_Comment_Time;
        ImageView User_Comment_Profile;
        TextView Other_Comment_Author,Other_Comment_Date,Other_Comment_Content,Other_Comment_Time;
        ImageView Other_Comment_Profile;
        LinearLayout User,Other;
        public PostHolder(@NonNull View itemView) {
            super(itemView);

            User = itemView.findViewById(R.id.currentUserLayout);
            User_Comment_Author = itemView.findViewById(R.id.user_CommentAuthor);
            User_Comment_Date = itemView.findViewById(R.id. user_CommentDate);
            User_Comment_Time = itemView.findViewById(R.id. user_CommentTime);
            User_Comment_Content = itemView.findViewById(R.id. user_CommentContent);
            User_Comment_Profile = itemView.findViewById(R.id.home_brgy_profile);

            Other = itemView.findViewById(R.id.otherUserLayout);
            Other_Comment_Author = itemView.findViewById(R.id.other_CommentAuthor);
            Other_Comment_Date = itemView.findViewById(R.id. other_CommentDate);
            Other_Comment_Time = itemView.findViewById(R.id. other_CommentTime);
            Other_Comment_Content = itemView.findViewById(R.id. other_CommentContent);
            Other_Comment_Profile = itemView.findViewById(R.id.other_profileImage);



            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener_long != null){
                        listener_long.onItemClick(getSnapshots().getSnapshot(position),position);
                    }
                    return false;
                }
            });

        }

    }
    public interface OnLongClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }
    public void setOnLongClickListener(OnLongClickListener listener_long){
        this.listener_long = listener_long;
    }
}
