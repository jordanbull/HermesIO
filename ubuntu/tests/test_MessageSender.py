import unittest
import MessageSender
import TCPClient
import TCPServer
import MessageHelper
from multiprocessing import Process


class TestTCPConnection(unittest.TestCase):
    PORT = 8888

    def setUp(self):
        self.server = TCPServer.TCPServer(self.PORT)
        self.sender = MessageSender.MessageSender(self.server)
        self.sock = TCPClient.TCPClient('', self.PORT)

    def tearDown(self):
        self.server.close_server()
        self.sock.close()

    def testSend(self):
        sms = MessageHelper.create_sms("to_num", "content")
        p = Process(target=self.sender.send, args=(sms,))
        p.start()
        data = self.sock.read()
        self.assertEqual(MessageHelper.create_header(sms).SerializeToString(), data)
        self.sock.write(data)
        self.sock.close()
        self.sock = TCPClient.TCPClient('', self.PORT)
        data = self.sock.read()
        p.join()
        self.assertEqual(sms.SerializeToString(), data)