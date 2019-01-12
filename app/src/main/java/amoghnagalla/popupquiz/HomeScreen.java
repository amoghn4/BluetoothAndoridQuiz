package amoghnagalla.popupquiz;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class HomeScreen extends AppCompatActivity
{
    private static final String TAG = "LOG";
    private final int MESSAGE_READ = 1;
    private final int REQUEST_ENABLE_BT = 1;
    private final int  MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 2;
    private static final UUID MY_UUID =UUID.fromString("33d2dd36-75b3-42f3-954e-76a77dd749b94");


    private Handler mHandler = new Handler();
    private String deviceMacAddress;

    boolean locationPermission = false;


    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter newDeviceArrayAdapter;
    private ArrayAdapter pairedDeviceArrayAdapter;
    private ListView newDeviceList;
    private ListView pairedDeviceList;
    private IntentFilter filter;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_LISTEN = 1;     // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private int mState = STATE_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Setup the Window
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize button to scan for bluetooth devices
        Button scanForDevices  = (Button) findViewById(R.id.button_scan);
        scanForDevices.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                pairedDeviceArrayAdapter.clear();
                registerReceiver(mReceiver,filter);
                scanForDevices();
                queryPairedDevices();
                newDeviceArrayAdapter.clear();
            }
        });

//        //Initialize button to end scanning and recover resources
//        Button endDiscover = (Button) findViewById(R.id.endDiscover);
//        endDiscover.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                mBluetoothAdapter.cancelDiscovery();
//                stop();
//                unregisterReceiver(mReceiver);
//
//            }
//        });


        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        newDeviceArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice);
        pairedDeviceArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice);

        // Find and set up the ListView for new devices
        newDeviceList = (ListView) findViewById(R.id.new_devices);
        newDeviceList.setAdapter(newDeviceArrayAdapter);
        newDeviceList.setOnItemClickListener(deviceClickListener);

        // Find and set up the ListView for paired devices
        pairedDeviceList = (ListView) findViewById(R.id.paired_devices);
        pairedDeviceList.setAdapter(pairedDeviceArrayAdapter);
        pairedDeviceList.setOnItemClickListener(deviceClickListener);

        //Register for broadcasts when a device is discovered
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Initially make device discoverable in this activity
        makeDiscoverable();

        // Get list of paired devices to display
        queryPairedDevices();
        pairedDeviceList.setAdapter(pairedDeviceArrayAdapter);

        // Initially set this device up as a server
        start();
    }
    public void endScan(View view)
    {
        mBluetoothAdapter.cancelDiscovery();
        stop();
        unregisterReceiver(mReceiver);
        Intent quizIntent = new Intent(HomeScreen.this, QuizActivity.class);
        quizIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(quizIntent);
        Log.i(TAG, "Loading Quiz");
        stop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null)
        {
            mBluetoothAdapter.cancelDiscovery();
            stop();
        }

        // Unregister broadcast listeners
        //this.unregisterReceiver(mReceiver);
    }

    //Checks if device supports bluetooth
    //if so request to enable bluetooth
    public void checkSupportBluetooth()
    {
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(mBluetoothAdapter.isEnabled())
            {
                Toast.makeText(this, "Bluetooth is already ENABLED ", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Device supports BLUETOOTH", Toast.LENGTH_SHORT).show();
                enableBluetooth();
            }
        }
    }

    //Helper method that enables bluetooth
    private void enableBluetooth()
    {
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    //Scans for bluetooth devices
    public void scanForDevices()
    {
        if(mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();
        }

        enableBluetooth();
        requestLocationPermission();

        if(locationPermission)
        {
            findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
            mBluetoothAdapter.startDiscovery();
        }
    }

    //Scans for paired bluetooth devices
    public void queryPairedDevices()
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices
        if (pairedDevices.size() > 0)
        {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices)
            {
                // Add the name and address to an array adapter to show in a ListView
                pairedDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        else
        {
            String noDevices = "No Devices have been paired".toString();
            pairedDeviceArrayAdapter.add(noDevices);
        }

    }

    private void  makeDiscoverable()
    {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(discoverableIntent);
    }

    //BroadcastReceiver that listens for discovered devices
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            //When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //If it's already paired, skip it. Need to display it on paired device list
                if(device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    if (device.getName() == null)
                    {
                        String deviceInfo = "Device Name: UNKNOWN" + "\n" + device.getAddress();
                        newDeviceArrayAdapter.add(deviceInfo);
                        //newDeviceList.setAdapter(newDeviceArrayAdapter);
                    }
                    else
                    {
                        String deviceInfo = "Device Name: " + device.getName() + "\n"  + device.getAddress();
                        newDeviceArrayAdapter.add(deviceInfo);
                        //newDeviceList.setAdapter(newDeviceArrayAdapter);
                    }
                }
            }
        }
    };

    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            //Cancel discovery, takes up too much resource
            mBluetoothAdapter.cancelDiscovery();

            //Listen fom a connection to be made. By default devices will be setup as servers
            // and can initiate a connection request at any time

            //Get the device MAC address, which is the last 17 chars in the View
            String deviceInfo = ((TextView) view).getText().toString();
            deviceMacAddress = deviceInfo.substring(deviceInfo.length()-17);

            //Not sure what I am doing with this yet
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceMacAddress);
            //If device is not already paired, pair device
            //if(device.getBondState() != BluetoothDevice.BOND_BONDED)
            //{
                HomeScreen.this.connect(device);

            //}
            Toast.makeText(HomeScreen.this, "MAC Address: " + deviceMacAddress, Toast.LENGTH_SHORT).show();

        }
    };

    public void requestLocationPermission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
            {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                //  MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
        {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch(requestCode)
        {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    locationPermission = true;
                    Log.d("Location:" , "Access Granted");
                }
                else
                {
                    //permission denied. Disable the functionality
                    // that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            makeDiscoverable();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }
    private void setState(int state)
    {
       Log.d(TAG, "setState() " + mState + " -> " + state);
       mState = state;
    }

    // Start AcceptThread to begin a session
    // in listening (server) mode.
    public synchronized void start()
    {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Cancel any thread listening for a connection
        // and starts a new session
        setState(STATE_LISTEN);
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

    }

    // Start the ConnectThread to initiate a connection to a remote device.
    public synchronized void connect(BluetoothDevice device)
    {
        Log.d(TAG, "Attempting to connect: " + device.getName());

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if(mConnectedThread != null) {
               mConnectedThread.cancel();
               mConnectedThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if(mConnectedThread != null) {
           mConnectedThread.cancel();
           mConnectedThread = null;
        }

        // Start the thread to connect with the given devices
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // Start the ConnectedThread to begin managing a Bluetooth connection
    public synchronized void connected(BluetoothSocket socket)
    {
        //Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        setState(STATE_CONNECTED);
    }

    // Stop all threads
    public synchronized void stop()
    {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState(STATE_NONE);
        //Toast.makeText(this, mState, Toast.LENGTH_SHORT).show();
    }

    private void connectionFailed() {
        Toast.makeText(this, "Unable to connect device", Toast.LENGTH_SHORT).show();

        // Start the service over to restart listening mode
        this.start();
    }

    // Write to the ConnectedThread in an unsynchronized manner
    public void write(byte[] out)
    {
        //Create temporary object
        ConnectedThread r;

        // Synchronize a copy of the ConnectThread
        synchronized(this)
        {
            if(mState != STATE_CONNECTED)
            {
                return;
            }
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
//  ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private class AcceptThread extends Thread
    {
        //The local server socket
        private BluetoothServerSocket mmServerSocket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try
            {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Nexus 6P", MY_UUID);
            }
            catch (IOException e)
            {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN mAcceptThread");
            Log.d(TAG, "(Server Socket): Listening for connection");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try
                {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();

                }
                catch (IOException e)
                {
                    Log.e(TAG," accept() failed" ,e);
                    break;
                }

                 //If a connection was accepted
                if (socket != null) {
                    synchronized (this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e)
                                {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread");
        }
        public void cancel()
        {
            Log.d(TAG, "cancel " + this);
            try
            {
                mmServerSocket.close();
                Log.d(TAG, "mAcceptThread closed");
            }
            catch(IOException e)
            {
                Log.e(TAG, "close() of server failed",e);
            }
        }
    }
//  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // This thread runs while attempting to make an outgoing connection
    // with a device. It runs straight through; the connection either
    // succeeds of fails.
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {

                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "Socket is created");
            } catch (IOException e)
            {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN mConnectThread");

            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try
            {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

            }
            catch (IOException connectException)
            {
                // Unable to connect; close the socket and get out
                try
                {
                    mmSocket.close();
                }
                catch (IOException closeException)
                {
                    Log.e(TAG, "unable to close() socket during connection failure", closeException);
                }
                return;
            }
            //Reset the ConnectThread because we're done
            synchronized (this)
            {
                mConnectThread = null;
            }
            Log.i(TAG,"Connected: " + mmDevice.getName());
            connected(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
//  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // This thread runs during a connection with a remote device.
    // It handles all incoming and outgoing transmissions.
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(TAG, "temp sockets creation: SUCCESSFUL");
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
                //Toast.makeText(HomeScreen.this, "temp sockets not created", Toast.LENGTH_SHORT).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {

            Log.i(TAG, "BEGIN mConnectedThread");


            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (mState == STATE_CONNECTED) {
                try {
                    Log.d(TAG, "(Server): Listening to InputStream");
                     //Read from the InputStream
                     bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                     mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    //Toast.makeText(HomeScreen.this, "disconnected", Toast.LENGTH_SHORT).show();
                    HomeScreen.this.start();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e)
            {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e)
            {
                Log.e(TAG, "close of connect socket failed",e);
            }
        }
    }
}
