/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.amapvox.viewer3d.loading.texture;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Texture {
        
    private int id;
    
    private boolean dirty = true; //if dirty is true it means texture needs an update
    
    private int width, height;
    private BufferedImage bufferedImage;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    

    public int getId() {
        return id;
    }
    
    public Texture(BufferedImage image){
        this.bufferedImage = image;
    }
    
    public static Texture createFromFile(GL3 gl, File file) throws IOException, Exception{
        
        try {
            BufferedImage image = ImageIO.read(file);
            
            Texture texture = new Texture();
            
            IntBuffer tmp = IntBuffer.allocate(1);
            gl.glGenTextures(1, tmp);
            texture.id = tmp.get(0);
            TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), image, false);
            gl.glEnable(GL3.GL_TEXTURE_2D);

            gl.glBindTexture(GL3.GL_TEXTURE_2D, texture.id);

            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);

            int internalFormat = textureData.getInternalFormat();
            int width = textureData.getWidth();
            int height = textureData.getHeight();
            int border = textureData.getBorder();
            int pixelFormat = textureData.getPixelFormat();
            int pixelType = textureData.getPixelType();
            Buffer buffer = textureData.getBuffer();


            try{
                gl.glTexImage2D(GL3.GL_TEXTURE_2D,         // target
                0,// level, 0 = base, no minimap,
                internalFormat, // internalformat
                width,// width
                height,    // height
                border,// border, always 0 in OpenGL ES
                pixelFormat,// format
                pixelType,// type
                buffer);
            }catch(Exception e){
                throw new Exception("cannot create texture from image", e);
            }

            gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

            texture.setWidth(width);
            texture.setHeight(height);

            return texture;
            
        } catch (IOException ex) {
            throw new IOException("cannot create texture from image", ex);
        }
    }
    
    public Texture(){
        
        
    }
    
    public void init(GL3 gl) throws Exception{
        
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenTextures(1, tmp);
        id = tmp.get(0);
        
        update(gl);
    }
    
    public void update(GL3 gl) throws Exception{
        
        if(bufferedImage == null){
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        
        TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), bufferedImage, false);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        
        gl.glBindTexture(GL3.GL_TEXTURE_2D, id);
        
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);

        int internalFormat = textureData.getInternalFormat();
        width = textureData.getWidth();
        height = textureData.getHeight();
        int border = textureData.getBorder();
        int pixelFormat = textureData.getPixelFormat();
        int pixelType = textureData.getPixelType();
        Buffer buffer = textureData.getBuffer();
        

        try{
            gl.glTexImage2D(GL3.GL_TEXTURE_2D,         // target
            0,// level, 0 = base, no minimap,
            internalFormat, // internalformat
            width,// width
            height,    // height
            border,// border, always 0 in OpenGL ES
            pixelFormat,// format
            pixelType,// type
            buffer);
        }catch(Exception e){
            throw new Exception("cannot create texture from image", e);
        }
            
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
        
        dirty = false;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }
}
