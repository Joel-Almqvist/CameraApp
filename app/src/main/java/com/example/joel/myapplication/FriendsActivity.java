package com.example.joel.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {
    static final int photo_intent_id = 0;
    String mImageFileLocation = "";
    int challengeID = -1;
    ArrayList<String> activeChallengesUsernames1 = new ArrayList<String>();
    ArrayList<String> activeChallengesUsernames2 = new ArrayList<String>();
    ArrayList<Integer> allActiveChallengesID = new ArrayList<Integer>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        String username = getIntent().getStringExtra("username");
        getActiveChallenges(this, username);
        setTitle("Friends");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ListView friendsListView = (ListView) findViewById(R.id.friendsListList);
        getAllFriends(this,username,friendsListView);

        final ImageButton button = (ImageButton) findViewById(R.id.toggleRequestButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent manage_friends_intent = new Intent(getBaseContext(), FriendsManageActivity.class);
                String username = getIntent().getStringExtra("username");
                manage_friends_intent.putExtra("username", username);
                startActivity(manage_friends_intent);

            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        final Button add_friend_button = (Button) findViewById(R.id.addFriendButton);
        add_friend_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.friendRequestEditText);
                String friends_name = editText.getText().toString();
                editText.setText("");
                String username = getIntent().getStringExtra("username");
                addFriend(getApplicationContext(), username, friends_name);
            }
        });

        final Button remove_friend_button = (Button) findViewById(R.id.buttonRemoveFriend);
        remove_friend_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.friendRequestEditText);
                String friends_name = editText.getText().toString();
                editText.setText("");
                String username = getIntent().getStringExtra("username");
                removeFriend(getApplicationContext(), username, friends_name);
            }
        });
    }

    public void getAllFriends (final Context context, final String username, final ListView friendsListView){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/get_friends/"+username,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray responseMessage = response.getJSONArray("respons");
                            final ArrayList<String> allFriends = new ArrayList<String>();
                            if(responseMessage != null){
                                for (int i=0; i<responseMessage.length(); i++){
                                    allFriends.add(responseMessage.get(i).toString());
                                }
                            }
                            final ListAdapter friendsAdapter = new ArrayAdapter<String>(getBaseContext(),
                                    android.R.layout.simple_list_item_1, allFriends);
                            friendsListView.setAdapter(friendsAdapter);

                            friendsListView.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                Intent friendProfileIntent = new Intent(getApplicationContext(), FriendProfileActivity.class);
                                            friendProfileIntent.putExtra("username",username);
                                            friendProfileIntent.putExtra("friendsname",allFriends.get(position));
                                                startActivity(friendProfileIntent);
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

                Toast.makeText(context, "Couldn't reach the server", Toast.LENGTH_SHORT).show();
            }
        }){
        };
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
        });
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
        });
        queue.add(sr);
    }

    public void getActiveChallenges (final Context context, final String username){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/all_active/"+username,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try{
                            JSONArray responseMessage = response.getJSONArray("respons");
                            JSONArray givenID = response.getJSONArray("id_list");

                            JSONArray challengerName1JSONArray = response.getJSONArray("challenger_1");
                            JSONArray challengerName2JSONArray = response.getJSONArray("challenger_2");
                            if(!response.getBoolean("empty")){
                                for (int i=0; i<responseMessage.length(); i++){
                                    activeChallengesUsernames1.add(challengerName1JSONArray.get(i).toString());
                                    activeChallengesUsernames2.add(challengerName2JSONArray.get(i).toString());
                                    allActiveChallengesID.add(Integer.parseInt(givenID.get(i).toString()));
                                }
                            }
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
        });
        queue.add(sr);
    }
}
