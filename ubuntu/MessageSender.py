import message_pb2

class MessageSender:

    def __init__(self, server):
        self.server = server

    def send(self, msg):
        i = 1
