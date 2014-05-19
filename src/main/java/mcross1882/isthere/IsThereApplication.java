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
import java.util.HashMap;
import java.util.Scanner;

/**
 * Main entry point for application
 *
 * @since  1.0
 * @access public
 * @author Matthew Cross <matthew@pmg.co>
 */
public class IsThereApplication
{
  /**
   * Static directory separator to use when building filepaths
   * @since    1.1
   * @access   protected
   * @modifier static final
   * @var      String
   */
  protected static final String DIRECTORY_SEPARATOR = System.getProperty("file.separator");
  
  /**
   * Static configuration path is always relative to execution directory
   * @since  1.0
   * @access   protected
   * @modifier static final
   * @var      String
   */
  protected static final String CONFIG_DIR = "config";
  
  /**
   * Application Start Point
   *
   * @since    1.0
   * @access   public
   * @param    String[] args command line arguments
   * @modifier static
   * @return   void
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
    
    String configName = "main";
    if (args.length == 2) {
      configName = args[1];
    }

    HashMap<String, String> params = null;
    try {
      params = loadConfiguration(configName);
    } catch(Exception e) {
      System.err.println(String.format("Failed to load configuration file: %s", e.getMessage()));
      return;
    }
    
    System.out.println(String.format("Watching %s [%s]", args[0], filename));
    FileWatcherService fileService = null;
    try {
      fileService = new FileWatcherService(args[0], DIRECTORY_SEPARATOR, FileSystems.getDefault().newWatchService());

      EmailService service = new EmailService(params.get("host"),
        params.get("user"),
        params.get("pass"),
        Integer.parseInt(params.get("port")));

      fileService.watchFile(service, params.get("emailTo"), params.get("emailFrom"), filename);
      
      service.close();
      fileService.close();
    } catch (Exception e) {
      System.err.println(String.format("Caught Exception: %s", e.getMessage()));
    }
  }

  /**
   * Reads a newline delimiter file containing
   * key-value pairs of configuration values
   *
   * @since    1.0
   * @access   protected
   * @modifier static
   * @param    name the configuration module to load
   * @throws   FileNotFoundException
   * @return   HashMap<String, String> Key-value configruation values
   */
  protected static HashMap<String, String> loadConfiguration(String name)
    throws FileNotFoundException
  {
    HashMap<String, String> params = new HashMap<String, String>();
    Scanner reader = new Scanner(new File(buildFilename(name)));

    while(reader.hasNext()) {
      setParameter(params, reader.nextLine());
    }
    reader.close();
    return params;
  }

  /**
   * Parses a parameter line where values are separted by :
   *
   * @since    1.0
   * @access   protected
   * @param    HashMap<String, String> parmater map
   * @param    String line the line to split
   * @modifier static
   * @return   void
   */
  protected static void setParameter(HashMap<String, String> params, String line)
  {
    String[] fields = line.split(":");
    if (fields.length < 2) {
      return;
    }
    params.put(fields[0].trim(), fields[1].trim());
  }

  /**
   * Prints the help dialog
   *
   * @since    1.1
   * @access   protected
   * @modifier static
   * @return   void
   */
  protected static void printHelp()
  {
    System.out.println(
        "isthere -- File notifications made simple\n"
      + "Website: https://github.com/mcross1882/isthere\n"
      + "----------------------------------------------\n"
      + "Syntax: isthere [file]\n"
    );
  }
  
  /**
   * Build a config filename
   *
   * @since   1.2
   * @access  protected
   * @param   name the module to load
   * @return  string fully qualified filename
   */
  protected static String buildFilename(String name)
  {
    return String.format("%s%s%s.config", CONFIG_DIR, DIRECTORY_SEPARATOR, name);
  }
}
