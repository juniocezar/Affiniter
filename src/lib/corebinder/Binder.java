package corebinder;

import android.os.Bundle;

/**
 *  Binder class. This class contains static methods that
 *  can be linked against soot instrumented code, allowing
 *  an application (in Android case) to get command line
 *  arguments, as well as being able to modify its core 
 *  configuration.
 */

public class Binder {


  /**
   * Configuration Map.
   * The method maps a set of big and little cores to a
   * specific representation with hexadecimal values.
   * @param numBig is the number of big cores to be used   
   * @param numLITTLE is the number of LITTLE cores to be used   
   * @param android defines if the output string must be in android format
   */
  public static String configStr(int numBig, int numLITTLE, boolean android) {
    String config = (android) ? "" : "0x";
    switch (numBig) {
      case 0: config += "0"; break;
      case 1: config += "8"; break;
      case 2: config += "9"; break;
      case 3: config += "e"; break;
      case 4: config += "f"; break;
      default: {
        config += "f";
        System.err.println("Binder: Invalid num of bigs " + numBig);
      }
    }
    switch (numLITTLE) {
      case 0: config += "0"; break;
      case 1: config += "1"; break;
      case 2: config += "6"; break;
      case 3: config += "7"; break;
      case 4: config += "f"; break;
      default: {
        config += "f";
        System.err.println("Binder: Invalid num of LITTLEs " + numLITTLE);
      }
    }

    // if case of bad mask, fall back to all cores config
    if (numBig + numLITTLE == 0) {
      return (android) ? "ff" : "0xff";
    }

    return config;
  }


  /**
   * Configuration Map.
   * Defaults to linux mode, where the hex init 0x is included.
   * This is due to different taskset implementations in Android
   * linux kernel and traditional linux kernel.
   * @param extras application main activity extra bundle.   
   */
  public static String configStr(int numBig, int numLITTLE) {
    return configStr(numBig, numLITTLE, false);
  }


  /**
   * Global Configuration Binder.
   * Binds the whole application (including all threads)
   * to a specific core configuration passed as cli argument
   * @param extras application main activity extra bundle.   
   */
  public static void bind(String config, String op, int pid) {
    try {
      Runtime r = Runtime.getRuntime();

      // Build command line
      String cmd = "taskset " + op + " " + config + " " + pid;

      // Set the configuration of cores:
      Process p = r.exec(cmd);
      
      // Check for ls failure
      if (p.waitFor() != 0) {
        System.err.println("Binder: Error while binding core configuration.\n"+ 
          "CMD: " + cmd + "\n" + 
          "exit value = " + p.exitValue());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Global Configuration Binder.
   * Binds the whole application (including all threads)
   * to a specific core configuration passed as cli argument
   * @param extras application main activity extra bundle.   
   */
  public static void androidGlobalBinder(android.os.Bundle extras) 
                                        throws NumberFormatException {

    String config = "ff";

    if ( extras != null ) {
      if ( extras.containsKey ( "configuration" ) ) {
        config = extras.getString ( "configuration" );
      } else {
        if ( extras.containsKey ( "big" ) && extras.containsKey ( "little" )) {
          String big    = extras.getString ( "big" );
          String little = extras.getString ( "little" );
          config = configStr(Integer.parseInt(big), 
                                    Integer.parseInt(little), true);
        } else {
          System.err.println("Binder: Core configuration not defined. " 
            + "The param must be named 'configuration' or 'big' and 'little'.\n"
            + "Binder: Using default configuration ff (4b4L)");
        }
      }
    } else {
      System.err.println("Binder: Core configuration not defined. " 
          + "The param must be named 'configuration' or 'big' and 'little'.\n"
          + "Binder: Using default configuration ff (4b4L)");
    }

    // getting process id
    final int pid = android.os.Process.myPid();
    // possible exception is handled in method
    bind(config, "-pa", pid);
  }


  /**
   * Point Configuration Binder.
   * Binds a specific portion of code, running on a specific thread
   * to a core configuration passed as argument
   * @param config core configuration to bind current thread
   */
  public static void androidPointBinder(String config) {
    // getting thread specific id
    final int tid = android.os.Process.myTid();
    bind(config, "-p", tid);
  }
}