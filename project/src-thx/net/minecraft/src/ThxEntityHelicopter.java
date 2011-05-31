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
    static int KEY_ASCEND;
    static int KEY_DESCEND;
    static int KEY_FORWARD;
    static int KEY_BACK;
    static int KEY_LEFT;
    static int KEY_RIGHT;
    static int KEY_ROTATE_LEFT;
    static int KEY_ROTATE_RIGHT;
    static int KEY_FIRE_MISSILE;
    static int KEY_FIRE_ROCKET;
    static int KEY_ENTER_EXIT;
    static boolean ENABLE_LOOK_YAW;
    static boolean ENABLE_LOOK_PITCH;
    static boolean ENABLE_DRONE_MODE;
    static boolean ENABLE_PILOT_AIM;
    static boolean ENABLE_AUTO_LEVEL;
    static boolean ENABLE_LOOK_DOWN_TRANS;
    static boolean ENABLE_AUTO_THROTTLE_ZERO;

    final int MAX_HEALTH = 100;

    // handling properties
    final float MAX_ACCEL = 0.299f; // very slowly sink when neutral throttle
    final float MAX_VELOCITY = 0.44f;
    final float GRAVITY = 0.30f;
    final float TURN_SPEED_DEG = 2.00f;
    final float FRICTION = 0.98f;

    // v02: final float MAX_PITCH = 50.00f;
    final float MAX_PITCH = 60.00f;
    final float PITCH_SPEED_DEG = 1.80f;
    final float PITCH_RETURN = 0.98f;

    // v02: final float MAX_ROLL = 18.00f;
    final float MAX_ROLL = 30.00f;
    final float ROLL_SPEED_DEG = 2.00f;
    final float ROLL_RETURN = 0.92f;

    float throttle = 0.0f;
    final float THROTTLE_MIN = -.04f;
    final float THROTTLE_MAX = .10f;
    final float THROTTLE_INC = .02f;

    public float getThrottlePower()
    {
        return (throttle - THROTTLE_MIN) / (THROTTLE_MAX - THROTTLE_MIN);
    }

    // Vectors for repeated calculations
    Vector3f thrust;
    Vector3f velocity;
    
    // amount of vehicle motion to transfer upon projectile launch
    final float MOMENTUM = .2f;

    // total update count
    int _damage = 0;
    int _sinceHit = 0;
    
    int _missileDelay = 0;
    final int MISSILE_DELAY = 64;

    int _rocketDelay = 0;
    final int ROCKET_DELAY = 4;
    
    int _rocketCount = 0;
    final int FULL_ROCKET_COUNT = 8;
    
    int _rocketReload = 0;
    final int ROCKET_RELOAD = 32;

    double dronePilotPosX = 0.0;
    double dronePilotPosY = 0.0;
    double dronePilotPosZ = 0.0;

    public ThxEntityMissile missile = null;

    public ThxEntityHelicopter(World world)
    {
        super(world);

        // new Exception("EntityThxHelicopter call stack:").printStackTrace();

        model = new ThxModelHelicopter();
        renderTexture = "/thx/helicopter.png";

        setSize(1.8f, 2f);

        yOffset = .6f;

        thrust = new Vector3f();
        velocity = new Vector3f();

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

    /*
     * public void setPosition(double x, double y, double z) {
     * super.setPosition(x, y + yOffset, z); }
     */

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        //log("yaw: " + rotationYaw + ", pitch: " + rotationPitch + ", forwardV: " + getForward());
        //log("yaw: " + rotationYaw + ", pitch: " + rotationPitch + ", fwd     : " + fwd);
        //log("roll: " + rotationRoll + ", pitch: " + rotationPitch + ", side  : " + side);

        if (_damage > 0) _damage--;
        if (_sinceHit > 0) _sinceHit--;
        if (_missileDelay > 0) _missileDelay--;
        if (_rocketDelay > 0) _rocketDelay--;
        if (_rocketReload > 0) _rocketReload--;

        // if (ModLoader.isGUIOpen(null) && minecraft.thePlayer.ridingEntity == this)
        EntityPlayer pilot = getPilot();
        if (pilot != null) //minecraft.thePlayer.ridingEntity == this)
        {
            if (pilot.isDead) riddenByEntity = null;
            
            if (onGround) // very slow on ground
            {
                if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
                if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral

                // double apply friction when on ground
                motionX *= FRICTION;
                motionY = 0.0;
                motionZ *= FRICTION;
            }
            else
            {
                if (ENABLE_LOOK_DOWN_TRANS)
                {
	                // hide bottom panel for looking down when in air
	                //if (pilot.rotationPitch - rotationPitch > 60f)
	                if (pilot.rotationPitch > 50f)
	                {
	                    ((ThxModelHelicopter) model).bottomVisible = false;
	                }
	                else
	                {
	                    ((ThxModelHelicopter) model).bottomVisible = true;
	                }
                }
            }

            if (Keyboard.isKeyDown(KEY_ENTER_EXIT))
            {
                ((ThxModelHelicopter) model).bottomVisible = true;
                    
                double exitDist = 1.9;
                interact(pilot); // enter/exit vehicle
                pilot.setPosition(posX + fwd.z * exitDist, posY + pilot.yOffset, posZ - fwd.x * exitDist);
            }

            if (Keyboard.isKeyDown(KEY_FIRE_ROCKET) && _rocketDelay == 0 && _rocketReload == 0)
            {
                _rocketCount++;
                _rocketDelay = ROCKET_DELAY;
                
                float leftRight = (_rocketCount % 2 == 0) ? 1.0f : -1.0f;
                
                if (_rocketCount == FULL_ROCKET_COUNT)
                {
                    // must reload before next volley
                    _rocketReload = ROCKET_RELOAD;
                    _rocketCount = 0;
                }

                float offsetX = side.x * leftRight + fwd.x;
                float offsetY = side.y * leftRight + fwd.y;
                float offsetZ = side.z * leftRight + fwd.z;
                    
                float yaw = rotationYaw;
                float pitch = rotationPitch;
                if (ENABLE_PILOT_AIM && !ENABLE_DRONE_MODE)
                {
                    yaw = pilot.rotationYaw;
                    pitch = pilot.rotationPitch;
                }
                ThxEntityRocket newRocket = new ThxEntityRocket(worldObj, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
                worldObj.entityJoinedWorld(newRocket);
            }

            if (Keyboard.isKeyDown(KEY_FIRE_MISSILE) && _missileDelay == 0)
            {
                _missileDelay = MISSILE_DELAY;

                if (ENABLE_PILOT_AIM && !ENABLE_DRONE_MODE)
                {
                    // use pilot look to aim
                    
                    ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX, posY -yOffset, posZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, pilot.rotationYaw, pilot.rotationPitch);
                    //ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX, posY, posZ, 0.0, 0.0, 0.0, 0f, 0f);
                    worldObj.entityJoinedWorld(newMissile);
                }
                else
                {
                    // use helicopter to aim
                    
                    ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX, posY -yOffset, posZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, rotationYaw, rotationPitch);
                    worldObj.entityJoinedWorld(newMissile);
                }
            }

            if (ENABLE_LOOK_YAW && !ENABLE_DRONE_MODE)
            {
                // input from look control (mouse or analog stick)
                float deltaYawDeg = rotationYaw - pilot.rotationYaw; // 90

                while (deltaYawDeg > 180f) deltaYawDeg -= 360f;
                while (deltaYawDeg < -180f) deltaYawDeg += 360f;

                if (deltaYawDeg < -15f)
                {
                    rotationYaw += TURN_SPEED_DEG;
                    if (deltaYawDeg < -45f) rotationYaw += TURN_SPEED_DEG * 2f;
                }

                if (deltaYawDeg > 15f)
                {
                    rotationYaw -= TURN_SPEED_DEG;
                    if (deltaYawDeg > 45f) rotationYaw -= TURN_SPEED_DEG * 2f;
                }
            }
            else
            // buttonYaw:
            {
                // button yaw
                if (Keyboard.isKeyDown(KEY_ROTATE_LEFT)) // g, rotate left
                {
                    rotationYaw -= TURN_SPEED_DEG;
                }
                if (Keyboard.isKeyDown(KEY_ROTATE_RIGHT)) // h, rotate right
                {
                    rotationYaw += TURN_SPEED_DEG;
                }
            }

            rotationYaw %= 360f;

            // the cyclic (tilt) controls
            // only affects pitch and roll, acceleration done later
            if (ENABLE_LOOK_PITCH && !ENABLE_DRONE_MODE)
            {
                rotationPitch = pilot.rotationPitch;
                // rotationPitch %= 360f;
                if (rotationPitch > MAX_PITCH) rotationPitch = MAX_PITCH;
                if (rotationPitch < -MAX_PITCH) rotationPitch = -MAX_PITCH;
            }
            else // button pitch and roll
            {
                if (Keyboard.isKeyDown(KEY_FORWARD))
                {
                    // zero pitch is level, positive pitch is leaning forward
                    rotationPitch += PITCH_SPEED_DEG;
                    if (rotationPitch > MAX_PITCH) rotationPitch = MAX_PITCH;
                }
                else if (Keyboard.isKeyDown(KEY_BACK))
                {
                    rotationPitch -= PITCH_SPEED_DEG;
                    if (rotationPitch < -MAX_PITCH) rotationPitch = -MAX_PITCH;
                }
                else
                {
                    if (ENABLE_AUTO_LEVEL) rotationPitch *= PITCH_RETURN;
                }
            }

            if (Keyboard.isKeyDown(KEY_LEFT))
            {
                rotationRoll += ROLL_SPEED_DEG;
                if (rotationRoll > MAX_ROLL) rotationRoll = MAX_ROLL;
            }
            else if (Keyboard.isKeyDown(KEY_RIGHT))
            {
                rotationRoll -= ROLL_SPEED_DEG;
                if (rotationRoll < -MAX_ROLL) rotationRoll = -MAX_ROLL;
            }
            else
            {
                if (ENABLE_AUTO_LEVEL) rotationRoll *= ROLL_RETURN;
            }

            // collective (throttle) control
            if (Keyboard.isKeyDown(KEY_ASCEND)) // space, increase throttle
            {
                if (throttle < THROTTLE_MAX) throttle += THROTTLE_INC;
                if (throttle > THROTTLE_MAX) throttle = THROTTLE_MAX;
                // throttle = THROTTLE_MAX;
            }
            else if (Keyboard.isKeyDown(KEY_DESCEND))
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
	        //((ThxModelHelicopter) model).rotorSpeed = getThrottlePower() + .5f;
	        ((ThxModelHelicopter) model).rotorSpeed = dT;
            
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
            ((ThxModelHelicopter) model).rotorSpeed = 0;

            if (onGround)
            {
                // tend to stay put on ground
                motionY = 0.;
                motionX *= .5;
                motionZ *= .5;
            }
            else
            {
                // settle back to ground slowly if pilot bails
                motionX *= FRICTION;
                motionY -= GRAVITY * .16f * dT;
                motionZ *= FRICTION;
            }

            rotationPitch *= PITCH_RETURN;
            rotationRoll *= ROLL_RETURN;
        }
        
        // move in all cases
        moveEntity(motionX, motionY, motionZ);

        // from EntityBoat class:
        detectCollisions:
        {
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.20000000298023224D, 0.0D, 0.20000000298023224D));
            if (list != null && list.size() > 0)
            {
                for (int j1 = 0; j1 < list.size(); j1++)
                {
                    Entity entity = (Entity) list.get(j1);
                    if (entity != pilot && entity.canBePushed() && (entity instanceof EntityBoat))
                    {
                        entity.applyEntityCollision(this);
                    }
                }
            }
        }

        // crash, take damage and slow down
        if (isCollidedHorizontally || isCollidedVertically)
        {
            
	        double velSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
            log("crash velSq: " + velSq);
            
	        if (velSq > .01)
	        {
	            attackEntityFrom(this, 1);
	            
	            /*
	            motionX *= .5;
	            motionY *= .5;
	            motionZ *= .5;
	            */
	        }
            isCollidedHorizontally = false;
            isCollidedVertically = false;
        }
    }

    public void die()
    {
        riddenByEntity = null;
        
        setEntityDead();
        dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);

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
        
        if (_sinceHit > 0 || isDead) return true;

        worldObj.playSoundAtEntity(this, "random.drr", 0.6f, 1.0f);

        _damage += i * 20;
        log ("current damage percent: " + (100f * (float)_damage / (float)MAX_HEALTH));
        
        _sinceHit = 10; // delay before this entity can be hit again
        
        setBeenAttacked();

        if (_damage > MAX_HEALTH)
        {
            die();
        }
        return true;
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

        KEY_ASCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("ascend"));
        KEY_DESCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("descend"));
        KEY_FORWARD = Keyboard.getKeyIndex(ThxConfig.getProperty("forward"));
        KEY_BACK = Keyboard.getKeyIndex(ThxConfig.getProperty("back"));
        KEY_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("left"));
        KEY_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("right"));
        KEY_ROTATE_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("rotate_left"));
        KEY_ROTATE_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("rotate_right"));
        KEY_FIRE_MISSILE = Keyboard.getKeyIndex(ThxConfig.getProperty("key_fire_missile"));
        KEY_FIRE_ROCKET = Keyboard.getKeyIndex(ThxConfig.getProperty("key_fire_rocket"));
        KEY_ENTER_EXIT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_enter_exit"));

        ENABLE_LOOK_YAW = ThxConfig.getBoolProperty("enable_look_yaw");
        ENABLE_LOOK_PITCH = ThxConfig.getBoolProperty("enable_look_pitch");
        ENABLE_DRONE_MODE = ThxConfig.getBoolProperty("enable_drone_mode");
        ENABLE_PILOT_AIM = ThxConfig.getBoolProperty("enable_pilot_aim");
        ENABLE_AUTO_LEVEL = ThxConfig.getBoolProperty("enable_auto_level");
        ENABLE_LOOK_DOWN_TRANS = ThxConfig.getBoolProperty("enable_look_down_trans");
        ENABLE_AUTO_THROTTLE_ZERO = ThxConfig.getBoolProperty("enable_auto_throttle_zero");
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
        log("interact called");

        if (ENABLE_DRONE_MODE)
        {
            dronePilotPosX = player.posX;
            dronePilotPosY = player.posY;
            dronePilotPosZ = player.posZ;
        }

        if (riddenByEntity != null && (riddenByEntity instanceof EntityPlayer) && riddenByEntity != player)
        {
            return true;
        }
        
        ((ThxModelHelicopter) model).rotorSpeed = 1;
        player.mountEntity(this);

        // if (riddenByEntity == null) player.mountEntity(this);
        return false;
    }

    @Override
    public void updateRiderPosition()
    {
        EntityPlayer pilot = getPilot();

        if (pilot == null)
            return;

        // this will tell the default impl in pilot.updateRidden
        // that no adjustment need be made to the pilot's yaw or pitch
        // as a direct result of riding this helicopter entity
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;

        if (ENABLE_DRONE_MODE)
        {
            pilot.setPosition(dronePilotPosX, dronePilotPosY, dronePilotPosZ);
        }
        else
        {
            pilot.setPosition(posX, posY + pilot.getYOffset() + getMountedYOffset(), posZ);
        }
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

    public void debugControls()
    {
        Vector3f lookAt = getForward();
        log("yaw: " + rotationYaw + ", pitch: " + rotationPitch + ", lookAt: " + lookAt);
        
        if (riddenByEntity == null) return;
        
        rotationYaw = riddenByEntity.rotationYaw;
        rotationPitch = riddenByEntity.rotationPitch;
        
        if (riddenByEntity == null) return;
        
        Vector3f thrust = new Vector3f(lookAt); // start with look and transform
        
        // scale horizontal XZ thrust by tilt. needs smoothing?
        thrust.x *= (1 - thrust.y);
        thrust.z *= (1 - thrust.y);
        
        // debug controls
        if (Keyboard.isKeyDown(KEY_FORWARD))
        {
            motionX += thrust.x * MAX_ACCEL;
            motionZ += thrust.z * MAX_ACCEL;
        }
        else if (Keyboard.isKeyDown(KEY_BACK))
        {
            motionX -= thrust.x * MAX_ACCEL;
            motionZ -= thrust.z * MAX_ACCEL;
        }
        if (Keyboard.isKeyDown(KEY_LEFT))
        {
            // take perp
            motionX += thrust.z;
            motionZ -= thrust.x;
        }
        else if (Keyboard.isKeyDown(KEY_RIGHT))
        {
            motionX -= thrust.z;
            motionZ += thrust.x;
        }
        if (Keyboard.isKeyDown(KEY_ASCEND))
        {
            motionY += thrust.y;
        }
        else if (Keyboard.isKeyDown(KEY_DESCEND))
        {
            motionY -= thrust.y;
        }
    }
    
    @Override
    protected void fall(float f)
    {
        // prevent damage from falling
        
        /*
        if(riddenByEntity != null)
        {
            riddenByEntity.fall(f);
        }
        */
    }

    // enter upon contact
    /*
    public void onCollideWithPlayer(EntityPlayer player)
    {
        System.out.println("onCollideWithPlayer");
        
        if(riddenByEntity == null)
        {
            interact(player);
        }
    }
    */
}
