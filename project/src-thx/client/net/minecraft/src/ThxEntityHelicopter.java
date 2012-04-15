package net.minecraft.src;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

public class ThxEntityHelicopter extends ThxEntityHelicopterBase implements ISpawnable
{    
    Minecraft minecraft;
    
    // controls and options
    // set from mod_thx.properties
    static int KEY_ASCEND = Keyboard.getKeyIndex(mod_Thx.getProperty("key_ascend"));
    static int KEY_DESCEND = Keyboard.getKeyIndex(mod_Thx.getProperty("key_descend"));
    static int KEY_FORWARD = Keyboard.getKeyIndex(mod_Thx.getProperty("key_forward"));
    static int KEY_BACK = Keyboard.getKeyIndex(mod_Thx.getProperty("key_back"));
    static int KEY_LEFT = Keyboard.getKeyIndex(mod_Thx.getProperty("key_left"));
    static int KEY_RIGHT = Keyboard.getKeyIndex(mod_Thx.getProperty("key_right"));
    static int KEY_ROTATE_LEFT = Keyboard.getKeyIndex(mod_Thx.getProperty("key_rotate_left"));
    static int KEY_ROTATE_RIGHT = Keyboard.getKeyIndex(mod_Thx.getProperty("key_rotate_right"));
    static int KEY_FIRE_MISSILE = Keyboard.getKeyIndex(mod_Thx.getProperty("key_fire_missile"));
    static int KEY_FIRE_ROCKET = Keyboard.getKeyIndex(mod_Thx.getProperty("key_fire_rocket"));
    static int KEY_ROCKET_RELOAD = Keyboard.getKeyIndex(mod_Thx.getProperty("key_rocket_reload"));
    static int KEY_LOOK_PITCH = Keyboard.getKeyIndex(mod_Thx.getProperty("key_look_pitch"));
    static int KEY_AUTO_LEVEL = Keyboard.getKeyIndex(mod_Thx.getProperty("key_auto_level"));
    static int KEY_EXIT = Keyboard.getKeyIndex(mod_Thx.getProperty("key_exit"));
    static int KEY_LOOK_BACK = Keyboard.getKeyIndex(mod_Thx.getProperty("key_look_back"));
    static int KEY_CREATE_MAP = Keyboard.getKeyIndex(mod_Thx.getProperty("key_create_map"));
    static int KEY_HUD_MODE = Keyboard.getKeyIndex(mod_Thx.getProperty("key_hud_mode"));
    static int KEY_LOCK_ALT = Keyboard.getKeyIndex(mod_Thx.getProperty("key_lock_alt"));
    
        
    static boolean ENABLE_AUTO_LEVEL = mod_Thx.getBoolProperty("enable_auto_level");
    static boolean ENABLE_AUTO_THROTTLE_ZERO = mod_Thx.getBoolProperty("enable_auto_throttle_zero");
    static boolean ENABLE_LOOK_YAW = mod_Thx.getBoolProperty("enable_look_yaw");
        
    int prevViewMode; // = 2; // 2 is looking back
    
    float smokeDelay;
    
    public ThxEntityHelicopter(World world)
    {
        super(world);
        helper = new ThxEntityHelperClient(this, new ThxModelHelicopter());
	    minecraft = ModLoader.getMinecraftInstance();
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        setPositionAndRotation(x, y + yOffset, z, yaw, 0f);
    }

    public Entity getPilot()
    {
        //return (EntityPlayer) riddenByEntity;
        return riddenByEntity;
    }

    @Override
    public void onUpdate()
    {
        int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        plog(String.format("start  onUpdate, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
        
        super.onUpdate();
		
        // try sending update packet in all cases! bandwidth?
        helper.sendUpdatePacketToServer(getUpdatePacket());
            
        // create smoke to indicate damage
        smokeDelay -= deltaTime;
        if (smokeDelay < 0f)
        {
            smokeDelay = 1f - damage / MAX_HEALTH;
            if (smokeDelay > .4f) smokeDelay = .4f;

            for (int i = (int) (10f * damage / MAX_HEALTH); i > 0; i--)
            {
                if (i % 2 == 0) worldObj.spawnParticle("smoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
                else worldObj.spawnParticle("largesmoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
                
                //if (i > 6) break;
            }
            if (damage / MAX_HEALTH > .75f) worldObj.spawnParticle("flame", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
        }
        
        riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        plog(String.format("finish onUpdate, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
    }
    
    @Override
    void onUpdatePilot()
    {
        if (!minecraft.thePlayer.equals(riddenByEntity))
        {
	        // piloted by other player mp client, so already updated by server packet
        
	        // adjust model rotor speed according to throttle
	        float power = (throttle - THROTTLE_MIN) / (THROTTLE_MAX - THROTTLE_MIN);
	        ((ThxModelHelicopter) helper.model).rotorSpeed = power / 2f + .75f;
        
            return;
        }
        
        // player is the pilot
        Entity pilot = getPilot();
        
        //if (!worldObj.isRemote && pilot != null && pilot.isDead)
        //{
            //riddenByEntity = null;
            //pilot.mountEntity(this); // unmount
        //}
        if (riddenByEntity.isDead)
        {
            pilotExit();
            return;
        }
            
        if (onGround) // very slow on ground
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral

            // double apply friction when on ground
            motionX *= FRICTION;
            motionY = 0.0;
            motionZ *= FRICTION;
        }
        else if (inWater)
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral
                
            motionX *= .7;
            motionY *= .7;
            motionZ *= .7;
            
            // float up
            motionY += 0.01;
        }
                        
        // short-circuit if any GUI is open (including console or chat) to prevent keyboard commands
        if (minecraft.currentScreen != null) return;
        
        createMapDelay -= deltaTime;
        if (Keyboard.isKeyDown(KEY_CREATE_MAP) && createMapDelay < 0f && pilot != null)
        {
            createMapDelay = 10f; // the delay in seconds
                
            if (worldObj.isRemote)
            {
                cmd_create_map = 1;
            }
            else
            {
	            createMap();
            }
        }
            
        hudModeToggleDelay -= deltaTime;
        lookPitchToggleDelay -= deltaTime;
            
        if (Keyboard.isKeyDown(KEY_LOOK_BACK)) // look back while key is HELD (not toggle), switch to 3rd-person REVERSE view
        {
            //view modes: 0 = 1st-person, 1 = 3rd-person chase, 2 = looking back in third person (RE style)
            
            // remember current view mode and switch to reverse (only if not already)
            if (minecraft.gameSettings.thirdPersonView != 2)
            {
                log("prevViewMode: " + prevViewMode);
                prevViewMode = minecraft.gameSettings.thirdPersonView; 
                
                minecraft.gameSettings.thirdPersonView = 2;
            }
        }
        else if (prevViewMode != 2 && minecraft.gameSettings.thirdPersonView == 2) // return to original view after look back key released
        {
            minecraft.gameSettings.thirdPersonView = prevViewMode;
            prevViewMode = 2;
        }
        else if (Keyboard.isKeyDown(KEY_LOOK_PITCH) && lookPitchToggleDelay < 0f)
        {
            lookPitchToggleDelay = .5f;
                
            if (!lookPitch)
            {
                lookPitch = true;

                // reset level offset
                lookPitchZeroLevel = 0f;

                // show status message
                helper.addChatMessageToPilot("Look-Pitch:  ON, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
            }
            else
            {
                // turn off cockpit
                lookPitch = false;
                helper.addChatMessageToPilot("Look-Pitch: OFF, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
            }
        }
        else if (Keyboard.isKeyDown(KEY_HUD_MODE) && hudModeToggleDelay < 0f && pilot != null)
        {
            hudModeToggleDelay = .5f;
                
            ThxModel model = (ThxModel) helper.model;
            if (model.visible)
            {
                // change to 1st-person and hide model
                if (minecraft.gameSettings.thirdPersonView != 0) minecraft.gameSettings.thirdPersonView = 0;
                model.visible = false;
            }
            else
            {
                model.visible = true;
	            helper.addChatMessageToPilot("Cam 1, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
            }
        }
        else if (minecraft.gameSettings.thirdPersonView == 2) // must hold look back key, not a toggle, so cancel here
        {
            minecraft.gameSettings.thirdPersonView = 0;
        }
            
        // view could be switched by player using F5
        if (minecraft.gameSettings.thirdPersonView != 0) ((ThxModel) helper.model).visible = true;

        altitudeLockToggleDelay -= deltaTime;
        if (Keyboard.isKeyDown(KEY_LOCK_ALT) && altitudeLockToggleDelay < 0f && pilot != null)
        {
            altitudeLockToggleDelay = .5f;
            altitudeLock = !altitudeLock;
        }

        // PILOT EXIT
        pilotExitDelay -= deltaTime;
        if (Keyboard.isKeyDown(KEY_EXIT) && pilot != null)
        {
            pilotExitDelay = .5f; // the delay in seconds
            if (worldObj.isRemote) cmd_exit = 1; // queue for server packet
            pilotExit();
        }

        // MANUAL ROCKET RELOAD 
        if (Keyboard.isKeyDown(KEY_ROCKET_RELOAD) && rocketCount > 0)
        {
	        if (worldObj.isRemote) cmd_reload = 1; // queue for server packet
	        
            reload();
        }

        if (ENABLE_LOOK_YAW && pilot != null)
        {
            // input from look control (mouse or analog stick)
            float deltaYawDeg = pilot.rotationYaw - rotationYaw;

            while (deltaYawDeg > 180f) deltaYawDeg -= 360f;
            while (deltaYawDeg < -180f) deltaYawDeg += 360f;

            rotationYawSpeed = deltaYawDeg * 3f; // saving this for render use
            if (rotationYawSpeed > 90f) rotationYawSpeed = 90f;
            if (rotationYawSpeed < -90f) rotationYawSpeed = -90f;
            rotationYaw += rotationYawSpeed * deltaTime;
        }
        else
        // buttonYaw:
        {
            // button yaw
            if (Keyboard.isKeyDown(KEY_ROTATE_LEFT)) // g, rotate left
            {
                rotationYawSpeed -= 8f;
            }
            else if (Keyboard.isKeyDown(KEY_ROTATE_RIGHT)) // h, rotate right
            {
                rotationYawSpeed += 8f;
            }
            else
            {
                rotationYawSpeed *= .9f;
            }
            if (rotationYawSpeed > 80f) rotationYawSpeed = 80f;
            if (rotationYawSpeed < -80f) rotationYawSpeed = -80f;
            rotationYaw += rotationYawSpeed * deltaTime;
        }
            
        rotationYaw %= 360f;

        // PITCH CONTROL
        //
        // the cyclic (tilt control)
        // only affects pitch and roll, acceleration done later
        // zero pitch is level, positive pitch is leaning forward
            
        if (lookPitch) // helicopter follows player look pitch
        {
            if (Keyboard.isKeyDown(KEY_AUTO_LEVEL))
            {
                lookPitchZeroLevel = pilot.rotationPitch;
            }
                
            if (rotationPitch > MAX_PITCH)
            {
                rotationPitch = MAX_PITCH;
                rotationPitchSpeed = 0f;
            }
            else if (rotationPitch < -MAX_PITCH / 1.5f)
            {
                rotationPitch = -MAX_PITCH / 1.5f;
                rotationPitchSpeed = 0f;
            }
            else
            {
                float targetPitch = (pilot.rotationPitch / 80f) * MAX_PITCH - lookPitchZeroLevel;
                    
                // leaning backward, so further limit
                if (targetPitch < 0f) targetPitch *= .3f;
                    
                rotationPitchSpeed = 3f * (targetPitch - rotationPitch); // look down slightly

                rotationPitch += rotationPitchSpeed * deltaTime;
            }
                
            if (rotationPitch > MAX_PITCH) // check again to prevent judder
            {
                rotationPitch = MAX_PITCH;
                rotationPitchSpeed = 0f;
            }
            else if (rotationPitch < -MAX_PITCH / 1.5f)
            {
                rotationPitch = -MAX_PITCH / 1.5f;
                rotationPitchSpeed = 0f;
            }
        }
        else // normal button pitch and roll by player
        {
            // check for auto-level command, will be applied later if no other pitch key pressed
            if (Keyboard.isKeyDown(KEY_AUTO_LEVEL))
            {
                autoLevelDelay = 1.5f; // effect lasts a short time after key is released, but overriden by key presses
            }
                
            if (Keyboard.isKeyDown(KEY_FORWARD))
            {
                if (rotationPitch > MAX_PITCH)
                {
                    rotationPitch = MAX_PITCH;
                    rotationPitchSpeed = 0f;
                }
                else
                {
                    rotationPitchSpeed = PITCH_SPEED_DEG;
                    rotationPitch += rotationPitchSpeed * deltaTime;
                }
                    
                if (rotationPitch > MAX_PITCH) // check again to prevent judder
                {
                    rotationPitch = MAX_PITCH;
                    rotationPitchSpeed = 0f;
                }
            }
            else if (Keyboard.isKeyDown(KEY_BACK))
            {
                if (rotationPitch < -MAX_PITCH / 1.5f)
                {
                    rotationPitch = -MAX_PITCH / 1.5f;
                    rotationPitchSpeed = 0f;
                }
                else
                {
                    rotationPitchSpeed = -PITCH_SPEED_DEG;
                    rotationPitch += rotationPitchSpeed * deltaTime;
                }
                if (rotationPitch < -MAX_PITCH / 1.5f) // check again to prevent judder
                {
                    rotationPitch = -MAX_PITCH / 1.5f;
                    rotationPitchSpeed = 0f;
                }
            }
            else if (autoLevelDelay > 0) // this is fast, on-demand auto-level
            {
                autoLevelDelay -= deltaTime;
                    
                rotationPitchSpeed = -rotationPitch * 1.6f; // a bit faster than the normal auto-level
                rotationPitch += rotationPitchSpeed * deltaTime;
            }
            else
            {
                if (ENABLE_AUTO_LEVEL) // this is always-on auto-level, if enabled
                {
	                rotationPitchSpeed = -rotationPitch * .5f;
	                rotationPitch += rotationPitchSpeed * deltaTime;
                }
                else
                {
                    rotationPitchSpeed = 0f;
                }
            }
        }

        // ROLL
        if (Keyboard.isKeyDown(KEY_LEFT) && pilot != null)
        {
            if (rotationRoll > MAX_ROLL)
            {
                rotationRoll = MAX_ROLL;
                rotationRollSpeed = 0f;
            }
            else
            {
                rotationRollSpeed = ROLL_SPEED_DEG;
                rotationRoll += rotationRollSpeed * deltaTime;
            }
            if (rotationRoll > MAX_ROLL)
            {
                rotationRoll = MAX_ROLL;
                rotationRollSpeed = 0f;
            }
        }
        else if (Keyboard.isKeyDown(KEY_RIGHT) && pilot != null)
        {
            if (rotationRoll < -MAX_ROLL) 
            {
                rotationRoll = -MAX_ROLL;
                rotationRollSpeed = 0f;
            }
            else
            {
                rotationRollSpeed = -ROLL_SPEED_DEG;
                rotationRoll += rotationRollSpeed * deltaTime;
            }
            if (rotationRoll < -MAX_ROLL) 
            {
                rotationRoll = -MAX_ROLL;
                rotationRollSpeed = 0f;
            }
        }
        else if (autoLevelDelay > 0) // this is fast, on-demand auto-level
        {
            rotationRollSpeed = -rotationRoll * 1.6f;
            rotationRoll += rotationRollSpeed * deltaTime;
        }
        else
        {
            // auto-level roll
            rotationRollSpeed = -rotationRoll * .6f;
            rotationRoll += rotationRollSpeed * deltaTime;
        }
            

        // collective (throttle) control
        // default space, increase throttle
            
        if (Keyboard.isKeyDown(KEY_ASCEND) || (Keyboard.isKeyDown(KEY_FORWARD) && lookPitch)) // player
        {
	        altitudeLock = false;
            if (throttle < THROTTLE_MAX) throttle += THROTTLE_INC;
            if (throttle > THROTTLE_MAX) throttle = THROTTLE_MAX;
            // throttle = THROTTLE_MAX;
        }
        else if (Keyboard.isKeyDown(KEY_DESCEND) || (Keyboard.isKeyDown(KEY_BACK) && lookPitch)) 
        {
	        altitudeLock = false;
            if (throttle > THROTTLE_MIN) throttle -= THROTTLE_INC;
            if (throttle < THROTTLE_MIN) throttle = THROTTLE_MIN;
            // throttle = THROTTLE_MIN;
        }
        else
        {
            // zero throttle
            if (ENABLE_AUTO_THROTTLE_ZERO) throttle *= .6; // quickly zero throttle
            
	        //altitudeLock = true; // no falling during pitch/roll -- beginner mode 
        }
        
        // adjust model rotor speed according to throttle
        float power = (throttle - THROTTLE_MIN) / (THROTTLE_MAX - THROTTLE_MIN);
        ((ThxModelHelicopter) helper.model).rotorSpeed = power / 2f + .75f;
    }
    
    @Override
    void onUpdateDrone()
    {
        plog("client onUpdateDrone thottle: " + throttle);
        
        super.onUpdateDrone();
        
        // adjust model rotor speed according to throttle
        float power = (throttle - THROTTLE_MIN) / (THROTTLE_MAX - THROTTLE_MIN);
        ((ThxModelHelicopter) helper.model).rotorSpeed = power / 2f + .75f;
        
        float rotorSpeed = ((ThxModelHelicopter) helper.model).rotorSpeed;
        plog("other player rotorSpeed: " + rotorSpeed);
    }
    
    @Override
    void onUpdateVacant()
    {
        super.onUpdateVacant();
        
        // power down rotor
        ((ThxModelHelicopter) helper.model).rotorSpeed = 0f;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int damageAmount)
    {
        if (!super.attackEntityFrom(damageSource, damageAmount)) return false; // no hit
        
        Entity attackingEntity = damageSource.getEntity();
        log("Attacked by entity " + attackingEntity + " with amount " + damageAmount);

        // take damage sound
        worldObj.playSoundAtEntity(this, "random.bowhit", 1f, 1f);

        takeDamage((float) damageAmount * 3f);
        helper.addChatMessageToPilot("Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");

        //setBeenAttacked(); // this will cause Entity.velocityChanged to be true, so additional Packet28 to jump on hit
        setBeenAttacked();

        return true; // the hit landed
    }

    @Override
    public boolean interact(EntityPlayer player)
    {
        if (!super.interact(player)) return false;
        
        altitudeLock = false;
        
        // reset level to current look pitch
        lookPitchZeroLevel = player.rotationPitch;
        
        player.rotationYaw = rotationYaw;
        
        log("interact() added pilot " + player.entityId);
        return true;
    }

    double getPilotPositionAdjustment()
    {
        return 0.0;
    }
    
    @Override
    void pilotExit()
    {
        if (riddenByEntity == null) return;
        
        // place pilot to left of helicopter
        // (use fwd XZ perp to exit left: x = z, z = -x)
        double exitDist = 1.9;
        //Entity pilot = riddenByEntity;
        //pilot.setPosition(posX + fwd.z * exitDist, posY + pilot.yOffset, posZ - fwd.x * exitDist);
        riddenByEntity.setPosition(posX + fwd.z * exitDist, posY + riddenByEntity.yOffset, posZ - fwd.x * exitDist);
        
        super.pilotExit();
        
        ThxModelHelicopter model = (ThxModelHelicopter) helper.model;
        model.visible = true;
        model.rotorSpeed = 0f;
    }
    
    @Override
    void reload()
    {
        super.reload();
        
        worldObj.playSoundAtEntity(this, "random.click",  .4f, .4f); // volume, pitch
    }
}

