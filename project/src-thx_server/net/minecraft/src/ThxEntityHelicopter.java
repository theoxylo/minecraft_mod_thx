package net.minecraft.src;

import java.util.List;


public class ThxEntityHelicopter extends ThxEntity
{
    static int instanceCount = 0;

    // controls and options
    // set from mod_thx.properties
    static int KEY_ASCEND = 0;
    static int KEY_DESCEND = 0;
    static int KEY_FORWARD = 0;
    static int KEY_BACK = 0;
    static int KEY_LEFT = 0;
    static int KEY_RIGHT = 0;
    static int KEY_ROTATE_LEFT = 0;
    static int KEY_ROTATE_RIGHT = 0;
    static int KEY_FIRE_MISSILE = 0;
    static int KEY_FIRE_ROCKET = 0;
    static int KEY_LOOK_PITCH = 0;
    static int KEY_AUTO_LEVEL = 0;
    static int KEY_EXIT = 0;
    static int KEY_LOOK_BACK = 0;
    static int KEY_CREATE_MAP = 0;
    static int KEY_CREATE_ITEM = 0;
    static int KEY_HUD_MODE = 0;
    static int KEY_LOCK_ALT = 0;
    
        
    static boolean ENABLE_AUTO_LEVEL = ThxConfig.getBoolProperty("enable_auto_level");
    static boolean ENABLE_AUTO_THROTTLE_ZERO = ThxConfig.getBoolProperty("enable_auto_throttle_zero");
    static boolean ENABLE_DRONE_MODE = ThxConfig.getBoolProperty("enable_drone_mode");
    static boolean ENABLE_LOOK_YAW = ThxConfig.getBoolProperty("enable_look_yaw") && !ENABLE_DRONE_MODE;
        
    boolean enable_drone_mode = ENABLE_DRONE_MODE;
    
    final float MAX_HEALTH = 100;

    final float MAX_ACCEL    = 0.30f;
    final float GRAVITY      = 0.301f;
    final float MAX_VELOCITY = 0.44f;
    final float FRICTION = 0.98f;

    final float MAX_PITCH = 60.00f;
    final float PITCH_SPEED_DEG = 40f;
    final float PITCH_RETURN = 0.98f;

    final float MAX_ROLL = 30.00f;
    final float ROLL_SPEED_DEG = 40f;
    final float ROLL_RETURN = 0.92f;

    float throttle = 0.0f;
    final float THROTTLE_MIN = -.03f;
    final float THROTTLE_MAX = .07f;
    final float THROTTLE_INC = .005f;

    public float getThrottlePower()
    {
        return (throttle - THROTTLE_MIN) / (THROTTLE_MAX - THROTTLE_MIN);
    }
    
    float updateDelay;
    float UPDATE_RATE = 4; // update packets sent per second

    // Vectors for repeated calculations
    Vector3 thrust = new Vector3();
    Vector3 velocity = new Vector3();
    
    // amount of vehicle motion to transfer upon projectile launch
    final float MOMENTUM = .2f;

    // total update count
    float _damage;
    float timeSinceAttacked;
    float timeSinceCollided;
    float smokeDelay;
    
    boolean altitudeLock;
    float altitudeLockToggleDelay;
    
    float hudModeToggleDelay;
    
    float lookPitchToggleDelay;
    boolean lookPitch = false;
    float lookPitchZeroLevel;
    
    float createMapDelay;
    float createItemDelay;
    
    int prevViewMode = 2;
    
    float missileDelay;
    final float MISSILE_DELAY = 3f;

    float rocketDelay;
    final float ROCKET_DELAY = .12f;
    
    int rocketCount;
    final int FULL_ROCKET_COUNT = 12;
    
    float rocketReload;
    final float ROCKET_RELOAD_DELAY = 2f;
    
    float autoLevelDelay;
    float exitDelay;

    double dronePilotPosX;
    double dronePilotPosY;
    double dronePilotPosZ;
    
    // enemy AI helicopter or friend?
    public ThxEntityHelicopter targetHelicopter;
    public boolean isTargetHelicopterFriendly;
    Vector3 deltaPosToTarget = new Vector3();

    public ThxEntityHelicopter(World world)
    {
        super(world);

        setSize(1.8f, 2f);

        yOffset = .6f;

        instanceCount++;
        log("C1 - ThxEntityHelicopter instance count: " + instanceCount);
    }

    public ThxEntityHelicopter(World world, double x, double y, double z)
    {
        this(world);
        setPosition(x, y + yOffset, z);
        
        log("C2 - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw, Entity owner)
    {
        this(world);
        this.owner = owner;
        setLocationAndAngles(x, y, z, yaw, 0f);
        
        log("C3 - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ + ", yaw: " + yaw);
    }

    public EntityPlayer getPilot()
    {
        return (EntityPlayer) riddenByEntity;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
	        
        // for auto-heal: 
        if (_damage > 0f) _damage -= deltaTime; // heal rate: 1 pt / sec

        // create smoke to indicate damage
        if (smokeDelay < 0f)
        {
            smokeDelay = 1f - _damage / MAX_HEALTH;
            if (smokeDelay > .4f) smokeDelay = .4f;

            for (int i = (int) (10f * _damage / MAX_HEALTH); i > 0; i--)
            {
                if (i % 2 == 0) worldObj.spawnParticle("smoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
                else worldObj.spawnParticle("largesmoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
                
                if (i > 6) break;
            }
            if (_damage / MAX_HEALTH > .75f) worldObj.spawnParticle("flame", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
        }
        
        // decrement cooldown timers
        smokeDelay   -= deltaTime;
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
        missileDelay -= deltaTime;
        rocketDelay  -= deltaTime;
        rocketReload -= deltaTime;
        
        EntityPlayer pilot = getPilot();
        if (pilot != null || targetHelicopter != null)
        {
            if (pilot != null && pilot.isDead) riddenByEntity = null;
            
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
	            motionY += 0.02;
	        }
            /*
            else
            {
                if (ENABLE_LOOK_DOWN_TRANS && pilot != null)
                {
	                // hide bottom panel for looking down when in air
	                //if (pilot.rotationPitch - rotationPitch > 60f)
	                if (pilot.rotationPitch > 70f)
	                {
	                    ((ThxModelHelicopter) model).bottomVisible = false;
	                }
	                else
	                {
	                    ((ThxModelHelicopter) model).bottomVisible = true;
	                }
                }
            }
            */
            
            /* disable create item for now, too OP
            createItemDelay -= deltaTime;
            if (isKeyDown(KEY_CREATE_ITEM) && createItemDelay < 0f && pilot != null)
            {
                createItemDelay = 1f; // the delay in seconds
                
                dropItem(Item.egg.shiftedIndex, 1);
            }
            */
                        
            createMapDelay -= deltaTime;
            if (isKeyDown(KEY_CREATE_MAP) && createMapDelay < 0f && pilot != null)
            {
                createMapDelay = 10f; // the delay in seconds
                
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
                
                ItemStack mapStack = new ItemStack(Item.map.shiftedIndex, 1, mapIdx);
                
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
	                mapdata.dimension = (byte)worldObj.worldProvider.worldType;
	                mapdata.markDirty();
                }

                entityDropItem(mapStack, .5f);
            }
            
            hudModeToggleDelay -= deltaTime;
            lookPitchToggleDelay -= deltaTime;
            
            if (enable_drone_mode || pilot == null)
            {
                // no view changes for drone or AI
            }
            else if (isKeyDown(KEY_LOOK_BACK)) // look back while key is held
            {
                // remember current view mode and switch to reverse (only if not already)
                /*
                if (minecraft.gameSettings.thirdPersonView != 2)
                {
                    prevViewMode = minecraft.gameSettings.thirdPersonView; 
	                minecraft.gameSettings.thirdPersonView = 2; // switch to 3rd-person REVERSE view
                }
                */
            }
            /*
            else if (prevViewMode != 2 && minecraft.gameSettings.thirdPersonView == 2) // return to original view after look back key released
            {
                minecraft.gameSettings.thirdPersonView = prevViewMode;
                prevViewMode = 2;
            }
            */
            else if (isKeyDown(KEY_LOOK_PITCH) && lookPitchToggleDelay < 0f)
            {
                lookPitchToggleDelay = .5f;
                
                if (!lookPitch)
                {
                    lookPitch = true;

                    // reset level offset
                    lookPitchZeroLevel = 0f;

                    // show status message
                    //minecraft.ingameGUI.addChatMessage("Look-Pitch:  ON, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(_damage * 100 / MAX_HEALTH) + "%");
                }
                else
                {
                    // turn off cockpit
                    lookPitch = false;
                    //minecraft.ingameGUI.addChatMessage("Look-Pitch: OFF, PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(_damage * 100 / MAX_HEALTH) + "%");
                }
            }
            else if (isKeyDown(KEY_HUD_MODE) && hudModeToggleDelay < 0f && pilot != null)
            {
                hudModeToggleDelay = .5f;
                
                /*
                if (model.visible)
                {
                    // change to 1st-person and hide model
                    if (minecraft.gameSettings.thirdPersonView != 0) minecraft.gameSettings.thirdPersonView = 0;
                    model.visible = false;
                }
                else
                {
                    model.visible = true;
                }
                */
            }
            /*
            else if (minecraft.gameSettings.thirdPersonView == 2) // must hold look back key, not a toggle, so cancel here
            {
                minecraft.gameSettings.thirdPersonView = 0;
            }
            */
            
            // view could be switched by player using F5
            //if (minecraft.gameSettings.thirdPersonView != 0) model.visible = true;

            exitDelay -= deltaTime;
            if (isKeyDown(KEY_EXIT) && exitDelay < 0f && pilot != null)
            {
                exitDelay = 1f; // seconds before player can exit
                pilotExit();
            }

            altitudeLockToggleDelay -= deltaTime;
            if (isKeyDown(KEY_LOCK_ALT) && altitudeLockToggleDelay < 0f && pilot != null)
            {
                altitudeLockToggleDelay = .5f;
                altitudeLock = !altitudeLock;
            }

            float thd = 0f; // thd is targetHelicopter distance
            if (targetHelicopter != null)
            {
	            deltaPosToTarget.set((float)(targetHelicopter.posX - posX), 0f, (float)(targetHelicopter.posZ - posZ));
	            thd = deltaPosToTarget.length();
                //Vector3f deltaPos = Vector3f.add(targetHelicopter.pos, pos.negate(null), null);
                //thd = deltaPos.length();
            }
            
            // FIRE ROCKET
            if (((targetHelicopter != null && !isTargetHelicopterFriendly && thd < 20f && thd > 5f && Math.abs(targetHelicopter.posY - posY) < 2f) 
                || isKeyDown(KEY_FIRE_ROCKET)) 
                    && rocketDelay < 0f && rocketReload < 0f)
            {
                rocketCount++;
                rocketDelay = ROCKET_DELAY;
                if (targetHelicopter != null) rocketDelay *= 2f;
                
                float leftRight = (rocketCount % 2 == 0) ? 1.0f : -1.0f;
                
                // starting position of rocket relative to helicopter, out in front quite a bit to avoid collision
                float offsetX = side.x * leftRight + fwd.x * 2f;
                float offsetY = side.y * leftRight + fwd.y * 2f;
                float offsetZ = side.z * leftRight + fwd.z * 2f;
                    
                float yaw = rotationYaw;
                float pitch = rotationPitch + 5f; // slight downward from helicopter pitch
                
                // use pilot aim when in 1st-person
                /*
                if (pilot != null && minecraft.gameSettings.thirdPersonView == 0) 
                {
                    yaw = pilot.rotationYaw;
                    pitch = pilot.rotationPitch;
                }
                */
                
                // ai helicopter will use owner pilot aim
                if (targetHelicopter != null && isTargetHelicopterFriendly && targetHelicopter.riddenByEntity != null)
                {
	                yaw = targetHelicopter.riddenByEntity.rotationYaw;
	                pitch = targetHelicopter.riddenByEntity.rotationPitch;
                }
                
                /*
                ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                newRocket.owner = this;
                worldObj.spawnEntityInWorld(newRocket);
                */
                
                if (rocketCount == FULL_ROCKET_COUNT)
                {
                    // must reload before next volley
                    rocketReload = ROCKET_RELOAD_DELAY;
                    rocketCount = 0;
                }
            }

            // FIRE MISSILE
            if (missileDelay < 0f && targetHelicopter != null && !isTargetHelicopterFriendly && Math.abs(targetHelicopter.posY - posY) < 2f) // && _damage > MAX_HEALTH / 2f) // ai fire missle when low health
            {
                missileDelay = MISSILE_DELAY * 2f; // nerf fire rate
                
                float offX = fwd.x * 2f;
                float offY = fwd.y * 2f;
                float offZ = fwd.z * 2f;

                float yaw = rotationYaw;
                float pitch = rotationPitch;
                
                /*
                ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                newMissile.targetHelicopter = targetHelicopter;
                worldObj.spawnEntityInWorld(newMissile);
                */
            }
            else if (isKeyDown(KEY_FIRE_MISSILE) && missileDelay < 0f)
            {
                missileDelay = MISSILE_DELAY;
                
                float offX = fwd.x * 2f;
                float offY = fwd.y * 2f;
                float offZ = fwd.z * 2f;

                float yaw = rotationYaw;
                float pitch = rotationPitch + 5f; // slight downward from helicopter pitch
                
                // use pilot aim when in 1st-person
                /*
                if (pilot != null && minecraft.gameSettings.thirdPersonView == 0)
                {
                    yaw = pilot.rotationYaw;
                    pitch = pilot.rotationPitch;
                }
                */
                if (targetHelicopter != null && isTargetHelicopterFriendly && targetHelicopter.riddenByEntity != null)
                {
                    yaw = targetHelicopter.riddenByEntity.rotationYaw;
                    pitch = targetHelicopter.riddenByEntity.rotationPitch;
                }

                /*
                ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                worldObj.spawnEntityInWorld(newMissile);
                */
            }

            // START YAW
            if (targetHelicopter != null && isTargetHelicopterFriendly && thd < 10f)
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
            else if (targetHelicopter != null) // && !isTargetHelicopterFriendly)
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
            else if (ENABLE_LOOK_YAW && pilot != null)
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
                if (isKeyDown(KEY_ROTATE_LEFT)) // g, rotate left
                {
                    rotationYawSpeed -= 8f;
                }
                else if (isKeyDown(KEY_ROTATE_RIGHT)) // h, rotate right
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
            
            if (targetHelicopter != null) // we are AI
            {
                if (thd > 10f)
                {
                    rotationPitch = 45f * (thd - 10f) / 20f;
                }
				else 
			    {
				   if (!isTargetHelicopterFriendly) rotationPitch =  (1 - (thd / 10f)) * -20f;// -20f;
			    }
                rotationPitchSpeed = 0f;
            }
            else if (lookPitch) // helicopter follows player look pitch
            {
                if (isKeyDown(KEY_AUTO_LEVEL))
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
                if (isKeyDown(KEY_AUTO_LEVEL))
	            {
	                autoLevelDelay = 1.5f; // effect lasts a short time after key is released, but overriden by key presses
	            }
                
	            if (isKeyDown(KEY_FORWARD))
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
                else if (isKeyDown(KEY_BACK))
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

            if (targetHelicopter != null) // we are AI
            {
                if (!isTargetHelicopterFriendly)
                {
                    // roll toward or away?
                }
                if (thd > 10f)
                {
                    // seek target
                }
            }
            else if (isKeyDown(KEY_LEFT) && pilot != null)
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
            else if (isKeyDown(KEY_RIGHT) && pilot != null)
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
            
            
            // allow direct control of nearby ai friendly helicopters
            if (targetHelicopter != null && isTargetHelicopterFriendly && thd < 10f)
            {
                rotationPitch = targetHelicopter.rotationPitch;
                rotationPitchSpeed = targetHelicopter.rotationPitchSpeed;
                
                rotationRoll = targetHelicopter.rotationRoll;
                rotationRollSpeed = targetHelicopter.rotationRollSpeed;
            }


            // collective (throttle) control
            // default space, increase throttle
            
            if (targetHelicopter != null) // ai
            {
                if (posY + 1f < targetHelicopter.posY)
                {
	                if (throttle < THROTTLE_MAX * .6f) throttle += THROTTLE_INC * .4f;
	                if (throttle > THROTTLE_MAX * .6f) throttle = THROTTLE_MAX * .6f;
                }
                else if (posY - 2f > targetHelicopter.posY)
                {
	                if (throttle > THROTTLE_MIN * .6f) throttle -= THROTTLE_INC * .4f;
	                if (throttle < THROTTLE_MIN * .6f) throttle = THROTTLE_MIN * .6f;
                }
                else
                {
                    throttle *= .6; // auto zero throttle   
                }
            }
            else if (isKeyDown(KEY_ASCEND) 
                 || (isKeyDown(KEY_FORWARD) && lookPitch)) // player
            {
                if (throttle < THROTTLE_MAX) throttle += THROTTLE_INC;
                if (throttle > THROTTLE_MAX) throttle = THROTTLE_MAX;
                // throttle = THROTTLE_MAX;
            }
            else if (isKeyDown(KEY_DESCEND)
                 || (isKeyDown(KEY_BACK) && lookPitch)) 
            {
                if (throttle > THROTTLE_MIN) throttle -= THROTTLE_INC;
                if (throttle < THROTTLE_MIN) throttle = THROTTLE_MIN;
                // throttle = THROTTLE_MIN;
            }
            else
            {
                // zero throttle
                if (ENABLE_AUTO_THROTTLE_ZERO) throttle *= .6; // quickly zero throttle
            }
            
            // adjust rotor speed
	        //((ThxModelHelicopter) model).rotorSpeed = getThrottlePower() / 2f + .7f;
            
            // now calculate thrust and velocity based on yaw, pitch, roll, throttle
            
            ascendDescendLift:
            {
                // as pitch increases, lift decreases by fall-off function
                thrust.y = MathHelper.cos(pitchRad) * MathHelper.cos(rollRad);
            }

            forwardBack:
            {
                // as pitch increases, forward-back motion increases
                // but sin function was too touchy so using 1-cos
                float accel = 1f - MathHelper.cos(pitchRad);
                if (pitchRad > 0f) accel *= -1f;
                
                thrust.x = -fwd.x * accel;
                thrust.z = -fwd.z * accel;
            }

            strafeLeftRight:
            {
                // double strafe = (double) -MathHelper.sin(roll);
                float strafe = 1f - MathHelper.cos(rollRad);
                if (rollRad > 0f) strafe *= -1f;

                // use perp of yaw and scale by roll
                thrust.x -= fwd.z * strafe;
                thrust.z += fwd.x * strafe;
            }

            // start with current velocity
            velocity.set((float)motionX, (float)motionY, (float)motionZ);

            // friction, very little!
            velocity.scale(FRICTION);

            // scale thrust by current throttle and delta time
            thrust.normalize().scale(MAX_ACCEL * (1f + throttle) * deltaTime / .05f);

            // apply the thrust
            Vector3.add(velocity, thrust, velocity);

            // gravity is always straight down
            velocity.y -= GRAVITY * deltaTime / .05f;

            // limit max velocity
            if (velocity.lengthSquared() > MAX_VELOCITY * MAX_VELOCITY)
            {
                velocity.scale(MAX_VELOCITY / velocity.length());
            }

            // apply velocity changes
            motionX = velocity.x;
            motionY = velocity.y;
            motionZ = velocity.z;
            
            if (altitudeLock) motionY = 0f;
        }
        else
        // no pilot -- slowly sink to the ground
        {
            /*
            model.visible = true;
            ((ThxModelHelicopter) model).rotorSpeed = 0;
            ((ThxModelHelicopter) model).bottomVisible = true;
            */

            if (onGround || inWater)
            {
                if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
                if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral
                
                // tend to stay put on ground
                motionY = 0.;
                motionX *= .7;
                motionZ *= .7;
                
                rotationYawSpeed = 0f;
            }
            else
            {
                // settle back to ground naturally if pilot bails
                
	            rotationPitch *= PITCH_RETURN;
	            rotationRoll *= ROLL_RETURN;
                
                motionX *= FRICTION;
                motionY -= GRAVITY * .16f * deltaTime / .05f;
                motionZ *= FRICTION;
            }
        }
        
        // move in all cases
        moveEntity(motionX, motionY, motionZ);

        /*
        detectCollisionsAndBounce:
        {
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.2, 0.2 0.2));
            if (list != null && list.size() > 0)
            {
                for (int j1 = 0; j1 < list.size(); j1++)
                {
                    Entity entity = (Entity) list.get(j1);
                    if (entity != pilot && entity.canBePushed())
                    {
                        entity.applyEntityCollision(this);
                    }
                }
            }
        }
        */
        
        // crash, take damage and slow down
        if (isCollidedHorizontally || isCollidedVertically)
        {
	        double velSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
            
	        //if (velSq > .03 && timeSinceCollided  < 0f)
	        if (velSq > .005 && timeSinceCollided  < 0f)
	        {
	            log("crash velSq: " + velSq);
                
                timeSinceCollided = 1f; // sec delay before another collision possible
	            
                float volume = (float) velSq * 10f;
                if (volume > .8f) volume = .8f;
                
                float pitch = .4f + worldObj.rand.nextFloat() * .4f;
                log("volume: " + volume + ", pitch: " + pitch);
                
                worldObj.playSoundAtEntity(this, "random.explode",  volume, pitch);
                
                takeDamage((float) velSq * 100f);
                
	            motionX *= .7;
	            motionY *= .7;
	            motionZ *= .7;

	            for (int i = 0; i < 5; i++)
	            {
	                worldObj.spawnParticle("explode", posX - 1f + Math.random() *2f, posY - 1f + Math.random() *2f, posZ - 1f + Math.random() *2f, 0.0, 0.0, 0.0);
	            }
	        }
            //isCollidedHorizontally = false;
            //isCollidedVertically = false;
        }

    }
    
    private void takeDamage(float damage)
    {
        _damage += damage;
                
        /*
        if (riddenByEntity != null) // this is the player's helicopter, so show damage msg
        {
            Minecraft minecraft = ModLoader.getMinecraftInstance();
            minecraft.ingameGUI.addChatMessage("Damage: " + (int) (_damage * 100 / MAX_HEALTH) + "%");
        }
        */
        
        if (_damage > MAX_HEALTH && !worldObj.isRemote)
        {
            //die();

            // show message if not player helicopter
            if (riddenByEntity == null)
            {
                //Minecraft minecraft = ModLoader.getMinecraftInstance();
                //minecraft.ingameGUI.addChatMessage("Killed " + this);
            }
            else
            {
                riddenByEntity.ridingEntity = null;
                riddenByEntity = null;
            }
            
            boolean flaming = true;
            worldObj.newExplosion(this, posX, posY, posZ, 2.3f, flaming);
            
            setEntityDead();
        }
    }

    /*
    public void die()
    {
        riddenByEntity = null;
        
        setEntityDead();
        
        //dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);

        spawnParticles:
        {
            double d13 = Math.cos(((double) rotationYaw * 3.1415926535897931D) / 180D);
            double d15 = Math.sin(((double) rotationYaw * 3.1415926535897931D) / 180D);
            for (int i1 = 0; (double) i1 < 1.2 * 60D; i1++)
            {
                double d18 = rand.nextFloat() * 2.0F - 1.0F;
                double d20 = (double) (rand.nextInt(2) * 2 - 1) * 0.69999999999999996D;
                if (rand.nextBoolean())
                {
                    double d21 = (posX - d13 * d18 * 0.80000000000000004D) + d15 * d20;
                    double d23 = posZ - d15 * d18 * 0.80000000000000004D - d13 * d20;
                    worldObj.spawnParticle("smoke", d21, posY - 0.125D, d23, motionX, motionY, motionZ);
                }
                else
                {
                    double d22 = posX + d13 + d15 * d18 * 0.69999999999999996D;
                    double d24 = (posZ + d15) - d13 * d18 * 0.69999999999999996D;
                    worldObj.spawnParticle("explode", d22, posY - 0.125D, d24, motionX, motionY, motionZ);
                }
            }
        }
    }
    */
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int i)
    {
        log("attackEntityFrom called");
        return true; // the hit landed
    }

    @Override
    public boolean canBeCollidedWith()
    {
        // log("canBeCollidedWith called");
        return !isDead;
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    protected void entityInit()
    {
        log("EntityThxHelicopter entityInit called");
        
        // reload properties to pick up any changes
        //ThxConfig.loadProperties();

    }

    @Override
    public AxisAlignedBB getBoundingBox()
    {
        return boundingBox;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        return entity.boundingBox;
    }

    @Override
    public double getMountedYOffset()
    {
        return -.25;
    }

    @Override
    public boolean interact(EntityPlayer player)
    {
        if (riddenByEntity == player)
        {
            if (onGround || isCollidedVertically)
            {
                log("Landed " + this);
                
                pilotExit();
            }
            else log("Exited without landing, entering local drone mode");
            {
                enable_drone_mode = true; // fly helicopter remotely
                player.ridingEntity = null; // allow normal character movement
            }
            return true;
        }
        
        if (riddenByEntity != null)
        {
            // already ridden by some other entity
            return false;
        }
        
        // new pilot boarding
        
        if (player.ridingEntity != null) 
        {
            // already riding some entity, so clear it
            player.ridingEntity.riddenByEntity = null;
        }
        player.ridingEntity = this;
        riddenByEntity = player;
        
        // reset level to current look pitch
        lookPitchZeroLevel = player.rotationPitch;
        
        // inactivate ai
        targetHelicopter = null;
            
        if (ENABLE_DRONE_MODE)
        {
            // store original position of pilot
            dronePilotPosX = player.posX;
            dronePilotPosY = player.posY;
            dronePilotPosZ = player.posZ;
        }
        else
        {
            enable_drone_mode = false;
	        player.rotationYaw = rotationYaw;
        }
        
        log("interact() added pilot: " + player);
        return true;
    }

    @Override
    public void updateRiderPosition()
    {
        if (enable_drone_mode) return;
        
        EntityPlayer pilot = getPilot();
        if (pilot == null) return;

        // this will tell the default impl in pilot.updateRidden
        // that no adjustment need be made to the pilot's yaw or pitch
        // as a direct result of riding this helicopter entity.
        // rather, we let the player rotate the pilot and the helicopter follows
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;

        
        // for fixed pilot position while piloting drone
        /*
        if (enable_drone_mode) 
        {
            if (pilot.onGround)
            {
                pilot.setPosition(dronePilotPosX, dronePilotPosY, dronePilotPosZ);
            }
            else
            {
                // update pilot position
                dronePilotPosX = pilot.posX;
                dronePilotPosY = pilot.posY;
                dronePilotPosZ = pilot.posZ;
            }
            return;
        }
        */
        
        // use fwd XZ components to adjust front/back position of pilot based on helicopter pitch
        // when in 1st-person mode to improve view
        double posAdjust = 0; //-.1 + .02f * rotationPitch;

        //if (ModLoader.getMinecraftInstance().gameSettings.thirdPersonView != 0) posAdjust = 0.0;

        // to force camera to follow helicopter exactly, but stutters:
        //pilot.setPositionAndRotation(posX + fwd.x * posAdjust, posY + pilot.getYOffset() + getMountedYOffset(), posZ + fwd.z * posAdjust, rotationYaw, rotationPitch);
        //pilot.setLocationAndAngles(posX + fwd.x * posAdjust, posY -.7f, posZ + fwd.z * posAdjust, rotationYaw, rotationPitch);
        
        pilot.setPosition(posX + fwd.x * posAdjust, posY + pilot.getYOffset() + getMountedYOffset(), posZ + fwd.z * posAdjust);
        

    }

    private void pilotExit()
    {
        Entity pilot = getPilot();

        if (pilot == null) return;
        
        //model.visible = true; // hard to find otherwise!
        
        // clear pitch speed to prevent judder
        rotationPitchSpeed = 0f;
        
        pilot.mountEntity(this); // riddenByEntity is now null
        
        //((ThxModelHelicopter) model).rotorSpeed = 0f; // turn off rotor, it will spin down slowly
        
        if (enable_drone_mode) 
        {
            enable_drone_mode = false;
            return;
        }
        
        // place pilot to left of helicopter
        // (use fwd XZ perp to exit left: x = z, z = -x)
        double exitDist = 1.9;
        pilot.setPosition(posX + fwd.z * exitDist, posY + pilot.yOffset, posZ - fwd.x * exitDist);
    }
 
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        //log("writeEntityToNBT called");
        log("writeEntityToNBT - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        log("readEntityFromNBT called");
    }

    @Override
    protected void fall(float f)
    {
        // no damage from falling, unlike super.fall
        log("fall() called with arg " + f);
    }
    
    public boolean isKeyDown(int key)
    {
        return false;
    }
}
