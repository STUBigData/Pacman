# server.py
import time, socket, sys, random

print("Initialising....\n")
time.sleep(1)

s = socket.socket()
host = socket.gethostname()
ip = socket.gethostbyname(host)
port = 1234
s.bind((host, port))
print(host, "(", ip, ")\n")
           
s.listen(1)
print("\nWaiting for incoming connections...\n")
conn, addr = s.accept()
print("Received connection from ", addr[0], "(", addr[1], ")\n")



while True:
    message = conn.recv(1024)
    message = message.decode()
    print(message)
    actions = "NEWS"
    action = actions[random.randint(0,3)]
    outgoingMessage = "0:" + action + "\n"
    conn.send(outgoingMessage.encode())
