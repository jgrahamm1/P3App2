package com.p3app2.Chat_Window;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.p3app2.HomeFragment;
import com.p3app2.MessageReceiverService;
import com.p3app2.R;
import com.p3app2.XMPPConnections;

import org.jivesoftware.smack.SmackException;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatWindowActivity extends AppCompatActivity {


    private EditText messageET;
    private static ListView messagesContainer;
    private Button sendBtn;
    private static ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    private Button finishChat;
    private Button switchCounselor;
/*    private int studentChatCounter = 0;
    private int counselorChatCounter=0;*/
    private int globalMsgCounter=0;
    HomeFragment homeFragment = new HomeFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent ir  = new Intent(this, MessageReceiverService.class);
        startService(ir);
        setContentView(R.layout.chat_window);
        try {
            XMPPConnections connections = new XMPPConnections();
            initControls();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setTitle("Chat with a Counselor");
    }

    private void initControls() throws SmackException.NotConnectedException {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        finishChat = (Button) findViewById(R.id.finishChatButton);
        switchCounselor = (Button) findViewById(R.id.finishChatButton2);
        globalMsgCounter =0;
        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView companionLabel = (TextView) findViewById(R.id.friendLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        companionLabel.setText("");// Hard Coded
        loadDummyHistory();

        ////


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(Integer.toString(globalMsgCounter++));

                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setIsStudent(true);

                try {
                    XMPPConnections.sendMessage(chatMessage.getMessage());
                    displayMessage(chatMessage);

                }
                catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

                messageET.setText("");


            }
        });


        finishChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ////Save all the conversations to flat file

                MessageReceiverService.unsetNotification();
                serializeChat();
                finish();

            }
            });

        switchCounselor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ////Save all the conversations to flat file

                MessageReceiverService.unsetNotification();
                serializeChat();
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder
                        .setMessage("About to switch Counselor - Please confirm")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                        /*Chat Window Intent Code goes here*/
                                finish();
                                Intent intent = new Intent(v.getContext(), ChatWindowActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
               /* Intent intent = new Intent(v.getContext(), ChatWindowActivity.class);
                startActivity(intent);
*/
                //insert new conversatin logic here

                //finish();
            }
            });
    }

    public static void displayMessage(ChatMessage message)
    {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    public static void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);

        messagesContainer.setSelection(adapter.getCount() - 1);
    }

    private void serializeChat() {
        List<ChatMessage> conversation = adapter.getChat();
        //assign a chat record to serialize
        ChatRecord record = new ChatRecord();
        record.setChatMessages(conversation);
        record.setNumberOfMessages(conversation.size());
        record.setDateTimeStarted(conversation.get(0).getDate());
        record.setDateTimeEnded(conversation.get(conversation.size()-1).getDate());

        Gson gson = new Gson();
        String finalRecord = gson.toJson(record);
        FileOutputStream outputStream;
        String filename = "ChatRecord1.txt";
        Log.d("finalrecord sample here","");
        Log.d(finalRecord,"");
        Log.d("finalrecord ends here","");
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(finalRecord.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error in writing GSON","");
        }



      /*  for(ChatMessage cm: conversation)
        {
            Log.d(cm.getId(), cm.getMessage());
        }*/

    }



    private void loadDummyHistory() throws SmackException.NotConnectedException {

        chatHistory = new ArrayList<ChatMessage>();

        ChatMessage msg = new ChatMessage();
        msg.setId(String.valueOf(globalMsgCounter++));

        msg.setIsStudent(false);
        msg.setMessage("Hello, Welcome to the Dick's House Online Counseling.");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(String.valueOf(globalMsgCounter++));
        msg1.setIsStudent(false);
        msg1.setMessage("I am your Counselor Tabby. What's bothering you today?");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);

        adapter = new ChatAdapter(ChatWindowActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i<chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }
}
