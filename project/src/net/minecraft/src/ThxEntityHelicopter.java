package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

public class ThxEntityHelicopter extends ThxEntity
{    
    int prevViewMode; // = 2; // 2 is looking back
    
    float smokeDelay;
    
    List<ThxEntity> followers = new ArrayList<ThxEntity>(); // list of ai entities led by this one
    
    int rocketCount;
    
    float MAX_HEALTH = 160f;

    float MAX_ACCEL    = 0.2000f;
    float GRAVITY      = 0.2005f;
    //float MAX_VELOCITY = 0.30f;
    float MAX_VELOCITY = 0.26f;
    float FRICTION = 0.98f;

    //float MAX_PITCH = 60.00f;
    float MAX_PITCH = 50.00f;
    float PITCH_SPEED_DEG = 40f;
    float PITCH_RETURN = 0.98f;

    float MAX_ROLL = 30.00f;
    float ROLL_SPEED_DEG = 40f;
    float ROLL_RETURN = 0.92f;

    float THROTTLE_MIN = -.06f;
    float THROTTLE_MAX = .09f;
    float THROTTLE_INC = .004f;

    // amount of vehicle motion to transfer upon projectile launch
    float MOMENTUM = .2f;

    boolean altitudeLock;
    float altitudeLockToggleDelay;
    
    float hudModeToggleDelay;
    
    float lookPitchToggleDelay;
    boolean lookPitch = false;
    float lookPitchZeroLevel;
    
    float createMapDelay;
    float pilotExitDelay;
    
    float missileDelay;
    final float MISSILE_DELAY = 6f;

    float rocketDelay;
    final float ROCKET_DELAY = .3f; // only applies to drones, pilot rocket rate controlled by interact
    final int FULL_ROCKET_COUNT = 12;
    float rocketReload;
    final float ROCKET_RELOAD_DELAY = 3f;
    
    float autoLevelDelay;
    
    Vector3 thrust = new Vector3();
    Vector3 velocity = new Vector3();
    
    // enemy AI helicopter or friend?
    public boolean isTargetHelicopterFriendly;
    public boolean isDroneArmed;
    Vector3 deltaPosToTarget = new Vector3();
        
    ThxEntityMissile lastMissileFired;
    
    public ThxEntityHelicopter(World world)
    {
        super(world);
        
        setSize(1.8f, 2f);

        yOffset = .8f; // avoid getting stuck in ground upon spawn, but looks high
        //yOffset = .7f;
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        setPositionAndRotation(x, y + yOffset, z, yaw, 0f);
    }
    
    @Override
    ThxEntityHelper createEntityHelper()
    {
        if (worldObj.isRemote)
        {
	        ThxModelHelicopterBase model = ThxConfig.getBoolProperty("enable_alt_model") ? new ThxModelHelicopterAlt() : new ThxModelHelicopter();
	        return new ThxEntityHelperClientHelicopter(this, model);
        }
        return new ThxEntityHelperServerHelicopter(this);
    }

    public Entity getPilot()
    {
        //return (EntityPlayer) riddenByEntity;
        return riddenByEntity;
    }

    @Override
    public void onUpdate() // entrypoint; called by minecraft each tick
    {
        //int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        //plog(String.format("onUpdate, pilot %d [posX: %6.2f, posY: %6.2f, posZ: %6.2f, yaw: %6.2f, pitch: %6.2f, roll: %6.2f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f, throttle: %6.3f, damage: %d]", riddenById, posX, posY, posZ, rotationYaw, rotationPitch, rotationRoll, motionX, motionY, motionZ, throttle, (int) damage));
        
        super.onUpdate();
        
        // all helicopters should have exhaust smoke as a test in lan mp: particles must be spawned by client, no effect on server
        //if (worldObj.isRemote) worldObj.spawnParticle("smoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
        
        // decrement cooldown timers
        missileDelay -= deltaTime;
        rocketDelay  -= deltaTime;
        
        if (rocketReload > 0f) rocketReload -= deltaTime;
        if (rocketReload < 0f)
        {
            // play sound to indicate rocket reload is complete
            rocketReload = 0f; // prevent completion sound from playing repeatedly
            worldObj.playSoundAtEntity(this, "random.click",  .4f, .7f); // volume, pitch
            
//          // testing to find out current entity IDs in use
//	        for (int i = 0; i < 3000; i++) 
//	        {
//	            log("class for id " + i + ": " + EntityList.getClassFromID(i));
//		    }
        
        }
        
        if (riddenByEntity != null)
        {
            sidedHelper.onUpdateWithPilot();
        }
        else if (targetEntity != null)
        {
            onUpdateDrone(); // drone ai
	        if (!worldObj.isRemote) updateMotion(false);
	        moveEntity(motionX, motionY, motionZ);
        }
        else
        {
            onUpdateVacant(); // vacant helicopter, no pilot or ai
	        if (!worldObj.isRemote) updateMotion(false);
	        moveEntity(motionX, motionY, motionZ);
        }
        
        if (handleCollisions()) // true if collided with other entity or environment
        {
            sidedHelper.addChatMessageToPilot("Damage: " + (int) (damage * 100 / MAX_HEALTH) + "%");
        }
        
        if (damage > MAX_HEALTH && !worldObj.isRemote) // helicopter destroyed! on server
        {
            float power = 2.3f;
            boolean flaming = true;
            boolean smoking = true;
            worldObj.newExplosion(this, posX, posY, posZ, power, flaming, smoking);
            
            if (riddenByEntity != null) pilotExit();
            
            dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
            
            setDead();
        }
        
        // auto-heal: 
        if (isBurning()) damage += deltaTime * 5f; // damage from fire
        else if (damage > 0f) damage -= deltaTime; // heal rate: 1 pt / sec
        
        if (damage / MAX_HEALTH > .9f && ticksExisted % 20 == 0)
        {
            sidedHelper.addChatMessageToPilot("Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
        }
		
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
        
        
        sidedHelper.updateAnimation(); // adjust rotor spin rate
        
        // on server, update watched values for all cases
        if (!worldObj.isRemote) updateDataWatcher();
        
        //riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        //plog(String.format("finish onUpdate, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
    }
    
    void onUpdateWithPilotPlayerInput()
    {
        Entity pilot = getPilot();
        
        if (pilot == null) return;
        
        if (pilot.entityId != minecraft.thePlayer.entityId) return;
        
        if (pilot.isDead)
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
        if (Keyboard.isKeyDown(ThxConfig.KEY_CREATE_MAP) && createMapDelay < 0f && pilot != null)
        {
            createMapDelay = 10f; // the delay in seconds
            cmd_create_map = 1;
        }
            
        hudModeToggleDelay -= deltaTime;
        lookPitchToggleDelay -= deltaTime;
            
        if (Keyboard.isKeyDown(ThxConfig.KEY_LOOK_BACK)) // look back while key is HELD (not toggle), switch to 3rd-person REVERSE view
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
        else if (Keyboard.isKeyDown(ThxConfig.KEY_LOOK_PITCH) && lookPitchToggleDelay < 0f)
        {
            lookPitchToggleDelay = .5f;
                
            if (!lookPitch)
            {
                lookPitch = true;

                // reset level offset
                lookPitchZeroLevel = 0f;

                // show status message
                sidedHelper.addChatMessageToPilot("Look-Pitch:  ON, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
            }
            else
            {
                // turn off cockpit
                lookPitch = false;
                sidedHelper.addChatMessageToPilot("Look-Pitch: OFF, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
            }
        }
        else if (Keyboard.isKeyDown(ThxConfig.KEY_HUD_MODE) && hudModeToggleDelay < 0f && pilot != null)
        {
            hudModeToggleDelay = .5f;
                
            ThxModel model = (ThxModel) sidedHelper.getModel();
            if (model != null)
            {
	            if (model.visible)
	            {
	                // change to 1st-person and hide model
	                if (minecraft.gameSettings.thirdPersonView != 0) minecraft.gameSettings.thirdPersonView = 0;
	                model.visible = false;
	            }
	            else
	            {
	                model.visible = true;
		            sidedHelper.addChatMessageToPilot("Cam 1, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
	            }
            }
        }
        else if (minecraft.gameSettings.thirdPersonView == 2) // must hold look back key, not a toggle, so cancel here
        {
            minecraft.gameSettings.thirdPersonView = 0;
        }
            
        // view could be switched by player using F5
        if (minecraft.gameSettings.thirdPersonView != 0) ((ThxModel) sidedHelper.getModel()).visible = true;

        altitudeLockToggleDelay -= deltaTime;
        if (Keyboard.isKeyDown(ThxConfig.KEY_LOCK_ALT) && altitudeLockToggleDelay < 0f && pilot != null)
        {
            altitudeLockToggleDelay = .5f;
            altitudeLock = !altitudeLock;
        }

        // PILOT EXIT
        pilotExitDelay -= deltaTime;
        if (Keyboard.isKeyDown(ThxConfig.KEY_EXIT) && pilot != null)
        {
            pilotExitDelay = .5f; // the delay in seconds
            //cmd_exit = 1; // queue for server packet
            pilotExit();
        }

        // MANUAL ROCKET RELOAD 
        if (Keyboard.isKeyDown(ThxConfig.KEY_ROCKET_RELOAD) && rocketCount > 0)
        {
	        cmd_reload = 1; // queue for server packet
            reload();
        }

        if (ThxConfig.ENABLE_LOOK_YAW && pilot != null)
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
            if (Keyboard.isKeyDown(ThxConfig.KEY_ROTATE_LEFT)) // g, rotate left
            {
                rotationYawSpeed -= 8f;
            }
            else if (Keyboard.isKeyDown(ThxConfig.KEY_ROTATE_RIGHT)) // h, rotate right
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
            if (Keyboard.isKeyDown(ThxConfig.KEY_AUTO_LEVEL))
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
            if (Keyboard.isKeyDown(ThxConfig.KEY_AUTO_LEVEL))
            {
                autoLevelDelay = 1.5f; // effect lasts a short time after key is released, but overriden by key presses
            }
                
            if (Keyboard.isKeyDown(ThxConfig.KEY_FORWARD))
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
            else if (Keyboard.isKeyDown(ThxConfig.KEY_BACK))
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
                if (ThxConfig.ENABLE_AUTO_LEVEL) // this is always-on auto-level, if enabled
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
        if (Keyboard.isKeyDown(ThxConfig.KEY_LEFT) && pilot != null)
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
        else if (Keyboard.isKeyDown(ThxConfig.KEY_RIGHT) && pilot != null)
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
            
        if (Keyboard.isKeyDown(ThxConfig.KEY_ASCEND) || (Keyboard.isKeyDown(ThxConfig.KEY_FORWARD) && lookPitch)) // player
        {
	        altitudeLock = false;
            if (throttle < THROTTLE_MAX) throttle += THROTTLE_INC;
            if (throttle > THROTTLE_MAX) throttle = THROTTLE_MAX;
            // throttle = THROTTLE_MAX;
        }
        else if (Keyboard.isKeyDown(ThxConfig.KEY_DESCEND) || (Keyboard.isKeyDown(ThxConfig.KEY_BACK) && lookPitch)) 
        {
	        altitudeLock = false;
            if (throttle > THROTTLE_MIN) throttle -= THROTTLE_INC;
            if (throttle < THROTTLE_MIN) throttle = THROTTLE_MIN;
            // throttle = THROTTLE_MIN;
        }
        else
        {
            // zero throttle
            if (ThxConfig.ENABLE_AUTO_THROTTLE_ZERO) throttle *= .6; // quickly zero throttle
            
	        //altitudeLock = true; // no falling during pitch/roll -- beginner mode 
        }
    }
    
    void onUpdateDrone()
    {
        if (worldObj.isRemote) // client-side drone receives standard mc update packets, no 250 custom
        {
            //already called in super.onUpdate: 
            //readDataWatcher();
            return; // position and velocity updates are handled by mc
        }
        
        ThxEntityHelicopter targetHelicopter = (ThxEntityHelicopter) targetEntity;
        
        if (targetHelicopter == null) return;
        
        if (targetHelicopter.isDead)
        {
            targetHelicopter = null;
            return;
        }
        
        float thd = 0f; // thd is targetHelicopter distance
        deltaPosToTarget.set((float)(targetHelicopter.posX - posX), 0f, (float)(targetHelicopter.posZ - posZ));
        thd = deltaPosToTarget.length();
            
        if (isTargetHelicopterFriendly)
        {
            // TODO: defend or help friendly target helicopter somehow?
        }
        else // not friendly, attack target
        {
            if (thd < 20f && thd > 5f && Math.abs(targetHelicopter.posY - posY) < 2.0)
            {
                // extra checks here on fire delays to avoid methods which do log
                if (damage > .6 * MAX_HEALTH && missileDelay < 0f) fireMissile();
                else if (rocketDelay < 0f) fireRocket();
            }
        }
        
        // YAW
        if (isTargetHelicopterFriendly && thd < 10f)
        {
            // mimic friendly target yaw rotation when close by
            float deltaYawDeg = targetHelicopter.rotationYaw - rotationYaw;

            while (deltaYawDeg > 180f) deltaYawDeg -= 360f;
            while (deltaYawDeg < -180f) deltaYawDeg += 360f;

            rotationYawSpeed = deltaYawDeg * 3f; // saving this for render use
            if (rotationYawSpeed > 90f) rotationYawSpeed = 90f;
            if (rotationYawSpeed < -90f) rotationYawSpeed = -90f;
            rotationYaw += rotationYawSpeed * deltaTime;
        }
        else
        {
            // turn toward target helicopter
                
            Vector3 deltaPos = Vector3.add(targetHelicopter.pos, pos.negate(null), null);
            //deltaPos.add(vel, deltaPos, deltaPos);
                
            if (Vector3.dot(side, deltaPos) > 0f)
            {
                rotationYaw += 60f * deltaTime;
            }
            else
            {
                rotationYaw -= 60f * deltaTime;
            }
        }
        
        if (thd > 10f)
        {
            rotationPitch = 45f * (thd - 10f) / 20f;
        }
        else 
        {
           if (!isTargetHelicopterFriendly) rotationPitch =  (1 - (thd / 10f)) * -20f;// -20f;
        }
        rotationPitchSpeed = 0f;
        
        
        // friendly PITCH and ROLL control
        if (isTargetHelicopterFriendly && thd < 10f)
        {
            rotationPitch = targetHelicopter.rotationPitch;
            rotationPitchSpeed = targetHelicopter.rotationPitchSpeed;
                
            rotationRoll = targetHelicopter.rotationRoll;
            rotationRollSpeed = targetHelicopter.rotationRollSpeed;
        }

        if (posY + 1f < targetHelicopter.posY)
        {
            //if (throttle < THROTTLE_MAX) throttle += THROTTLE_INC;
            //if (throttle > THROTTLE_MAX) throttle = THROTTLE_MAX;
            if (throttle < THROTTLE_MAX * .6f) throttle += THROTTLE_INC * .4f;
            if (throttle > THROTTLE_MAX * .6f) throttle = THROTTLE_MAX * .6f;
        }
        else if (posY - 2f > targetHelicopter.posY)
        {
            //if (throttle > THROTTLE_MIN) throttle -= THROTTLE_INC;
            //if (throttle < THROTTLE_MIN) throttle = THROTTLE_MIN;
            if (throttle > THROTTLE_MIN * .6f) throttle -= THROTTLE_INC * .4f;
            if (throttle < THROTTLE_MIN * .6f) throttle = THROTTLE_MIN * .6f;
        }
        else
        {
            throttle *= .6; // auto zero throttle   
        }
    }
    
    void onUpdateVacant()
    {
        //((ThxModel) helper.model).visible = true; // needed? 

        // adjust position height to avoid collisions
        // causes 'jumping'?
        /*
        List list = worldObj.getCollidingBoundingBoxes(this, boundingBox.contract(0.03125, 0.0, 0.03125));
        if (list.size() > 0)
        {
            double d3 = 0.0D;
            for (int j = 0; j < list.size(); j++)
            {
                AxisAlignedBB axisalignedbb = (AxisAlignedBB)list.get(j);
                if (axisalignedbb.maxY > d3)
                {
                    d3 = axisalignedbb.maxY;
                }
            }

            posY += d3 - boundingBox.minY;
            setPosition(posX, posY, posZ);
        }
        */
        
        throttle *= .8; // quickly zero throttle
        
        if (onGround) // very slow on ground
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral

            // tripple apply friction when on ground
            motionX *= FRICTION;
            motionX *= FRICTION;
            motionY = 0.0;
            motionZ *= FRICTION;
            motionZ *= FRICTION;
                
            rotationYawSpeed = 0f;
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
        else
        {
            // settle back to ground naturally if pilot bails
                
            rotationPitch *= PITCH_RETURN;
            rotationRoll *= ROLL_RETURN;
                
            motionX *= FRICTION;
            motionY -= GRAVITY * deltaTime;
            //motionY -= (GRAVITY / 2f) * deltaTime; // weakened gravity since no thrust
            motionZ *= FRICTION;
        }        
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damageAmount)
    {
        if (!super.attackEntityFrom(damageSource, damageAmount)) return false; // no hit
        
        Entity attackingEntity = damageSource.getEntity();
        log("Attacked by entity " + attackingEntity + " with amount " + damageAmount);

        // take damage sound
        worldObj.playSoundAtEntity(this, "random.bowhit", 1f, 1f);

        takeDamage((float) damageAmount * 3f);
        sidedHelper.addChatMessageToPilot("Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");

        setBeenAttacked(); // this will cause Entity.velocityChanged to be true, so additional Packet28 to jump on hit

        return true; // the hit landed
    }

    @Override
    public boolean func_130002_c/*interact*/(EntityPlayer player)
    //public boolean interact(EntityPlayer player)
    {
        // super.interact returns true if player boards and becomes new pilot
        if (!super.func_130002_c/*interact*/(player)) return true; // return true to handle interact() -- prevents currently held item from being used
        //if (!super.interact(player)) return true; // return true to handle interact() -- prevents currently held item from being used
        
        targetEntity = null; // inactivate ai
        
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
        log("pilotExit() called on " + (worldObj.isRemote ? "client" : "server"));
        
        if (riddenByEntity == null) 
        {
            log("### riddenByEntity is already null, returning");
            return;
        }
        
        Entity pilot = getPilot();
        riddenByEntity.mountEntity(this); // riddenByEntity is now null // no more pilot
	        
        // these should already be null from call to mountEntity above
        riddenByEntity = null;
        if (pilot != null) 
        {
            if (pilot.ridingEntity != null) pilot.ridingEntity.riddenByEntity = null; ; 
            pilot.ridingEntity = null; 
        }
	
        sidedHelper.pilotExit(pilot);
        
        // can't set these private fields if not using mountEntity() as above, might need better defaults
        //riddenByEntity.entityRiderPitchDelta = 0.0D;
        //riddenByEntity.entityRiderYawDelta = 0.0D;

        targetEntity = null;
    }
    
    void reload()
    {
        rocketReload = ROCKET_RELOAD_DELAY;
        rocketCount = 0;
        
        worldObj.playSoundAtEntity(this, "random.click",  .4f, .4f); // volume, pitch
    }
    
    @Override
    public double getMountedYOffset()
    {
        return -.25;
    }
    
    void fireRocket()
    {
        if (worldObj.isRemote) return;
        
        if (rocketDelay > 0f && riddenByEntity == null) return;
        if (rocketReload > 0f) return;
        
        rocketDelay = ROCKET_DELAY;
                
        rocketCount++;
        
        float leftRightAmount = .6f;
        float leftRight = (rocketCount % 2 == 0) ? leftRightAmount  : -leftRightAmount;
                
        // starting position of rocket relative to helicopter, out in front a bit to avoid collision
        float offsetX = (side.x * leftRight) + (fwd.x * 2.5f) + (up.x * -.5f);
        float offsetY = (side.y * leftRight) + (fwd.y * 2.5f) + (up.y * -.5f);
        float offsetZ = (side.z * leftRight) + (fwd.z * 2.5f) + (up.z * -.5f);
                    
        float yaw = rotationYaw;
        float pitch = rotationPitch + 5f;
                
        // pilot is owner to get xp, if no pilot (ai) then helicopter is owner
        Entity newOwner = riddenByEntity != null ? riddenByEntity : this;
        ThxEntityRocket newRocket = new ThxEntityRocket(newOwner, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        worldObj.spawnEntityInWorld(newRocket);
        
        for (Object followerItem : followers)
        {
            ThxEntityHelicopter follower = (ThxEntityHelicopter) followerItem;
            if (follower.isDroneArmed) follower.fireRocket();
        }
        
        if (rocketCount == FULL_ROCKET_COUNT)
        {
            reload();
        }
    }
    
    void fireMissile()
    {
        if (worldObj.isRemote) return; // only on server, then packets will spawn the client entity and synchronize
        
        if (missileDelay > 0f)
        {
            if (lastMissileFired != null && !lastMissileFired.isDead)
            {
                log("remote detonating missile");
                lastMissileFired.detonate();
            }
            return;
        }
        
        log("firing missile");
        missileDelay = MISSILE_DELAY;
                
        float offX = (fwd.x * 2.5f) + (up.x * -.5f);
        float offY = (fwd.y * 2.5f) + (up.y * -.5f);
        float offZ = (fwd.z * 2.5f) + (up.z * -.5f);

        // aim with cursor if pilot
        //float yaw = riddenByEntity != null ? riddenByEntity.rotationYaw : rotationYaw;
        //float pitch = riddenByEntity != null ? riddenByEntity.rotationPitch : rotationPitch;
        float yaw = rotationYaw;
        float pitch = rotationPitch + 5f;
                
        // pilot is owner to get xp, if no pilot (ai) then helicopter is owner
        Entity newOwner = riddenByEntity != null ? riddenByEntity : this;
        ThxEntityMissile newMissile = new ThxEntityMissile(newOwner, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        lastMissileFired = newMissile;
        worldObj.spawnEntityInWorld(newMissile);
    }

    void createMap()
    {
        //if (worldObj.isRemote) return;
        log("creating map");
        
        // TODO: comming soon for 1.3.2?
        if (true) return;
        
        int mapSize = 960; // full size region is 1024, but we want a little bit of overlap
                
        int mapIdxX = (int)posX / mapSize;
        if (posX < 0d) mapIdxX -= 1;
        else mapIdxX += 1;
                
        int mapIdxZ = (int)posZ / mapSize;
        if (posZ < 0d) mapIdxZ -= 1;
        else mapIdxZ += 1;
                
        // create 4 digit number with first 2 digits indicating
        // x and last 2 z in region units
        // (only works within 49 * mapSize of origin)
        int mapIdx = ((mapIdxX + 50) * 100) + mapIdxZ + 50;
        //System.out.println("Map idx: " + mapIdx);
                
        ItemStack mapStack = new ItemStack(Item.map.itemID, 1, mapIdx);
                
        String mapIdxString = "map_" + mapIdx;
                
        // this code was adapted from MapItem.onCreate to initialize the map location
        MapData mapdata = (MapData)worldObj.loadItemData(MapData.class, mapIdxString);
        if(mapdata == null)
        {
            mapdata = new MapData(mapIdxString);
            worldObj.setItemData(mapIdxString, mapdata);

            int mapX = mapIdxX * mapSize;
            if (mapX < 0) mapX += mapSize / 2;
            else mapX -= mapSize / 2;
            mapdata.xCenter = mapX;
                
            int mapZ = mapIdxZ * mapSize;
            if (mapZ < 0) mapZ += mapSize / 2;
            else mapZ -= mapSize / 2;
            mapdata.zCenter = mapZ;
                
                
            mapdata.scale = 3;
            //mapdata.dimension = (byte)worldObj.worldProvider.worldType; // worldProvider removed in 1.3.2
            mapdata.markDirty();
        }

        entityDropItem(mapStack, .5f);
    }
    
    // calculate thrust based on pitch, roll, throttle, and gravity
    // used on SERVER for drone ai, used on CLIENT by player pilots
    void updateThrust()
    {
        ascendDescendLift:
        {
            // as pitch and roll increases, lift decreases by fall-off function.
            // thrust.y ranges from 0 to 1. at 1, almost perfectly balances gravity
            
            //thrust.y = MathHelper.cos(pitchRad) * MathHelper.cos(rollRad);
            thrust.y = MathHelper.cos(pitchRad) * MathHelper.cos(rollRad) * MathHelper.cos(rollRad);
        }

        forwardBack:
        {
            // as pitch increases, forward-back motion increases
            // but sin function was too touchy so using 1-cos
            float accel = 1f - MathHelper.cos(pitchRad);
            if (pitchRad > 0f) accel *= -1f;
            
            thrust.x = -fwd.x * accel;
            thrust.z = -fwd.z * accel;
            
            // also adjust y in addition to ascend/descend to simulate diving
            //thrust.y += -fwd.y * accel * .3f;
            thrust.y += -fwd.y * accel;
        }

        strafeLeftRight:
        {
            // float strafe = -MathHelper.sin(roll);
            float strafe = 1f - MathHelper.cos(rollRad);
            if (rollRad > 0f) strafe *= -1f;

            // use perp of yaw and scale by roll
            thrust.x -= fwd.z * strafe;
            thrust.z += fwd.x * strafe;
        }
        
        // scale thrust by current throttle
        //thrust.normalize().scale(MAX_ACCEL * (1f + throttle) * deltaTime / .05f);
        thrust.normalize().scale(MAX_ACCEL * (1f + throttle));

        // gravity is always straight down
        //if (!inWater && !onGround) velocity.y -= GRAVITY * deltaTime / .05f;
        //velocity.y -= GRAVITY * deltaTime / .05f;
        thrust.y -= GRAVITY;
    }

    void updateMotion(boolean altitudeLock)
    {
        updateThrust();
        
        // start with current velocity
        velocity.set((float)motionX, (float)motionY, (float)motionZ);

        // friction or wind resistence
        velocity.scale(FRICTION);

        // apply the thrust
        Vector3.add(velocity, thrust, velocity);

        // limit max velocity
        if (velocity.lengthSquared() > MAX_VELOCITY * MAX_VELOCITY)
        {
            velocity.scale(MAX_VELOCITY / velocity.length());
        }

        // apply velocity changes
        motionX = (double) velocity.x;
        motionY = (double) velocity.y;
        motionZ = (double) velocity.z;
            
        // altitudeLock will quickly zero out the y component
        if (altitudeLock && riddenByEntity != null)
        {
            motionY *= .9;
            if (motionY < .00001) motionY = .0;
        }
    }
    
    /**
     * @return boolean if damage was taken as a result of collision
     */
    boolean handleCollisions()
    {
        boolean isCollidedWithEntity = false;
        
        /* causing problems...
        detectCollisionsAndBounce:
        {
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.2, 0.2, 0.2));
            for (int j1 = 0; list != null && j1 < list.size(); j1++)
            {
                Entity entity = (Entity) list.get(j1);
                if (entity.equals(riddenByEntity)) continue;
                if (!entity.canBeCollidedWith()) continue;
                        
                if (entity instanceof ThxEntity)
                {
                    Entity otherOwner = ((ThxEntity) entity).owner;
                    if (otherOwner != null && (equals(otherOwner.ridingEntity) || equals(otherOwner))) 
                    {
                        log("ignoring collision with own thx child");
                        continue;
                    }
                    else log("hit by other thx entity with owner " + otherOwner);
                }
                    
                log("collided with entity " + entity.entityId);
                entity.applyEntityCollision(this);
                        
                isCollidedWithEntity = true;
            }
        }
        */
        
        // crash, take damage and slow down
        if (isCollidedHorizontally || isCollidedVertically || isCollidedWithEntity)
        {
            double velSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
            
            if (velSq > .005 && timeSinceCollided  < 0f && !onGround)
            {
                log("crash velSq: " + velSq);
                
                //timeSinceCollided = .5f; // sec delay before another collision possible
                timeSinceCollided = .2f; // sec delay before another collision possible
                
                if (riddenByEntity != null)
                {
                    float crashDamage = (float) velSq * 1000f; 
                    // velSq seems to range between .010 and .080, 10 to 80 damage, so limit:
                    if (crashDamage < 3f) crashDamage = 3f; 
                    if (crashDamage > 49f) crashDamage = 49f; 
                
                    log("crash damage: " + crashDamage);
                    takeDamage(crashDamage); // crash damage based on velocity
                }
                
                for (int i = 0; i < 5; i++)
                {
                    worldObj.spawnParticle("explode", posX - 1f + Math.random() *2f, posY - 1f + Math.random() *2f, posZ - 1f + Math.random() *2f, 0.0, 0.0, 0.0);
                }
                
                float volume = (float) velSq * 10f;
                if (volume > .8f) volume = .8f;
                if (volume < .3f) volume = .3f;
                
                float pitch = .4f + worldObj.rand.nextFloat() * .4f;
                log("volume: " + volume + ", pitch: " + pitch);
                
                worldObj.playSoundAtEntity(this, "random.explode",  volume, pitch);
                
                motionX *= .7;
                motionY *= .7;
                motionZ *= .7;
            
                return true;
            }
            //isCollidedHorizontally = false;
            //isCollidedVertically = false;
        }
        return false;
    }
    
    @Override
    public void updateRiderPosition()
    {
        if (riddenByEntity == null) return;
        
        // this will tell the default impl in Entity.updateRidden()
        // that no adjustment need be made to the pilot's yaw or pitch
        // as a direct result of riding this helicopter entity.
        // rather, we let the player rotate the pilot and the helicopter follows
        // TODO: add "free look" zone wherecontrol within limited arc
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        
        // to force camera to follow helicopter exactly, but stutters:
        //pilot.setPositionAndRotation(posX + fwd.x * posAdjust, posY + pilot.getYOffset() + getMountedYOffset(), posZ + fwd.z * posAdjust, rotationYaw, rotationPitch);
        // or?
        //pilot.setLocationAndAngles(posX + fwd.x * posAdjust, posY -.7f, posZ + fwd.z * posAdjust, rotationYaw, rotationPitch);
        
        riddenByEntity.setPosition(posX, posY + riddenByEntity.getYOffset() + getMountedYOffset(), posZ);
    }
    
    @Override
    void attackedByPilot()
    {
        fireMissile();
    }
    
    @Override
    void interactByPilot()
    {
        fireRocket();
    }
    
    void convertToItem()
    {
        exit_helicopter_and_convert_back_to_item:
        {
            pilotExit();
            setDead();
            if (this instanceof ThxEntityHelicopter) // && !worldObj.isRemote)
            {
                dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
            }
        }
    }
    
    void attackedByThxEntity(ThxEntity attackingEntity) // activate/adjust AI for helicopter hit by another helicopter
    {
        if (riddenByEntity != null || !(attackingEntity instanceof ThxEntityHelicopter))
        {
            return;
        }
        
        log("attacked by " + attackingEntity + " with pilot: " + attackingEntity.riddenByEntity);
            
        if (targetEntity == null) // first attack by another helo, begin tracking as friendly
        {
            targetEntity = attackingEntity;
            targetEntity =  attackingEntity;
            
            ((ThxEntityHelicopter) targetEntity).followers.add(this);
                
            isTargetHelicopterFriendly = true;
            isDroneArmed = false;
                
            worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
                
            log("new targetEntity: " + targetEntity);
        }
        else if (targetEntity.equals(attackingEntity)) // already tracking
        {
            if (isTargetHelicopterFriendly) // friendly fire
            {
                if (!isDroneArmed)
                {
                    isDroneArmed = true; // now armed, still friendly, earn xp!
                    owner = targetEntity.owner;
                        
                    worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
                }
                else
                {
                    isTargetHelicopterFriendly = false;
                        
                    ((ThxEntityHelicopter) targetEntity).followers.remove(this);
                        
                    owner = this; // no more xp
                        
                    missileDelay = 10f; // initial missile delay
                    rocketDelay  =  5f; // initial rocket delay
                        
                    worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
                }
            }
            else
            {
                // enemy hit us again! surrender, change to friendly if almost dead
                if (damage / MAX_HEALTH > .9f && !isBurning())
                {
                    isTargetHelicopterFriendly = true;
                    isDroneArmed = false;
                }
            }
        }
        else
        {
            // hit by a helicopter other than the one we are following, so attack it
                
            ((ThxEntityHelicopter) targetEntity).followers.remove(this);
                
            targetEntity = (ThxEntity) attackingEntity;
                
            isTargetHelicopterFriendly = false;
                
            worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
                
            log("new enemy targetEntity: " + targetEntity);
                
        }
    }
    
    /*
    @Override
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }
    */
    
    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity)
    {
        return null;
    }

    @Override
    public void setVelocity(double dx, double dy, double dz)
    {
        assertClientSideOnly();
        
	    if (riddenByEntity != null && riddenByEntity.entityId == minecraft.thePlayer.entityId) // player is the client pilot
        {
	        // already sending velocity to server, so ignore server updates
	        return;
        }
	    
        super.setVelocity(dx, dy, dz);
    }

    @Override
    public void setPositionAndRotation2(double posX, double posY, double posZ, float yaw, float pitch, int unused)
    {
        assertClientSideOnly();
        
        // bypassing check for collision in supermethod which seems to be hitting pilot and causing jumping
        // but it is still needed for vacant helicopters to prevent sinking into the ground
        
	    if (riddenByEntity != null && riddenByEntity.entityId == minecraft.thePlayer.entityId) // player is the client pilot
        {
	        // already sending position to server, so ignore server updates
	        return;
        }
	    
	    super.setPositionAndRotation2(posX, posY, posZ, yaw, pitch, unused);
    }
}

