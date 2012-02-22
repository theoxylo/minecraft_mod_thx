package net.minecraft.src;

import java.util.List;


public class ThxEntityHelicopter extends ThxEntity implements IClientDriven
{
    final float MAX_HEALTH = 100;

    final float GRAVITY      = 0.301f;
    final float MAX_VELOCITY = 0.44f;
    final float FRICTION = 0.98f;

    final float PITCH_RETURN = 0.98f;
    final float ROLL_RETURN = 0.92f;

    // amount of vehicle motion to transfer upon projectile launch
    final float MOMENTUM = .2f;

    // total update count
    float timeSinceAttacked;
    float timeSinceCollided;
    
    int rocketCount;
    
    public ThxEntityHelicopter(World world)
    {
        super(world);

        setSize(1.8f, 2f);

        yOffset = .6f;
        
        NET_PACKET_TYPE = 75;
        
        log("C1 - ThxEntityHelicopter() with world: " + world.getWorldInfo());
    }

    public ThxEntityHelicopter(World world, double x, double y, double z, float yaw)
    {
        this(world);
        
        setPositionAndRotation(x, y + yOffset, z, yaw, 0f);
        
        log("C2 - posX: " + posX + ", posY: " + posY + ", posZ: " + posZ + ", yaw: " + yaw);
    }

    @Override
    public void onUpdate()
    {
        
        // adjust position height to avoid collisions
        List list = worldObj.getCollidingBoundingBoxes(this, boundingBox.contract(0.03125D, 0.0D, 0.03125D));
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
        

        super.onUpdate(); // ThxEntity.onUpdate will apply latest client packet if there is one
        
        // decrement cooldown timers
        timeSinceAttacked -= deltaTime;
        timeSinceCollided -= deltaTime;
        
        if (riddenByEntity != null)
        {
            // entity updates will come from client for player pilot
            
            if (riddenByEntity.isDead) riddenByEntity.mountEntity(this);
            
	        moveEntity(motionX, motionY, motionZ);
	        handleCollisions();
	        
	        // fire weapons and clear flags
            if (fire1 > 0)
            {
                fire1 = 0;
                fireRocket();
            }
            if (fire2 > 0)
            {
                fire2 = 0;
                fireMissile();
            }
	        
            return;
        }
        
        // for auto-heal unattended, otherwise damage set by pilot client
        if (damage > 0f) damage -= deltaTime; // heal rate: 1 pt / sec

        onUpdateVacant();
            
        moveEntity(motionX, motionY, motionZ);
        handleCollisions();
    }
    
    protected void onUpdateVacant()
    {
        throttle *= .6; // quickly zero throttle
        
        if (onGround || isInWater())
        {
            if (Math.abs(rotationPitch) > .1f) rotationPitch *= .70f;
            if (Math.abs(rotationRoll) > .1f) rotationRoll *= .70f; // very little lateral
                
            // tend to stay put on ground
            motionY = 0.;
            motionX *= .7;
            motionZ *= .7;
                
            rotationYawSpeed = 0f;
        }
        else
        {
            // settle back to ground naturally if pilot bails
                
            rotationPitch *= PITCH_RETURN;
            rotationRoll *= ROLL_RETURN;
                
            motionX *= FRICTION;
            motionY -= GRAVITY * .16f * deltaTime / .05f;
            motionZ *= FRICTION;
        }
    }
    
    
    private void handleCollisions()
    {
        /*
        detectCollisionsAndBounce:
        {
            List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(0.2, 0.2 0.2));
            if (list != null && list.size() > 0)
            {
                for (int j1 = 0; j1 < list.size(); j1++)
                {
                    Entity entity = (Entity) list.get(j1);
                    if (entity != riddenByEntity && entity.canBePushed())
                    {
                        entity.applyEntityCollision(this);
                    }
                }
            }
        }
        */
        
        // crash, take damage and slow down
        if (isCollidedHorizontally || isCollidedVertically)
        {
	        double velSq = motionX * motionX + motionY * motionY + motionZ * motionZ;
            
	        //if (velSq > .03 && timeSinceCollided  < 0f)
	        if (velSq > .005 && timeSinceCollided  < 0f)
	        {
	            log("crash velSq: " + velSq);
                
                timeSinceCollided = 1f; // sec delay before another collision possible
	            
                takeDamage((float) velSq * 100f); // crash damage based on velocity
                
	            motionX *= .7;
	            motionY *= .7;
	            motionZ *= .7;
	        }
            //isCollidedHorizontally = false;
            //isCollidedVertically = false;
        }

    }
    
    private void takeDamage(float damage)
    {
        damage += damage;
                
        /*
        if (riddenByEntity != null) // this is the player's helicopter, so show damage msg
        {
            Minecraft minecraft = ModLoader.getMinecraftInstance();
            minecraft.ingameGUI.addChatMessage("Damage: " + (int) (damage * 100 / MAX_HEALTH) + "%");
        }
        */
        
        if (damage > MAX_HEALTH && !worldObj.isRemote) // helicopter destroyed!
        {
            // show message if not player helicopter
            if (riddenByEntity != null)
            {
                riddenByEntity.mountEntity(this);
            }
            
            boolean flaming = true;
            worldObj.newExplosion(this, posX, posY, posZ, 2.3f, flaming);
            
            // TEST drop item if helicopter is destroyed by crashes or attacks
	        dropItemWithOffset(ThxItemHelicopter.shiftedId, 1, 0); // will it be destroy or launched if placed after explosion?

            setEntityDead();
            
		    /*
	        spawnParticles: // does server spawn particles?
	        {
	            double d13 = Math.cos(((double) rotationYaw * 3.1415926535897931D) / 180D);
	            double d15 = Math.sin(((double) rotationYaw * 3.1415926535897931D) / 180D);
	            for (int i1 = 0; (double) i1 < 1.2 * 60D; i1++)
	            {
	                double d18 = rand.nextFloat() * 2.0F - 1.0F;
	                double d20 = (double) (rand.nextInt(2) * 2 - 1) * 0.69999999999999996D;
	                if (rand.nextBoolean())
	                {
	                    double d21 = (posX - d13 * d18 * 0.80000000000000004D) + d15 * d20;
	                    double d23 = posZ - d15 * d18 * 0.80000000000000004D - d13 * d20;
	                    worldObj.spawnParticle("smoke", d21, posY - 0.125D, d23, motionX, motionY, motionZ);
	                }
	                else
	                {
	                    double d22 = posX + d13 + d15 * d18 * 0.69999999999999996D;
	                    double d24 = (posZ + d15) - d13 * d18 * 0.69999999999999996D;
	                    worldObj.spawnParticle("explode", d22, posY - 0.125D, d24, motionX, motionY, motionZ);
	                }
	            }
	        }
		    */
        }
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource damageSource, int i)
    {
        log("attackEntityFrom called");

        if (timeSinceAttacked > 0f || isDead) return false;
        
        if (damageSource == null) return false; // when is this the case?
        
        Entity attackingEntity = damageSource.getEntity();
      
        if (attackingEntity == null) return false; // when is this the case?
        if (attackingEntity.equals(this)) return false; // ignore damage from self
        if (attackingEntity.equals(riddenByEntity)) return false; // ignore damage from pilot
	        
        log("attacked by entity: " + attackingEntity);
        
        takeDamage((float) i * 3f);
        
        timeSinceAttacked = .5f; // sec delay before this entity can be attacked again
        
        setBeenAttacked(); // this will cause Entity.velocityChanged to be true, so additional Packet28
        
        return true; // the hit landed
    }

    @Override
    public boolean canBeCollidedWith()
    {
        // log("canBeCollidedWith called");
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
        log("EntityThxHelicopter entityInit called");
        
        // reload properties to pick up any changes
        //ThxConfig.loadProperties();

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
        
        if (riddenByEntity != null)
        {
            // already ridden by some other entity
            return false;
        }
        
        if (player.ridingEntity != null) 
        {
            // player is already riding some other entity
            return false;
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

    @Override
    public void updateRiderPosition()
    {
        if (riddenByEntity == null) return;

        // this will tell the default impl in Entity.updateRidden()
        // that no adjustment need be made to the pilot's yaw or pitch
        // as a direct result of riding this helicopter entity.
        // rather, we let the player rotate the pilot and the helicopter follows
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        
        // use fwd XZ components to adjust front/back position of pilot based on helicopter pitch
        // when in 1st-person mode to improve view
        double posAdjust = 0; //-.1 + .02f * rotationPitch;

        //if (ModLoader.getMinecraftInstance().gameSettings.thirdPersonView != 0) posAdjust = 0.0;

        // to force camera to follow helicopter exactly, but stutters:
        //pilot.setPositionAndRotation(posX + fwd.x * posAdjust, posY + pilot.getYOffset() + getMountedYOffset(), posZ + fwd.z * posAdjust, rotationYaw, rotationPitch);
        //pilot.setLocationAndAngles(posX + fwd.x * posAdjust, posY -.7f, posZ + fwd.z * posAdjust, rotationYaw, rotationPitch);
        
        riddenByEntity.setPosition(posX + fwd.x * posAdjust, posY + riddenByEntity.getYOffset() + getMountedYOffset(), posZ + fwd.z * posAdjust);
    }

    private void pilotExit()
    {
        log("pilotExit()");

        log("pilotExit called for pilot " + riddenByEntity + " " + getPilotId());
        
        if (riddenByEntity == null) return;
        
        //model.visible = true; // hard to find otherwise!
        
        // clear pitch speed to prevent judder
        rotationPitchSpeed = 0f;
        
        // place pilot to left of helicopter
        // (use fwd XZ perp to exit left: x = z, z = -x)
        double exitDist = 2.9;
        riddenByEntity.setPosition(posX + fwd.z * exitDist, posY + riddenByEntity.getYOffset(), posZ - fwd.x * exitDist);
        
        log("pilotExit() calling mountEntity on player " + riddenByEntity);
        riddenByEntity.mountEntity(this); // riddenByEntity is now null
    }
 
    static class Keyboard // no-op replacement for client-side org.lwjgl.input.Keyboard
    {
	    public static boolean isKeyDown(int key)
	    {
	        return false;
	    }
    }
    
    private void fireRocket()
    {
        rocketCount++;
                
        float leftRightAmount = .6f;
        float leftRight = (rocketCount % 2 == 0) ? leftRightAmount  : -leftRightAmount;
                
        // starting position of rocket relative to helicopter, out in front quite a bit to avoid collision
        float offsetX = (side.x * leftRight) + (fwd.x * 2f) + (up.x * -.8f);
        float offsetY = (side.y * leftRight) + (fwd.y * 2f) + (up.y * -.8f);
        float offsetZ = (side.z * leftRight) + (fwd.z * 2f) + (up.z * -.8f);
                    
        float yaw = rotationYaw;
        float pitch = rotationPitch; // + 5f; // slight downward from helicopter pitch
                
        ThxEntityRocket newRocket = new ThxEntityRocket(this, posX + offsetX, posY + offsetY, posZ + offsetZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        worldObj.spawnEntityInWorld(newRocket);
    }
    
    private void fireMissile()
    {
        float offX = fwd.x * 2f;
        float offY = fwd.y * 2f;
        float offZ = fwd.z * 2f;

        // aim with cursor if pilot
        float yaw = riddenByEntity != null ? riddenByEntity.rotationYaw : rotationYaw;
        float pitch = riddenByEntity != null ? riddenByEntity.rotationPitch : rotationPitch;
                
        ThxEntityMissile newMissile = new ThxEntityMissile(worldObj, posX + offX, posY + offY, posZ + offZ, motionX * MOMENTUM, motionY * MOMENTUM, motionZ * MOMENTUM, yaw, pitch);
        worldObj.spawnEntityInWorld(newMissile);
    }
}
