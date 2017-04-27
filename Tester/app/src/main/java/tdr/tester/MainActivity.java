package tdr.tester;

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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/*
    A demo Activity, which shows how to build a connection with SIS server, register itself to the server,
    and send a message to the server as well.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Tester";

    private static Button connectToServerButton,registerToServerButton;

    private EditText serverIp,serverPort, scope;

    //A Thread object that used to control the connection with the SIS server.
    static ComponentSocket client;

    private static TextView messageReceivedListText;

    private static final String SENDER = "Tester";

    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED =  "Disconnect";
    private static String SCOPE, RESULT;

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
                    String str = msg.obj.toString();
                    messageReceivedListText.append("\n********************" + str);
                    final int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
                    if (scrollAmount > 0)
                        messageReceivedListText.scrollTo(0, scrollAmount);
                    else
                        messageReceivedListText.scrollTo(0, 0);
                    KeyValueList list = (KeyValueList)msg.obj;

                    String message = list.getValue("Message");

                    if (message.equals("New"))
                    {
                        if (RESULT == null)
                            messageReceivedListText.append("\n********************\nError: No Expected Test Result Saved");
                        else if (list.getValue("Result").equals(RESULT))
                            messageReceivedListText.append("\n********************\nTest Results Approved");
                        else
                            messageReceivedListText.append("\n********************\nTest Results Failed");
                    }
                    else if(message.equals("Preset"))
                    {
                        RESULT = list.getValue("Result");
                    }
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
        serverIp = (EditText) findViewById(R.id.serverIp);
        serverPort = (EditText) findViewById(R.id.serverPort);
        scope = (EditText) findViewById(R.id.scope);
        messageReceivedListText = (TextView) findViewById(R.id.messageReceivedList);
        //Pass a scrolling movement manager to the text view so that this view can be scrolled
        messageReceivedListText.setMovementMethod(ScrollingMovementMethod.getInstance());
        //Pass a scrolling movement manager to the text view so that this view can be scrolled

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
                    }, 500);
                }
            }
        });
        connectToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
}
