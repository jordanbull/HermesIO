import socket


class TCPClient:
    SIZE = 16384

    def __init__(self, host, port):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((host, port))

    def read(self, size=SIZE):
        data = self.sock.recv(size)
        return data

    def write(self, data):
        self.sock.send(data)

    def close(self):
        self.sock.close()