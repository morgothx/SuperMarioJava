package com.blogspot.programmingheroes.supermariojava.loaders;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;

public class ImagesLoader extends Loader 
{
    private GraphicsConfiguration gc;
    public ImagesLoader() 
    {
        this(".", "");
    }

    public ImagesLoader(String path) 
    {
        this(path, "");
    }

    public ImagesLoader(String path, String loader) 
    {
        super(path, loader);
	loaded = new HashMap<String, Object>();
	GraphicsEnvironment ge = GraphicsEnvironment
	.getLocalGraphicsEnvironment();
	gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
    }
	
    public GraphicsConfiguration getGraphicsConfiguration() 
    {
        return gc;
    }

    public void loadLine(String line) 
    {
        super.loadLine(line);
	if (line.startsWith("3 ")) 
        {
            loadSprites(line);
        } 
    }

    private boolean loadSprites(String line) 
    {
        boolean error = false;
	StringTokenizer st = new StringTokenizer(line, " ");
        if (st.countTokens() >= 4) 
        {
            st.nextToken();
            String name = st.nextToken();
            String nameFile = "";
            int equals = name.indexOf("=");
            int row = 0, col = 0;
            try 
            {
                row = Integer.parseInt(st.nextToken());
		col = Integer.parseInt(st.nextToken());
            }
            catch (NumberFormatException e) 
            {
                error = true;
            }
            if (!error) 
            {
                if (equals == -1) 
                {
                    if (!super.load(name)) 
                    {
                        return false;
                    }
		}
                else 
                { 
                    nameFile = name.substring(equals+1);
                    name = name.substring(0, equals);
                    if (!super.load(nameFile, name)) 
                    {
                        return false;
                    }
		}
		BufferedImage bi = getImage(name);
		int w = (int)bi.getWidth()/col;
		int h = bi.getHeight()/row;
		int transparency = bi.getColorModel().getTransparency();
                BufferedImage img;
                Graphics g;
		String nextName = null;
		equals = -1;
		for (int i=0; i<row; i++)
                    for (int j=0; j<col; j++) 
                    {
                        img = gc.createCompatibleImage(w, h, transparency);
                        g = img.getGraphics();
			g.drawImage(bi, 0, 0, w, h, j*w, i*h, (j*w)+w, (i*h)+h, null);
                        if (nextName == null) 
                        {
                            nextName = "";
                            while (st.hasMoreTokens()) 
                            {
                                nextName = st.nextToken();
				equals=nextName.indexOf("=");
				if (equals != -1) 
                                {
                                    break;
				}
                            }
			}
                        if (nextName.startsWith(i+"_"+j)) 
                        {
                            loaded.put(nextName.substring(equals+1), img);
                            System.out.print(nextName.substring(equals+1)+" -> ");
                            nextName = null;
			} 
                        else 
                        {
                            loaded.put(name+i+"_"+j, img);
			}
			g.dispose();
			System.out.println(name+i+"_"+j);
                    }
                    removeObject(name);
            }
	} 
        else 
        {
            error = true;
	}
	if (error) 
        {
            System.err.println("Error format in line: "+line);
	}
	return error;
    }

    public boolean load(File f, String name, boolean rewrite) 
    {
        if (name == null) 
        {
            name = f.getName();
	}
	
        if (!rewrite && loaded.containsKey(name)) 
        {
            return false;
        }
	try 
        {
            BufferedImage bi = ImageIO.read(f);
            int transparency = bi.getColorModel().getTransparency();
            BufferedImage img = gc.createCompatibleImage(bi.getWidth(), bi.getHeight(), transparency);
            Graphics2D g = img.createGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            loaded.put(name, img);
        } 
        catch (IOException e) 
        {
            System.err.println("Error loanding image " +f.getPath());
            e.printStackTrace();
            return false;
	}
	
        System.out.println("Loaded "+name+" from "+f.getName());
	return true;
    }

    public BufferedImage getImage(String name) 
    {
        return (BufferedImage)loaded.get(name);
    }

    public BufferedImage getImage(String name, boolean load, boolean rewrite) 
    {
        Object o = super.getObject(name, load, rewrite);
	if (o == null) 
        {
            return null;
	}
	return (BufferedImage)o;
    }
}