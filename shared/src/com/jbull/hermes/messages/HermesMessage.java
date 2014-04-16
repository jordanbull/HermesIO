package com.jbull.hermes.messages;

import com.google.protobuf.AbstractMessageLite;

import java.lang.reflect.Constructor;

public abstract class HermesMessage<T extends AbstractMessageLite> {
    public abstract T getProtobufRep();

    public static <T extends AbstractMessageLite, C extends HermesMessage<T>> C createFromProtobufRep(T protobufRep, Class<C> c) {
        try {
            Constructor<C> constr = c.getDeclaredConstructor(new Class[0]);
            constr.setAccessible(true);
            return (C) constr.newInstance(new Class[0]).fromProtobufRep(protobufRep);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public abstract HermesMessage<T> fromProtobufRep(T protobufRep);
}
