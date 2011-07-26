package net.minecraft.src;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class ThxEntityHelicopter extends ThxEntity
{
    static int instanceCount = 0;

    // controls and options
    // set from mod_thx.properties
    static int KEY_ASCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("key_ascend"));
    static int KEY_DESCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("key_descend"));
    static int KEY_FORWARD = Keyboard.getKeyIndex(ThxConfig.getProperty("key_forward"));
    static int KEY_BACK = Keyboard.getKeyIndex(ThxConfig.getProperty("key_back"));
    static int KEY_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_left"));
    static int KEY_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_right"));
    static int KEY_ROTATE_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_rotate_left"));
    static int KEY_ROTATE_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_rotate_right"));
    static int KEY_FIRE_MISSILE = Keyboard.getKeyIndex(ThxConfig.getProperty("key_fire_missile"));
    static int KEY_FIRE_ROCKET = Keyboard.getKeyIndex(ThxConfig.getProperty("key_fire_rocket"));
    static int KEY_HUD_MODE = Keyboard.getKeyIndex(ThxConfig.getProperty("key_hud_mode"));
    static int KEY_AUTO_LEVEL = Keyboard.getKeyIndex(ThxConfig.getProperty("key_auto_level"));
    static int KEY_EXIT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_exit"));
    static int KEY_LOOK_BACK = Keyboard.getKeyIndex(ThxConfig.getProperty("key_look_back"));
        
    static boolean ENABLE_LOOK_YAW = ThxConfig.getBoolProperty("enable_look_yaw");
    static boolean ENABLE_LOOK_PITCH = ThxConfig.getBoolProperty("enable_look_pitch");
    static boolean ENABLE_DRONE_MODE = ThxConfig.getBoolProperty("enable_drone_mode");
    static boolean ENABLE_PILOT_AIM = ThxConfig.getBoolProperty("enable_pilot_aim");
    static boolean ENABLE_AUTO_LEVEL = ThxConfig.getBoolProperty("enable_auto_level");
    static boolean ENABLE_LOOK_DOWN_TRANS = ThxConfig.getBoolProperty("enable_look_down_trans");
    static boolean ENABLE_AUTO_THROTTLE_ZERO = ThxConfig.getBoolProperty("enable_auto_throttle_zero");
    static boolean ENABLE_HEAVY_WEAPONS = ThxConfig.getBoolProperty("enable_heavy_weapons");
        
    final float MAX_HEALTH = 80f;

    // handling properties
    final float MAX_ACCEL    = 0.30f; // very slowly sink when neutral throttle
    final float GRAVITY      = 0.301f;
    final float MAX_VELOCITY = 0.44f;
    final float FRICTION = 0.98f;

    // v02: final float MAX_PITCH = 50.00f;
    final float MAX_PITCH = 60.00f;
    final float PITCH_SPEED_DEG = 40f;
    final float PITCH_RETURN = 0.98f;

    // v02: final float MAX_ROLL = 18.00f;
    final float MAX_ROLL = 30.00f;
    final float ROLL_SPEED_DEG = 40f;
    final float ROLL_RETURN = 0.92f;

    float throttle = 0.0f;
    final float THROTTLE_MIN = -.03f;
    //final float THROTTLE_MIN = -.04f;
    //final float THROTTLE_MAX = .10f;
    final float THROTTLE_MAX = .07f;
    final float THROTTLE_INC = .005f;

    public float getThrottlePower()
    {
        return (throttle - THROTTLE_MIN) / (THROTTLE_MAX - THROTTLE_MIN);
    }

    // Vectors for repeated calculations
    Vector3f thrust = new Vector3f();
    Vector3f velocity = new Vector3f();
    
    // amount of vehicle motion to transfer upon projectile launch
    final float MOMENTUM = .2f;

    // total update count
    float _damage;
    float timeSinceHit;
    
    float hudDelay;
    
    boolean lookBack;
    boolean toggleLookBack;
    float toggleLookBackDelay;
    
    float missileDelay;
    final float MISSILE_DELAY = 3f;

    float rocketDelay;
    final float ROCKET_DELAY = .12f;
    
    int rocketCount;
    final int FULL_ROCKET_COUNT = 12;
    
    float rocketReload;
    final float ROCKET_RELOAD = 2f;
    
    float autoLevelDelay; // seconds remaining, not cycles
    float exitDelay; // seconds remaining, not cycles

    double dronePilotPosX;
    double dronePilotPosY;
    double dronePilotPosZ;
    
    boolean prevThirdPersonView;
    
    final float TURN_SPEED_DEG = -2.00f;
    
    // enemy AI helicopter or friend?
    public ThxEntityHelicopter targetHelicopter;
    public boolean isTargetHelicopterFriendly;
    Vector3f deltaPosToTarget = new Vector3f();

    public ThxEntityHelicopter(World world)
    {
        super(world);

        // new Exception("EntityThxHelicopter call stack:").printStackTrace();

        model = new ThxModelHelicopter();
        model.renderTexture = "/thx/helicopter.png";

        setSize(1.8f, 2f);

        yOffset = .6f;

        instanceCount++;
        log("ThxEntityHelicopter instance count: " + instanceCount);
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        setLocationAndAngles(x, y + yOffset, z, yaw, 0f);
        //setPosition(x, y + yOffset, z);
        
        log(toString() + " - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
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
        
        timeSinceHit -= deltaTime;
        missileDelay -= deltaTime;
        rocketDelay -= deltaTime;
        rocketReload -= deltaTime;
        
        Minecraft minecraft = ModLoader.getMinecraftInstance();
        
        // cancel ai if target helicopter is empty
        //if (targetHelicopter != null && targetHelicopter.riddenByEntity == null) targetHelicopter = null;
        
        // if (ModLoader.isGUIOpen(null) && minecraft.thePlayer.ridingEntity == this)
        EntityPlayer pilot = getPilot();
        if (pilot != null || targetHelicopter != null) //minecraft.thePlayer.ridingEntity == this)
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
            else
            {
                if (ENABLE_LOOK_DOWN_TRANS && pilot != null)
                {
	                // hide bottom panel for looking down when in air
	                //if (pilot.rotationPitch - rotationPitch > 60f)
	                if (pilot.rotationPitch > 60f)
	                {
	                    ((ThxModelHelicopter) model).bottomVisible = false;
	                }
	                else
	                {
	                    ((ThxModelHelicopter) model).bottomVisible = true;
	                }
                }
            }

            toggleLookBackDelay -= deltaTime;
            if (Keyboard.isKeyDown(KEY_LOOK_BACK) && toggleLookBackDelay < 0f && !ENABLE_DRONE_MODE && !ENABLE_LOOK_PITCH && pilot != null)
            {
                lookBack = !lookBack;
                toggleLookBack = true;
                toggleLookBackDelay = .5f;
            }
                
            hudDelay -= deltaTime;
            if (Keyboard.isKeyDown(KEY_HUD_MODE) && hudDelay < 0f && !ENABLE_DRONE_MODE && pilot != null)
            {
                hudDelay = .5f;
                
                // toggle hud
                if (model.visible)
                {
                    // engage hud
                    model.visible = false;
                    
                    //Minecraft.isGuiEnabled();
                    //if(ModLoader.isGUIOpen(null) && minecraft.thePlayer.ridingEntity != null && minecraft.thePlayer.ridingEntity == this)

                    minecraft.ingameGUI.addChatMessage("PosX: " + (int)posX + ", PosZ: " + (int)posZ + ", Alt: " + (int)posY + ", Damage: " + (int)(_damage * 100 / MAX_HEALTH) + "%");
                    
                    prevThirdPersonView = minecraft.gameSettings.thirdPersonView;
                    minecraft.gameSettings.thirdPersonView = false;
                }
                else
                {
                    // turn off hud
                    model.visible = true;
                    minecraft.gameSettings.thirdPersonView = prevThirdPersonView;
                }
            }
            
            // view could be switched by player
            if (minecraft.gameSettings.thirdPersonView) model.visible = true;

            exitDelay -= deltaTime;
            if (Keyboard.isKeyDown(KEY_EXIT) && exitDelay < 0f && pilot != null)
            {
                exitDelay = 1f; // seconds, not update cycles
                pilotExit();
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
                || Keyboard.isKeyDown(KEY_FIRE_ROCKET)) 
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
                float pitch = rotationPitch;
                if (ENABLE_PILOT_AIM && !ENABLE_DRONE_MODE && pilot != null)
                {
                    yaw = pilot.rotationYaw;
                    pitch = pilot.rotationPitch;
                }
                if (targetHelicopter != null && isTargetHelicopterFriendly && targetHelicopter.riddenByEntity != null)
                {
	                yaw = targetHelicopter.riddenByEntity.rotationYaw;
	                pitch = targetHelicopter.riddenByEntity.rotationPitch;
                }
                
                ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                if (ENABLE_HEAVY_WEAPONS) newRocket.enableHeavyWeapons = true;
                newRocket.owner = this;
                worldObj.entityJoinedWorld(newRocket);
                
                if (rocketCount == FULL_ROCKET_COUNT)
                {
                    // must reload before next volley
                    rocketReload = ROCKET_RELOAD;
                    rocketCount = 0;
                }
            }

            // FIRE MISSILE
            if (missileDelay < 0f && targetHelicopter != null && !isTargetHelicopterFriendly && Math.abs(targetHelicopter.posY - posY) < 2f) // && _damage > MAX_HEALTH / 2f) // ai fire missle when low health
            {
                missileDelay = MISSILE_DELAY * 2; // nerf fire rate
                
                float offX = fwd.x * 2f;
                float offY = fwd.y * 2f;
                float offZ = fwd.z * 2f;

                float yaw = rotationYaw;
                float pitch = rotationPitch;
                
                ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                newMissile.targetHelicopter = targetHelicopter;
                worldObj.entityJoinedWorld(newMissile);

            }
            else if (Keyboard.isKeyDown(KEY_FIRE_MISSILE) && missileDelay < 0f)
            {
                missileDelay = MISSILE_DELAY;
                
                float offX = fwd.x * 2f;
                float offY = fwd.y * 2f;
                float offZ = fwd.z * 2f;

                float yaw = rotationYaw;
                float pitch = rotationPitch;
                if (ENABLE_PILOT_AIM && !ENABLE_DRONE_MODE && pilot != null)
                {
                    yaw = pilot.rotationYaw;
                    pitch = pilot.rotationPitch;
                }
                if (targetHelicopter != null && isTargetHelicopterFriendly && targetHelicopter.riddenByEntity != null)
                {
                    yaw = targetHelicopter.riddenByEntity.rotationYaw;
                    pitch = targetHelicopter.riddenByEntity.rotationPitch;
                }

                ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                //ThxEntityAgent newMissile = new ThxEntityAgent(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                if (ENABLE_HEAVY_WEAPONS) newMissile.enableHeavyWeapons = true;
                worldObj.entityJoinedWorld(newMissile);
            }

            // START YAW
            if (targetHelicopter != null && isTargetHelicopterFriendly && thd < 10f)
            {
	                // mimic friendly target yaw rotation when close by
	                float deltaYawDeg = targetHelicopter.rotationYaw - rotationYaw;
	
	                while (deltaYawDeg > 180f) deltaYawDeg -= 360f;
	                while (deltaYawDeg < -180f) deltaYawDeg += 360f;
	
	                rotationYawSpeed = deltaYawDeg * 3f; // saving this for render use
	                if (rotationYawSpeed > 90) rotationYawSpeed = 90;
	                if (rotationYawSpeed < -90) rotationYawSpeed = -90;
	                rotationYaw += rotationYawSpeed * deltaTime;
            }
            else if (targetHelicopter != null) // && !isTargetHelicopterFriendly)
            {
                // turn toward target helicopter
                
                Vector3f deltaPos = Vector3f.add(targetHelicopter.pos, pos.negate(null), null);
                //deltaPos.add(vel, deltaPos, deltaPos);
                
                if (Vector3f.dot(side, deltaPos) > 0)
                {
                    rotationYaw += 60f * deltaTime;
                }
                else
                {
                    rotationYaw -= 60f * deltaTime;
                }
                
            }
            else if (ENABLE_LOOK_YAW && !ENABLE_DRONE_MODE && pilot != null)
            {
                // input from look control (mouse or analog stick)
                float deltaYawDeg = pilot.rotationYaw - rotationYaw;
                if (lookBack) deltaYawDeg += 180f;

                while (deltaYawDeg > 180f) deltaYawDeg -= 360f;
                while (deltaYawDeg < -180f) deltaYawDeg += 360f;

                //rotationYaw += deltaYawDeg * TURN_SPEED_DEG * .04f;
                //rotationYawSpeed = deltaYawDeg * TURN_SPEED_DEG; // saving this for render use
                rotationYawSpeed = deltaYawDeg * 3f; // saving this for render use
                if (rotationYawSpeed > 90) rotationYawSpeed = 90;
                if (rotationYawSpeed < -90) rotationYawSpeed = -90;
                rotationYaw += rotationYawSpeed * deltaTime;
            }
            else
            // buttonYaw:
            {
                // button yaw
                if (Keyboard.isKeyDown(KEY_ROTATE_LEFT)) // g, rotate left
                {
                    //rotationYaw -= TURN_SPEED_DEG * deltaTime;
                    rotationYawSpeed -= 8f;
                }
                else if (Keyboard.isKeyDown(KEY_ROTATE_RIGHT)) // h, rotate right
                {
                    //rotationYaw += TURN_SPEED_DEG * deltaTime;
                    rotationYawSpeed += 8f;
                }
                else
                {
                    rotationYawSpeed *= .9f;
                }
                if (rotationYawSpeed > 80) rotationYawSpeed = 80;
                if (rotationYawSpeed < -80) rotationYawSpeed = -80;
                rotationYaw += rotationYawSpeed * deltaTime;
            }
            
            rotationYaw %= 360f;

            // PITCH CONTROL
            //
            // the cyclic (tilt control)
            // only affects pitch and roll, acceleration done later
            // zero pitch is level, positive pitch is leaning forward
            
            if (targetHelicopter != null)
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
            else if (ENABLE_LOOK_PITCH && !ENABLE_DRONE_MODE)
            {
                if (rotationPitch > MAX_PITCH)
                {
                    rotationPitch = MAX_PITCH;
                    rotationPitchSpeed = 0f;
                }
                else if (rotationPitch < -MAX_PITCH)
                {
                    rotationPitch = -MAX_PITCH;
                    rotationPitchSpeed = 0f;
                }
                else
                {
                    rotationPitchSpeed = 4f * (pilot.rotationPitch - rotationPitch -20f); // look down slightly on level helicopter in 3rd person view
                    rotationPitch += rotationPitchSpeed * deltaTime;
                }
                
                if (rotationPitch > MAX_PITCH) // check again to prevent judder
                {
                    rotationPitch = MAX_PITCH;
                    rotationPitchSpeed = 0f;
                }
                else if (rotationPitch < -MAX_PITCH)
                {
                    rotationPitch = -MAX_PITCH;
                    rotationPitchSpeed = 0f;
                }
            }
            else // if (pilot != null)// button pitch and roll
            {
                if (autoLevelDelay > 0)
                {
                    // TODO: need to use delta time to adjust for framerate
                    autoLevelDelay -= deltaTime;
                    if (Math.abs(rotationPitch) > 1f) rotationPitch *= .8f;
                    else rotationPitch = 0f;
                    if (Math.abs(rotationRoll) > 1f) rotationRoll *= .8f;
                    else rotationRoll = 0f;
                }
                else if (Keyboard.isKeyDown(KEY_AUTO_LEVEL))
	            {
	                autoLevelDelay = 1f; // seconds, not update cycles
	            }
	            else if (Keyboard.isKeyDown(KEY_FORWARD))
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
                    if (rotationPitch < -MAX_PITCH)
                    {
                        rotationPitch = -MAX_PITCH;
	                    rotationPitchSpeed = 0f;
                    }
                    else
                    {
	                    rotationPitchSpeed = -PITCH_SPEED_DEG;
                        rotationPitch += rotationPitchSpeed * deltaTime;
                    }
                    if (rotationPitch < -MAX_PITCH) // check again to prevent judder
                    {
                        rotationPitch = -MAX_PITCH;
	                    rotationPitchSpeed = 0f;
                    }
                }
                else
                {
                    if (ENABLE_AUTO_LEVEL)
                    {
		                rotationPitchSpeed = -rotationPitch * .5f;
		                rotationPitch += rotationPitchSpeed * deltaTime;
                    }
                }
            }

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
            if (targetHelicopter != null)
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
            else if (Keyboard.isKeyDown(KEY_ASCEND) 
                    || (Keyboard.isKeyDown(KEY_FORWARD) && ENABLE_LOOK_PITCH && !ENABLE_DRONE_MODE)) 
            {
                if (throttle < THROTTLE_MAX) throttle += THROTTLE_INC;
                if (throttle > THROTTLE_MAX) throttle = THROTTLE_MAX;
                // throttle = THROTTLE_MAX;
            }
            else if (Keyboard.isKeyDown(KEY_DESCEND)
                    || (Keyboard.isKeyDown(KEY_BACK) && ENABLE_LOOK_PITCH && !ENABLE_DRONE_MODE)) 
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
	        ((ThxModelHelicopter) model).rotorSpeed = getThrottlePower() / 2f + .7f;
	        //((ThxModelHelicopter) model).rotorSpeed = 1f;
            
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
            thrust.normalise().scale(MAX_ACCEL * (1f + throttle) * dT);

            // apply the thrust
            Vector3f.add(velocity, thrust, velocity);

            // gravity is always straight down
            velocity.y -= GRAVITY * dT;

            // limit max velocity
            if (velocity.lengthSquared() > MAX_VELOCITY * MAX_VELOCITY)
            {
                velocity.scale(MAX_VELOCITY / velocity.length());
            }

            // apply velocity changes
            motionX = velocity.x;
            motionY = velocity.y;
            motionZ = velocity.z;
        }
        else
        // no pilot -- slowly sink to the ground
        {
            model.visible = true;
            ((ThxModelHelicopter) model).rotorSpeed = 0;
            ((ThxModelHelicopter) model).bottomVisible = true;

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
                motionY -= GRAVITY * .16f * dT;
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
	        if (velSq > .06)
	        {
	            log("crash velSq: " + velSq);
	            attackEntityFrom(this, 4);
	            
	            motionX *= .7;
	            motionY *= .7;
	            motionZ *= .7;
	        }
            isCollidedHorizontally = false;
            isCollidedVertically = false;
        }
    }

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
    
    @Override
    public boolean attackEntityFrom(Entity entity, int i)
    {
        log("attackEntityFrom called");
        
        log("attacked by entity: " + entity);
        
        // take damage sound
        worldObj.playSoundAtEntity(this, "random.drr", 1.0f, 1.0f);

        if (timeSinceHit > 0 || isDead || riddenByEntity == entity) return false;
        
        //if (riddenByEntity == null && targetHelicopter == null && entity instanceof ThxEntityHelicopter)
        if (riddenByEntity == null && entity != this && entity instanceof ThxEntityHelicopter)
        {
            // crashing takes damage from self, so have to check if this
            
            if (targetHelicopter == null)
            {
	            // wake up ai if empty helicopter is attacked, friendly at first
                isTargetHelicopterFriendly = true;
	            targetHelicopter = (ThxEntityHelicopter) entity;
	            worldObj.playSoundAtEntity(entity, "random.fuse", 1.0f, 1.0f);
            }
            else if (entity == targetHelicopter && isTargetHelicopterFriendly)
            {
                isTargetHelicopterFriendly = false;
                System.out.println("enemy helo");
            }
            //return true;
        }
               
        _damage += (float)i * 4f;
        log ("current damage percent: " + (100f * _damage / MAX_HEALTH));
        
        Minecraft minecraft = ModLoader.getMinecraftInstance();
        if (riddenByEntity != null) // this is the player's helicopter, so show damage
        {
            minecraft.ingameGUI.addChatMessage("Damage: " + (int) (_damage * 100 / MAX_HEALTH) + "%");
        }
        timeSinceHit = .5f; // sec delay before this entity can be hit again
        
        setBeenAttacked();

        if (_damage > MAX_HEALTH)
        {
            die();
            if (riddenByEntity == null) minecraft.ingameGUI.addChatMessage("Killed " + this);
            
            worldObj.newExplosion(this, posX, posY, posZ, 1f, true);
        }
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
        //log("interact called");

        if (riddenByEntity == null)
        {
            // new pilot boarding
	        player.mountEntity(this);
            prevThirdPersonView = ModLoader.getMinecraftInstance().gameSettings.thirdPersonView;
	        
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
		        player.rotationYaw = rotationYaw;
	        }
        }
        else pilotExit();
        
        return false;
    }

    @Override
    public void updateRiderPosition()
    {
        EntityPlayer pilot = getPilot();

        if (pilot == null) return;

        // this will tell the default impl in pilot.updateRidden
        // that no adjustment need be made to the pilot's yaw or pitch
        // as a direct result of riding this helicopter entity.
        // rather, we let the player rotate the pilot and the helicopter follows
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;

        if (ENABLE_DRONE_MODE)
        {
            pilot.setPosition(dronePilotPosX, dronePilotPosY, dronePilotPosZ);
            return;
        }
        
        pilot.setPosition(posX, posY + pilot.getYOffset() + getMountedYOffset(), posZ);
            
        if (toggleLookBack)
        {
            toggleLookBack = false;
	        pilot.prevRotationYaw = pilot.rotationYaw = pilot.rotationYaw -180;
	        //if (ENABLE_LOOK_PITCH) pilot.prevRotationPitch = pilot.rotationPitch = -pilot.rotationPitch;
        }
    }

    private void pilotExit()
    {
        Entity pilot = getPilot();
        
        model.visible = true; // hard to find otherwise!
        
        if (ENABLE_DRONE_MODE) // end drone mode
        {
	        pilot.mountEntity(this); // riddenByEntity is now null
	        ((ThxModelHelicopter) model).rotorSpeed = 0; // turn off rotor, it will spin down slowly
	        
	        // place pilot at position where drone mode was engaged
	        pilot.setPosition(dronePilotPosX, dronePilotPosY, dronePilotPosZ);
	        
	        return;
        }

        // cancel look back for next entry
        toggleLookBack = false;
        if (lookBack)
        {
	        lookBack = false;
	        pilot.prevRotationYaw = pilot.rotationYaw = pilot.rotationYaw -180;
        }
        
        // restore former view setting
        ModLoader.getMinecraftInstance().gameSettings.thirdPersonView = prevThirdPersonView;
        
        pilot.mountEntity(this); // riddenByEntity is now null
        
        ((ThxModelHelicopter) model).rotorSpeed = 0; // turn off rotor, it will spin down slowly
        
        // use fwd XZ perp to exit left: x = z; z = -x;
        double exitDist = 1.9 ;
        pilot.setPosition(posX + fwd.z * exitDist, posY + pilot.yOffset, posZ - fwd.x * exitDist);
    }

 
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        // log("writeEntityToNBT called");
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        // log("readEntityFromNBT called");
    }
    
    @Override
    protected void fall(float f)
    {
        // no damage from falling, unlike super.fall
    }
}
