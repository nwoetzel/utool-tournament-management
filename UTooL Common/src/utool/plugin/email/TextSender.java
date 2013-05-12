package utool.plugin.email;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * This class is for sending texts programmatically.
 * Call sendSMS with a desired phone number to send the message to, and the application
 * context of the app. Errors and sucess with be sent as a toast to the application.
 * 
 * @author waltzm
 * @version 4/20/2013
 */
public class TextSender
{
	/**
	 * ---sends an SMS message to another device---
	 * @param phoneNumber the phone number to send the message to
	 * @param message the message
	 * @param c the application context
	 */
	public static void sendSMS(String phoneNumber, String message, Context c)
	{     
		try
		{
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(phoneNumber, null, message, null, null);        
		}
		catch(Exception e)
		{
			Log.e("TextSender","Message Failed",e);
		}
	}

}
