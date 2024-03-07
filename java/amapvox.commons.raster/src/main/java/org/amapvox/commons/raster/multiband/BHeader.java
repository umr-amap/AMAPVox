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

import org.amapvox.commons.raster.multiband.BCommon.ByteOrder;
import org.amapvox.commons.raster.multiband.BCommon.Layout;
import org.amapvox.commons.raster.multiband.BCommon.NumberOfBits;
import org.amapvox.commons.raster.multiband.BCommon.PixelType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



/**
 *
 * @author calcul
 */


public class BHeader {
    
    
    private int nrows;
    private int ncols;
    private int nbands;
    private NumberOfBits nbits;
    private PixelType pixelType;
    private ByteOrder byteorder;
    private Layout layout;
    private int skipbytes;
    private double ulxmap;
    private double ulymap;
    private float xdim;
    private float ydim;
    private int bandrowbytes;
    private int totalrowbytes;
    private int bandgapbytes;
    private int byteNumber;
    private Map<String, String> metadata = new HashMap<>();

    public BHeader(int ncols, int nrows, int nbands, NumberOfBits numberOfBits) {
        
        this.nrows = nrows;
        this.ncols = ncols;
        this.nbands = nbands;
        this.nbits = numberOfBits;
        this.pixelType = PixelType.UNSIGNED_INT;
        this.byteorder = ByteOrder.LITTLE_ENDIAN;
        this.layout = Layout.BSQ;
        this.skipbytes = 0;
        this.ulxmap = 0;
        this.ulymap = 0;
        this.xdim = 1;
        this.ydim = 1;
        
        this.byteNumber = (int) Math.ceil( nbits.getNumberOfBits()/8);
        this.bandrowbytes = (int) Math.ceil((ncols*nbits.getNumberOfBits())/8);
        
        switch(layout){
            case BIL:
                this.totalrowbytes = nbands*bandrowbytes;
                break;
            case BIP:
                this.totalrowbytes = (int)Math.ceil((ncols*nbands*nbits.getNumberOfBits())/8);
                break;
            default:
                this.totalrowbytes = bandrowbytes;
        }
        
        this.bandgapbytes = 0;
    }
    

    public BHeader(int nrows, int ncols, int nbands, NumberOfBits nbits, PixelType pixelType, ByteOrder byteorder, Layout layout, int skipbytes, int ulxmap, int ulymap, short xdim, short ydim, int bandrowbytes, int totalrowbytes, int bandgapbytes) {
        this.nrows = nrows;
        this.ncols = ncols;
        this.nbands = nbands;
        this.nbits = nbits;
        this.pixelType = pixelType;
        this.byteorder = byteorder;
        this.layout = layout;
        this.skipbytes = skipbytes;
        this.ulxmap = ulxmap;
        this.ulymap = ulymap;
        this.xdim = xdim;
        this.ydim = ydim;
        this.bandrowbytes = (int) Math.ceil((ncols*nbits.getNumberOfBits())/8);
        this.totalrowbytes = totalrowbytes;
        this.bandgapbytes = bandgapbytes;
    }
    
    
    
    @Override
    public String toString(){
        
        String result = "nrows "+nrows+"\n"+
               "ncols "+ncols+"\n"+
                "nbands "+nbands+"\n"+
                "nbits "+nbits.getNumberOfBits()+"\n"+
                "pixeltype "+pixelType+"\n"+
                "byteorder "+byteorder+"\n"+
                "layout "+layout+"\n"+
                "skipbytes "+skipbytes+"\n"+
                "ulxmap "+ulxmap+"\n"+
                "ulymap "+ulymap+"\n"+
                "xdim "+xdim+"\n"+
                "ydim "+ydim+"\n"+
                "bandrowbytes "+bandrowbytes+"\n"+
                "totalrowbytes "+totalrowbytes+"\n"+
                "bandgapbytes "+bandgapbytes+"\n";
        
        Iterator<Map.Entry<String, String>> iterator = metadata.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            result += "#"+next.getKey()+" "+next.getValue()+"\n";
        }
        
        return result;
    }

    public int getNrows() {
        return nrows;
    }

    public int getNcols() {
        return ncols;
    }

    public int getNbands() {
        return nbands;
    }

    public NumberOfBits getNbits() {
        return nbits;
    }

    public PixelType getPixelType() {
        return pixelType;
    }

    public ByteOrder getByteorder() {
        return byteorder;
    }

    public Layout getLayout() {
        return layout;
    }

    public int getSkipbytes() {
        return skipbytes;
    }

    public double getUlxmap() {
        return ulxmap;
    }

    public double getUlymap() {
        return ulymap;
    }

    public float getXdim() {
        return xdim;
    }

    public float getYdim() {
        return ydim;
    }

    public int getBandrowbytes() {
        return bandrowbytes;
    }

    public int getTotalrowbytes() {
        return totalrowbytes;
    }

    public int getBandgapbytes() {
        return bandgapbytes;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public void setNbands(short nbands) {
        this.nbands = nbands;
    }

    public void setNbits(NumberOfBits nbits) {
        this.nbits = nbits;
    }

    public void setPixelType(PixelType pixelType) {
        this.pixelType = pixelType;
    }

    public void setByteorder(ByteOrder byteorder) {
        this.byteorder = byteorder;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public void setSkipbytes(int skipbytes) {
        this.skipbytes = skipbytes;
    }

    public void setUlxmap(double ulxmap) {
        this.ulxmap = ulxmap;
    }

    public void setUlymap(double ulymap) {
        this.ulymap = ulymap;
    }

    public void setXdim(float xdim) {
        this.xdim = xdim;
    }

    public void setYdim(float ydim) {
        this.ydim = ydim;
    }

    public void setBandrowbytes(int bandrowbytes) {
        this.bandrowbytes = bandrowbytes;
    }

    public void setTotalrowbytes(int totalrowbytes) {
        this.totalrowbytes = totalrowbytes;
    }

    public void setBandgapbytes(int bandgapbytes) {
        this.bandgapbytes = bandgapbytes;
    }

    public int getByteNumber() {
        return byteNumber;
    }
    
    public void addMetadata(String key, String value){
        metadata.put(key, value);
    }
    
    public void removeMetadata(String key){
        metadata.remove(key);
    }
}
