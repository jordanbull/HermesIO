__author__ = 'jbull'
from CommunicationManager import CommunicationManager
from TCPServer import TCPServer
from MessageSender import MessageSender
from MessageListener import MessageListener

PORT = 8888

server = TCPServer(PORT)
listener = MessageListener(server)
sender = MessageSender(server)
comm_manager = CommunicationManager(listener, sender)

comm_manager.listen()