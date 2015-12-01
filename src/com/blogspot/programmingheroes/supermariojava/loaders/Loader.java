package com.blogspot.programmingheroes.supermariojava.loaders;

import java.util.*;
import java.io.*;

public abstract class Loader 
{
    public static ArrayList<String> validExtensions;
    private String packageDirectory;
    private String relativePath;
    private File loader;
    protected HashMap<String, Object> loaded;
    public Loader() 
    {
        this(".", "");
    } 
    
    public Loader(String path) 
    {
        this(path, "");
    }

    public Loader(String path, String loader) 
    {
        validExtensions = new ArrayList<String>();
	validExtensions.add("");
        packageDirectory = getClass().getClassLoader().getResource("").getPath();
	packageDirectory = packageDirectory.substring(0, packageDirectory.lastIndexOf("classes/"));
	setPath(path);
	setLoader(loader);
        System.out.println("Package Directory "+packageDirectory);
        System.out.println("Relative Path: "+relativePath);
	System.out.println("Loader: "+loader+" exists-> "+existsLoader());
    }

    public void setPath(String path)
    {
	try 
        {
            path = path.trim();
            StringBuffer s = new StringBuffer(path);
            for (int i=0; i<s.length(); i++) 
            {
                if (s.charAt(i) == '\\') 
                {
                    s.replace(i, i+1, "/");
		}
            }
            path = s.toString();
            if (path.charAt(path.length()-1) != '/') 
            {
                path += "/";
            }
            this.relativePath = path;
	} 
        catch (Exception e) 
        {
            System.err.println("Error by setting the resource directory.");
            e.printStackTrace();
	}
    }

    public boolean setLoader(String name) 
    {
        if (name == null) return false;
            loader = getFile(name);
	if (loader == null) return false;
            return loader.exists();
    }

    public boolean startLoader() 
    {
        if (loader != null && loader.exists()) 
        {
            if (loader.isDirectory()) 
            {
                return loadDirectory(loader);
            } 
            else if (loader.isFile()) 
            {
                if (loader.canRead()) 
                {
                    return readLoaderFile();
		}
            }
	}
	return false;
    }

    private boolean readLoaderFile() 
    {
        BufferedReader br;
	try 
        {
            InputStream is = new FileInputStream(loader);
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            System.out.println("-- Reading loader --");
            String line;
            while ((line = br.readLine()) != null) 
            {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("//")) 
                {
                    continue;
		}
		loadLine(line);
            }
            br.close();
	} 
        catch (IOException e) 
        {
            System.err.println("Error reading file loader:\n"+loader.toString());
	} catch (Exception e) 
        {
            e.printStackTrace();
	}
	return true;
    }

    public void loadLine(String line) 
    {
        if (line.startsWith("1 ")) 
        {
            loadSingleFile(line);					
	} 
        else if (line.startsWith("2 ")) 
        {
            loadNumeratedFiles(line);
	}
    }

    private void loadSingleFile(String line) 
    {
        int equals = line.indexOf("=");
	if (equals == -1) 
        {
            load(line.substring(2));
	} 
        else 
        {
            load(line.substring(equals+1),
            line.substring(2, equals));
	}
    }

    private void loadNumeratedFiles(String line) 
    {
        boolean error = false;
        StringTokenizer st = new StringTokenizer(line, " ");
	int tokens = st.countTokens();
	st.nextToken();
        String fileName = st.nextToken();
	String loadedName = null;
	int equals = fileName.indexOf("=");
	if (equals != -1) 
        {
            loadedName = fileName.substring(0, equals);
            fileName = fileName.substring(equals+1);
	}
	int wildcard = fileName.indexOf("*");
	if (wildcard != -1) 
        {
            int i=0;
            String fullName;
            if (tokens == 2) 
            {
                do 
                {
                    fullName = fileName.substring(0,wildcard)+(i++)+fileName.substring(wildcard+1);
		} 
                while (load(fullName,loadedName+(i-1)));
            } else if (tokens >= 3) 
            {
                int numFiles = 0;
                try 
                {
                    numFiles = Integer.parseInt(st.nextToken());
                    if (tokens == 4) 
                    {
                        i = Integer.parseInt(st.nextToken());
			numFiles += i;
                    }
		} 
                catch (NumberFormatException e) 
                {
                    error = true;
                }
		while (i<numFiles) 
                {
                    fullName = fileName.substring(0,wildcard)+(i++)+fileName.substring(wildcard+1);
                    load(fullName, loadedName);
                }
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
    }

    public boolean loadDirectory(File d) 
    {
        if (!d.isDirectory()) 
            return false;
	
        File[] f = d.listFiles();
	for (File file : f) 
        {
            if( file.isFile() && hasValidExtension(file) ) 
            {
                load(file);
            }
	}
	return true;
    }

    public boolean load(File f) 
    {
        return load(f, f.getName(), false);
    }

    public abstract boolean load(File f, String name, boolean rewrite);

    public boolean load(String n) 
    {
        return load(n, n);
    }

    public boolean load(String fileName, String name) 
    {
        File f = new File(getPath()+fileName);
	if (f.exists()) 
        {
            if (f.isDirectory()) 
            {
                loadDirectory(f);
            } 
            else if (f.isFile() && hasValidExtension(f)) 
            {
                return load(f, name, false);
            }
	} 
        else 
        {
            System.err.println("No found: "+f.getPath());
	}
	return false;
    }

    public Object getObject(String name, boolean load, boolean rewrite) 
    {
        Object aux = loaded.get(name);
	if ((load && aux == null) || rewrite) 
        {
            File f = new File(getPath()+name);
            if (f.exists() && f.isFile() && hasValidExtension(f)) 
            {
                load(f, name, rewrite);
            }
	}
	return loaded.get(name);
    }

    public void removeObject(String name) 
    {
        loaded.remove(name);
    }

    public void removeAllObjects() 
    {
        loaded.clear();
    }

    public File getFile(String name) 
    {
        try 
        {
            File file = new File(packageDirectory + relativePath + name);
		return file;
        } 
        catch (Exception e) 
        {
            System.err.println("Error loanding a file "+name);
        }
	return null;
    }

    public String getRelativePath() 
    {
        return relativePath;
    }

    public String getPath() 
    {
        return packageDirectory+relativePath;
    }

    public String getPackagePath() 
    {
        return packageDirectory;
    }

    public File getLoader()
    {
        return loader;
    }

    public void changeName(String name, String newName)
    {
        loaded.put(newName, loaded.get(name));		
	removeObject(name);
    }

    public void putObject(String name, Object object) 
    {
        loaded.put(name, object);
    }

    public boolean hasValidExtension(File f) 
    {
        for (int i=0; i<validExtensions.size(); i++) 
        {
            String ext = "."+validExtensions.get(i).toLowerCase();
            if (ext.equals(".")) 
            {
                return true;
            }
            if (f.getName().toLowerCase().lastIndexOf(ext) + ext.length() == f.getName().length()) 
            {
                return true;
            }
	}
	return false;
    }

    public boolean existsLoader() 
    {
        if (loader != null)
            return loader.exists();
	
        return false;
    }
}