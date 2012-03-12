package net.minecraft.src;

import java.util.List;

public abstract class ThxEntityHelicopterBase extends ThxEntity
{
    int rocketCount;
    
    float MAX_HEALTH = 160f;

    float MAX_ACCEL    = 0.2000f;
    //float GRAVITY      = 0.201f;
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

    float smokeDelay;
    
    boolean altitudeLock;
    float altitudeLockToggleDelay;
    
    float hudModeToggleDelay;
    
    float lookPitchToggleDelay;
    boolean lookPitch = false;
    float lookPitchZeroLevel;
    
    float createMapDelay;
    
    int prevViewMode = 2;
    
    float missileDelay;
    final float MISSILE_DELAY = 6f;

    float rocketDelay;
    //final float ROCKET_DELAY = .12f;
    final float ROCKET_DELAY = .20f;
    final int FULL_ROCKET_COUNT = 12;
    float rocketReload;
    final float ROCKET_RELOAD_DELAY = 3f;
    
    float autoLevelDelay;
    float exitDelay;
    
    public ThxEntityHelicopterBase(World world)
    {
        super(world);
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
                
        if (damage > MAX_HEALTH && !worldObj.isRemote) // helicopter destroyed!
        {
            if (riddenByEntity != null) riddenByEntity.mountEntity(this);
            
            boolean flaming = true;
            worldObj.newExplosion(this, posX, posY, posZ, 2.3f, flaming);
            
            dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
            
            setEntityDead();
        }
    }
    
    @Override
    public double getMountedYOffset()
    {
        return -.25;
    }
    
    void fireRocket()
    {
        rocketCount++;
                
        float leftRightAmount = .6f;
        float leftRight = (rocketCount % 2 == 0) ? leftRightAmount  : -leftRightAmount;
                
        // starting position of rocket relative to helicopter, out in front quite a bit to avoid collision
        float offsetX = (side.x * leftRight) + (fwd.x * 2.5f) + (up.x * -.5f);
        float offsetY = (side.y * leftRight) + (fwd.y * 2.5f) + (up.y * -.5f);
        float offsetZ = (side.z * leftRight) + (fwd.z * 2.5f) + (up.z * -.5f);
                    
        float yaw = rotationYaw;
        float pitch = rotationPitch + 5f;
                
        ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        newRocket.owner = riddenByEntity != null ? riddenByEntity : this;
        worldObj.spawnEntityInWorld(newRocket);
    }
    
    void fireMissile()
    {
        log("firing missile");
        
        float offX = (fwd.x * 2.5f) + (up.x * -.5f);
        float offY = (fwd.y * 2.5f) + (up.y * -.5f);
        float offZ = (fwd.z * 2.5f) + (up.z * -.5f);

        // aim with cursor if pilot
        //float yaw = riddenByEntity != null ? riddenByEntity.rotationYaw : rotationYaw;
        //float pitch = riddenByEntity != null ? riddenByEntity.rotationPitch : rotationPitch;
        float yaw = rotationYaw;
        float pitch = rotationPitch + 5f;
                
        ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        newMissile.owner = riddenByEntity != null ? riddenByEntity : this;
        worldObj.spawnEntityInWorld(newMissile);
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
        
	    // Vectors for repeated calculations
	    Vector3 thrust = new Vector3();
	    Vector3 velocity = new Vector3();
    
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
            motionY *= .6;
            if (motionY < .000001) motionY = .0;
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
                
                timeSinceCollided = 1f; // sec delay before another collision possible
	            
                float crashDamage = (float) velSq * 1000f; 
                // velSq seems to range between .010 and .080, 10 to 80 damage, so limit:
                if (crashDamage < 3f) crashDamage = 3f; 
                if (crashDamage > 49f) crashDamage = 49f; 
                
	            log("crash damage: " + crashDamage);
                takeDamage(crashDamage); // crash damage based on velocity
                
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
}
