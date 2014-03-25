

class CommunicationManager:
    LISTENING = 1
    SENDING = 2

    def __init__(self, listener, sender):
        # In listening mode by default since client controls flow
        self.mode = self.LISTENING
        self.queued_msgs = []
        self.listener = listener
        self.sender = sender

    def listen(self):
        while self.mode == self.LISTENING:
            self.mode = self.listener.listen()
        self.send()

    def send(self):
        while self.mode == self.SENDING and self.queue_size() > 0:
            msg = self.dequeue()
            self.sender.send(msg)
        self.listen()

    def enqueue(self, msg):
        self.queued_msgs.append(msg)

    def dequeue(self):
        return self.queued_msgs.pop(0)

    def queue_size(self):
        return len(self.queued_msgs)