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
    static boolean LOG_INCOMING_PACKETS = false;
    
    // 'true' will allow client-side master entity, sending update packets to server. server updates its entity and send updates to all clients except the pilot player source client
    // 'false' for normal mc/ml behavior with no mod to obfuscated files (no modloader minecraft.jar changes, just mods/mod.zip to install)
    static boolean CLIENT_DRIVEN = false; 

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
        
        LOG_INCOMING_PACKETS = getBoolProperty("enable_logging_p250_inbound");
        log("inbound packet 250 logging enabled: " + LOG_INCOMING_PACKETS);
    }
    
    boolean loadDefaults(Properties props)
    {
        // add any missing properties using default values
        boolean defaultAdded = false;
        
        defaultAdded = ensureDefault(props, "enable_logging", "false") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_logging_p250_inbound", "false") || defaultAdded;
        
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
}
