package com.example.todolist;

import java.util.HashMap;
import java.util.Map;

public class TodoInfo {
    public String todo;
    public Boolean checked;

    public TodoInfo(String todo, boolean checked){
        this.todo = todo;
        this.checked = checked;
    }

    public String getTodo(){
        return todo;
    }

    public void setTodo(String todo){
        this.todo = todo;
    }

    public boolean isChecked(){
        return checked;
    }

    public void setChecked(boolean checked){
        this.checked = checked;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();

        result.put("todo", todo);
        result.put("checked", checked);

        return result;
    }
}
