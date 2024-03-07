/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.io;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author calcul
 */
public class LittleEndianUtility {
    
    public static short bytesToShort(byte b1, byte b2) {

        byte[] bytes = new byte[]{b1, b2};
        short result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return result;
    }

    public static int bytesToShortInt(byte b1, byte b2) {

        byte b3 = 0, b4 = 0;
        return bytesToInt(b1, b2, b3, b4);
    }

    public static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += ((256 + bytes[i]) % 256) * (int) Math.pow(256, i);
        }
        return result;
    }

    public static int bytesToInt(byte b1, byte b2, byte b3, byte b4) {

        byte[] bytes = new byte[]{b1, b2, b3, b4};
        int result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return result;
    }
    
    public static int toInt(byte b1, byte b2, byte b3, byte b4) {

        byte[] bytes = new byte[]{b4, b3, b2, b1};
        int d = ByteBuffer.wrap(bytes).getInt();
        return d;
    }
    
    public static long tolong(byte[] bytes) {

        byte[] newArray = new byte[bytes.length];
        
        for(int i = 0;i<newArray.length;i++){
            newArray[i] = bytes[bytes.length - i - 1];
        }
        
        long d = ByteBuffer.wrap(newArray).getLong();
        return d;
    }

    public static double toDouble(byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7, byte byte8) {

        byte[] bytes = new byte[]{byte8, byte7, byte6, byte5, byte4, byte3, byte2, byte1};
        double d = ByteBuffer.wrap(bytes).getDouble() + 0.0;
        return d;
    }
    
    public static float toFloat(byte byte1, byte byte2, byte byte3, byte byte4) {

        byte[] bytes = new byte[]{byte4, byte3, byte2, byte1};
        float value = ByteBuffer.wrap(bytes).getFloat()+ 0.0f;
        return value;
    }

    public static BigInteger toBigInteger(byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7, byte byte8) {

        byte[] bytes = new byte[]{byte8, byte7, byte6, byte5, byte4, byte3, byte2, byte1};
        BigInteger bi = new BigInteger(bytes);
        return bi;
    }
}
