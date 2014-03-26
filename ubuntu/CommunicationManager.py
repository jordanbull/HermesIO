import message_pb2
import time

class CommunicationManager:
    LISTENING = 1
    SENDING = 2
    STOPPED = 3

    def __init__(self, listener, sender):
        # In listening mode by default since client controls flow
        self.mode = self.STOPPED
        self.queued_msgs = []
        self.listener = listener
        self.sender = sender

    def listen(self):
        cur_mode = self.mode
        while cur_mode == self.LISTENING:
            cur_mode = self.listener.listen()
        self.switch_mode(cur_mode)

    def send(self):
        while self.mode == self.SENDING and self.queue_size() > 0:
            msg = self.dequeue()
            self.sender.send(msg)
        send_done_msg = message_pb2.Mode()
        send_done_msg.lastUpdate = int(round(time.time() * 1000))
        send_done_msg.currentTimestamp = send_done_msg.lastUpdate
        send_done_msg.serverSend = False
        self.sender.send(send_done_msg)
        self.switch_mode(self.LISTENING)

    def switch_mode(self, to_mode):
        self.mode = to_mode
        if to_mode == self.LISTENING:
            self.listen()
        elif to_mode == self.SENDING:
            self.send()
        elif to_mode == self.STOPPED:
            i = 1
            # do nothing
        else:
            assert False
            #should not reach

    def enqueue(self, msg):
        self.queued_msgs.append(msg)

    def dequeue(self):
        return self.queued_msgs.pop(0)

    def queue_size(self):
        return len(self.queued_msgs)