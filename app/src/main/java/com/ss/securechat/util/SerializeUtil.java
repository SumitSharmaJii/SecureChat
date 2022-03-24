package com.ss.securechat.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeUtil {

    public static byte[] serialize(Serializable value) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try(ObjectOutputStream outputStream = new ObjectOutputStream(out)){
            outputStream.writeObject(value);
            out.flush();
        }
        return out.toByteArray();
    }

    public static <T extends Serializable> T deserialize(byte[] data) throws  IOException, ClassNotFoundException{
        try(ByteArrayInputStream bis = new ByteArrayInputStream(data)){
            return (T) new ObjectInputStream(bis).readObject();
        }
    }

}
