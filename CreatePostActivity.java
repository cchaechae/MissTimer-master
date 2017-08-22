package hu.ait.missbeauty;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.ait.missbeauty.data.Post;
import hu.ait.missbeauty.data.User;

public class CreatePostActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_TAKE_PHOTO = 101;
    private static final String TAG = "CreatePostActivity";
    private static final String REQUIRED = "Required";
    Button btnAttach;

    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etCaption)
    EditText etCaption;
    @BindView(R.id.imgAttach)
    ImageView imgAttach;

    private DatabaseReference mDatabase;

    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        btnAttach = (Button) findViewById(R.id.btnAttach);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachClick();
            }
        });

        ButterKnife.bind(this);
    }

    public void attachClick() {
        Intent intentTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentTakePhoto, REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            Bitmap img = (Bitmap) data.getExtras().get("data");
            imgAttach.setImageBitmap(img);
            imgAttach.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btnSend)
    public void sendClick() {
        if (imgAttach.getVisibility() == View.GONE) {
            uploadPost();
        } else {
            try {
                uploadPostWithImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPost(final String... imageUrl) {

        final String userId = currentUser.getUid();
        final String name = etName.getText().toString();
        final String caption = etCaption.getText().toString();

        if (TextUtils.isEmpty(name)){
            etName.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(caption)){
            etCaption.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null){
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(CreatePostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else{

                            //write new post
                            writeNewPost(userId, user.getUsername(), name, caption, imageUrl);
                        }

                        setEditingEnabled(true);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                }
        );

    }

    private void writeNewPost(String userId, String userName, String name, String caption, String ...imageUrl){

        String key = mDatabase.child("posts").push().getKey();
        Post newPost = new Post(
                userId,
                userName,
                name,
                caption
        );

        if (imageUrl != null && imageUrl.length>0) {
            newPost.setImageUrl(imageUrl[0]);
        }

        Map<String, Object> postValues = newPost.toMap();

        Map<String, Object> childUpdates = new HashMap<>(); //Map for posts
        childUpdates.put("/posts/"+key, postValues); //corresponding to all posts
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues); //corresponding to users

        mDatabase.updateChildren(childUpdates);

        finish();
    }


    public void uploadPostWithImage() throws Exception {
        imgAttach.setDrawingCacheEnabled(true);
        imgAttach.buildDrawingCache();
        Bitmap bitmap = imgAttach.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageInBytes = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8")+".jpg";
        StorageReference newImageRef = storageRef.child(newImage);
        StorageReference newImageImagesRef = storageRef.child("images/"+newImage);
        newImageRef.getName().equals(newImageImagesRef.getName());    // true
        newImageRef.getPath().equals(newImageImagesRef.getPath());    // false

        UploadTask uploadTask = newImageImagesRef.putBytes(imageInBytes);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(CreatePostActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                uploadPost(taskSnapshot.getDownloadUrl().toString());
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FirebaseAuth.getInstance().signOut();
            super.onBackPressed();
        }
    }

    //prevents from creating multiple posts
    private void setEditingEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etCaption.setEnabled(enabled);
        if (enabled) {
            btnAttach.setVisibility(View.VISIBLE);
        } else {
            btnAttach.setVisibility(View.GONE);
        };
    }

}
