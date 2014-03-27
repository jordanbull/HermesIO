import MessageHelper


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
        while self.mode == self.LISTENING:
            self.mode = self.listener.listen()

    def send(self):
        while self.mode == self.SENDING and self.queue_size() > 0:
            msg = self.dequeue()
            self.sender.send(msg)
        self.mode = self.LISTENING

    def done_sending(self):
        send_done_msg = MessageHelper.create_mode()
        self.sender.send(send_done_msg)

    def loop(self):
        while not self.mode == self.STOPPED:
            if self.mode == self.LISTENING:
                self.listen()
            elif self.mode == self.SENDING:
                self.send()
                self.done_sending()
            elif self.mode == self.STOPPED:
                break
            else:
                assert False
                #should not reach

    def enqueue(self, msg):
        self.queued_msgs.append(msg)

    def dequeue(self):
        return self.queued_msgs.pop(0)

    def queue_size(self):
        return len(self.queued_msgs)