package tdr.trendsanalyzer;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.widget.GridLayout.VERTICAL;

/*
    A demo Activity, which shows how to build a connection with SIS server, register itself to the server,
    and send a message to the server as well.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TrendsAnalyzer";

    private static Button connectToServerButton,registerToServerButton, refresh;

    private EditText serverIp,serverPort;
    private static TextView results;

    private static RecyclerView recyclerView;
    private static RecyclerView.Adapter adapter;

    static ComponentSocket client;

    private static final String SENDER = "TrendsAnalyzer";
    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED =  "Disconnect";
    private static final String SCOPE = "SIS.Scope1";

    private static TableList table = new TableList();
    private static HashMap<String, String> candidateMap = new HashMap<>();

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int MESSAGE_RECEIVED = 3;

    private static String RECEIVER;
    private static StringBuilder finalTally;
    private static HashMap<String, TallyItem> tallies;

    static Handler callbacks = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //String str;
            //String[] strs;
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
                    KeyValueList list = null;

                    String message = ((KeyValueList)msg.obj).getValue("Message");
                    RECEIVER = ((KeyValueList)msg.obj).getValue("Sender");
                    //temp = ((KeyValueList)msg.obj).getValue("Passcode");

                    switch (message) {
                        case "Add":
                            String newCategory = ((KeyValueList)msg.obj).getValue("Category");
                            if (newCategory.equals(""))
                                list = generateReplyMessage(2, message);
                            else if (table.contains(newCategory))
                                list = generateReplyMessage(1, message);
                            else {
                                table.addCategory(newCategory);
                                refresh.performClick();
                                list = generateReplyMessage(0, message);
                            }
                            if (client != null)
                            {
                                client.setMessage(list);
                            }
                            break;
                        case "New Vote":
                            if (candidateMap.containsKey(((KeyValueList)msg.obj).getValue("CandidateID")))
                                table.add((KeyValueList)msg.obj);
                            adapter.notifyDataSetChanged();
                            break;
                        case "Table":
                            String newID = ((KeyValueList)msg.obj).getValue("CandidateID");
                            String newType = ((KeyValueList)msg.obj).getValue("Candidate Type");
                            if (newID.equals("") || newType.equals(""))
                                list = generateReplyMessage(3, message);
                            else {
                                candidateMap.put(newID, newType);
                                list = generateReplyMessage(0, message);
                            }
                            break;
                        case "Close":
                            organizeResults();
                            break;
                        case "Open":
                            results.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            table.clear();
                            adapter.notifyDataSetChanged();
                            break;
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectToServerButton = (Button) findViewById(R.id.connectToServer);
        registerToServerButton = (Button) findViewById(R.id.registerToServerButton);
        refresh = (Button) findViewById(R.id.refresh);
        serverIp = (EditText) findViewById(R.id.serverIp);
        serverPort = (EditText) findViewById(R.id.serverPort);
        recyclerView = (RecyclerView) findViewById(R.id.messageReceivedList);
        results = (TextView) findViewById(R.id.results);

        table.addCategory("CandidateID");
        table.addCategory("Candidate Type");

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, VERTICAL, false));
        adapter = new TableAdapter(table);
        recyclerView.setAdapter(adapter);

        //addCandidateButton.setVisibility(View.GONE);
        //idLayout.setVisibility(View.GONE);
        //typeLayout.setVisibility(View.GONE);

        registerToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client!=null && client.isSocketAlive() && registerToServerButton.getText().toString().equalsIgnoreCase(REGISTERED)){
                    Toast.makeText(MainActivity.this,"Already registered.",Toast.LENGTH_SHORT).show();
                }else{
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

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, table.categories(), VERTICAL, false));
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static String organizeResults() {
        finalTally = new StringBuilder();
        tallies = new HashMap<>(candidateMap.size());

        for (int i = 0; i < table.size(); i++) {
            String value = table.get(i, 0);
            if (tallies.containsKey(value))
                tallies.get(value).increment();
            else
                tallies.put(value, new TallyItem(value, 1));
        }

        Collection values = tallies.values();
        List<TallyItem> sortValues = new ArrayList<>();
        sortValues.addAll(values);
        Collections.sort(sortValues);
        finalTally.append("Most Popular Candidate: ID ")
                .append(sortValues.get(sortValues.size() - 1).getKey())
                .append("; ")
                .append(candidateMap.get(sortValues.get(sortValues.size() - 1).getKey()))
                .append(" type with ")
                .append(sortValues.get(sortValues.size() - 1).getValue())
                .append(" votes out of ")
                .append(table.size())
                .append("\n");

        for (int i = 1; i < table.categories(); i++)
        {
            mostPopularCategory(table.getCategory(i));
        }

        recyclerView.setVisibility(View.GONE);
        results.setVisibility(View.VISIBLE);
        results.setText(finalTally);
        return finalTally.toString();
    }

    public static String mostPopularCategory(String category) {
        tallies = new HashMap<>();
        int total = 0;

        for (int i = 0; i < table.size(); i++)
        {
            String value = table.get(i, category);
            if (tallies.containsKey(value))
                tallies.get(value).increment();
            else {
                tallies.put(value, new TallyItem(value, 1));
                total++;
            }
        }
        Collection values = tallies.values();
        List<TallyItem> sortValues = new ArrayList<>();
        sortValues.addAll(values);
        Collections.sort(sortValues);
        finalTally.append("Most Popular ")
                .append(category)
                .append(" out of ")
                .append(total)
                .append(": ")
                .append(sortValues.get(sortValues.size() - 1).getKey())
                .append(" with ")
                .append(sortValues.get(sortValues.size() - 1).getValue())
                .append(" votes out of ")
                .append(table.size())
                .append("\n");
        return finalTally.toString();
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
        list.putPair("Name",SENDER);
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
                list.putPair("Description", "Action Denied. Category Already Exists");
                break;
            case 2:
                list.putPair("Description", "Action Denied. No Category Found");
                break;
            case 3:
                list.putPair("Description", "Action Denied. Missing Candidate Information");
                break;
            case 0:
                list.putPair("Description", "Action Approved");
                break;
            default:
                list.putPair("Description", "Error");
                break;
        }

        return list;
    }
}
