package com.example.tablenew;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button mSend;
    private Button mBtnUp;
    private Button mBtnDown;
    private Button mBtnRefresh;
    private EditText mCommand;
    private TextView mLogViewer;
    private TextView mPositionDisplay;

    private static final int STEP_SIZE = 500;

    private int currentPosition = 0;
    private String logs = "";

    String serverIP = "10.0.0.239";
    int port = 34175;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSend = (Button) findViewById(R.id.Send);
        mBtnUp = (Button) findViewById(R.id.btnUp);
        mBtnDown = (Button) findViewById(R.id.btnDown);
        mBtnRefresh = (Button) findViewById(R.id.btnRefresh);
        mCommand = (EditText) findViewById(R.id.commandText);
        mLogViewer = (TextView) findViewById(R.id.LogViewer);
        mPositionDisplay = (TextView) findViewById(R.id.positionDisplay);

        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 123);
        }

        // Fetch calibration buttons
        String resp = SendMessage("getAllCalibration");
        String[] cals = resp.split("@");

        for (int i = 0; i < cals.length; i++) {
            Button myButton = new Button(this);
            myButton.setText(cals[i]);

            LinearLayout layout = findViewById(R.id.linearlayout);
            layout.addView(myButton);

            int finalI = i;
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cal = cals[finalI];
                    int splitIndex = cal.indexOf(":");
                    String calName = cal.substring(0, splitIndex);
                    String command = "moveTo:" + calName;
                    String response = SendMessage(command);

                    logs += "Command:" + command + " response:" + response + "\r\n";
                    mLogViewer.setText(logs);

                    refreshPosition();
                }
            });
        }

        // Up: move to current position + STEP_SIZE
        mBtnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int target = currentPosition + STEP_SIZE;
                String command = "goto:" + target;
                String response = SendMessage(command);
                logs += "Command:" + command + " response:" + response + "\r\n";
                mLogViewer.setText(logs);
                refreshPosition();
            }
        });

        // Down: move to current position - STEP_SIZE
        mBtnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int target = currentPosition - STEP_SIZE;
                String command = "goto:" + target;
                String response = SendMessage(command);
                logs += "Command:" + command + " response:" + response + "\r\n";
                mLogViewer.setText(logs);
                refreshPosition();
            }
        });

        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPosition();
            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = String.valueOf(mCommand.getText());
                mCommand.setText("");
                String response = SendMessage(command);
                logs += "Command:" + command + " response:" + response + "\r\n";
                mLogViewer.setText(logs);
            }
        });

        // Read initial position
        refreshPosition();
    }

    private void refreshPosition() {
        String response = SendMessage("getenc");
        try {
            currentPosition = Integer.parseInt(response.trim());
            mPositionDisplay.setText(String.valueOf(currentPosition));
        } catch (NumberFormatException e) {
            mPositionDisplay.setText("? (" + response.trim() + ")");
        }
    }

    public String SendMessage(String command) {
        TCPClient tcp = new TCPClient(serverIP, port);
        String response = tcp.sendMessage(command);
        return response;
    }
}
