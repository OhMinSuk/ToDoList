package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.recaptcha.Recaptcha;
import com.google.android.recaptcha.RecaptchaAction;
import com.google.android.recaptcha.RecaptchaTasksClient;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    // 리캡챠 사이트 키
    private static final String SITE_KEY = "6LcsvGAmAAAAACCMeNO3EBfaBTslakrEahoVdYwM";

    private FirebaseAuth mAuth;

    @Nullable
    private RecaptchaTasksClient recaptchaTasksClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 리캡챠 초기화
        initializeRecaptchaClient();

        // 파이어베이스 계정 초기화
        mAuth = FirebaseAuth.getInstance();

        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this::executeSignUpAction);
    }

    @Override()
    public void onStart() {
        super.onStart();

    }

    // 리캡챠 클라이언트 초기화
    private void initializeRecaptchaClient() {
        Recaptcha
                .getTasksClient(getApplication(), SITE_KEY)
                .addOnSuccessListener(this, new OnSuccessListener<RecaptchaTasksClient>() {
                    @Override
                    public void onSuccess(RecaptchaTasksClient client) {
                        SignUpActivity.this.recaptchaTasksClient = client;
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getStackTrace().toString());
                    }
                });
    }

    // 리캡챠 작업을 수행한 후 회원가입 진행
    private void executeSignUpAction(View v) {
        assert recaptchaTasksClient != null;
        recaptchaTasksClient
                .executeTask(RecaptchaAction.SIGNUP)
                .addOnSuccessListener(this, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        signUp();
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

    // 회원가입
    private void signUp() {
        EditText inputEmail = findViewById(R.id.inputEmail);
        String email = inputEmail.getText().toString();

        EditText inputPassword = findViewById(R.id.inputPassword);
        String password = inputPassword.getText().toString();

        EditText inputPasswordCheck = findViewById(R.id.inputPasswordCheck);
        String passwordCheck = inputPasswordCheck.getText().toString();

        // 이메일, 비밀번호, 비밀번호 확인 란이 빈 값인지 확인
        if (email.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            // 비밀번호와 비밀번호 확인
            // 결과 값이 0이면 두개의 문자열이 동일하다는 의미
            if (password.compareTo(passwordCheck) == 0) {
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmailAndPassword:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);

                            Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());

                            if (task.getException().toString().contains("FirebaseAuthInvalidCredentialsException")) {
                                Toast.makeText(SignUpActivity.this, "아이디를 이메일 형식으로 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }

                            if (task.getException().toString().contains("FirebaseAuthWeakPasswordException")) {
                                Toast.makeText(SignUpActivity.this, "비밀번호를 6자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }

                            if (task.getException().toString().contains("FirebaseAuthUserCollisionException")) {
                                Toast.makeText(SignUpActivity.this, "이미 존재하는 이메일이 있습니다.\r\n다른 이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(SignUpActivity.this, "비밀번호를 서로 일치하게 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}