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
   * Path that points to the applications local home directory (OS dependent).
   *
   * @since 1.3
   */
  protected static String HOME_ENVIRONMENT_KEY = "ISTHERE_HOME";
  
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
  public IsThereApplication(String homeDirectory, String group)
  {
    String path = String.format("%s%s%s.conf", homeDirectory, DIRECTORY_SEPARATOR, group); 
    mConfig = ConfigFactory.parseFile(new File(path)).getConfig("emailSettings");
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
    
    String appHome = System.getenv(HOME_ENVIRONMENT_KEY);
    if (null == appHome || 0 == appHome.length()) {
        System.out.println(String.format("Warning: %s environment variable is not defined", HOME_ENVIRONMENT_KEY));
    }

    String filename = extractAndValidateBaseName(args);
    if (null == filename || 0 == filename.length()) {
      System.out.println("Warning: No file was specified aborting...");
      return;
    }
    
    String configName = "application";
    if (args.length >= 2) {
      configName = args[1];
    }

    try {
      IsThereApplication app = new IsThereApplication(appHome, configName);
      app.startFileWatcher(String.format("%s%s%s", appHome, DIRECTORY_SEPARATOR, filename), filename);
    } catch(Exception e) {
      System.err.println(String.format("Caught Exception %s: %s", e.toString(), e.getMessage()));
    }
  }
  
  /**
   * Extracts the base file name and validates that it is a directory
   *
   * @since  1.0
   * @param  args the command line arguments
   * @return the base name of the filepath
   */
  protected static String extractAndValidateBaseName(String[] args)
  {
    String filename = "";
    if (!args[0].contains(DIRECTORY_SEPARATOR)) {
      System.out.println("Warning: No directory specified defaulting to the current working directory.");
      filename = "." + DIRECTORY_SEPARATOR + args[0];
    }
    return filename.substring(filename.lastIndexOf(DIRECTORY_SEPARATOR)+1);
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
