package net.minecraft.src;

import java.util.List;
import java.io.*;

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
    
    Packet250CustomPayload lastUpdatePacket;
    
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

        //log(world.isRemote ? "Created new MP client entity" : "Created new SP/MP master entity");

        preventEntitySpawning = true;

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

        lastTickPosX = prevPosX = posX;
        lastTickPosY = prevPosY = posY;
        lastTickPosZ = prevPosZ = posZ;

        prevRotationPitch = rotationPitch;
        prevRotationYaw = rotationYaw;
        prevRotationRoll = rotationRoll;
        
        if (worldObj.isRemote)
        {
	        applyUpdatePacket(lastUpdatePacket);
	        lastUpdatePacket = null; // only apply once
        }
        
        inWater = isInWater();
        
        // decrement cooldown timers
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
        
        updateRotation();
        updateVectors();
        
        isActive = true; // trigger custom packets updates for IClientDriven subclasses
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
        return this.getClass().getSimpleName() + " " + entityId;
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
        
        // new pilot boarding!
        if (!worldObj.isRemote)
        {
	        log("interact() calling mountEntity on player " + player.entityId);
	        player.mountEntity(this); // boarding
	        owner = player;
        }
        
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

    /* ISpawnable SERVER interface */
    public Packet250CustomPayload getSpawnPacket()
    {
        Packet250CustomPayload packet = getUpdatePacket();
        //packet.dataString[0] = "spawn packet for thx entity " + entityId;
        
        log("Returning spawn packet: " + packet);
        return packet;
    }
    
    /* ISpawnable CLIENT interface */
    public void spawn(Packet250CustomPayload packet)
    {
        log("Received spawn packet: " + packetToString(packet));

        int entityIdOrig = entityId;

        byte[] bytes = packet.data;

        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(bytes));

        try
        {
            int packetTypeId = dataStream.readInt();
            // TODO: spawn packet type

            entityId = dataStream.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        applyUpdatePacket(packet);
        
        updateRotation();
        updateVectors();

        log("spawn with pos, rot, mot, and id for entity with previous id " + entityIdOrig);
        log("spawn(): posX: " + posX + ", posY: " + posY + ", posZ: " + posZ);
    }
    
    public Packet250CustomPayload getUpdatePacket()
    {
        Packet250CustomPayload packet = new Packet250CustomPayload();

        packet.channel = mod_Thx.channelName;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try
        {
            data.writeInt(getPacketTypeId()); // 75-77 for Thx entity IDs

            data.writeInt(entityId);
            data.writeInt(owner != null ? owner.entityId : 0);
            data.writeInt(riddenByEntity != null ? riddenByEntity.entityId : 0);
            data.writeInt(cmd_create_item);
            data.writeInt(cmd_reload);
            data.writeInt(cmd_exit);
            data.writeInt(cmd_create_map);
        
            // clear cmd flags after setting them in packet
            cmd_reload = 0;
            cmd_create_item = 0;
            cmd_exit = 0;
            cmd_create_map = 0;
                
            data.writeFloat((float) posX);
            data.writeFloat((float) posY);
            data.writeFloat((float) posZ);
            data.writeFloat(rotationYaw);
            data.writeFloat(rotationPitch);
            data.writeFloat(rotationRoll);
            data.writeFloat((float) motionX);
            data.writeFloat((float) motionY);
            data.writeFloat((float) motionZ);
            data.writeFloat(damage);
            data.writeFloat(throttle);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        
        return packet;
    }

    void applyUpdatePacket(Packet250CustomPayload packet)
    {
        if (packet == null) return;
        
        if (ThxConfig.LOG_INCOMING_PACKETS) plog("<<< " + packetToString(packet));

        byte[] bytes = packet.data;
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(bytes));

        int inPacketTypeId, inEntityID, inOwnerEntityId, inRiddenByEntityId, in_cmd_create_item, in_cmd_reload, in_cmd_exit, in_cmd_create_map; 
        float inPosX, inPosY, inPosZ, inRotationYaw, inRotationPitch, inRotationRoll, inMotionX, inMotionY, inMotionZ, inDamage, inThrottle; 

        try
        {
            inPacketTypeId = dataStream.readInt();

            inEntityID = dataStream.readInt();
            inOwnerEntityId = dataStream.readInt();
            inRiddenByEntityId = dataStream.readInt();    // TODO: set?
            in_cmd_create_item = dataStream.readInt();
            in_cmd_reload = dataStream.readInt();
            in_cmd_exit = dataStream.readInt();
            in_cmd_create_map = dataStream.readInt();
                
            inPosX = dataStream.readFloat();
            inPosY = dataStream.readFloat();
            inPosZ = dataStream.readFloat();
            inRotationYaw = dataStream.readFloat();
            inRotationPitch = dataStream.readFloat();
            inRotationRoll = dataStream.readFloat();
            inMotionX = dataStream.readFloat();
            inMotionY = dataStream.readFloat();
            inMotionZ = dataStream.readFloat();
            inDamage = dataStream.readFloat();
            inThrottle = dataStream.readFloat();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        
        if (!worldObj.isRemote)
        {
	        cmd_create_item = in_cmd_create_item;
	        cmd_reload      = in_cmd_reload;
	        cmd_exit        = in_cmd_exit;
	        cmd_create_map  = in_cmd_create_map;
        }
        
        setPositionAndRotation(inPosX, inPosY, inPosZ, inRotationYaw, inRotationPitch);
        
        rotationRoll = inRotationRoll % 360f;

        motionX =  inMotionX;
        motionY =  inMotionY;
        motionZ =  inMotionZ;
        
        damage = inDamage;
        throttle = inThrottle;

        helper.applyUpdatePacket(inOwnerEntityId, inRiddenByEntityId, inPosX, inPosY, inPosZ);
        
        int riddenById = riddenByEntity != null ? riddenByEntity.entityId : 0;
        plog(String.format("end applyPaket, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", riddenById, posX, posY, posZ, rotationYaw, throttle, motionX, motionY, motionZ));
    }    
    
    public String packetToString(Packet250CustomPayload packet)
    {
        StringBuffer s = new StringBuffer();
        s.append("Packet250 {");
        s.append("channel: ").append(packet.channel).append(", ");

        for (int i = 0; i < packet.data.length; i++)
        {
            s.append(packet.data[i]);
            s.append(", ");
        }
        s.append("}");

        return s.toString();
    }
    
    abstract int getPacketTypeId();
    
    abstract ThxEntityHelper createHelper();
}
