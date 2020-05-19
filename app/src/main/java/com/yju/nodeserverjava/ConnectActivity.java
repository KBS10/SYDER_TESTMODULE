package com.yju.nodeserverjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectActivity extends AppCompatActivity {
    private Button buttonSendMessage, buttonDisconnect, buttonLocation, buttonStatus, buttonUpdate;
    private EditText SendMessage, LocationLat, LocationLng;
    private TextView CarNumber, CarStatus;
    private String carName;

    public Socket socket;
    public static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        buttonSendMessage   = findViewById(R.id.buttonConnectTest);
        buttonDisconnect    = findViewById(R.id.buttonDisconnect);
        buttonStatus        = findViewById(R.id.buttonStatus);
//        buttonLocation      = findViewById(R.id.buttonLocation);
        buttonUpdate        = findViewById(R.id.buttonUpdate);
        SendMessage         = findViewById(R.id.textSendMessage);
        LocationLat         = findViewById(R.id.textLat);
        LocationLng         = findViewById(R.id.textLng);
        CarNumber           = findViewById(R.id.textCarNumber);
        CarStatus           = findViewById(R.id.textCarStatus);

        Intent intent = getIntent();
        String url  = intent.getStringExtra("URL");
        carName     = intent.getStringExtra("CAR");
        CarNumber.setText(carName + "호차");

        try {
            socket = IO.socket(url);
            Log.i("socket", "지금 연결함!!");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        testSocket();
    }

    @Override
    protected void onPause() {
        socket.disconnect();
        super.onPause();
        finish();
    }

    private void testSocket() {
        socket.on("answer", onMessage);
        socket.on("location", onLocation);
        socket.on("statusChange", onStatus);

        // 메세지 전송
        buttonSendMessage.setOnClickListener(v -> {
            socket.emit("say", SendMessage.getText().toString());
            Log.i("Socket", "서버로 data 전송");
        });

        // 서버 연결 종료
        buttonDisconnect.setOnClickListener( v -> {
            // disconnect to Car
            socket.emit("car_disconnect");
            // disconnect to Server
            socket.disconnect();
            Toast.makeText(getApplicationContext(), carName + " : 서버와의 연결을 종료합니다.", Toast.LENGTH_SHORT).show();
            onPause();
        });

        // 움직이는 모듈 테스트
//        buttonLocation.setOnClickListener( v -> {
////             String[] lat = {"35.89623422094425", "35.896279612631304", "35.89640230015868", "35.8964995774788", "35.896512961300466", "35.896434077940036", "35.89635800671141", "35.896245151363686", "35.89644662499194" };
////             String[] lng = {"128.62013646906556", "128.62027860119983", "128.6205358286924", "128.62083960798512", "128.6211832086395", "128.62151938968248", "128.6218140959231", "128.6221634262081", "128.62242503324413"};
////             count = 0;
////                Timer m_timer = new Timer();
////                TimerTask m_task = new TimerTask() {
////                    @Override
////                    public void run() {
////                        if (count < 8) {
////                            JSONObject object = new JSONObject();
////                            try {
////                                object.put("name", carName);
////                                object.put("lat", lat[count]);
////                                object.put("lng", lng[count]);
////                                socket.emit("update", object);
////                            } catch (JSONException e) {
////                                Log.e("socket", "소켓통신 JSON 오류!");
////                                e.printStackTrace();
////                            }
////                            count++;
////                        } else {
////                            m_timer.cancel();
////                            Toast.makeText(getApplicationContext(), " 위치 전송 종료..", Toast.LENGTH_SHORT).show();
////                        }
////                    }
////                };
////                m_timer.schedule(m_task, 0, 1000);
//        });

        buttonStatus.setOnClickListener(v-> {
            try {
                JSONObject object = new JSONObject();
                object.put("name", carName);
                object.put("status", "운행중");
                socket.emit("status", object);
            } catch (JSONException e) {
                Log.e("socket", "Failed to create JSONObject", e);
            }
        });

        // 차량 위치 변경 전송 모듈
        buttonUpdate.setOnClickListener(v -> {
            try {
                JSONObject object = new JSONObject();
                object.put("status", 301);
                object.put("carNumber", carName);
                object.put("car_lat", LocationLat.getText().toString());
                object.put("car_lng", LocationLng.getText().toString());
                object.put("car_battery", 98);
                socket.emit("car_update", object);
            } catch (JSONException e) {
                Log.e("socket", "Failed to create JSONObject", e);
            }

        });
    }

    private Emitter.Listener onStatus = args -> runOnUiThread(() -> {
        String data = (String)args[0];
        CarStatus.setText(data);
        Toast.makeText(getApplicationContext(), "운행상태 변경!", Toast.LENGTH_SHORT).show();
    });

    private Emitter.Listener onMessage = args -> runOnUiThread(() -> {
        String data = (String)args[0];
        Log.i("Socket", "받은 데이터: " + data);
        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
    });

    private Emitter.Listener onLocation = items -> runOnUiThread(() -> {
        String data = (String)items[0];
        Log.i("Socket", "(서버) 위치정보 데이터 :" + data);
        try {
            JSONObject jObject = new JSONObject(data);
            String lat = jObject.getString("lat");
            String lng = jObject.getString("lng");
            Toast.makeText(getApplicationContext(), "서버로 부터 받은 데이터\n" + lat + "\n" + lng, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });
}

