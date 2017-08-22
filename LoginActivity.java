package hu.ait.missbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.ait.missbeauty.data.User;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private EditText etEmail;
    private EditText etPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Button btnLognin = (Button) findViewById(R.id.btnLogin);
        btnLognin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity();
            }
        });

        Button btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupActivity();
            }
        });
        etEmail= (EditText) findViewById(R.id.email);
        etPassword= (EditText) findViewById(R.id.password);
    }

    public void loginActivity(){
        if (!isFormValid()) {
            return;
        }
        showProgreeDialog();
        firebaseAuth.signInWithEmailAndPassword(etEmail.getText().toString(),
                etPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            // go response from server, doesnt mean login is sucessful
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this,
                            "Login ok",
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }else{
                    Toast.makeText(LoginActivity.this,
                            "Failed: "+task.getException().getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signupActivity(){
        if(!isFormValid()){
            return;
        }

        showProgreeDialog();
        firebaseAuth.createUserWithEmailAndPassword(
                etEmail.getText().toString(),etPassword.getText().toString()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    firebaseUser.updateProfile(
                            new UserProfileChangeRequest.Builder().
                                    setDisplayName(
                                            userNameFromEmail(
                                                    firebaseUser.getEmail())).build()
                    );

                    writeNewUser(firebaseUser.getUid(), userNameFromEmail(firebaseUser.getEmail()), firebaseUser.getEmail());

                    Toast.makeText(LoginActivity.this, "Successfully signed up",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed: "+
                                    task.getException().getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                Toast.makeText(LoginActivity.this,
                        "error: "+e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean isFormValid() {
        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError("This should not be empty");
            return false;
        }

        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError("This should not be empty");
            return false;
        }

        return true;
    }

    private String userNameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void writeNewUser(String uid, String userName, String email){

        User user = new User(userName, email);

        mDatabase.child("users").child(uid).setValue(user);

    }
}




