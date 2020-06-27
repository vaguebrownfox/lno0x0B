package com.fiaxco.lno0x0b;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fiaxco.lno0x0b.bluetoothstuff.PnoiBluetoothService;
import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class RecordActivity extends AppCompatActivity implements PnoiBluetoothService.MessageConstants {

    private ContentValues mProfileValues;

    static final int MSG_START_TIMER = 0;
    static final int MSG_STOP_TIMER = 1;
    static final int MSG_UPDATE_TIMER = 2;

    private static final int REQUEST_ENABLE_BLUETOOTH = 3;

    static Stopwatch timer = new Stopwatch();
    TimerHandler mHandler = new TimerHandler();
    boolean timerRunning = false;

    TextView mTextViewProfileInfo;
    TextView mTextViewRecordTimer;
    TextView mTextViewRecLocation;

    Button recordButton;

    Button mLUL, mRUL, mLLL, mRLL;
    static String lungLocation;
    byte[] currentLoc;

    private BluetoothAdapter bluetoothAdapter;
    private UUID mUUID = UUID.fromString("93fd0c34-5cf0-4c07-8b12-06fcc82e17f0");
    private BluetoothDevice mBTDev = null;
    private BluetoothSocket mSocket = null;
    private Handler btServiceHandler;
    private ConnectThread mConnectThread = null;
    private PnoiBluetoothService.ConnectedThread mConnectedThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent intent = getIntent();
        mProfileValues = intent.getParcelableExtra(EditorActivity.EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA);

        assert mProfileValues != null;
        String title = mProfileValues.getAsString(ProfileEntry.NAME);
        setTitle(title);

        mTextViewProfileInfo = findViewById(R.id.text_view_record);
        mTextViewRecordTimer = findViewById(R.id.text_view_record_timer);
        mTextViewRecLocation = findViewById(R.id.text_view_location);

        mLUL = findViewById(R.id.button_left_upper_lung);
        mRUL = findViewById(R.id.button_right_upper_lung);
        mLLL = findViewById(R.id.button_left_lower_lung);
        mRLL = findViewById(R.id.button_right_lower_lung);
        currentLoc = new byte[] { 1, 0, 0, 0};
        lungLocButtonSelectionHandler(currentLoc, "LUL"); // initial selection


        // Record:Stop  Button
        recordButton = findViewById(R.id.button_record_start);
        recordButton.setOnClickListener(v -> {
            if ( mConnectedThread == null) {
                makeToast("Connect to pnoi");
            }
            if (!timerRunning && mConnectedThread != null) {

                mHandler.sendEmptyMessage(MSG_START_TIMER);
                lungLocButtonVisibilityHandler(currentLoc);
                recordButton.setText(R.string.record_button_stop);
                String filename = getProfileFilename(mProfileValues);
                mTextViewProfileInfo.setText(String.format("filename: %s", filename));

                String r = "record";
                mConnectedThread.write(r.getBytes());   //filename.getBytes());
                timerRunning = true;
            } else if (timerRunning) {
                recordStopHandler(false);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btServiceHandler = new BtServiceHandler();

        IntentFilter btStateBroadcast = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiverBTState, btStateBroadcast);

    }

    private void recordStopHandler(boolean SocIsBroken) {
        if (mConnectedThread != null && !SocIsBroken) {
            String s = "stop";
            mConnectedThread.write(s.getBytes());
        }

        mHandler.sendEmptyMessage(MSG_STOP_TIMER);
        lungLocButtonVisibilityHandler(new byte[] { 1, 1, 1, 1});
        recordButton.setText(R.string.record_button_record);
        timerRunning = false;
    }

    // App bar clickable
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record_bluetooth:
                btMenuIconActionHandler();
                return true;
            case R.id.action_record_bluetooth_disconnect:
                btDisconnectMenuHandler(false);
                return true;
            case R.id.action_record_download_record:
                // TODO : connected Thread read()
                btGetData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Menu bluetooth icon press handler
    private void btMenuIconActionHandler() {

        if (bluetoothAdapter == null) {
            makeToast("Device doesn't support Bluetooth");
            finish();

        } else if (!bluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);

        } else if (mBTDev == null) {

            mBTDev = getBtDevicesWithName("naomi");

        } else if (mSocket == null || mConnectedThread == null) {

            mConnectThread = new ConnectThread(mBTDev);
            mConnectThread.start();

        }
    }

    private void btDisconnectMenuHandler(boolean SocIsBroken) {
        if (mConnectThread != null) {

            recordStopHandler(SocIsBroken);
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
            }
            mConnectThread.cancel();
            makeToast("Pnoi Disconnected");

            invalidateOptionsMenu();
        }
    }

    private void btGetData() {
        if (mConnectedThread != null) {
            String d = "download";
            mConnectedThread.write(d.getBytes());
            mConnectedThread.start();
        } else {
            makeToast("Pnoi not connected");
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem btIcon = menu.findItem(R.id.action_record_bluetooth);
        if (btIcon != null) {
            if (bluetoothAdapter.isEnabled()) {
                btIcon.setIcon(R.drawable.ic_baseline_bluetooth_on_24);
            } else {
                btIcon.setIcon(R.drawable.ic_baseline_bluetooth_before_con_24);
            }

            if (mSocket != null) {
                if (mSocket.isConnected()) {
                    btIcon.setIcon(R.drawable.ic_baseline_bluetooth_connected_24);
                }
            } else if (bluetoothAdapter.isEnabled()) {
                btIcon.setIcon(R.drawable.ic_baseline_bluetooth_on_24);
            }
        }
        return true;
    }

    // Broadcast Receiver to observe Bluetooth ON OFF state
    private final BroadcastReceiver broadcastReceiverBTState = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    //case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mBTDev = null;
                        if (mConnectThread != null) {
                            mConnectThread.cancel();
                        }
                        invalidateOptionsMenu();
                        makeToast("Bluetooth is required");
                        break;
                    case BluetoothAdapter.STATE_ON:
                    //case BluetoothAdapter.STATE_TURNING_ON:
                        invalidateOptionsMenu();
                        makeToast("Bluetooth Enabled");
                        break;
                }
            }
        }
    };


    private class ConnectThread extends Thread {

        private static final String TAG = "BT Connect Thread";
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {

            BluetoothSocket tempSoc = null;

            try {
                tempSoc = device.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Socket create method failed", e);
            }
            mmSocket = tempSoc;
        }

        @Override
        public void run() {

            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();

            } catch (IOException e) {

                Log.e(TAG, "run: Socket connect failed lol", e);
                runOnUiThread(() -> makeToast("Can't reach pnoi"));
                cancel();
            }

            if (mmSocket.isConnected()) {
                runOnUiThread(() -> makeToast("Pnoi Connected!"));
                manageSocketConnection(mmSocket);
            }

        }

        public void cancel() {
            mSocket = null;
            mConnectThread = null;
            mConnectedThread = null;
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Socket close failed", e);
            }
        }
    }

    private void manageSocketConnection(BluetoothSocket socket) {
        Log.d("Record Activity", "manageSocketConnection: " + socket.isConnected());
        mSocket = socket;

        PnoiBluetoothService pnoiBluetoothService = new PnoiBluetoothService(btServiceHandler);
        mConnectedThread = pnoiBluetoothService.getConnectedThread(mSocket);

        invalidateOptionsMenu();
    }

    // Location selection buttons
    public void onClickLUL(View view) {
        currentLoc = new byte[]{1, 0, 0, 0};
        lungLocButtonSelectionHandler(currentLoc, "LUL");
    }

    public void onClickRUL(View view) {
        currentLoc = new byte[]{0, 1, 0, 0};
        lungLocButtonSelectionHandler(currentLoc, "RUL");
    }

    public void onClickLLL(View view) {
        currentLoc = new byte[]{0, 0, 1, 0};
        lungLocButtonSelectionHandler(currentLoc, "LLL");
    }

    public void onClickRLL(View view) {
        currentLoc = new byte[]{0, 0, 0, 1};
        lungLocButtonSelectionHandler(currentLoc, "RLL");
    }


    private BluetoothDevice getBtDevicesWithName(String name) {

        Set<BluetoothDevice> btDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice btDevice : btDevices) {
            String devName = btDevice.getName();
            if (devName.equals(name)) {
                makeToast(name + " is paired, press again to connect");
                return btDevice;

            }
        }
        makeToast("No device named " + name + " is paired");
        return null;
    }


    // Helper Methods
    public static String getProfileFilename(ContentValues values) {
        SimpleDateFormat d = new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.ENGLISH);
        String time = d.format(new Date());

        return values.getAsString(ProfileEntry._ID) + "__" +
                time + "__" + values.getAsString(ProfileEntry.NAME).replace(" ", "-") + "_" +
            values.getAsString(ProfileEntry.AGE) + "_" +
            ProfileEntry.genderType(values.getAsInteger(ProfileEntry.GENDER)) + "_" +
                values.getAsString(ProfileEntry.HEIGHT) + "_" +
                values.getAsString(ProfileEntry.WEIGHT) + "_" +
                lungLocation + ".wav";
    }

    private String locFullForm(String loc) {
        switch (loc) {
            case "LUL":
                return "Left Upper Lobe";
            case "RUL":
                return "Right Upper Lobe";
            case "LLL":
                return "Left Lower Lobe";
            case "RLL":
                return "Right Lower Lobe";
            default:
                return "";
        }
    }

    private void lungLocButtonVisibilityHandler(byte[] buttonLoc) {
        int[] visible = {
                View.INVISIBLE,
                View.VISIBLE,
        };

        mLUL.setVisibility(visible[buttonLoc[0]]);
        mRUL.setVisibility(visible[buttonLoc[1]]);
        mLLL.setVisibility(visible[buttonLoc[2]]);
        mRLL.setVisibility(visible[buttonLoc[3]]);

    }

    private void lungLocButtonSelectionHandler(byte[] buttonLoc, String loc) {
        lungLocation = loc;
        int[] color = {
                getResources().getColor(R.color.colorUnselected),
                getResources().getColor(R.color.colorAccent)
        };
        mLUL.setBackgroundTintList(ColorStateList.valueOf(color[buttonLoc[0]]));
        mRUL.setBackgroundTintList(ColorStateList.valueOf(color[buttonLoc[1]]));
        mLLL.setBackgroundTintList(ColorStateList.valueOf(color[buttonLoc[2]]));
        mRLL.setBackgroundTintList(ColorStateList.valueOf(color[buttonLoc[3]]));

        mTextViewRecLocation.setText(locFullForm(lungLocation));
    }


    class TimerHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_START_TIMER :
                    timer.start();
                    sendEmptyMessage(MSG_UPDATE_TIMER);
                    break;
                case MSG_UPDATE_TIMER :
                    mTextViewRecordTimer.setText(timer.getElapsedTimeMinutes());
                    sendEmptyMessageDelayed(MSG_UPDATE_TIMER, 1000);
                    break;
                case MSG_STOP_TIMER :
                    removeMessages(MSG_UPDATE_TIMER);
                    timer.stop();
                    break;
                default:
                    break;
            }
        }
    }

    class BtServiceHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MESSAGE_TOAST :
                    Log.d("BtHan", "handleMessage: Write error");
                    btDisconnectMenuHandler(true);
            }
        }
    }

    public static class Stopwatch {

        private long startTime = 0;
        private long stopTime = 0;
        private boolean running = false;


        public void start() {
            this.startTime = System.currentTimeMillis();
            this.running = true;
        }


        public void stop() {
            this.stopTime = System.currentTimeMillis();
            this.running = false;
        }


        // elapsed time in milliseconds
        public long getElapsedTime() {
            if (running) {
                return System.currentTimeMillis() - startTime;
            }
            return stopTime - startTime;
        }


        // elapsed time in seconds
        public long getElapsedTimeSecs() {
            if (running) {
                return ((System.currentTimeMillis() - startTime) / 1000);
            }
            return ((stopTime - startTime) / 1000);
        }

        public String getElapsedTimeMinutes() {
            long unitMin = 60L;
            if (running) {
                long totalSeconds = getElapsedTimeSecs();
                long minutes = totalSeconds / unitMin;
                long seconds = totalSeconds - unitMin * minutes;
                Log.d("LLL", "getElapsedTimeMinutes: " + minutes);
                Log.d("LLL", "getElapsedTimeMinutes sec: " + seconds);

                String minutesS;
                String secondsS;

                if (minutes < 10) {
                    minutesS = "0" + minutes;
                } else {
                    minutesS = "" + minutes;
                }
                if (seconds < 10) {
                    secondsS = "0" + seconds;
                } else {
                    secondsS = "" + seconds;
                }
                return "00:" + minutesS + ":" + secondsS;
            } else {
                return "00:00:00";
            }

        }

    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        unregisterReceiver(broadcastReceiverBTState);
    }
}