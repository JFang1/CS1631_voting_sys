package tdr.inputprocessor;

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

    public static final String TAG = "VotingSoftware";

    private static Button connectToServerButton,registerToServerButton;

    private EditText serverIp,serverPort, scope, passcode;

    //A Thread object that used to control the connection with the SIS server.
    static ComponentSocket client;

    private static TextView messageReceivedListText;

    private static final String SENDER = "VotingSoftware";


    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED =  "Disconnect";
    private static String SCOPE, RECEIVER, SAVED_PASS;

    private KeyValueList readingMessage;

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int MESSAGE_RECEIVED = 3;

    private static ArrayList<String> VoterTable = new ArrayList<>();
    private static Map<String, VoteItem> TallyTable = new HashMap<>();
    private static boolean open = false;

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
                    //str = (String)msg.obj;
                    KeyValueList list = null;
                    String str, message, temp;

                    str = ((KeyValueList)msg.obj).toString();
                    messageReceivedListText.append("\n********************" + str);
                    final int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
                    if (scrollAmount > 0)
                        messageReceivedListText.scrollTo(0, scrollAmount);
                    else
                        messageReceivedListText.scrollTo(0, 0);

                    message = ((KeyValueList)msg.obj).getValue("Message");
                    RECEIVER = ((KeyValueList)msg.obj).getValue("Sender");
                    temp = ((KeyValueList)msg.obj).getValue("Passcode");

                    switch (message) {
                        case "Vote":
                            String VoterPhone, CandidateID;

                            VoterPhone = ((KeyValueList)msg.obj).getValue("VoterPhone");
                            CandidateID = ((KeyValueList)msg.obj).getValue("CandidateID");

                            if (!open)
                            {
                                list = generateReplyMessage(1, message);
                            }
                            else if (VoterPhone == null || VoterTable.contains(VoterPhone))
                            {
                                list = generateReplyMessage(2, message);
                            }
                            else if (CandidateID == null || !TallyTable.containsKey(CandidateID))
                            {
                                list = generateReplyMessage(3, message);
                            }
                            else
                            {
                                VoterTable.add(VoterPhone);
                                TallyTable.get(CandidateID).add();
                                list = generateReplyMessage(0, message);
                            }
                            list.putPair("Phone", VoterPhone);
                            break;
                        case "Kill":
                            if (temp == null || !temp.equals(SAVED_PASS))
                            {
                                list = generateReplyMessage(6, message);
                            }
                            else
                            {
                                client.killThread();
                                System.exit(0);
                            }
                            break;
                        case "Add":
                            String id = ((KeyValueList)msg.obj).getValue("CandidateID");

                            if (temp == null || !temp.equals(SAVED_PASS))
                            {
                                list = generateReplyMessage(6, message);
                            }
                            else if (TallyTable.containsKey(id))
                            {
                                list = generateReplyMessage(9, message);
                            }
                            else if (!id.equals(""))
                            {
                                TallyTable.put(id, new VoteItem(id));
                                list = generateReplyMessage(0, message);
                            }
                            else
                            {
                                list = generateReplyMessage(4, message);
                            }
                            break;
                        case "Open":
                            if (temp == null || !temp.equals(SAVED_PASS))
                            {
                                list = generateReplyMessage(6, message);
                            }
                            else if (!open)
                            {
                                open = true;
                                list = generateReplyMessage(0, message);
                            }
                            else
                            {
                                list = generateReplyMessage(7, message);
                            }
                            break;
                        case "Close":
                            if (temp == null || !temp.equals(SAVED_PASS))
                            {
                                list = generateReplyMessage(6, message);
                            }
                            else if (open)
                            {
                                open = false;
                                processTallies();
                                VoterTable.clear();
                                TallyTable.clear();
                                list = generateReplyMessage(0, message);
                            }
                            else
                            {
                                list = generateReplyMessage(8, message);
                            }
                            break;
                        default:
                            list = generateReplyMessage(5, "");
                            break;
                    }

                    if (client != null)
                    {
                        client.setMessage(list);
                    }
                    break;
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
        passcode = (EditText) findViewById(R.id.Passcode);
        messageReceivedListText = (TextView) findViewById(R.id.messageReceivedListText);
        //Pass a scrolling movement manager to the text view so that this view can be scrolled
        messageReceivedListText.setMovementMethod(ScrollingMovementMethod.getInstance());

        registerToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String temp = passcode.getText().toString();

                if(client!=null && client.isSocketAlive() && registerToServerButton.getText().toString().equalsIgnoreCase(REGISTERED)){
                    Toast.makeText(MainActivity.this,"Already registered.",Toast.LENGTH_SHORT).show();
                }
                else if(temp.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Enter Passcode.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SCOPE = scope.getText().toString();
                    SAVED_PASS = temp;
                    passcode.setText("");
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
                String temp = passcode.getText().toString();
                if (temp.equals(""))
                {
                    Toast.makeText(MainActivity.this,"Enter Passcode.",Toast.LENGTH_SHORT).show();
                }
                else if (!temp.equals(SAVED_PASS))
                {
                    Toast.makeText(MainActivity.this,"Incorrect Passcode.",Toast.LENGTH_SHORT).show();
                }
                else if(connectToServerButton.getText().toString().equalsIgnoreCase(DISCOONECTED)){
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
                passcode.setText("");
            }
        });
    }

    private static void processTallies() {
        Collection values = TallyTable.values();
        List<VoteItem> sortValues = new ArrayList<>();
        sortValues.addAll(values);
        Collections.sort(sortValues);
        Collections.reverse(sortValues);
        KeyValueList list = generateReadingMessage(sortValues);
        client.setMessage(list);
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
    static KeyValueList generateReplyMessage(int msgID, String msg) {
        KeyValueList list = new KeyValueList();
        //Set the scope of the message
        list.putPair("Scope",SCOPE);
        //Set the message type
        list.putPair("MessageType","Reading");
        //Set the sender or name of the message
        list.putPair("Sender",SENDER);
        //Set the role of the message
        list.putPair("Message","Reply to " + msg);
        list.putPair("Receiver", RECEIVER);

        switch (msgID)
        {
            case 1:
                list.putPair("Description:", "Action Denied. Voting Closed");
                break;
            case 2:
                list.putPair("Description", "Action Denied. Invalid Voter");
                break;
            case 3:
                list.putPair("Description", "Action Denied. Cannot Find Candidate");
                break;
            case 4:
                list.putPair("Description", "Action Denied. No Candidate ID");
                break;
            case 5:
                list.putPair("Description", "Action Denied. Message Unknown");
                break;
            case 6:
                list.putPair("Description", "Action Denied. Invalid Passcode");
                break;
            case 7:
                list.putPair("Description", "Action Denied. Voting Already Open");
                break;
            case 8:
                list.putPair("Description", "Action Denied. Voting Already Closed");
                break;
            case 9:
                list.putPair("Description", "Action Denied. Candidate Already Exists");
            case 0:
                list.putPair("Description", "Action Approved");
                break;
            default:
                list.putPair("Description", "Error");
                break;
        }

        return list;
    }
    //Generate a test register message, please replace something of attributes with your own.
    static KeyValueList generateReadingMessage(List values){
        KeyValueList list = new KeyValueList();
        //Set the scope of the message
        list.putPair("Scope",SCOPE);
        //Set the message type
        list.putPair("MessageType","Reading");
        //Set the sender or name of the message
        list.putPair("Sender",SENDER);
        //Set the role of the message
        list.putPair("Message","Announcement");

        //the following three attributes are necessary for sending the message to Uploader through PC SIS server.
        list.putPair("Broadcast", "True");
        list.putPair("Direction", "Up");
        list.putPair("Receiver", "Uploader");

        for (int i = 0; i < values.size(); i++)
        {
            list.putPair("Rank" + i, ((VoteItem)values.get(i)).getID() + ": " + Integer.toString(((VoteItem)values.get(i)).getTally()) + " votes");
        }

        return list;
    }

}
