package net.minecraft.src;

import java.util.Date;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class ThxRender extends Render
{
    @Override
    public void doRender(Entity entityArg, double x, double y, double z, float yaw, float deltaTime)
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
        
        ThxModel model = (ThxModel) entity.helper.model;
        
        GL11.glPushMatrix();
        
        // following translation is relative to player position
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        
        model.paused = entity.helper.isPaused();
        
        if (entity.prevTime > model.entityPrevTime)
        {
            // entity was updated (usually at ~20 fps)
            model.entityPrevTime = entity.prevTime;
	        
	        model.rotationYaw = entity.rotationYaw;
	        model.rotationYawSpeed = entity.rotationYawSpeed;
	        
	        model.rotationPitch = entity.rotationPitch;
	        model.rotationPitchSpeed = entity.rotationPitchSpeed;
	        
	        model.rotationRoll  = entity.rotationRoll;
	        model.rotationRollSpeed  = entity.rotationRollSpeed;
        }
        else if (model.prevTime - entity.prevTime > 100000000) // .1 sec
        {
            // entity is not updating, game may be paused
	        model.rotationYawSpeed = 0f;
	        model.rotationPitchSpeed = 0f;
	        model.rotationRollSpeed = 0f;
        }
        
        
        int texture = renderManager.renderEngine.getTexture(model.renderTexture);
        renderManager.renderEngine.bindTexture(texture);

        model.render();
        
        GL11.glPopMatrix();
    }
}
