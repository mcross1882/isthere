/**
 * isthere file notifier
 *
 * @since     1.0
 * @package   isthere
 * @copyright 2014
 * @license   See LICENSE
 */
package mcross1882.isthere;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * EmailService provides methods for sending notification
 * emails to smtp mail servers
 *
 * @since  1.0
 * @access public
 * @author Matthew Cross <matthew@pmg.co>
 */
class EmailService
{
  protected final String HOST_KEY = "isthere.email.host";
  protected final String USER_KEY = "isthere.email.user";
  protected final String PASS_KEY = "isthere.email.pass";
  protected final String PORT_KEY = "isthere.email.port";
  
  protected Properties mProperties;
  
  protected Session mSession;
  
  protected Transport mTransport;
  
  public EmailService(String host, String user, String pass, int port)
    throws MessagingException, NoSuchProviderException
  {
    mProperties = System.getProperties();
    mSession = Session.getDefaultInstance(mProperties);
    mTransport = mSession.getTransport("smtps");
    
    mProperties.setProperty(HOST_KEY, host);
    mProperties.setProperty(USER_KEY, user);
    mProperties.setProperty(PASS_KEY, pass);
    mProperties.setProperty(PORT_KEY, Integer.toString(port));
  }
  
  public void connect() throws MessagingException
  {
    mTransport.connect(mProperties.getProperty(HOST_KEY)
      , Integer.parseInt(mProperties.getProperty(PORT_KEY))
      , mProperties.getProperty(USER_KEY)
      , mProperties.getProperty(PASS_KEY));
  }
  
  public void sendEmail(Email em) throws MessagingException
  {
    MimeMessage message = new MimeMessage(mSession);
    message.setFrom(new InternetAddress(em.getFrom()));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(em.getTo()));
    message.setSubject(em.getSubject());
    message.setText(em.getMessage());
    
    mTransport.sendMessage(message, message.getAllRecipients());
    System.out.println("Sent message successfully....");
  }
  
  public void close() throws MessagingException
  {
    if (null != mTransport) {
      mTransport.close();
    }
  }
}
