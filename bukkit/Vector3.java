package net.minecraft.server;

public class Vector3
{
    public float x;
    public float y;
    public float z;

    public Vector3() {}

    public Vector3(float var1, float var2, float var3)
    {
        this.set(var1, var2, var3);
    }

    public void set(float var1, float var2, float var3)
    {
        this.x = var1;
        this.y = var2;
        this.z = var3;
    }

    public float lengthSquared()
    {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public float length()
    {
        return (float)Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z));
    }

    public Vector3 normalize()
    {
        float var1 = this.length();

        if (var1 == 0.0F)
        {
            return this;
        }
        else
        {
            this.x /= var1;
            this.y /= var1;
            this.z /= var1;
            return this;
        }
    }

    public Vector3 scale(float var1)
    {
        this.x *= var1;
        this.y *= var1;
        this.z *= var1;
        return this;
    }

    public Vector3 negate(Vector3 var1)
    {
        if (var1 == null)
        {
            var1 = new Vector3();
        }

        var1.x = -this.x;
        var1.y = -this.y;
        var1.z = -this.z;
        return var1;
    }

    public static Vector3 cross(Vector3 var0, Vector3 var1, Vector3 var2)
    {
        if (var2 == null)
        {
            var2 = new Vector3();
        }

        var2.set(var0.y * var1.z - var0.z * var1.y, var1.x * var0.z - var1.z * var0.x, var0.x * var1.y - var0.y * var1.x);
        return var2;
    }

    public static Vector3 add(Vector3 var0, Vector3 var1, Vector3 var2)
    {
        if (var2 == null)
        {
            var2 = new Vector3();
        }

        var2.set(var0.x + var1.x, var0.y + var1.y, var0.z + var1.z);
        return var2;
    }

    public static Vector3 subtract(Vector3 var0, Vector3 var1, Vector3 var2)
    {
        if (var2 == null)
        {
            var2 = new Vector3();
        }

        var2.set(var0.x - var1.x, var0.y - var1.y, var0.z - var1.z);
        return var2;
    }

    public static float dot(Vector3 var0, Vector3 var1)
    {
        return var0.x * var1.x + var0.y * var1.y + var0.z * var1.z;
    }

    public String toString()
    {
        return "[x " + this.x + ", y " + this.y + ", z " + this.z + "]";
    }
}
