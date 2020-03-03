package com.example.votingsoftware;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "VotingSoftware";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private static Button connectToServerButton,registerToServerButton;

    private static EditText serverIp,serverPort;

    static ComponentSocket client;

    private static TextView messageReceivedListText;

    private static final String SENDER = "VotingSoftware";
    private static final String REGISTERED = "Registered";
    private static final String DISCOONECTED =  "Disconnect";
    private static final String SCOPE = "SIS.Scope1";

    private KeyValueList readingMessage;

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 2;
    public static final int MESSAGE_RECEIVED = 3;

    static Handler callbacks = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str;
            String[] strs;
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
                    str = (String)msg.obj;
                    messageReceivedListText.append(str+"********************\n");
                    final int scrollAmount = messageReceivedListText.getLayout().getLineTop(messageReceivedListText.getLineCount()) - messageReceivedListText.getHeight();
                    if (scrollAmount > 0)
                        messageReceivedListText.scrollTo(0, scrollAmount);
                    else
                        messageReceivedListText.scrollTo(0, 0);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


    }


    //Pass a message to the socket thread and update the sent-text view.
    static void sendMessage(final KeyValueList messageInfo){
        if(client!=null){
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    client.setMessage(messageInfo);
                }
            }, 100);

            /*messageToSendList.append(messageInfo.toString()+"********************\n");
            final int scrollAmount = messageToSendList.getLayout().getLineTop(messageToSendList.getLineCount()) - messageToSendList.getHeight();
            if (scrollAmount > 0)
                messageToSendList.scrollTo(0, scrollAmount);
            else
                messageToSendList.scrollTo(0, 0);*/
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        //EditText serverIp, serverPort, refreshRate, connectcope;

        //EditText messageScope, messageType, roleType, messageName, receiver, messageContent, addMessageAttrKey, addMessageAttrValue;//sender

        View rootView1, rootView2;

        //Button msgSendButton, xmlLoadButton, msgClearButton, addMessageAttrButton;
        KeyValueList messageInfo = new KeyValueList();

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int num = getArguments().getInt(ARG_SECTION_NUMBER);

            if (num == 1) {
                if (rootView1 == null) {
                    rootView1 = inflater.inflate(R.layout.connection_config, container, false);
                    connectToServerButton = (Button) rootView1.findViewById(R.id.connectButton);
                    registerToServerButton = (Button) rootView1.findViewById(R.id.registerButton);
                    serverIp = (EditText) rootView1.findViewById(R.id.serverIp);
                    serverPort = (EditText) rootView1.findViewById(R.id.serverPort);
                    messageReceivedListText = (TextView) rootView1.findViewById(R.id.messageReceivedList);
                    messageReceivedListText.setMovementMethod(ScrollingMovementMethod.getInstance());

                    registerToServerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (client != null && client.isSocketAlive() && registerToServerButton.getText().toString().equalsIgnoreCase(REGISTERED)) {
                                Toast.makeText(getActivity(), "Already registered.", Toast.LENGTH_SHORT).show();
                            } else {
                                client = new ComponentSocket(serverIp.getText().toString(), Integer.parseInt(serverPort.getText().toString()), callbacks);
                                client.start();
                                KeyValueList list = generateRegisterMessage();
                                sendMessage(list);
                            }
                        }
                    });
                    connectToServerButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (connectToServerButton.getText().toString().equalsIgnoreCase(DISCOONECTED)) {
                                Log.e(MainActivity.TAG, "Sending connectToServerButton.2");
                                client.killThread();
                                connectToServerButton.setText("Connect");
                            } else {
                                KeyValueList list = generateConnectMessage();
                                sendMessage(list);

                                connectToServerButton.setText(DISCOONECTED);
                            }
                        }
                    });
                }
                return rootView1;
            }
            else if(num==2){
                if(rootView2==null){
                    rootView1 = inflater.inflate(R.layout.voting_config, container, false);
                    //set up voting config
                }
                return rootView2;
            }
            return rootView1;

        }

        //
    }


    //Generate a test register message, please replace something of attributes with your own.
    static KeyValueList generateRegisterMessage(){
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
    static KeyValueList generateConnectMessage(){
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

    //Generate a reply
    static KeyValueList generateReplyMessage(String recipient){
        KeyValueList list = new KeyValueList();
        //Set the scope of the message
        list.putPair("Scope",SCOPE);
        //Set the message type
        list.putPair("MessageType","Confirm");
        //Set the sender or name of the message
        list.putPair("Sender",SENDER);
        //Set the role of the message
        list.putPair("Role","Basic");
        //Set the name of the component
        list.putPair("Name",SENDER);
        list.putPair("Receiver",recipient);
        return list;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Connnection Config";
                case 1:
                    return "Voting Config";

            }
            return null;
        }
    }

}
