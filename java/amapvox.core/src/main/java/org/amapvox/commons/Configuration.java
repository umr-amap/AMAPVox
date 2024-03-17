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
package org.amapvox.commons;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author calcul
 */
public abstract class Configuration {

    private final static Logger LOGGER = Logger.getLogger(Configuration.class);

    private File file;

    private String type;
    private String longName;
    private String description;
    private String[] deprecatedNames;
    final private boolean deprecated;

    protected Element racine;
    protected Document document;
    protected Element processElement;
    private boolean initialized = false;
    protected String className;

    abstract public void readProcessElements(final Element processElement) throws JDOMException, IOException;

    abstract public void writeProcessElements(final Element processElement) throws JDOMException, IOException;

    abstract public Release[] getReleases();

    /**
     * Return the associated task class.
     *
     * @return the task class.
     */
    abstract public Class<? extends AVoxTask> getTaskClass();

    public Configuration(
            String type,
            String longName,
            String description,
            String[] deprecatedNames,
            boolean deprecated) {
        this.type = type;
        this.longName = longName;
        this.description = description;
        this.deprecatedNames = deprecatedNames;
        this.deprecated = deprecated;
    }

    public Configuration(String type, String longName, String description, String[] deprecatedNames) {
        this(type, longName, description, deprecatedNames, false);
    }

    public Configuration(String type, String longName, String description) {
        this(type, longName, description, new String[0], false);
    }

    public Configuration(String type, String longName, String description, boolean deprecated) {
        this(type, longName, description, new String[0], deprecated);
    }

    /**
     * Instantiates class extending Configuration class with given class name.
     *
     * @param className, class name of a class extending Configuration class.
     * @return a Configuration instance.
     * @throws Exception
     */
    public static Configuration newInstance(String className) throws Exception {
        Class<? extends Configuration> clazz = (Class<? extends Configuration>) Class.forName(className);
        return clazz.getConstructor().newInstance();
    }

    /**
     * Instantiates class extending Configuration class by reading the class
     * name form XML configuration file.
     *
     * Gets class name from 'classname' attribute in 'process' element.
     *
     * @param file, AMAPVox XML configuration file
     * @return a Configuration instance.
     * @throws Exception
     */
    public static Configuration newInstance(File file) throws Exception {

        // creates and initializes new default configuration
        Configuration cfg = new Configuration.Default();
        cfg.initDocument(file);
        // creates and initializes specific configuration
        cfg = newInstance(cfg.className);
        cfg.initDocument(file);
        return cfg;
    }

    /**
     * Checks whether 'process' element exists and contains children.
     *
     * @return 'true' if process element does not exist of is empty
     */
    public boolean isEmpty() {
        return (null == processElement) || processElement.getChildren().isEmpty();
    }

    private synchronized void init(File file) throws JDOMException, IOException {

        if (!initialized) {
            // read invariable content
            initDocument(file);
            // read version
            VersionNumber cfgVersion = getConfigurationVersion();
            VersionNumber jarVersion = getJarVersion();
            // check whether it needs to update
            if (null != jarVersion && cfgVersion.compareTo(jarVersion) < 0) {
                LOGGER.info("Configuration " + file.getName() + " is outdated (version " + cfgVersion.toString() + "). It will be automatically updated to version " + jarVersion.toString());
                // backup configuration file
                File backup = getBackupFile(file.getAbsolutePath(), cfgVersion);
                copyFile(file, backup);
                LOGGER.info("Outdated configuration " + file.getName() + " saved as " + backup.getAbsolutePath());
                // update configuration file
                updateConfiguration(jarVersion, cfgVersion);
                // write updated configuration file
                writeDocument(file);
            } else {
                LOGGER.debug("Configuration " + file.getName() + " is up to date (version " + cfgVersion.toString() + ")");
            }

            // initialization succeeded
            initialized = true;
        }
    }

    /**
     *
     * @param file
     * @throws JDOMException
     * @throws IOException
     */
    public void read(File file) throws JDOMException, IOException {

        this.file = file;
        init(file);

        // read updated configuration file
        readProcessElements(processElement);
    }

    public void write(File file, boolean headerOnly) throws IOException, JDOMException {

        this.file = file;
        createCommonData(getJarVersion().toString());
        processElement.setAttribute(new Attribute("mode", getType()));
        processElement.setAttribute(new Attribute("classname", getClass().getCanonicalName()));
        if (!headerOnly) {
            writeProcessElements(processElement);
        }
        writeDocument(file);
    }

    public void write(File file) throws IOException, JDOMException {
        write(file, false);
    }

    /*
     * Upgrade the configuration file to the application version.
     */
    private void updateConfiguration(VersionNumber jarVersion, VersionNumber cfgVersion) {

        if (null != getReleases()) {
            
            // sort available releases by version number
            Arrays.sort(getReleases(), (r1, r2) -> {
                return r1.getVersionNumber().compareTo(r2.getVersionNumber());
            });

            // Update the configuration file
            for (Release release : getReleases()) {
                VersionNumber version = release.getVersionNumber();
                if ((version.compareTo(jarVersion) <= 0) && (cfgVersion.compareTo(version) < 0)) {
                    LOGGER.info("Applying update " + version.toString());
                    try {
                        release.update(processElement);
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to apply update " + version.toString() + ". Moving on to next update", ex);
                    }
                }
            }
        }

        // update cfg number to jar version in case last release is anterior to jar version
        racine.setAttribute("build-version", jarVersion.toString());
        racine.setAttribute("update-date", new Date().toString());
        LOGGER.debug("Updated configuration file to version " + jarVersion.toString());
    }

    private File getBackupFile(String src, VersionNumber srcVersion) {

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        formatter.setCalendar(calendar);
        StringBuilder bak = new StringBuilder(src);
        bak.append(".backup-");
        bak.append(srcVersion);
        bak.append('-');
        bak.append(formatter.format(calendar.getTime()));
        return new File(bak.toString());
    }

    private void copyFile(File src, File dest) throws IOException {

        FileOutputStream fos;
        try (FileInputStream fis = new FileInputStream(src)) {
            fos = new FileOutputStream(dest);
            java.nio.channels.FileChannel channelSrc = fis.getChannel();
            java.nio.channels.FileChannel channelDest = fos.getChannel();
            channelSrc.transferTo(0, channelSrc.size(), channelDest);
        }
        fos.close();
    }

    private VersionNumber getConfigurationVersion() {
        try {
            return VersionNumber.valueOf(document.getRootElement().getAttributeValue("build-version"));
        } catch (VersionNumberFormatException ex) {
            LOGGER.warn("Could not identify version of the configuration (" + ex.toString() + "). AMAPVox assumes version 1.0.0");
        }
        return VersionNumber.valueOf("1.0.0");
    }

    private VersionNumber getJarVersion() {
        try {
            return VersionNumber.valueOf(Util.getVersion());
        } catch (Exception ex) {
            // 
            LOGGER.error("Could not identify AMAPVox version.", ex);
        }
        return null;
    }

    private void createCommonData(String buildVersion) {

        racine = new Element("configuration");
        racine.setAttribute("creation-date", new Date().toString());
        racine.setAttribute("build-version", buildVersion);
        document = new Document(racine);
        processElement = new Element("process");
        racine.addContent(processElement);
    }

    private void initDocument(File inputFile) throws JDOMException, IOException {

        SAXBuilder sxb = new SAXBuilder();
        document = sxb.build(inputFile);
        racine = document.getRootElement();
        processElement = racine.getChild("process");
        if (null == processElement) {
            throw new IOException(inputFile.getName() + " is not a valid AMAPVox configuration file");
        }
        className = readClassName();
    }

    private String readClassName() throws IOException {

        if (null != processElement.getAttribute("classname")) {
            return processElement.getAttributeValue("classname");
        } else {
            // prior to version 2, classname attribute does not exist
            // must infer it from mode attribute
            String processMode = processElement.getAttributeValue("mode").toUpperCase();
            return switch (processMode) {
                case "VOXELIZATION", "VOXELISATION" ->
                    "org.amapvox.voxelisation.VoxelizationCfg";
                case "HEMI_PHOTO" ->
                    "org.amapvox.canopy.hemi.HemiPhotoCfg";
                case "TRANSMITTANCE" ->
                    "org.amapvox.canopy.transmittance.TransmittanceCfg";
                case "CANOPY_ANALYZER" ->
                    "org.amapvox.canopy.lai2xxx.CanopyAnalyzerCfg";
                case "OBJ_EXPORT" ->
                    "org.amapvox.voxelisation.postproc.ObjExporterCfg";
                case "MERGING" ->
                    "org.amapvox.deprecated.MergingCfg";
                case "CROPPING" ->
                    "org.amapvox.deprecated.CroppingCfg";
                case "BUTTERFLY_REMOVING" ->
                    "org.amapvox.deprecated.ButterflyCfg";
                default ->
                    throw new IOException("Unsupported configuration type (" + processMode + ")");
            };
        }
    }

    private void writeDocument(File outputFile) throws IOException {

        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        try {
            output.output(document, new BufferedOutputStream(new FileOutputStream(outputFile)));
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     * Relativizes a file path against the the provided path. If filename is a
     * directory the function ensures the path ends with a separator.
     *
     * @param filename, the file path to relativize
     * @param relativeTo, the path against the file must be relativized
     * @return the relativized file path
     */
    protected String relativize(String filename, String relativeTo) {

        try {
            Path against = Paths.get(relativeTo);
            if (Files.isRegularFile(against)) {
                against = against.getParent();
            }
            return against.relativize(Paths.get(filename)).toString();
        } catch (Exception ex) {
            // do nothing, simply return filename
        }
        return filename;
    }

    /**
     * Resolves a file path against the the provided path. If filename is a
     * directory the function ensures the path ends with a separator.
     *
     * @param filename, the file path to resolve
     * @param relativeTo, the path against the file must be resolved
     * @return the resolved file path
     */
    protected String resolve(String filename, String relativeTo) {

        try {
            Path against = Paths.get(relativeTo);
            if (Files.isRegularFile(against)) {
                against = against.getParent();
            }
            return against.resolve(Paths.get(filename))
                    .toFile().getCanonicalPath();
        } catch (Exception ex) {
            // do nothing, simply return filename
        }
        return filename;
    }

    /**
     * Resolve a file path against the current configuration path.
     *
     * @param filename, the file path to resolve
     * @return the resolved file path
     */
    protected String resolve(String filename) {
        return resolve(filename, file.getAbsolutePath());
    }

    /**
     * Relativizes a file path against the current configuration path.
     *
     * @param filename, the file path to relativize
     * @return the relativized file path
     */
    protected String relativize(String filename) {
        return relativize(filename, file.getAbsolutePath());
    }

    protected Element createFilesElement(List<File> files) {

        Element filesElement = new Element("files");
        files.forEach(f -> {
            filesElement.addContent(new Element("file").setAttribute("src", f.getAbsolutePath()));
        });
        return filesElement;
    }

    protected Element createLimitElement(String name, String min, String max) {

        Element limitElement = new Element("limit");
        limitElement.setAttribute("name", name);
        limitElement.setAttribute("min", min);
        limitElement.setAttribute("max", max);

        return limitElement;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the longName
     */
    public String getLongName() {
        return longName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return the deprecatedNames
     */
    public String[] getDeprecatedNames() {
        return deprecatedNames;
    }

    private static class Default extends Configuration {

        public Default() {
            super("DEFAULT", "Default configuration", "Empty default configuration");
        }

        @Override
        public void readProcessElements(Element processElement) throws JDOMException, IOException {
            // nothing to read
        }

        @Override
        public void writeProcessElements(Element processElement) throws JDOMException, IOException {
            // nothing to write
        }

        @Override
        public Class<? extends AVoxTask> getTaskClass() {
            return null;
        }

        @Override
        public Release[] getReleases() {
            return null;
        }
    }

    /**
     * @return the deprecated
     */
    public boolean isDeprecated() {
        return deprecated;
    }
}
