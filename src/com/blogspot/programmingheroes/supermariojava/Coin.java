package com.blogspot.programmingheroes.supermariojava;

import java.applet.*;

public class Coin extends WorldObject 
{
    protected static int indexClass = 0;
    protected static boolean changeImg = false;
    public static int LENGHT_IMAGES = 4;
    public static void actClass() 
    {
        if (changeImg) 
        {
            indexClass = (indexClass+1)%LENGHT_IMAGES;
            changeImg = false;
	}
    }

    public static int N_COINS = 0;
    public static int COINS_CATCHED = 0;
    public String imgNormal = "coin*_0";
    public String imgAnimation = "coinEfect*_0";
    public boolean effect = false;
    public static AudioClip[] audio;
    public static int indexAudio;
    public static boolean first = true;

    public Coin(Stage s)
    {
        super(s);
	setPreferredSize(map.tileXSize, map.tileYSize);
	setImages(imgNormal, 0, LENGHT_IMAGES);
        bounds.add(new java.awt.Rectangle(2, 2, width-4, height-4));
	N_COINS++;
	if (first) 
        {
            first = false;
            indexAudio = 0;
            audio = new AudioClip[5];
            for (int i=0; i<audio.length; i++) 
            {
                audio[i] = stage.getSoundsLoader().getAudio("coin.wav", true, true);
            }
	}
    }

    public void act() 
    {
        move();
	if (effect) 
        {
            int frameFrec = (int)(stage.getFPS()/20);
            if (frameFrec==0 || stage.getTotalUpdates()%frameFrec == 0) 
            {
                if (nextImg()) 
                {
                    delete = true;
                    COINS_CATCHED++;
                }
            } 
            else 
            {
                frameFrec = (int)(stage.getFPS()/10);
		if (frameFrec==0 || stage.getTotalUpdates()%frameFrec == 0) 
                {
                    setImage(indexClass);
                    changeImg = true;
		}
            }
	}
    }

    public void collision(Sprite s) 
    {
        if (!effect) 
        {
            stage.getSoundsLoader().play("coin.wav", false);
            audio[indexAudio].play();
            indexAudio = (indexAudio+1)%audio.length;
            setImages(imgAnimation, 0, 7);
            effect = true;
            speed.setY(2);
	}
    }
}