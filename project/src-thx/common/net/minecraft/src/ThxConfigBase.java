package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;

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

    public void addHelicopterRecipe(ItemStack itemStack)
    {
        Object[] recipe = loadHelicopterRecipe();

        log("Adding recipe for helicopter");
        ModLoader.addRecipe(itemStack, recipe);
    }

    private Object[] loadHelicopterRecipe()
    {
        final Object[] defaultRecipe = new Object[] { " X ", "X X", "XXX", Character.valueOf('X'), Block.planks };
        ArrayList<Object> recipeArray = new ArrayList<Object>();

        String[] format = getProperty("recipe_format").split(",");
        String[] itemStrings = getProperty("recipe_items").split(",");

        // " X ", "X X", "XXX",
        for (String formatLine: format) {
            recipeArray.add(formatLine);
        }

        // Character.valueOf('X'), Block.planks
        char code = 'a';

        for (String itemString: itemStrings) {
            int id, damage;
            try {
                if (itemString.contains(":")) {
                    String[] parts = itemString.split(":", 2);
                    id = Integer.parseInt(parts[0]);
                    damage = Integer.parseInt(parts[1]);
                } else {
                    id = Integer.parseInt(itemString);
                    damage = -1;
                }
            } catch (Exception e) { 
                System.out.println("Invalid item specification: " + itemString + ": " + e + ", using default recipe");
                return defaultRecipe;
            }

            ItemStack item = new ItemStack(id, 1, damage);
            if (item == null) {
                System.out.println("No such item for crafting: " + id + ":" + damage + ", using default recipe");
                return defaultRecipe;
            }

            recipeArray.add(Character.valueOf(code));
            recipeArray.add(item);
            
            code += 1;
        }

        log("recipeArray = " + recipeArray);

        return recipeArray.toArray();
    }
    
    void log(String s)
    {
        //mod_Thx.log(s);
        System.out.println("ThxConfig: " + s);
    }
}
