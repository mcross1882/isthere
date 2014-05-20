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
   * Static configuration path is always relative to execution directory
   * @since 1.0
   */
  protected static final String CONFIG_DIR = "config";
  
  /**
   * Required fields in the configuration file
   *
   * @since 1.3
   */
  protected static final String[] REQUIRED_FIELDS = {"host", "user", "pass", "port", "emailto", "emailfrom"};
  
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
    
    String configName = "main";
    if (args.length >= 2) {
      configName = args[1];
    }
    
    IsThereApplication app = new IsThereApplication();

    HashMap<String, String> params = null;
    try {
      params = app.loadConfiguration(configName);
      app.startFileWatcher(args[0], filename, params);
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
   * @param  params key value pair of imported settings
   */
  protected void startFileWatcher(String fullPath, String filename, HashMap<String,String> params)
  {
    System.out.println(String.format("Watching %s [%s]", fullPath, filename));
    FileWatcherService fileService = null;
    try {
      fileService = new FileWatcherService(fullPath, DIRECTORY_SEPARATOR, FileSystems.getDefault().newWatchService());

      EmailService service = new EmailService(params.get("host"),
        params.get("user"),
        params.get("pass"),
        Integer.parseInt(params.get("port")));
        
      fileService.watchFile(service, params.get("emailto"), params.get("emailfrom"), filename);
      
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
   * @param    name the configuration module to load
   * @throws   FileNotFoundException, Exception
   * @return   Key-value configruation values
   */
  protected HashMap<String, String> loadConfiguration(String name)
    throws FileNotFoundException, Exception
  {
    HashMap<String, String> params = new HashMap<String, String>();
    Scanner reader = new Scanner(getClass().getResourceAsStream(buildFilename(name)));

    while(reader.hasNext()) {
      setParameter(params, reader.nextLine());
    }
    reader.close();
    
    for (String field : REQUIRED_FIELDS) {
      if (!params.containsKey(field)) {
        throw new Exception(String.format("Missing %s field in configuration file\n", field));
      }
    }
    
    return params;
  }

  /**
   * Parses a parameter line where values are separted by :
   *
   * @since  1.0
   * @param  params the key value parameters
   * @param  line the line to split
   */
  protected void setParameter(HashMap<String, String> params, String line)
  {
    String[] fields = line.split(":");
    if (fields.length < 2) {
      return;
    }
    params.put(fields[0].trim().toLowerCase(), fields[1].trim());
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
  
  /**
   * Build a config filename
   *
   * @since   1.2
   * @param   name the module to load
   * @return  string fully qualified filename
   */
  protected static String buildFilename(String name)
  {
    return String.format("/%s.config", name);
  }
}
