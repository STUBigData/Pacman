package sim.app.pacman;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class PacClient extends Thread{

    final Scanner in; 
    final PrintWriter out; 
    final Socket socket; 
    PacMan pacman;
  
    // Constructor 
    public PacClient(Socket socket, Scanner in, PrintWriter out, PacMan pacman)  
    { 
        this.socket = socket; 
        this.in = in; 
        this.out = out; 
        this.pacman = pacman;
    } 
  
  
    public void run()  {
    	
        while (true)  { 
	       if (in.hasNextLine()) {
				String line = in.nextLine();
				int pacIndex = Integer.parseInt(line.substring(0, 1));
				char action = line.charAt(2);
				//System.out.println(pacIndex + " " + action);
				//System.out.println(line);
				switch(action) {
					case 'N':
						pacman.actions[pacIndex] = Pac.N;
						break;
					case 'E':
						pacman.actions[pacIndex] = Pac.E;
						break;
					case 'W':
						pacman.actions[pacIndex] = Pac.W;
						break;
					case 'S':
						pacman.actions[pacIndex] = Pac.S;
						break;
				}			
				
				
			} 
        } 
        
    } 
}
