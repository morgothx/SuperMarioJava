package com.blogspot.programmingheroes.supermariojava;

import com.blogspot.programmingheroes.supermariojava.loaders.Map;

public class WorldObject extends Sprite 
{
    public static final int NOT_FLOOR = 1000000000;
    protected boolean supportsPlayer;
    protected boolean supportsEnemige;
    protected Map map;
    
    public WorldObject(Stage s) 
    {
        super(s);
	map = ((Main)s).getCurrentMap();
	supportsPlayer = supportsEnemige = false;
    }

    public int getFloor() 
    {
        if (supportsPlayer || supportsEnemige) 
        {
            return (int)y;
	}
	return NOT_FLOOR;
    }

    public void setSupportPlayer(boolean support) 
    {
        this.supportsPlayer = false;
    }

    public boolean supportsPlayer() 
    {
        return supportsPlayer;
    }
}