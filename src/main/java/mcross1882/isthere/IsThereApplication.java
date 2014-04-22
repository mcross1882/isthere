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
class IsThereApplication 
{
  protected static final String CONFIG_FILE = "config/main.config";
  
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
}
