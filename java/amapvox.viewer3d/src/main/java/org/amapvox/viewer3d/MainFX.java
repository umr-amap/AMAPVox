package org.amapvox.viewer3d;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.amapvox.commons.Matrix;
import org.amapvox.commons.Util;
import org.amapvox.commons.math.geometry.BoundingBox2F;
import org.amapvox.commons.raster.asc.AsciiGridHelper;
import org.amapvox.commons.raster.asc.Face;
import org.amapvox.commons.raster.asc.Point;
import org.amapvox.commons.raster.asc.Raster;
import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.util.image.ScaleGradient;
import org.amapvox.viewer3d.loading.shader.InstanceLightedShader;
import org.amapvox.viewer3d.loading.shader.PhongShader;
import org.amapvox.viewer3d.loading.shader.SimpleShader;
import org.amapvox.viewer3d.loading.shader.TextureShader;
import org.amapvox.viewer3d.loading.texture.Texture;
import org.amapvox.viewer3d.mesh.GLMesh;
import org.amapvox.viewer3d.mesh.GLMeshFactory;
import org.amapvox.viewer3d.mesh.SimpleGLMesh;
import org.amapvox.viewer3d.object.camera.TrackballCamera;
import org.amapvox.viewer3d.object.scene.MousePicker;
import org.amapvox.viewer3d.object.scene.SceneObject;
import org.amapvox.viewer3d.object.scene.SceneObjectFactory;
import org.amapvox.viewer3d.object.scene.SceneObjectListener;
import org.amapvox.viewer3d.object.scene.SimpleSceneObject;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import org.amapvox.voxelisation.output.OutputVariable;
import org.controlsfx.dialog.ProgressDialog;

public class MainFX extends Application {

    private double screenWidth;
    private double screenHeight;

    // voxel file
    private File voxFile;
    private String voxVariable;

    // dtm
    private File dtmFile;
    private Matrix4d dtmVop;
    private int dtmMargin = 0;

    private final static Logger LOGGER = Logger.getLogger(MainFX.class.getCanonicalName());

    @Override
    public void start(Stage stage) throws Exception {

        // look for --help/-h option
        if (getParameters().getUnnamed().contains("--help")) {
            showHelp();
            System.exit(0);
        }

        if (getParameters().getUnnamed().size() != 1) {
            LOGGER.log(Level.SEVERE, "AMAPVox viewer3d takes one and only one voxel file parameter.");
            showHelp();
            System.exit(1);
        }

        // add voxel file as a named parameter
        Map<String, String> parameters = new HashMap(getParameters().getNamed());
        parameters.put("vox-file", getParameters().getUnnamed().get(0));

        initializeAndShowStage(parameters, stage);
    }

    public void initializeAndShowStage(Map<String, String> parameters, Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("fxml/Viewer3DFrame.fxml"));
        Parent root = loader.load();
        Viewer3DFrameController controller = loader.getController();
        ObservableList<Screen> screens = Screen.getScreensForRectangle(0, 0, 10, 10);

        if (screens != null && !screens.isEmpty()) {
            stage.setWidth(screens.get(0).getBounds().getWidth());
            stage.setHeight(screens.get(0).getBounds().getHeight());
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(MainFX.class.getResource("styles/Styles.css").toExternalForm());

        stage.setTitle("AMAPVox viewer3D " + Util.getVersion());
        stage.setScene(scene);
        stage.getIcons().addAll(new Image(MainFX.class.getResource("icons/amapvox-icon_256x256.png").toExternalForm()),
                new Image(MainFX.class.getResource("icons/amapvox-icon_128x128.png").toExternalForm()),
                new Image(MainFX.class.getResource("icons/amapvox-icon_64x64.png").toExternalForm()));

        controller.setStage(stage);

        stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, (WindowEvent event) -> {
            System.exit(0);
        });

        parseParameters(parameters);

        showViewer3D(stage);

    }

    private void parseParameters(Map<String, String> parameters) throws IOException {

        // voxel file
        if (!parameters.keySet().stream().anyMatch(p -> p.equalsIgnoreCase("vox-file"))) {
            throw new IOException("Voxel file parameter missing");
        }
        voxFile = new File(parameters.get("vox-file"));
        if (!voxFile.exists()) {
            throw new IOException("Voxel file does not exist " + voxFile.getAbsolutePath());
        }

        VoxelFileReader vfr = new VoxelFileReader(voxFile);

        // voxel file variable
        if (!parameters.keySet().stream().anyMatch(p -> p.equalsIgnoreCase("vox-var"))) {
            try {
                voxVariable = Arrays.asList(vfr.getHeader().getColumnNames()).stream()
                        .filter(name -> !OutputVariable.find(name).isCoordinateVariable())
                        .findFirst().get();
            } catch (NullPointerException | NoSuchElementException ex) {
                voxVariable = vfr.getHeader().getColumnNames()[0];
            }
//            LOGGER.log(Level.INFO, "Displaying variable {0}", voxVariable);
        } else {
            voxVariable = parameters.get("vox-var");
            try {
                OutputVariable.find(voxVariable);
            } catch (NullPointerException ex) {
                throw new IOException(ex);
            }
        }

        // dtm parameters
        if (parameters.keySet().stream().anyMatch(p -> p.equalsIgnoreCase("dtm-file"))) {

            // dtm file
            dtmFile = new File(parameters.get("dtm-file"));
            if (!dtmFile.exists()) {
                LOGGER.log(Level.WARNING, "DTM file does not exist {0}", dtmFile.getAbsolutePath());
                return;
            }

            // dtm VOP matrix
            if (parameters.keySet().stream().anyMatch(p -> p.equalsIgnoreCase("dtm-vop"))) {
                String strVop = parameters.get("dtm-vop");
                if (new File(strVop).exists()) {
                    dtmVop = Matrix.valueOf(readFileAsString(strVop)).toMatrix4d();
                } else {
                    // vop matrix from command line
                    dtmVop = Matrix.valueOf(strVop).toMatrix4d();
                }
            }

            // dtm margin
            if (parameters.keySet().stream().anyMatch(p -> p.equalsIgnoreCase("dtm-margin"))) {
                dtmMargin = Integer.parseInt(parameters.get("dtm-margin"));
            }
        }

    }

    private String readFileAsString(String filePath) throws IOException {

        StringBuilder fileData = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader(
                new FileReader(filePath))) {
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
            }
        }
        return fileData.toString();
    }

    /**
     * Print help message in standard output.
     */
    public static void showHelp() {

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("AMAPVox Viewer3D. Run 3D viewer for given voxel file.\n\n");
        sb.append("Usage:\n");
        sb.append("  amapvox viewer3d [options] file \n");
        sb.append("\n");
        sb.append("Options:\n");
        sb.append("  --help            ");
        sb.append("print help message.\n");
        sb.append("  --vox-var=name    ");
        sb.append("variable name to display.\n");
        sb.append("  --dtm-file=path   ");
        sb.append("path of the Digital Terrain Model file (ASCII format)\n");
        sb.append("  --dtm-vop=matrix  ");
        sb.append("VOP matrix or path to VOP matrix file.\n");
        sb.append("  --dtm-margin=int  ");
        sb.append("margin in number of voxels for cropping the DTM around the voxel file. Missing or -1 means DTM not cropped.\n");

        System.out.println(sb.toString());
    }

    private void showViewer3D(Stage stage) {

        //Matrix4d dtmVOPMatrix = Matrix.valueOf("0.9540688863574789 0.29958731629459895 0.0 -448120.0441687209 -0.29958731629459895 0.9540688863574789 0.0 -470918.3928060016 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0").toMatrix4d();
        //window size
        ObservableList<Screen> screens = Screen.getScreens();

        if (screens != null && !screens.isEmpty()) {
            screenWidth = screens.get(0).getBounds().getWidth();
            screenHeight = screens.get(0).getBounds().getHeight();
        }

        try {

            Service s = new Service() {

                @Override
                protected Task createTask() {
                    return new Task() {

                        @Override
                        protected Object call() throws Exception {

                            final SimpleViewer viewer3D = new SimpleViewer((int) (screenWidth / 4.0d), (int) (screenHeight / 4.0d), (int) (screenWidth / 1.5d), (int) (screenHeight / 1.5d), voxFile.toString());
                            //viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
                            viewer3D.setDynamicDraw(false);
                            org.amapvox.viewer3d.object.scene.Scene scene = viewer3D.getScene();

                            /**
                             * *VOXEL SPACE**
                             */
                            updateMessage("Loading voxel space: " + voxFile.getAbsolutePath());

                            final VoxelSpaceSceneObject voxelSpace = new VoxelSpaceSceneObject(voxFile);
                            voxelSpace.setMousePickable(true);

                            voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                                @Override
                                public void voxelSpaceCreationProgress(int progress) {
                                    updateProgress(progress, 100);
                                }
                            });

                            voxelSpace.loadVoxels();
                            Point3f halfVoxelSize = new Point3f(voxelSpace.getVoxelFileHeader().getVoxelSize());
                            halfVoxelSize.scale(0.5f);

                            SceneObject sceneObjectSelectedVox = new SimpleSceneObject(GLMeshFactory.createBoundingBox(
                                    -halfVoxelSize.x, -halfVoxelSize.y, -halfVoxelSize.z,
                                    halfVoxelSize.x, halfVoxelSize.y, halfVoxelSize.z), false);

                            SimpleShader simpleShader = new SimpleShader();
                            simpleShader.setColor(new Vector3f(1, 0, 0));
                            sceneObjectSelectedVox.setVisible(false);
                            sceneObjectSelectedVox.setShader(simpleShader);
                            sceneObjectSelectedVox.setDrawType(GLMesh.DrawType.LINES);

                            viewer3D.getScene().addSceneObject(sceneObjectSelectedVox);

                            final SimpleBooleanProperty spaceKeyDown = new SimpleBooleanProperty(false);

                            viewer3D.getRenderFrame().addKeyListener(new KeyAdapter() {

                                @Override
                                public void keyPressed(KeyEvent e) {
                                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                                        spaceKeyDown.set(true);
                                    }
                                }

                                @Override
                                public void keyReleased(KeyEvent e) {
                                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                                        spaceKeyDown.set(false);
                                    }
                                }
                            });

                            voxelSpace.setAttribute(voxVariable);
                            voxelSpace.setShader(new InstanceLightedShader());
                            voxelSpace.setDrawType(GLMesh.DrawType.TRIANGLES);
                            scene.addSceneObject(voxelSpace);

                            VoxelFileHeader header = new VoxelFileReader(voxFile).getHeader();

                            //coordinates offset for float precision view
                            Point3d oldMinCorner = new Point3d(header.getMinCorner());
                            Point3d max = new Point3d(header.getMaxCorner());
                            max.sub(header.getMinCorner());
                            Point3d min = new Point3d();

                            /**
                             * *DTM**
                             */
                            SceneObject dtmSceneObject = null;

                            if (dtmFile != null && dtmFile.exists()) {

                                updateMessage("Loading DTM");
                                updateProgress(0, 100);

                                Raster dtm = AsciiGridHelper.readFromAscFile(dtmFile);

                                Matrix4d dtmTransfMatrix = new Matrix4d();
                                dtmTransfMatrix.setIdentity();
                                dtmTransfMatrix.setTranslation(new Vector3d(-oldMinCorner.x, -oldMinCorner.y, -oldMinCorner.z));

                                if (dtmVop != null) {
                                    dtmTransfMatrix.mul(dtmVop);
                                }

                                dtm.setTransformationMatrix(dtmTransfMatrix);

                                if (dtmMargin >= 0) {

                                    dtm.setLimits(new BoundingBox2F(
                                            new Point2f((float) min.x, (float) min.y),
                                            new Point2f((float) max.x, (float) max.y)), dtmMargin);
                                }

                                updateMessage("Converting raster to mesh");
                                dtm.buildMesh();

                                GLMesh dtmMesh = createMeshAndComputeNormalesFromDTM(dtm);
                                dtmSceneObject = new SimpleSceneObject(dtmMesh, false);
                                dtmSceneObject.setShader(new PhongShader());
                                scene.addSceneObject(dtmSceneObject);

                                updateProgress(100, 100);

                            }

                            /**
                             * *scale**
                             */
                            updateMessage("Generating scale");
                            final Texture scaleTexture = new Texture(ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                    voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                    viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                    ScaleGradient.Orientation.HORIZONTAL, 5, 8));

                            SceneObject scalePlane = SceneObjectFactory.createTexturedPlane(
                                    new Vector3f(40, 20, 0),
                                    (int) (viewer3D.getWidth() - 80),
                                    (int) (viewer3D.getHeight() / 20),
                                    scaleTexture);

                            scalePlane.setShader(new TextureShader());
                            scalePlane.setDrawType(GLMesh.DrawType.TRIANGLES);
                            scalePlane.setName("color scale");
                            scene.addSceneObjectAsHud(scalePlane);

                            GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox(
                                    (float) min.x, (float) min.y, (float) min.z,
                                    (float) max.x, (float) max.y, (float) max.z);

                            SceneObject boundingBox = new SimpleSceneObject(boundingBoxMesh);

                            SimpleShader s = scene.simpleShader;
                            s.setColor(new Vector3f(1, 0, 0));
                            boundingBox.setShader(s);
                            boundingBox.setDrawType(GLMesh.DrawType.LINES);
                            scene.addSceneObject(boundingBox);

                            voxelSpace.addPropertyChangeListener("gradientUpdated", (PropertyChangeEvent evt) -> {
                                BufferedImage image = ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                        voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                        viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                        ScaleGradient.Orientation.HORIZONTAL, 5, 8);

                                scaleTexture.setBufferedImage(image);
                            });

                            /**
                             * Axis
                             */
//                            InputStream axis3ObjStream = SimpleViewer.class.getResourceAsStream("mesh/axis3.obj");
//                            InputStream axis3MtlStream = SimpleViewer.class.getResourceAsStream("mesh/axis3.mtl");
//                            GLMesh axisMesh = GLMeshFactory.createMeshFromObj(axis3ObjStream, axis3MtlStream);
//                            //axisMesh.scale(new Vector3f(0.2f, 0.2f, 0.2f));
//                            axisMesh.translate(new Vector3f(25, 25, 25));
//                            SceneObject axisSceneObject = new SimpleSceneObject(axisMesh);
//                            axisSceneObject.setShader(new AxisShader());
//                            System.out.println("axis " + axisSceneObject.getGravityCenter());
//                            viewer3D.getScene().addSceneObject(axisSceneObject);
                            /**
                             * *light**
                             */
                            Point3f lightPosition = new Point3f(voxelSpace.getGravityCenter());
                            lightPosition.add(new Point3f(0.f, 0.f, voxelSpace.getWidth().z + 100.f));
                            scene.setLightPosition(lightPosition);

                            /**
                             * *camera**
                             */
                            TrackballCamera trackballCamera = new TrackballCamera();
                            trackballCamera.setPivot(voxelSpace);
                            Vector3f cameraLocation = new Vector3f(voxelSpace.getGravityCenter());
                            cameraLocation.add(voxelSpace.getWidth());
                            trackballCamera.setLocation(cameraLocation);
                            viewer3D.getScene().setCamera(trackballCamera);

                            final SceneObject dtmSceneObjectFinal = dtmSceneObject;

                            Platform.runLater(() -> {
                                final Stage viewer3DStage = new Stage();
                                final FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/amapvox/viewer3d/fxml/Viewer3DFrame.fxml"));
                                try {
                                    stage.setAlwaysOnTop(false);
                                    Parent root = loader.load();
                                    Scene scene1 = new Scene(root);
                                    viewer3DStage.setScene(scene1);
                                    viewer3DStage.getIcons().addAll(stage.getIcons());
                                    Viewer3DFrameController viewer3DFrameController = loader.getController();
                                    viewer3DFrameController.setStage(viewer3DStage);
                                    viewer3DStage.setX(0);
                                    viewer3DStage.setY(0);
                                    viewer3DStage.setHeight(screenHeight);
                                    viewer3DFrameController.setViewer3D(viewer3D);
                                    viewer3DFrameController.setAttributes(voxVariable, voxelSpace.getVoxelFileHeader().getColumnNames());
                                    viewer3DFrameController.initContent(voxelSpace);
                                    viewer3DFrameController.addSceneObject(voxelSpace, voxFile.getName());
                                    viewer3DFrameController.addDefaultFilters();
                                    if (dtmSceneObjectFinal != null) {
                                        viewer3DFrameController.addSceneObject(dtmSceneObjectFinal, dtmFile.getName());
                                    }

                                    SceneObjectListener listener = (SceneObject sceneObject, MousePicker mousePicker, Point3d intersection) -> {
//                                        Vector3f camLocation = viewer3D.getScene().getCamera().getLocation();
                                        VoxelObject selectedVoxel = voxelSpace.doPicking(mousePicker);
                                        if (selectedVoxel != null) {
                                            LinkedHashMap<String, Double> attributes = new LinkedHashMap<>();
                                            for (int i = 0; i < voxelSpace.getVoxelFileHeader().getColumnNames().length; i++) {

                                                float attribut = selectedVoxel.getAttribute(i);

                                                attributes.put(voxelSpace.getVoxelFileHeader().getColumnNames()[i], Double.valueOf(attribut));
                                            }
                                            Platform.runLater(() -> {
                                                viewer3DFrameController.setAttributes(attributes);
                                            });
                                            Point3f voxelPosition = voxelSpace.getVoxelPosition(selectedVoxel.getIndex());
                                            sceneObjectSelectedVox.setPosition(new Point3f(voxelPosition));
                                            sceneObjectSelectedVox.setVisible(true);
                                            if (spaceKeyDown.get()) {
                                                viewer3D.getScene().getCamera().setTarget(new Vector3f(voxelPosition));
                                            }
                                        } else {
                                            sceneObjectSelectedVox.setVisible(false);
                                        }
                                    };
                                    voxelSpace.addSceneObjectListener(listener);

                                    // windows closed event from viewer3d
                                    viewer3D.getRenderFrame().setUndecorated(false);
                                    viewer3D.getRenderFrame().addWindowListener(new WindowAdapter() {
                                        @Override
                                        public void windowDestroyNotify(com.jogamp.newt.event.WindowEvent e) {

                                            viewer3D.close();
                                            Platform.runLater(() -> {
                                                if (viewer3DStage.isShowing()) {
                                                    viewer3DStage.close();
                                                }
                                            });
                                        }
                                    });
                                    // windows closed event from control panel
                                    viewer3DStage.setOnCloseRequest((WindowEvent event1) -> {
                                        viewer3D.close();
                                        viewer3DStage.close();
                                        event1.consume();
                                    });

                                    // show viewer and control panel
                                    viewer3DStage.show();
                                    viewer3D.show();
                                    // update initial size and position of the viewer according to the control panel
                                    Bounds bounds = viewer3DFrameController.getAnchorPaneRoot().getBoundsInLocal();
                                    final Bounds localToScreen = viewer3DFrameController.getAnchorPaneRoot().localToScreen(bounds);
                                    viewer3D.getRenderFrame().setVisible(false);
                                    viewer3D.getRenderFrame().setPosition((int) (0.9 * localToScreen.getWidth() + 0.1 * screenWidth), (int) (0.1 * screenHeight));
                                    viewer3D.getRenderFrame().setSize((int) (0.8 * (screenWidth - localToScreen.getWidth())), (int) (0.8 * screenHeight));
                                    viewer3D.getRenderFrame().setVisible(true);

                                } catch (IOException e) {
                                    LOGGER.log(Level.SEVERE, "Loading ToolBarFrame.fxml failed", e);
                                } catch (Exception e) {
                                    LOGGER.log(Level.SEVERE, "Error during toolbar init", e);
                                }
                            });

                            return null;
                        }
                    };
                }
            };

            ProgressDialog d = new ProgressDialog(s);
            d.show();

            s.start();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot launch 3d view", ex);
        }
    }

    public static GLMesh createMeshAndComputeNormalesFromDTM(Raster dtm) {

        List<Point> points = dtm.getPoints();
        List<Face> faces = dtm.getFaces();

        GLMesh mesh = new SimpleGLMesh();

        float[] vertexData = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            vertexData[j] = points.get(i).x;
            vertexData[j + 1] = points.get(i).y;
            vertexData[j + 2] = points.get(i).z;
        }

        float[] normalData = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            Vector3f meanNormale = new Vector3f(0, 0, 0);

            for (Integer faceIndex : points.get(i).faces) {

                Face face = faces.get(faceIndex);

                Point point1 = points.get(face.getPoint1());
                Point point2 = points.get(face.getPoint2());
                Point point3 = points.get(face.getPoint3());

                Vector3f vec1 = new Vector3f();
                vec1.sub(point2, point1);

                Vector3f vec2 = new Vector3f();
                vec2.sub(point3, point1);

                Vector3f cross = new Vector3f();
                cross.cross(vec2, vec1);
                cross.normalize();
                meanNormale.add(cross);
            }

            meanNormale.normalize();

            normalData[j] = meanNormale.x;
            normalData[j + 1] = meanNormale.y;
            normalData[j + 2] = meanNormale.z;
        }

        int indexData[] = new int[faces.size() * 3];
        for (int i = 0, j = 0; i < faces.size(); i++, j += 3) {

            indexData[j] = faces.get(i).getPoint1();
            indexData[j + 1] = faces.get(i).getPoint2();
            indexData[j + 2] = faces.get(i).getPoint3();
        }

        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
        mesh.vertexCount = indexData.length;

        ColorGradient gradient = new ColorGradient(dtm.getzMin(), dtm.getzMax());
        gradient.setGradientColor(ColorGradient.GRADIENT_RAINBOW3);

        float colorData[] = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            Color color = gradient.getColor(points.get(i).z);
            colorData[j] = color.getRed() / 255.0f;
            colorData[j + 1] = color.getGreen() / 255.0f;
            colorData[j + 2] = color.getBlue() / 255.0f;

        }

        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);

        return mesh;
    }
}
