import MessageHelper


class MessageSender:

    def __init__(self, server):
        self.server = server

    def send(self, msg):
        self.server.accept()
        header = MessageHelper.create_header(msg)
        self.server.write(header.SerializeToString())
        data = self.server.read()
        self.server.close_sock()
        if data == header.SerializeToString():
            self.server.accept()
            self.server.write(msg.SerializeToString())
            self.server.close_sock()
        else:
            raise "problem getting header back"
