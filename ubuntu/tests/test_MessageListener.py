import unittest
import mock
import MessageListener
import TCPClient
import TCPServer
import MessageHelper
from multiprocessing import Process
import notifications


class TestMessageListener(unittest.TestCase):
    PORT = 8888

    def setUp(self):
        self.server = TCPServer.TCPServer(self.PORT)
        self.listener = MessageListener.MessageListener(self.server)
        self.sock = TCPClient.TCPClient('', self.PORT)
        notifications.notify_sms = mock.Mock

    def tearDown(self):
        self.server.close_server()
        self.sock.close()

    def test_listen(self):
        sms = MessageHelper.create_sms("to_num", "content")
        p = Process(target=self.listener.listen)
        p.start()
        header = MessageHelper.create_header(sms)
        self.sock.write(header.SerializeToString())
        data = self.sock.read()
        self.assertEqual(header.SerializeToString(), data)
        self.sock.close()
        self.sock = TCPClient.TCPClient('', self.PORT)
        self.sock.write(sms.SerializeToString())
        p.join()
