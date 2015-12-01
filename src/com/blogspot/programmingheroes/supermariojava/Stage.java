package com.blogspot.programmingheroes.supermariojava;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import com.blogspot.programmingheroes.supermariojava.loaders.ImagesLoader;
import com.blogspot.programmingheroes.supermariojava.loaders.SoundsLoader;

public abstract class Stage implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener, ComponentListener, WindowStateListener, WindowFocusListener, WindowListener 
{
    protected static int WIDTH = 500;
    protected static int HEIGHT = 400;
    protected Thread animator;
    protected volatile boolean running = false;
    protected volatile boolean gameOver = false;
    protected volatile boolean pause = false;
    protected BufferStrategy bs;
    protected static int NO_SLEEPS_FOR_YIELD = 15;
    protected static int MAX_FRAME_SKIPS = 8;
    protected static int FPS;
    protected static long period;
    protected long totalFrames = 0;
    protected long totalUpdates = 0;
    protected long totalSleepTime = 0;
    protected long initTime;
    protected long playedTime;
    protected long pausedTime;
    protected ImagesLoader imgLoader;
    protected ImagesEffects imgEffects;
    protected SoundsLoader soundsLoader;
    public static final int FSEM = 0;
    public static final int AFS = 1;
    public static final int UFS = 2;
    public static final int JFRAME = 3;
    protected JFrame window;
    public static final int CANVAS = 4;
    protected Canvas canvas;
    public static final int JPANEL = 5;
    protected JPanel panel;
    protected int mode;
    protected Component component; 
    protected GraphicsEnvironment ge;
    protected GraphicsDevice screenDevice;
    protected DisplayMode defaultDisplay;

    public Stage(int mode) 
    {
        initTime=System.currentTimeMillis();
	setFPS(80);
	ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	screenDevice = ge.getDefaultScreenDevice();
	switchMode(mode);
    }

    public void switchMode(int mode) 
    {
        this.mode = mode;
	switch(mode) 
        {
            case FSEM: initFSEM(); break;
            case AFS: initAFS(); break;
            case UFS: initUFS(); break;
            case JFRAME: initJFrame(); break;
            case CANVAS: initCanvas(); break;
            case JPANEL: initJPanel(); break;
            default: throw new IllegalArgumentException("The mode of the Stage is invalid.");
	}
		
        component.addMouseListener(this);
        component.setFocusable(true);
        component.requestFocus();
	component.addKeyListener(this);
        component.addMouseWheelListener(this);
        component.addComponentListener(this);
        component.addFocusListener(this);
        component.addMouseMotionListener(this);
    }

    public void initFSEM() 
    {
        if(!screenDevice.isFullScreenSupported())
            throw new IllegalArgumentException("No FSEM supported.");
	
        window = new JFrame("GameFrame");
        window.addWindowFocusListener(this);
	window.addWindowListener(this);
	window.addWindowStateListener(this);
	window.setUndecorated(true);
	window.setIgnoreRepaint(true);
	window.setResizable(false);
		
	try 
        {
            screenDevice.setFullScreenWindow(window);
            Toolkit tk = Toolkit.getDefaultToolkit();
            WIDTH = (int)tk.getScreenSize().getWidth();
            HEIGHT = (int)tk.getScreenSize().getHeight();
	} 
        catch(Exception e) 
        {
            System.err.println("Error with setting FSEM.");
            e.printStackTrace();
            screenDevice.setFullScreenWindow(null);
	}
        
        try 
        {
            EventQueue.invokeAndWait( new Runnable() 
            {
                public void run() 
                {
                    window.createBufferStrategy(2);
		}
            });
	} 
        catch(Exception e) 
        {
            System.out.println("Error while creating buffer" +" strategy (FSEM).");
            e.printStackTrace();
	}
		
	try 
        { 
            Thread.sleep(0);
	}
        catch(InterruptedException ex) {}
        bs = window.getBufferStrategy();
        component = window;
    }
	
    public void initAFS() 
    {
        initJFrame();
        Toolkit tk = Toolkit.getDefaultToolkit( );
	Dimension d = tk.getScreenSize();
	window.setSize(d);
	window.setResizable(false);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
	window.addComponentListener(this);
    }

    public void initUFS() 
    {
        initJFrame();
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setUndecorated(true);
	WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
   	HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    public void initCanvas() 
    {
        canvas = new Canvas() 
        {
            private static final long serialVersionUID = 1L;
            public void addNotify() 
            {
                super.addNotify();
		canvas.createBufferStrategy(2);
		bs = canvas.getBufferStrategy();
            }
	};
	canvas.setIgnoreRepaint(true);
	try 
        {
            Thread.sleep(1000);
        } 
        catch(InterruptedException ex) {}
        component = canvas;
    }

    public void initJFrame() 
    {
        initCanvas();
        window = new JFrame("GameFrame");
	window.addWindowFocusListener(this);
	window.addWindowListener(this);
	window.addWindowStateListener(this);
	window.setIgnoreRepaint(true);
	window.getContentPane().add(canvas);
    }

    public void initJPanel() 
    {
        panel = new JPanel() 
        {
            private static final long serialVersionUID = 1L;
            public void addNotify() 
            {
                super.addNotify();
            }
        };
	panel.setIgnoreRepaint(true);
        component = panel;
    }

    public void startGame() 
    {
        if(animator!=null || !running) 
        {
            animator = new Thread(this);
            animator.start();
	}
    }

    public void stopGame() 
    {
        running = false;
    }

    public void run() 
    {
        if(bs==null) throw new NullPointerException("Buffer"+"Stragegy is null.");
	long beforeTime, afterTime, diff, sleepTime;
	long extraSleepTime = 0L, excessTime = 0L;
	int noSleeps = 0;
	initStage();
	initTime = playedTime = System.nanoTime();
        running = true;
	while(running) 
        {
            beforeTime = System.nanoTime();
            int skips = 0;
            while(skips<MAX_FRAME_SKIPS && excessTime>period) 
            {
                excessTime -= period;
		updateStage();
		skips++;
		totalUpdates++;
            }
            updateStage(); 
            updateScreen();
            afterTime = System.nanoTime();
            diff = afterTime - beforeTime;
            sleepTime = (period - diff) - extraSleepTime;
            if(sleepTime>0) 
            {
                totalSleepTime += sleepTime;
                try 
                {
                    Thread.sleep(sleepTime/1000000L);
		} 
                catch(InterruptedException e) 
                {
                    System.err.println(e.getMessage());
                }
		extraSleepTime = System.nanoTime()-afterTime-sleepTime;
            }
            else 
            {
                excessTime -= sleepTime;
                extraSleepTime = 0L;
		if (++noSleeps>=NO_SLEEPS_FOR_YIELD) 
                {
                    Thread.yield();
                    noSleeps = 0;
		}
            }
	}
        showPerformance();
        if (mode==FSEM) closeFSEM();
	System.exit(0);
    }

    public void updateScreen() 
    {
        ++totalFrames; ++totalUpdates;
	try 
        {
            Graphics g = bs.getDrawGraphics();
            renderStage(g);
            if (bs.contentsLost()) 
            {
                System.out.println("Contents of the buffer are lose.");
            }
            else 
            {
                bs.show();
            }
            g.dispose();
            Toolkit.getDefaultToolkit().sync();
	} 
        catch (Exception e) 
        {
            e.printStackTrace();
	}
    }

    public void showPerformance() 
    {
        double totalTime = (System.nanoTime()-initTime)/1000000000.0;
	System.out.println("------------------------------");
	System.out.println("Total time: "+totalTime+"\nFPS: "+FPS+"  Period: "+period+"\nTotal frames: "+totalFrames+"\nAverage FPS: "+((float)totalFrames/totalTime)+"\nTotal updates: "+totalUpdates+"\n"+"Average UPS: "+((float)totalUpdates/totalTime)+"\nSleepTime: "+totalSleepTime+"\nAverage SleepTime: "+((float)totalSleepTime/period));
    }

    public abstract void updateStage();
    public abstract void renderStage(Graphics g);
    public abstract void initStage();
    
    public void gameOver() 
    {
        gameOver = true;
    }
    
    public void closeFSEM() 
    {
        try 
        {
            window.dispose();
            screenDevice.setFullScreenWindow(null);
	} 
        catch (Exception ex) 
        {
            System.err.println("Error closing the application.");
            ex.printStackTrace();
	}
    }

    public void exit() 
    {
        running = false;
    }

    public void setPause(boolean p) 
    {
        pause = p;
    }

    public void setWindowVisible(boolean v) 
    {
        if(window!=null)
	window.setVisible(v);
    }

    public void setImagesLoader(ImagesLoader il) 
    {
        this.imgLoader = il;
	this.imgEffects = new ImagesEffects(il);
    }

    public ImagesLoader getImagesLoader() 
    {
        return imgLoader;
    }

    public ImagesEffects getImagesEffects() 
    {
        return imgEffects;
    }

    public void setSoundsLoader(SoundsLoader sl) 
    {
        this.soundsLoader = sl;
    }

    public SoundsLoader getSoundsLoader() 
    {
        return soundsLoader;
    }

    public JFrame getWindow() 
    {
        return window;
    }

    public long getTimeRunning() 
    {
        return System.nanoTime()-initTime;
    }

    public long getTimePlayed() 
    {
        return System.nanoTime()-playedTime;
    }

    public Component getComponent() 
    {
        return component;
    }

    public int getFPS() 
    {
        return FPS;
    }

    public long getTotalUpdates() 
    {
        return totalUpdates;
    }

    public long getTotalFrames() 
    {
        return totalFrames;
    }

    public int getWidth() 
    {
        return WIDTH;
    }

    public int getHeight() 
    {
        return HEIGHT;
    }

    public boolean isPause() 
    {
        return pause;
    }

    public boolean isGameOver() 
    {
        return gameOver;
    }

    public boolean isRunning() 
    {
        return running;
    }

    public void setFPS(int fps) 
    {
        FPS = fps;
        period = 1000000000L/FPS;
    }

    public void updateSize() 
    {
        WIDTH = component.getWidth();
	HEIGHT = component.getHeight();
    }
	
    public void setSize(int w, int h) 
    {
        component.setPreferredSize(new Dimension(w, h));
        if (mode==JFRAME) 
            window.pack();
	else 
            updateSize();
    }

    public boolean isDisplayModeAvailable(DisplayMode d) 
    {
        DisplayMode[] dm = screenDevice.getDisplayModes();
	for(int i=0; i<dm.length; i++)
            if( dm[i].getWidth()==d.getWidth() && dm[i].getHeight()==d.getHeight() && dm[i].getBitDepth()==d.getBitDepth() && dm[i].getRefreshRate()==d.getRefreshRate() )
                return true;
		
        return false;
    }

    public boolean setDisplayMode(DisplayMode dm) 
    {
        if(mode!=FSEM) 
            return false;
	
        if(!screenDevice.isDisplayChangeSupported() && isDisplayModeAvailable(dm))
            return false;
	
        defaultDisplay = screenDevice.getDisplayMode();
	try 
        {
            screenDevice.setDisplayMode(dm);
	} 
        catch(Exception e) 
        {
            System.err.println("Error setting DisplayMode.");
            e.printStackTrace();
            screenDevice.setDisplayMode(defaultDisplay);
	}
	return true;
    }

    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) 
    {
        int keyCode = e.getKeyCode();
	if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_END) || ((keyCode == KeyEvent.VK_C) && e.isControlDown()))
            exit();
    }
    
    public void keyTyped(KeyEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mouseWheelMoved(MouseWheelEvent e) {}
    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    
    public void componentMoved(ComponentEvent e) 
    {
	if (mode == AFS && e.getComponent() instanceof JFrame)
            window.setLocation(0,0);
    }

    public void componentResized(ComponentEvent e) 
    {
        updateSize();
    }

    public void componentShown(ComponentEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowClosing(WindowEvent e) 
    {
        exit();
    }
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowGainedFocus(WindowEvent e) {}
    public void windowLostFocus(WindowEvent e) {}
    public void windowStateChanged(WindowEvent e) {}
}