all: java

java:
	protoc message.proto --java_out=../shared/src/

#python:
#	protoc message.proto --python_out=../ubuntu/

clean:
	rm ../shared/src/com/jbull/hermes/Message.java ../ubuntu/message_pb2.py
