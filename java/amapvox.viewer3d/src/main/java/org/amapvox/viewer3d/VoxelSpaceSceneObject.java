/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import org.amapvox.commons.math.geometry.AABB;
import org.amapvox.commons.math.geometry.BoundingBox3D;
import org.amapvox.commons.math.geometry.Plane;
import org.amapvox.commons.math.geometry.BoundingBox3F;
import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.util.filter.CombinedFilterItem;
import org.amapvox.commons.math.util.StandardDeviation;
import org.amapvox.voxelfile.VoxelFileVoxel;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import org.amapvox.viewer3d.mesh.GLMesh;
import org.amapvox.viewer3d.mesh.GLMeshFactory;
import org.amapvox.viewer3d.mesh.InstancedGLMesh;
import org.amapvox.viewer3d.object.scene.MousePicker;
import org.amapvox.viewer3d.object.scene.SceneObject;
import org.amapvox.commons.raytracing.geometry.LineSegment;
import org.amapvox.commons.raytracing.voxel.VoxelManager;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceSceneObject extends SceneObject {

    public static final int FLOAT_SIZE = Buffers.SIZEOF_FLOAT;
    public static final int INT_SIZE = Buffers.SIZEOF_INT;

    private Map<String, Integer> attributTable;

    private Point3f voxelSize;
    private float voxelScale = 0.5f;

    private final Vector3f width = new Vector3f();
    private boolean fileLoaded;
    private float valueMax;
    private float valueMin;
    private float clippedValueMax;
    private float clippedValueMin;
    private boolean clipped;
    private float stretchedValueMin;
    private float stretchedValueMax;

    private final File voxelFile;
    private VoxelFileHeader voxelFileHeader;
    private final HashMap<Integer, VoxelObject> voxels = new HashMap<>();

    private final Map<String, Attribute> attributeNames;
    private final Set<String> variables;
    private String attributeName;

    private Color[] gradient = ColorGradient.GRADIENT_HEAT;
    private ColorGradient colorGradient;

    private boolean gradientUpdated;
    private boolean cubeSizeUpdated = true;
    private boolean instancesUpdated;

    private int voxelNumberToDraw = 0;

    private boolean stretched;

    private List<CombinedFilterItem> filters;

    private final EventListenerList loadingListeners;
    private float sdValue;
    private float average;

    //handle cutting plane
    private boolean isCuttingInit;
    private Vector3f lastRightVector = new Vector3f();
    private Vector3f loc;
    private float cuttingIncrementFactor = 1.0f;

    private final PropertyChangeSupport props = new PropertyChangeSupport(this);
    private VoxelManager voxelManager;

    public VoxelSpaceSceneObject(File voxelFile) {

        this.voxelFile = voxelFile;

        filters = new ArrayList();
        attributeNames = new LinkedHashMap();
        variables = new TreeSet();
        loadingListeners = new EventListenerList();
        fileLoaded = false;
        voxels.clear();
    }

    private int ijkToIndex(Point3i ijk) {

        return (ijk.x * voxelFileHeader.getDimension().y * voxelFileHeader.getDimension().z)
                + (ijk.y * voxelFileHeader.getDimension().z)
                + ijk.z;
    }

    @Override
    public void updateBuffers(GL3 gl, int index, FloatBuffer buffer) {
        // do nothing
    }

    @Override
    public VoxelObject doPicking(MousePicker mousePicker) {

        Point3f closestPoint = mousePicker.getPointOnray(-100);
        Point3f farestPoint = mousePicker.getPointOnray(999);

        LineSegment lineSegment = new LineSegment(
                new Point3d(closestPoint.x, closestPoint.y, closestPoint.z),
                new Point3d(farestPoint.x, farestPoint.y, farestPoint.z));

        VoxelManager.VoxelCrossingContext context = voxelManager.getFirstVoxelV2(lineSegment);

        while ((context != null) && (context.indices != null)) {

            VoxelObject voxel = voxels.get(ijkToIndex(context.indices));

            if (voxel.getAlpha() > 0 && !voxel.isHidden()) {
                return voxel;
            } else {
                context = voxelManager.CrossVoxel(lineSegment, context.indices);
            }
        }

        return null;
    }

    @Override
    public BoundingBox3D getBoundingBox() {

        return new BoundingBox3D(
                new Point3d(voxelFileHeader.getMinCorner()),
                new Point3d(voxelFileHeader.getMaxCorner()));
    }

    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        props.addPropertyChangeListener(propName, l);
    }

    public float getVoxelScale() {
        return voxelScale;
    }

    private int product(Point3i p) {
        return p.x * p.y * p.z;
    }

    public void loadVoxels() throws IOException, Exception {

        loadFromFile(voxelFile);

        voxelSize = new Point3f(voxelFileHeader.getVoxelSize());
        voxelSize.scale(voxelScale);

        int instanceNumber = product(voxelFileHeader.getDimension());
        //mesh = new InstancedGLMesh(GLMeshFactory.createBoundingBox(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f), instanceNumber);
        mesh = new InstancedGLMesh(GLMeshFactory.createCuboid(voxelSize), instanceNumber);

        if (mousePickable) {
            voxelManager = new VoxelManager(
                    voxelFileHeader.getMinCorner(),
                    voxelFileHeader.getMaxCorner(),
                    voxelFileHeader.getDimension());
        }

        /*Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            iterator.next().getValue().buildHistogram();
        }*/
    }

    public void setReadFileProgress(int progress) {
        fireReadFileProgress(progress);
    }

    public void fireReadFileProgress(int progress) {

        for (VoxelSpaceListener listener : loadingListeners.getListeners(VoxelSpaceListener.class)) {

            listener.voxelSpaceCreationProgress(progress);
        }
    }

    public void setFileLoaded(boolean fileLoaded) {
        this.fileLoaded = fileLoaded;

        if (fileLoaded) {
            firefileLoaded();
        }
    }

    public void setStretched(boolean stretched) {
        this.stretched = stretched;
    }

    public boolean isFileLoaded() {
        return fileLoaded;
    }

    public Color[] getGradient() {
        return gradient;
    }

    public void firefileLoaded() {

        for (VoxelSpaceListener listener : loadingListeners.getListeners(VoxelSpaceListener.class)) {

            listener.voxelSpaceCreationFinished();
        }
    }

    public void addVoxelSpaceListener(VoxelSpaceListener listener) {
        loadingListeners.add(VoxelSpaceListener.class, listener);
    }

    public boolean isGradientUpdated() {
        return gradientUpdated;
    }

    public void setGradientUpdated(boolean value) {
        props.firePropertyChange("gradientUpdated", gradientUpdated, value);
        gradientUpdated = value;
    }

    public void setAttribute(String attributeName) {

        this.attributeName = attributeName;
        updateValue();
    }

    private void setWidth() {

        Point3i last = new Point3i();
        last.sub(voxelFileHeader.getDimension(), new Point3i(1, 1, 1));
        Point3f lastVoxelPosition = getVoxelPosition(last);
        Point3f firstVoxelPosition = getVoxelPosition(new Point3i());
        width.sub(lastVoxelPosition, firstVoxelPosition);
    }

    public Vector3f getWidth() {
        return width;
    }

    private void setCenter() {

        Point3i last = new Point3i();
        last.sub(voxelFileHeader.getDimension(), new Point3i(1, 1, 1));
        Point3f lastVoxelPosition = getVoxelPosition(last);
        Point3f firstVoxelPosition = getVoxelPosition(new Point3i());

        Point3f center = new Point3f();
        center.add(firstVoxelPosition, lastVoxelPosition);
        center.scale(0.5f);
        setGravityCenter(center);
    }

    private void initAttributes(String[] columnsNames) {

        attributTable = new HashMap<>();

        int id = 0;
        for (String name : columnsNames) {
            variables.add(name);
            attributTable.put(name, id);
            id++;
        }

        for (String name : columnsNames) {
            attributeNames.put(name, new Attribute(name, name, variables));
        }

    }

    private void readVoxelFormat(File f) throws IOException, Exception {

        try {

            VoxelFileReader reader = new VoxelFileReader(f);
            VoxelFileHeader header = reader.getHeader();

            //coordinates offset for float precision view
            Point3d newMaxCorner = new Point3d(header.getMaxCorner());
            newMaxCorner.sub(header.getMinCorner());
            voxelFileHeader = header.crop(new Point3d(), newMaxCorner, header.getDimension());
            initAttributes(header.getColumnNames());

            int count = header.getDimension().x * header.getDimension().y * header.getDimension().z;

            int lineNumber = 0;

            for (VoxelFileVoxel voxel : reader) {

                if (voxel != null) {

                    Point3i index = new Point3i(voxel.i, voxel.j, voxel.k);
                    voxels.put(ijkToIndex(index), new VoxelObject(index, voxel.variables, 1.0f));
                    lineNumber++;
                    setReadFileProgress((lineNumber * 100) / count);
                }
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    public Point3f getVoxelPosition(Point3i index) {

        double posX = voxelFileHeader.getMinCorner().x + (voxelFileHeader.getVoxelSize().x / 2.0d) + (index.x * voxelFileHeader.getVoxelSize().x);
        double posY = voxelFileHeader.getMinCorner().y + (voxelFileHeader.getVoxelSize().y / 2.0d) + (index.y * voxelFileHeader.getVoxelSize().y);
        double posZ = voxelFileHeader.getMinCorner().z + (voxelFileHeader.getVoxelSize().z / 2.0d) + (index.z * voxelFileHeader.getVoxelSize().z);

        return new Point3f((float) posX, (float) posY, (float) posZ);
    }

    public final void loadFromFile(File f) throws IOException, Exception {

        setFileLoaded(false);
        
        readVoxelFormat(f);
        setCenter();
        setWidth();
        
        setFileLoaded(true);
    }

    public void setRange(float minClipped, float maxClipped) {

        clipped = true;
        clippedValueMin = minClipped;
        clippedValueMax = maxClipped;
    }

    public void resetRange() {
        clipped = false;
    }

    public void updateValue() {

        if (attributeName == null) {
            attributeName = attributeNames.entrySet().iterator().next().getKey();
        }

        Attribute attribute = attributeNames.get(attributeName);

        boolean minMaxInit = false;

        StandardDeviation sd = new StandardDeviation();

        for (VoxelObject voxel : voxels.values()) {

            float attributeValue;

            for (int i = 0; i < voxelFileHeader.getColumnNames().length; i++) {
                String name = voxelFileHeader.getColumnNames()[i];
                double value = voxel.getAttribute(i);
                attribute.getExpression().setVariable(name, value);
            }

            try {
                attributeValue = (float) attribute.getExpression().evaluate();
            } catch (Exception e) {
                attributeValue = 0;
            }

            voxel.setValue(attributeValue);
            //voxel.color = getColorFromValue(attributValue);

            if (!Float.isNaN(attributeValue)) {
                if (!minMaxInit) {
                    valueMax = attributeValue;
                    valueMin = attributeValue;
                    minMaxInit = true;
                } else {
                    //set maximum attribut value
                    if (attributeValue > valueMax) {
                        valueMax = attributeValue;
                    }
                    //set minimum attribut value
                    if (attributeValue < valueMin) {
                        valueMin = attributeValue;
                    }
                }
            }

            voxel.setAlpha(255);

            //values[count] = voxel.attributValue;
            if (stretched) {
                if (clipped) {
                    if (voxel.getValue() < clippedValueMin) {
                        sd.addValue(clippedValueMin);
                    } else if (voxel.getValue() > clippedValueMax) {
                        sd.addValue(clippedValueMax);
                    } else {
                        sd.addValue(voxel.getValue());
                    }
                } else {
                    sd.addValue(voxel.getValue());
                }
            }
        }

        //calculate standard deviation
        if (stretched) {
            sdValue = sd.getStandardDeviation();
            average = sd.getAverage();

            stretchedValueMin = average - (2 * sdValue);
            stretchedValueMax = average + (2 * sdValue);

            if (clipped) {
                if (stretchedValueMin < clippedValueMin) {
                    stretchedValueMin = clippedValueMin;
                }

                if (stretchedValueMax > clippedValueMax) {
                    stretchedValueMax = clippedValueMax;
                }
            } else {
                if (stretchedValueMin < valueMin) {
                    stretchedValueMin = valueMin;
                }

                if (stretchedValueMax > valueMax) {
                    stretchedValueMax = valueMax;
                }
            }

            setGradientColor(gradient, stretchedValueMin, stretchedValueMax);

        } else {
            if (clipped) {
                setGradientColor(gradient, clippedValueMin, clippedValueMax);
            } else {
                setGradientColor(gradient, valueMin, valueMax);
            }
        }

    }

    public boolean isStretched() {
        return stretched;
    }

    public void updateColorValue(Color[] gradient) {
        if (stretched) {
            setGradientColor(gradient, stretchedValueMin, stretchedValueMax);
        } else {
            if (clipped) {
                setGradientColor(gradient, clippedValueMin, clippedValueMax);
            } else {
                setGradientColor(gradient, valueMin, valueMax);
            }

        }

    }

    public Vector3f getColorFromValue(float value) {

        Color c = colorGradient.getColor(value);

        return new Vector3f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
    }

    private float getAttribute(String attributName, VoxelObject voxel) {

        return voxel.getAttribute(attributTable.get(attributName));
    }

    private boolean doFiltering(VoxelObject voxel) {

        boolean filtered = false;

        if (filters != null) {

            for (CombinedFilterItem filter : filters) {

                //true if the field value fit the condition, false otherwise
                boolean doFilter = filter.accept(getAttribute(filter.getScalarField(), voxel));

                if (filter.isDisplay()) {

                    if (doFilter) {
                        filtered = false;
                    } else {
                        return true;
                    }
                } else {
                    if (doFilter) {
                        return true;
                    } else {
                        filtered = false;
                    }
                }
            }
        }

        return filtered;
    }

    public void setGradientColor(Color[] gradientColor, float valMin, float valMax) {

        this.gradient = gradientColor;

        ColorGradient color = new ColorGradient(valMin, valMax);
        color.setGradientColor(gradientColor);
        //ArrayList<Float> values = new ArrayList<>();
        voxelNumberToDraw = 0;

        for (VoxelObject voxel : voxels.values()) {

            //float ratio = voxel.attributValue/(attributValueMax-attributValueMin);
            //float value = valMin+ratio*(valMax-valMin);
            //Color colorGenerated = color.getColor(value);
            Color colorGenerated = color.getColor(voxel.getValue());

            voxel.setColor(colorGenerated.getRed(), colorGenerated.getGreen(), colorGenerated.getBlue());
            //values.add(voxel.attributValue);

            //boolean isFiltered = combinedFilters.doFilter(voxel.attributValue);
            boolean isFiltered = doFiltering(voxel);

            if (isFiltered /*&& displayValues*/) {
                voxel.setAlpha(0);
                //voxelNumberToDraw++;
                /*}else if(isFiltered && !displayValues){
                voxel.setAlpha(0);
                 */
            } else if (!isFiltered/* && displayValues*/) {
                voxel.setAlpha(1);
                voxelNumberToDraw++;
            } else {
                voxel.setAlpha(1);
                voxelNumberToDraw++;
            }
        }
        //System.out.println("test");
        //voxelList = ImageEqualisation.scaleHistogramm(voxelList);
        //voxelList = ImageEqualisation.voxelSpaceFormatEqualisation(voxelList);
    }

    public void updateInstanceColorBuffer() {

        setGradientUpdated(false);
    }

    public void scaleVoxel(float scale) {

        voxelScale = scale;
        voxelSize = new Point3f(voxelFileHeader.getVoxelSize());
        voxelSize.scale(scale);
        cubeSizeUpdated = false;
    }

    public void setCuttingPlane(Plane plane) {

        Vector3f normale = plane.getNormale();
        Vector3f point = new Vector3f(plane.getPoint().x, plane.getPoint().y, plane.getPoint().z);

        for (VoxelObject voxel : voxels.values()) {

            Point3f pt = getVoxelPosition(voxel.getIndex());
            Vector3f position = new Vector3f(pt.x, pt.y, pt.z);

            Vector3f tmp = new Vector3f();
            tmp.sub(position, point);
            float side = tmp.dot(normale);

            voxel.setHidden(side > 0);
        }

    }

    public void clearCuttingPlane() {

        voxels.values().forEach((voxel) -> {
            voxel.setHidden(false);
        });
    }

    public void resetCuttingPlane() {

        clearCuttingPlane();
        updateVao();

        isCuttingInit = false;
        lastRightVector = new Vector3f();
    }

    public void setCuttingIncrementFactor(float cuttingIncrementFactor) {
        this.cuttingIncrementFactor = cuttingIncrementFactor;
    }

    public void setCuttingPlane(boolean increase, Vector3f forwardVector, Vector3f rightVector, Vector3f upVector, Vector3f cameraLocation) {

        rightVector.normalize();
        upVector.normalize();

        if (lastRightVector.x != rightVector.x || lastRightVector.y != rightVector.y || lastRightVector.z != rightVector.z) {
            isCuttingInit = false;
        }

        lastRightVector = rightVector;

        //init
        if (!isCuttingInit) {

            loc = cameraLocation;
            Point3d bottomCorner = voxelFileHeader.getMinCorner();
            Point3d topCorner = voxelFileHeader.getMaxCorner();
            AABB aabb = new AABB(new BoundingBox3F(new Point3f((float) bottomCorner.x, (float) bottomCorner.y, (float) bottomCorner.z),
                    new Point3f((float) topCorner.x, (float) topCorner.y, (float) topCorner.z)));

            Point3f nearestPoint = aabb.getNearestPoint(new Point3f(loc.x, loc.y, loc.z));
            loc = new Vector3f(nearestPoint.x, nearestPoint.y, nearestPoint.z);
            isCuttingInit = true;

        } else {
            Vector3f forward = forwardVector;
            Vector3f direction = new Vector3f(forward);
            direction.normalize();
            direction.scale(cuttingIncrementFactor);
            if (increase) {
                loc.add(direction);
            } else {
                loc.sub(direction);
            }

        }

        Plane plane = new Plane(rightVector, upVector, new Point3f(loc.x, loc.y, loc.z));
        //System.out.println(loc.x+" "+loc.y+" "+loc.z);

        setCuttingPlane(plane);
        updateVao();
    }

    @Override
    public void initVao(GL3 gl) {

        //generate vao
        int[] tmp2 = new int[1];
        gl.glGenVertexArrays(1, tmp2, 0);
        vaoId = tmp2[0];

        gl.glBindVertexArray(vaoId);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());

        gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
        gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);

        gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_position"));
        gl.glVertexAttribPointer(shader.attributeMap.get("instance_position"), 3, GL3.GL_FLOAT, false, 0, mesh.getVertexBuffer().capacity() * FLOAT_SIZE);
        gl.glVertexAttribDivisor(shader.attributeMap.get("instance_position"), 1);

        gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_color"));
        gl.glVertexAttribPointer(shader.attributeMap.get("instance_color"), 4, GL3.GL_FLOAT, false, 0, (mesh.getVertexBuffer().capacity() + ((InstancedGLMesh) mesh).instancePositionsBuffer.capacity()) * FLOAT_SIZE);
        gl.glVertexAttribDivisor(shader.attributeMap.get("instance_color"), 1);

        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);

        gl.glBindVertexArray(0);

        setGradientUpdated(true);
    }

    public void updateVao() {

        //List<Float> instancePositionsList = new ArrayList<>();
        //List<Float> instanceColorsList = new ArrayList<>();
        float[] instancePositions = new float[voxelNumberToDraw * 3];
        float[] instanceColors = new float[voxelNumberToDraw * 4];

        int positionCount = 0;
        int colorCount = 0;

        for (VoxelObject voxel : voxels.values()) {

            if (voxel.getAlpha() != 0 && !voxel.isHidden()) {

                if (positionCount < instancePositions.length && colorCount < instanceColors.length) {

                    Point3f position = getVoxelPosition(voxel.getIndex());

                    instancePositions[positionCount] = position.x;
                    instancePositions[positionCount + 1] = position.y;
                    instancePositions[positionCount + 2] = position.z;

                    instanceColors[colorCount] = voxel.getRed();
                    instanceColors[colorCount + 1] = voxel.getGreen();
                    instanceColors[colorCount + 2] = voxel.getBlue();
                    instanceColors[colorCount + 3] = voxel.getAlpha();

                    positionCount += 3;
                    colorCount += 4;
                }

                /*
                instancePositionsList.add(voxel.gravityCenter.x);
                instancePositionsList.add(voxel.gravityCenter.y);
                instancePositionsList.add(voxel.gravityCenter.z);
                
                instanceColorsList.add(voxel.getRed());
                instanceColorsList.add(voxel.getGreen());
                instanceColorsList.add(voxel.getBlue());
                instanceColorsList.add(voxel.getAlpha());
                 */
            }
        }

        /*
        for(int i=0;i<instancePositionsList.size();i++){
            instancePositions[i] = instancePositionsList.get(i);
        }
        
        for(int i=0;i<instanceColorsList.size();i++){
            instanceColors[i] = instanceColorsList.get(i);
        }*/
        ((InstancedGLMesh) mesh).instancePositionsBuffer = Buffers.newDirectFloatBuffer(instancePositions);
        ((InstancedGLMesh) mesh).instanceColorsBuffer = Buffers.newDirectFloatBuffer(instanceColors);

        ((InstancedGLMesh) mesh).setInstanceNumber(instancePositions.length / 3);

        instancesUpdated = false;
    }

    @Override
    public void initBuffers(GL3 gl) {

        //mesh = new SimpleGLMesh(gl);
        int maxSize = (mesh.getVertexBuffer().capacity() * GLMesh.FLOAT_SIZE) + (voxels.size() * 3 * GLMesh.FLOAT_SIZE) + (voxels.size() * 4 * GLMesh.FLOAT_SIZE);
        mesh.initBuffers(gl, maxSize);

        updateVao();
    }

    @Override
    public void draw(GL3 gl) {

        if (!instancesUpdated) {

            //update buffers
            mesh.updateBuffer(gl, 1, ((InstancedGLMesh) mesh).instancePositionsBuffer);
            mesh.updateBuffer(gl, 2, ((InstancedGLMesh) mesh).instanceColorsBuffer);

            //update offsets
            gl.glBindVertexArray(vaoId);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());

            /*gl.glEnableVertexAttribArray(shader.attributeMap.get("gravityCenter"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("gravityCenter"), 3, GL3.GL_FLOAT, false, 0, 0);*/
            gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_position"));
            gl.glVertexAttribPointer(shader.attributeMap.get("instance_position"), 3, GL3.GL_FLOAT, false, 0, mesh.getVertexBuffer().capacity() * FLOAT_SIZE);
            gl.glVertexAttribDivisor(shader.attributeMap.get("instance_position"), 1);

            gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_color"));
            gl.glVertexAttribPointer(shader.attributeMap.get("instance_color"), 4, GL3.GL_FLOAT, false, 0, (mesh.getVertexBuffer().capacity() + ((InstancedGLMesh) mesh).instancePositionsBuffer.capacity()) * FLOAT_SIZE);
            gl.glVertexAttribDivisor(shader.attributeMap.get("instance_color"), 1);

            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);

            gl.glBindVertexArray(0);

            instancesUpdated = true;
        }

        if (!gradientUpdated) {

            float instanceColors[] = new float[voxels.size() * 4];

            int count0 = 0;

            for (VoxelObject voxel : voxels.values()) {

                if (voxel.getAlpha() != 0 && !voxel.isHidden()) {

                    instanceColors[count0] = voxel.getRed();
                    instanceColors[count0 + 1] = voxel.getGreen();
                    instanceColors[count0 + 2] = voxel.getBlue();
                    instanceColors[count0 + 3] = voxel.getAlpha();
                    count0 += 4;
                }
            }

            ((InstancedGLMesh) mesh).instanceColorsBuffer = Buffers.newDirectFloatBuffer(instanceColors, 0, count0);

            mesh.updateBuffer(gl, 2, ((InstancedGLMesh) mesh).instanceColorsBuffer);

            setGradientUpdated(true);
        }

        if (!cubeSizeUpdated) {
            GLMesh cuboid = GLMeshFactory.createCuboid(voxelSize);
            mesh.updateBuffer(gl, 0, cuboid.getVertexBuffer());
            cubeSizeUpdated = true;
        }

        gl.glBindVertexArray(vaoId);
        if (texture != null) {
            gl.glBindTexture(GL3.GL_TEXTURE_2D, textureId);
        }
        mesh.draw(gl, drawType);

        if (texture != null) {
            gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
        }
        gl.glBindVertexArray(0);
    }

    public Set<String> getVariables() {
        return variables;
    }

    public float getRealAttributValueMax() {
        return valueMax;
    }

    public float getRealAttributValueMin() {
        return valueMin;
    }

    public float getAttributValueMax() {

        if (clipped) {
            return clippedValueMax;
        } else {
            return valueMax;
        }

    }

    public float getAttributValueMin() {
        if (clipped) {
            return clippedValueMin;
        } else {
            return valueMin;
        }
    }

    public File getVoxelFile() {
        return voxelFile;
    }

    public VoxelFileHeader getVoxelFileHeader() {
        return voxelFileHeader;
    }

    @Override
    public String toString() {
        return voxelFile.getAbsolutePath();
    }

    public void setFilters(List<CombinedFilterItem> filters) {
        this.filters = filters;
    }
}
