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
 * @author Matthew Cross <matthew@pmg.co>
 */
public class EmailService
{
  protected final String HOST_KEY = "isthere.email.host";
  protected final String USER_KEY = "isthere.email.user";
  protected final String PASS_KEY = "isthere.email.pass";
  protected final String PORT_KEY = "isthere.email.port";

  /**
   * System and stored email properties
   *
   * @since  1.0
   */
  protected Properties mProperties;

  /**
   * Email session
   *
   * @since  1.0
   */
  protected Session mSession;

  /**
   * Email Transport
   *
   * @since  1.0
   */
  protected Transport mTransport;

  /**
   * Constructs an EmailService from the meta data provided in the constructor arguments
   *
   * @since  1.0
   * @param  host the host to connect
   * @param  user the account username
   * @param  pass the account password
   * @param  port the port number
   * @throws MessagingException, NoSuchProviderException
   */
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

  /**
   * Connect to the SMTP host
   *
   * @since  1.0
   * @throws MessagingException
   */
  public void connect() throws MessagingException
  {
    mTransport.connect(mProperties.getProperty(HOST_KEY)
      , Integer.parseInt(mProperties.getProperty(PORT_KEY))
      , mProperties.getProperty(USER_KEY)
      , mProperties.getProperty(PASS_KEY));
  }

  /**
   * Send an email
   *
   * @since  1.0
   * @param  em the email class to send
   * @throws MessagingException
   */
  public void sendEmail(Email em) throws MessagingException
  {
    MimeMessage message = new MimeMessage(mSession);
    message.setFrom(new InternetAddress(em.getFrom()));
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(em.getTo()));
    message.setSubject(em.getSubject());
    message.setText(em.getMessage());

    mTransport.sendMessage(message, message.getAllRecipients());
  }

  /**
   * Close the current connection
   *
   * @since  1.0
   * @throws MessagingException
   */
  public void close() throws MessagingException
  {
    if (null != mTransport && mTransport.isConnected()) {
      mTransport.close();
    }
  }

  /**
   * Send a missing file email to a user
   *
   * @since  1.0
   * @param  to TO field in the email
   * @param  from FROM field in the email
   * @param  filename the attachment file
   * @throws MessagingException
   */
  public void sendFileMissingEmail(String to, String from, String filename) 
    throws MessagingException
  {
    connect();
    sendEmail(new Email()
      .setTo(to)
      .setFrom(from)
      .setSubject(String.format("File is missing [%s]", filename))
      .setMessage(String.format(
        "The file %s is currently not available (%s). When it arrives I will notify you again.",
        filename, new Date().toString()
      ))
    );
    close();
  }

  /**
   * Send a file arrived email
   *
   * @since  1.0
   * @param  to TO field in the email
   * @param  from FROM field in the email
   * @param  filename the attachment file
   * @throws MessagingException
   */
  public void sendFileArrivedEmail(String to, String from, String filename) 
    throws MessagingException
  {
    connect();
    sendEmail(new Email()
      .setTo(to)
      .setFrom(from)
      .setSubject(String.format("File has arrived [%s]", filename))
      .setMessage(String.format(
        "The file %s has arrived and is ready for processing (%s).", 
        filename, new Date().toString()
      ))
    );
    close();
  }
}
