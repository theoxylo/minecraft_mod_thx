package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

public class ThxConfig
{
    static boolean ENABLE_LOGGING = false;
    
    Properties props;
    
    ThxConfig()
    {
        loadProperties();
    }
    
    void log(String s)
    {
        //mod_Thx.log(s);
        System.out.println("ThxConfig: " + s);
    }
    
    String getFilename()
    {
        //return ModLoader.getMinecraftInstance().getMinecraftDir() + "/mods/mod_thx_options.txt";
        return Minecraft.getMinecraftDir() + "/mods/mod_thx_options.txt";
    }
    
    String getProperty(String name)
    {
        if (props == null)
        {
            loadProperties();
        }
        return props.getProperty(name);
    }
    
    int getIntProperty(String name)
    {
        try
        {
            return Integer.parseInt(getProperty(name));
        }
        catch (Exception e)
        {
            log("Error loading int property '" + name + "'");
            return 0;
        }
    }
    
    boolean getBoolProperty(String name)
    {
        try
        {
            return Boolean.parseBoolean(getProperty(name));
        }
        catch (Exception e)
        {
            log("Boolean property '" + name + "' not found");
            return false;
        }
    }
    
    boolean ensureDefault(Properties props, String name, String defaultValue)
    {
        if (props.getProperty(name) == null)
        {
            log("adding default property value for " + name + ": " + defaultValue);
            props.setProperty(name, defaultValue);
            return true;
        }
        return false;
    }
    
    void loadProperties()
    {
        boolean writeFile = false;
        
        String filename = getFilename();
        
        props = new Properties();
        try
        {
            File file = new File(filename);
            log("Reading properties from file: " + file.getAbsolutePath());
            
            props.load(new FileInputStream(file));
        }
        catch(FileNotFoundException ioe1)
        {
            writeFile = true;
        }
        catch (Exception e)
        {
            log("Error loading properties: " + e);
        }
        
        writeFile = loadDefaults(props) || writeFile;
        
        log("Loaded properties: " + props);
        
        if (writeFile) // update file with defaults if any properties were missing
        {
            try
            {
                // create default properties file
                log("Creating/updating config file " + filename);
                props.store(new FileOutputStream(filename), "Added default properties");
            }
            catch(IOException e) { log("Error writing default properties file: " + e); }
        }
        
        ENABLE_LOGGING = getBoolProperty("enable_logging");
        log("logging enabled: " + ENABLE_LOGGING);
    }
    
    boolean loadDefaults(Properties props)
    {
        // add any missing properties using default values
        boolean defaultAdded = false;
        
        defaultAdded = ensureDefault(props, "enable_logging", "false") || defaultAdded;
        
        defaultAdded = ensureDefault(props, "key_ascend", Keyboard.getKeyName(Keyboard.KEY_SPACE)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_descend", Keyboard.getKeyName(Keyboard.KEY_X)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_forward", Keyboard.getKeyName(Keyboard.KEY_W)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_back", Keyboard.getKeyName(Keyboard.KEY_S)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_left", Keyboard.getKeyName(Keyboard.KEY_A)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_right", Keyboard.getKeyName(Keyboard.KEY_D)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_rotate_left", Keyboard.getKeyName(Keyboard.KEY_G)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_rotate_right", Keyboard.getKeyName(Keyboard.KEY_H)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_rocket_reload", Keyboard.getKeyName(Keyboard.KEY_I)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_look_pitch", Keyboard.getKeyName(Keyboard.KEY_L)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_auto_level", Keyboard.getKeyName(Keyboard.KEY_K)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_exit", Keyboard.getKeyName(Keyboard.KEY_Y)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_look_back", Keyboard.getKeyName(Keyboard.KEY_U)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_create_map", Keyboard.getKeyName(Keyboard.KEY_O)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_hud_mode", Keyboard.getKeyName(Keyboard.KEY_C)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_lock_alt", Keyboard.getKeyName(Keyboard.KEY_P)) || defaultAdded;
        
        defaultAdded = ensureDefault(props, "rotor_speed_percent", "70") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_look_yaw", "true") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_rotor", "true") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_auto_level", "true") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_auto_throttle_zero", "true") || defaultAdded;
        
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
