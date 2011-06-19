package net.minecraft.src;

public class ThxModel extends ModelBase
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean visible = true;
    
    long prevTime;
    float deltaTime;

    /**
     *  deltaTime is used to render model movement more smoothly
     */
    public void updateDeltaTime()
    {
	    long time = System.nanoTime();
	    deltaTime = (float) (time - prevTime) / 1000000000f; // convert to sec
	    prevTime = time;
	    
	    // this happens when we haven't rendered in awhile (e.g. out of view)
	    if (deltaTime > .1f) deltaTime = .015f; // default to ~60 fps
    }
}
