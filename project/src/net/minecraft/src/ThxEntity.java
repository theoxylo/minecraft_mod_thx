package net.minecraft.src;

import net.minecraft.client.Minecraft;

public abstract class ThxEntity extends Entity
{
    ThxEntity targetEntity; // e.g. helicopter for ai to follow/attack
    int prevTargetEntityId = 0;
    
    Minecraft minecraft;
    
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

    Vector3 pos = new Vector3(); // position
    Vector3 vel = new Vector3(); // velocity
    Vector3 acc = new Vector3(); // velocity
    Vector3 ypr = new Vector3(); // yaw, pitch, roll
    
    // vectors relative to entity orientation
    Vector3 fwd  = new Vector3(); // straight ahead
    Vector3 side = new Vector3(); // left side perp
    Vector3 up   = new Vector3(); // up
    
    ThxEntityHelper sidedHelper;
    
    abstract ThxEntityHelper createEntityHelper();
    
    public ThxEntityPacket250Data lastUpdatePacket;
    
    int cmd_reload;
    int cmd_create_item;
    int cmd_exit;
    int cmd_create_map;
    
    float damage;
    float throttle;
    
    Entity owner;
    
    // total update count
    float timeSinceAttacked;
    float timeSinceCollided;
    
    public ThxEntity(World world)
    {
        super(world);

        preventEntitySpawning = true; // keeps other entities from spawning on top of us

        prevTime = System.nanoTime();
        
        minecraft = ModLoader.getMinecraftInstance();
        
        log("ThxEntity() called with world: " + world);
        
        sidedHelper = createEntityHelper();
    }
    
    @Override
    protected void entityInit()
    {
        //log("entityInit() called");
        dataWatcher.addObject(22, new Integer(0)); // roll
        dataWatcher.addObject(23, new Integer(0)); // throttle
        dataWatcher.addObject(24, new Integer(0)); // targetHelicopter id
    }
    
    public void setWatched_Roll(float roll) // test: setting on server for vacant helicopters
    {
        assertServerSideOnly();
        dataWatcher.updateObject(22, Integer.valueOf((int) (roll * 1000f)));
    }
    
    public float getWatched_Roll() // test: and calling on client to sync current state 
    {
        assertClientSideOnly();
        return ((float) dataWatcher.getWatchableObjectInt(22)) / 1000f;
    }

    public void setWatched_Throttle(float f) // test: setting on server for drone helicopters
    {
        //plog("setWatched_Throttle: " + f);
        
        assertServerSideOnly();
        dataWatcher.updateObject(23, Integer.valueOf((int) (f * 1000f)));
    }
    
    public float getWatched_Throttle() // test: and calling on client to sync current state 
    {
        assertClientSideOnly();
        float f = ((float) dataWatcher.getWatchableObjectInt(23)) / 1000f;
        //plog("getWatched_Throttle: " + f);
        return f;
    }

    @Override
    public void onUpdate()
    {
        ticksExisted++;
        
        long time = System.nanoTime();
        deltaTime = ((float) (time - prevTime)) / 1000000000f; // convert to sec
        if (deltaTime > .05f) deltaTime = .05f; // 20 ticks per second
        prevTime = time;

        // 
        lastTickPosX = prevPosX = posX;
        lastTickPosY = prevPosY = posY;
        lastTickPosZ = prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        prevRotationRoll = rotationRoll;
        
        // apply custom update packet 250 if any
        if (lastUpdatePacket != null)
        {
            if (worldObj.isRemote) applyUpdatePacketFromServer(lastUpdatePacket);
            else applyUpdatePacketFromClient(lastUpdatePacket);
            lastUpdatePacket = null; // only apply once
        }
        
        // apply dataWatcher changes for target entity 
        if (worldObj.isRemote)
        {
            // read data and apply if changed on client
	        int id = dataWatcher.getWatchableObjectInt(24);
	        
	        if (id != prevTargetEntityId)
	        {
                prevTargetEntityId = id;
                
                if (id > 0)
                {
		            Entity target = ((WorldClient) worldObj).getEntityByID(id);
		            if (target != null && !target.isDead && target instanceof ThxEntity)
		            {
		                targetEntity = (ThxEntity) target;
		            }
                }
                else
                {
                    targetEntity = null;
                }
	        }
        }
        else
        {
            // set data if changed on server
	        if (targetEntity != null && targetEntity.entityId != prevTargetEntityId)
	        {
		        dataWatcher.updateObject(24, Integer.valueOf(targetEntity.entityId));
                prevTargetEntityId = targetEntity.entityId;
	        }
	        else if (targetEntity == null && prevTargetEntityId > 0)
            {
		        dataWatcher.updateObject(24, Integer.valueOf(0));
	            prevTargetEntityId = 0;
            }
        }
        
        inWater = isInWater();
        
        // decrement cooldown timers
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
        
        updateRotation();
        updateVectors();
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

    /*
    public Vector3 getForward()
    {
        float f3 = MathHelper.sin(-rotationYaw * 0.01745329F - 3.141593F);
        float f1 = MathHelper.cos(-rotationYaw * 0.01745329F - 3.141593F);
        float f5 = -MathHelper.cos(-rotationPitch * 0.01745329F);
        float f7 = MathHelper.sin(-rotationPitch * 0.01745329F);
        return new Vector3(f3 * f5, f7, f1 * f5);
    }
    */

    @Override
    public void setDead()
    {
        log("setDead called");
        super.setDead();
    }
    
    /*
    @Override
    public void setPosition(double x, double y, double z)
    {
        // called on both client and server
        super.setPosition(x, y, z);
    }
    */
    
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
        return this.getClass().getSimpleName() + " " + entityId + (worldObj.isRemote ? " (client)" : " (server)");
    }

    public void log(String s)
    {
        mod_thx.log(String.format("[%5d] ", ticksExisted) + this + ": " + s);
    }
    
    void plog(String s) // periodic log
    {
        if (worldObj.getWorldTime() % 60 == 0)
        {
            log(s); //
        }
    }
    
    /* subclasses can react to player pilot right click, e.g. helicopter fires missile */
    void interactByPilot()
    {
    }
    
    @Override
    public boolean interact(EntityPlayer player)
    {
        log("interact called with player " + player.entityId);
        
        if (player.equals(riddenByEntity))
        {
            interactByPilot();
            return false;
        }
        
        // feature to allow capture of a near helicopter, ejecting the current pilot
        if (riddenByEntity != null)
        {
            // already ridden by some other entity, allow takeover if close
            if (getDistanceSqToEntity(player) < 3.0)
            {
                log("current pilot [" + riddenByEntity + "] was ejected in boarding attempt by [" + player + "]");
                pilotExit();
            }
            else
            {
	            return false; // boarding attempt failed
            }
        }
        
        if (player.ridingEntity != null) 
        {
            // player is already riding some other entity?
            return false;
        }
        
        
        // new pilot boarding! on server
		if (!worldObj.isRemote) 
        {
	        log("interact() calling mountEntity on player " + player.entityId);
            player.mountEntity(this); // boarding, server
        }
        
        owner = player;
        
        player.rotationYaw = rotationYaw;
        updateRiderPosition();
        
        log("interact() added pilot: " + player);
        return true;
    }
    
    void pilotExit()
    {
        // vehicle subclasses will override
    }
    
    void takeDamage(float amount)
    {
        damage += amount;
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int damageAmount)
    {
        if (worldObj.isRemote) return false; // server only
        
        log("attackEntityFrom called with damageSource: " + damageSource + " with amount " + damageAmount);

        if (timeSinceAttacked > 0f || isDead || damageSource == null) return false;
        //timeSinceAttacked = .5f; // sec delay before this entity can be attacked again
        timeSinceAttacked = .2f; // sec delay before this entity can be attacked again
        

        Entity attackingEntity = damageSource.getEntity();
        log("attacked by entity: " + attackingEntity);
        
        if (attackingEntity == null) return false; // when is this the case?
        if (attackingEntity.equals(this)) return false; // ignore damage from self?
        if (attackingEntity.equals(riddenByEntity))
        {
            attackedByPilot();
            return false; // ignore attack by pilot (player left click)
        }
        if (attackingEntity instanceof ThxEntity) // check for drone
        {
            attackedByThxEntity((ThxEntity) attackingEntity);
        }
        if (attackingEntity.ridingEntity instanceof ThxEntity) // check for other player pilot helicopter
        {
            attackedByThxEntity((ThxEntity) attackingEntity.ridingEntity);
        }
        return true; // hit landed
    }
    
    /* subclasses can react to player left click, e.g. helicopter fires rocket */
    void attackedByPilot()
    {
    }
    
    /* subclasses can react to attack by other thx entity */
    void attackedByThxEntity(ThxEntity entity)
    {
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
    public boolean isInRangeToRenderDist(double d)
    {
        return d < 128.0 * 128.0;
    }

    private Packet250CustomPayload createUpdatePacket()
    {
        return new ThxEntityPacket250Data(this).createPacket250();
    }
    
    public void sendUpdatePacketFromClient()
    {
        assertClientSideOnly();
        
        minecraft.getSendQueue().addToSendQueue(createUpdatePacket());
    }
    
    public void applyUpdatePacketFromClient(ThxEntityPacket250Data packet)
    {
        assertServerSideOnly();
        
        setPositionAndRotation(packet.posX, packet.posY, packet.posZ, packet.yaw, packet.pitch);
        //setRotation(packet.yaw, packet.pitch);
        
        rotationRoll = packet.roll % 360f;

        motionX =  packet.motionX;
        motionY =  packet.motionY;
        motionZ =  packet.motionZ;
        
        damage = packet.damage;
        throttle = packet.throttle;

        // server command queue
        cmd_create_item = packet.cmd_create_item;
        cmd_reload      = packet.cmd_reload;
        cmd_exit        = packet.cmd_exit;
        cmd_create_map  = packet.cmd_create_map;
        
        updateDataWatcher(); // this will send roll and throttle to clients
        
        if (packet.pilotId == 0 && riddenByEntity != null)
        {
            ////log("*** current pilot " + riddenByEntity.entityId + " is exiting (NOT calling pilotExit on server based on packet, deferring to interact");
            // must call pilotExit on serveror else pilot will exit helicopter but stay in seated position, no way to move helicoper, bugged!
            
            // called by pilotExit below
            //riddenByEntity.mountEntity(entity); // unmount
            
            // yes, it happens. should we ignore client update packets with no pilot?
            //if (true) throw new RuntimeException("does this happen?");
            
            log("*** current pilot " + riddenByEntity.entityId + " is exiting");
            pilotExit(); 
        }
	        
        //int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        //plog(String.format("end applyUpdatePacket, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
    }    
    
    void readDataWatcher()
    {
        // read from dataWatcher
        throttle = getWatched_Throttle();
        rotationRoll = getWatched_Roll();
    }
    
    void updateDataWatcher()
    {
        // record values in dataWatcher
        setWatched_Throttle(throttle);
        setWatched_Roll(rotationRoll);
    }
    
    public void sendUpdatePacketFromServer()
    {
        assertServerSideOnly();
        
        Packet250CustomPayload packet = createUpdatePacket();
        
        for (int i = 0; i < worldObj.playerEntities.size(); i++) 
        {
            EntityPlayerMP player = (EntityPlayerMP) worldObj.playerEntities.get(i);
            if (riddenByEntity != null && riddenByEntity.entityId == player.entityId) continue; // skip pilot
            player.playerNetServerHandler.sendPacketToPlayer(packet);
        }
    }
    
    public void applyUpdatePacketFromServer(ThxEntityPacket250Data packet)
    {
        log("applyUpdatePacketFromServer: " + packet);
        
        assertClientSideOnly();
        
        setPositionAndRotation(packet.posX, packet.posY, packet.posZ, packet.yaw, packet.pitch);
        
        rotationRoll = packet.roll % 360f;

        motionX =  packet.motionX;
        motionY =  packet.motionY;
        motionZ =  packet.motionZ;
        
        damage = packet.damage;
        throttle = packet.throttle;

        // not sure what owner is used for on the client...
        /*
        if (packet.ownerId > 0)
        {
            if (owner == null || owner.entityId != packet.ownerId)
            {
                log("*** New entity owner id: " + packet.ownerId);
                    
                // first check for owner that is entity (e.g. helicopter)
                owner = ((WorldClient) worldObj).getEntityByID(packet.ownerId);
                    
                if (owner == null)
                {
                    // otherwise, check for owner that is a player
                    owner = mod_Thx.getEntityPlayerById(packet.ownerId);
                }
                    
                log("*** New entity owner: " + owner);
            }
        }
        */

        // no or wrong current pilot
        if (packet.pilotId > 0 && (riddenByEntity == null)) // || riddenByEntity.entityId != packet.pilotId))
        {
            Entity pilot = ((WorldClient) worldObj).getEntityByID(packet.pilotId);
            if (pilot != null && !pilot.isDead)
            {
                //log("*** applyUpdatePacket: pilot " + pilot + " now boarding");
                //pilot.mountEntity(this); // boarding
            }
        }
        else if (packet.pilotId == 0 && riddenByEntity != null)
        {
            log("*** applyUpdatePacket: current pilot " + riddenByEntity + " is exiting");
            //riddenByEntity.mountEntity(entity); // unmount
            pilotExit();
        }
            
        serverPosX = MathHelper.floor_float(packet.posX * 32f);
        serverPosY = MathHelper.floor_float(packet.posY * 32f);
        serverPosZ = MathHelper.floor_float(packet.posZ * 32f);
        
        //int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        //plog(String.format("end applyUpdatePacket, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, pitch: %6.3f, roll: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, rotationPitch, rotationRoll, throttle, motionX, motionY, motionZ));
    }    
    
    @Override
    public void updateRidden()
    {
        log("updateRidden() called, not calling super"); // super.updateRidden();
    }
    
    @Override
    protected void setBeenAttacked()
    {
        //super.setBeenAttacked(): this.velocityChanged = true; // causes EntityTrackerEntry to send Packet28 "knock-back"
    }
    
    void assertClientSideOnly()
    {
        if (worldObj.isRemote) return; // OK, we are on client as expected
        
        throw new RuntimeException("Client-side method was called on Server");
    }
    
    void assertServerSideOnly()
    {
        if (!worldObj.isRemote) return; // OK, we are on server as expected
            
        throw new RuntimeException("Server-side method was called on client");
    }
}
