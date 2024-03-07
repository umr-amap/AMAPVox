/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @see <a href= "http://jessicarbrown.com/resources/unsignedtojava.html">http://jessicarbrown.com/resources/unsignedtojava.html</a>
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ByteConverter {
    
    public static int unsignedShortToInteger(int unsignedShort){
        return (int)(unsignedShort & 0xffff);
    }
    
    public static int unsignedByteToShort(byte unsignedByte){
        return (int)(unsignedByte & 0xff);
    }
    
    public static long unsignedIntegerToLong(int unsignedInteger){
        
        return (long)(unsignedInteger & 0xffffffffL);
    }
    
    public static short bytesToShort(byte b1, byte b2) {

        byte[] bytes = new byte[]{b1, b2};
        short result = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return result;
    }

    public static int bytesToShortInt(byte b1, byte b2) {

        byte b3 = 0, b4 = 0;
        return ByteConverter.bytesToInt(b1, b2, b3, b4);
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

    public static double toDouble(byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7, byte byte8) {

        byte[] bytes = new byte[]{byte8, byte7, byte6, byte5, byte4, byte3, byte2, byte1};
        double d = ByteBuffer.wrap(bytes).getDouble() + 0.0;
        return d;
    }

}
