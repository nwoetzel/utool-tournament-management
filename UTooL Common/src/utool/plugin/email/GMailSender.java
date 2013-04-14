package utool.plugin.email;

import javax.activation.DataHandler;   
import javax.activation.DataSource;   
import javax.mail.Message;   
import javax.mail.PasswordAuthentication;   
import javax.mail.Session;   
import javax.mail.Transport;   
import javax.mail.internet.InternetAddress;   
import javax.mail.internet.MimeMessage;   
import android.util.Log;
import java.io.ByteArrayInputStream;   
import java.io.IOException;   
import java.io.InputStream;   
import java.io.OutputStream;   
import java.security.Security;   
import java.util.Properties;   

/**
 * This class is for sending emails programmatically. An example use of this class is as follows:
 * Example Use:
 * 
    public void updateSubscriber(String address)
	{
		//send notification to subscriber of setup
		new RetreiveFeedTask().execute(address);
	}
 
	class RetreiveFeedTask extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... urls) {

			try {   
				GMailSender sender = new GMailSender("emailaddress@gmail.com", "password");
				String subject = "This is the Subject";
				String body = "This is the email body";
				String senderEmail = "msoetablet@gmail.com";
				sender.sendMail(subject, body, , urls[0]);   
				Log.d("email", "sent");
			} catch (Exception e) {   
				Log.e("AEH", "Error:"+e.getMessage());
			} 
			return null;
		}

		protected void onPostExecute(String feed) {
		}
	}
	
	Parts of the Code pulled from
	http://stackoverflow.com/questions/2020088/sending-email-in-android-using-javamail-api-without-using-the-default-built-in-a
	Author: Vinayak.B
	
 * @author waltzm
 * @version 1/16/2013
 */
public class GMailSender extends javax.mail.Authenticator { 
	
	/**
	 * Holds the address of the mail host
	 */
	private String mailhost = "smtp.gmail.com";   
	
	/**
	 * Holds the username to the email account
	 */
	private String user;   
	
	/**
	 * Holds the password to the email account
	 */
	private String password;
	
	/**
	 * Holds a reference to the session
	 */
	private Session session;   

	/**
	 * Holds the error message
	 */
	String error;
	
	
	static {   
		Security.addProvider(new utool.plugin.email.JSSEProvider());   
	}  

	/**
	 * Constructor for the gmail Sender attached to the username and password.
	 * @param user the email's username
	 * @param password the email's password
	 */
	public GMailSender(String user, String password) {   
		this.user = user;   
		this.password = password;   

		Properties props = new Properties();   
		props.setProperty("mail.transport.protocol", "smtp");   
		props.setProperty("mail.host", mailhost);   
		props.put("mail.smtp.auth", "true");   
		props.put("mail.smtp.port", "465");   
		props.put("mail.smtp.socketFactory.port", "465");   
		props.put("mail.smtp.socketFactory.class",   
				"javax.net.ssl.SSLSocketFactory");   
		props.put("mail.smtp.socketFactory.fallback", "false");   
		props.setProperty("mail.smtp.quitwait", "false");   

		session = Session.getDefaultInstance(props, this);   
	}   

	protected PasswordAuthentication getPasswordAuthentication() {   
		return new PasswordAuthentication(user, password);   
	}   


	/**
	 * Sends an email to the recipient with the subject and body.
	 * @param subject the subject of the email
	 * @param body the body of the email
	 * @param sender the sender of the email
	 * @param recipient the recipient of the email
	 */
	public synchronized void sendMail(String subject, String body, String sender, String recipient)
	{   
		
		try{
			MimeMessage message = new MimeMessage(session);   
			DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/html"));   
			message.setSender(new InternetAddress(sender));   
			message.setSubject(subject);   
			message.setDataHandler(handler);   
			if (recipient.indexOf(',') > 0)   
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));   
			else  
				message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));   
			Transport.send(message);   
			Log.e("email", "sent to transport");

		}catch(Exception e){
			this.error=e.getMessage();
			Log.e("email", "sent: "+e.getMessage());
			//a.notifyError(e.getMessage());
			
		}
	}   

	/**
	 * Holds the byte array data source
	 */
	public class ByteArrayDataSource implements DataSource 
	{   
		/**
		 * Holds the data
		 */
		private byte[] data;
		
		/**
		 * Holds the type
		 */
		private String type;   

		/**
		 * Constructor
		 * @param data data of the source
		 * @param type type of the data
		 */
		public ByteArrayDataSource(byte[] data, String type) 
		{   
			super();   
			this.data = data;   
			this.type = type;   
		}   

		/**
		 * Constructor
		 * @param data byte array of data
		 */
		public ByteArrayDataSource(byte[] data) 
		{   
			super();   
			this.data = data;   
		}   

		/**
		 * Sets the type
		 * @param type the type
		 */
		public void setType(String type) 
		{   
			this.type = type;   
		}   

		public String getContentType() 
		{   
			if (type == null)   
				return "application/octet-stream";   
			else  
				return type;   
		}   

		public InputStream getInputStream() throws IOException {   
			return new ByteArrayInputStream(data);   
		}   

		public String getName() {   
			return "ByteArrayDataSource";   
		}   

		public OutputStream getOutputStream() throws IOException {   
			throw new IOException("Not Supported");   
		}   
	}   
}  