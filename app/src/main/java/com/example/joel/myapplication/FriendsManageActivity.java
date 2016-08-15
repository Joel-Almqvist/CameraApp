package com.example.joel.myapplication;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsManageActivity extends AppCompatActivity {

    public int clicked;
    ArrayList<String> requestsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_manage);
        setTitle("Friends Requests");

        final ListView requestsListView = (ListView) findViewById(R.id.RequestsListView);
        final TextView friendName = (TextView) findViewById(R.id.textViewAddFriend);
        clicked = 0;

        String username = getIntent().getStringExtra("username");
        getFriendRequests(this,username,requestsListView, friendName);
    }
    public void getFriendRequests (final Context context, final String username, final ListView requestsListView, final TextView displayedName){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/get_requests/"+username,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONArray responseMessage = response.getJSONArray("respons");
                    final ArrayList<String> friendRequests = new ArrayList<String>();
                    if(responseMessage != null){
                        for (int i=0; i<responseMessage.length(); i++){
                            friendRequests.add(responseMessage.get(i).toString());
                        }
                    }
                    final ArrayAdapter requestsAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, friendRequests);
                    requestsListView.setAdapter(requestsAdapter);
                    displayedName.setText(friendRequests.get(0));

                    requestsListView.setOnItemClickListener(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    clicked = position;
                                    displayedName.setText(friendRequests.get(position));
                                }
                            });

                    final Button button = (Button) findViewById(R.id.DeclineFriendButton);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (friendRequests.isEmpty() || clicked < 0){
                                Toast.makeText(getApplicationContext(), "Nothing Selected", Toast.LENGTH_SHORT).show();
                                displayedName.setText("");
                            }

                            else if (clicked == 0){
                                removeFriend(getApplicationContext(),username, displayedName.getText().toString());
                                friendRequests.remove(clicked);
                                requestsAdapter.notifyDataSetChanged();
                                if (friendRequests.isEmpty()) {
                                    displayedName.setText("");
                                    clicked = -1;}
                                else{
                                    displayedName.setText(friendRequests.get(0));
                                }
                            }
                            else {
                                removeFriend(getApplicationContext(),username, displayedName.getText().toString());
                                friendRequests.remove(clicked);
                                requestsAdapter.notifyDataSetChanged();
                                clicked --;
                                displayedName.setText(friendRequests.get(clicked));

                            }
                        }
                    });

                    final Button buttonAccept = (Button) findViewById(R.id.AcceptFriendButton);
                    buttonAccept.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            if (friendRequests.isEmpty() || clicked < 0){
                                displayedName.setText("");
                            }

                            else if (clicked == 0){
                                addFriend(getApplicationContext(),username, displayedName.getText().toString());
                                friendRequests.remove(clicked);
                                requestsAdapter.notifyDataSetChanged();
                                if (friendRequests.isEmpty()) {
                                    displayedName.setText("");
                                    clicked = -1;}
                                else{
                                    displayedName.setText(friendRequests.get(0));
                                }
                            }
                            else {
                                addFriend(getApplicationContext(),username, displayedName.getText().toString());
                                friendRequests.remove(clicked);
                                requestsAdapter.notifyDataSetChanged();
                                clicked --;
                                displayedName.setText(friendRequests.get(clicked));
                            }
                        }
                    });
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(context, "Connection to server failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "No pending requests", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(sr);
    }

    public void addFriend (final Context context, final String username, final String friends_username){
        RequestQueue queue = Volley.newRequestQueue(context);
        final JSONObject object = new JSONObject();
        try{
            object.put("username", username);
            object.put("friends_username", friends_username);
        }catch(JSONException e){e.printStackTrace();}
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST,"http://cameraapp-messagesserver.openshift.ida.liu.se/add_friend", object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    String responseMesssage = response.getString("respons");
                    Toast.makeText(context, responseMesssage, Toast.LENGTH_SHORT).show();
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(context, "Connection to server failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(context, "Couldn't reach the server", Toast.LENGTH_SHORT).show();
            }
        }){
        };
        queue.add(sr);
    }

    public void removeFriend (final Context context, final String username, final String friends_username){
        RequestQueue queue = Volley.newRequestQueue(context);
        final JSONObject object = new JSONObject();
        try{
            object.put("username", username);
            object.put("friends_username", friends_username);
        }catch(JSONException e){e.printStackTrace();}
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.POST,"http://cameraapp-messagesserver.openshift.ida.liu.se/remove_friend", object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    String responseMesssage = response.getString("respons");
                    Toast.makeText(context, responseMesssage, Toast.LENGTH_SHORT).show();
                }catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(context, "Connection to server failed", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Couldn't reach the server", Toast.LENGTH_SHORT).show();
            }
        }){
        };
        queue.add(sr);
    }
}
