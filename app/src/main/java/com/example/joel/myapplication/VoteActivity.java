package com.example.joel.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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


public class VoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        setTitle("Vote");
        ListView voteList= (ListView) findViewById(R.id.listViewVote);
        String username = getIntent().getStringExtra("username");
        getFriendsChallenges(this,username,voteList);
    }
    public void getFriendsChallenges (final Context context, final String username, final ListView requestedListView){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET,
                "http://cameraapp-messagesserver.openshift.ida.liu.se/challenges/all_friends/"+username,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray responseMessage = response.getJSONArray("respons");
                            final JSONArray givenID = response.getJSONArray("id_list");
                            final JSONArray challengerName1 = response.getJSONArray("challenger_1");
                            final JSONArray challengerName2 = response.getJSONArray("challenger_2");
                            final ArrayList<String> friendsChallenges = new ArrayList<String>();
                            final ArrayList<Integer> challengesIDArray = new ArrayList<Integer>();

                            if(responseMessage != null){
                                for (int i=0; i<responseMessage.length(); i++){
                                    friendsChallenges.add(responseMessage.get(i).toString());
                                    challengesIDArray.add(Integer.parseInt(givenID.get(i).toString()));
                                }
                            }
                            final ListAdapter challenges_adapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, friendsChallenges);
                            requestedListView.setAdapter(challenges_adapter);
                            requestedListView.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent vote_intent = new Intent(getBaseContext(), VoteScreenActivity.class);
                                            vote_intent.putExtra("username", username);
                                            vote_intent.putExtra("challengeID", challengesIDArray.get(position));
                                            try{
                                                String challenger1 = challengerName1.get(position).toString();
                                                String challenger2 = challengerName2.get(position).toString();
                                                vote_intent.putExtra("challenger1",challenger1);
                                                vote_intent.putExtra("challenger2",challenger2);
                                            }catch (JSONException e){e.printStackTrace();}

                                            startActivity(vote_intent);
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
}