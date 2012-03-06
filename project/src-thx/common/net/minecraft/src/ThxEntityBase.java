package net.minecraft.src;

import java.util.List;

public abstract class ThxEntityBase extends Entity
{
    boolean plog = true; // enable periodic logging for rapidly repeating events

    final float RAD_PER_DEG = 00.01745329f;
    final float PI = 03.14159265f;

    long prevTime;
    float deltaTime;

    double prevMotionX;
    double prevMotionY;
    double prevMotionZ;

    float rotationRoll;
    float prevRotationRoll;

    float yawRad;
    float pitchRad;
    float rollRad;

    float rotationYawSpeed;
    float rotationPitchSpeed;
    float rotationRollSpeed;

    Vector3 pos;
    Vector3 vel;
    Vector3 ypr;

    Vector3 fwd;
    Vector3 side;
    Vector3 up;
    
    int NET_PACKET_TYPE;
    float damage;
    int fire1;
    int fire2;

    Entity owner;
    
    int rocketCount;
    
    float MAX_HEALTH = 200f;

    float MAX_ACCEL    = 0.20f;
    float GRAVITY      = 0.201f;
    float MAX_VELOCITY = 0.30f;
    float FRICTION = 0.98f;

    float MAX_PITCH = 60.00f;
    float PITCH_SPEED_DEG = 40f;
    float PITCH_RETURN = 0.98f;

    float MAX_ROLL = 30.00f;
    float ROLL_SPEED_DEG = 40f;
    float ROLL_RETURN = 0.92f;

    float throttle = 0.0f;
    float THROTTLE_MIN = -.03f;
    float THROTTLE_MAX = .07f;
    float THROTTLE_INC = .005f;

    // Vectors for repeated calculations
    Vector3 thrust = new Vector3();
    Vector3 velocity = new Vector3();
    
    // amount of vehicle motion to transfer upon projectile launch
    float MOMENTUM = .2f;

    // total update count
    float timeSinceAttacked;
    float timeSinceCollided;
    
    public ThxEntityBase(World world)
    {
        super(world);

        log(world.isRemote ? "Created new MP entity" : "Created new SP entity");

        preventEntitySpawning = true;

        // vectors relative to entity orientation
        fwd = new Vector3();
        side = new Vector3();
        up = new Vector3();

        pos = new Vector3();
        vel = new Vector3();
        ypr = new Vector3();

        prevTime = System.nanoTime();
    }

    @Override
    public void onUpdate()
    {
        ticksExisted++;
        plog(String.format("start onUpdate, pilot %d [posX: %5.2f, posY: %5.2f, posZ: %5.2f, yaw: %5.2f]", getPilotId(), posX, posY, posZ, rotationYaw));
        
        long time = System.nanoTime();
        deltaTime = ((float) (time - prevTime)) / 1000000000f; // convert to sec
        prevTime = time;

        lastTickPosX = prevPosX = posX;
        lastTickPosY = prevPosY = posY;
        lastTickPosZ = prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        prevRotationRoll = rotationRoll;
        
        inWater = isInWater();
        
        // decrement cooldown timers
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
    }
    
    public int getPilotId()
    {
        return riddenByEntity != null ? riddenByEntity.entityId : 0;
    }
    
    public boolean isInWater()
    {
        // check for contact with water
        return worldObj.isAABBInMaterial(boundingBox.expand(.0, -.4, .0), Material.water);
    }
    
    /*
     * Normalize all rotations to -180 to +180 degrees (typically only yaw is * affected)
     */
    public void updateRotation()
    {
        rotationYaw %= 360f;
        if (rotationYaw > 180f) rotationYaw -= 360f;
        else if (rotationYaw < -180f) rotationYaw += 360f;
        yawRad = rotationYaw * RAD_PER_DEG;

        rotationPitch %= 360f;
        if (rotationPitch > 180f) rotationPitch -= 360f;
        else if (rotationPitch < -180f) rotationPitch += 360f;
        pitchRad = rotationPitch * RAD_PER_DEG;

        rotationRoll %= 360f;
        if (rotationRoll > 180f) rotationRoll -= 360f;
        else if (rotationRoll < -180f) rotationRoll += 360f;
        rollRad = rotationRoll * RAD_PER_DEG;
    }

    public void updateVectors()
    {
        float cosYaw   = (float) MathHelper.cos(-yawRad - PI);
        float sinYaw   = (float) MathHelper.sin(-yawRad - PI);
        float cosPitch = (float) MathHelper.cos(-pitchRad);
        float sinPitch = (float) MathHelper.sin(-pitchRad);
        float cosRoll  = (float) MathHelper.cos(-rollRad);
        float sinRoll  = (float) MathHelper.sin(-rollRad);

        fwd.x = -sinYaw * cosPitch;
        fwd.y = sinPitch;
        fwd.z = -cosYaw * cosPitch;

        side.x = cosYaw * cosRoll;
        side.y = -sinRoll;
        side.z = -sinYaw * cosRoll;

        // up.x = cosYaw * sinRoll - sinYaw * sinPitch * cosRoll;
        // up.y = cosPitch * cosRoll;
        // up.z = -sinYaw * sinRoll - sinPitch * cosRoll * cosYaw;
        Vector3.cross(side, fwd, up);

        pos.x = (float) posX;
        pos.y = (float) posY;
        pos.z = (float) posZ;

        vel.x = (float) motionX;
        vel.y = (float) motionY;
        vel.z = (float) motionZ;

        ypr.x = rotationYaw;
        ypr.y = rotationPitch;
        ypr.z = rotationRoll;
    }

    public Vector3 getForward()
    {
        float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
        float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
        float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
        float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
        return new Vector3(f3 * f5, f7, f1 * f5);
    }

    @Override
    public void setEntityDead()
    {
        log("setEntityDead called");
        super.setEntityDead();
    }

    @Override
    protected void fall(float f)
    {
        // no damage from falling, unlike super.fall
        log("fall() called with arg " + f);
    }
    
    /* abstract methods from Entity base class */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        //log("readEntityFromNBT called");
    }
    
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        //log("writeEntityToNBT called");
    }
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + entityId;
    }

    void log(String s)
    {
        mod_Thx.log(String.format("[%5d] ", ticksExisted) + this + ": " + s);
    }
    
    void plog(String s) // periodic log
    {
        if (plog && ticksExisted % 60 == 0)
        {
            log(s);
        }
    }
    
    public Packet230ModLoader getUpdatePacket()
    {
        Packet230ModLoader packet = new Packet230ModLoader();

        packet.modId = mod_Thx.instance.getId();
        packet.packetType = NET_PACKET_TYPE;

        packet.dataString = new String[] { "thx update packet for tick " + ticksExisted };

        packet.dataInt = new int[5];
        packet.dataInt[0] = entityId;
        packet.dataInt[1] = riddenByEntity != null ? riddenByEntity.entityId : 0;
        packet.dataInt[2] = fire1;
        packet.dataInt[3] = fire2;
        packet.dataInt[4] = owner != null ? owner.entityId : 0;
        
        // clear fire flags after use
        fire1 = 0;
        fire2 = 0;

        packet.dataFloat = new float[11];
        packet.dataFloat[0] = (float) posX;
        packet.dataFloat[1] = (float) posY;
        packet.dataFloat[2] = (float) posZ;
        packet.dataFloat[3] = rotationYaw;
        packet.dataFloat[4] = rotationPitch;
        packet.dataFloat[5] = rotationRoll;
        packet.dataFloat[6] = (float) motionX;
        packet.dataFloat[7] = (float) motionY;
        packet.dataFloat[8] = (float) motionZ;
        packet.dataFloat[9] = damage;
        packet.dataFloat[10] = throttle;
        
        return packet;
    }
    
    
    /**
     * 
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
                        
                if (entity instanceof ThxEntityBase)
                {
                    Entity otherOwner = ((ThxEntityBase) entity).owner;
                    if (equals(otherOwner)) 
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
    public boolean interact(EntityPlayer player)
    {
        log("interact called with player " + player.entityId);
        
        if (player.equals(riddenByEntity))
        {
            if (onGround || isCollidedVertically)
            {
                log("Landed and exited");
            }
            else 
            {
                log("Exited without landing");
            }
            pilotExit();
            return true;
        }
        
        if (player.ridingEntity != null) 
        {
            // player is already riding some other entity
            return false;
        }
        
        if (riddenByEntity != null)
        {
            // already ridden by some other entity, allow takeover if close
            if (getDistanceSqToEntity(player) < 2.0)
            {
                log("current pilot was ejected");
                pilotExit();
            }
            else
            {
	            return false;
            }
        }
        
        // new pilot boarding
        if (!worldObj.isRemote)
        {
	        log("interact() calling mountEntity on player " + player.entityId);
	        player.mountEntity(this);
        }
        
        player.rotationYaw = rotationYaw;
        
        log("interact() added pilot: " + player);
        return true;
    }

    void takeDamage(float amount)
    {
        damage += amount;
                
        if (damage > MAX_HEALTH && !worldObj.isRemote) // helicopter destroyed!
        {
            if (riddenByEntity != null) riddenByEntity.mountEntity(this);
            
            boolean flaming = true;
            worldObj.newExplosion(this, posX, posY, posZ, 2.3f, flaming);
            
            setEntityDead();
        }
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int i)
    {
        log("attackEntityFrom called with damageSource: " + damageSource);

        if (timeSinceAttacked > 0f || isDead || damageSource == null) return false;

        Entity attackingEntity = damageSource.getEntity();
        if (attackingEntity == null) return false; // when is this the case?
        if (attackingEntity.equals(this)) return false; // ignore damage from self
        if (attackingEntity.equals(riddenByEntity))
        {
            //riddenByEntity.mountEntity(this);
            pilotExit();
            setEntityDead();
            if (this instanceof ThxEntityHelicopter && !worldObj.isRemote)
            {
                // could add a dropItem() method for subclasses instead of instanceof
                dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0);
            }
            return false; // ignore damage from pilot
        }
        log("attacked by entity: " + attackingEntity);
        return true; // hit landed
    }
    
    protected void pilotExit()
    {
        log("pilotExit called for pilot " + riddenByEntity + " " + getPilotId());
    }
    
    @Override
    public boolean canBeCollidedWith()
    {
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
        log(this + " entityInit called");
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
    
    void fireRocket()
    {
        rocketCount++;
                
        float leftRightAmount = .6f;
        float leftRight = (rocketCount % 2 == 0) ? leftRightAmount  : -leftRightAmount;
                
        // starting position of rocket relative to helicopter, out in front quite a bit to avoid collision
        float offsetX = (side.x * leftRight) + (fwd.x * 1.9f) + (up.x * -.5f);
        float offsetY = (side.y * leftRight) + (fwd.y * 1.9f) + (up.y * -.5f);
        float offsetZ = (side.z * leftRight) + (fwd.z * 1.9f) + (up.z * -.5f);
                    
        float yaw = rotationYaw;
        float pitch = rotationPitch + 10f;
                
        ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        newRocket.owner = this;
        worldObj.spawnEntityInWorld(newRocket);
    }
    
    void fireMissile()
    {
        float offX = (fwd.x * 1.9f) + (up.x * -.5f);
        float offY = (fwd.y * 1.9f) + (up.y * -.5f);
        float offZ = (fwd.z * 1.9f) + (up.z * -.5f);

        // aim with cursor if pilot
        float yaw = riddenByEntity != null ? riddenByEntity.rotationYaw : rotationYaw;
        float pitch = riddenByEntity != null ? riddenByEntity.rotationPitch : rotationPitch;
                
        ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        newMissile.owner = this;
        worldObj.spawnEntityInWorld(newMissile);
    }

    void createMap()
    {
        if (worldObj.isRemote) return;
        
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
}