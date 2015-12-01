package com.blogspot.programmingheroes.supermariojava;

public class Vector2D
{
    protected double x, y, module, angle;

    public Vector2D()
    {
        this(0, 0);
    }

    public Vector2D(double module) 
    {
        this(module, 0);
    }

    public Vector2D(double module, double angle) 
    {
        this.setModule(module);
	this.setAngle(angle);
    }

    public Vector2D(Vector2D vector) 
    {
        this.module = vector.getModule();
	this.angle = vector.getAngle();
	this.x = vector.getAccurateX();
	this.y = vector.getAccurateY();
    }

    public void setModule(double module) 
    {
        this.module = module;
	updateXY();
    }

    public void setAngle(double angle) 
    {
        this.angle = angle % (2*Math.PI);
	if (this.angle > Math.PI)
        {
            this.angle = -angle % Math.PI;
	} 
        else if (this.angle < -Math.PI) 
        {
            this.angle = 2*Math.PI+angle;
	}
        else if (this.angle < 0) 
        {
            this.angle = angle % Math.PI;
	}
	updateXY();
    }

    public void setAngle(int angle) 
    {
        this.setAngle(Math.toRadians(angle));
    }

    public void setX(double x)
    {
        this.x = x;
	updateModule();
	updateAngle();
    }

    public void setY(double y) 
    {
        this.y = y;
	updateModule();
	updateAngle();
    }
	
    public int getX() 
    {
        return ((int)x);
    }

    public int getY() 
    {
	return ((int)y);
    }

    public double getAccurateX() 
    {
        return x;
    }

    public double getAccurateY()
    {
        return y;
    }

    public double getAngle()
    {
        return angle;
    }

    public double getModule() 
    {
        return module;
    }
	
    public void invertX() 
    {
	if (Math.abs(angle) > Math.PI/2) 
        {
            angle -= Math.PI/2;
	}
        else 
        {
            angle += Math.PI/2;
	}
	x = -x;
    }

    public void invertY() 
    {
	angle = -angle;
	y = -y;
    }

    private void updateXY() 
    {
        x = Math.cos(angle)*module;
	y = Math.sin(angle)*module;
    }

    private void updateModule() 
    {
        module = Math.sqrt(x*x+y*y);
    }

    private void updateAngle() 
    {
        if (x == 0 && y == 0) 
        {
            angle = 0;
	}
        else if (x == 0) 
        {
            angle = (y > 0) ? Math.PI/2: -Math.PI/2;
	}
        else if (y == 0) 
        {
            angle = (x > 0) ? 0 : Math.PI;
	}
        else 
        {
            angle = Math.atan(y/x);
	}
    }
}