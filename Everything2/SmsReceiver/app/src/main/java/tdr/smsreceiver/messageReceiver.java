package tdr.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by A on 3/14/2017.
 */

public class messageReceiver extends BroadcastReceiver {

    public messageReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++){
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                for (SmsMessage message : messages){

                    String strMessageFrom = message.getDisplayOriginatingAddress();
                    String strMessageBody = message.getDisplayMessageBody();

                    String str = new String("From: " + strMessageFrom + "\nMessage: " + strMessageBody + "\n");

                    MainActivity inst = MainActivity.instance();
                    //inst.updateList(str);
                    inst.handleInfo(strMessageFrom, strMessageBody);
                }
            }
        }

    }
}