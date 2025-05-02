package com.example.homeautomation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.homeautomation.Utility.MyWebSocketClient;

import org.json.JSONObject;

import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class HomeActivity extends AppCompatActivity {
    private MyWebSocketClient webSocketClient;
    ImageView connectButton;
    Button button2;
    private WebSocket websocket;
    private TextView connectionStatus, temperatureValue;
    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    TextView temperatureLabel, connectionDot;
    ImageView temperatureImage;

    CardView fireAlertContainer, intruderAlertContainer;
    Switch fanSwitch, heaterSwitch, indoorBulbSwitch,
            outdoorBulbSwitch, doorSwitch, intruderSwitch, automaticControlSwitch;
    ProgressBar progressBar;

    ImageView intruderCloseButton, voiceButton, fireCloseButton, optionButton;
    boolean AUTOMATIC_CONTROL = true;
    private static final int SPEECH_REQUEST_CODE = 1;

    MediaPlayer mediaPlayer;

    int selected_option = 0;

    final int[] selectedHour = new int[4];
    final int[] selectedMinute = new int[4];

    final int INDOOR_LIGHT_ON_TIME = 0;
    final int INDOOR_LIGHT_OFF_TIME = 1;
    final int INTRUDER_ON_TIME = 2;
    final int INTRUDER_OFF_TIME = 3;
    final int AUTOMATIC_ON_TIME = 4;
    final int AUTOMATIC_OFF_TIME = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        connectButton = findViewById(R.id.connect_button);
        connectionStatus = findViewById(R.id.connection_status_text);
        button2 = findViewById(R.id.button2);
        temperatureValue = findViewById(R.id.temperature_value);
        progressBar = findViewById(R.id.circularProgressBar);
        fanSwitch = findViewById(R.id.fan_switch);
        heaterSwitch = findViewById(R.id.heater_switch);
        indoorBulbSwitch = findViewById(R.id.indoor_bulb_switch);
        outdoorBulbSwitch = findViewById(R.id.out_door_bulb_switch);
        doorSwitch = findViewById(R.id.door_switch);
        intruderSwitch = findViewById(R.id.intruder_switch);
        fireAlertContainer = findViewById(R.id.fire_alert_container);
        intruderAlertContainer = findViewById(R.id.intruder_alert_container);
        intruderCloseButton = findViewById(R.id.intruder_close_button);
        temperatureLabel = findViewById(R.id.temperature_label);
        temperatureImage = findViewById(R.id.temperature_image);
        automaticControlSwitch = findViewById(R.id.automatic_switch);
        voiceButton = findViewById(R.id.voice_button);
        fireCloseButton = findViewById(R.id.fire_close_button);
        optionButton = findViewById(R.id.options_button);
        connectionDot = findViewById(R.id.connection_dot);
        progressBar.setMax(1000);
        connectWebsocket();

//        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_siren_sound_effect); // alert.mp3 should be in res/raw
//        mediaPlayer.setLooping(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        intruderAlertContainer.setVisibility(View.GONE);
        slideUp(intruderAlertContainer);
        slideUp(fireAlertContainer);



//        try {
//            URI serverUri = new URI("ws://172.20.10.10:8765");
//            webSocketClient = new MyWebSocketClient(serverUri);
//            webSocketClient.connect();
//        } catch (Exception e) {
//            System.out.println("-----------------Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//        Request request = new Request.Builder()
//                .url("ws://172.20.10.10:8765") // Replace with your Raspberry Pi's IP address.
//                .build();
        intruderCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideUp(intruderAlertContainer);
                websocket.send("stop intruder alert");
            }
        });

        fireCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideUp(fireAlertContainer);
                websocket.send("stop fire alert");
            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                try {
//                    URI serverUri = new URI("ws://172.20.10.10:8765");
//                    webSocketClient = new MyWebSocketClient(serverUri);
//                    webSocketClient.connect();
//                } catch (Exception e) {
//                    System.out.println("-----------------Error: " + e.getMessage());
//                    e.printStackTrace();
//                }
                connectWebsocket();

//                if (webSocketClient != null && webSocketClient.isOpen()) {
//                    webSocketClient.send("Hello from Android!");
//                    Toast.makeText(HomeActivity.this, "button pressed", Toast.LENGTH_SHORT).show();
//                }
//                webSocketClient.send("Hello from Android!");
//                websocket.send("Hello from Android!");
            }
        });
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                webSocketClient.send("Hello from Android!");
//                websocket.send("Hello from Android!");
//            }
//        });


//        Request request = new Request.Builder()
//                .url("ws://172.20.10.10:8765") // Replace with your Raspberry Pi's IP address.
//                .build();
        automaticControlSwitch.setChecked(true);
        automaticControlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                 websocket.send("automatic_on");
                } else {
                    websocket.send("automatic_off");
                }
                AUTOMATIC_CONTROL = isChecked;

            }
        });
        sendData();
        voiceButton.setOnClickListener(v-> startSpeechToText());


    }

    void connectWebsocket(){
        Request request = new Request.Builder()
                .url("ws://172.20.10.10:8765") // Replace with your Raspberry Pi's IP address.
                .build();
        websocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                handler.post(() -> connectionStatus.setText("Connected\n"));
                handler.post(() -> connectionDot.setTextColor(Color.GREEN));
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // handler.post(() -> output.append("Received: " + text + "\n"));
//                handler.post(() -> output.setText("Received: " + text + "\n"));
                handler.post(() ->  parseJsonData(text));

//                parseJsonData(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
//                handler.post(() -> output.append("Received bytes: " + bytes.hex() + "\n"));
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(NORMAL_CLOSURE_STATUS, null);
                handler.post(() -> connectionStatus.setText("Connection Disconnected"));
                handler.post(() -> connectionDot.setTextColor(Color.WHITE));
//                handler.post(() -> connectionStatus.setText("Error: " + reason ));

            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                handler.post(() -> connectionStatus.setText("Connection Failed"));
                handler.post(() -> connectionDot.setTextColor(Color.RED));
//                handler.post(() -> connectionStatus.setText("Error: " + t.getMessage() ));
            }
        });

        optionButton.setOnClickListener(v-> openDialog());
    }

    private void openDialog() {
        // Inflate the custom dialog layout




        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.temperature_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.temperature_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.temperature_dialog, null);

        // Find the EditText fields
        final EditText minTemperatureEditText = dialog.findViewById(R.id.minTemperatureEditText);
        final EditText maxTemperatureEditText = dialog.findViewById(R.id.maxTemperatureEditText);
        final TextView turnOnLightET = dialog.findViewById(R.id.turn_on_light_ET);
        final TextView turnOffLightET = dialog.findViewById(R.id.turn_off_light_ET);
        final CardView cancelCardView = dialog.findViewById(R.id.cancel_card_view);
        final CardView okCardView = dialog.findViewById(R.id.ok_card_view);

        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                selectedHour[0] = hourOfDay;
                selectedMinute[0] = minute;

                switch (selected_option){
                    case INDOOR_LIGHT_ON_TIME:
                        selectedHour[selected_option] = hourOfDay;
                        selectedMinute[selected_option] = minute;
                        turnOnLightET.setText(selectedHour[0] + ":" + String.format("%02d", selectedMinute[0]));
                        break;
                    case INDOOR_LIGHT_OFF_TIME:
                        selectedHour[selected_option] = hourOfDay;
                        selectedMinute[selected_option] = minute;
                        turnOffLightET.setText(selectedHour[0] + ":" + String.format("%02d", selectedMinute[0]));
                        break;

                }


            }
        };

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder//setTitle("Enter Temperature Range")
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the input values
                        String minTemp = minTemperatureEditText.getText().toString();
                        String maxTemp = maxTemperatureEditText.getText().toString();


                        // Display the values or use them as needed
                        if (!minTemp.isEmpty() && !maxTemp.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Min Temp: " + minTemp + ", Max Temp: " + maxTemp, Toast.LENGTH_SHORT).show();
                            websocket.send("min temp," + minTemp);
                            websocket.send("max temp," + maxTemp);

                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter both temperatures", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        // Show the dialog
        turnOnLightET.setOnClickListener(v->{
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
            selected_option = INDOOR_LIGHT_ON_TIME;
            timePickerDialog.show();
        });

        turnOffLightET.setOnClickListener(v->{
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
            selected_option = INDOOR_LIGHT_OFF_TIME;
            timePickerDialog.show();
        });
        okCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String minTemp = minTemperatureEditText.getText().toString();
                String maxTemp = maxTemperatureEditText.getText().toString();

                if (!minTemp.isEmpty() && !maxTemp.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Min Temp: " + minTemp + ", Max Temp: " + maxTemp, Toast.LENGTH_SHORT).show();
                    websocket.send("min temp," + minTemp);
                    websocket.send("max temp," + maxTemp);
                }
                dialog.dismiss();

            }
        });
        cancelCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        dialog.show();
//          builder.create().show();




//        timePicker.show();



    }

    private void parseJsonData(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            String fan = json.getString("fan");
            String heater = json.getString("heater");
            String indoorBulb = json.getString("indoor_bulb");
            String outdoorBulb = json.getString("outdoor_bulb");
            boolean fire = json.getBoolean("fire");
            boolean intruder = json.getBoolean("intruder");
            double temperature = json.getDouble("temperature");

            System.out.println("Fan: " + fan);
            System.out.println("Heater: " + heater);
            System.out.println("Indoor Bulb: " + indoorBulb);
            System.out.println("Outdoor Bulb: " + outdoorBulb);
            System.out.println("Fire: " + fire);
            System.out.println("Intruder: " + intruder);
            System.out.println("Temperature: " + temperature);
            temperatureValue.setText(temperature + " °C");
            progressBar.setProgress((int) (temperature * 10));

            if (temperature>25){
                temperatureLabel.setText("Heating");
                temperatureImage.setImageDrawable(getResources().getDrawable(R.drawable.sun_icon));
            }else{
                temperatureLabel.setText("Cooling");
                temperatureImage.setImageDrawable(getResources().getDrawable(R.drawable.snow_flake));
            }

            if (AUTOMATIC_CONTROL){
                if (fan.equals("on")) {
                    fanSwitch.setChecked(true);
                } else {
                    fanSwitch.setChecked(false);
                }

                if (outdoorBulb.equals("on")) {
                    outdoorBulbSwitch.setChecked(true);
                } else {
                    outdoorBulbSwitch.setChecked(false);
                }
                if (heater.equals("on")) {
                    heaterSwitch.setChecked(true);
                } else {
                    heaterSwitch.setChecked(false);
                }
            }


//            if (indoorBulb.equals("on")) {
//                indoorBulbSwitch.setChecked(true);
//            } else {
//                indoorBulbSwitch.setChecked(false);
//            }



            if(intruderSwitch.isChecked()){
                if (intruder){
//                intruderAlertContainer.setVisibility(View.VISIBLE);
                    slideDown(intruderAlertContainer);
                }else{
//              slideUp(intruderAlertContainer);
                }
            }


            if (fire){
                slideDown(fireAlertContainer);
            }else{
//                slideUp(fireAlertContainer);
            }

            checkTime(INDOOR_LIGHT_ON_TIME);
            checkTime(INDOOR_LIGHT_OFF_TIME);

        } catch (Exception e) {
            System.out.println("JSON Parsing Error: " + e.getMessage());
        }
    }

    void sendData(){
        indoorBulbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    indoorBulbSwitch.setChecked(true);
                    websocket.send("indoor_bulb_on");
                } else {
                    indoorBulbSwitch.setChecked(false);
                    websocket.send("indoor_bulb_off");
                }
            }
        });

        outdoorBulbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    outdoorBulbSwitch.setChecked(true);
                    websocket.send("outdoor_bulb_on");
                } else {
                    outdoorBulbSwitch.setChecked(false);
                    websocket.send("outdoor_bulb_off");
                }
            }
        });

        heaterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    heaterSwitch.setChecked(true);
                    websocket.send("heater_on");
                } else {
                    heaterSwitch.setChecked(false);
                    websocket.send("heater_off");
                }
            }
        });

        fanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fanSwitch.setChecked(true);
                    websocket.send("fan_on");
                } else {
                    fanSwitch.setChecked(false);
                    websocket.send("fan_off");
                }
            }
        });

        intruderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    intruderSwitch.setChecked(true);
                    websocket.send("intruder_detection_on");
                } else {
                    intruderSwitch.setChecked(false);
                    websocket.send("intruder_detection_off");
                }
            }
        });

        doorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    doorSwitch.setChecked(true);
                    websocket.send("door open");
                } else {
                    doorSwitch.setChecked(false);
                    websocket.send("door close");
                }
            }
        });
    }
    public void slideUp(View view) {

        view.animate()
                .translationY(-view.getHeight()) // Move up
                .alpha(0f) // Fade out
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
        stopAlarm();
    }
    public void slideDown(View view) {
        view.setAlpha(0f);
        view.setTranslationY(-view.getHeight()); // Start from top
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0f) // Move down
                .alpha(1f) // Fade in
                .setDuration(300)
                .setListener(null);
        startAlarm();
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Use free form speech input
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Set language
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Prompt
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
//                textView.setText(result.get(0));
                analyzeAndSendCommand(result.get(0));
                Toast.makeText(this, result.get(0), Toast.LENGTH_SHORT).show();// Display the first recognized result
            }
        }
    }
    public void analyzeAndSendCommand(String inputText) {
        inputText = inputText.toLowerCase(Locale.ROOT);

        if (inputText.contains("fan on")) {
//            sendCommand("fan_on");
            fanSwitch.setChecked(true);
        } else if (inputText.contains("fan off")) {
            fanSwitch.setChecked(false);
        } else if (inputText.contains("heater on")) {
            heaterSwitch.setChecked(true);
        } else if (inputText.contains("heater off")) {
            heaterSwitch.setChecked(false);
        } else if (inputText.contains("indoor light on")) {
            indoorBulbSwitch.setChecked(true);
        } else if (inputText.contains("indoor light off")) {
            indoorBulbSwitch.setChecked(false);
        } else if (inputText.contains("outdoor light on")) {
            outdoorBulbSwitch.setChecked(true);
        } else if (inputText.contains("outdoor light off")) {
            outdoorBulbSwitch.setChecked(false);
        } else if (inputText.contains("automatic on")) {
            automaticControlSwitch.setChecked(true);
        } else if (inputText.contains("automatic off")) {
            automaticControlSwitch.setChecked(false);
        } else if (inputText.contains("intruder detection on")) {
            intruderSwitch.setChecked(true);
        } else if (inputText.contains("intruder detection off")) {
            intruderSwitch.setChecked(false);
        } else if (inputText.contains("door open")) {
            doorSwitch.setChecked(true);
        } else if (inputText.contains("door close")) {
            doorSwitch.setChecked(false);
        } else if (inputText.contains("stop intruder alert")) {
//            sendCommand("stop intruder alert");
        } else if (inputText.contains("stop fire alert")) {
//            sendCommand("stop fire alert");
        } else {
            System.out.println("No matching command found.");
        }
    }
    private void sendCommand(String command) {
            websocket.send(command);
            System.out.println("Sent command: " + command);
    }

    void startAlarm(){
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_siren_sound_effect); // alert.mp3 should be in res/raw
        mediaPlayer.setLooping(true); // 🔁 Loop forever
        mediaPlayer.start();
    }

    void stopAlarm(){
//        mediaPlayer.pause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    void checkTime(int option){

        if (selectedHour[option] != 0 && selectedMinute[option] != 0
        ) {
            int targetHour = selectedHour[option];
            int targetMinute = selectedMinute[option];


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalTime now = LocalTime.now();
                LocalTime targetTime = LocalTime.of(targetHour, targetMinute);
                if (targetTime.isBefore(now)) {
                    if (selected_option == INDOOR_LIGHT_ON_TIME) {
                        Toast.makeText(this, "bulb on.", Toast.LENGTH_SHORT).show();
                        indoorBulbSwitch.setChecked(true);
                    } else if (selected_option == INDOOR_LIGHT_OFF_TIME) {
                        indoorBulbSwitch.setChecked(false);
                    }
                    Log.d("TimeCheck", "The target time has already passed.");
                } else {
                    Log.d("TimeCheck", "The target time has not yet passed.");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        stopAlarm();
    }
}
