package net.minecraft.src;

import java.util.List;

public abstract class ThxEntityHelicopterBase extends ThxEntity implements IClientDriven
{
    int rocketCount;
    
    float MAX_HEALTH = 160f;

    float MAX_ACCEL    = 0.2000f;
    float GRAVITY      = 0.2005f;
    float MAX_VELOCITY = 0.30f;
    float FRICTION = 0.98f;

    float MAX_PITCH = 60.00f;
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
    
    float missileDelay;
    final float MISSILE_DELAY = 6f;

    float rocketDelay;
    //final float ROCKET_DELAY = .12f;
    final float ROCKET_DELAY = .001f; // now controlled by interact rate //.20f;
    final int FULL_ROCKET_COUNT = 12;
    float rocketReload;
    final float ROCKET_RELOAD_DELAY = 3f;
    
    float autoLevelDelay;
    
    Vector3 thrust = new Vector3();
    Vector3 velocity = new Vector3();
    
    // enemy AI helicopter or friend?
    public ThxEntityHelicopter targetHelicopter;
    public boolean isTargetHelicopterFriendly;
    Vector3 deltaPosToTarget = new Vector3();
	    
    public ThxEntityHelicopterBase(World world)
    {
        super(world);
        
        setSize(1.8f, 2f);

        yOffset = .8f;
        
        NET_PACKET_TYPE = 75;
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
                
        // decrement cooldown timers
        missileDelay -= deltaTime;
        rocketDelay  -= deltaTime;
        
        if (rocketReload > 0f) rocketReload -= deltaTime;
        if (rocketReload < 0f)
        {
            // play sound to indicate rocket reload is complete
            rocketReload = 0f; // prevent completion sound from playing repeatedly
            worldObj.playSoundAtEntity(this, "random.click",  .4f, .7f); // volume, pitch
        }
        
        if (riddenByEntity != null)
        {
            onUpdatePilot(); // player pilot
        }
        else if (targetHelicopter != null)
        {
            onUpdateDrone(); // drone ai
        }
        else
        {
            onUpdateVacant(); // empty
        }
            
        // convert pitch, roll, and throttle into thrust and call moveEntity
        updateMotion(altitudeLock);
        
        if (handleCollisions())
        {
	        helper.addChatMessage(this + " - Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
        }
        
        if (damage > MAX_HEALTH && !worldObj.isRemote) // helicopter destroyed!
        {
            float power = 2.3f;
            boolean flaming = true;
            worldObj.newExplosion(this, posX, posY, posZ, power, flaming);
            
            if (riddenByEntity != null) riddenByEntity.mountEntity(this); // unmount
            
            dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
            
            setEntityDead();
        }
        
        // auto-heal: 
        if (damage > 0f) damage -= deltaTime; // heal rate: 1 pt / sec
    }
    
    @Override
    public double getMountedYOffset()
    {
        return -.25;
    }
    
    void fireRocket()
    {
        if (rocketDelay > 0f) return;
        if (rocketReload > 0f) return;
        
        rocketDelay = ROCKET_DELAY;
                
        rocketCount++;
        
        float leftRightAmount = .6f;
        float leftRight = (rocketCount % 2 == 0) ? leftRightAmount  : -leftRightAmount;
                
        // starting position of rocket relative to helicopter, out in front quite a bit to avoid collision
        float offsetX = (side.x * leftRight) + (fwd.x * 2.5f) + (up.x * -.5f);
        float offsetY = (side.y * leftRight) + (fwd.y * 2.5f) + (up.y * -.5f);
        float offsetZ = (side.z * leftRight) + (fwd.z * 2.5f) + (up.z * -.5f);
                    
        float yaw = rotationYaw;
        float pitch = rotationPitch + 5f;
                
        if (!worldObj.isRemote)
        {
	        ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
	        newRocket.owner = riddenByEntity != null ? riddenByEntity : this;
	        worldObj.spawnEntityInWorld(newRocket);
        }
        
        if (rocketCount == FULL_ROCKET_COUNT)
        {
            reload();
        }
    }
    
    void reload()
    {
        rocketReload = ROCKET_RELOAD_DELAY;
        rocketCount = 0;
    }
    
    void fireMissile()
    {
        log("firing missile");
        
        if (missileDelay > 0f) return;
        missileDelay = MISSILE_DELAY;
                
        float offX = (fwd.x * 2.5f) + (up.x * -.5f);
        float offY = (fwd.y * 2.5f) + (up.y * -.5f);
        float offZ = (fwd.z * 2.5f) + (up.z * -.5f);

        // aim with cursor if pilot
        //float yaw = riddenByEntity != null ? riddenByEntity.rotationYaw : rotationYaw;
        //float pitch = riddenByEntity != null ? riddenByEntity.rotationPitch : rotationPitch;
        float yaw = rotationYaw;
        float pitch = rotationPitch + 5f;
                
        if (!worldObj.isRemote)
        {
	        ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
	        newMissile.owner = riddenByEntity != null ? riddenByEntity : this;
	        worldObj.spawnEntityInWorld(newMissile);
        }
    }

    void createMap()
    {
        //if (worldObj.isRemote) return;
        log("creating map");
        
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
    
    void updateMotion(boolean altitudeLock)
    {
        // now calculate thrust and velocity based on yaw, pitch, roll, throttle
    
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
            thrust.y += -fwd.y * accel * .3f;
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

        // start with current velocity
        velocity.set((float)motionX, (float)motionY, (float)motionZ);

        // friction, very little!
        velocity.scale(FRICTION);

        // scale thrust by current throttle and delta time
        //thrust.normalize().scale(MAX_ACCEL * (1f + throttle) * deltaTime / .05f);
        thrust.normalize().scale(MAX_ACCEL * (1f + throttle) * deltaTime / .05f);

        // apply the thrust
        Vector3.add(velocity, thrust, velocity);

        // gravity is always straight down
        //if (!inWater && !onGround) velocity.y -= GRAVITY * deltaTime / .05f;
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
            
        if (altitudeLock)
        {
            motionY *= .9;
            if (motionY < .00001) motionY = .0;
        }
        
        moveEntity(motionX, motionY, motionZ);
    }
    
    /**
     * @return boolean if damage was taken as a result of collision
     */
    boolean handleCollisions()
    {
        boolean isCollidedWithEntity = false;
        
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
        
        // crash, take damage and slow down
        if (isCollidedHorizontally || isCollidedVertically || isCollidedWithEntity)
        {
	        double velSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
            
	        if (velSq > .005 && timeSinceCollided  < 0f && !onGround)
	        {
	            log("crash velSq: " + velSq);
                
                timeSinceCollided = .5f; // sec delay before another collision possible
	            
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
        // but might be good to have some "free motion" control within limited arc
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
            setEntityDead();
            if (this instanceof ThxEntityHelicopter && !worldObj.isRemote)
            {
                dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
            }
        }
    }
    
    abstract void onUpdatePilot();
    
    void onUpdateDrone()
    {
        if (targetHelicopter == null) return;
        
        if (targetHelicopter.isDead)
        {
            targetHelicopter = null;
            return;
        }
        
        float thd = 0f; // thd is targetHelicopter distance
        deltaPosToTarget.set((float)(targetHelicopter.posX - posX), 0f, (float)(targetHelicopter.posZ - posZ));
        thd = deltaPosToTarget.length();
            
        if (!isTargetHelicopterFriendly 
                && thd < 20f 
                && thd > 5f 
                && Math.abs(targetHelicopter.posY - posY) < 2f 
           )
        {
            // fire rocket
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
        
        // PITCH
        if (thd > 10f)
        {
            rotationPitch = 45f * (thd - 10f) / 20f;
        }
		else 
	    {
		   if (!isTargetHelicopterFriendly) rotationPitch =  (1 - (thd / 10f)) * -20f;// -20f;
	    }
        rotationPitchSpeed = 0f;
        
        
        if (isTargetHelicopterFriendly && thd < 10f)
        {
            rotationPitch = targetHelicopter.rotationPitch;
            rotationPitchSpeed = targetHelicopter.rotationPitchSpeed;
                
            rotationRoll = targetHelicopter.rotationRoll;
            rotationRollSpeed = targetHelicopter.rotationRollSpeed;
        }

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
    
    void onUpdateVacant()
    {
        isActive = false; 
        
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

            // double apply friction when on ground
            motionX *= FRICTION;
            motionY = 0.0;
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
    
    void attackedByThxEntity(ThxEntity entity)
    {
        if (entity.equals(targetHelicopter))
        {
            isTargetHelicopterFriendly = false;
        }
        else if (entity instanceof ThxEntityHelicopter)
        {
            targetHelicopter = (ThxEntityHelicopter) entity;
            isTargetHelicopterFriendly = true;
        }
    }
}

