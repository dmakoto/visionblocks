package edu.mit.cameraCulture.vblocks.predefined;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.mit.cameraCulture.vblocks.CommitableView;
import edu.mit.cameraCulture.vblocks.EngineActivity;
import edu.mit.cameraCulture.vblocks.Module;
import edu.mit.cameraCulture.vblocks.Sample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcel;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class EMailNotifier extends Module {

	public static final String REGISTER_SERVICE_NAME = "EMailNotifier";
	
	private String mMail = "your@email.com";
	private String mText = "Notification text";
	private boolean mWasSent;
	
	public EMailNotifier(){
		super(REGISTER_SERVICE_NAME);
	}

	
	@Override
	public void onCreate(EngineActivity context) {
		mWasSent = false;
		super.onCreate(context);
	}
	
	@Override
	public ExecutionCode execute(Sample image) {
		if(!mWasSent){
			mWasSent = true;
			new Thread(new Runnable() {
				TextView txt = null;
				
				@Override
				public void run() {
					
					final RelativeLayout layout = mContext.getLayout();
					
					layout.post(new Runnable() {
						@Override
						public void run() {
							RelativeLayout.LayoutParams lp = new LayoutParams(
									LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,-1);
							lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,-1);
							txt = new TextView(mContext);
							txt.setText("Sending e-mail...");
							txt.setTextSize(txt.getTextSize());
							
							layout.addView(txt, lp);
						}
					});
					
					final boolean result = sendNotification(mMail, mText);
					
					layout.post(new Runnable() {
						@Override
						public void run() {
							RelativeLayout.LayoutParams lp = new LayoutParams(
									LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
							lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,-1);
							lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,-1);
							//TextView txt = new TextView(mContext);
							txt.setTextColor(result?Color.GREEN :Color.RED);
							txt.setText(result? 
									"The notification was successfully sent.":
									"Unable to send a notification."
								);
							//layout.addView(txt, lp);
						}
					});
				}
			}).start();
		}
		return ExecutionCode.NONE;
	}
	
	@Override
	public void onDestroyModule() {
		super.onDestroyModule();
	}

	
	public static String getModuleName(){
		return "E-mail notification";
	}
	
	@Override
	public String getName() {
		return getModuleName();
	}
	
	
	private static boolean sendNotification(String to, String mailText) 
	{    
		  boolean result = false;
	      // Sender's email ID needs to be mentioned
	      String from = "vblocks-notification@media.mit.edu";

	      // Assuming you are sending email from localhost
	      String host = "outgoing.mit.edu";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Set Subject: header field
	         message.setSubject("Notification!");
	         
	         // Now set the actual message
	         message.setText(mailText);

	         // Send message
	         Transport.send(message);
	         Log.d(REGISTER_SERVICE_NAME, "Sent message successfully....");
	         result = true;
	      }catch (Exception e) {
	         e.printStackTrace();
	         Log.e(REGISTER_SERVICE_NAME, e.getMessage());
	      }
	      return result;
	   }

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CommitableView getConfigurationView(Context context) {
		return new EMailNotifierConfiguration(context, this);
	}
	
	public String getMailAddress() {
		return mMail;
	}

	public void setMailAddress(String mMail) {
		this.mMail = mMail;
	}

	public String getMailText() {
		return mText;
	}

	public void setMailText(String mText) {
		this.mText = mText;
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.d("TOUCH", this.toString());
		return false;
	}
}
