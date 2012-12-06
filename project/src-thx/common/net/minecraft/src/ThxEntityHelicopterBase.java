package net.minecraft.src;

import java.util.ArrayList;
import java.util.List;

public abstract class ThxEntityHelicopterBase extends ThxEntity implements ThxClientDriven
{
    List followers = new ArrayList();
    
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
    public ThxEntityHelicopter targetHelicopter;
    public boolean isTargetHelicopterFriendly;
    public boolean isDroneArmed;
    Vector3 deltaPosToTarget = new Vector3();
	    
    ThxEntityMissile lastMissileFired;
    
    public ThxEntityHelicopterBase(World world)
    {
        super(world);
        
        setSize(1.8f, 2f);

        yOffset = .8f; // avoid getting stuck in ground upon spawn, but looks high
        //yOffset = .7f;
        
        helper = createHelper();
    }
    
    public ThxEntityHelicopterBase(World world, double x, double y, double z, float yaw)
    {
        this(world);
        setPositionAndRotation(x, y + yOffset, z, yaw, 0f);
    }
    
    @Override
    public int getPacketTypeId()
    {
        return 75;
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
            onUpdatePilot(); // some pilot, check for current player later
	        updateMotion(altitudeLock);
        }
        else if (targetHelicopter != null)
        {
            onUpdateDrone(); // drone ai
	        updateMotion(false);
        }
        else
        {
            onUpdateVacant(); // empty
	        updateMotion(false);
        }
        
        if (handleCollisions()) // true if collided with other entity or environment
        {
	        helper.addChatMessageToPilot("Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
        }
        
        if (damage > MAX_HEALTH) // && !worldObj.isRemote) // helicopter destroyed!
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
	        helper.addChatMessageToPilot("Damage: " + (int)(damage * 100 / MAX_HEALTH) + "%");
        }
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
    
    void reload()
    {
        rocketReload = ROCKET_RELOAD_DELAY;
        rocketCount = 0;
    }
    
    void fireMissile()
    {
        if (worldObj.isRemote) return;
        
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
        
        for (Object followerItem : followers)
        {
            ThxEntityHelicopter follower = (ThxEntityHelicopter) followerItem;
            //too much, rockets only? follower.fireMissile(); 
        }
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
            //mapdata.dimension = (byte)worldObj.worldProvider.worldType; // worldProvider removed in 1.3.2
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
        motionX = (double) velocity.x;
        motionY = (double) velocity.y;
        motionZ = (double) velocity.z;
            
        if (altitudeLock && riddenByEntity != null)
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
    
    abstract void onUpdatePilot();
    
    void onUpdateDrone()
    {
        if (targetHelicopter == null) return;
        
        if (targetHelicopter.isDead)
        {
            targetHelicopter = null;
            return;
        }
        
        /*
        //if (worldObj.isRemote) // TODO: how to detect server updates?
        {
            return; // applyUpdatePacket will do update for pos, vel, ypr
        }
        */
        
        float thd = 0f; // thd is targetHelicopter distance
        deltaPosToTarget.set((float)(targetHelicopter.posX - posX), 0f, (float)(targetHelicopter.posZ - posZ));
        thd = deltaPosToTarget.length();
            
        if (isTargetHelicopterFriendly)
        {
            // TODO: defend targer helicopter somehow?
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
        isActive = false; // skip sending update packet to client, use standard mc packets
        
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
    
    void attackedByThxEntity(ThxEntity attackingEntity)
    {
        if (riddenByEntity != null)
        {
            // for piloted helicopter, track attacker? could allow guided missiles, radar, nemesis
            //targetHelicopter = (ThxEntityHelicopter) attackingEntity;
            //return;
        }
        
        // activate/adjust AI for drone helicopter hit by another helicopter
        if (riddenByEntity == null && attackingEntity instanceof ThxEntityHelicopter)
        {
            log("attacked by " + attackingEntity + " with pilot: " + attackingEntity.riddenByEntity);
            
            if (targetHelicopter == null) // first attack by another helo, begin tracking as friendly
            {
	            targetHelicopter = (ThxEntityHelicopter) attackingEntity;
	            targetHelicopter.followers.add(this);
	            
                isTargetHelicopterFriendly = true;
	            isDroneArmed = false;
	            
                worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
                
                log("new targetHelicopter: " + targetHelicopter);
            }
            else if (targetHelicopter.equals(attackingEntity)) // already tracking
            {
		        if (isTargetHelicopterFriendly) // friendly fire
		        {
		            if (!isDroneArmed)
		            {
			            isDroneArmed = true; // now armed, still friendly, earn xp!
			            owner = targetHelicopter.owner;
			            
		                worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
		            }
		            else
		            {
			            isTargetHelicopterFriendly = false;
			            
			            targetHelicopter.followers.remove(this);
			            
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
                
                // prevTargetHelicopter = targetHelicopter; // TODO: switch back to original target if friendly?
                
	            targetHelicopter.followers.remove(this);
	            targetHelicopter = (ThxEntityHelicopter) attackingEntity;
	            
                isTargetHelicopterFriendly = false;
	            
                worldObj.playSoundAtEntity(this, "random.fuse", 1f, 1f); // activation sound
                
                log("new enemy targetHelicopter: " + targetHelicopter);
                
            }
        }
    }
    
    @Override
    void pilotExit()
    {
        if (worldObj.isRemote) 
        {
            log("pilotExit() call ignored on client, waiting for server packet with pilotId 0; see ThxEntityHelperClient.applyUpdatePacket(ThxEntityPacket250)");
            new Exception().printStackTrace();
            return;
        }
        
        log("pilotExit called for pilot entity :" + riddenByEntity);
        log("pilotExit called for pilot entity " + (riddenByEntity != null ? (riddenByEntity.toString() + riddenByEntity.entityId) : ("warning, no pilot to exit")));
        
        targetHelicopter = null;
        
        // not using mountEntity here
        //riddenByEntity.mountEntity(this); // riddenByEntity is now null // no more pilot

        if (riddenByEntity == null) return; // no pilot
        
        if (!equals(riddenByEntity.ridingEntity)) return; // pilot is somehow flying a different helicopter instance
        log ("this == riddenByEntity.ridingEntity; //" + (this == riddenByEntity.ridingEntity));
        
        // can't set these private fields as mountEntity does. problem?
        //riddenByEntity.entityRiderPitchDelta = 0.0D;
        //riddenByEntity.entityRiderYawDelta = 0.0D;

        riddenByEntity.ridingEntity = null;
        riddenByEntity = null;
        targetHelicopter = null;
        
        // clear rotation speed to prevent judder
        rotationYawSpeed = 0f;
        rotationPitchSpeed = 0f;
        rotationRollSpeed = 0f;
    }
    
    @Override
    public boolean interact(EntityPlayer player)
    {
        // super.interact returns true if player boards and becomes new pilot
        if (!super.interact(player)) return false;
        
        targetHelicopter = null; // inactivate ai
        
        return true;
    }

}

