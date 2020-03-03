package com.example.votingsoftware;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class InputProcessor extends BroadcastReceiver {
    private static final String TAG = InputProcessor.class.getSimpleName();
    public static final String pdu_type = "pdus";


    MainActivity main = null;
    void setMainActivityHandler(MainActivity main){
        this.main=main;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String strMessage = "";
        String sender;
        String toastMsg;
        String format = bundle.getString("format");

        KeyValueList kvList;

        //MainActivity host = (MainActivity) context;

        // get the sms message
        Object[] pdus = (Object[]) bundle.get(pdu_type);

        if (pdus != null) {
            // check version
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

            // fill msgs array
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                //check
                if (isVersionM) {
                    // If Android version M or newer:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    // If Android version L or older:
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                // Build the message to show.
                sender = msgs[i].getOriginatingAddress();
                //strMessage += "SMS from " + msgs[i].getOriginatingAddress();
                strMessage = msgs[i].getMessageBody();
                toastMsg = "Vote from : " + sender + " for ID " + strMessage + "\n";
                // Log and display the SMS message.
                Log.d(TAG, "onReceive: " + toastMsg);
                //Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
                kvList = MainActivity.generateVoteMessage(sender, strMessage);
                //Tell the activity that a new message has been received.
                Message msg = MainActivity.callbacks.obtainMessage(MainActivity.MESSAGE_RECEIVED);
                msg.obj = kvList.toString();
                MainActivity.callbacks.sendMessage(msg);

                // handle the vote
                main.handleVote(sender, strMessage);
            }
        }
    }
}
