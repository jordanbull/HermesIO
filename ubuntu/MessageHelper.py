import message_pb2
import time

msg_num = 0


def create_header(msg):
    if isinstance(msg, message_pb2.SmsMessage):
        msg_type = message_pb2.Header.SMSMESSAGE
    elif isinstance(msg, message_pb2.Mode):
        msg_type = message_pb2.Header.MODE
    else:
        assert False
    global msg_num
    msg_num += 1
    header = message_pb2.Header()
    length = len(msg.SerializeToString())
    header.length = length
    header.msgNum = msg_num
    header.type = msg_type
    return header


def create_sms(to_number, content, from_number="", to_name=""):
    sms = message_pb2.SmsMessage()
    sms.timeStamp = time_millis()
    recipent = sms.recipents.add()
    recipent.phoneNumber = to_number
    recipent.name = to_name
    sms.content = content
    sender = sms.sender
    sender.phoneNumber = from_number
    sender.name = "me"
    return sms


def create_mode():
    mode = message_pb2.Mode()
    mode.lastUpdate = time_millis()
    mode.currentTimestamp = mode.lastUpdate
    mode.serverSend = False
    return mode


def time_millis():
    return int(round(time.time() * 1000))
