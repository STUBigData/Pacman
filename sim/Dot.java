/*
  Copyright 2009  by Sean Luke and Vittorio Zipparo
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.pacman;

/** An empty class.  Dots merely need to exist, not do anything. */

public class Dot{
    public static final long serialVersionUID = 1;

    public String name;
    public double x;
    public double y;
    
    public Dot(String name, double x, double y) {
    	this.name = name;
    	this.x = x;
    	this.y = y;
    }
}
