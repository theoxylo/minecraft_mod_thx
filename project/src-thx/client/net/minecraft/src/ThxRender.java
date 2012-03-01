package net.minecraft.src;

import java.util.Date;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class ThxRender extends Render
{
    @Override
    public void doRender(Entity entityArg, double x, double y, double z, float yaw, float pitch)
    {
        ThxEntity entity = null;
        
        try
        {
            entity = (ThxEntity) entityArg;
        }
        catch (ClassCastException e)
        {
            return;
        }
        
        GL11.glPushMatrix();
        
        // following translation is relative to player position
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        
        entity.model.paused = entity.paused;
        
        if (entity.prevTime > entity.model.entityPrevTime)
        {
            // entity was updated (usually at ~20 fps)
            entity.model.entityPrevTime = entity.prevTime;
	        
	        entity.model.rotationYaw = entity.rotationYaw;
	        entity.model.rotationYawSpeed = entity.rotationYawSpeed;
	        
	        entity.model.rotationPitch = entity.rotationPitch;
	        entity.model.rotationPitchSpeed = entity.rotationPitchSpeed;
	        
	        entity.model.rotationRoll  = entity.rotationRoll;
	        entity.model.rotationRollSpeed  = entity.rotationRollSpeed;
        }
        else if (entity.model.prevTime - entity.prevTime > 100000000) // .1 sec
        {
            // entity is not updating, game may be paused
	        entity.model.rotationYawSpeed = 0f;
	        entity.model.rotationPitchSpeed = 0f;
	        entity.model.rotationRollSpeed = 0f;
        }
        
        
        int texture = renderManager.renderEngine.getTexture(entity.model.renderTexture);
        renderManager.renderEngine.bindTexture(texture);

        entity.model.render();
        
        GL11.glPopMatrix();
    }
}
