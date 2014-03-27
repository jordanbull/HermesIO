import CommunicationManager
import message_pb2
import notifications


class MessageListener:

    BUFFER_SIZE = 16384

    def __init__(self, server):
        self.server = server

    def listen(self):
        self.server.accept()
        data = self.server.read()
        start_sending = False
        if data:
            start_sending = self.handle_incoming_message(data)
        self.server.close_sock()
        #TODO return send if receive send
        if start_sending:
            return CommunicationManager.CommunicationManager.SENDING
        else:
            return CommunicationManager.CommunicationManager.LISTENING

    def handle_incoming_message(self, header_data):
        length, msg_type = self.read_header(header_data)
        print "received header: " + str(msg_type)
        print length
        n_read = 0
        msg_data = ''
        self.server.accept()
        while n_read < length:
            print "n_read " + str(n_read)
            msg_data += self.server.read()
            n_read = len(msg_data)
        return message_types[msg_type](msg_data)

    def read_header(self, data):
        header = message_pb2.Header()
        header.ParseFromString(data)
        self.server.write(data)
        self.server.close_sock()
        return header.length, header.type


def setup(data):
    msg = message_pb2.SetupMessage()
    msg.ParseFromString(data)
    print "received data:", msg
    return False


def incoming_sms(data):
    sms = message_pb2.SmsMessage()
    sms.ParseFromString(data)
    print sms.sender.name
    print sms.content
    notifications.notify_sms(sms.sender.name, sms.content, sms.sender.image)
    return False


def incoming_mode(data):
    mode = message_pb2.Mode()
    mode.ParseFromString(data)
    print mode
    return mode.serverSend

message_types = {message_pb2.Header.SMSMESSAGE: incoming_sms,
                     message_pb2.Header.SETUPMESSAGE: setup,
                     message_pb2.Header.MODE: incoming_mode,
                     }