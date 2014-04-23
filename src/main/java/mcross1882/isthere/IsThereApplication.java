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
   * Static configuration path is always relative to execution directory
   * @since  1.0
   */
  protected static final String CONFIG_FILE = "config/main.config";

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
    HashMap<String, String> params = null;
    try {
      params = loadConfiguration();
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }

    FileWatcherService fileService = null;
    try {
      fileService = new FileWatcherService(args[0], FileSystems.getDefault().newWatchService());
    } catch (Exception e) {
      System.err.println(String.format("Caught Exception: %s", e.getMessage()));
      return;
    }

    System.out.println("Connecting to " + params.get("host") + " as " + params.get("user"));
    try {
      EmailService service = new EmailService(params.get("host"),
        params.get("user"),
        params.get("pass"),
        Integer.parseInt(params.get("port")));

      fileService.watchFile(service, params.get("emailTo"), args[0].substring(args[0].lastIndexOf("/")+1));

      service.close();
      fileService.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Reads a newline delimiter file containing
   * key-value pairs of configuration values
   *
   * @since    1.0
   * @access   protected
   * @modifier static
   * @throws   FileNotFoundException
   * @return   HashMap<String, String> Key-value configruation values
   */
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
}
