package com.example.musagoboard;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class BoardListActivity extends AppCompatActivity {
    private static final String TAG = "zzix";
    tbAdapter adapter;

    ArrayList<tbFreeBoard> BoardList;

    Button btn_new;


    private static String IP_ADDRESS = "192.168.0.150/eunhye"; //본인 IP주소를 넣으세요.


    RecyclerView BoardRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boardlist);
        BoardRecyclerView = findViewById(R.id.BoardRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        BoardRecyclerView.setLayoutManager(layoutManager);

        adapter = new tbAdapter();


        Init();

        Button btn_new = findViewById(R.id.btn_new);
        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        adapter.setOnItemClickListener((new OnTbItemListener(){
            @Override
            public void onItemClick(tbAdapter.ViewHolder holder, View view, int posotoin) {
                tbFreeBoard item = adapter.getItem(posotoin);
                Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
                startActivity(intent);
               Log.d(TAG,"아이템 선택됨 :" + item.getSeqNo());
                  intent.putExtra("seqNo",item.getSeqNo());
                startActivity(intent);
                   // selectData(sqeNo);
//                showToast("아이템 선택됨 :" + item.getName());
            }
        }));
    }



    private void Init() {
        BoardList = new ArrayList<>();
        listBoard listBoard = new listBoard();
        listBoard.execute("name");
    }

    class listBoard extends AsyncTask<String, Void, String> {
        public listBoard() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "확인");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {

                Log.d(TAG, "result: " + result);

                // 결과값이 JSONArray 형태로 넘어오기 때문에
                // JSONArray, JSONObject 를 사용해서 파싱
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(result);
                String list = jsonObject.optString("board");
                Log.d(TAG, "****** list :" + list);

                JSONArray jsonArray = null;
//                jsonArray = new JSONArray(jsonObject.optString("board"));
                jsonArray = new JSONArray(list);


                Log.d(TAG, "jsonArray.length(): " + jsonArray.length());

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    // Database 의 데이터들을 변수로 저장한 후 해당 TextView 에 데이터 입력
                    int seqNo = jsonObject1.getInt("seqNo");
                    Log.d(TAG, "seqNo :" + seqNo);
                    String subject = jsonObject1.optString("subject");
                    Log.d(TAG, "subject :" + subject);
                    String author = jsonObject1.optString("author");
                    Log.d(TAG, "author :" + author);
                    String regDate = jsonObject1.optString("regDate");
                    Log.d(TAG, "regDate :" + regDate);


                    adapter.addItem(new tbFreeBoard(seqNo, subject, author, regDate));

//                    Adapter = new Adapter();
//                    Adapter.addItem(new tbFreeBoard(0, "오늘은 더움", "킴은혜", "선풍기를 쐬자"));
//                    Adapter.addItem(BoardList.add(0, sqeNo, subject, author, regDate));
//                    Adapter.notifyItemInserted(0);
//                    BoardRecyclerView.setAdapter(Adapter);
                }


                BoardRecyclerView.setAdapter(adapter);

            } catch (JSONException e) {
//                throw new RuntimeException(e);
                //e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String seqNo = params[0];

// 호출할 php 파일 경로
            String server_url = "http://" + IP_ADDRESS + "/android_log_List.php";

            Log.d(TAG, "URL:" + server_url);


            URL url;
            String response = "";
            try {
                url = new URL(server_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
//                Uri.Builder builder = new Uri.Builder()
//                        .appendQueryParameter("name", "");
//                Log.i(TAG,"builder : "+builder);
//                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
//                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.i(TAG, "responseCode : " + responseCode);
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }
    }
//
////    //실제 요청 작업을 수행해주는 요청큐 객체 생성
////    RequestQueue requestQueue= Volley.newRequestQueue(this);
////
////    //요청큐에 요청 객체 생성
////        requestQueue.add(jsonArrayRequest);
//
//    }
//

}