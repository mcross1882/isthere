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
class Email
{
  private String mTo;
  private String mFrom;
  private String mSubject;
  private String mMessage;
  
  public Email setTo(String to)
  {
    mTo = to;
    return this;
  }
  
  public String getTo()
  {
    return mTo;
  }
  
  public Email setFrom(String from)
  {
    mFrom = from;
    return this;
  }
  
  public String getFrom()
  {
    return mFrom;
  }
  
  public Email setSubject(String subject)
  {
    mSubject = subject;
    return this;
  }
  
  public String getSubject()
  {
    return mSubject;
  }
  
  public Email setMessage(String message)
  {
    mMessage = message;
    return this;
  }
  
  public String getMessage()
  {
    return mMessage;
  }
}
