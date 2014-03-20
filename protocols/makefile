all: java python

java:
	protoc message.proto --java_out=../android/src/

python:
	protoc message.proto --python_out=../ubuntu/

clean:
	rm ../android/src/org/jbull/jmessage/Message.java ../ubuntu/message_pb2.py
