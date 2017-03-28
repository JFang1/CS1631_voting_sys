package tdr.smsreceiver;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/*
    A demo Activity, which shows how to build a connection with SIS server, register itself to the server,
    and send a message to the server as well.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "SmsReceiver";

    private static Button connectToServerButton,registerToServerButton;

    private EditText serverIp,serverPort, scope;

    //A Thread object that used to control the connection with the SIS server.
    static ComponentSocket client;

    private static TextView messageReceivedListText;

    private static final String SENDER = "SmsReceiver";


    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED =  "Disconnect";
    private static String SCOPE;

    private KeyValueList readingMessage;

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int MESSAGE_RECEIVED = 3;

    private static MainActivity inst;
    SmsManager manager = SmsManager.getDefault();

    private static final int REQUEST_READ_SMS = 1;
    private static final int REQUEST_SEND_SMS = 2;
    private static final int REQUEST_RECEIVE_SMS = 3;

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
                    KeyValueList kvList = (KeyValueList) msg.obj;
                    if (kvList.getValue("Message").equals("Reply to Vote"))
                    {
                        String result = kvList.getValue("Description");
                        String phone = kvList.getValue("Phone");
                        String str = "New vote from " + phone + "\n" + result + "\n";
                        messageReceivedListText.append("\n********************\n" + str);

                        final int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
                        if (scrollAmount > 0)
                            messageReceivedListText.scrollTo(0, scrollAmount);
                        else
                            messageReceivedListText.scrollTo(0, 0);

                        instance().sendSMS(phone, result);
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectToServerButton = (Button) findViewById(R.id.connectToServer);
        registerToServerButton = (Button) findViewById(R.id.registerToServerButton);
        serverIp = (EditText) findViewById(R.id.serverIp);
        serverPort = (EditText) findViewById(R.id.serverPort);
        scope = (EditText) findViewById(R.id.scope);
        messageReceivedListText = (TextView) findViewById(R.id.messageReceivedListText);
        //Pass a scrolling movement manager to the text view so that this view can be scrolled
        messageReceivedListText.setMovementMethod(ScrollingMovementMethod.getInstance());

        getPermissionToReadSMS();
        getPermissionToReceiveSMS();
        getPermissionToSendSMS();

        registerToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                }
            }
        });

    }

    public void handleInfo(String phone, String message) {
        KeyValueList list;

        if (!message.equals(""))
        {
            String[] lines = message.split("\\n");

            if (lines[0].trim().equalsIgnoreCase("Vote"))
            {
                if (lines.length != 2)
                {
                    sendSMS(phone, "Error: Unreadable Vote.\nPlease send votes as:\nVote\n(Candidate ID)");

                }
                else
                {
                    list = generateVoteMessage(phone, lines[1].trim());
                    if (client != null)
                    {
                        client.setMessage(list);
                    }
                }
            }
        }
    }

    public void sendSMS(String phone, String msg) {
        manager.sendTextMessage(phone, null, msg, null, null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToReadSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)) {

                }
            }

            requestPermissions(new String[]{Manifest.permission.READ_SMS},REQUEST_READ_SMS);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToReceiveSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {

                }
            }

            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},REQUEST_RECEIVE_SMS);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToSendSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {

                }
            }

            requestPermissions(new String[]{Manifest.permission.SEND_SMS},REQUEST_SEND_SMS);
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == REQUEST_READ_SMS) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "READ_SMS permission granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


        if(requestCode == REQUEST_SEND_SMS){
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SEND_SMS permission granted", Toast.LENGTH_SHORT).show();
            }

        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


        if(requestCode == REQUEST_RECEIVE_SMS){
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "RECEIVE_SMS permission granted", Toast.LENGTH_SHORT).show();
            }

        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
        list.putPair("Role","Basic");
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
        list.putPair("Role","Basic");
        //Set the name of the component
        //list.putPair("Name",SENDER);
        return list;
    }

    KeyValueList generateVoteMessage(String phone, String id){
        KeyValueList list = new KeyValueList();
        //Set the scope of the message
        list.putPair("Scope",SCOPE);
        //Set the message type
        list.putPair("MessageType","Reading");
        //Set the sender or name of the message
        list.putPair("Sender",SENDER);
        //Set the role of the message
        list.putPair("Message", "Vote");
        list.putPair("VoterPhone",phone);
        list.putPair("CandidateID", id);
        return list;
    }
}
