package com.blogspot.programmingheroes.supermariojava;

import java.awt.event.*;
import java.applet.*;

public abstract class Player extends WorldObject 
{
    public static final int NOT_JUMPING = 0;
    public static final int RISING = 1;
    public static final int FALLING = 2;
    public static final int STOPPED = 0;
    public static final int MOVING_RIGHT = 1;
    public static final int MOVING_LEFT = 2;
    public static AudioClip[] audio;
    public static int indexAudio;
    public static boolean first = true;
    public abstract void createKeys();
    public int keyLeft;
    public int keyRight;
    public int keyRun;
    public int keyJump;
    public int keyCrouch;
    public boolean keyLeftDown = false;
    public boolean keyRightDown = false;
    public boolean keyRunDown = false;
    public boolean keyJumpDown = false;
    public boolean keyCrouchDown = false;
    protected abstract void createImgs();
    protected String imgLStop;
    protected String imgRStop;
    protected String imgLJump;
    protected String imgRJump;
    protected String imgLFall;
    protected String imgRFall;
    protected String imgRight;
    protected String imgLeft;
    protected String imgRSlip;
    protected String imgLSlip;
    protected String imgRCrouch;
    protected String imgLCrouch;
    protected int yState;
    protected int xState;
    protected int leftAndRight;
    protected float acelX = 0.05F;
    protected float speedX = 2;
    protected float acelY = ((Main)stage).getGravity();
    protected float speedY = 10;
    protected float reward = 1;
    protected float acelRunningX = 0.1F;
    protected float speedRunningX = 4;
    protected int floor = WorldObject.NOT_FLOOR;
    protected int lastFloor = WorldObject.NOT_FLOOR;
    protected volatile boolean lookingRight = false;
    protected volatile boolean lookingLeft = false;
    protected volatile boolean slipping = false;
    protected volatile boolean running = false;
    protected volatile boolean crouching = false;

    public Player(Stage s) 
    {
        super(s);
        if (first) 
        {
            first = false;
            indexAudio = 0;
            audio = new AudioClip[2];
            for (int i=0; i<audio.length; i++) 
            {
                audio[i] = stage.getSoundsLoader().getAudio("jump.wav", true, true);
            }
	}
        createKeys();
        createImgs();
        xState = STOPPED;
	yState = NOT_JUMPING;
	leftAndRight = STOPPED;
	lookingRight = true;
	setImage(imgRStop);
    }

    public synchronized void act() 
    {
        movePlayer();
        speedXUpdate();
        speedYUpdate();
        if (keyJumpDown && yState==NOT_JUMPING) 
        {
            jump();
	}

        if (keyRightDown && keyLeftDown) 
        {
            if (leftAndRight == STOPPED) 
            {
                leftAndRight = xState;
            }
	}
        else 
        {
            leftAndRight = STOPPED;
	}

        if (keyRightDown && xState != MOVING_RIGHT && leftAndRight != MOVING_RIGHT) 
        {
            moveRight();
	}
        else if (keyLeftDown && xState != MOVING_LEFT && leftAndRight != MOVING_LEFT) 
        {
            moveLeft();
	}
        else if (!keyLeftDown && !keyRightDown && xState != STOPPED) 
        {
            stop();
	}
        if (xState == STOPPED && yState == NOT_JUMPING) 
        {
            if (keyCrouchDown && !crouching) 
            {
                crouch();
            } 
            else if (!keyCrouchDown && crouching) 
            {
                standUp();
            }
	}

        updateImg();
    }

    public void movePlayer() 
    {
        move();
        if (map.readyRightXMap(this) && speed.getAccurateX() > 0) 
        {
            map.setSpeedX(speed.getAccurateX());
	}
        else if (map.readyLeftXMap(this) && speed.getAccurateX() < 0) 
        {
            map.setSpeedX(speed.getAccurateX());
	}
        if (map.readyUpYMap(this) && speed.getAccurateY() > 0) 
        {
            map.setSpeedY(-speed.getAccurateY());
	} else if (map.readyDownYMap(this) && speed.getAccurateY() < 0) 
        {
            map.setSpeedY(-speed.getAccurateY());
	}
    }

    public void speedXUpdate() 
    {
        double s = 0;
	float acel = (keyRunDown)?acelRunningX:acelX;
	float limitSpeed = (keyRunDown)?speedRunningX:speedX;
	if (xState == MOVING_RIGHT) 
        {
            if (speed.getX() > limitSpeed) 
            {
                s = speed.getAccurateX()-acelX;
            } 
            else 
            {
                s = speed.getAccurateX()+acel;
		if (s > limitSpeed) 
                {
                    s = limitSpeed;
		}
            }
	}
        else if (xState == MOVING_LEFT)
        {
            if (speed.getX() < -limitSpeed) 
            {
                s = speed.getAccurateX()+acelX;
            }
            else 
            {
                s = speed.getAccurateX()-acel;
		if (s < -limitSpeed) 
                {
                    s = -limitSpeed;
		}
            }
	}
        else if (xState == STOPPED) 
        {
            if (slipping) 
            {
                if (lookingLeft) 
                {
                    s = speed.getAccurateX()-acelX;
                    if (s < 0)
                    {
                        s = 0;
                    }
		}
                else if (lookingRight) 
                {
                    s = speed.getAccurateX()+acelX;
                    if (s > 0) 
                    {
                        s = 0;
                    }
		}
            }
            else
            {
                if (lookingRight) 
                {
                    s = speed.getAccurateX()-acelX;
                    if (s < 0)
                    {
                        s = 0;
                    }
		}
                else if (lookingLeft) 
                {
                    s = speed.getAccurateX()+acelX;
                    if (s > 0) 
                    {
                        s = 0;
                    }
		}
            }
	}
	speed.setX(s);
        if (x <= 0 && s < 0) 
        {
            x = 0;
            speed.setX(0);
	}
        else if (x >= map.getWidth()-width && s > 0) 
        {
            x = map.getWidth()-width;
            speed.setX(0);
	}
    }

    public void speedYUpdate() 
    {
        if (yState == NOT_JUMPING && floor == NOT_FLOOR) 
        {
            fall();
	}
        else if (yState != NOT_JUMPING && floor != NOT_FLOOR && floor != lastFloor) 
        {
            land();
            y = floor-height+1;
            speed.setY(0);
	}
	lastFloor = floor;
	floor = NOT_FLOOR;
        if (yState != NOT_JUMPING) 
        {
            if (yState==RISING && speed.getAccurateY()==0) 
            {
                if (xState == MOVING_RIGHT)
                {
                    speed.setY(speedY +speed.getAccurateX()/speedX*reward);
		}
                else if (xState == MOVING_LEFT) 
                {
                    speed.setY(speedY-speed.getAccurateX()/speedX *reward);
		}
                else 
                {
                    speed.setY(speedY);
		}
            }
            else 
            {
                if (yState == RISING && !keyJumpDown) 
                {
                    if (speed.getAccurateY()>4)
                    {
                        speed.setY(4);
                    }
		}
                double s = speed.getAccurateY()-acelY;
		if (yState != FALLING && s <= 0)
                {
                    fall();
		}
                if (-s > speedY)
                {
                    s = -speedY;
		}
                speed.setY(s);
            }
	}
    }

    public void updateImg()
    {
        if (slipping) 
        {
            if (xState == MOVING_LEFT && speed.getAccurateX() < 0) 
            {
                slipping = false;
		setImages(imgLeft, 1, 3);
            } 
            else if (xState == MOVING_RIGHT && speed.getAccurateX() > 0) 
            {
                slipping = false;
		setImages(imgRight, 1, 3);
            }
	}
	
        if (xState != STOPPED && yState == NOT_JUMPING && !slipping) 
        {
            int num = 0;
            if (running) 
            {
                num = (stage.getFPS()/15);
            } 
            else 
            {
                num = (stage.getFPS()/10);
            }
            if (num == 0 || stage.getTotalFrames() % num == 0) 
            {
                nextImg();
            }
	}
    }

    public void moveLeft() 
    {
        xState = MOVING_LEFT;
	lookingLeft = true;
	lookingRight = false;
	crouching = false;	
	if (yState != NOT_JUMPING) 
        {
            setImage(imgLJump);
	}
        else if (speed.getAccurateX() > 0) 
        {
            slipping = true;
            setImage(imgLSlip);
	}
        else 
        {
            setImages(imgLeft, 1, 3);
	}
    }

    public void moveRight() 
    {
        xState = MOVING_RIGHT;
	lookingRight = true;
	lookingLeft = false;
	crouching = false;
	if (yState != NOT_JUMPING) 
        {
            setImage(imgRJump);
	}
        else if (speed.getAccurateX() < 0) 
        {
            slipping = true;
            setImage(imgRSlip);
	}
        else 
        {
            setImages(imgRight, 1, 3);
	}
    }

    public void stop() 
    {
        xState = STOPPED;
	if (lookingRight) 
        {
            if (yState != NOT_JUMPING) 
            {
                setImage(imgRJump);
            }
            else 
            {
                setImage(imgRStop);
            }
	}
        else if (lookingLeft)
        {
            if (yState != NOT_JUMPING) 
            {
                setImage(imgLJump);
            }
            else
            {
		setImage(imgLStop);
            }
	}
    }

    public void jump() 
    {
        yState = RISING;
        audio[indexAudio].play();
	indexAudio = (indexAudio+1)%audio.length;
        if (lookingRight) 
        {
            setImage(imgRJump);
	}
        else if (lookingLeft)
        {
            setImage(imgLJump);
	}
    }

    public void fall() 
    {
        yState = FALLING;
        if (lookingRight) 
        {
            setImage(imgRFall);
	}
        else if (lookingLeft)
        {
            setImage(imgLFall);
	}
    }

    public void crouch() 
    {
        crouching = true;
	if (lookingRight) 
        {
            setImage(imgRCrouch);
        }
        else if (lookingLeft) 
        {
            setImage(imgLCrouch);
	}
    }

    public void standUp() 
    {
        crouching = false;
	if (lookingRight) 
        {
            setImage(imgRStop);
	}
        else if (lookingLeft)
        {
            setImage(imgLStop);
	}
    }

    public void land()
    {
        if (yState == NOT_JUMPING) 
        {
            return;
	}
	yState = NOT_JUMPING;
	if (xState == MOVING_LEFT)
        {
            if (speed.getAccurateX() > 0)
            {
                slipping = true;
		setImage(imgLSlip);
            }
            else
            {
		setImages(imgLeft, 1, 3);
            }
	}
        else if (xState == MOVING_RIGHT) 
        {
            if (speed.getAccurateX() < 0)
            {
                slipping = true;
		setImage(imgRSlip);
            }
            else
            {
                setImages(imgRight, 1, 3);
            }
        } else if (xState == STOPPED) 
        {
            if (lookingRight) 
            {
                if (crouching) 
                {
                    setImage(imgRCrouch);
		}
                else 
                {
                    setImage(imgRStop);
		}
            }
            else if (lookingLeft)
            {
                if (crouching) 
                {
                    setImage(imgLCrouch);
		}
                else 
                {
                    setImage(imgLStop);
		}
            }
	}
    }

    public void setXStage(int s)
    {
        if (s == MOVING_RIGHT || s == MOVING_LEFT || s == STOPPED) 
        {
            xState = s;
	}
    }

    public void setYState(int s) 
    {
        if (s == NOT_JUMPING || s == RISING || s == FALLING) 
        {
            yState = s;
	}
    }

    public void setFloor(int floor) 
    {
        this.floor = floor;
    }

    public void setRightWall(int wall) 
    {
        x = wall-(int)bounds.get(0).getX();
    }

    public void setLeftWall(int wall) 
    {
        x = wall-width+(int)bounds.get(0).getX();
    }
	
    public boolean isRising() 
    {
        return yState == RISING;
    }
	
    public boolean isFalling() 
    {
        return yState == FALLING;
    }

    public boolean isRunning() 
    {
        return running;
    }

    public boolean isCrouching()
    {
        return crouching;
    }

    public boolean isWalkingRight() 
    {
        return xState == MOVING_RIGHT;
    }

    public boolean isWalkingLeft() 
    {
        return xState == MOVING_LEFT;
    }

    public synchronized void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == keyJump)
            keyJumpDown = true;
	else if (e.getKeyCode() == keyRight)
            keyRightDown = true;
	else if (e.getKeyCode() == keyLeft)
            keyLeftDown = true;
	else if (e.getKeyCode() == keyRun)
            keyRunDown = true;
	else if (e.getKeyCode() == keyCrouch)
            keyCrouchDown = true;
    }

    public synchronized void keyReleased(KeyEvent e) 
    {
        if (e.getKeyCode() == keyJump)
            keyJumpDown = false;
	else if (e.getKeyCode() == keyRight)
            keyRightDown = false;
	else if (e.getKeyCode() == keyLeft)
            keyLeftDown = false;
	else if (e.getKeyCode() == keyRun)
            keyRunDown = false;
	else if (e.getKeyCode() == keyCrouch)
            keyCrouchDown = false;
	}
}