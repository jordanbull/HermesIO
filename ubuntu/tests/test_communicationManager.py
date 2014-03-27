import unittest
import CommunicationManager
import mock
from mock import call
from mock import Mock

__author__ = 'jordan'


class TestCommunicationManager(unittest.TestCase):

    def setUp(self):
        self.mock_sender = mock.MagicMock()
        self.mock_listener = mock.MagicMock()
        self.comm = CommunicationManager.CommunicationManager(self.mock_listener, self.mock_sender)
        self.msg1 = "msg1"
        self.msg2 = "msg2"

    def test_dequeue(self):
        self.comm.enqueue(self.msg1)
        self.comm.enqueue(self.msg2)

        self.assertEqual(self.msg1, self.comm.dequeue())
        self.assertEqual(self.msg2, self.comm.dequeue())
        self.assertEqual(0, self.comm.queue_size())

    def test_queue_size(self):
        self.assertEqual(0, self.comm.queue_size())
        self.comm.enqueue(self.msg1)
        self.assertEqual(1, self.comm.queue_size())
        self.comm.enqueue(self.msg1)
        self.assertEqual(2, self.comm.queue_size())
        self.comm.dequeue()
        self.assertEqual(1, self.comm.queue_size())
        self.comm.dequeue()
        self.assertEqual(0, self.comm.queue_size())

    def test_send(self):
        self.comm.enqueue(self.msg1)
        self.comm.enqueue(self.msg2)
        self.comm.mode = self.comm.SENDING
        self.comm.send()
        calls = self.mock_sender.send.call_args_list
        expected = [mock.call(self.msg1), mock.call(self.msg2)]
        self.assertTrue(calls == expected)

    def test_listen(self):
        self.mock_listener.listen.return_value=self.comm.SENDING
        self.comm.mode = self.comm.LISTENING
        self.comm.listen()
        self.mock_listener.listen.assert_called_once_with()
        self.assertEqual(self.comm.SENDING, self.comm.mode)

        modes = [self.comm.LISTENING, self.comm.SENDING]
        def side_effect():
            return modes.pop(0)
        self.mock_listener.listen = mock.MagicMock(side_effect=side_effect)
        self.comm.listener = self.mock_listener
        self.comm.mode = self.comm.LISTENING
        self.comm.listen()
        self.assertEqual(self.mock_listener.listen.mock_calls, [call(), call()])
        self.assertEqual(self.comm.SENDING, self.comm.mode)