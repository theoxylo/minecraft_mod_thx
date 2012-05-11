package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

abstract public class ThxConfigBase
{
    // to be implemented by client and server to add relevant defaults
    abstract boolean loadDefaults(Properties props);
    
    static boolean ENABLE_LOGGING;
    static boolean LOG_INCOMING_PACKETS;
    
    Properties props;
    
    abstract String getFilename();
    
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
        
        LOG_INCOMING_PACKETS = getBoolProperty("enable_logging_packets_inbound");
        log("inbound packet logging enabled: " + LOG_INCOMING_PACKETS);
    }
    
    void log(String s)
    {
        //mod_Thx.log(s);
        System.out.println("ThxConfig: " + s);
    }
}
