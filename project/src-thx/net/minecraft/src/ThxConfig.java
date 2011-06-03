package net.minecraft.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;

public class ThxConfig
{
    static boolean ENABLE_LOGGING;
    static String filename = Minecraft.getMinecraftDir() + "/mods/mod_thx.options";
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
            System.out.println("Error loading int property '" + name + "'");
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
            System.out.println("Error loading boolean property '" + name + "'");
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
            System.out.println("mod_thx: Error loading properties: " + e);
        }
        
        // add any missing properties using default values
        writeFile = ensureDefault(props, "enable_logging", "false") || writeFile;
        writeFile = ensureDefault(props, "ascend", Keyboard.getKeyName(Keyboard.KEY_SPACE)) || writeFile;
        writeFile = ensureDefault(props, "descend", Keyboard.getKeyName(Keyboard.KEY_X)) || writeFile;
        writeFile = ensureDefault(props, "forward", Keyboard.getKeyName(Keyboard.KEY_W)) || writeFile;
        writeFile = ensureDefault(props, "back", Keyboard.getKeyName(Keyboard.KEY_S)) || writeFile;
        writeFile = ensureDefault(props, "left", Keyboard.getKeyName(Keyboard.KEY_A)) || writeFile;
        writeFile = ensureDefault(props, "right", Keyboard.getKeyName(Keyboard.KEY_D)) || writeFile;
        writeFile = ensureDefault(props, "rotate_left", Keyboard.getKeyName(Keyboard.KEY_G)) || writeFile;
        writeFile = ensureDefault(props, "rotate_right", Keyboard.getKeyName(Keyboard.KEY_H)) || writeFile;
        writeFile = ensureDefault(props, "key_fire_missile", Keyboard.getKeyName(Keyboard.KEY_M)) || writeFile;
        writeFile = ensureDefault(props, "key_fire_rocket", Keyboard.getKeyName(Keyboard.KEY_R)) || writeFile;
        writeFile = ensureDefault(props, "key_hud_mode", Keyboard.getKeyName(Keyboard.KEY_L)) || writeFile;
        writeFile = ensureDefault(props, "key_auto_level", Keyboard.getKeyName(Keyboard.KEY_K)) || writeFile;
        writeFile = ensureDefault(props, "rotor_speed_percent", "60") || writeFile;
        writeFile = ensureDefault(props, "enable_look_yaw", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_look_pitch", "false") || writeFile;
        writeFile = ensureDefault(props, "enable_drone_mode", "false") || writeFile;
        writeFile = ensureDefault(props, "enable_pilot_aim", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_rotor", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_auto_level", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_look_down_trans", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_auto_throttle_zero", "true") || writeFile;
        writeFile = ensureDefault(props, "enable_heavy_weapons", "false") || writeFile;
        
        System.out.println("mod_thx: Loaded properties: " + props);
        
        if (writeFile) // update file with defaults if any properties were missing
        {
	        try
	        {
	            // create default properties file
	            System.out.println("mod_thx: Creating/updating config file " + filename);
	            props.store(new FileOutputStream(filename), "Added default properties");
	        }
	        catch(IOException e) { System.out.println("mod_thx: Error writing default properties file: " + e); }
        }
        
        ENABLE_LOGGING = getBoolProperty("enable_logging");
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
    
    static void logKeyMap()
    {
        log("Key names:");
        for (int i = 0; i < 200; i++)
        {
            log("Key " + i + ": " + Keyboard.getKeyName(i));
        }
    }
    
    static void log(String s)
    {
        if (ENABLE_LOGGING) System.out.println(new java.util.Date() + " [mod_thx]: " + s);
    }
}
