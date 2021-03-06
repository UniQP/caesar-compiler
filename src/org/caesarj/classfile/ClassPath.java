/*
 * This source file is part of CaesarJ 
 * For the latest info, see http://caesarj.org/
 * 
 * Copyright � 2003-2005 
 * Darmstadt University of Technology, Software Technology Group
 * Also see acknowledgements in readme.txt
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * $Id: ClassPath.java,v 1.6 2005-11-15 16:52:23 klose Exp $
 */

package org.caesarj.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.caesarj.util.Utils;

/**
 * This class implements the conceptual directory structure for .class files
 */
public class ClassPath {

  // ----------------------------------------------------------------------
  // CONSTRUCTORS
  // ----------------------------------------------------------------------

  /**
   * Constructs a class path object.
   *
   * @param	path		the directory names defining the class path
   */
  public ClassPath(String path, String extdirs) {
    if (path == null) {
      // no path specified, use default
      path = System.getProperty("java.class.path");
    }
    if (path == null) {
      // last resort, use current directory
      path = ".";
    }

    this.dirs = loadClassPath(path);

    if (extdirs == null) {
      extdirs = System.getProperty("java.ext.dirs");
    }

    if (extdirs != null) {
      Vector<ClassDirectory>		container = new Vector<ClassDirectory>();
      ClassDirectory[]  tmp;
      File              extDirectory = new File(extdirs);
      
      if (extDirectory.isDirectory()) {
        File[]        extFiles = extDirectory.listFiles();

        for (int i=0; i < extFiles.length; i++) {
          File        file = extFiles[i];

          if (file.isFile() && (file.getName().endsWith(".zip") || file.getName().endsWith(".jar"))) {
            try {
              container.add(new ZipClassDirectory(new ZipFile(file)));
            } catch (ZipException e) {
              // it was not a zip file, ignore it
            } catch (IOException e) {
              // ignore it
            }
          } else {
	  // wrong suffix, ignore it
          }
        }
      }
      
      tmp = new ClassDirectory[dirs.length+container.size()];
      container.copyInto(tmp);
      System.arraycopy(dirs, 0, tmp, container.size(), dirs.length);
      dirs = tmp;
    }
  }

  /**
   * Loads the conceptual directories defining the class path.
   *
   * @param	classPath	the directory names defining the class path
   */
  private static ClassDirectory[] loadClassPath(String classPath) {
    Vector<ClassDirectory>	container = new Vector<ClassDirectory>();

    // load specified class directories
    StringTokenizer	entries;

    entries = new StringTokenizer(classPath, File.pathSeparator);
    while (entries.hasMoreTokens()) {
      ClassDirectory	dir;

      dir = loadClassDirectory(entries.nextToken());
      if (dir != null) {
	container.addElement(dir);
      }
    }

    // add system directories
    if (System.getProperty("sun.boot.class.path") != null) {
      // !!! graf 010508 : can there be more than one entry ?
      entries = new StringTokenizer(System.getProperty("sun.boot.class.path"),
				    File.pathSeparator);
      while (entries.hasMoreTokens()) {
	ClassDirectory	dir;

	dir = loadClassDirectory(entries.nextToken());
	if (dir != null) {
	  container.addElement(dir);
	}
      }
    } else {
      String	version = System.getProperty("java.version");

      if (version.startsWith("1.2") || version.startsWith("1.3")) {
	ClassDirectory	dir;

	dir = loadClassDirectory(System.getProperty("java.home")
				 + File.separatorChar + "lib"
				 + File.separatorChar + "rt.jar");
	if (dir != null) {
	  container.addElement(dir);
	}
      }
    }

    return (ClassDirectory[])Utils.toArray(container, ClassDirectory.class);
  }

  /**
   * Loads a conceptual class directory.
   *
   * @param	name		the name of the directory
   */
  private static ClassDirectory loadClassDirectory(String name) {
    try {
      File	file = new File(name);

      if (file.isDirectory()) {
	return new DirClassDirectory(file);
      } else if (file.isFile()) {
	// check if file is zipped (.zip or .jar)
	if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
	  try {
	    return new ZipClassDirectory(new ZipFile(file));
	  } catch (ZipException e) {
	    // it was not a zip file, ignore it
	    return null;
	  } catch (IOException e) {
	    // ignore it
	    return null;
	  }
	} else {
	  // wrong suffix, ignore it
	  return null;
	}
      } else {
	// wrong file type, ignore it
	return null;
      }
    } catch (SecurityException e) {
      // unreadable file, ignore it
      return null;
    }
  }


  // ----------------------------------------------------------------------
  // CLASS LOADING
  // ----------------------------------------------------------------------

  /**
   * Loads the class with specified name.
   *
   * @param	name		the qualified name of the class
   * @param	interfaceOnly	do not load method code ?
   * @return	the class info for the specified class,
   *		or null if the class cannot be found
   */
  public ClassInfo loadClass(String name, boolean interfaceOnly) {
    for (int i = 0; i < dirs.length; i++) {
      ClassInfo		info;

      info = dirs[i].loadClass(name, interfaceOnly);
      if (info != null) {
	return info;
      }
    }

    return null;
  }


  /**
   * Returns ture iff the specified package exists in the classpath
   *
   * @param	name		the name of the package
   */
  public boolean packageExists(String name) {
    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].packageExists(name)) {
        return true;
      }
    }

    return false;
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private ClassDirectory[]	dirs;	// list of directories in class path
}

/**
 * This class represents a conceptual directory which may hold
 * Java class files. Since Java can use archived class files found in
 * a compressed ("zip") file, this entity may or may not correspond to
 * an actual directory on disk.
 */
abstract class ClassDirectory {

  /**
   * Loads the class with specified name from this directory.
   *
   * @param	name		the qualified name of the class
   * @param	interfaceOnly	do not load method code ?
   * @return	the class info for the specified class,
   *		or null if the class cannot be found in this directory
   */
  public abstract ClassInfo loadClass(String name, boolean interfaceOnly);
  /**
   * Returns ture iff the specified package exists in the classpath
   *
   * @param	name		the name of the package
   */
  public abstract boolean packageExists(String name);
}

class DirClassDirectory extends ClassDirectory {
  /**
   * Constructs a class directory representing a real directory
   */
  public DirClassDirectory(File dir) {
    this.dir = dir;
  }

  /**
   * Loads the class with specified name from this directory.
   *
   * @param	name		the qualified name of the class
   * @param	interfaceOnly	do not load method code ?
   * @return	the class info for the specified class,
   *		or null if the class cannot be found in this directory
   */
  public ClassInfo loadClass(String name, boolean interfaceOnly) {
    File		file;

    file = new File(dir.getPath(),
		    name.replace('/', File.separatorChar) + ".class");
    if (!file.canRead()) {
      return null;
    } else {
      try {
	Data		data = new Data(new FileInputStream(file));

	try {
	  return new ClassInfo(data.getDataInput(), interfaceOnly);
	} catch (ClassFileFormatException e) {
	  e.printStackTrace();
	  return null;
	} catch (IOException e) {
	  e.printStackTrace();
	  return null;
	} finally {
	  data.release();
	}
      } catch (FileNotFoundException e) {
	return null; // really bad : file exists but is not accessible
      }
    }
  }

  /**
   * Returns ture iff the specified package exists in the classpath
   *
   * @param	name		the name of the package
   */
  public boolean packageExists(String name) {
    File		file;

    file = new File(dir.getPath(),
		    name.replace('/', File.separatorChar));

    return file.isDirectory();
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private File		dir;		// non null iff is a real directory
}

class ZipClassDirectory extends ClassDirectory {
  /**
   * Constructs a class directory representing a zipped file
   */
  public ZipClassDirectory(ZipFile zip) {
    this.zip = zip;
  }

  /**
   * Loads the class with specified name from this directory.
   *
   * @param	name		the qualified name of the class
   * @param	interfaceOnly	do not load method code ?
   * @return	the class info for the specified class,
   *		or null if the class cannot be found in this directory
   */
  public ClassInfo loadClass(String name, boolean interfaceOnly) {
    ZipEntry		entry;

    entry = zip.getEntry(name + ".class");
    if (entry == null) {
      return null;
    } else {
      try {
	Data		data = new Data(zip.getInputStream(entry));

	try {
	  return new ClassInfo(data.getDataInput(), interfaceOnly);
	} catch (ClassFileFormatException e) {
	  e.printStackTrace();
	  return null;
	} catch (IOException e) {
	  e.printStackTrace();
	  return null;
	} finally {
	  data.release();
	}
      } catch (IOException e) {
	return null; // really bad : file exists but is not accessible
      }
    }
  }

  /**
   * Returns true iff the specified package exists in the classpath
   *
   * @param	name		the name of the package
   */
  public boolean packageExists(String name) {
    ZipEntry		entry;

    entry = zip.getEntry(name);
    if (entry != null) {
      // !!! FIXME lackner 25.11.01 .isDirectory() returns false for packages ?!?
      // return entry.isDirectory();
      return true;
    } else {
      // in rt.jar of JRE 1.4, no directories are stored : 
      // look if a class exists in the specified package.
      for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
        entry = (ZipEntry)e.nextElement();

        if (entry.getName().startsWith(name + "/")
            && entry.getName().endsWith(".class")) {
          return true;
        }
      }
      return false;
    }
  }
  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private ZipFile	zip;		// non null iff is a zip or jar file
}

// optimization
class Data {
  public Data(InputStream is) {
    this.is = is;
  }

  public DataInput getDataInput() throws IOException {
    ba = getByteArray();
    int		n = 0;
    int		available;
    while (true) {
      int count = is.read(ba, n, ba.length - n);
      if (count < 0) {
	break;
      }
      available = is.available();
      n += count;
      if (n + available > ba.length) {
	byte[] temp = new byte[ba.length * 2];
	System.arraycopy(ba, 0, temp, 0, ba.length);
	ba = temp;
      } else if (available == 0) {
	break;
      }
    }

    is.close();
    is = null;

    return new DataInputStream(new ByteArrayInputStream(ba, 0, n));
  }

  public void release() {
    release(ba);
  }

  private static byte[] getByteArray() {
    if (!ClassfileConstants2.ENV_USE_CACHE || stack.empty()) {
      return new byte[10000];
    }
    return (byte[])stack.pop();
  }

  private static void release(byte[] arr) {
    if (ClassfileConstants2.ENV_USE_CACHE) {
      stack.push(arr);
    }
  }

  // ----------------------------------------------------------------------
  // DATA MEMBERS
  // ----------------------------------------------------------------------

  private InputStream	is;
  private byte[]	ba;
  private static Stack<byte[]>	stack = new Stack<byte[]>();
}
