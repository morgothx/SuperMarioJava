package com.blogspot.programmingheroes.supermariojava;

import java.awt.*;
import java.awt.event.*;
import com.blogspot.programmingheroes.supermariojava.loaders.ImagesLoader;
import com.blogspot.programmingheroes.supermariojava.loaders.Map;
import com.blogspot.programmingheroes.supermariojava.loaders.SoundsLoader;

public class Main extends Stage 
{
    private ImagesLoader loader;
    private SoundsLoader sounds;
    private Map map;
    private float gravity = 0.2F;
    Point pointCursor = new Point(-1,-1);

    public Main(boolean applet) 
    {
        super(CANVAS);
	setFPS(80);
	setSize(960, 640);
        map = new Map(this, 1, 1);
        loader = new ImagesLoader("res/img", "loader");
	sounds = new SoundsLoader("res/sounds", "loader");
        setImagesLoader(loader);
	setSoundsLoader(sounds);
    }

    public Main() 
    {
        super(JFRAME);
	setFPS(80);
	setSize(960-6, 640-6);
	window.setResizable(false);
        map = new Map(this, 1, 1);
        loader = new ImagesLoader("res/img", "loader");
	sounds = new SoundsLoader("res/sounds", "loader");
        setImagesLoader(loader);
	setSoundsLoader(sounds);
    }
	
    public synchronized void initStage() 
    {
        loader.startLoader();
	sounds.startLoader();
        map.initMap();
        Mario m = new Mario(this);
	map.addPlayer(m);
    }

    public synchronized void updateStage() 
    {
        map.act();
	if (!gameOver && Coin.N_COINS == Coin.COINS_CATCHED) 
        {
            gameOver();
            final Stage s = this;
            new Thread(new Runnable() 
            {
                public void run() 
                {
                    try 
                    {
                        Thread.sleep(2000);
                    } 
                    catch (Exception e) {}
                    Coin.N_COINS = 0;
                    gameOver = false;
                    map.nextLevel();
                    map.addPlayer(new Mario(s));
                    Coin.COINS_CATCHED = 0;
		}
            }).start();
	}
    }

    public synchronized void renderStage(Graphics g) 
    {
        g.setColor(Color.BLACK);
	g.fillRect(0,0,WIDTH,HEIGHT);
	map.paint(g);
        if (gameOver) 
        {
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.WHITE);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
            g2.drawString("Fin del juego", WIDTH/2-100, HEIGHT/2-10);
	}
    }

    public synchronized void mouseMoved(MouseEvent e){}

    public void keyPressed(KeyEvent e) 
    {
        for (int i=0; i<map.players.size(); i++) 
        {
            map.players.get(i).keyPressed(e);
	}
    }

    public void keyReleased(KeyEvent e) 
    {
        for (int i=0; i<map.players.size(); i++) 
        {
            map.players.get(i).keyReleased(e);
	}
    }

    public void mousePressed(MouseEvent e){}

    public float getGravity()
    {
        return gravity;
    }

    public Map getCurrentMap() 
    {
        return map;
    }

    public static void main(String[] args) 
    {
        Main p = new Main();
	p.getWindow().setVisible(true);
	p.startGame();
    }
}