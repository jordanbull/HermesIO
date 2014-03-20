import message_pb2
import notifications
import TCPServer


class MessageHandler:

    HOST = ''
    PORT = 8888
    BUFFER_SIZE = 16384

    def __init__(self):
        self.server = TCPServer.TCPServer(self.HOST, self.PORT)
        self.listen()

    def listen(self):
        while True:
            self.server.accept()
            data = self.server.read()
            if data:
                self.handle_incoming_message(data)
            self.server.close_sock()

    def handle_incoming_message(self, header_data):
        length, msg_type = self.read_header(header_data)
        print "received header"
        print length
        nread = 0
        msg_data = ''
        self.server.accept()
        while nread < length:
            msg_data += self.server.read()
            nread = len(msg_data)
        self.message_types[msg_type](self, msg_data)

    def read_header(self, data):
        header = message_pb2.Header()
        header.ParseFromString(data)
        self.server.write(data)
        self.server.close_sock()
        return header.length, header.type

    def setup(self, data):
        msg = message_pb2.SetupMessage()
        msg.ParseFromString(data)
        print "received data:", msg

    def incoming_sms(self, data):
        sms = message_pb2.SmsMessage()
        sms.ParseFromString(data)
        notifications.notify_sms(sms.sender.name, sms.content, sms.sender.image)

    message_types = {message_pb2.Header.SMSMESSAGE: incoming_sms,
                     message_pb2.Header.SETUPMESSAGE: setup,
                     }