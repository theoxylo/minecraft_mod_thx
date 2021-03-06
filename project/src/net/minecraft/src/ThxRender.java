package net.minecraft.src;

import org.lwjgl.opengl.GL11;

public class ThxRender extends Render
{
    @Override
    protected ResourceLocation func_110775_a(Entity arg) { 
        System.out.println("******************* ThxRender.func_110775 called");
        return null; 
    } // new in 1.6.1

    @Override
    public void doRender(Entity entityArg, double x, double y, double z, float yaw, float deltaTime)
    {
        ThxEntity entity = (ThxEntity) entityArg;
        
        ThxModel model = (ThxModel) entity.sidedHelper.getModel();
        
        GL11.glPushMatrix();
        
        // following translation is relative to player position
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        
        model.paused = entity.sidedHelper.isPaused();
        
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
        
        //loadTexture(model.renderTexture);
        //func_110776_a/*loadTexture*/(new ResourceLocation(model.renderTexture)); // new in 1.6.1
        func_110776_a/*loadTexture*/(new ResourceLocation(model.renderTexture)); // new in 1.6.1

        model.render();
        
        GL11.glPopMatrix();
    }
}
