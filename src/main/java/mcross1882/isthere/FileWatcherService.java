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
import java.util.HashMap;
import java.util.Scanner;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javax.mail.MessagingException;

/**
 * FileWatcherService watches a given directory for files changes and if they
 * occur (i.e. a file we need is detected or missing) it will dispatch a
 * notification email to alert the user.
 *
 * @since  1.0
 * @access public
 * @author Matthew Cross <matthew@pmg.co>
 */
class FileWatcherService
{
  /**
   * WatchService to create callbacks for directory changes
   *
   * @since  1.0
   * @accces protected
   */
  protected WatchService mWatcher = null;

  /**
   * The starting/root directory to listen for changes
   *
   * @since  1.0
   * @accces protected
   */
  protected Path mStartingDirectory;

  /**
   * Constructs a FileWatcherService base off the provided uri
   *
   * @since  1.0
   * @access public
   * @param  String uri base directory to watch
   * @param  WatchService the service listener to bind
   * @return self
   */
  public FileWatcherService(String uri, WatchService service)
    throws Exception
  {
    mWatcher = service;
    if (-1 == uri.lastIndexOf("/")) {
      throw new Exception(String.format("%s: Failed to resolve uri [%s].", FileWatcherService.class, uri));
    }
    mStartingDirectory = Paths.get(uri.substring(0, uri.lastIndexOf("/")) + "/");
  }

  /**
   * watchFile listens for directory changes on the uri provided from the constructor
   * if the filename provided in this call exists the method will exit. If it doesn't
   * the method will dispatch a notification and wait for the file to arrive. Once it does
   * a second email will be dispatched notifying the user it has arrived.
   *
   * @since  1.0
   * @access protected
   * @param  EmailService service
   * @param  String emailTo the email address to send notification too
   * @param  String filename the file to watch
   * @return void
   */
  protected void watchFile(EmailService service, String emailTo, String filename)
    throws MessagingException, IOException
  {
    boolean hasFile = true;
    File file = new File(filename);

    if (!file.exists() && !file.isDirectory()) {
      hasFile = false;
      service.sendFileMissingEmail(emailTo, filename);
    } else {
      hasFile = true;
      System.out.println("File is present");
    }

    WatchKey key = mStartingDirectory.register(mWatcher,
      StandardWatchEventKinds.ENTRY_CREATE,
      StandardWatchEventKinds.ENTRY_DELETE,
      StandardWatchEventKinds.ENTRY_MODIFY);

    while (!hasFile) {
      try {
        key = mWatcher.take();
      } catch (InterruptedException x) {
        return;
      }

      for (WatchEvent<?> event: key.pollEvents()) {
        WatchEvent.Kind kind = event.kind();

        if (kind == StandardWatchEventKinds.OVERFLOW) {
          continue;
        }

        // This generates a link warning and is noted in the javax mail api
        WatchEvent<Path> ev = (WatchEvent<Path>)event;
        Path foundFile = ev.context();

        System.out.format("Comparing %s to %s", filename, foundFile);
        if (!hasFile && filename.equals(foundFile.toString())) {
          hasFile = true;
          service.sendFileArrivedEmail(emailTo, filename);
        }
      }

      if (!key.reset()) {
        break;
      }
    }
  }
}