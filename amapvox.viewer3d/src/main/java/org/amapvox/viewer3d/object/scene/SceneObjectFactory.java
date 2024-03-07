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
package org.amapvox.viewer3d.object.scene;

import com.jogamp.common.nio.Buffers;
import org.amapvox.commons.format.mesh3d.Obj;
import org.amapvox.commons.format.mesh3d.ObjHelper;
import org.amapvox.viewer3d.loading.shader.SimpleShader;
import org.amapvox.viewer3d.loading.texture.Texture;
import org.amapvox.viewer3d.mesh.GLMesh;
import org.amapvox.viewer3d.mesh.GLMeshFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class SceneObjectFactory {

    public static SceneObject createTexturedPlane(Vector3f startPoint, float width, float height, Texture texture) {

        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, width, height), true);

        sceneObject.attachTexture(texture);

        return sceneObject;
    }

    public static SceneObject createTexturedPlane(Vector3f startPoint, Texture texture, int shaderId) {

        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, texture.getWidth(), texture.getHeight()), true);
        sceneObject.attachTexture(texture);

        return sceneObject;
    }

    public static SceneObject createGizmo() {

        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createLandmark(-5, 5), false);
        sceneObject.setPosition(new Point3f());

        SimpleShader colorShader = new SimpleShader();
        colorShader.setColor(new Vector3f(0, 0, 1));

        sceneObject.setShader(colorShader);
        sceneObject.setDrawType(GLMesh.DrawType.LINES);
        return sceneObject;
    }

    public static SimpleSceneObject createFlag() throws IOException {

        InputStream flag = SceneObjectFactory.class.getResource("/org/amapvox/viewer3d/mesh/flag.obj").openStream();
        InputStreamReader isr = new InputStreamReader(flag);

        Obj obj = ObjHelper.readObj(isr);

        GLMesh mesh = GLMeshFactory.createMesh(obj.getPoints(), obj.getNormals(), obj.get1DFaces());

        int nbPoints = obj.getPoints().length;
        float colorData[] = new float[nbPoints * 3];

        for (int i = 0, j = 0; i < nbPoints; i++, j += 3) {

            colorData[j] = 0;
            colorData[j + 1] = 0;
            colorData[j + 2] = 0;
        }

        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);

        SimpleSceneObject sceneObjectFlag = new SimpleSceneObject(mesh, false);

        return sceneObjectFlag;
    }

}
