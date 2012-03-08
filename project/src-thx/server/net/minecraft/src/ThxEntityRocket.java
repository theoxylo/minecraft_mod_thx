package net.minecraft.src;

import java.util.List;
import java.util.Random;

public class ThxEntityRocket  extends ThxEntity implements ISpawnable
{
    private int xTile;
    private int yTile;
    private int zTile;
    private int inTile;
    private boolean inGround;
    private int ticksInGround;
    
    boolean enteredWater;
    boolean launched;
    
	public ThxEntityRocket(World world)
    {
        super(world);
        
	    helper = new ThxEntityHelperServer(this);
	    
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inGround = false;
        //setSize(0.25F, 0.25F);
        setSize(0.5f, 0.5f);
        
        NET_PACKET_TYPE = 76;
    }

	/* only needed if using Packet23VehicleSpawn instead of ISpawnable
    public ThxEntityRocket(World world, double x, double y, double z)
    {
        this(world);
        setPositionAndRotation(x, y, z, 0f, 0f);
    }
    */

    public ThxEntityRocket(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
    {
        this(owner.worldObj);
        
        this.owner = owner;
        
        setPositionAndRotation(x, y, z, yaw, pitch);
        
        float acceleration = .7f;
        motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * acceleration;
        motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * acceleration;
        motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F) * acceleration;
        
        motionX += dx;
        motionY += dy;
        motionZ += dz;
        
        setHeading(motionX, motionY, motionZ, 1.5f, 1.0f);
    }

    public void setHeading(double d, double d1, double d2, float f, 
            float f1)
    {
        float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
        d /= f2;
        d1 /= f2;
        d2 /= f2;
        d += rand.nextGaussian() * 0.0074999998323619366D * (double)f1;
        d1 += rand.nextGaussian() * 0.0074999998323619366D * (double)f1;
        d2 += rand.nextGaussian() * 0.0074999998323619366D * (double)f1;
        d *= f;
        d1 *= f;
        d2 *= f;
        motionX = d;
        motionY = d1;
        motionZ = d2;
        //float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
        //prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
        //prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f3) * 180D) / 3.1415927410125732D);
    }

    public void setVelocity(double d, double d1, double d2)
    {
        log("setVelocity");
        motionX = d;
        motionY = d1;
        motionZ = d2;
        /*
        if(prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(d * d + d2 * d2);
            prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
            prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f) * 180D) / 3.1415927410125732D);
        }
        */
    }

    public void onUpdate()
    {
        if (ticksExisted > 500) 
        {
            setEntityDead();
            return;
        }
	        
        super.onUpdate();

        helper.applyUpdatePacketFromClient();    

        updateRotation();
        updateVectors();
        
        if(inGround)
        {
            // stuck in block like an arrow after spawn from save file,
            // will expires soon
            
            log("inGround: " + inGround);
            
            int i = worldObj.getBlockId(xTile, yTile, zTile);
            if(i != inTile)
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
            } 
            else
            {
                ticksInGround++;
                log("ticksInGround: " + ticksInGround);
                
                if(ticksInGround == 1200)
                {
                    log("rocket stuck in ground, removing");
                    setEntityDead();
                    worldObj.playSoundAtEntity(this, "random.explode", 1f, 1f);
                }
                return;
            }
        } 
        
        Vec3D vec3d = Vec3D.createVector(posX, posY, posZ);
        Vec3D vec3d1 = Vec3D.createVector(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(vec3d, vec3d1);
        
        if(movingobjectposition != null)
        {
            vec3d1 = Vec3D.createVector(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
        }
        Entity entity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double d = 0.0;
        for(int i1 = 0; i1 < list.size(); i1++)
        {
            Entity entity1 = (Entity)list.get(i1);
            if(!entity1.canBeCollidedWith() || entity1 == owner) // && field_20049_i < 50)
            {
                continue;
            }
            float f4 = 0.3F;
            AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f4, f4, f4);
            MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
            if(movingobjectposition1 == null)
            {
                continue;
            }
            double d1 = vec3d.distanceTo(movingobjectposition1.hitVec);
            if(d1 < d || d == 0.0D)
            {
                entity = entity1;
                d = d1;
            }
        }

        if(entity != null)
        {
            movingobjectposition = new MovingObjectPosition(entity);
        }
        if(movingobjectposition != null)
        {
            log("rocket hit an entity");
            
            // we hit an entity!
            if(movingobjectposition.entityHit != null)
            {
                int attackStrength = 8;
                movingobjectposition.entityHit.attackEntityFrom(new EntityDamageSource("player", owner), attackStrength);
            }
            
            setEntityDead();
            
            return;
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        
        setPosition(posX, posY, posZ);
        
        if(!enteredWater && inWater)
        {
            enteredWater = true;
        }
    }

    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        /*
        nbttagcompound.setShort("xTile", (short)xTile);
        nbttagcompound.setShort("yTile", (short)yTile);
        nbttagcompound.setShort("zTile", (short)zTile);
        nbttagcompound.setByte("inTile", (byte)inTile);
        nbttagcompound.setByte("inGround", (byte)(inGround ? 1 : 0));
        */
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        /*
        xTile = nbttagcompound.getShort("xTile");
        yTile = nbttagcompound.getShort("yTile");
        zTile = nbttagcompound.getShort("zTile");
        inTile = nbttagcompound.getByte("inTile") & 0xff;
        inGround = nbttagcompound.getByte("inGround") == 1;
        */
    }

    public float getShadowSize()
    {
        return 0.0F;
    }

    /* from ISpawnable interface */
    public Packet230ModLoader getSpawnPacket()
    {
        return helper.getSpawnPacket();
    }
}

