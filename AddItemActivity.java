package hu.ait.missbeauty;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Calendar;
import java.util.UUID;

import hu.ait.missbeauty.data.Product;
import hu.ait.missbeauty.data.User;

import static java.lang.Boolean.TRUE;


public class AddItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static final int REQUEST_CODE_TAKE_PHOTO = 101;
    private static final String TAG = "AddItemActivity";
    static final String KEY_ITEM = "KEY_ITEM";

    private Calendar calendar;
    private int year, month, day;
    private EditText etName;
    private TextView expDate;
    private TextView opnDate;
    private EditText etMemo;
    private Switch openstatusSwtich;
    private Spinner spinnerExpDate;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private String openExpDate;
    private ImageButton btnAttach;
    private ImageView imageView;
    private int isExpired;
    private String key;
    private Button btnDelete;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        key = "";

        setupUI();
        setupCalendar();

        if (getIntent().getSerializableExtra(KEY_ITEM) != null){

            initEdit();
        }

    }

    private void initEdit() {

        key = getIntent().getStringExtra(KEY_ITEM);
        etName.setText(getIntent().getStringExtra("name"));
        System.out.println("name is " + getIntent().getStringExtra("name"));
        expDate.setText(getIntent().getStringExtra("expDate"));
        opnDate.setText(getIntent().getStringExtra("opnDate"));
        etMemo.setText(getIntent().getStringExtra("memo"));
        imageURL = getIntent().getStringExtra("imageURL");
        if (getIntent().getBooleanExtra("isOpen", false)){
            openstatusSwtich.setChecked(true);
        }
        btnDelete.setVisibility(View.VISIBLE);}

    private void setupUI(){

        spinnerExpDate = (Spinner) findViewById(R.id.spinnerAutoDate);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.itemOpenDate_array, R.layout.support_simple_spinner_dropdown_item);
        spinnerExpDate.setPrompt("Your choice");
        spinnerExpDate.setAdapter(adapter);
        spinnerExpDate.setOnItemSelectedListener(this);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        etName = (EditText) findViewById(R.id.etName);
        expDate = (TextView) findViewById(R.id.tvExpDate);
        opnDate = (TextView) findViewById(R.id.tvOpnDate);
        calendar = Calendar.getInstance();
        etMemo = (EditText) findViewById(R.id.etMemo);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });
        openstatusSwtich = (Switch) findViewById(R.id.openSwitch);
        btnAttach = (ImageButton) findViewById(R.id.btnAttach);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intentTakePhoto, REQUEST_CODE_TAKE_PHOTO);
            }
        });

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getVisibility() == View.GONE) {
                    saveItem();
                } else {
                    try {
                        uploadPostWithImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            Bitmap img = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(img);
            imageView.setVisibility(View.VISIBLE);
        }
    }


    public void uploadPostWithImage() throws Exception {

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
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
                Toast.makeText(AddItemActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                saveItem(taskSnapshot.getDownloadUrl().toString());
            }
        });
    }

    private void setupCalendar(){
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showExpDate(year, month, day);
        showOpnDate(year, month, day);
    }


    @SuppressWarnings("deprecation")
    public void setExpDate(View view){
        showDialog(999);
        Toast.makeText(getApplicationContext(), "set expiration date", Toast.LENGTH_SHORT).show();
    }

    public void setOpnDate(View view){

        showDialog(998);
        Toast.makeText(getApplicationContext(), "set opened date", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Dialog onCreateDialog(int id){
        if (id == 999){

            return new DatePickerDialog(this, expDateListener, year, month, day);
        }

        else if (id == 998){

            return new DatePickerDialog(this, opnDateListener, year, month, day);
        }

        return null;
    }

    private DatePickerDialog.OnDateSetListener expDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    showExpDate(year, month+1, dayOfMonth);
                }
            };

    private DatePickerDialog.OnDateSetListener opnDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    showOpnDate(year, month+1, dayOfMonth);
                }
            };

    private void showExpDate(int year, int month, int day){

        expDate.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
    }

    private void showOpnDate(int year, int month, int day){

        opnDate.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
    }

    private void deleteItem(){

        final String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);

                        if (user == null){

                            Log.e(TAG, "User " + userId + "is unexpectedly null");
                            Toast.makeText(AddItemActivity.this,
                                    "ERROR: could not fetch user "+ userId,
                                    Toast.LENGTH_SHORT).show();
                        } else{

                            String key = getIntent().getStringExtra(KEY_ITEM);

                            deleteNewProduct(userId, etName.getText().toString(), expDate.getText().toString(),
                                    opnDate.getText().toString(), openstatusSwtich.isChecked(), etMemo.getText().toString(), 2, openExpDate, key);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        finish();


    }

    private void saveItem(final String ... imageURL){

        final String name = etName.getText().toString();
        final String exp = expDate.getText().toString();
        final String opn = opnDate.getText().toString();

        if (TextUtils.isEmpty(name)){

            etName.setError("please type name");
            return;
        }

        if (!validExp(exp, opn)){

            expDate.setError("please check expiration data again");
            return;
        }

        final String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);

                        if (user == null){

                            Log.e(TAG, "User " + userId + "is unexpectedly null");
                            Toast.makeText(AddItemActivity.this,
                                    "ERROR: could not fetch user "+ userId,
                                    Toast.LENGTH_SHORT).show();
                        } else{

                            if (key.equals("")) {

                                addNewProduct(userId, etName.getText().toString(), expDate.getText().toString(),
                                        opnDate.getText().toString(), openstatusSwtich.isChecked(), etMemo.getText().toString(), isExpired, openExpDate, imageURL);
                            }

                            else{
                                    updateNewProduct(userId, etName.getText().toString(), expDate.getText().toString(),
                                        opnDate.getText().toString(), openstatusSwtich.isChecked(), etMemo.getText().toString(), isExpired, openExpDate, key, imageURL);


                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                }
        );

        finish();
    }

    //check if the open date is valid
    private boolean validExp(String exp, String opn){

        return true;

    }


    private void addNewProduct(String userId, String productName, String expDate,
                               String opnDate, boolean openStatus, String memo, int isExpired, String spinnerAutoDate, String...imageUrl){

        //START single_value_read
        String key = mDatabase.child("users").child(userId).child("products").push().getKey();

        Product newProduct = new Product(

                userId, productName, expDate, opnDate, openStatus, memo, isExpired, spinnerAutoDate);

        if(imageUrl!=null && imageUrl.length>0){
            newProduct.setImageURL(imageUrl[0]);
        }
        isExpired = newProduct.getIsExpired();
        mDatabase.child("users").child(userId).child("products").child(key).setValue(newProduct);

        startActivity(new Intent(AddItemActivity.this, MainActivity.class));

    }

    private void updateNewProduct(String userId, String productName, String expDate,
                                  String opnDate, boolean openStatus, String memo, int isExpired, String spinnerAutoDate, String key, String ...imageUrl){


        Product newProduct = new Product(

                userId, productName, expDate, opnDate, openStatus, memo, isExpired, spinnerAutoDate);


        if(imageUrl!=null && imageUrl.length>0){
            newProduct.setImageURL(imageUrl[0]);
        }
        isExpired = newProduct.getIsExpired();

        mDatabase.child("users").child(userId).child("products").child(key).setValue(newProduct);

        startActivity(new Intent(AddItemActivity.this, MainActivity.class));


    }


    private void deleteNewProduct(String userId, String productName, String expDate,
                               String opnDate, boolean openStatus, String memo, int isExpired, String spinnerAutoDate, String key){

        Product newProduct = new Product(

                userId, productName, expDate, opnDate, openStatus, memo, isExpired, spinnerAutoDate);


        mDatabase.child("users").child(userId).child("products").child(key).setValue(newProduct);
        startActivity(new Intent(AddItemActivity.this, MainActivity.class));

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        openExpDate =parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }




}
