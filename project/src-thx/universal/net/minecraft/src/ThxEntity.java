package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class ThxEntity extends Entity
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

    Vector3 pos = new Vector3(); // position
    Vector3 vel = new Vector3(); // velocity
    Vector3 acc = new Vector3(); // velocity
    Vector3 ypr = new Vector3(); // yaw, pitch, roll
    
    // vectors relative to entity orientation
    Vector3 fwd  = new Vector3(); // straight ahead
    Vector3 side = new Vector3(); // left side perp
    Vector3 up   = new Vector3(); // up
    
    ThxEntityHelper helper;
    
    abstract ThxEntityHelper createHelper();
    
    ThxEntityPacket250 lastUpdatePacket;
    
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
    
    boolean isActive;
    
    public ThxEntity(World world)
    {
        super(world);

        preventEntitySpawning = true; // keeps other entities from spawning on top of us

        prevTime = System.nanoTime();
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
        
        if (lastUpdatePacket != null)
        {
            if (worldObj.isRemote) applyUpdatePacketFromServer(lastUpdatePacket);
            else applyUpdatePacketFromClient(lastUpdatePacket);
            lastUpdatePacket = null; // only apply once
        }
        
        inWater = isInWater();
        
        // decrement cooldown timers
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
        
        updateRotation();
        updateVectors();
        
        isActive = false; // by default, don't trigger custom packets updates from server, will be set true later for drone and piloted helicopters
        // also going to try using for missile and rocket entities
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

    void log(String s)
    {
        mod_Thx.log(String.format("[%5d] ", ticksExisted) + this + ": " + s);
    }
    
    void plog(String s) // periodic log
    {
        if (plog && worldObj.worldInfo.getWorldTime() % 60 == 0)
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
        
        if (riddenByEntity != null)
        {
            // already ridden by some other entity, allow takeover if close
            if (getDistanceSqToEntity(player) < 3.0)
            {
                log("current pilot was ejected");
                pilotExit();
            }
            else
            {
	            return false;
            }
        }
        
        if (player.equals(riddenByEntity))
        {
            interactByPilot();
            return false;
        }
        
        if (player.ridingEntity != null) 
        {
            // player is already riding some other entity
            return false;
        }
        
        
        // new pilot boarding!
        if (!worldObj.isRemote) 
        {
	        log("interact() calling mountEntity on player " + player.entityId);
            player.mountEntity(this); // boarding, server only
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
    protected void entityInit()
    {
        log(this + " entityInit");
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

    public boolean isInRangeToRenderDist(double d)
    {
        return d < 128.0 * 128.0;
    }

    /*
    public void spawn(ThxEntityPacket250 packet)
    {
        log("Received spawn packet: " + packetToString(packet));

        int entityIdOrig = entityId;
        entityId = packet.dataInt[0];

        applyUpdatePacket(packet);
        
        updateRotation();
        updateVectors();

        log("spawn with pos, rot, mot, and id for entity with previous id " + entityIdOrig);
        log("spawn(): posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }
    */
    
    public Packet250CustomPayload getUpdatePacketFromClient()
    {
        // make sure we are on the client before creating update packet
        if (!worldObj.isRemote) throw new RuntimeException("server should not be asked to create client packet!");
        
        ThxEntityPacket250 data = new ThxEntityPacket250();
        
        data.msg = "client timestamp: " + worldObj.getWorldTime();
        
        data.entityId = entityId;
        data.ownerId = owner != null ? owner.entityId : 0;
        data.pilotId = riddenByEntity != null ? riddenByEntity.entityId : 0;
        
        serverCommandQueue: // clear cmd flags after setting them in packet to avoid resending them later
        {
	        data.cmd_create_item = cmd_create_item;
		    cmd_create_item = 0;
		    
	        data.cmd_reload      = cmd_reload;
		    cmd_reload = 0;
		    
	        data.cmd_exit        = cmd_exit;
		    cmd_exit = 0;
		    
	        data.cmd_create_map  = cmd_create_map;
		    cmd_create_map = 0;
        }
	    
        data.posX            = (float) posX;
        data.posY            = (float) posY;
        data.posZ            = (float) posZ;
        data.yaw             = rotationYaw;
        data.pitch           = rotationPitch;
        data.roll            = rotationRoll;
        data.motionX         = (float) motionX;
        data.motionY         = (float) motionY;
        data.motionZ         = (float) motionZ;
        data.damage          = damage;
        data.throttle        = throttle;
	
        return data.createPacket250CustomPayload();
    }

    public Packet250CustomPayload getUpdatePacketFromServer()
    {
        // make sure we are on the server before creating update packet
        if (worldObj.isRemote) throw new RuntimeException("client should not be asked to create server packet!");
        
        ThxEntityPacket250 data = new ThxEntityPacket250();
        
        data.msg = "server timestamp: " + worldObj.getWorldTime();
        
        data.entityId = entityId;
        data.ownerId = owner != null ? owner.entityId : 0;
        data.pilotId = riddenByEntity != null ? riddenByEntity.entityId : 0;
        
        // don't send any server commands to client as they are not used there
        data.cmd_create_item = 0;
        data.cmd_reload      = 0;
        data.cmd_exit        = 0;
        data.cmd_create_map  = 0;
	    
        data.posX            = (float) posX;
        data.posY            = (float) posY;
        data.posZ            = (float) posZ;
        data.yaw             = rotationYaw;
        data.pitch           = rotationPitch;
        data.roll            = rotationRoll;
        data.motionX         = (float) motionX;
        data.motionY         = (float) motionY;
        data.motionZ         = (float) motionZ;
        data.damage          = damage;
        data.throttle        = throttle;
	
        return data.createPacket250CustomPayload();
    }

    public void applyUpdatePacketFromClient(ThxEntityPacket250 packet)
    {
        plog("Applying client packet: " + packet);
        
        // make sure we are on the server before applying update from client
        if (worldObj.isRemote) throw new RuntimeException("client should not be asked to apply client packet!");
        
        setPositionAndRotation(packet.posX, packet.posY, packet.posZ, packet.yaw, packet.pitch);
        
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
        
        if (packet.pilotId == 0 && riddenByEntity != null)
        {
            ////log("*** current pilot " + riddenByEntity.entityId + " is exiting (NOT calling pilotExit on server based on packet, deferring to interact");
            // must call pilotExit on serveror else pilot will exit helicopter but stay in seated position, no way to move helicoper, bugged!
            
            // called by pilotExit below
            //riddenByEntity.mountEntity(entity); // unmount
            
            log("*** current pilot " + riddenByEntity.entityId + " is exiting");
            pilotExit(); 
        }
	        
        int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        //plog(String.format("end applyUpdatePacket, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
    }    
    
    public void applyUpdatePacketFromServer(ThxEntityPacket250 packet)
    {
        plog("Applying server packet: " + packet);
        
        // make sure we are on the client before applying update from server
        if (!worldObj.isRemote) throw new RuntimeException("server should not be asked to apply server packet!");
        
        setPositionAndRotation(packet.posX, packet.posY, packet.posZ, packet.yaw, packet.pitch);
        
        rotationRoll = packet.roll % 360f;

        motionX =  packet.motionX;
        motionY =  packet.motionY;
        motionZ =  packet.motionZ;
        
        damage = packet.damage;
        throttle = packet.throttle;

        // not sure what owner is used for on the client...
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

        // no or wrong current pilot
        if (packet.pilotId > 0 && (riddenByEntity == null || riddenByEntity.entityId != packet.pilotId))
        {
            Entity pilot = ((WorldClient) worldObj).getEntityByID(packet.pilotId);
            if (pilot != null && !pilot.isDead)
            {
                log("*** applyUpdatePacket: pilot " + pilot + " now boarding");
                pilot.mountEntity(this); // boarding
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
        
        int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        //plog(String.format("end applyUpdatePacket, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, pitch: %6.3f, roll: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, rotationPitch, rotationRoll, throttle, motionX, motionY, motionZ));
    }    
    
    @Override
    public void updateRidden()
    {
        log("updateRidden() called, not calling super");
    }
    
    @Override
    public void setPositionAndRotation2(double posX, double posY, double posZ, float yaw, float pitch, int unused)
    {
        // bypassing check for collision in super method which seems to be hitting pilot and causing jumping
	    if (riddenByEntity != null) setPositionAndRotation(posX, posY, posZ, yaw, pitch);
	    
	    else super.setPositionAndRotation2(posX, posY, posZ, yaw, pitch, unused);
    }
}
