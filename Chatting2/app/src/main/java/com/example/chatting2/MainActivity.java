package com.example.chatting2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler;
    InetAddress serverAddr;
    Socket socket;
    PrintWriter sendWriter;
    private String ip; //"175.195.237.59"
    private int port;

    TextView greeting_username;
    String UserID;
    Button chatbutton, exitbutton;
    TextView chatView;
    EditText message;
    String sendmsg;
    String read;

    int count=0;
    TextView count_people;

    List<String> list;
    ArrayAdapter<String> adapter;
    ListView personlistView;

    //server connect
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        greeting_username = (TextView) findViewById(R.id.greeting_username);
        chatView = (TextView) findViewById(R.id.chatView);
        message = (EditText) findViewById(R.id.message);
        Intent intent = getIntent();

        ip = intent.getStringExtra("host");
        port = intent.getIntExtra("port",8000);
        UserID = intent.getStringExtra("username");
        greeting_username.setText(UserID);
        chatbutton = (Button) findViewById(R.id.chatbutton);
        exitbutton = (Button) findViewById(R.id.exitbutton);

        count_people= (TextView)findViewById(R.id.count_people);

        list = new ArrayList<>(20);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        list.add("*** 접속자 목록 ***");

        personlistView = (ListView) findViewById(R.id.personListView);
        personlistView.setAdapter(adapter);

        new Thread() {
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(ip);
                    socket = new Socket(serverAddr, port);
                    sendWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"euc-kr")),true);
                    sendWriter.println(UserID);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(),"euc-kr"));
                    while(true){
                        read = input.readLine();
                        if(read!=null){
                            //들어올때
                            if(read.indexOf("/f")==0){
                                user = read.substring(2);
                                System.out.println("123 :" + user);

                                addName(user);
                                mHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        count++;
                                        count_people.setText(String.valueOf(count));
                                        refreshList();
                                    }
                                });
                            }
                            //나갈때
                            else if(read.indexOf("/e")==0){
                                user = read.substring(2);

                                //Toast.makeText(getApplicationContext(),"나감"+count,Toast.LENGTH_LONG).show();
                                removeName(user);
                                //handler 사용해서 ui 변경
                                mHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        count--;
                                        count_people.setText(String.valueOf(count));
                                        refreshList();
                                    }
                                });
                            }
                            else{
                            mHandler.post(new msgUpdate(read));
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } }}.start();

        chatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmsg = message.getText().toString();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sendWriter.println(sendmsg);
                            System.out.println("보냄  : " + sendmsg);
                            sendWriter.flush();
                            message.setText("");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        //exit 버튼 클릭 -> 종료
        exitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {System.exit(1);}
            }
        });
    }

    //-------------------------------리스트에 이름추가-------------------
    public void addName(String user){
        System.out.println(user+"came in");
        list.add(user);
    }
    public void removeName(String user){
        System.out.println(user+"went out");
        list.remove(user);
    }

    //동적으로 list 변환 수행
    public void refreshList(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                personlistView.invalidateViews();
            }
        });
    }

    @Override
    protected void onStop() {  //앱 종료시
        super.onStop();
        try {
            socket.close(); //소켓을 닫는다.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class msgUpdate implements Runnable{
        private String msg;
        public msgUpdate(String str) {this.msg=str;}

        @Override
        public void run() {
            chatView.setText(chatView.getText().toString()+msg+"\n");
        }
    }
}