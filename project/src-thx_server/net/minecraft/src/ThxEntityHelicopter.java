package net.minecraft.src;

import java.util.List;


public class ThxEntityHelicopter extends ThxEntity implements IPacketSource
{

    static boolean ENABLE_DRONE_MODE = ThxConfig.getBoolProperty("enable_drone_mode");
        
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

    final float THROTTLE_MIN = -.03f;
    final float THROTTLE_MAX = .07f;
    final float THROTTLE_INC = .005f;

    // Vectors for repeated calculations
    Vector3 thrust = new Vector3();
    Vector3 velocity = new Vector3();
    
    // amount of vehicle motion to transfer upon projectile launch
    final float MOMENTUM = .2f;

    // total update count
    float timeSinceAttacked;
    float timeSinceCollided;
    float smokeDelay;
    
    
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
        
        NET_PACKET_TYPE = 75;
        
        log("C1 - ThxEntityHelicopter() with world: " + world.getWorldInfo());
    }

    /*
    public ThxEntityHelicopter(World world, double x, double y, double z)
    {
        this(world);
        //setPosition(x, y + yOffset, z);
        setPosition(x, y, z);
        
        log("Is this used? C2 - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }
    */

    // constructor for item-use spawn
    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        
        //setLocationAndAngles(x, y, z, yaw, 0f);
        //setPositionAndRotation(x, y, z, yaw, 0f);
        setPosition(x, y, z);
        setRotation(yaw, 0f);
        
        log("C3 - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ + ", yaw: " + yaw);
    }

    public Entity getPilot()
    {
        //return (EntityPlayer) riddenByEntity;
        return riddenByEntity;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate(); // ThxEntity.onUpdate will apply latest client packet if there is one
        
        // create smoke to indicate damage
        smokeDelay -= deltaTime;
        if (smokeDelay < 0f)
        {
            smokeDelay = 1f - damage / MAX_HEALTH;
            if (smokeDelay > .4f) smokeDelay = .4f;

            for (int i = (int) (10f * damage / MAX_HEALTH); i > 0; i--)
            {
                /*
                if (i % 2 == 0) worldObj.spawnParticle("smoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
                else worldObj.spawnParticle("largesmoke", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
                */
                
                if (i > 6) break;
            }
            //if (damage / MAX_HEALTH > .75f) worldObj.spawnParticle("flame", posX -.5f + Math.random(), posY -.5f + Math.random(), posZ -.5f + Math.random(), 0.0, 0.0, 0.0);
        }
        
        if (riddenByEntity != null)
        {
            // entity updates will come from client for player pilot
            
            if (riddenByEntity.isDead) riddenByEntity.mountEntity(this);
            
            // lock to zero for server since position is updated by client
            motionX = 0.0;
            motionY = 0.0;
            motionZ = 0.0;
            
	        moveEntity(motionX, motionY, motionZ);

	        //handleCollisions();
	        
	        // fire weapons and clear flags
            if (fire1 > 0)
            {
                fire1 = 0;
                fireRocket();
            }
            if (fire2 > 0)
            {
                fire2 = 0;
                fireMissile();
            }
	        
            return;
        }
        
        // for auto-heal: 
        if (damage > 0f) damage -= deltaTime; // heal rate: 1 pt / sec

        // decrement cooldown timers
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
        missileDelay -= deltaTime;
        rocketDelay  -= deltaTime;
        rocketReload -= deltaTime;
        
        if (targetHelicopter == null) // empty helicopter, no ai, just fall
        {
            if (onGround || isInWater())
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
	        moveEntity(motionX, motionY, motionZ);
	        handleCollisions();
	        
            return;
        }
        
        // all below if for ai or empty
        // all below if for ai or empty
        // all below if for ai or empty
        // all below if for ai or empty
        
        if (onGround) // very slow on ground
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral

            // double apply friction when on ground
            motionX *= FRICTION;
            motionY = 0.0;
            motionZ *= FRICTION;
        }
        else if (isInWater())
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral
                
            motionX *= .7;
            motionY *= .7;
            motionZ *= .7;
            
            // float up
            motionY += 0.02;
        }
            
        float thd = 0f; // thd is targetHelicopter distance
        deltaPosToTarget.set((float)(targetHelicopter.posX - posX), 0f, (float)(targetHelicopter.posZ - posZ));
        thd = deltaPosToTarget.length();
            
        // FIRE ROCKET
        if (!isTargetHelicopterFriendly 
                && thd < 30f 
                && thd > 5f 
                && Math.abs(targetHelicopter.posY - posY) < 2f 
                && rocketDelay < 0f 
                && rocketReload < 0f)
        {
            fireRocket();
        }

        // FIRE MISSILE
        if (missileDelay < 0f 
                && targetHelicopter != null 
                && !isTargetHelicopterFriendly 
                && Math.abs(targetHelicopter.posY - posY) < 2f) 
             // && damage > MAX_HEALTH / 2f) // ai fire missle when low health
        {
            fireMissile();
        }

        // START YAW
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
        rotationYaw %= 360f;

        if (thd > 10f)
        {
            rotationPitch = 45f * (thd - 10f) / 20f;
        }
		else 
	    {
		   if (!isTargetHelicopterFriendly) rotationPitch =  (1 - (thd / 10f)) * -20f;// -20f;
	    }
        rotationPitchSpeed = 0f;

        if (!isTargetHelicopterFriendly)
        {
            // roll toward or away?
        }
        if (thd > 10f)
        {
            // seek target
        }
            
        // allow direct control of nearby ai friendly helicopters
        if (isTargetHelicopterFriendly && thd < 10f)
        {
            rotationPitch = targetHelicopter.rotationPitch;
            rotationPitchSpeed = targetHelicopter.rotationPitchSpeed;
                
            rotationRoll = targetHelicopter.rotationRoll;
            rotationRollSpeed = targetHelicopter.rotationRollSpeed;
        }


        // collective (throttle) control
        // default space, increase throttle
            
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
            
        // now calculate thrust and velocity based on yaw, pitch, roll, throttle
        // now calculate thrust and velocity based on yaw, pitch, roll, throttle
        // now calculate thrust and velocity based on yaw, pitch, roll, throttle
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
        
        // move in all cases
        moveEntity(motionX, motionY, motionZ);

        handleCollisions();
    }
    
    private void handleCollisions()
    {
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
	            
                takeDamage((float) velSq * 100f); // crash damage based on velocity
                
	            motionX *= .7;
	            motionY *= .7;
	            motionZ *= .7;
	        }
            //isCollidedHorizontally = false;
            //isCollidedVertically = false;
        }

    }
    
    private void takeDamage(float damage)
    {
        damage += damage;
                
        /*
        if (riddenByEntity != null) // this is the player's helicopter, so show damage msg
        {
            Minecraft minecraft = ModLoader.getMinecraftInstance();
            minecraft.ingameGUI.addChatMessage("Damage: " + (int) (damage * 100 / MAX_HEALTH) + "%");
        }
        */
        
        if (damage > MAX_HEALTH && !worldObj.isRemote) // helicopter destroyed!
        {
            // show message if not player helicopter
            if (riddenByEntity != null)
            {
                riddenByEntity.mountEntity(this);
            }
            
            boolean flaming = true;
            worldObj.newExplosion(this, posX, posY, posZ, 2.3f, flaming);
            
            // TEST drop item if helicopter is destroyed by crashes or attacks
	        dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0); // will it be destroy or launched if placed after explosion?

            setEntityDead();
            
		    /*
	        spawnParticles: // does server spawn particles?
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
		    */
        }
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int i)
    {
        log("attackEntityFrom called");

        if (timeSinceAttacked > 0f || isDead) return false;
        
        if (damageSource == null) return false; // when is this the case?
        
        Entity attackingEntity = damageSource.getEntity();
      
        // 1 hit to change entity back to item
        testingItemDrop:
        {
	        if (riddenByEntity == null)
	        {
		        dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
		        setEntityDead();
		        return true;
	        }
        }

        if (attackingEntity == null) return false; // when is this the case?
        if (attackingEntity.equals(this)) return false; // ignore damage from self
        if (attackingEntity.equals(riddenByEntity)) return false; // ignore damage from pilot
	        
        log("attacked by entity: " + attackingEntity);
        
        // take damage sound
        worldObj.playSoundAtEntity(this, "random.bowhit", 1f, 1f);
        
        // activate AI for empty helicopter hit by another helicopter
        if (riddenByEntity == null && attackingEntity instanceof ThxEntityHelicopter)
        {
            if (targetHelicopter == null)
            {
	            targetHelicopter = (ThxEntityHelicopter) attackingEntity;
                    
	            // friendly at first
                isTargetHelicopterFriendly = true;
                    
	            worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f);
            }
            else if (attackingEntity == targetHelicopter && isTargetHelicopterFriendly)
            {
                isTargetHelicopterFriendly = false;
                
                missileDelay = 10f; // initial missile delay
                rocketDelay  =  5f; // initial rocket delay
            }
        }
               
        takeDamage((float) i * 3f);
        
        timeSinceAttacked = .5f; // sec delay before this entity can be attacked again
        
        setBeenAttacked(); // this will cause Entity.velocityChanged to be true, so additional Packet28
        
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
        log("interact called with player " + player.entityId);
        
        if (player.equals(riddenByEntity))
        {
            if (onGround || isCollidedVertically)
            {
                log("Landed " + this);
            }
            else 
            {
                log("Exited without landing, entering local drone mode");
                
                //enable_drone_mode = true; // fly helicopter remotely
                //player.ridingEntity = null; // allow normal character movement
            }
            pilotExit();
            return true;
        }
        
        if (riddenByEntity != null)
        {
            // already ridden by some other entity
            return false;
        }
        
        if (player.ridingEntity != null) 
        {
            // player is already riding some other entity
            return false;
        }
        
        // new pilot boarding
        if (!worldObj.isRemote)
        {
	        log("interact() calling mountEntity on player " + player.entityId);
	        player.mountEntity(this);
        }
        
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
        
        Entity pilot = getPilot();
        if (pilot == null) return;

        // this will tell the default impl in Entity.updateRidden()
        // that no adjustment need be made to the pilot's yaw or pitch
        // as a direct result of riding this helicopter entity.
        // rather, we let the player rotate the pilot and the helicopter follows
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        
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
        log("pilotExit()");

        log("pilotExit called for pilot " + pilot + " " + getPilotId());
        
        if (pilot == null) return;
        
        //model.visible = true; // hard to find otherwise!
        
        // clear pitch speed to prevent judder
        rotationPitchSpeed = 0f;
        
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
        
        log("pilotExit() calling mountEntity on player " + pilot);
        pilot.mountEntity(this); // riddenByEntity is now null
    }
 
    static class Keyboard // no-op replacement for client-side org.lwjgl.input.Keyboard
    {
	    public static boolean isKeyDown(int key)
	    {
	        return false;
	    }
    }
    
    private void fireRocket()
    {
        rocketCount++;
        rocketDelay = ROCKET_DELAY;
                
        float leftRightAmount = .6f;
        float leftRight = (rocketCount % 2 == 0) ? leftRightAmount  : -leftRightAmount;
                
        // starting position of rocket relative to helicopter, out in front quite a bit to avoid collision
        float offsetX = side.x * leftRight + fwd.x * 1f;
        float offsetY = side.y * leftRight + fwd.y * 1f;
        float offsetZ = side.z * leftRight + fwd.z * 1f;
                    
        float yaw = rotationYaw;
        float pitch = rotationPitch + 10f; // slight downward from helicopter pitch
                
        ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        worldObj.spawnEntityInWorld(newRocket);
                
        if (rocketCount == FULL_ROCKET_COUNT)
        {
            // must reload before next volley
            rocketReload = ROCKET_RELOAD_DELAY;
            rocketCount = 0;
        }
    }
    
    private void fireMissile()
    {
        float offX = fwd.x * 2f;
        float offY = fwd.y * 2f;
        float offZ = fwd.z * 2f;

        float yaw = rotationYaw;
        float pitch = rotationPitch;
                
        ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        newMissile.targetHelicopter = targetHelicopter;
        worldObj.spawnEntityInWorld(newMissile);
    }
}
