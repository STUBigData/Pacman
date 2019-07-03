# PacMan
PacMan Simulation and Server

The PacMan simulator is a multiplayer version of PacMan that connects to a server, transmits's the Pacs' states and receives actions (up, down, left, right directions). The Ghosts are controlled by AI that is built in the simulation, not controlled by the server.

This project extends the work of Sean Luke and MASON. The PacMan java code requires Mason version 19 to run.

# Set Up
For both components to communicate, the server and the client must be running and configured to establish a connection.

## Server:
The server is a python 3 script that listens on port 1234. It will accept a connection from the PacMan simulator, output the json (RFC 4627 json standard) state at every simulation step, and send a random (N,E,W,S) action for PacMan 0. The server must be running before the simulation can connect to it.

python PacServer.py

## Client:
Go to MASON's page and download the entire source. Place the files in the sim folder into the PacMan director of MASON's source.
https://cs.gmu.edu/~eclab/projects/mason/

### GUI: 
Run PacManWithUI to see the gui of the simulation. Run with the following parameters:

java PacManWithUI [number of Pacs] [Server IP] [Server Port] [Steps to transmit interval]

java -jar PacManWithUI_Client.jar 1 127.0.0.1 1234 2

 - Number of Pacs: (int) - The number of pacmans in the simulator. They can be controled by index, starting with 0.
 - Server IP: IP of the python server that is running: 127.0.0.1
 - ServerPort: (int) - Port of the python server: 1234
 - Steps to transmit interval: (int) - The number of simulation steps to transmit the state to the server. The simulation will only transmit the state ever n steps. This variable is used to reduce lag and trashing of the client but the higher the number, the less frequent the state will be analyzed and an action will be returned.
 
### Headless: 
The simulation can run headless, without the GUI. The parameters are the same as PacManWithUI:
 
 java PacManWithUI [number of Pacs] [Server IP] [Server Port] [Steps to transmit interval]
 
 java -jar PacMan_Client.jar 1 127.0.0.1 1234 2
 
## Output:
Client transmits a json state to the server. Here is an example of what is transmitted to the server from the client:

{ "host": "UnknownHost", "date": "06/21/2019", "time": "00:24:15", "energizers": [ { "x": 1, "y": 5 }, { "x": 26, "y": 5 } ], "dots": [ { "x": 0, "y": 16 }, { "x": 1, "y": 3 } ], "ghosts": [ { "name": "Ghost_Blinky", "x": 13.5, "y": 13.0, "frightenTime": 0, "lastAction": "W" }, { "name": "Ghost_Pinky", "x": 12.5, "y": 16.0, "frightenTime": 0, "lastAction": "W" }, { "name": "Ghost_Inky", "x": 13.5, "y": 16.0, "frightenTime": 0, "lastAction": "W" }, { "name": "Ghost_Clyde", "x": 14.5, "y": 16.0, "frightenTime": 0, "lastAction": "W" } ], "pacs": [ { "name": "Pac1", "x": 21.0, "y": 25.0, "lastAction": "E" } ], "dotsRemaining": 292, "energizersRemaining": 4, "deaths": 0, "level": 1, "step": 100, "score":80 }

 The server will output each state it receives in json format. At the end of the game, by default when the first Pac dies or completes level 1, the simulation will output a score and the number of steps that elapsed in the game in the format "score:180\nsteps:270" to the console, where score and steps are on separate lines.
 
 # Acknowledgement:
 Credit for the PacMan simulation implementation to Sean Luke and Vittorio Zipparo. The work here extends their work to allow remote decision making for the Pacs.
