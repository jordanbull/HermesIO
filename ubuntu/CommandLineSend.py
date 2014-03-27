import MessageHelper


def sms_from_std_in(comm_manager):
    while True:
        number = raw_input("Enter the number you would like to send an SMS to:")
        comm_manager.enqueue(MessageHelper.create_sms(number, "This is a test message sent from my computer"))