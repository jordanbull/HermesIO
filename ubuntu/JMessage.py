import MessageHelper

__author__ = 'jbull'
from CommunicationManager import CommunicationManager
from TCPServer import TCPServer
from MessageSender import MessageSender
from MessageListener import MessageListener
import CommandLineSend
from multiprocessing import Process

PORT = 8888

server = TCPServer(PORT)
listener = MessageListener(server)
sender = MessageSender(server)
comm_manager = CommunicationManager(listener, sender)
comm_manager.enqueue(MessageHelper.create_sms("7073307917","this is a test message sent from my desktop"))
comm_manager.mode = comm_manager.LISTENING
comm_manager.loop()