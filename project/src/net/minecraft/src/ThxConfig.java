package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.lwjgl.input.Keyboard;

// controls and options set from mods/mod_thx_options.txt
public class ThxConfig
{
    private static Properties _PROPERTIES;
    
    static
    {
        boolean writeFile = false;
        
        //String filename = "/mods/mod_thx_options.txt"; // in 161, maps to c:\mods\mod_thx_options.txt
        String filename = "mods/mod_thx_options.txt"; // in 161, maps to c:\mods\mod_thx_options.txt
        
        _PROPERTIES = new Properties();
        try
        {
            File file = new File(filename);
            filename = file.getAbsolutePath();
            System.out.println("Reading properties from file: " + file.getAbsolutePath());
            
            _PROPERTIES.load(new FileInputStream(file));
        }
        catch(FileNotFoundException ioe1)
        {
            System.out.println("FileNotFoundException - " + ioe1);
            writeFile = true;
        }
        catch (Exception e)
        {
            System.out.println("Error loading properties: " + e);
        }
        
        writeFile = loadDefaults() || writeFile;
        
        System.out.println("ThxConfig - Loaded properties: " + _PROPERTIES);
        
        if (writeFile) // update file with defaults if any properties were missing
        {
            try
            {
                // create default properties file
                System.out.println("Creating/updating config file " + filename);
                _PROPERTIES.store(new FileOutputStream(filename), "Added default properties");
            }
            catch(IOException e) { System.out.println("Error writing default properties file: " + e); }
        }
    }
    
    public static boolean ENABLE_LOGGING = getBoolProperty("enable_logging");
    
    public static boolean ENABLE_AUTO_LEVEL = ThxConfig.getBoolProperty("enable_auto_level");
    public static boolean ENABLE_AUTO_THROTTLE_ZERO = ThxConfig.getBoolProperty("enable_auto_throttle_zero");
    public static boolean ENABLE_LOOK_YAW = ThxConfig.getBoolProperty("enable_look_yaw");
    
    public static int KEY_ASCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("key_ascend"));
    public static int KEY_DESCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("key_descend"));
    public static int KEY_FORWARD = Keyboard.getKeyIndex(ThxConfig.getProperty("key_forward"));
    public static int KEY_BACK = Keyboard.getKeyIndex(ThxConfig.getProperty("key_back"));
    public static int KEY_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_left"));
    public static int KEY_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_right"));
    public static int KEY_ROTATE_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_rotate_left"));
    public static int KEY_ROTATE_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_rotate_right"));
    public static int KEY_ROCKET_RELOAD = Keyboard.getKeyIndex(ThxConfig.getProperty("key_rocket_reload"));
    public static int KEY_LOOK_PITCH = Keyboard.getKeyIndex(ThxConfig.getProperty("key_look_pitch"));
    public static int KEY_AUTO_LEVEL = Keyboard.getKeyIndex(ThxConfig.getProperty("key_auto_level"));
    public static int KEY_EXIT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_exit"));
    public static int KEY_LOOK_BACK = Keyboard.getKeyIndex(ThxConfig.getProperty("key_look_back"));
    public static int KEY_CREATE_MAP = Keyboard.getKeyIndex(ThxConfig.getProperty("key_create_map"));
    public static int KEY_HUD_MODE = Keyboard.getKeyIndex(ThxConfig.getProperty("key_hud_mode"));
    public static int KEY_LOCK_ALT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_lock_alt"));
        
    public static void log(String s)
    {
        //mod_Thx.log(s);
        if (ENABLE_LOGGING) System.out.println("ThxConfig: " + s);
    }
    
    static String getProperty(String name)
    {
        return _PROPERTIES.getProperty(name);
    }
    
    public static int getIntProperty(String name)
    {
        try
        {
            return Integer.parseInt(getProperty(name));
        }
        catch (Exception e)
        {
            System.out.println("int property not found '" + name + "'");
            return 0;
        }
    }
    
    public static boolean getBoolProperty(String name)
    {
        try
        {
            return Boolean.parseBoolean(getProperty(name));
        }
        catch (Exception e)
        {
            System.out.println("Boolean property '" + name + "' not found");
            return false;
        }
    }
    
    // returns true if missing property was added and file needs to be written
    private static boolean ensureDefault(String name, String defaultValue)
    {
        if (_PROPERTIES.getProperty(name) == null)
        {
            log("adding default property value for " + name + ": " + defaultValue);
            _PROPERTIES.setProperty(name, defaultValue);
            return true;
        }
        return false;
    }
    
    private static boolean loadDefaults()
    {
        // add any missing properties using default values
        boolean defaultAdded = false;
        
        defaultAdded = ensureDefault("enable_logging", "false") || defaultAdded;
        
        defaultAdded = ensureDefault("key_ascend", Keyboard.getKeyName(Keyboard.KEY_SPACE)) || defaultAdded;
        defaultAdded = ensureDefault("key_descend", Keyboard.getKeyName(Keyboard.KEY_X)) || defaultAdded;
        defaultAdded = ensureDefault("key_forward", Keyboard.getKeyName(Keyboard.KEY_W)) || defaultAdded;
        defaultAdded = ensureDefault("key_back", Keyboard.getKeyName(Keyboard.KEY_S)) || defaultAdded;
        defaultAdded = ensureDefault("key_left", Keyboard.getKeyName(Keyboard.KEY_A)) || defaultAdded;
        defaultAdded = ensureDefault("key_right", Keyboard.getKeyName(Keyboard.KEY_D)) || defaultAdded;
        defaultAdded = ensureDefault("key_rotate_left", Keyboard.getKeyName(Keyboard.KEY_G)) || defaultAdded;
        defaultAdded = ensureDefault("key_rotate_right", Keyboard.getKeyName(Keyboard.KEY_H)) || defaultAdded;
        defaultAdded = ensureDefault("key_rocket_reload", Keyboard.getKeyName(Keyboard.KEY_I)) || defaultAdded;
        defaultAdded = ensureDefault("key_look_pitch", Keyboard.getKeyName(Keyboard.KEY_L)) || defaultAdded;
        defaultAdded = ensureDefault("key_auto_level", Keyboard.getKeyName(Keyboard.KEY_K)) || defaultAdded;
        defaultAdded = ensureDefault("key_exit", Keyboard.getKeyName(Keyboard.KEY_Y)) || defaultAdded;
        defaultAdded = ensureDefault("key_look_back", Keyboard.getKeyName(Keyboard.KEY_U)) || defaultAdded;
        defaultAdded = ensureDefault("key_create_map", Keyboard.getKeyName(Keyboard.KEY_O)) || defaultAdded;
        defaultAdded = ensureDefault("key_hud_mode", Keyboard.getKeyName(Keyboard.KEY_C)) || defaultAdded;
        defaultAdded = ensureDefault("key_lock_alt", Keyboard.getKeyName(Keyboard.KEY_P)) || defaultAdded;
        
        defaultAdded = ensureDefault("rotor_speed_percent", "70") || defaultAdded;
        defaultAdded = ensureDefault("enable_look_yaw", "true") || defaultAdded;
        defaultAdded = ensureDefault("enable_rotor", "true") || defaultAdded;
        defaultAdded = ensureDefault("enable_auto_level", "true") || defaultAdded;
        defaultAdded = ensureDefault("enable_auto_throttle_zero", "true") || defaultAdded;
        
        defaultAdded = ensureDefault("texture_helicopter", "textures/entity/helicopter.png") || defaultAdded;
        defaultAdded = ensureDefault("texture_missile", "textures/entity/missile.png") || defaultAdded;
        defaultAdded = ensureDefault("texture_rocket", "textures/entity/rocket.png") || defaultAdded;
        
        return defaultAdded;
    }
    
    /* java.util.logging approach...
    try
    {
        Handler handler = new FileHandler("mods/mod_thx.log");
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        
        String level = ThxConfig.getProperty("enable_logging_level", "SEVERE");
        System.out.println("thxLog.level: " + level);
        logger.setLevel(Level.parse(level));
    }
    catch (Exception e)
    {
        System.out.println("Could not open log file 'mods/mod_thx.log': " + e);
    }
    logger.fine("log fine test");
    logger.info("log info test");
    logger.warning("log warning test");
    */
}
