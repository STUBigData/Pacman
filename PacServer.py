# PacServer.py
# This script accepts a single connection to a PacMan simulator,
# accepts the simulation's state and returns a random action.
# The simulation then performs the provided action.
# The code is heavily inspired from Ashok Kumar T's:
# https://www.biob.in/2018/04/simple-server-and-client-chat-using.html
# Author: S. Mondesire (c) 2019 All rights reserved.

import time, socket, sys, random

print("PacMan Server: Initializing\n")
time.sleep(1)

# Create the socket to listen to the port
s = socket.socket()
host = socket.gethostname()
ip = socket.gethostbyname(host)
port = 1234
s.bind((host, port))
print(host, "(", ip, ")\n")
           
s.listen(1)

# Port is being listened to, await a connection
print("\nAwaiting incoming connections.\n")
conn, addr = s.accept()
print("Connection received by ", addr[0], "(", addr[1], ")\n")


# Receive the state and return a random action
while True:
    # Receive the next message and output it
    message = conn.recv(1024)
    message = message.decode()
    print(message)

    # Select a random action and return it to the PacmanClient
    actions = "NEWS"
    action = actions[random.randint(0,3)]
    # The action is always selected for the first Pac
    outgoingMessage = "0:" + action + "\n"
    conn.send(outgoingMessage.encode())
