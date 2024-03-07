/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.format.mesh3d;

import javax.vecmath.Vector3f;

/**
 *
 * @author calcul
 */
public class Mtl {
    
    private final String name;
    
    private final Vector3f diffuseColor;
    private final Vector3f ambientColor;
    private final Vector3f specularColor;

    public Mtl(String name, Vector3f diffuseColor, Vector3f ambientColor, Vector3f specularColor) {
        this.name = name;
        this.diffuseColor = diffuseColor;
        this.ambientColor = ambientColor;
        this.specularColor = specularColor;
    }

    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public Vector3f getSpecularColor() {
        return specularColor;
    }
}
