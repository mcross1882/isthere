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
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import javax.mail.MessagingException;

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
  protected static final String[] REQUIRED_FIELDS = {
    "host", 
    "user", 
    "pass", 
    "port", 
    "emailto", 
    "emailfrom"
  };
  
  /**
   * Path that points to the applications local home directory (OS dependent).
   *
   * @since 1.3
   */
  protected static String HOME_ENVIRONMENT_KEY = "ISTHERE_HOME";
  
  /**
   * Root key value in the configuration file
   *
   * @since  1.4
   */
  protected static String ROOT_EMAIL_KEY = "emailSettings";
  
  /**
   * Applications home directory
   *
   *  @since  1.4
   */
  protected static String mHomeDirectory = "";
  
  /**
   * Configuration parameters defined in src/universal/conf/app.config
   * @since  1.3
   */
  protected Config mConfig = null;

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

    try {
      String configName = "application";
      String baseName = "";
      String fullPath = "";
      
      if (args.length >= 2) {
        configName = args[0];
        fullPath = args[1];
      } else {
        fullPath = args[0];
      }
      baseName = extractAndValidateBaseName(fullPath);
      
      IsThereApplication app = new IsThereApplication(configName);
      app.startFileWatcher(baseName, fullPath);
    } catch(Exception e) {
      System.err.println(String.format("Caught Exception %s: %s", e.toString(), e.getMessage()));
    }
  }
 
  /**
   * Constructs the main application with a specific group
   * configuration
   *
   * @since  1.3
   * @param baseConfigName the email configuration name to load
   */
  public IsThereApplication(String baseConfigName)
  {
    findApplicationDirectory();
    String path = resolveFilename(baseConfigName + ".conf");
    
    File configurationFile = new File(path);
    configurationFile.setReadOnly();
    
    mConfig = ConfigFactory
      .parseFile(configurationFile)
      .getConfig(ROOT_EMAIL_KEY);
  }
  
  /**
   * Finds the applications local home directory. This is
   * an OS dependent value and is determined by the
   * system environment
   *
   * @since  1.4
   */
  protected void findApplicationDirectory() throws Exception
  {
    mHomeDirectory = System.getenv(HOME_ENVIRONMENT_KEY);
    if (null == mHomeDirectory || 0 == mHomeDirectory.length()) {
        throw new Exception(String.format("Warning: %s environment variable is not defined", HOME_ENVIRONMENT_KEY));
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
    try {
      runFileWatcher(fullPath, filename);
    } catch (Exception e) {
      System.err.println(String.format("Caught Exception: %s", e.getMessage()));
    }
  }
  
  /**
   * Runs the file watcher and email service
   **
   * @since  1.4
   * @param  fullPath the absolute path to the file
   * @param  filename the file to load
   */
  protected void runFileWatcher(String fullPath, String filename)
    throws InterruptedException, MessagingException, IOException, Exception
  {
    FileWatcherService fileService = buildFileWatcherService(fullPath);
    EmailService service = buildEmailService();

    fileService.watchFile(
      service,
      mConfig.getString("emailTo"),
      mConfig.getString("emailFrom"),
      filename);

    service.close();
    fileService.close();
  }
  
  /**
   * Returns a filewatcher service with the specified fullPath
   *
   * @since  1.4
   * @param  fullPath the fully qualified filepath to watch
   */
  protected FileWatcherService buildFileWatcherService(String fullPath)
    throws IOException, Exception
  {
    return new FileWatcherService(fullPath, DIRECTORY_SEPARATOR, FileSystems.getDefault().newWatchService());
  }
  
  /**
   * Returns an authenticated email service for sending messages
   *
   * @since  1.4
   */
  protected EmailService buildEmailService()
    throws MessagingException
  {
    return new EmailService(
      mConfig.getString("host"),
      mConfig.getString("user"),
      mConfig.getString("pass"),
      mConfig.getInt("port"));
  }
  
  /**
   * Extracts the base file name and validates that it is a directory
   *
   * @since  1.0
   * @param  filename to append
   * @return the base name of the filepath
   */
  protected static String extractAndValidateBaseName(String filename)
    throws Exception
  {
    filename = prependPathToFilename(filename);
    if (null == filename || filename.length() == 0) {
      throw new Exception("Failed to extract a filename from argument.");
    }
    return filename;
  }
  
  /**
   * Prepends a relative path to the filename (if none exists)
   *
   * @since  1.4
   * @param  filename the filename to append
   * @return fully-qualified path
   */
  protected static String prependPathToFilename(String filename)
  {
    if (!filename.contains(DIRECTORY_SEPARATOR)) {
      System.out.println("Warning: No directory specified defaulting to the current working directory.");
      filename = "." + DIRECTORY_SEPARATOR + filename;
    }
    return filename;
  }
  
  /**
   * Builds an absolute path to the configuration file
   * specified by baseConfigName. This method does not
   * perform file checking so open files with caution.
   *
   * @since  1.4
   * @param  baseConfigName the configuration name to load
   * @return absolute path to the configuration file
   */
  protected static String resolveFilename(String baseConfigName)
  {
    return String.format("%s%s%s", mHomeDirectory, DIRECTORY_SEPARATOR, baseConfigName);
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
      + "Syntax: isthere [config] [file]\n"
      + "\n"
      + "config -- Optional configuration file to use\n"
      + "file   -- the file to watch for\n"
    );
  }
}
