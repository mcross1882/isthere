/**
 * isthere file notifier
 *
 * @since     1.0
 * @package   isthere
 * @copyright 2014
 * @license   See LICENSE
 */
package mcross1882.isthere;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Main entry point for application
 *
 * @since  1.0
 * @author Matthew Cross <matthew@pmg.co>
 */
public class IsThereApplication
{
  /**
   * Static directory separator to use when building filepaths
   * @since 1.1
   */
  protected static final String DIRECTORY_SEPARATOR = System.getProperty("file.separator");
  
  /**
   * Required fields in the configuration file
   *
   * @since 1.3
   */
  protected static final String[] REQUIRED_FIELDS = {"host", "user", "pass", "port", "emailto", "emailfrom"};
  
  /**
   * Configuration parameters defined in src/universal/conf/app.config
   * @since  1.3
   */
  protected Config mConfig = null;
  
  /**
   * Constructs the main application with a specific group
   * configuration
   *
   * @since  1.3
   */
  public IsThereApplication(String group)
  {
    mConfig = ConfigFactory.load(group).getConfig("emailSettings");
  }
  
  /**
   * Application Start Point
   *
   * @since  1.0
   * @param  args command line arguments
   */
  public static void main(String[] args)
  {
    if (args.length < 1) {
      printHelp();
      return;
    }

    if (!args[0].contains(DIRECTORY_SEPARATOR)) {
      System.out.println("Warning: No directory specified defaulting to the current working directory.");
      args[0] = "." + DIRECTORY_SEPARATOR + args[0];
    }

    String filename = args[0].substring(args[0].lastIndexOf(DIRECTORY_SEPARATOR)+1);
    if (null == filename || 0 == filename.length()) {
      System.out.println("Warning: No file was specified aborting...");
      return;
    }
    
    String configName = "application";
    if (args.length >= 2) {
      configName = args[1];
    }
    
    IsThereApplication app = new IsThereApplication(configName);

    HashMap<String, String> params = null;
    try {
      app.startFileWatcher(args[0], filename);
    } catch(Exception e) {
      System.err.println(String.format("Caught Exception %s: %s", e.toString(), e.getMessage()));
      return;
    }
  }
  
  /**
   * Starts the file watcher service until the resource appears
   *
   * @since  1.3
   * @param  fullPath the absolute path to the file
   * @param  filename the file to load
   */
  protected void startFileWatcher(String fullPath, String filename)
  {
    System.out.println(String.format("Watching %s [%s]", fullPath, filename));
    FileWatcherService fileService = null;
    try {
      fileService = new FileWatcherService(fullPath, DIRECTORY_SEPARATOR, FileSystems.getDefault().newWatchService());

      EmailService service = new EmailService(mConfig.getString("host"),
        mConfig.getString("user"),
        mConfig.getString("pass"),
        mConfig.getInt("port"));
        
      fileService.watchFile(service, 
        mConfig.getString("emailTo"), 
        mConfig.getString("emailFrom"), 
        filename);
      
      service.close();
      fileService.close();
    } catch (Exception e) {
      System.err.println(String.format("Caught Exception: %s", e.getMessage()));
    }
  }

  /**
   * Prints the help dialog
   *
   * @since    1.1
   */
  protected static void printHelp()
  {
    System.out.println(
        "isthere -- File notifications made simple\n"
      + "Website: https://github.com/mcross1882/isthere\n"
      + "----------------------------------------------\n"
      + "Syntax: isthere [file] [config]\n"
      + "\n"
      + "file   -- the file to watch for\n"
      + "config -- Optional configuration file to use\n"
    );
  }
}
