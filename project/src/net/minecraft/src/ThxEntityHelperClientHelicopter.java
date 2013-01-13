package net.minecraft.src;

public class ThxEntityHelperClientHelicopter extends ThxEntityHelperClient
{
    ThxEntityHelicopter entity;
    
    ThxModelHelicopterBase model;
    
    ThxEntityHelperClientHelicopter(ThxEntityHelicopter e, ThxModelHelicopterBase m)
    {
        super(e, m);
        
        entity = e;
        model = m;
    }
    
    @Override
    void addChatMessageToPilot(String s)
    {
        // only the pilot player should see this message
        if (!entity.minecraft.thePlayer.equals(entity.riddenByEntity)) return;

        entity.minecraft.ingameGUI.getChatGUI().printChatMessage(s);
    }
    
    @Override
    void updateAnimation()
    {
        if (entity.riddenByEntity != null || entity.targetEntity != null)
        {
	        // adjust according to throttle (0 is idle speed, not stopped)
	        float power = (entity.throttle - entity.THROTTLE_MIN) / (entity.THROTTLE_MAX - entity.THROTTLE_MIN);
	        model.rotorSpeed = power / 2f + .75f;
        }
        else
        {
	        model.rotorSpeed = 0f; // spin down rotor to complete stop on vacant helicopters
        }
    }
    
    @Override
    void pilotExit(Entity pilot)
    {
        entity.log("client helper pilotExit() called");
        
        // place pilot to left of helicopter
        // ? not needed on the client, only on the server, then sync?
        // (use fwd XZ perp to exit left: x = z, z = -x)
        //double exitDist = 1.9;
        //pilot.setPosition(posX + fwd.z * exitDist, posY + pilot.yOffset, posZ - fwd.x * exitDist);
        
        // shut down model/rotor
        model.visible = true;
        model.rotorSpeed = 0f;
        
        // clear rotation speed to prevent judder
        entity.rotationYawSpeed = 0f;
        entity.rotationPitchSpeed = 0f;
        entity.rotationRollSpeed = 0f;        
        
        // we must update the serverPos because it is used when the helicopter is vacant
        /*
        entity.serverPosX = MathHelper.floor_double(entity.posX * 32f);
        entity.serverPosY = MathHelper.floor_double(entity.posY * 32f);
        entity.serverPosZ = MathHelper.floor_double(entity.posZ * 32f);
        */
    }
    
    @Override
    void onUpdateWithPilot()
    {
        if (entity.riddenByEntity.entityId == entity.minecraft.thePlayer.entityId) // player is the client pilot
        {
            entity.onUpdateWithPilotPlayerInput();
            entity.updateMotion(entity.altitudeLock);
	        entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ);
            //entity.setPosition(entity.posX + entity.motionX, entity.posY + entity.motionY, entity.posZ + entity.motionZ);
                
            /*
	        entity.serverPosX = MathHelper.floor_double(entity.posX * 32f);
	        entity.serverPosY = MathHelper.floor_double(entity.posY * 32f);
	        entity.serverPosZ = MathHelper.floor_double(entity.posZ * 32f);
	        */
                
	        entity.sendUpdatePacketFromClient();
        }
        else // pilot is a different player, not using this client
        {
            // already called on client in ThxEntity.onUpdate: 
            //entity.readDataWatcher();
            
            // either of the following approaches causes drift and entity de-sync:
	        //entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ); // causing drift on client
            //entity.setPosition(entity.posX + entity.motionX, entity.posY + entity.motionY, entity.posZ + entity.motionZ);
            
            /*
	        entity.serverPosX = MathHelper.floor_double(entity.posX * 32f);
	        entity.serverPosY = MathHelper.floor_double(entity.posY * 32f);
	        entity.serverPosZ = MathHelper.floor_double(entity.posZ * 32f);
	        */
        }
    }
}
