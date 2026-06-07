package com.example.tablenew;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TableNew";

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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: server=" + serverIP + ":" + port);
        setContentView(R.layout.activity_main);

        mSend        = findViewById(R.id.Send);
        mBtnUp       = findViewById(R.id.btnUp);
        mBtnDown     = findViewById(R.id.btnDown);
        mBtnRefresh  = findViewById(R.id.btnRefresh);
        mCommand     = findViewById(R.id.commandText);
        mLogViewer   = findViewById(R.id.LogViewer);
        mPositionDisplay = findViewById(R.id.positionDisplay);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 123);
        }

        // Fetch calibration buttons async — UI stays responsive while waiting
        sendAsync("getAllCalibration", resp -> {
            LinearLayout layout = findViewById(R.id.linearlayout);
            String[] cals = resp.split("@");
            for (int i = 0; i < cals.length; i++) {
                if (cals[i].isEmpty()) continue;
                Button btn = new Button(this);
                btn.setText(cals[i]);
                layout.addView(btn);
                int idx = i;
                btn.setOnClickListener(v -> {
                    String cal = cals[idx];
                    String calName = cal.substring(0, cal.indexOf(":"));
                    String command = "moveTo:" + calName;
                    sendAsync(command, response -> {
                        appendLog("Command:" + command + " response:" + response);
                        refreshPosition();
                    });
                });
            }
            refreshPosition();
        });

        mBtnUp.setOnClickListener(v -> {
            String command = "goto:" + (currentPosition + STEP_SIZE);
            sendAsync(command, response -> {
                appendLog("Command:" + command + " response:" + response);
                refreshPosition();
            });
        });

        mBtnDown.setOnClickListener(v -> {
            String command = "goto:" + (currentPosition - STEP_SIZE);
            sendAsync(command, response -> {
                appendLog("Command:" + command + " response:" + response);
                refreshPosition();
            });
        });

        mBtnRefresh.setOnClickListener(v -> refreshPosition());

        mSend.setOnClickListener(v -> {
            String command = String.valueOf(mCommand.getText());
            mCommand.setText("");
            sendAsync(command, response -> appendLog("Command:" + command + " response:" + response));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void refreshPosition() {
        sendAsync("getenc", response -> {
            try {
                currentPosition = Integer.parseInt(response.trim());
                mPositionDisplay.setText(String.valueOf(currentPosition));
            } catch (NumberFormatException e) {
                mPositionDisplay.setText("? (" + response.trim() + ")");
            }
        });
    }

    private void appendLog(String line) {
        logs += line + "\r\n";
        mLogViewer.setText(logs);
    }

    private void sendAsync(String command, Consumer<String> callback) {
        executor.execute(() -> {
            String response = new TCPClient(serverIP, port).sendMessage(command);
            mainHandler.post(() -> callback.accept(response));
        });
    }
}
