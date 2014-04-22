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
 * Main entry point for application
 *
 * @since  1.0
 * @access public
 * @author Matthew Cross <matthew@pmg.co>
 */
class IsThereApplication 
{
  protected static final String CONFIG_FILE = "config/main.config";
  
  protected static WatchService mWatcher = null;
  
  protected static Path mStartingDirectory;
  
  public static void main(String[] args)
  {
    HashMap<String, String> params = null;
    try {
      mWatcher = FileSystems.getDefault().newWatchService();
      // Resolve the base directory without the filename
      mStartingDirectory = Paths.get(args[0].substring(0, args[0].lastIndexOf("/")) + "/");
      
      params = loadConfiguration();
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }
    
    System.out.println("Connecting to " + params.get("host") + " as " + params.get("user"));
    try {
      EmailService service = new EmailService(params.get("host"), 
        params.get("user"), 
        params.get("pass"), 
        Integer.parseInt(params.get("port")));

        watchFile(service, params.get("emailTo"), args[0].substring(args[0].lastIndexOf("/")+1));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected static HashMap<String, String> loadConfiguration()
    throws FileNotFoundException
  {
    HashMap<String, String> params = new HashMap<String, String>();
    Scanner reader = new Scanner(new File(CONFIG_FILE));
    
    while(reader.hasNext()) {
      setParameter(params, reader.nextLine());
    }
    reader.close();
    return params;
  }
  
  protected static void setParameter(HashMap<String, String> params, String line)
  {
    String[] fields = line.split(":");
    if (fields.length < 2) {
      return;
    }
    params.put(fields[0].trim(), fields[1].trim());
  }
  
  protected static void watchFile(EmailService service, String emailTo, String filename)
    throws MessagingException, IOException
  {
    boolean hasFile = true;
    File file = new File(filename);
    
    if (!file.exists() && !file.isDirectory()) {
      hasFile = false;
      sendFileMissingEmail(service, emailTo, filename);
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

        WatchEvent<Path> ev = (WatchEvent<Path>)event;
        Path foundFile = ev.context();

        System.out.format("Comparing %s to %s", filename, foundFile);
        if (filename.equals(foundFile.toString())) {
          hasFile = true;
          sendFileArrivedEmail(service, emailTo, filename);
        }
      }

      if (!key.reset()) {
        break;
      }
    }
  }
  
  protected static void sendFileMissingEmail(EmailService service, String to, String filename)
    throws MessagingException
  {
    service.connect();
    service.sendEmail(new Email()
      .setTo(to)
      .setFrom("KoddiFileWatcher")
      .setSubject("File is missing [%s]".format(filename))
      .setMessage("The file " + filename + " is currently not available. When it arrives I will notify you again.")
    );
    service.close();
  }
  
  protected static void sendFileArrivedEmail(EmailService service, String to, String filename)
    throws MessagingException
  {
    service.connect();
    service.sendEmail(new Email()
      .setTo(to)
      .setFrom("KoddiFileWatcher")
      .setSubject("File has arrived " + filename)
      .setMessage("The file " + filename + " has arrived and is ready for processing.")
    );
    service.close();
  }
}
