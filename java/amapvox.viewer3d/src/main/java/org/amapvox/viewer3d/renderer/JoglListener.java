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
package org.amapvox.viewer3d.renderer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import org.amapvox.viewer3d.event.EventManager;
import org.amapvox.viewer3d.object.scene.Scene;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JoglListener implements GLEventListener {

    private final static Logger LOGGER = Logger.getLogger(JoglListener.class.getCanonicalName());

    private final List<EventManager> eventListeners;
    private final Scene scene;
    private Vector3f worldColor;
    private final static Vector3f DEFAULT_WORLD_COLOR = new Vector3f(0.78f, 0.78f, 0.78f);

    private int width;
    private int height;

    public int viewportWidth;
    public int viewportHeight;

    private int startX = 0;
    private int startY = 0;

    private boolean justOnce = true;
    private final FPSAnimator animator;
    private boolean dynamicDraw = false;

    private int depthrenderbufferID;
    private int fboBufferID;
    private IntBuffer drawBuffers;
    private int screenshotTexture;
    private boolean screenshotThingsDirty = true;
    private boolean takeScreenShot;
    private RenderListener renderListener;

    public Scene getScene() {
        return scene;
    }

    public Vector3f getWorldColor() {
        return worldColor;
    }

    public void setWorldColor(Vector3f worldColor) {
        this.worldColor = worldColor;
    }

    public void setWorldColor(int r, int g, int b) {
        this.worldColor = new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    public List<EventManager> getEventListeners() {
        return eventListeners;
    }

    public JoglListener(FPSAnimator animator) {

        scene = new Scene();
        eventListeners = new ArrayList<>();
        this.animator = animator;
        worldColor = DEFAULT_WORLD_COLOR;
    }

    public void addEventListener(EventManager eventListener) {

        if (eventListener != null) {
            eventListeners.add(eventListener);
        }
    }

    public void removeEventListener(EventManager eventListener) {

        if (eventListener != null) {
            eventListeners.remove(eventListener);
        }
    }

    @Override
    public void init(GLAutoDrawable drawable) {

        GL gl_base = drawable.getGL();

        IntBuffer majorVersion = IntBuffer.allocate(1);
        gl_base.glGetIntegerv(GL3.GL_MAJOR_VERSION, majorVersion);

        int majVersion = majorVersion.get();
        if (majVersion < 3) {
            LOGGER.log(Level.SEVERE, "Opengl major version is {0}, this value should be higher than 3.\nTry to update the driver of the graphic card.", majVersion);

            drawable.destroy();
            return;
        }

        GL3 gl = gl_base.getGL3();

        //String extensions = gl.glGetString(GL3.GL_EXTENSIONS);
        try {
            scene.init(gl);
        } catch (Exception ex) {
            throw ex;
        }

        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);

        gl.glClearDepthf(1.0f);

        // Génération d'un second FBO
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenFramebuffers(1, tmp);
        fboBufferID = tmp.get(0);

        IntBuffer depthrenderbuffer = IntBuffer.allocate(1);
        gl.glGenRenderbuffers(1, depthrenderbuffer);

        depthrenderbufferID = depthrenderbuffer.get(0);
        drawBuffers = IntBuffer.wrap(new int[]{GL.GL_COLOR_ATTACHMENT0});

        //gl.glEnable(GL3.GL_LINE_SMOOTH);
        //gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        //gl.glPolygonMode(GL3.GL_FRONT, GL3.GL_LINE);
        //gl.glPolygonMode(GL3.GL_BACK, GL3.GL_LINE);
        //gl.glPolygonMode(GL3.GL_FRONT_AND_BACK, GL3.GL_LINE);
        //gl.glEnable(GL3.GL_CULL_FACE);
        //gl.glCullFace(GL3.GL_FRONT_AND_BACK);
        //gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        //gl.glHint(GL3.GL_GENERATE_MIPMAP_HINT, GL3.GL_NICEST);
        //gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        //gl.glHint(GL3.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL3.GL_NICEST);
        //drawable.setAutoSwapBufferMode(true);
//        
//        screenshotTexture = new Texture();
//        try {
//            screenshotTexture.setWidth(width);
//            screenshotTexture.init(gl);
//        } catch (Exception ex) {
//            LOGGER.log(Level.SEVERE,  "Cannot init screenshot texture.");
//        }
        // Génération d'une texture
        tmp = IntBuffer.allocate(1);
        gl.glGenTextures(1, tmp);
        screenshotTexture = tmp.get(0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    private void updateScreenshotsThings(GL3 gl) {

        // Binding de la texture pour pouvoir la modifier.
        gl.glBindTexture(GL.GL_TEXTURE_2D, screenshotTexture);

        // Création de la texture 2D vierge de la taille de votre fenêtre OpenGL
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);

        // Paramètrage de notre texture (étirement et filtrage)
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);

        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, depthrenderbufferID);
        gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL3.GL_DEPTH_COMPONENT, width, height);
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);

        // On bind le FBO
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, fboBufferID);

        // Affectation de notre texture au FBO
        gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, screenshotTexture, 0);

        gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, depthrenderbufferID);

        // Affectation d'un drawbuffer au FBO
        gl.glDrawBuffers(1, drawBuffers);

        int status = gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER);
        if (status != GL.GL_FRAMEBUFFER_COMPLETE) {
            LOGGER.log(Level.SEVERE, "Error occured while creating frame buffer object, status = {0}", status);
        }

        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();

        /*
        From doc:
        glViewport specifies the affine transformation of x and y from normalized 
        device coordinates to window coordinates. 
        Let x nd y nd be normalized device coordinates. 
        Then the window coordinates x w y w are computed as follows:

        x w = x nd + 1 ⁢* width * 2 + x
        y w = y nd + 1 ⁢* height * 2 + y
         */
        for (EventManager eventManager : eventListeners) {
            eventManager.updateEvents();
        }

        if (takeScreenShot) {

            if (screenshotThingsDirty) {
                updateScreenshotsThings(gl);
                screenshotThingsDirty = false;
            }

            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, fboBufferID);
        }

        gl.glViewport(startX, startY, viewportWidth, viewportHeight);

        //specify clear values for the color buffers (must be called before glClear)
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);

        gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

        gl.glDisable(GL.GL_BLEND);


        scene.draw(gl);

        if (justOnce && !dynamicDraw) { //draw one single frame
            animator.pause();
            justOnce = false;
        }

        if (takeScreenShot) {

            // Binding de la texture pour pouvoir la modifier.
            gl.glBindTexture(GL.GL_TEXTURE_2D, screenshotTexture);

            ByteBuffer imgBuffer = Buffers.newDirectByteBuffer(width * height * 4);

            //get pixels from the FBO
            gl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, imgBuffer);

            BufferedImage screenShotImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            int[] rgbArray = new int[width * height * 3];
            int count = 0;
            while (imgBuffer.hasRemaining()) {

                /*rgbArray[count] = imgBuffer.get()&0xff;
                rgbArray[count+1] = imgBuffer.get()&0xff;
                rgbArray[count+2] = imgBuffer.get()/*&0xff;
                count+=3;*/
                //2d indices
                int j = count / width;
                int i = count - (j * width);

                //inverted j
                int jj = height - j - 1;

                int newCount = (jj * width) + i;

                int red = imgBuffer.get() & 0xff;
                int green = imgBuffer.get() & 0xff;
                int blue = imgBuffer.get() & 0xff;
                int alpha = imgBuffer.get() & 0xff;

                rgbArray[newCount] = new Color(red, green, blue/*, alpha*/).getRGB();
                count++;
            }

            screenShotImg.setRGB(0, 0, width, height, rgbArray, 0, width);
            renderListener.screenshotIsReady(screenShotImg);

            takeScreenShot = false;
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
        }
    }

    /**
     * Draw the next frame
     */
    public void refresh() {

        justOnce = true;

        if (animator.isPaused()) {
            animator.resume();
        }

    }

    public void updateMousePicker(int mouseXLoc, int mouseYLoc) {

        scene.updateMousePicker(mouseXLoc, mouseYLoc, startX, startY, viewportWidth, viewportHeight);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        this.width = width;
        this.height = height;

        GL3 gl = drawable.getGL().getGL3();

        viewportWidth = this.width - startX;
        viewportHeight = this.height;

        gl.glViewport(startX, startY, viewportWidth, viewportHeight);

        /*if(isInit){
            scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);
        }*/
        updateCamera();

        screenshotThingsDirty = true;
    }

    public void updateCamera() {

        scene.getCamera().setViewportWidth(viewportWidth);
        scene.getCamera().setViewportHeight(viewportHeight);

        if (scene.getCamera().isPerspective()) {

            scene.getCamera().setPerspective(scene.getCamera().getFovy(), (1.0f * this.width - startX) / height, scene.getCamera().getNearPersp(), scene.getCamera().getFarPersp());
        } else {

            //scene.getCamera().initOrtho(-1, 1, 1, -1, scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
            scene.getCamera().initOrtho(-((viewportWidth) / 100), (viewportWidth) / 100, viewportHeight / 100, -(viewportHeight) / 100, scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
            scene.getCamera().setOrthographic(scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
        }

    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {

        this.startX = startX;

        viewportWidth = this.width - startX;
        viewportHeight = this.height;

        updateCamera();

        /*scene.getCamera().setViewportWidth(viewportWidth);
        scene.getCamera().setViewportHeight(viewportHeight);
        
        scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);*/
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public boolean isDynamicDraw() {
        return dynamicDraw;
    }

    public void setDynamicDraw(boolean dynamicDraw) {
        this.dynamicDraw = dynamicDraw;
    }

    public void setTakeScreenShot(boolean takeScreenShot, RenderListener listener) {
        this.takeScreenShot = takeScreenShot;
        this.renderListener = listener;
    }
}
