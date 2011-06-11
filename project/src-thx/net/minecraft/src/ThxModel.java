package net.minecraft.src;

public class ThxModel extends ModelBase
{
    final float RAD_PER_DEG = 00.01745329f;
    final float PI          = 03.14159265f;

    boolean visible = true;
    
    long time;
    long prevTime;
    float deltaTime;

    @Override
    public void render(float f, float f1, float f2, float f3, float f4, float f5)
    {
	    time = System.nanoTime();
	    deltaTime = (float) (time - prevTime) / 1000000000f; // convert to sec
	    //dT = deltaTime / .05f; // relative to 20 fps
	    prevTime = time;

	    //System.out.println("delta time sec: " + deltaTime);
	    //System.out.println("dT: " + dT);
    }

}
