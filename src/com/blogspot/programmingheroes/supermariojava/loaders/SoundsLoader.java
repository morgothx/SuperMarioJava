package com.blogspot.programmingheroes.supermariojava.loaders;

import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SoundsLoader extends Loader 
{
    public ArrayList<String> playing;
    public SoundsLoader() 
    {
        this(".", "");
    }
	
    public SoundsLoader(String path) 
    {
        this(path, "");
    }
    
    public SoundsLoader(String path, String loader) 
    {
        super(path, loader);
	loaded = new HashMap<String, Object>();
	playing = new ArrayList<String>();
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
            URL url = f.toURI().toURL();
            AudioClip a = Applet.newAudioClip(url);
            loaded.put(name, a);
	} 
        catch (Exception e) 
        {
            System.err.println("Error loanding sound "+name+" from "+f.getPath());
            e.printStackTrace();
            return false;
	}
	System.out.println("Loaded "+name+" from "+f.getName());
	return true;
    }

    public void play(String name, boolean loop) 
    {
        if (loop) 
        {
            ((AudioClip)loaded.get(name)).loop();
            playing.add(name);
            return;
	}
	((AudioClip)loaded.get(name)).play();
    }

    public AudioClip getAudio(String name) 
    {
        return (AudioClip)loaded.get(name);
    }

    public AudioClip getAudio(String name, boolean load, boolean rewrite) 
    {
        Object o = super.getObject(name, load, rewrite);
        if (o == null) 
            return null;
    
        return (AudioClip)o;
    } 
}