import unittest
import CommunicationManager

__author__ = 'jordan'


class TestCommunicationManager(unittest.TestCase):

    def setUp(self):
        self.sender = TestSender()
        self.comm = CommunicationManager.CommunicationManager(TestListener(), self.sender)
        self.msg1 = "msg1"
        self.msg2 = "msg2"
        self.empty_queue = []
        self.queue_msg1 = [self.msg1]
        self.queue_msg2 = [self.msg2]
        self.queue_2msgs = [self.msg1, self.msg2]

    """
        def tearDown(self):
    """

    def test_enqueue(self):
        self.assertEqual(self.empty_queue, self.comm.queued_msgs)
        self.comm.enqueue(self.msg1)
        self.assertEqual(self.queue_msg1, self.comm.queued_msgs)
        self.comm.enqueue(self.msg2)
        self.assertEqual(self.queue_2msgs, self.comm.queued_msgs)

    def test_dequeue(self):
        self.comm.enqueue(self.msg1)
        self.comm.enqueue(self.msg2)
        self.assertEqual(self.queue_2msgs, self.comm.queued_msgs)
        msg = self.comm.dequeue()
        self.assertEqual(self.msg1, msg)
        self.assertEqual(self.queue_msg2, self.comm.queued_msgs)
        msg = self.comm.dequeue()
        self.assertEqual(self.msg2, msg)
        self.assertEqual(self.empty_queue, self.comm.queued_msgs)

    def test_queue_size(self):
        s = self.comm.queue_size()
        self.assertEqual(0, s)
        self.comm.enqueue(self.msg1)
        s = self.comm.queue_size()
        self.assertEqual(1, s)
        self.comm.enqueue(self.msg1)
        s = self.comm.queue_size()
        self.assertEqual(2, s)
        self.comm.dequeue()
        s = self.comm.queue_size()
        self.assertEqual(1, s)
        self.comm.dequeue()
        s = self.comm.queue_size()
        self.assertEqual(0, s)

    def test_send(self):
        self.comm.mode = self.comm.SENDING
        self.comm.enqueue(self.msg1)
        self.comm.enqueue(self.msg2)
        self.comm.send()
        self.assertEqual(self.queue_2msgs, self.sender.sent_msgs)


class TestListener:
    def __init__(self):
        self.msgs_read = []


class TestSender:
    def __init__(self):
        self.sent_msgs = []

    def send(self, msg):
        self.sent_msgs.append(msg)