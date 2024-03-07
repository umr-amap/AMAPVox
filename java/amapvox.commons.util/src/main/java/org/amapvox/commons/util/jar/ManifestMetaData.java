/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.jar;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author pverley
 */
public class ManifestMetaData {

    public static String getValue(Class aClass, String attribute) throws Exception {

        String clz = aClass.getSimpleName() + ".class";
        String pth = aClass.getResource(clz).toString();
        String mnf = pth.substring(0, pth.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        URL url = new URL(mnf);
        Manifest manifest = new Manifest(url.openStream());
        Attributes attributes = manifest.getMainAttributes();
        return attributes.getValue(attribute);
    }
}
