package net.minecraft.src;

import java.util.Properties;
import org.lwjgl.input.Keyboard;

public class ThxConfig extends ThxConfigBase
{
    @Override
    boolean loadDefaults(Properties props)
    {
        // add any missing properties using default values
        boolean defaultAdded = false;
        
        defaultAdded = ensureDefault(props, "enable_logging", "false") || defaultAdded;
        defaultAdded = ensureDefault(props, "enable_logging_p230_inbound", "false") || defaultAdded;
        
        defaultAdded = ensureDefault(props, "key_ascend", Keyboard.getKeyName(Keyboard.KEY_SPACE)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_descend", Keyboard.getKeyName(Keyboard.KEY_X)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_forward", Keyboard.getKeyName(Keyboard.KEY_W)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_back", Keyboard.getKeyName(Keyboard.KEY_S)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_left", Keyboard.getKeyName(Keyboard.KEY_A)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_right", Keyboard.getKeyName(Keyboard.KEY_D)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_rotate_left", Keyboard.getKeyName(Keyboard.KEY_G)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_rotate_right", Keyboard.getKeyName(Keyboard.KEY_H)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_fire_missile", Keyboard.getKeyName(Keyboard.KEY_M)) || defaultAdded;
        defaultAdded = ensureDefault(props, "key_fire_rocket", Keyboard.getKeyName(Keyboard.KEY_R)) || defaultAdded;
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
