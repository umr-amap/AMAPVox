/*
 This software is distributed WITHOUT ANY WARRANTY and without even the
 implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 This program is open-source LGPL 3 (see copying.txt).
 Authors:
 Gregoire Vincent    gregoire.vincent@ird.fr
 Julien Heurtebize   julienhtbe@gmail.com
 Jean Dauzat         jean.dauzat@cirad.fr
 Rémi Cresson        cresson.r@gmail.com

 For further information, please contact Gregoire Vincent.
 */
package org.amapvox.commons.raster.multiband;

/**
 *
 * @author calcul
 */
public class BCommon {

    public enum NumberOfBits {

        N_BITS_1((short) 1),
        N_BITS_4((short) 4),
        N_BITS_8((short) 8),
        N_BITS_16((short) 16),
        N_BITS_32((short) 32);

        private final short nBits;

        private NumberOfBits(short nBits) {
            this.nBits = nBits;
        }
        
        public short getNumberOfBits(){
            return nBits;
        }
    }

    public enum ByteOrder {

        LITTLE_ENDIAN("I"),
        BIG_ENDIAN("M");

        private final String byteOrder;

        private ByteOrder(String byteOrder) {
            this.byteOrder = byteOrder;
        }
        
        @Override
        public String toString(){
            return byteOrder;
        }
        
    }

    public enum PixelType {

        UNSIGNED_INT(""),
        SIGNED_INT("SIGNEDINT");

        private final String pixelType;

        private PixelType(String pixelType) {
            this.pixelType = pixelType;
        }
        
        @Override
        public String toString(){
            return pixelType;
        }
    }

    public enum Layout {

        BIL("bil"),
        BIP("bip"),
        BSQ("bsq");

        private final String layout;

        private Layout(String layout) {
            this.layout = layout;
        }
        
        @Override
        public String toString(){
            return layout;
        }
    }
    
    public static boolean[] getBooleanBitsArray(int input, int numberOfBits){
        
        boolean[] bits = new boolean[numberOfBits];
        
        for (int i = bits.length-1; i >= 0; i--) {
            bits[i] = (input & (1 << i)) != 0;
        }
        
        return bits;
    }
    
    public static byte encodebool(boolean[] array) {

        byte val = 0;
        
        int count = 0;
        
        for (boolean b : array) {
            
            if (b) {
                val |= (byte)(1 << count);
            }
            
            count++;
        }
        return val;
    }
}
