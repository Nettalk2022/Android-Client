package com.example.chatting2;

import static java.lang.Boolean.TRUE;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
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

    String read;

    int count=0;
    TextView count_people;

    List<String> list;
    ArrayAdapter<String> adapter;
    ListView personlistView;
    RadioGroup radioGroup;

    //server connect
    String user, sendmsg;
    String name_;
    int every_state=0;

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
        radioGroup = (RadioGroup) findViewById(R.id.radio_group); //귓속말, 전체 채팅 radiogroup

        list = new ArrayList<>(20);
        adapter = new ArrayAdapter<>(this, R.layout.my_list_item, list);
        //list.add("*** 접속자 목록 ***");

        personlistView = (ListView) findViewById(R.id.personListView);
        personlistView.setAdapter(adapter);

        //귓속말로 선택한 대상의 이름 받기 위함 (listview 아이템 클릭시) 초기화됨
        personlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {

                //클릭한 아이템의 문자열을 가져옴
                name_ = (String)adapterView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),"대화상대 "+name_+" 으로 선택",Toast.LENGTH_LONG).show();
            }
        });

        //radio button에 따라 다른 event 적용
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.personal_talk) {//귓속말을 선택했을 경우
                    every_state=1;
                    Toast.makeText(getApplicationContext(),"귓속말 선택:"+every_state,Toast.LENGTH_LONG).show();

                }
                else{ //전체말을 선택했을 경우
                    every_state=0;
                    Toast.makeText(getApplicationContext(),"전체 대화 선택:"+every_state,Toast.LENGTH_LONG).show();
                }
            }
        });

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
                            //radio button에 따라 다른 event 적용
                            if (every_state == 0){ //전체대화라면
                                sendWriter.println("/a"+sendmsg);
                                System.out.println("Send: " + sendmsg);
                                message.setText("");
                            }else{//귓속말을 선택했을 경우
                                try {
                                    sendWriter.println(("/s" + name_ + "-" + sendmsg));
                                    System.out.println("보냄  : /s" + name_ + "-" + sendmsg);
                                    chatView.append(name_+ "님께 보내는 귓말 ▶▶ " + sendmsg + "\n");
                                    message.setText("");
                                }catch (Exception e) {
                                    e.printStackTrace();}
                            }
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