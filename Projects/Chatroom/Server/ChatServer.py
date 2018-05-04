import socket
import select
import sys
import json
import datetime
from thread import *

"""The first argument AF_INET is the address domain of the
socket. This is used when we have an Internet Domain with
any two hosts The second argument is the type of socket.
SOCK_STREAM means that data or characters are read in
a continuous flow."""
server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

# checks whether sufficient arguments have been provided
if len(sys.argv) != 1:
    print "Correct usage: script"
    exit()

# takes the first argument from command prompt as IP address
IP_address = ''

# takes second argument from command prompt as port number
Port = 1134

"""
binds the server to an entered IP address and at the
specified port number.
The client must be aware of these parameters
"""
server.bind((IP_address, Port))


server.listen(30)
userConnectionsList = {}
list_of_clients = []




def clientthread(conn, addr):
    sendingMessage = {}
    username = ''
    now = datetime.datetime.now()
    # sends a message to the client whose user object is conn
    #conn.send("Welcome to this chatroom!")

    while True:
            try:
                message = conn.recv(2048)
                jsonObject = json.loads(message)
                if 'username' in jsonObject:
                    username = jsonObject['username']
                    if username in userConnectionsList:
                        print "Username exists"
                        sendingMessage['isConnected'] = False
                        sendingMessage['errorCode'] = 1
                        conn.send(json.dumps(sendingMessage))
                        conn.close()
                    else:
                        userConnectionsList[username] = conn
                        print (username + " added to list")
                        sendingMessage['dm'] = ''
                        sendingMessage['message'] = '%s has entered the chat'% username
                        sendingMessage['sender'] = ''
                        sendingMessage['length'] = len(sendingMessage['message'])
                        sendingMessage['date'] = now.strftime("%Y-%m-%d %H:%M:%S")
                        # conn.send(json.dumps(sendingMessage))
                        broadcast(json.dumps(sendingMessage), conn)
                else :
                    # print "Json object: ", jsonObject
                    if 'dm' in jsonObject:
                        sendingMessage['dm'] = jsonObject['dm']
                        # print "moves into if statement", jsonObject['dm']
                        if sendingMessage['dm'] is '':
                            sendingMessage['message'] = jsonObject['message']
                            sendingMessage['sender'] = jsonObject['sender']
                            sendingMessage['length'] = int(jsonObject['length'])
                            sendingMessage['date'] = jsonObject['date']
                            # conn.send(json.dumps(sendingMessage))
                            broadcast(json.dumps(sendingMessage), conn)
                        else:
                            sendingMessage['message'] = jsonObject['message']
                            sendingMessage['sender'] = jsonObject['sender']
                            sendingMessage['length'] = int(jsonObject['length'])
                            sendingMessage['date'] = jsonObject['date']
                            # print "sending message: ", sendingMessage
                            # conn.send(json.dumps(sendingMessage))
                            broadcast(json.dumps(sendingMessage), conn)
                    else:
                        if 'disconnected' in jsonObject:
                            # sendingMessage['disconnected'] = jsonObject['disconnected']
                            username = jsonObject['sender']
                            sendingMessage['dm'] = ''
                            sendingMessage['message'] = '%s left the server', username
                            sendingMessage['sender'] = 'Server'
                            sendingMessage['length'] = len(sendingMessage['message'])
                            sendingMessage['date'] = now.strftime("%Y-%m-%d %H:%M:%S")
                            broadcast(sendingMessage, conn)
                            remove(username)
                            conn.close()

            except Exception:
                #print "continue"
                continue


"""Using the below function, we broadcast the message to all
clients who's object is not the same as the one sending
the message """
def broadcast(message, connection):
    for username in userConnectionsList:
        #if clients!=connection:
        try:
            if userConnectionsList[username] is not connection:
                userConnectionsList[username].send(message)
            # print "Sent", message
        except:
            userConnectionsList[username].close()

            # if the link is broken, we remove the client
            remove(username)

"""The following function simply removes the object
from the list that was created at the beginning of
the program"""
def remove(username):
    if username in userConnectionsList:
        userConnectionsList.remove(username)



"""
This is the next thing that needs implemented. it is extra credit
"""
def directMessage(sendingMessage, conn):
    for dm in sendingMessage['dm']:
        userConnectionsList[dm].send(json.loads(sendingMessage))
    pass

def setDate(self):
    #YYYY-MM-DD-HH-mm-SS
    year = datetime.date.year
    month = datetime.date.month
    day = datetime.date.day
    hour = datetime.time.hour
    minute = datetime.time.minute
    second = datetime.time.second
while True:

    """Accepts a connection request and stores two parameters,
    conn which is a socket object for that user, and addr
    which contains the IP address of the client that just
    connected"""

    conn, addr = server.accept()

    """Maintains a list of clients for ease of broadcasting
    a message to all available people in the chatroom"""
    list_of_clients.append(conn)


    # creates and individual thread for every user
    # that connects

    start_new_thread(clientthread,(conn,addr))
