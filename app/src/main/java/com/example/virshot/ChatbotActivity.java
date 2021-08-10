package com.example.virshot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatbotActivity extends AppCompatActivity {

    private EditText editText;
    ImageButton sendBtn;
    final int USER = 0;
    final int BOT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        editText = findViewById(R.id.edittext_chatbox);
        sendBtn = findViewById(R.id.send_button);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                OkHttpClient okHttpClient = new OkHttpClient();

//                 Retrofit turns your HTTP API into a Java interface.
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://c7c8f2c8cf59.ngrok.io/webhooks/rest/")
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                UserMessage userMessage = null;
                if (msg.trim().isEmpty()) {
                    Toast.makeText(ChatbotActivity.this, "Please enter your query!", Toast.LENGTH_LONG).show();
                } else {
                    showTextView(msg, USER);
                    editText.setText("");

                    userMessage = new UserMessage("User",msg);
                }
                Toast.makeText(ChatbotActivity.this, ""+ userMessage.getMessage(), Toast.LENGTH_LONG).show();
                MessageSender messageSender = retrofit.create(MessageSender.class);
                Call<List<BotResponse>> response = messageSender.sendMessage(userMessage);
                response.enqueue(new Callback<List<BotResponse>>() {
                @Override
                public void onResponse(Call<List<BotResponse>> call, Response<List<BotResponse>> response) {
                    if(response.body() == null || response.body().size() == 0){
                        showTextView("Sorry didn't understand",BOT);
                    }
                    else{
                        BotResponse botResponse = response.body().get(0);
                        showTextView(botResponse.getText(),BOT);
                    }
                }
                @Override
                public void onFailure(Call<List<BotResponse>> call, Throwable t) {
                    showTextView("Waiting for message",BOT);
                    Toast.makeText(ChatbotActivity.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                }
                });
            }
            });
    }

    // Shifting to user and Bot layouts

    private void showTextView(String message, int type) {
        LinearLayout chatLayout = findViewById(R.id.chat_layout);
        LayoutInflater inflater = LayoutInflater.from(ChatbotActivity.this);
        FrameLayout layout;
        switch (type) {
            case USER:
                layout = getUserLayout();
                break;
            case BOT:
                layout = getBotLayout();
                break;
            default:
                layout = getBotLayout();
                break;
        }
        layout.setFocusableInTouchMode(true);
        chatLayout.addView(layout);
        TextView tv = layout.findViewById(R.id.chat_msg);
        tv.setText(message);
        layout.requestFocus();
        editText.requestFocus();
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm aa",
                Locale.ENGLISH);
        String time = dateFormat.format(date);
        TextView timeTextView = layout.findViewById(R.id.message_time);
        timeTextView.setText(time.toString());
    }


    // Getter functions

    FrameLayout getUserLayout() {
        LayoutInflater inflater = LayoutInflater.from(ChatbotActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.activity_user, null);
    }

    FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(ChatbotActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.activity_bot, null);
    }
}

