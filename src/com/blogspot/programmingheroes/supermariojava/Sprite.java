package com.blogspot.programmingheroes.supermariojava;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Sprite 
{
    public static boolean drawBounds = false;
    protected float x, y;
    protected int width, height;
    protected int z;
    protected ArrayList<Rectangle> bounds;
    protected Vector2D speed;
    protected String[] imgNames;
    protected BufferedImage img;
    protected int imgIndex;
    protected Stage stage;
    protected boolean visible;
    protected boolean active;
    protected boolean preferredSize;
    protected boolean delete;

    public Sprite(Stage s) 
    {
        stage = s;
	x = 0;
	y = 0;
	z = 0;
	visible = true;
	active = true;
	preferredSize = false;
	delete = false;
	imgIndex = 0;
	speed = new Vector2D();
	bounds = new ArrayList<Rectangle>();
    }

    public void move() 
    {
        x += speed.getAccurateX();
	y -= speed.getAccurateY();
    }

    public void moveX() 
    {
        x += speed.getAccurateX();
    }

    public void moveY() 
    {
        y -= speed.getAccurateY();
    }

    public void setImages(String[] imgNames) 
    {
        setImages(imgNames, 0);
    }

    public void setImages(String[] imgNames, int initIndex) 
    {
        this.imgNames = imgNames;
	imgIndex = initIndex;
	this.setImage(imgNames[initIndex]);
    }

    public boolean nextImg()
    {
        imgIndex = (imgIndex+1)%imgNames.length;
	this.setImage(imgNames[imgIndex]);
        if (imgIndex == imgNames.length-1) 
            return true;
        
	return false;
    }

    public boolean previousImg() 
    {
	imgIndex = (imgIndex-1)%imgNames.length;
	this.setImage(imgNames[imgIndex]);
        if (imgIndex == 0) 
            return true;
        
	return false;
    }

    public boolean setImage(String name) 
    {
        return setImage(stage.getImagesLoader().getImage(name));
    }

    public boolean setImage(int index) 
    {
        return setImage(imgNames[index]);
    }

    public boolean setImages(String name, int n1, int n2)
    {
        int wildcard = name.indexOf("*");
	if (wildcard != -1 && n2 > 1)
        {
            imgNames = new String[n2];
            int i = 0;
            String fullName;
            while (i<n2) 
            {
                fullName = name.substring(0,wildcard)+(n1+i) + name.substring(wildcard+1);
		imgNames[i++] = fullName;
            }
            setImages(imgNames);
            return true;
	} 
        else 
            return setImage(name);
    }

    public boolean setImage(BufferedImage img) 
    {
        this.img = img;
	if (img == null || !active) 
            return false;
        
        if (!preferredSize) 
        {
            width = img.getWidth();
            height = img.getHeight();
	}
	return true;
    }

    public void setPreferredSize(int w, int h) 
    {
        preferredSize = (w <= 0 && h <= 0)?false:true;
	if (preferredSize) 
        {
            width = w;
            height = h;
	} 
        else 
        {
            width = img.getWidth();
            height = img.getHeight();
	}
    }

    public void paint(Graphics g) 
    {
        if (visible) 
            g.drawImage(img, (int)x, (int)y, width, height, null);
    }

    public void paint(Graphics g, double x, double y) 
    {
        if (visible) 
            g.drawImage(img, (int)x, (int)y, width, height, null);
    }

    public void paint(Graphics g, double x, double y,int w, int h) 
    {
        if (visible) 
        {
            g.drawImage(img, (int)x, (int)y, w, h, null);
            if (drawBounds) 
            {
                g.setColor(Color.RED);
		for (int i=0; i<bounds.size(); i++) 
                {
                    Rectangle r = bounds.get(i);
                    g.drawRect((int)(x+r.getX()), (int)(y+r.getY()),
                    (int)(r.getWidth()), (int)(r.getHeight()));
		}
            }
	}
    }

    public void act(){}

    public void collision(Sprite o){}

    public boolean collidesWith(Sprite o, boolean imgBounds) 
    {
        if (z == o.getZ()) 
        {
            if (imgBounds) 
            {
                Rectangle r1 = new Rectangle((int)x, (int)y, width, height);
		Rectangle r2 = new Rectangle((int)o.getX(), (int)o.getY(),o.getWidth(), o.getHeight());
		return r1.intersects(r2);
            }
            else 
            {
                for (int j=0; j<o.getBounds().size(); j++) 
                {
                    Rectangle r = o.getBounds().get(j);
                    if (collidesWith(new Rectangle((int)(o.getX()+r.getX()), (int)(o.getY()+r.getY()),(int)(r.getWidth()), (int)(r.getHeight())), false)) 
                        return true;
		}
            }
	}
	return false;
    }

    public boolean collidesWith(Rectangle r, boolean imgBounds) 
    {
        if (imgBounds) 
        {
            Rectangle bound = new Rectangle((int)x, (int)y, width, height);
            if (r.intersects(bound)) 
                return true;
	}
        else 
        {
            for (int i=0; i<bounds.size(); i++) 
            {
                Rectangle bound = bounds.get(i);
		if (new Rectangle((int)(x+bound.getX()),
		(int)(y+bound.getY()), (int)(bound.getWidth()),
		(int)(bound.getHeight())).intersects(r)) 
                {
                    return true;
                }
            }
        }
	return false;
    }

    public boolean collidesWith(Point p, boolean imgBounds) 
    {
        if (imgBounds) 
        {
            Rectangle bound = new Rectangle((int)x, (int)y, width, height);
            if (bound.contains(p)) 
                return true;
        } 
        else 
        {
            for (int i=0; i<bounds.size(); i++) 
            {
                Rectangle bound = bounds.get(i);
                if (new Rectangle((int)(x+bound.getX()),
                (int)(y+bound.getY()), (int)(bound.getWidth()),
                (int)(bound.getHeight())).contains(p)) 
                {
                    return true;
                }
            }
        }
        return false;
    }

    int jope = 0;
    public boolean collideTop(Sprite s) 
    {
        if (Math.abs(s.getY()+s.getHeight()-y) <= Math.abs(speed.getAccurateY()+s.getSpeed().getAccurateY()+1)) 
            return true;
		
        return false;
    }

    public boolean collideBottom(Sprite s) 
    {
        if (Math.abs(s.getY()-y-height) <= Math.abs(speed.getAccurateY()+s.getSpeed().getAccurateY())) 
            return true;
        
        return false;
    }

    public boolean collideRight(Sprite s) 
    {
        if (Math.abs(x+width-s.getX()) <= Math.abs(speed.getAccurateY()+s.getSpeed().getAccurateY())) 
            return true;
    
        return false;
    }

    public boolean collideLeft(Sprite s) 
    {
        if (Math.abs(s.getX()+s.getWidth()-x) <= Math.abs(speed.getAccurateY()+s.getSpeed().getAccurateY()))
            return true;

        return false;
    }

    public void setX(float x) 
    {
        this.x = x;
    }

    public void setY(float y) 
    {
        this.y = y;
    }

    public void setVisible(boolean visible) 
    {
        this.visible = visible;
    }

    public void setPosition(Point p) 
    {
        this.x = Float.parseFloat(""+p.getX());
	this.y = Float.parseFloat(""+p.getY());
	System.out.println(x+" "+y);
    }
    
    public float getX() 
    {
        return x;
    }

    public float getY() 
    {
        return y;
    }

    public float getZ() 
    {
        return z;
    }

    public int getWidth() 
    {
        return width;
    }

    public int getHeight() 
    {
        return height;
    }
	
    public BufferedImage getImage() 
    {
        return img;
    }
	
    public ArrayList<Rectangle> getBounds() 
    {
        return bounds;
    }
	
    public Vector2D getSpeed() 
    {
        return speed;
    }

    int c = 13;
    int gap = 6;

    public Rectangle getFoot() 
    {
        return new Rectangle((int)(x+gap), (int)(y+height-1), width-gap*2, c);
    }

    public Rectangle getHead() 
    {
        return new Rectangle((int)(x+gap), (int)(y-1), width-gap*2, c);
    }

    public Rectangle getLeft() 
    {
        return new Rectangle((int)(x), (int)(y+gap), c, height-gap*2);
    }

    public Rectangle getRight() 
    {
        return new Rectangle((int)(x+width-c), (int)(y+gap),c, height-gap*2);
    }

    public boolean isToDelete() 
    {
        return delete;
    }

    public boolean isVisible() 
    {
        return visible;
    }
}