package net.minecraft.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ThxEntity extends Entity
{
    boolean plog = true;
    final float RAD_PER_DEG = 0.01745329F;
    final float PI = (float)Math.PI;
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
    Vector3 pos = new Vector3();
    Vector3 vel = new Vector3();
    Vector3 acc = new Vector3();
    Vector3 ypr = new Vector3();
    Vector3 fwd = new Vector3();
    Vector3 side = new Vector3();
    Vector3 up = new Vector3();
    ThxEntityHelper helper;
    Packet250CustomPayload lastUpdatePacket;
    int cmd_reload;
    int cmd_create_item;
    int cmd_exit;
    int cmd_create_map;
    float damage;
    float throttle;
    Entity owner;
    float timeSinceAttacked;
    float timeSinceCollided;
    boolean isActive;

    public ThxEntity(World var1)
    {
        super(var1);
        this.bf = true;
        this.prevTime = System.nanoTime();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void F_()
    {
        ++this.ticksLived;
        long var1 = System.nanoTime();
        this.deltaTime = (float)(var1 - this.prevTime) / 1.0E9F;

        if (this.deltaTime > 0.05F)
        {
            this.deltaTime = 0.05F;
        }

        this.prevTime = var1;
        this.bL = this.lastX = this.locX;
        this.bM = this.lastY = this.locY;
        this.bN = this.lastZ = this.locZ;
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        this.prevRotationRoll = this.rotationRoll;

        if (this.world.isStatic)
        {
            this.applyUpdatePacket(this.lastUpdatePacket);
            this.lastUpdatePacket = null;
        }

        this.bV = this.aU();
        this.timeSinceAttacked -= this.deltaTime;
        this.timeSinceCollided -= this.deltaTime;
        this.updateRotation();
        this.updateVectors();
        this.isActive = true;
    }

    /**
     * Checks if this entity is inside water (if inWater field is true as a result of handleWaterMovement() returning
     * true)
     */
    public boolean aU()
    {
        return this.world.b(this.boundingBox.grow(0.0D, -0.4D, 0.0D), Material.WATER);
    }

    public void updateRotation()
    {
        this.yaw %= 360.0F;

        if (this.yaw > 180.0F)
        {
            this.yaw -= 360.0F;
        }
        else if (this.yaw < -180.0F)
        {
            this.yaw += 360.0F;
        }

        this.yawRad = this.yaw * 0.01745329F;
        this.pitch %= 360.0F;

        if (this.pitch > 180.0F)
        {
            this.pitch -= 360.0F;
        }
        else if (this.pitch < -180.0F)
        {
            this.pitch += 360.0F;
        }

        this.pitchRad = this.pitch * 0.01745329F;
        this.rotationRoll %= 360.0F;

        if (this.rotationRoll > 180.0F)
        {
            this.rotationRoll -= 360.0F;
        }
        else if (this.rotationRoll < -180.0F)
        {
            this.rotationRoll += 360.0F;
        }

        this.rollRad = this.rotationRoll * 0.01745329F;
    }

    public void updateVectors()
    {
        float var1 = MathHelper.cos(-this.yawRad - (float)Math.PI);
        float var2 = MathHelper.sin(-this.yawRad - (float)Math.PI);
        float var3 = MathHelper.cos(-this.pitchRad);
        float var4 = MathHelper.sin(-this.pitchRad);
        float var5 = MathHelper.cos(-this.rollRad);
        float var6 = MathHelper.sin(-this.rollRad);
        this.fwd.x = -var2 * var3;
        this.fwd.y = var4;
        this.fwd.z = -var1 * var3;
        this.side.x = var1 * var5;
        this.side.y = -var6;
        this.side.z = -var2 * var5;
        Vector3.cross(this.side, this.fwd, this.up);
        this.pos.x = (float)this.locX;
        this.pos.y = (float)this.locY;
        this.pos.z = (float)this.locZ;
        this.vel.x = (float)this.motX;
        this.vel.y = (float)this.motY;
        this.vel.z = (float)this.motZ;
        this.ypr.x = this.yaw;
        this.ypr.y = this.pitch;
        this.ypr.z = this.rotationRoll;
    }

    /**
     * Will get destroyed next tick.
     */
    public void die()
    {
        this.log("setDead called");
        super.die();
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void a(float var1)
    {
        this.log("fall() called with arg " + var1);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void a(NBTTagCompound var1) {}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void b(NBTTagCompound var1) {}

    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.id;
    }

    void log(String var1)
    {
        mod_Thx.log(String.format("[%5d] ", new Object[] {Integer.valueOf(this.ticksLived)}) + this + ": " + var1);
    }

    void plog(String var1)
    {
        if (this.plog && this.world.worldData.getTime() % 60L == 0L)
        {
            this.log(var1);
        }
    }

    void interactByPilot() {}

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean b(EntityHuman var1)
    {
        this.log("interact called with player " + var1.id);

        if (var1.equals(this.passenger))
        {
            this.interactByPilot();
            return false;
        }
        else if (var1.vehicle != null)
        {
            return false;
        }
        else
        {
            if (this.passenger != null)
            {
                if (this.j(var1) >= 3.0D)
                {
                    return false;
                }

                this.log("current pilot was ejected");
                this.pilotExit();
            }

            if (!this.world.isStatic)
            {
                this.log("interact() calling mountEntity on player " + var1.id);
                var1.mount(this);
                this.owner = var1;
            }

            var1.yaw = this.yaw;
            this.i_();
            this.log("interact() added pilot: " + var1);
            return true;
        }
    }

    void pilotExit() {}

    void takeDamage(float var1)
    {
        this.damage += var1;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean damageEntity(DamageSource var1, int var2)
    {
        this.log("attackEntityFrom called with damageSource: " + var1 + " with amount " + var2);

        if (this.timeSinceAttacked <= 0.0F && !this.dead && var1 != null)
        {
            this.timeSinceAttacked = 0.2F;
            Entity var3 = var1.getEntity();
            this.log("attacked by entity: " + var3);

            if (var3 == null)
            {
                return false;
            }
            else if (var3.equals(this))
            {
                return false;
            }
            else if (var3.equals(this.passenger))
            {
                this.attackedByPilot();
                return false;
            }
            else
            {
                if (var3 instanceof ThxEntity)
                {
                    this.attackedByThxEntity((ThxEntity)var3);
                }

                if (var3.vehicle instanceof ThxEntity)
                {
                    this.attackedByThxEntity((ThxEntity)var3.vehicle);
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    void attackedByPilot() {}

    void attackedByThxEntity(ThxEntity var1) {}

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean o_()
    {
        return !this.dead;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean e_()
    {
        return true;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean g_()
    {
        return false;
    }

    protected void b()
    {
        this.log(this + " entityInit");
    }

    /**
     * returns the bounding box for this entity
     */
    public AxisAlignedBB h()
    {
        return this.boundingBox;
    }

    /**
     * Returns a boundingBox used to collide the entity with other entities and blocks. This enables the entity to be
     * pushable on contact, like boats or minecarts.
     */
    public AxisAlignedBB b_(Entity var1)
    {
        return var1.boundingBox;
    }

    public boolean isInRangeToRenderDist(double var1)
    {
        return var1 < 16384.0D;
    }

    public Packet250CustomPayload getSpawnPacket()
    {
        Packet250CustomPayload var1 = this.getUpdatePacket();
        this.log("Returning spawn packet: " + var1);
        return var1;
    }

    public void spawn(Packet250CustomPayload var1)
    {
        this.log("Received spawn packet: " + this.packetToString(var1));
        int var2 = this.id;
        byte[] var3 = var1.data;
        DataInputStream var4 = new DataInputStream(new ByteArrayInputStream(var3));

        try
        {
            int var5 = var4.readInt();
            this.id = var4.readInt();
        }
        catch (IOException var6)
        {
            var6.printStackTrace();
            return;
        }

        this.applyUpdatePacket(var1);
        this.updateRotation();
        this.updateVectors();
        this.log("spawn with pos, rot, mot, and id for entity with previous id " + var2);
        this.log("spawn(): posX: " + this.locX + ", posY: " + this.locY + ", posZ: " + this.locZ);
    }

    public Packet250CustomPayload getUpdatePacket()
    {
        Packet250CustomPayload var1 = new Packet250CustomPayload();
        var1.tag = "mod_Thx";
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        DataOutputStream var3 = new DataOutputStream(var2);

        try
        {
            var3.writeInt(this.getPacketTypeId());
            var3.writeInt(this.id);
            var3.writeInt(this.owner != null ? this.owner.id : 0);
            var3.writeInt(this.passenger != null ? this.passenger.id : 0);
            var3.writeInt(this.cmd_create_item);
            var3.writeInt(this.cmd_reload);
            var3.writeInt(this.cmd_exit);
            var3.writeInt(this.cmd_create_map);
            this.cmd_reload = 0;
            this.cmd_create_item = 0;
            this.cmd_exit = 0;
            this.cmd_create_map = 0;
            var3.writeFloat((float)this.locX);
            var3.writeFloat((float)this.locY);
            var3.writeFloat((float)this.locZ);
            var3.writeFloat(this.yaw);
            var3.writeFloat(this.pitch);
            var3.writeFloat(this.rotationRoll);
            var3.writeFloat((float)this.motX);
            var3.writeFloat((float)this.motY);
            var3.writeFloat((float)this.motZ);
            var3.writeFloat(this.damage);
            var3.writeFloat(this.throttle);
        }
        catch (IOException var5)
        {
            var5.printStackTrace();
        }

        var1.data = var2.toByteArray();
        var1.length = var1.data.length;
        return var1;
    }

    void applyUpdatePacket(Packet250CustomPayload var1)
    {
        if (var1 != null)
        {
            if (ThxConfig.LOG_INCOMING_PACKETS)
            {
                this.plog("<<< " + this.packetToString(var1));
            }

            byte[] var2 = var1.data;
            DataInputStream var3 = new DataInputStream(new ByteArrayInputStream(var2));
            int var6;
            int var7;
            int var8;
            int var9;
            int var10;
            int var11;
            float var12;
            float var13;
            float var14;
            float var15;
            float var17;
            float var16;
            float var19;
            float var18;
            float var21;
            float var20;
            float var22;

            try
            {
                int var4 = var3.readInt();
                int var5 = var3.readInt();
                var6 = var3.readInt();
                var7 = var3.readInt();
                var8 = var3.readInt();
                var9 = var3.readInt();
                var10 = var3.readInt();
                var11 = var3.readInt();
                var12 = var3.readFloat();
                var13 = var3.readFloat();
                var14 = var3.readFloat();
                var15 = var3.readFloat();
                var16 = var3.readFloat();
                var17 = var3.readFloat();
                var18 = var3.readFloat();
                var19 = var3.readFloat();
                var20 = var3.readFloat();
                var21 = var3.readFloat();
                var22 = var3.readFloat();
            }
            catch (IOException var24)
            {
                var24.printStackTrace();
                return;
            }

            if (!this.world.isStatic)
            {
                this.cmd_create_item = var8;
                this.cmd_reload = var9;
                this.cmd_exit = var10;
                this.cmd_create_map = var11;
            }

            this.setLocation((double)var12, (double)var13, (double)var14, var15, var16);
            this.rotationRoll = var17 % 360.0F;
            this.motX = (double)var18;
            this.motY = (double)var19;
            this.motZ = (double)var20;
            this.damage = var21;
            this.throttle = var22;
            this.helper.applyUpdatePacket(var6, var7, var12, var13, var14);
            int var23 = this.passenger != null ? this.passenger.id : 0;
            this.plog(String.format("end applyPaket, pilot %d [posX: %6.3f, posY: %6.3f, posZ: %6.3f, yaw: %6.3f, throttle: %6.3f, motionX: %6.3f, motionY: %6.3f, motionZ: %6.3f]", new Object[] {Integer.valueOf(var23), Double.valueOf(this.locX), Double.valueOf(this.locY), Double.valueOf(this.locZ), Float.valueOf(this.yaw), Float.valueOf(this.throttle), Double.valueOf(this.motX), Double.valueOf(this.motY), Double.valueOf(this.motZ)}));
        }
    }

    public String packetToString(Packet250CustomPayload var1)
    {
        StringBuffer var2 = new StringBuffer();
        var2.append("Packet250 {");
        var2.append("channel: ").append(var1.tag).append(", ");

        for (int var3 = 0; var3 < var1.data.length; ++var3)
        {
            var2.append(var1.data[var3]);
            var2.append(", ");
        }

        var2.append("}");
        return var2.toString();
    }

    abstract int getPacketTypeId();

    abstract ThxEntityHelper createHelper();
}
