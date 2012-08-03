package net.minecraft.server;

import java.util.Properties;

public class ThxConfig extends ThxConfigBase
{
    ThxConfig()
    {
        this.loadProperties();
    }

    String getFilename()
    {
        return "mods/mod_thx_options.cfg";
    }

    boolean loadDefaults(Properties var1)
    {
        boolean var2 = false;
        var2 = this.ensureDefault(var1, "enable_logging", "false") || var2;
        var2 = this.ensureDefault(var1, "enable_logging_packets_inbound", "false") || var2;
        var2 = this.ensureDefault(var1, "recipe_format", " a ,a a,aaa") || var2;
        var2 = this.ensureDefault(var1, "recipe_items", "5") || var2;
 
        return var2;
    }
}
