package net.minecraft.src;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ThxConfig extends ThxConfigBase
{
    ThxConfig()
    {
        loadProperties();
    }
    
    @Override
    String getFilename()
    {
        return "mods/mod_thx_options.cfg";
    }
    
    @Override
    boolean loadDefaults(Properties props)
    {
        boolean defaultAdded = false;
        
        // add any missing properties using default values
        defaultAdded = ensureDefault(props, "enable_logging", "false") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_logging_packets_inbound", "false") || defaultAdded;
        defaultAdded = ensureDefault(props, "recipe_format", " a ,a a,aaa") || defaultAdded;
        defaultAdded = ensureDefault(props, "recipe_items", "5") || defaultAdded;
        
        return defaultAdded;
    }
}
