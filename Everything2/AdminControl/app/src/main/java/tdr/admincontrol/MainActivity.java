package tdr.admincontrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Collections;

/*
    A demo Activity, which shows how to build a connection with SIS server, register itself to the server,
    and send a message to the server as well.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "AdminControl";

    private static Button connectToServerButton,registerToServerButton,sendButton;

    private EditText serverIp,serverPort, scope, type, receiver, message,passcode,candidateID;

    //A Thread object that used to control the connection with the SIS server.
    static ComponentSocket client;

    private static TextView messageReceivedListText;

    private static LinearLayout ipLayout, portLayout, scopeLayout, typeLayout, receiverLayout, messageLayout, passLayout, idLayout;

    private static final String SENDER = "AdminControl";


    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED =  "Disconnect";
    private static String SCOPE;

    private KeyValueList readingMessage;

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int MESSAGE_RECEIVED = 3;

    //The object is passed to the socket thread and used as callbacks to update UI.
    static Handler callbacks = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case CONNECTED:
                    registerToServerButton.setText(REGISTERED);
                    Log.e(TAG, "===============================================================CONNECTED" );
                    break;
                case DISCONNECTED:
                    connectToServerButton.setText("Connect");
                    Log.e(TAG, "===============================================================DISCONNECTED" );
                    break;
                case MESSAGE_RECEIVED:
                    String str = (String)msg.obj;
                    messageReceivedListText.append("\n********************" + str);
                    final int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
                    if (scrollAmount > 0)
                        messageReceivedListText.scrollTo(0, scrollAmount);
                    else
                        messageReceivedListText.scrollTo(0, 0);
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectToServerButton = (Button) findViewById(R.id.connectToServer);
        registerToServerButton = (Button) findViewById(R.id.registerToServerButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        serverIp = (EditText) findViewById(R.id.serverIp);
        serverPort = (EditText) findViewById(R.id.serverPort);
        scope = (EditText) findViewById(R.id.scope);
        type = (EditText) findViewById(R.id.messageTypeContent);
        receiver = (EditText) findViewById(R.id.receiverContent);
        message = (EditText) findViewById(R.id.messageContent);
        passcode = (EditText) findViewById(R.id.passContent);
        candidateID = (EditText) findViewById(R.id.idContent);
        messageReceivedListText = (TextView) findViewById(R.id.messageReceivedListText);
        //Pass a scrolling movement manager to the text view so that this view can be scrolled
        messageReceivedListText.setMovementMethod(ScrollingMovementMethod.getInstance());
        //Pass a scrolling movement manager to the text view so that this view can be scrolled
        ipLayout = (LinearLayout) findViewById(R.id.serverIpLayout);
        portLayout = (LinearLayout) findViewById(R.id.serverPortLayout);
        scopeLayout = (LinearLayout) findViewById(R.id.scopeLayout);
        typeLayout = (LinearLayout) findViewById(R.id.messageTypeLayout);
        receiverLayout = (LinearLayout) findViewById(R.id.receiverLayout);
        messageLayout = (LinearLayout) findViewById(R.id.messageLayout);
        passLayout = (LinearLayout) findViewById(R.id.passLayout);
        idLayout = (LinearLayout) findViewById(R.id.idLayout);

        sendButton.setVisibility(View.GONE);
        typeLayout.setVisibility(View.GONE);
        receiverLayout.setVisibility(View.GONE);
        messageLayout.setVisibility(View.GONE);
        passLayout.setVisibility(View.GONE);
        idLayout.setVisibility(View.GONE);

        registerToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String temp = passcode.getText().toString();

                if(client!=null && client.isSocketAlive() && registerToServerButton.getText().toString().equalsIgnoreCase(REGISTERED)){
                    Toast.makeText(MainActivity.this,"Already registered.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SCOPE = scope.getText().toString();
                    client = new ComponentSocket(serverIp.getText().toString(), Integer.parseInt(serverPort.getText().toString()),callbacks);
                    client.start();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            KeyValueList list = generateRegisterMessage();
                            client.setMessage(list);
                        }
                    }, 100);
                }
            }
        });
        connectToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(MainActivity.TAG, "Sending connectToServerButton.1" );
                if(connectToServerButton.getText().toString().equalsIgnoreCase(DISCOONECTED)){
                    Log.e(MainActivity.TAG, "Sending connectToServerButton.2" );
                    client.killThread();
                    connectToServerButton.setText("Connect");
                    registerToServerButton.setVisibility(View.VISIBLE);
                    sendButton.setVisibility(View.GONE);
                    typeLayout.setVisibility(View.GONE);
                    receiverLayout.setVisibility(View.GONE);
                    messageLayout.setVisibility(View.GONE);
                    passLayout.setVisibility(View.GONE);
                    idLayout.setVisibility(View.GONE);
                    ipLayout.setVisibility(View.VISIBLE);
                    portLayout.setVisibility(View.VISIBLE);
                    scopeLayout.setVisibility(View.VISIBLE);
                }else{
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            KeyValueList list = generateConnectMessage();
                            client.setMessage(list);
                        }
                    }, 100);

                    connectToServerButton.setText(DISCOONECTED);
                    registerToServerButton.setVisibility(View.GONE);
                    sendButton.setVisibility(View.VISIBLE);
                    typeLayout.setVisibility(View.VISIBLE);
                    receiverLayout.setVisibility(View.VISIBLE);
                    messageLayout.setVisibility(View.VISIBLE);
                    passLayout.setVisibility(View.VISIBLE);
                    idLayout.setVisibility(View.VISIBLE);
                    ipLayout.setVisibility(View.GONE);
                    portLayout.setVisibility(View.GONE);
                    scopeLayout.setVisibility(View.GONE);
                }
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgType = type.getText().toString();
                String receive = receiver.getText().toString();
                String msg = message.getText().toString();
                String pass = passcode.getText().toString();
                String id = candidateID.getText().toString();

                if (client == null || !client.isSocketAlive())
                {
                    Toast.makeText(MainActivity.this,"Connect to Server First.",Toast.LENGTH_SHORT).show();
                }
                else if (msgType.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please Enter MessageType.",Toast.LENGTH_SHORT).show();
                }
                else if (receive.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please Enter Receiver.",Toast.LENGTH_SHORT).show();
                }
                else if (msg.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please Enter Message.",Toast.LENGTH_SHORT).show();
                }
                else if (pass.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Please Enter Passcode.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    KeyValueList list = new KeyValueList();
                    list.putPair("Scope", SCOPE);
                    list.putPair("MessageType", msgType);
                    list.putPair("Sender", SENDER);
                    list.putPair("Receiver", receive);
                    list.putPair("Message", msg);
                    list.putPair("Passcode", pass);
                    if (!id.equals(""))
                    {
                        list.putPair("CandidateID", id);
                    }

                    if (client != null)
                    {
                        client.setMessage(list);
                    }
                    passcode.setText("");
                    message.setText("");
                    candidateID.setText("");
                }
            }
        });

    }

    //Generate a test register message, please replace something of attributes with your own.
    KeyValueList generateRegisterMessage(){
        KeyValueList list = new KeyValueList();
        //Set the scope of the message
        list.putPair("Scope",SCOPE);
        //Set the message type
        list.putPair("MessageType","Register");
        //Set the sender or name of the message
        list.putPair("Sender",SENDER);
        //Set the role of the message
        list.putPair("Role","Controller");
        //Set the name of the component
        //list.putPair("Name",SENDER);
        return list;
    }
    //Generate a test connect message, please replace something of attributes with your own.
    KeyValueList generateConnectMessage(){
        KeyValueList list = new KeyValueList();
        //Set the scope of the message
        list.putPair("Scope",SCOPE);
        //Set the message type
        list.putPair("MessageType","Connect");
        //Set the sender or name of the message
        list.putPair("Sender",SENDER);
        //Set the role of the message
        list.putPair("Role","Controller");
        //Set the name of the component
        //list.putPair("Name",SENDER);
        return list;
    }
}
