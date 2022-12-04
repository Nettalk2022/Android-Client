package com.example.chatting2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EnterActivity extends AppCompatActivity {
    EditText host, port, username;
    Button enterButton, exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        enterButton = (Button)findViewById(R.id.enter_button);
        exitButton=(Button)findViewById(R.id.exit_button);

        host = (EditText)findViewById(R.id.host_address);
        port = (EditText)findViewById(R.id.port_number);
        username = (EditText)findViewById(R.id.user_name);

        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);

                String host_=host.getText().toString();
                int port_=Integer.parseInt(port.getText().toString());
                String username_ = username.getText().toString();
                intent.putExtra("host",host_);
                intent.putExtra("port",port_);
                intent.putExtra("username",username_);
                startActivity(intent);
            }
        });


        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                {System.exit(1);}
            }
        });

    }
}
