package org.amapvox.deprecated;

import java.io.IOException;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Configuration;
import org.amapvox.commons.Release;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author pverley
 */
@Deprecated
public class MergingCfg extends Configuration {
    
    public MergingCfg() {
        super("MERGING", "Voxel files merging",
                "Merging function has been moved to R package AMAPVox::merge",
                true);
    }

    @Override
    public void readProcessElements(Element processElement) throws JDOMException, IOException {
    }

    @Override
    public void writeProcessElements(Element processElement) throws JDOMException, IOException {
    }

    @Override
    public Release[] getReleases() {
        return null;
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return null;
    }

}
