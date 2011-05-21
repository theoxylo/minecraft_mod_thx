package net.minecraft.src;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

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
    static int KEY_ENTER_EXIT;
    static boolean ENABLE_LOOK_YAW;
    static boolean ENABLE_LOOK_PITCH;
    static boolean ENABLE_DRONE_MODE;
    static boolean ENABLE_PILOT_AIM;
    static boolean ENABLE_AUTO_LEVEL;

    final int MAX_HEALTH = 50;

    // handling properties
    final double MAX_ACCEL = 0.299; // very slowly sink when neutral throttle
    final double MAX_VELOCITY = 0.40;
    final double GRAVITY = 0.30;
    final float TURN_SPEED_DEG = 2.00f;
    final double FRICTION = 0.98;

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
    Vec3D thrust;
    Vec3D velocity;

    // total update count
    int _damage = 0;
    
    int _missileDelay = 0;
    final int MISSILE_RELOAD = 90;

    // public float prevRotationRoll = 0f;
    // public float rotationRoll = 0f;

    double dronePilotPosX = 0.0;
    double dronePilotPosY = 0.0;
    double dronePilotPosZ = 0.0;

    public ThxEntityMissile missile = null;

    // constructors:
    // constructors:
    // constructors:

    public ThxEntityHelicopter(World world)
    {
        super(world);

        // new Exception("EntityThxHelicopter call stack:").printStackTrace();

        renderModel = new ThxModelHelicopter();
        renderTexture = "/thx/helicopter.png";

        setSize(1.5F, 0.6F);

        yOffset = .8f;

        thrust = Vec3D.createVector(.0, .0, .0);
        velocity = Vec3D.createVector(.0, .0, .0);

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

        if (_damage > 0) _damage--;
        if (_missileDelay > 0) _missileDelay--;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevMotionX = motionX;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;

        EntityPlayer pilot = getPilot();

        Minecraft minecraft = ModLoader.getMinecraftInstance();
        if (Keyboard.isKeyDown(KEY_ENTER_EXIT))
        {
            // minecraft.displayInGameMenu();
        }

        // if (ModLoader.isGUIOpen(null) && minecraft.thePlayer.ridingEntity ==
        // this)
        if (minecraft.thePlayer.ridingEntity == this)
        {
            // if (pilot.isDead) riddenByEntity = null;

            if (onGround) // very slow on ground
            {
                if (Math.abs(rotationPitch) > 1.0f)
                    rotationPitch *= .70f;
                if (Math.abs(rotationRoll) > 1.0f)
                    rotationRoll *= .40f; // very little lateral

                // double apply friction when on ground
                motionX *= FRICTION;
                motionY = 0.0;
                motionZ *= FRICTION;
            }

            if (Keyboard.isKeyDown(KEY_ENTER_EXIT))
            {
                interact(pilot); // enter/exit vehicle
            }

            if (Keyboard.isKeyDown(KEY_FIRE_MISSILE) && _missileDelay == 0)
            {
                _missileDelay = MISSILE_RELOAD;

                if (ENABLE_PILOT_AIM && !ENABLE_DRONE_MODE)
                {
                    // use pilot look to aim
                    
                    ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX, posY, posZ, motionX * .1, motionY * .1, motionZ * .1, pilot.rotationYaw, pilot.rotationPitch);
                    worldObj.entityJoinedWorld(newMissile);
                }
                else
                {
                    // use helicopter to aim
                    // helicopter yaw reference is perpendicular
                    
                    ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX, posY, posZ, motionX * .1, motionY * .1, motionZ * .1, rotationYaw + 90f, rotationPitch);
                    worldObj.entityJoinedWorld(newMissile);
                }
            }

            if (ENABLE_LOOK_YAW && !ENABLE_DRONE_MODE)
            {
                // input from look control (mouse or analog stick)
                float deltaYawDeg = rotationYaw + 90 - pilot.rotationYaw;

                while (deltaYawDeg > 180f)
                    deltaYawDeg -= 360f;
                while (deltaYawDeg < -180f)
                    deltaYawDeg += 360f;

                if (deltaYawDeg < -15f)
                {
                    rotationYaw += TURN_SPEED_DEG;
                    if (deltaYawDeg < -45f)
                        rotationYaw += TURN_SPEED_DEG * 2f;
                }

                if (deltaYawDeg > 15f)
                {
                    rotationYaw -= TURN_SPEED_DEG;
                    if (deltaYawDeg > 45f)
                        rotationYaw -= TURN_SPEED_DEG * 2f;
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
                if (rotationPitch > MAX_PITCH)
                    rotationPitch = MAX_PITCH;
                if (rotationPitch < -MAX_PITCH)
                    rotationPitch = -MAX_PITCH;
            }
            else
            // button pitch and roll
            {
                if (Keyboard.isKeyDown(KEY_FORWARD))
                {
                    // zero pitch is level, positive pitch is leaning forward
                    rotationPitch += PITCH_SPEED_DEG;
                    if (rotationPitch > MAX_PITCH)
                        rotationPitch = MAX_PITCH;
                }
                else if (Keyboard.isKeyDown(KEY_BACK))
                {
                    rotationPitch -= PITCH_SPEED_DEG;
                    if (rotationPitch < -MAX_PITCH)
                        rotationPitch = -MAX_PITCH;
                }
                else
                {
                    if (ENABLE_AUTO_LEVEL)
                        rotationPitch *= PITCH_RETURN;
                }
            }

            if (Keyboard.isKeyDown(KEY_LEFT))
            {
                rotationRoll += ROLL_SPEED_DEG;
                if (rotationRoll > MAX_ROLL)
                    rotationRoll = MAX_ROLL;
            }
            else if (Keyboard.isKeyDown(KEY_RIGHT))
            {
                rotationRoll -= ROLL_SPEED_DEG;
                if (rotationRoll < -MAX_ROLL)
                    rotationRoll = -MAX_ROLL;
            }
            else
            {
                if (ENABLE_AUTO_LEVEL)
                    rotationRoll *= ROLL_RETURN;
            }

            // collective (throttle) control
            if (Keyboard.isKeyDown(KEY_ASCEND)) // space, increase throttle
            {
                if (throttle < THROTTLE_MAX)
                    throttle += THROTTLE_INC;
                if (throttle > THROTTLE_MAX)
                    throttle = THROTTLE_MAX;
                // throttle = THROTTLE_MAX;
            }
            else if (Keyboard.isKeyDown(KEY_DESCEND))
            {
                if (throttle > THROTTLE_MIN)
                    throttle -= THROTTLE_INC;
                if (throttle < THROTTLE_MIN)
                    throttle = THROTTLE_MIN;
                // throttle = THROTTLE_MIN;
            }
            else
            {
                // zero throttle
                throttle *= .5; // quickly zero throttle
            }
            // set rotor speed on model
            // if (throttle > .01f) ((ThxModelHelicopter)renderModel).rotorSpeed
            // = .33f + throttlePower();

            // use non-unit heading vector with 2D XZ yaw and Y based on pitch
            float yaw = rotationYaw * RAD_PER_DEG;
            float pitch = rotationPitch * RAD_PER_DEG;
            float roll = rotationRoll * RAD_PER_DEG;

            // movement
            thrust.xCoord = .0;
            thrust.yCoord = .0;
            thrust.zCoord = .0;

            ascendDescendLift:
            {
                // as pitch increases, lift decreases by fall-off function
                // pitch ranges from around -60 (back) to 60 degrees (forward)
                // pitch of zero is level for full upward thrust
                thrust.yCoord = MathHelper.cos(pitch);
            }

            forwardBack:
            {
                // as pitch increases, forward-back motion increases
                // but sin function was too touchy so using 1-cos
                // double forward = (double) -MathHelper.sin(pitch);
                // double forward = 1.0 - (double) MathHelper.cos(pitch);
                // if (pitch > .0) forward *= -1;

                double forward = 1.0 - thrust.yCoord;
                if (pitch > .0)
                    forward *= -1;

                thrust.xCoord = MathHelper.cos(yaw) * forward;
                thrust.zCoord = MathHelper.sin(yaw) * forward;
            }

            strafeLeftRight:
            {
                // double strafe = (double) -MathHelper.sin(roll);
                double strafe = 1.0 - (double) MathHelper.cos(roll);
                if (roll > .0)
                    strafe *= -1;

                thrust.xCoord += MathHelper.sin(yaw) * strafe;
                thrust.zCoord += -MathHelper.cos(yaw) * strafe;
            }

            // start with current velocity
            velocity.xCoord = motionX;
            velocity.yCoord = motionY;
            velocity.zCoord = motionZ;

            // friction, very little!
            vectorScale(velocity, FRICTION);

            // scale thrust by current throttle
            vectorSetLength(thrust, MAX_ACCEL * (1.0 + throttle));

            // apply the thrust
            vectorAdd(velocity, thrust);

            // gravity is always straight down
            velocity.yCoord -= GRAVITY;

            // limit max velocity
            vectorLimit(velocity, MAX_VELOCITY);

            // apply velocity changes
            motionX = velocity.xCoord;
            motionY = velocity.yCoord;
            motionZ = velocity.zCoord;

            moveEntity(motionX, motionY, motionZ);
            /*
             * if (velocity.squareDistanceTo(.0, .0, .0) > .000001) {
             * moveEntity(motionX, motionY, motionZ); }
             */
        }
        else
        // no pilot -- slowly sink to the ground
        {
            ((ThxModelHelicopter) renderModel).rotorOn = 0;

            if (onGround)
            {
                // tend to stay put on ground
                motionY = 0.;
                motionX *= .7;
                motionZ *= .7;
            }
            else
            {
                // settle back to ground slowly if pilot bails
                motionX *= FRICTION;
                motionY -= GRAVITY * .16;
                motionZ *= FRICTION;
            }

            rotationPitch *= PITCH_RETURN;
            rotationRoll *= ROLL_RETURN;

            moveEntity(motionX, motionY, motionZ);
        }

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

        if (isCollidedHorizontally || isCollidedVertically && motionY > .005)
        {
            die();
        }
    }

    public void die()
    {
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

        if (isDead) return true;

        worldObj.playSoundAtEntity(this, "mob.ghast.fireball", 1.0f, 1.0f);

        _damage += i * 10;
        log ("current damage percent: " + (100f * (float)_damage / (float)MAX_HEALTH));
        
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

        KEY_ASCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("ascend"));
        KEY_DESCEND = Keyboard.getKeyIndex(ThxConfig.getProperty("descend"));
        KEY_FORWARD = Keyboard.getKeyIndex(ThxConfig.getProperty("forward"));
        KEY_BACK = Keyboard.getKeyIndex(ThxConfig.getProperty("back"));
        KEY_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("left"));
        KEY_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("right"));
        KEY_ROTATE_LEFT = Keyboard.getKeyIndex(ThxConfig.getProperty("rotate_left"));
        KEY_ROTATE_RIGHT = Keyboard.getKeyIndex(ThxConfig.getProperty("rotate_right"));
        KEY_FIRE_MISSILE = Keyboard.getKeyIndex(ThxConfig.getProperty("key_fire_missile"));
        KEY_ENTER_EXIT = Keyboard.getKeyIndex(ThxConfig.getProperty("key_enter_exit"));

        ENABLE_LOOK_YAW = ThxConfig.getBoolProperty("enable_look_yaw");
        ENABLE_LOOK_PITCH = ThxConfig.getBoolProperty("enable_look_pitch");
        ENABLE_DRONE_MODE = ThxConfig.getBoolProperty("enable_drone_mode");
        ENABLE_PILOT_AIM = ThxConfig.getBoolProperty("enable_pilot_aim");
        ENABLE_AUTO_LEVEL = ThxConfig.getBoolProperty("enable_auto_level");
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
        if (!worldObj.multiplayerWorld)
        {
            ((ThxModelHelicopter) renderModel).rotorOn = 1; // start at quarter
                                                            // speed
            player.mountEntity(this);
        }

        // if (riddenByEntity == null) player.mountEntity(this);
        return true;
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

    public void vectorAdd(Vec3D vec, Vec3D arg)
    {
        vec.xCoord += arg.xCoord;
        vec.yCoord += arg.yCoord;
        vec.zCoord += arg.zCoord;
    }

    public void vectorScale(Vec3D vec, double factor)
    {
        double lengthSq = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;

        // if (lengthSq < .0001 && factor < 1.0) return; // already very short

        vec.xCoord *= factor;
        vec.yCoord *= factor;
        vec.zCoord *= factor;
    }

    void vectorLimit(Vec3D vec, double max)
    {
        double lengthSq = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;

        // if (lengthSq < .0001) return; // already very short

        if (lengthSq > max * max)
        {
            double scale = max / Math.sqrt(lengthSq);
            vec.xCoord *= scale;
            vec.yCoord *= scale;
            vec.zCoord *= scale;
        }
    }

    void vectorSetLength(Vec3D vec, double newLength)
    {
        double lengthSq = vec.xCoord * vec.xCoord + vec.yCoord * vec.yCoord + vec.zCoord * vec.zCoord;

        // if (lengthSq < .0001) return; // already very short

        if (lengthSq > newLength * newLength)
        {
            double scale = newLength / Math.sqrt(lengthSq);
            vec.xCoord *= scale;
            vec.yCoord *= scale;
            vec.zCoord *= scale;
        }
    }
}
