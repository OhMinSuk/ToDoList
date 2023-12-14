package com.example.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.recaptcha.Recaptcha;
import com.google.android.recaptcha.RecaptchaAction;
import com.google.android.recaptcha.RecaptchaTasksClient;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // 리캡챠 사이트 키
    private static final String SITE_KEY = "6LcsvGAmAAAAACCMeNO3EBfaBTslakrEahoVdYwM";

    private Button login_btnLogin, login_btnSignUp;
    private FirebaseAuth mAuth;

    @Nullable
    private RecaptchaTasksClient recaptchaTasksClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        initializeRecaptchaClient();

        login_btnLogin = findViewById(R.id.btnLogin);
        login_btnSignUp = findViewById(R.id.btnSignUp);

        login_btnLogin.setOnClickListener(this::executeLoginAction);
        login_btnSignUp.setOnClickListener(this::signUp);
    }

    @Override()
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    // 리캡챠 클라이언트 초기화
    private void initializeRecaptchaClient() {
        Recaptcha
                .getTasksClient(getApplication(), SITE_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<RecaptchaTasksClient>() {
                    @Override
                    public void onSuccess(RecaptchaTasksClient client) {
                        LoginActivity.this.recaptchaTasksClient = client;
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getStackTrace().toString());
                    }
                });
    }

    // 리캡챠 작업을 수행한 후 로그인 진행
    private void executeLoginAction(View v) {
        assert recaptchaTasksClient != null;
        recaptchaTasksClient
                .executeTask(RecaptchaAction.LOGIN)
                .addOnSuccessListener(this, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        login();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "recaptcha Failure");
                        Log.e(TAG, e.getStackTrace().toString());
                    }
                });
    }

    // 로그인
    private void login() {
        EditText login_inputEmail = findViewById(R.id.inputEmail);
        EditText login_inputPassword = findViewById(R.id.inputPassword);

        String email = login_inputEmail.getText().toString();
        String password = login_inputPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmailAndPassword:success");

                        Toast.makeText(LoginActivity.this, "환영합니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());

                        if (task.getException().toString().contains("FirebaseAuthInvalidCredentialsException")) {
                            Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호를 잘못 입력했습니다.\r\n입력하신 내용을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }

                        if (task.getException().toString().contains("FirebaseAuthInvalidUserException")) {
                            Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호를 잘못 입력했습니다.\r\n입력하신 내용을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    // 회원 가입 화면으로 이동
    private void signUp(View v) {
        Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(signUpIntent);
    }
}
