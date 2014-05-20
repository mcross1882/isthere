/**
 * isthere file notifier
 *
 * @since     1.0
 * @package   isthere
 * @copyright 2014
 * @license   See LICENSE
 */
package mcross1882.isthere;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Scanner;
import javax.mail.MessagingException;

/**
 * FileWatcherService watches a given directory for files changes and if they
 * occur (i.e. a file we need is detected or missing) it will dispatch a
 * notification email to alert the user.
 *
 * @since  1.0
 * @author Matthew Cross <matthew@pmg.co>
 */
public class FileWatcherService
{
  /**
   * WatchService to create callbacks for directory changes
   *
   * @since  1.0
   */
  protected WatchService mWatcher = null;

  /**
   * The starting/root directory to listen for changes
   *
   * @since  1.0
   */
  protected Path mStartingDirectory;

  /**
   * Constructs a FileWatcherService base off the provided uri
   *
   * @since  1.0
   * @param  uri base directory to watch
   * @param  separator the directory separator to use
   * @param  service listener to bind
   */
  public FileWatcherService(String uri, String separator, WatchService service) throws Exception
  {
    mWatcher = service;
    if (!uri.contains(separator)) {
      throw new Exception(String.format("%s: Failed to resolve uri [%s].", FileWatcherService.class, uri));
    }
    mStartingDirectory = FileSystems.getDefault().getPath(uri.substring(0, uri.lastIndexOf(separator))).toAbsolutePath();
  }

  /**
   * watchFile listens for directory changes on the uri provided from the constructor
   * if the filename provided in this call exists the method will exit. If it doesn't
   * the method will dispatch a notification and wait for the file to arrive. Once it does
   * a second email will be dispatched notifying the user it has arrived.
   *
   * @since  1.0
   * @param  service the email service sender
   * @param  emailTo the email address to send notification too
   * @param  emailFrom the email address to use as the from address
   * @param  filename the file to watch
   */
  protected void watchFile(EmailService service, String emailTo, String emailFrom, String filename)
    throws InterruptedException, IOException, MessagingException
  {
    boolean hasFile = true;

    Path expectedPath = mStartingDirectory.resolve(filename);
    File file = expectedPath.toFile();
    WatchKey key = null;
    
    if (!file.exists() && !file.isDirectory()) {
      hasFile = false;
      service.sendFileMissingEmail(emailTo, emailFrom, filename);
      
      key = mStartingDirectory.register(mWatcher,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY);
    }

    while (null != key && !hasFile) {
      key = mWatcher.take();

      for (WatchEvent<?> event: key.pollEvents()) {
        WatchEvent.Kind kind = event.kind();

        if (kind == StandardWatchEventKinds.OVERFLOW) {
          continue;
        }

        // This generates a link warning and is noted in the WatcherService API
        // @see http://docs.oracle.com/javase/tutorial/essential/io/notification.html#name
        // p.s. fuck this because its actually the recommended remedy...
        @SuppressWarnings("unchecked")
        WatchEvent<Path> ev = (WatchEvent<Path>)event;
        Path foundFile = mStartingDirectory.resolve(ev.context());

        if (!hasFile && expectedPath.equals(foundFile)) {
          hasFile = true;
          service.sendFileArrivedEmail(emailTo, emailFrom, filename);
        }
      }

      if (!key.reset()) {
        break;
      }
    }
  }

  /**
   * Close the underlying EventWatcher
   *
   * @since  1.0
   */
  public void close()
  {
    try {
      mWatcher.close();
    } catch (Exception e) {
      return;
    }
  }
}
