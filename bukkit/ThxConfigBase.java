package net.minecraft.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class ThxConfigBase
{
    static boolean ENABLE_LOGGING;
    static boolean LOG_INCOMING_PACKETS;
    Properties props;

    abstract boolean loadDefaults(Properties var1);

    abstract String getFilename();

    String getProperty(String var1)
    {
        if (this.props == null)
        {
            this.loadProperties();
        }

        return this.props.getProperty(var1);
    }

    int getIntProperty(String var1)
    {
        try
        {
            return Integer.parseInt(this.getProperty(var1));
        }
        catch (Exception var3)
        {
            this.log("Error loading int property \'" + var1 + "\'");
            return 0;
        }
    }

    boolean getBoolProperty(String var1)
    {
        try
        {
            return Boolean.parseBoolean(this.getProperty(var1));
        }
        catch (Exception var3)
        {
            this.log("Boolean property \'" + var1 + "\' not found");
            return false;
        }
    }

    boolean ensureDefault(Properties var1, String var2, String var3)
    {
        if (var1.getProperty(var2) == null)
        {
            this.log("adding default property value for " + var2 + ": " + var3);
            var1.setProperty(var2, var3);
            return true;
        }
        else
        {
            return false;
        }
    }

    void loadProperties()
    {
        boolean var1 = false;
        String var2 = this.getFilename();
        this.props = new Properties();

        try
        {
            File var3 = new File(var2);
            this.log("Reading properties from file: " + var3.getAbsolutePath());
            this.props.load(new FileInputStream(var3));
        }
        catch (FileNotFoundException var5)
        {
            var1 = true;
        }
        catch (Exception var6)
        {
            this.log("Error loading properties: " + var6);
        }

        var1 = this.loadDefaults(this.props) || var1;
        this.log("Loaded properties: " + this.props);

        if (var1)
        {
            try
            {
                this.log("Creating/updating config file " + var2);
                this.props.store(new FileOutputStream(var2), "Added default properties");
            }
            catch (IOException var4)
            {
                this.log("Error writing default properties file: " + var4);
            }
        }

        ENABLE_LOGGING = this.getBoolProperty("enable_logging");
        this.log("logging enabled: " + ENABLE_LOGGING);
        LOG_INCOMING_PACKETS = this.getBoolProperty("enable_logging_packets_inbound");
        this.log("inbound packet logging enabled: " + LOG_INCOMING_PACKETS);
    }

    void log(String var1)
    {
        System.out.println("ThxConfig: " + var1);
    }
}
