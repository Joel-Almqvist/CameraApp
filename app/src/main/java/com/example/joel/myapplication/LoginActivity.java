package com.example.joel.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setTitle("Login");

        final Button button = (Button) findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText loginUser = (EditText) findViewById(R.id.editTextUsername);
                EditText loginPassword = (EditText) findViewById(R.id.editTextPassword);
                String newUsername = loginUser.getText().toString();
                String newPassword = loginPassword.getText().toString();
                loginUser.setText("");
                loginPassword.setText("");
                loginUser(getApplicationContext(), newUsername, newPassword);
            }
        });
        final Button buttonCreateUser = (Button) findViewById(R.id.buttonCreateUser);
        buttonCreateUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText createUser = (EditText) findViewById(R.id.editTextCreateUser);
                EditText createPassword = (EditText) findViewById(R.id.editTextCreatePassword);
                String newUsername = createUser.getText().toString();
                String newPassword = createPassword.getText().toString();
                createUser.setText("");
                createPassword.setText("");
                createUser(getApplicationContext(), newUsername, newPassword);
            }
        });
        }

    public void createUser (final Context context, final String username, final String password){
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        try{
            object.put("username", username);
            object.put("password", password);
        }catch(JSONException e){e.printStackTrace();}
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST,"http://cameraapp-messagesserver.openshift.ida.liu.se/user", object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(context, "User Created", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Failed to create user", Toast.LENGTH_SHORT).show();
            }
        }){
        };
        queue.add(sr);
    }

    public void loginUser (final Context context, final String username, final String password){
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        try{
            object.put("username", username);
            object.put("password", password);
        }catch(JSONException e){e.printStackTrace();}
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST,"http://cameraapp-messagesserver.openshift.ida.liu.se/user/login", object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                     int userID = response.getInt("response");
                    Intent profile_intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    profile_intent.putExtra("userID", userID);
                    profile_intent.putExtra("username", username);
                    startActivity(profile_intent);
                }catch(JSONException e){e.printStackTrace();}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Failed to login", Toast.LENGTH_SHORT).show();
            }
        }){
        };
        queue.add(sr);
    }
}