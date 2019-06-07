/*
  Copyright 2009  by Sean Luke and Vittorio Zipparo
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.pacman;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.Oriented2D;
import sim.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ec.util.*;

/* The Pac is the Pac Man in the game.  Pac is an Agent and is also Steppable.  The Pac moves first, then the ghosts. */

public class Pac extends Agent implements Steppable
    {
    private static final long serialVersionUID = 1;

    /** How long we wait while the Pac dies (not spinning). */
    public static final int WAIT_TIME = 100;
        
    /** How long we wait while the Pac spins around while dying. */
    public static final int SPIN_TIME = 100;
        
    /** How often the Pac rotates 90 degrees while spinning. */
    public static final int SPIN_SPEED = 5;
        
    /** The Pac's discretization (9), which makes him faster than the ghosts, whose discretization is 10. */
    public static final int PAC_DISCRETIZATION = 9;
        
    /** The current score the Pac receives for eating a ghost. */
    public int eatGhostScore = 200;
        
    /** The Pac's index in the player array.  This will be used to allow multiple Pacs. */
    public int tag;
        
    /** The stoppable for this Pac so he can remove himself when he dies if it's multiplayer */
    public Stoppable stopper;
        
    /** Creates a Pac assigned to the given tag, puts him in pacman.agents at the start location, and schedules him on the schedule. */
    public Pac(PacMan pacman, int tag) 
        {
        super(pacman);
        this.tag = tag;
        discretization = PAC_DISCRETIZATION;  // I go a bit faster
        stopper = pacman.schedule.scheduleRepeating(this, 0, 1);  // schedule at time 0
        
    
        }

    // the pac's start location
    public Double2D getStartLocation() { return new Double2D(13.5, 25); }


   

    /* Default policy implementation: Pac is controlled through the joystick/keyboard
     * To changhe Pacs behavior derived classes should override this method
     */
    protected void doPolicyStep(SimState state)
        {
        int nextAction = pacman.getNextAction(tag);

        // pac man delays the next action until he can do it.  This requires a bit of special code
        if (isPossibleToDoAction(nextAction))
            {
            performAction(nextAction);
            }
        else if (isPossibleToDoAction(lastAction))
            {
            performAction(lastAction);
            }

        }
   
    /* Steps the Pac.  This does various things.  First, we look up the action from the user (getNextAction).
       Then we determine if it's possible to do the action.  If not, we determine if it's possible to do the
       previous action.  Then we do those actions.  As a result we may have eaten an energizer or a dot.  If so
       we remove the dot or energizer, update the score, and possibly frighten the ghosts.  If we've eaten all
       the dots, we schedule an event to reset the level.  Next we check to see if we've encountered a ghost.
       If the ghost is frightened, we eat it and put him in jail.  Otherwise we die.
    */
    public void step(SimState state) {
    	
    	// Output the state
    	//outputState(state);
    	
        doPolicyStep(state);
        // now maybe we eat a dot or energizer...

        Bag nearby = pacman.dots.getNeighborsWithinDistance(new Double2D(location), 0.3);  // 0.3 seems reasonable.  We gotta be right on top anyway
        for(int i=0; i < nearby.numObjs; i++)
            {
            Object obj = nearby.objs[i];
            if (obj instanceof Energizer && pacman.dots.getObjectLocation(obj).equals(location))  // uh oh
                {
                pacman.score+=40; // only 40 because there is a dot right below the energizer.  Total should appear to be 50
                pacman.dots.remove(obj);
                eatGhostScore = 200;  // reset
                pacman.frightenGhosts = true;

                // create a Steppable to turn off ghost frightening after the ghosts have had a chance to
                // be sufficiently frightened
                pacman.schedule.scheduleOnce(new Steppable()  // the pac goes first, then the ghosts, so they'll get frightened this timestep, so we turn it off first thing next time
                    {
                    public void step(SimState state)
                        {
                        pacman.frightenGhosts = false;
                        }
                    }, -1);
                }
            if (obj instanceof Dot && pacman.dots.getObjectLocation(obj) !=null && pacman.dots.getObjectLocation(obj).equals(location))
                {
                pacman.score+=10;
                pacman.dots.remove(obj);
                }
            }
        if (nearby.numObjs > 0)
            if (pacman.dots.size() == 0)  // empty!
                {
                pacman.schedule.scheduleOnceIn(0.25, new Steppable()            // so it happens next
                    {
                    public void step(SimState state)
                        { 
                        resetLevel();
                        }
                    });  // the Ghosts move a bit more
                }

        // a ghost perhaps?
                
        nearby = pacman.agents.getNeighborsWithinDistance(new Double2D(location), 0.3);  // 0.3 seems reasonable.  We gotta be right on top anyway
        for(int i=0; i < nearby.numObjs; i++)
            {
            Object obj = nearby.objs[i];
            if (obj instanceof Ghost && location.distanceSq(pacman.agents.getObjectLocation(obj)) <= 0.2) // within 0.4 roughly
                {
                Ghost m = (Ghost)obj;
                if (m.frightened > 0)  // yum
                    {
                    pacman.score += eatGhostScore;
                    eatGhostScore *= 2;  // each Ghost is 2x more
                    m.putInJail();
                    }
                else // ouch
                    {
                    pacman.schedule.scheduleOnceIn(0.5, new Steppable()             // so it happens next.  Should be after resetLEvel(), so we do 0.5 rather than 0.25
                        {
                        public void step(SimState state)
                            { 
                            die();
                            }
                        });  // the ghosts move a bit more
                    }
                }
            }
        
        	
        // Output the state
        transmitState(state);
        
        
    }


    /**
     * Output the state of the sim to file
     * @param state
     */
    public void outputState(SimState state) {
    	// Output the state
        int energizersRemaining = 0;    
        int dotsRemaining = 0;
        
        
        System.out.print("host:" +((PacMan)state).hostName + ",");
        
        LocalDateTime myDateObj = LocalDateTime.now(); 
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("MM/dd/yyyy"); 
        String formattedDate = myDateObj.format(myFormatObj); 
        System.out.print("date:" + formattedDate + ",");
        myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss"); 
        String formattedTime = myDateObj.format(myFormatObj); 
        System.out.print("time:" + formattedTime + ",");
        
        // Get Energizers
        for(int i = 0; i < pacman.dots.allObjects.size(); i++) {
        	Object obj = pacman.dots.allObjects.objs[i];
        	if(obj instanceof Energizer) {
        		Energizer energizer = (Energizer)obj;
	        	System.out.print("name:" + energizer.name + ",");
	        	System.out.print("x:" + (int)(energizer.x) + ",");
	        	System.out.print("y:" + (int)(energizer.y) + ",");
        	
        		energizersRemaining++;
        	}
        }
        
        // Dots
        for(int i = 0; i < pacman.dots.allObjects.size(); i++) {
        	Object obj = pacman.dots.allObjects.objs[i];
        	if(obj instanceof Dot && !(obj instanceof Energizer)) {
        		Dot dot = (Dot)obj;
	        	System.out.print("name:" + dot.name + ",");
	        	System.out.print("x:" + (int)(dot.x) + ",");
	        	System.out.print("y:" + (int)(dot.y) + ",");
        	
	        	dotsRemaining++;
        	}
        }
        
        // Ghosts
        for(int i = 0; i < pacman.agents.allObjects.size(); i++) {
        	Agent agent = ((Agent)pacman.agents.allObjects.objs[i]);
        	if(agent instanceof Ghost) {
	        	System.out.print("name:" + agent.name + ",");
	        	System.out.print("x:" + agent.location.x + ",");
	        	System.out.print("y:" + agent.location.y + ",");
	        	System.out.print("lastAction:" + agent.lastAction + ",");
        	}
        }

        for(int i = 0; i < pacman.agents.allObjects.size(); i++) {
        	Agent agent = ((Agent)pacman.agents.allObjects.objs[i]);
        	if(agent instanceof Pac) {
        		Pac pac = (Pac)agent;
	        	System.out.print("name:" + pac.name + ",");
	        	System.out.print("x:" + pac.location.x + ",");
	        	System.out.print("y:" + pac.location.y + ",");
	        	System.out.print("action:" + pacman.getNextAction(pac.tag) + ",");
        	}
        }
        
        //System.out.print("Key:" + c + ",");
        System.out.print("Deaths:" + pacman.deaths + ",");
        System.out.print("Score:" + pacman.score + ",");
        System.out.print("DotsRemaining:" + dotsRemaining + ",");
        System.out.println("EnergizersRemaining:" + energizersRemaining);
    }
    
    public void transmitState(SimState state) {
    	// Output the state
        int energizersRemaining = 0;    
        int dotsRemaining = 0;
        int ghostsRemaining = 0;
        int pacsRemaining = 0;
        
        String message = "{ host:" +((PacMan)state).hostName + ", ";
        
        LocalDateTime myDateObj = LocalDateTime.now(); 
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("MM/dd/yyyy"); 
        String formattedDate = myDateObj.format(myFormatObj); 
        message += "date:" + formattedDate + ", ";
        myFormatObj = DateTimeFormatter.ofPattern("HH:mm:ss"); 
        String formattedTime = myDateObj.format(myFormatObj); 
        message += "time:" + formattedTime + ", ";
        
        int numOfDots = 0;
        int numOfEnergizers = 0;
        int numOfGhosts = 0;
        int numOfPacs = 0;
        
        // Count the number of energizers and dots
        for(int i = 0; i < pacman.dots.allObjects.size(); i++) {
        	Object obj = pacman.dots.allObjects.objs[i];
        	if(obj instanceof Energizer) {
        		numOfEnergizers++;
        	}
        	else if(obj instanceof Dot) { 
        		numOfDots++;
        	}
        }
        
        // Count the number of ghosts and pacmans
        for(int i = 0; i < pacman.agents.allObjects.size(); i++) {
        	Agent agent = ((Agent)pacman.agents.allObjects.objs[i]);
        	if(agent instanceof Ghost) {
        		numOfGhosts++;
        	}
        	else if(agent instanceof Pac) {
        		numOfPacs++;
        	}
        }
        
        // Get Energizers
        message += "Energizers: [";
        for(int i = 0; i < pacman.dots.allObjects.size(); i++) {
        	Object obj = pacman.dots.allObjects.objs[i];
        	if(obj instanceof Energizer) {
        		Energizer energizer = (Energizer)obj;
        		message += "{name: {" + energizer.name + ", ";
        		message += "x:" + (int)(energizer.x) + ", ";
        		message += "y:" + (int)(energizer.y) + "}";
        	
        		if(energizersRemaining < numOfEnergizers-1) {
        			message += ", ";
        		}
        		energizersRemaining++;
        	}
        }
        message += "], ";
        
        // Dots
        message += "Dots: [";
        for(int i = 0; i < pacman.dots.allObjects.size(); i++) {
        	Object obj = pacman.dots.allObjects.objs[i];
        	if(obj instanceof Dot && !(obj instanceof Energizer)) {
        		Dot dot = (Dot)obj;
        		message += "{name:" + dot.name + ", ";
        		message += "x:" + (int)(dot.x) + ", ";
        		message += "y:" + (int)(dot.y) + "}";
        		
        		if(dotsRemaining < numOfDots-1) {
        			message += ", ";
        		}
        	
	        	dotsRemaining++;
        	}
        }
        message += "], ";
        
        // Ghosts
        message += "Ghosts: [";
        for(int i = 0; i < pacman.agents.allObjects.size(); i++) {
        	Agent agent = ((Agent)pacman.agents.allObjects.objs[i]);
        	if(agent instanceof Ghost) {
        		message += "{name:" + agent.name + ", ";
        		message += "x:" + agent.location.x + ", ";
        		message += "y:" + agent.location.y + ", ";
        		message += "lastAction:" + lastActionToChar(agent.lastAction) + "}";
        		
        		if(ghostsRemaining < numOfGhosts-1) {
        			message += ", ";
        		}
        		ghostsRemaining++;
        	}
        }
        message += "], ";
        
        for(int i = 0; i < pacman.agents.allObjects.size(); i++) {
        	Agent agent = ((Agent)pacman.agents.allObjects.objs[i]);
        	if(agent instanceof Pac) {
        		Pac pac = (Pac)agent;
        		message += "{name:" + pac.name + ", ";
        		message += "x:" + pac.location.x + ", ";
        		message += "y:" + pac.location.y + ", ";
        		message += "action:" + lastActionToChar(pacman.getNextAction(pac.tag)) + "}";
        		
        		if(pacsRemaining < numOfPacs-1) {
        			message += ", ";
        		}
        		pacsRemaining++;
        	}
        }
        message += "], ";
        
        message += "DotsRemaining:" + dotsRemaining + ", ";
        message += "EnergizersRemaining:" + energizersRemaining+ ", ";
        message += "Deaths:" + pacman.deaths + ", ";
        message += "Level:" + pacman.level + ", ";
        message += "Score:" + pacman.score + " }";
        
        
        if( pacman.deaths > 0 || pacman.level > 3 ) {
        	System.out.println("Score: " + pacman.score);
        	System.exit(0);
        }
        
        ((PacMan)state).stepCounter++;
        if(((PacMan)state).out != null) {
        	if(((PacMan)state).stepCounter == ((PacMan)state).stepsToTransmitInterval) {
        		((PacMan)state).out.println(message);
        		((PacMan)state).stepCounter = 0;
        	}
        }
    }
    
    /**
     * Converts the last action enum of an agent to a character
     * @param action The enum (int) of the last action the agent performed
     * @return Character representation of the last action
     */
    public char lastActionToChar(int action) {
    	switch(action) {
    		case -1:	// Nothing
    			return '-';
    		case 0:
    			return 'N';
    		case 1:
    			return 'E';
    		case 2:
    			return 'S';
    		case 3:
    			return 'W';
    	}
    	
    	return '-';
    }
    /** Resets the level as a result of eating all the dots.  To do this we first clear out the entire
        schedule; this will eliminate everything because resetLevel() was itself scheduled at a half-time
        timestep so it's the only thing going on right now.  Clever right?  I know!  So awesome.  Anyway,
        we then schedule a little pause to occur.  Then afterwards we reset the game.
    */
    public void resetLevel()
        {
        // clear out the schedule, we're done
        pacman.schedule.clear();

        // do a little pause
        pacman.schedule.scheduleOnce(
            new Steppable()
                {
                public int count = 0;
                public void step(SimState state) 
                    { 
                    if (++count < WAIT_TIME * 2) pacman.schedule.scheduleOnce(this); 
                    } 
                });
                                
        pacman.schedule.scheduleOnceIn(WAIT_TIME * 2,
            new Steppable()
                {
                public void step(SimState state) { pacman.level++; pacman.resetGame(); }
                });
        }
        
        
        
        
    /** Dies as a result of encountering a monster.  To do this we first clear out the entire
        schedule; this will eliminate everything because die() was itself scheduled at a half-time
        timestep so it's the only thing going on right now.  Clever right?  I know!  So awesome.  Anyway,
        we then schedule a little pause to occur.  Then afterwards we schedule a period where the pac
        spins around and around by changing his lastAction.  Then finally we wait a little bit more,
        then reset the agents so they're at their start locations again.
    */
        
    public void die()
        {
        pacman.deaths++;
        if (pacman.pacsLeft() > 1)
            {
            // there are other pacs playing.  We just delete ourselves.
            if (stopper != null) stopper.stop();
            stopper = null;
            pacman.agents.remove(this);
            pacman.pacs[tag] = null;
            return;
            }
                
        // okay so we're the last pac alive.  Let's do the little dance
                
        // clear out the schedule, we're done
        pacman.schedule.clear();

        // do a little pause
        pacman.schedule.scheduleOnce(
            new Steppable()
                {
                public int count = 0;
                public void step(SimState state) 
                    { 
                    if (++count < WAIT_TIME) pacman.schedule.scheduleOnce(this); 
                    } 
                });

        // wait a little more.
        pacman.schedule.scheduleOnceIn(WAIT_TIME,
            new Steppable()
                {
                public void step(SimState state)
                    {
                    // remove the Ghosts
                    Bag b = pacman.agents.getAllObjects();
                    for(int i = 0; i < b.numObjs; i++) {if (b.objs[i] != Pac.this) { b.remove(i); i--; } }
                    }
                });
                        
        // do a little spin
        pacman.schedule.scheduleOnceIn(WAIT_TIME + 1,
            new Steppable() 
                { 
                public int count = 0;
                public void step(SimState state) 
                    { 
                    if (count % SPIN_SPEED == 0) { lastAction = (lastAction + 1) % 4; }  // spin around
                    if (++count < SPIN_TIME) pacman.schedule.scheduleOnce(this); 
                    } 
                });

        // wait a little more, then reset the agents.
        pacman.schedule.scheduleOnceIn(WAIT_TIME * 2 + SPIN_TIME,
            new Steppable()
                {
                public void step(SimState state) { pacman.resetAgents(); }
                });
        }
    }
