# PacMan
PacMan Simulation and Server

The PacMan simulator is a multiplayer version of PacMan that connects to a server, transmits's the Pacs' states and receives actions (up, down, left, right directions). The Ghosts are controlled by AI that is built in the simulation, not controlled by the server.

This project extends the work of Sean Luke and MASON. The PacMan java code requires Mason version 19 to run.

# Set Up
Server:
The server is a python 3 script that listens on port 1234. It will accept a connection from the PacMan simulator, output the json state at every simulation step, and send a random (N,E,W,S) action for PacMan 0. The server must be running before the simulation can connect to it.
python PacServer.py

Client:
Go to MASON's page and download the entire source. Place the files in the sim folder into the PacMan director of MASON's source.
https://cs.gmu.edu/~eclab/projects/mason/

GUI: Run PacManWithUI to see the gui of the simulation. Run with the following parameters:

java PacManWithUI [number of Pacs] [Server IP] [Server Port] [Steps to transmit interval]

 - Number of Pacs: (int) - The number of pacmans in the simulator. They can be controled by index, starting with 0.
 - Server IP: IP of the python server that is running: 127.0.0.1
 - ServerPort: (int) - Port of the python server: 1234
 - Steps to transmit interval: (int) - The number of simulation steps to transmit the state to the server. The simulation will only transmit the state ever n steps. This variable is used to reduce lag and trashing of the client but the higher the number, the less frequent the state will be analyzed and an action will be returned.
 
 Headless: The simulation can run headless, without the GUI. The parameters are the same as PacManWithUI:
 
 java PacManWithUI [number of Pacs] [Server IP] [Server Port] [Steps to transmit interval]
 
 Output:
 The server will output each state it receives in json format. At the end of the game, by default when the first Pac dies or the game reaches level 4, the simulation will output a score in the format "score:180" to the console.
 
 # Acknowledgement:
 Credit for the PacMan simulation implementation to Sean Luke and Vittorio Zipparo. The work here extends their work to allow remote decision making for the Pacs.
