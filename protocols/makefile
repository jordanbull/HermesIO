all: java

java:
	protoc protobuf_rep.proto --java_out=../shared/src/

clean:
	rm ../shared/src/com/jbull/hermes/messages/ProtobufRep.java
