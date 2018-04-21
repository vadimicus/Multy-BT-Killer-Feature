package com.vadimicus.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.companion.BluetoothDeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.vadimicus.bluetoothtest.utils.Constants;
import com.vadimicus.bluetoothtest.utils.OnReceiverClick;
import com.vadimicus.bluetoothtest.utils.Receiver;
import com.vadimicus.bluetoothtest.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final String serverURL = "http://hack.multy.io";

    Button butReceive, butSend;
    EditText etAmount;
    TextView tvStatus;

    public static String status;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    BluetoothAdapter btAdapter;
    public static int REQUEST_BLUETOOTH = 1;

    private AdvertiseCallback mAdvertiseCallback;
    private ScanCallback discoveryCallback;

    private boolean isReceiving = false;
    private boolean isDiscovering = false;

    public static ArrayList<Receiver> devices = new ArrayList<>();

    private Socket socket;

    private OnReceiverClick onReceiverClick = new OnReceiverClick() {
        @Override
        public void clicked(Receiver receiver) {
            Log.d("SOCKET", "PAY TO RECEIVER CLICKED:"+receiver.getUserCode());
            if (socket!=null && socket.connected()){

                JSONObject paymentData = new JSONObject();

                try {
                    paymentData.put("from_id", Constants.SENDER_ID );
                    paymentData.put("to_id", receiver.getUserCode());
                    paymentData.put("currency_id", receiver.getCurrencyId());
                    paymentData.put("amount", receiver.getAmount());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("SOCKET", "PAY TO RECEIVER CLICKED DATA:"+paymentData.toString());

                if (paymentData!=null && paymentData.has("amount")){
                    socket.emit(Constants.EVENT_PAY, paymentData, new Ack() {
                        @Override
                        public void call(Object... args) {
                            Log.d("SOCKET", "PAYMENT RESPONCE:"+ args[0].toString());
                        }
                    });
                } else {
                    Log.e("SOCKET", "WRONG BUILD PAYMENT");
                }

            }
        }
    };


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("SOCKET", "Connected:");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MainActivity.this, "Connected to WS!!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("SOCKET", "Disconnected:"+args.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MainActivity.this, "Disconnected from WS!!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onNewReceiver = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            Log.d("SOCKET","GOT NEW RECEIVER:"+args[0].toString());

            Receiver receiver = null;

            try {
                JSONObject inData = new JSONObject(args[0].toString());

                receiver = new Receiver(
                        inData.getString("user_id"),
                        inData.getInt("currency_id"),
                        inData.getLong("amount"),
                        inData.getString("user_code")
                );



            } catch (JSONException e) {
                e.printStackTrace();
            }
            boolean changed = false;
            if (receiver != null){

                for (int i =0; i < devices.size();i++){
                    if (devices.get(i).getUserCode().equals(receiver.getUserCode())){
                        devices.remove(i);
                        devices.add(receiver);
                        changed = true;
                        i = devices.size();
                    }
                }

//                for (Receiver visibleRes : devices){
//                    if (visibleRes.getUserCode().equals(receiver.getUserCode())){
//                        devices.remove(visibleRes);
//                        devices.add(receiver);
//                        changed = true;
//                    }
//                }
            }



            if (changed){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
    //                    Log.d("SOCKET", args.toString());
                    }
                });
            }


        }
    };


    private Emitter.Listener onPaymentSend = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("SOCKET", "PaymentReceived:"+args[0].toString());



//            Toast.makeText(MainActivity.this, "Got Payment\nfrom: "+from +"\nby Code: "+to+"\nAmount: "+amount , Toast.LENGTH_SHORT).show();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String from = null;
                    String to = null;
                    long amount = 0;

                    try {
                        JSONObject inData = new JSONObject(args[0].toString());

                        from = inData.getString("from_id");
                        to = inData.getString("to_id");
                        amount = inData.getLong("amount");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(MainActivity.this, "Got Payment\nfrom: "+from +"\nby Code: "+to+"\nAmount: "+amount , Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initializeViews();

        mAdvertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);

                Log.d("ADVERTIZING", "\n\n\n\nEVERYTHING IS AWESOME MAAAAN\n\n\n\n\n");
            }
            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);

                String errMSG = null;

                switch (errorCode){
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errMSG = "Already started";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errMSG = "data too large";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errMSG = "Feature not supported";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errMSG = "Internal error";
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errMSG = "too many advertizers";
                        break;

                }
                Log.d("ADVERTIZING", "\n\n\n\nEVERYTHING BAAAAD MAAAAN\n\n\n\n\n"+errMSG + "\n\n\n\n");
            }
        };

    }

    @Override
    protected void onResume(){
        super.onResume();

        initializeSockets();
        checkAndLaunchBlueTooth();




    }


    @Override
    protected void onPause(){
        super.onPause();

        stopDiscovering();
        closeSockets();
    }


    private void startDiscoveryDevices(){

//        TODO
        discoveryCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

//                Log.d("LE SCAN", "RESULT IS:"+ result.getDevice().getName());


                ParcelUuid uuids[] = result.getDevice().getUuids();
                String stringUuids = "All Uuids\n";


                List<ParcelUuid> uuidss = result.getScanRecord().getServiceUuids();
                if (uuidss!=null && uuidss.size() > 0){
                    for (ParcelUuid uuid : uuidss){
                        Log.d("RECEIVED UUID", uuid.getUuid().toString());

                        String sendUUID = uuid.getUuid().toString();
                        sendUUID = sendUUID.substring(sendUUID.length() - 8);
                        if (!haveReceiver(sendUUID)){
//                        if (!devices.contains()){

//                            sendUUID = sendUUID.substring(sendUUID.length() - 6);

                            Log.d("RECEIVED UUID", "CUTTED UUID:"+sendUUID);




//                            devices.add(uuid.getUuid().toString());
                            devices.add(new Receiver(null, -1,0, sendUUID));

                            mAdapter.notifyDataSetChanged();

                            JSONObject toSend = new JSONObject();

                            try {
                                toSend.put("user_code", sendUUID);
                                toSend.put("user_id",Constants.SENDER_ID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            socket.emit(Constants.EVENT_SENDER_ON, toSend, new Ack() {
                                @Override
                                public void call(Object... args) {
                                    if (args[0]!=null)
                                        Log.d("SOCKET", "I GOT ACK:"+args[0].toString());
                                    else
                                        Log.d("SOCKET", "I GOT NO ACK:");
                                }
                            });

                        }
                    }
                }
            }
        };

        btAdapter.getBluetoothLeScanner().startScan(discoveryCallback);

    }

    private void checkAndLaunchBlueTooth(){

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (btAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (!btAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        btAdapter.enable();
    }



    private void initializeViews(){


        butSend = (Button) findViewById(R.id.butSend);
        butReceive = (Button) findViewById(R.id.butReceive);

        etAmount = (EditText) findViewById(R.id.etAmount);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        butSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isDiscovering){
                    startDiscoveryDevices();
                    Log.d("BLUETOOTH","START DISCOVERING" );
                } else {
                    Log.d("BLUETOOTH","STOP DISCOVERING" );

                    devices.clear();
                    mAdapter.notifyDataSetChanged();

                    stopDiscovering();
                }

                isDiscovering = !isDiscovering;
            }
        });

        butReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothLeAdvertiser advertiser = btAdapter.getBluetoothLeAdvertiser();

                String strAmount = etAmount.getText().toString();

                boolean changeStatus = false;

                if (!isReceiving && strAmount != null && strAmount.length() > 0){

                    int amount = Integer.parseInt(strAmount);
                    String code = Utils.generateCode();
                    Log.d("GOT CODE", ""+code);
                    AdvertiseSettings settings = Utils.buildAdvertiseSettings();
                    AdvertiseData data = Utils.buildAdvertiseData(code);
                    advertiser.startAdvertising(settings, data, mAdvertiseCallback );
                    status = "Receiving with code:" + code + ", amount:"+amount;
                    tvStatus.setText(status);

                    JSONObject toSend = new JSONObject();
                    try{
                        toSend.put("user_id", Constants.RECEIVER_ID);
                        toSend.put("currency_id", 0);
                        toSend.put("amount", amount);
                        toSend.put("user_code",code);

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    socket.emit(Constants.EVENT_RECEIVER_ON, toSend, new Ack() {
                        @Override
                        public void call(Object... args) {
                            Log.d("SOCKET", "RECEIVER ON ACK:"+ args[0].toString());
                        }
                    });

                    changeStatus = true;

                } else if (isReceiving){
                    if (advertiser != null){
                        advertiser.stopAdvertising(mAdvertiseCallback);
//                        Toast.makeText(MainActivity.this, "STOPED", Toast.LENGTH_SHORT).show();
                        status = "Unactive";
                        tvStatus.setText(status);
                        changeStatus = true;
                        etAmount.setText("");
                    }
                } else if (strAmount == null ){
                    Toast.makeText(MainActivity.this, "Please Enter Amount",Toast.LENGTH_SHORT).show();
                }

                if (changeStatus)
                    isReceiving = !isReceiving;


            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new SendAdapter(devices, onReceiverClick);
        mRecyclerView.setAdapter(mAdapter);


//        mRecyclerView.setOn
    }


    private void initializeSockets(){
        try {
            IO.Options options = new IO.Options();
            options.path = "/socket.io";
            options.transports = new String[] { WebSocket.NAME };
            socket = IO.socket(serverURL, options);
        } catch (URISyntaxException e){
            Log.e("SOCKET","Wrong url:"+e);
        }


        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Constants.EVENT_NEW_RECEIVER, onNewReceiver);
        socket.on(Constants.EVENT_PAYMENT_SEND, onPaymentSend);
        socket.connect();
    }


    private void stopDiscovering(){
        if (btAdapter!=null){
            btAdapter.cancelDiscovery();

            btAdapter.getBluetoothLeScanner().stopScan(discoveryCallback);
        }
    }

    private void closeSockets(){
        if (socket!=null){
            socket.disconnect();
            socket.off(Constants.CONNECTED);
            socket.off(Constants.NEW_SENDER);
            socket.off(Constants.EVENT_NEW_RECEIVER);
            socket.off(Constants.EVENT_PAYMENT_SEND);
        }
    }

    private boolean haveReceiver(String code){
        boolean contain = false;
        for (Receiver receiver : devices){
            if (receiver.getUserCode().equals(code)) {
                contain = true;
            }
        }
        Log.d("BLUETOUTH", "HAVE DEVICE:"+contain+ " CODE:"+code);
        return contain;
    }

}
