package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String userNo;

    ArrayList<String> toDoList;
    ArrayAdapter<String> adapter;
    ArrayList<String> itemKeys;
    ListView listView;
    EditText inputAddText;

    FirebaseDatabase myDB;
    DatabaseReference myDB_ref = null;

    HashMap<String, Object> TodoList_Value = null;

    String strHeader = "TodoList Information";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        myDB = FirebaseDatabase.getInstance();
        myDB_ref = myDB.getReference();

        TodoList_Value = new HashMap<>();

        listView = findViewById(R.id.listView);
        inputAddText = findViewById(R.id.inputAddText);

        itemKeys = new ArrayList<>();

        // 할일 추가 버튼 이벤트
        Button btnAddList = findViewById(R.id.btnAddList);
        btnAddList.setOnClickListener(this::addItemToList);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        userNo = currentUser.getUid();

        getData();
    }

    // 할일 추가
    private void addItemToList(View v) {
        String todo = inputAddText.getText().toString();

        if (todo.isEmpty()) {
            Toast.makeText(MainActivity.this, "할 일을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            TodoList_Value.put("todo", todo);
            TodoList_Value.put("checked", false);

            setData(true);
            getData();

            inputAddText.setText("");
        }
    }

    private void setData(boolean bFlag) {
        if (bFlag) {
            myDB_ref.child(strHeader).child(userNo).push().setValue(TodoList_Value);
        }
    }

    private void getData() {
        // 초기화
        toDoList = new ArrayList<>();
        itemKeys = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.todo_list_item, toDoList);
        listView.setAdapter(adapter);

        Query query = myDB_ref.child(strHeader).child(userNo);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    toDoList.clear();
                    itemKeys.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String strKey = postSnapshot.child("todo").getValue(String.class);
                        String itemKey = postSnapshot.getKey();

                        // 아이템 등록
                        toDoList.add(strKey);
                        itemKeys.add(itemKey);

                        // 적용
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Tag: ", "Failed to read value", error.toException());
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                boolean isChecked;

                // 취소선 스타일 토글
                if (textView.getPaintFlags() == 1283) {
                    textView.setPaintFlags(1299);
                    isChecked = true;
                } else {
                    textView.setPaintFlags(1283);
                    isChecked = false;
                }

                updateItemCheckedStatusInFirebase(position, isChecked);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteItemFromFirebase(position);
                return true;
            }
        });
    }

    private void updateItemCheckedStatusInFirebase(int position, boolean isChecked) {
        String itemKey = itemKeys.get(position);
        myDB_ref.child(strHeader).child(userNo).child(itemKey).child("checked").setValue(isChecked);
    }

    private void deleteItemFromFirebase(int position) {
        String itemKey = itemKeys.get(position);
        myDB_ref.child(strHeader).child(userNo).child(itemKey).removeValue();
    }
}