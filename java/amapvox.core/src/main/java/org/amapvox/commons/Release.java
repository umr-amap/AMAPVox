/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons;

import org.jdom2.Element;

/**
 *
 * @author pverley
 */
public abstract class Release {

    final private VersionNumber versionNumber;

    abstract public void update(final Element processElement);

    public Release(String versionNumber) {
        this.versionNumber = VersionNumber.valueOf(versionNumber);
    }

    public VersionNumber getVersionNumber() {
        return versionNumber;
    }
}
