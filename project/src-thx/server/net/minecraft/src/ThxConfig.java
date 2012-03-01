package net.minecraft.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.server.MinecraftServer;

public class ThxConfig
{
    static boolean ENABLE_LOGGING;
    static String filename = "mods/mod_thx_server.options";
    static Properties props;
    
    static String getProperty(String name)
    {
        if (props == null)
        {
            loadProperties();
        }
        return props.getProperty(name);
    }
    
    static int getIntProperty(String name)
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
    
    static boolean getBoolProperty(String name)
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
    
    static void loadProperties()
    {
        boolean writeFile = false;
        
        props = new Properties();
        try
        {
            props.load(new FileInputStream(filename));
        }
        catch(FileNotFoundException ioe1)
        {
            writeFile = true;
        }
        catch (Exception e)
        {
            log("Error loading properties: " + e);
        }
        
        // add any missing properties using default values
        writeFile = ensureDefault(props, "enable_logging", "false") || writeFile;
        writeFile = ensureDefault(props, "rotor_speed_percent", "70") || writeFile;
        writeFile = ensureDefault(props, "enable_look_yaw", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_drone_mode", "false") || writeFile;
        writeFile = ensureDefault(props, "enable_rotor", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_auto_level", "true") || writeFile;
        //writeFile = ensureDefault(props, "enable_look_down_trans", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_auto_throttle_zero", "true") || writeFile;
        
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
        
        //logKeyMap();
    }
    
    static boolean ensureDefault(Properties props, String name, String defaultValue)
    {
        if (props.getProperty(name) == null)
        {
            props.setProperty(name, defaultValue);
            return true;
        }
        return false;
    }
    
    static void log(String s)
    {
        System.out.println("mod_thx_server: " + s);
    }
}
