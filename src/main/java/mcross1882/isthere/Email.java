/**
 * isthere file notifier
 *
 * @since     1.0
 * @package   isthere
 * @copyright 2014
 * @license   See LICENSE
 */
package mcross1882.isthere;

/**
 * Entity class for storing email meta data
 *
 * @since  1.0
 * @access public
 * @author Matthew Cross <matthew@pmg.co>
 */
public class Email
{
  private String mTo;
  private String mFrom;
  private String mSubject;
  private String mMessage;

  /**
   * Set the "to" field
   *
   * @since  1.0
   * @access public
   * @param  String to
   * @return this
   */
  public Email setTo(String to)
  {
    mTo = to;
    return this;
  }

  /**
   * Return the "to" field
   *
   * @since  1.0
   * @access public
   * @return String
   */
  public String getTo()
  {
    return mTo;
  }


  /**
   * Set the "from" field
   *
   * @since  1.0
   * @access public
   * @param  String from
   * @return this
   */
  public Email setFrom(String from)
  {
    mFrom = from;
    return this;
  }

  /**
   * Return the "from" field
   *
   * @since  1.0
   * @access public
   * @return String
   */
  public String getFrom()
  {
    return mFrom;
  }

  /**
   * Set the "subject" field
   *
   * @since  1.0
   * @access public
   * @param  String subject
   * @return this
   */
  public Email setSubject(String subject)
  {
    mSubject = subject;
    return this;
  }

  /**
   * Return the "subject" field
   *
   * @since  1.0
   * @access public
   * @return String
   */
  public String getSubject()
  {
    return mSubject;
  }

  /**
   * Set the "message" field
   *
   * @since  1.0
   * @access public
   * @param  String message
   * @return this
   */
  public Email setMessage(String message)
  {
    mMessage = message;
    return this;
  }

  /**
   * Return the "message" field
   *
   * @since  1.0
   * @access public
   * @return String
   */
  public String getMessage()
  {
    return mMessage;
  }
}
