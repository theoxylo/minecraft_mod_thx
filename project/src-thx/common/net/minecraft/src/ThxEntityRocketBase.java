package net.minecraft.src;

import java.util.List;
import java.util.Random;

public abstract class ThxEntityRocketBase extends ThxEntity
{
    boolean enteredWater;
    boolean launched;
    
    final float exhaustDelay = .01f;
    float exhaustTimer = 0f;
    
    abstract ThxEntityHelper createHelper();
    
	public ThxEntityRocketBase(World world)
    {
        super(world);
        
        helper = createHelper();
        
        setSize(0.25F, 0.25F);
        
        NET_PACKET_TYPE = 76;
    }

    public ThxEntityRocketBase(Entity owner, double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch)
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

    public void setHeading(double d, double d1, double d2, float f, float f1)
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
    
    @Override
    public boolean canBeCollidedWith()
    {
        return !isDead;
        //return false;
    }
 
    public void onUpdate()
    {
        if (ticksExisted > 500) 
        {
            setDead();
            return;
        }
	        
        super.onUpdate();
        
        if (!launched)
        {
            launched = true;
	        worldObj.playSoundAtEntity(this, "random.fizz", 1f, 1f);
        }
        
        exhaustTimer -= deltaTime;
        if (exhaustTimer < 0f)
        {
            exhaustTimer = exhaustDelay;
            worldObj.spawnParticle("smoke", posX, posY, posZ, 0.0, 0.0, 0.0);
        }
        
        Vec3D posStart = Vec3D.createVector(posX, posY, posZ);
        Vec3D posEnd = Vec3D.createVector(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks(posStart, posEnd);
        
        if(movingobjectposition != null)
        {
            posEnd = Vec3D.createVector(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
        }
        Entity entity = null;
        double closest = .0;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        for(int i1 = 0; i1 < list.size(); i1++)
        {
            Entity nextEntity = (Entity)list.get(i1);
            if (nextEntity == null) continue;
            if(!nextEntity.canBeCollidedWith()) continue;
            if (nextEntity == owner) // && field_20049_i < 50)
            {
                continue;
            }
            float f4 = 0.3F;
            AxisAlignedBB axisalignedbb = nextEntity.boundingBox.expand(f4, f4, f4);
            MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(posStart, posEnd);
            if(movingobjectposition1 == null)
            {
                continue;
            }
            double d1 = posStart.distanceTo(movingobjectposition1.hitVec);
            if (d1 < closest || closest == .0)
            {
                entity = nextEntity; // remember closest entity
                closest = d1;
            }
        }

        if(entity != null)
        {
            movingobjectposition = new MovingObjectPosition(entity);
        }
        if(movingobjectposition != null)
        {
            // we hit an entity!
            if(movingobjectposition.entityHit != null && !worldObj.isRemote)
            {
	            log("rocket hit entity " + movingobjectposition.entityHit);
                if (owner.equals(movingobjectposition.entityHit.riddenByEntity) || owner.equals(movingobjectposition.entityHit)) 
                {
                    log(owner + " ignoring hit from own rocket");
                }
                else
                {
	                int attackStrength = 6;
	                movingobjectposition.entityHit.attackEntityFrom(new EntityDamageSource("player", owner), attackStrength);
                }
            }
            
            // for hit markers
            //worldObj.spawnParticle("flame", posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            worldObj.playSoundAtEntity(this, "random.explode", .7f, .5f + (float) Math.random() * .2f);
            //worldObj.playSoundAtEntity(this, "random.explode", .3f, 1f);

	        int i = MathHelper.floor_double(posX);
	        int j = MathHelper.floor_double(posY - 0.2 -(double)yOffset);
	        int k = MathHelper.floor_double(posZ);
	        int blockId = worldObj.getBlockId(i, j, k);
	        if (blockId > 0)
	        {
		        // kick up some debris if we hit a block, but only works for top surface
	            for (int k1 = 0; k1 < 2; k1++)
	            {
		            worldObj.spawnParticle((new StringBuilder()).append("tilecrack_").append(blockId).toString(), posX + ((double)rand.nextFloat() - 0.5) * (double)width, boundingBox.minY + 0.1, posZ + ((double)rand.nextFloat() - 0.5) * (double)width, 1.0 + ((double)rand.nextFloat() - 0.5),  1.0 + ((double)rand.nextFloat() - 0.5),  1.0 + ((double)rand.nextFloat() - 0.5));
	            }
	        }
	        else
	        {
	            // create some non-block debris instead
	            for (int k1 = 0; k1 < 2; k1++)
	            {
		            //worldObj.spawnParticle("flame", posX, posY, posZ, 0.0, 0.0, 0.0);
		            worldObj.spawnParticle("snowballpoof", posX + ((double)rand.nextFloat() - 0.5), boundingBox.minY + 0.1, posZ + ((double)rand.nextFloat() - 0.5),  1.0 + ((double)rand.nextFloat() - 0.5),  1.0 + ((double)rand.nextFloat() - 0.5),  1.0 + ((double)rand.nextFloat() - 0.5));
	            }
	        }
	        
	        if (!worldObj.isRemote) setDead();
            
            return;
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        
        setPosition(posX, posY, posZ);
        
        if(!enteredWater && inWater)
        {
            enteredWater = true;
            
            worldObj.playSoundAtEntity(this, "random.splash", 1f, 1f);
            for(int l = 0; l < 4; l++)
            {
                float f3 = 0.25F;
                worldObj.spawnParticle("bubble", posX - motionX * (double)f3, posY - motionY * (double)f3, posZ - motionZ * (double)f3, motionX, motionY, motionZ);
            }
        }
    }
}

