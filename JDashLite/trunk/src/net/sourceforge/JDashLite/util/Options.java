package net.sourceforge.JDashLite.util;

import waba.io.*;
import superwaba.ext.xplat.util.props.Properties;

/**
 * 
 * NOTE:!!!  This is a copy and paste from the SuperWaba Options.java class.  I wanted to use it's exact functionality, but
 * saw no need to re-write it from scratch.  The only catch was I need to expose the constructor.
 * 
 * The game options management class. <br>
 * <br>
 * The options are stored in a waba Catalog object which is linked to the
 * application through the software creatorID. This causes the options
 * database deletion when the game with the same creatorID is erased.
 * <p>
 * The complete database name is composed by the game name 'appl' and its creatorID, both
 * provided during the engine initialization via the GameEngineClient interface.<br>
 * The options database name is : ${appl}_OPT.${creatorID}.DATA
 * <p>
 * You can find a complete game API sample named 'Ping' in the SW examples folder.<br>
 * Here is some sample code:
 *
 * <pre>
 * import superwaba.ext.xplat.game.*;
 * import superwaba.ext.xplat.util.props.*;
 * ...
 *
 * <i>public class Ping extends <U>GameEngine</U></i> {
 *
 * // constructor
 * public Ping()
 * {
 *   waba.sys.Settings.setPalmOSStyle(true);
 *
 *   // define the game API setup attributes
 *
 *   gameName             = "Ping";
 *
 *   // when not run on device, appCreatorId does not always return the same value.
 *
 *   gameCreatorID        = Settings.onDevice ? waba.sys.Settings.appCreatorId:"PiNg";
 *
 *   gameVersion          = 100;   // v1.00
 *   gameHighscoresSize   = 7;     // store the best 7 highscores
 *   gameRefreshPeriod    = 75;    // 75 ms refresh periods
 *   gameIsDoubleBuffered = true;  // used double buffering to prevent flashing display
 *   gameDoClearScreen    = true;  // screen is cleared before each frame display
 *   gameHasUI            = false; // no UI elements, frame displays are optimized
 *   ...
 * }
 *
 * // declare 2 game settings
 *
 * protected <B>Properties.Str</B>      optUserName;
 * protected <B>Properties.Boolean</B>  optSound;
 *
 * //---------------------------------------------------------
 * // overload the API's game init event.
 * // this function is called when the game is launched.
 * //---------------------------------------------------------
 *
 * <i>public void onGameInit()</i> {
 *
 *   // access the game settings: 'username' & 'sound'
 *   // if the properties do not yet exist, the default values are used
 *
 *   <B>Options</B> settings=<U>getOptions</U>();
 *
 *   optUserName = settings.<B>declareString</B>    ("userName","noname");
 *   optSound    = settings.<B>declareBoolean</B>   ("sound",false);
 *   ...
 *
 *   if (optSound.<B>value</B>) Sound.tone(1520,10);
 *
 *  }
 * }
 * </pre>
 * @author Frank Diebolt
 * @version 1.0
 */

public class Options extends Properties
{
  private Catalog cat;
  private final static String dbName_suffix = "_OPT.";
  private final static String dbType        = ".DATA";
  private static final String duplicatedProperty = "OPT:duplicated:";

  public Options(String prefix, String createorID)
  {
    cat = new Catalog(prefix+dbName_suffix+createorID+dbType,Catalog.CREATE);
    if (!cat.isOpen())
      throw new RuntimeException("OPT:access error");


    try {
    if (cat.setRecordPos(0))
    {
       DataStream ds = new DataStream(cat);
       oldVersion = ds.readInt();
       load(ds);
    }
    } catch (IOError e) {}
  }

  /**
   * Declare a boolean option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   * @param name the property name.
   * @param value the property default value.
   * @return a boolean property object.
   */
  public Boolean declareBoolean(String name,boolean value)
  {
    Value v=get(name);
    if (v!=null)
    {
      if (v.type==Boolean.TYPE)
         return (Boolean)v;
      throw new RuntimeException(duplicatedProperty+name);
    }
    Boolean b=new Boolean(value);
    put(name,b);
    return b;
  }

  /**
   * Declare a long option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   * @param name the property name.
   * @param value the property default value.
   * @return a long property object.
   */
  public Long declareLong(String name, long value)
  {
    Value v=get(name);
    if (v!=null)
    {
      if (v.type==Long.TYPE)
         return (Long)v;
      throw new RuntimeException(duplicatedProperty+name);
    }
    Long b=new Long(value);
    put(name,b);
    return b;
  }

  /**
   * Declare an integer option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   * @param name the property name.
   * @param value the property default value.
   * @return an integer property object.
   */
  public Int declareInteger (String name,int value)
  {
    Value v=get(name);
    if (v!=null)
    {
      if (v.type==Int.TYPE)
         return (Int)v;
      throw new RuntimeException(duplicatedProperty+name);
    }
    Int i=new Int(value);
    put(name,i);
    return i;
  }

  /**
   * Declare a double option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   * @param name the property name.
   * @param value the property default value.
   * @return a double property object.
   */
  public Double declareDouble (String name,double value)
  {
    Value v=get(name);
    if (v!=null)
    {
      if (v.type==Double.TYPE)
         return (Double)v;
      throw new RuntimeException(duplicatedProperty+name);
    }
    Double d=new Double(value);
    put(name,d);
    return d;
  }

  /**
   * Declare a string option.<br>
   * This function tries to lookup the property in the Options database.<br>
   * If it fails, a new property with the default value specified is created.
   * @param name the property name.
   * @param value the property default value.
   * @return a string property object.
   */
  public Str declareString (String name,String value)
  {
    Value v=get(name);
    if (v!=null)
    {
      if (v.type==Str.TYPE)
         return (Str)v;
      throw new RuntimeException(duplicatedProperty+name);
    }
    Str s = new Str(value);
    put(name,s);
    return s;
  }

  /**
   * The options database new version number.
   */
  public int newVersion;
  /**
   * The options database old version number.
   */
  public int oldVersion;

  /**
   * Get a property value given the key.
   * @param key name of the property
   * @return Value that can be casted to Properties.Str, Properties.Int,...
   * depending on the value type, that can be retrieved with the <code>type</code> read-only property.
   **/
  public Properties.Value getProp(String key)
  {
    return get(key);
  }

  /**
   * stores the settings database. <br>
   * @return false if an error occurs
   */
  public boolean save()
  {
    // use this nice object to resize the options record
     ResizeRecord rs=new ResizeRecord(cat,100);

    try {if (!rs.restartRecord(0))
      return false;
    } catch (IOError e) {return false;}

    DataStream ds=new DataStream(rs);
    ds.writeInt(newVersion);

    save(ds);

    return true;
  }

  /**
   * closes the settings database.
   * @return true if succeeded.
   */
  public boolean close()
  {
    if (!save())
       return false;
    return cat.close();
  }
}
