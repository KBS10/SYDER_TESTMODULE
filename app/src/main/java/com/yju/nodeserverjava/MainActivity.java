package com.yju.nodeserverjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    private Button buttonConnect;
    private EditText InputIP, InputPORT;
    private TextView ConnectMessage;
    private RadioButton firstCar, secondCar, thirdCar;
    private RadioGroup carGroup;
    private String IP, PORT, Socket_URL, carNumber;

    private SharedPreferences preferences;  // 안드로이드 내장 data
    public static Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputIP             = findViewById(R.id.inputIP);
        InputPORT           = findViewById(R.id.inputPORT);
        ConnectMessage      = findViewById(R.id.textError);
        carGroup            = findViewById(R.id.carGroup);
        firstCar            = findViewById(R.id.radioCar1);
        secondCar           = findViewById(R.id.radioCar2);
        thirdCar            = findViewById(R.id.radioCar3);
        buttonConnect       = findViewById(R.id.buttonConnect);

        // 저장된 데이터 불러오기
        dataImport();

        // 먼저 아이피, 포트를 입력했는지 검사.
        buttonConnect.setOnClickListener(v -> {
            if(InputIP.getText().toString().equals("") || InputPORT.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), "아이피 포트를 정확하게 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else if (carGroup.getCheckedRadioButtonId() ==  -1){
                Toast.makeText(getApplicationContext(), "차량 호수를 체크해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                IP = InputIP.getText().toString();
                PORT = InputPORT.getText().toString();
                connectServer(IP, PORT);
                ConnectMessage.setVisibility(View.VISIBLE);
                Log.i("Socket", "서버 접속 에러");
            }
        });
    }

    protected void connectServer(String Socket_IP, String Socket_PORT) {
        Socket_URL = "http://" + Socket_IP  +":" + Socket_PORT + "/device";
        try {
            socket = IO.socket(Socket_URL);
            socket.connect();
            carConnect();
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void carConnect() {
        Log.i("socket", "메인 엑티비티 onConnect 부분");
        carNumber = selectedCar();
        // 서버 입장시 차량 번호 같이 전송.
        socket.emit("connectCar", carNumber);
        JSONObject object = new JSONObject();
        try {
            object.put("carNumber", carNumber);
            object.put("status", "운행대기");
        } catch (JSONException e) {
            Log.e("socket", "JSONObject 생성 오류!");
            e.printStackTrace();
        }
        // 네임서버 [차] -> 룸에 접속.
        socket.emit("JOIN:CAR", object);
        ConnectMessage.setVisibility(View.INVISIBLE);
        // save input data
        dataSave();
        //  intent put data
        Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
        intent.putExtra("URL", Socket_URL);
        intent.putExtra("CAR", carNumber);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "서버 연결 성공!", Toast.LENGTH_SHORT).show();
    }

    private String selectedCar() {
        String car = null;
        if (firstCar.isChecked())         car = "1";
        else if (secondCar.isChecked())   car = "2";
        else if (thirdCar.isChecked())    car = "3";
        Log.i("socket", "선택된 차량은 " + car + "입니다.");
        return car;
    };

    private void dataImport() {
        preferences         = getSharedPreferences("Car", Context.MODE_PRIVATE);
        String saved_ip     = preferences.getString("ip", null);
        String saved_port   = preferences.getString("port", null);

        if (saved_ip==null) {
            Toast.makeText(getApplicationContext(), "아이피, 포트, 차량 호수를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {
            InputIP.setText(saved_ip);
            InputPORT.setText(saved_port);
        }
    };

    private void dataSave() {
        SharedPreferences.Editor saveEditor = preferences.edit();
        saveEditor.putString("ip", IP);
        saveEditor.putString("port", PORT);
        saveEditor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}
