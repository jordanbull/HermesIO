import socket


class TCPServer:
    SIZE = 16384

    def __init__(self, port):
        self.servsock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.servsock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.servsock.bind(('', port))
        self.servsock.listen(5)

    def accept(self):
        self.sock, addr = self.servsock.accept()

    def read(self, size=SIZE):
        data = self.sock.recv(size)
        return data

    def write(self, data):
        self.sock.sendall(data)

    def close_sock(self):
        if self.sock:
            self.sock.close()

    def close_server(self):
        self.servsock.close()

