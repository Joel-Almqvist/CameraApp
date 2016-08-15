package com.example.joel.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
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

public class ProfileActivity extends AppCompatActivity {
    final ArrayList<String> allOldChallenges = new ArrayList<String>();
    final ArrayList<Integer> oldChallengesID = new ArrayList<Integer>();
    ArrayAdapter challengesHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile");

        challengesHistoryAdapter = new ArrayAdapter<String>(getBaseContext(),
               android.R.layout.simple_list_item_1, allOldChallenges);

        final String username = getIntent().getStringExtra("username");
        ListView historyList = (ListView) findViewById(R.id.history_list);
        getOldChallenges(this,username,historyList);
        TextView titleTextView = (TextView) findViewById(R.id.profileTitle);
        titleTextView.setText(username);
        titleTextView.setTextColor(Color.rgb(100,100,200));

        final Button challengesButton = (Button) findViewById(R.id.challenges_button);
        challengesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent challengesIntent = new Intent(getBaseContext(), ChallengesActivity.class);
                challengesIntent.putExtra("username", username);
                startActivity(challengesIntent);
            }
        });

        final ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshProfileImageButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshHistoryListView(getApplicationContext(),username);
            }
        });

        final Button friendsButton = (Button) findViewById(R.id.friendslist_button);
        friendsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent friendIntent = new Intent(getBaseContext(), FriendsActivity.class);
                String username = getIntent().getStringExtra("username");
                friendIntent.putExtra("username", username);
                startActivity(friendIntent);

            }
        });

        final Button challengeFriendButton = (Button) findViewById(R.id.challengeFriendButton);
        challengeFriendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent challengeIntent = new Intent(getBaseContext(), ChallengeFriendActivity.class);
                String username = getIntent().getStringExtra("username");
                challengeIntent.putExtra("username", username);
                startActivity(challengeIntent );

            }
        });

        final Button voteButton = (Button) findViewById(R.id.vote_button);
        voteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent voteIntent = new Intent(getBaseContext(), VoteActivity.class);
                String username = getIntent().getStringExtra("username");
                voteIntent.putExtra("username", username);
                startActivity(voteIntent);
            }
        });
    }

    public void getOldChallenges (final Context context, final String username, final ListView oldChallengesListView){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/all_nonactive/"+username,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray allChallengesJSONArray = response.getJSONArray("respons");
                            final JSONArray idJSONArray = response.getJSONArray("id_list");
                            if(allChallengesJSONArray != null){
                                for (int i=0; i<allChallengesJSONArray.length(); i++){
                                    allOldChallenges.add(allChallengesJSONArray.getString(i));
                                    oldChallengesID.add(idJSONArray.getInt(i));
                                }
                            }
                            oldChallengesListView.setAdapter(challengesHistoryAdapter );

                            oldChallengesListView.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            try {
                                                Intent postGameIntent = new Intent(getApplicationContext(), PostGameActivity.class);
                                                postGameIntent.putExtra("username", username);
                                                postGameIntent.putExtra("challengeID", idJSONArray.getInt(position));
                                                startActivity(postGameIntent);
                                            }catch(JSONException e){e.printStackTrace();}
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
                Toast.makeText(context, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(sr);
    }

    public void refreshHistoryListView (final Context context, final String username){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/all_nonactive/"+username,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray allChallengesJSONArray = response.getJSONArray("respons");
                            final JSONArray idJSONArray = response.getJSONArray("id_list");
                            if(allChallengesJSONArray != null){
                                allOldChallenges.clear();
                                oldChallengesID.clear();
                                for (int i=0; i<allChallengesJSONArray.length(); i++){
                                    allOldChallenges.add(allChallengesJSONArray.getString(i));
                                    oldChallengesID.add(idJSONArray.getInt(i));
                                }
                                challengesHistoryAdapter.notifyDataSetChanged();
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