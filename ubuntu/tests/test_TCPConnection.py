import unittest
import TCPClient
import TCPServer


class TestTCPConnection(unittest.TestCase):
    HOST = ''
    PORT = 8888

    def setUp(self):
        self.server = TCPServer.TCPServer(self.HOST, self.PORT)
        self.client = TCPClient.TCPClient(self.HOST, self.PORT)
        self.server.accept()
        self.data = "12345"

    def tearDown(self):
        self.server.close_sock()
        self.server.close_server()
        self.client.close()

    def test_client_to_server(self):
        self.client.write(self.data)
        output = self.server.read()
        self.assertEqual(self.data, output)

    def test_server_to_client(self):
        self.server.write(self.data)
        output = self.client.read()
        self.assertEqual(self.data, output)

    def test_echo(self):
        self.client.write(self.data)
        output = self.server.read()
        self.server.write(output)
        echo = self.client.read()
        self.assertEqual(self.data, echo)

    def test_double_echo(self):
        self.client.write(self.data)
        output = self.server.read()
        self.server.write(output)
        output = self.client.read()
        self.client.write(output)
        output = self.server.read()
        self.server.write(output)
        output = self.client.read()
        self.assertEqual(self.data, output)


if __name__ == '__main__':
    unittest.main()