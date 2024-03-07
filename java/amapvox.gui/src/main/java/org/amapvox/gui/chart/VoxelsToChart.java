/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.chart;

import org.amapvox.voxelfile.VoxelFileVoxel;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.voxelfile.VoxelFileReader;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.util.Iterator;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.HorizontalAlignment;

/**
 *
 * @author calcul
 */
public class VoxelsToChart {

    private final static Logger LOGGER = Logger.getLogger(VoxelsToChart.class);

    private final VoxelFileChart[] voxelFiles;
    public final static XYLineAndShapeRenderer DEFAULT_RENDERER = (XYLineAndShapeRenderer) ((XYPlot) ChartFactory.createXYLineChart(
            "", "", "", null, PlotOrientation.VERTICAL, true, true, false).getPlot()).getRenderer();

    //quadrats
    private boolean makeQuadrats;
    private QuadratAxis axis;
    private int split = 1;
    private int length = -1;

    public enum QuadratAxis {

        X_AXIS(0),
        Y_AXIS(1),
        Z_AXIS(2);

        private final int axis;

        private QuadratAxis(int axis) {
            this.axis = axis;
        }
    }

    public enum LayerReference {

        FROM_ABOVE_GROUND("Height above ground"),
        FROM_BELOW_CANOPEE("Height below canopy");

        private final String label;

        public String getLabel() {
            return label;
        }

        private LayerReference(String label) {
            this.label = label;
        }
    }

    public VoxelsToChart(VoxelFileChart voxelFile) {
        voxelFiles = new VoxelFileChart[]{voxelFile};
    }

    public VoxelsToChart(VoxelFileChart[] voxelFiles) {
        this.voxelFiles = voxelFiles;

        for (VoxelFileChart voxelFileChart : this.voxelFiles) {
            try {
                voxelFileChart.reader = new VoxelFileReader(voxelFileChart.file);
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
    }

    public void configureQuadrats(QuadratAxis axis, int split, int length) {

        makeQuadrats = true;
        this.axis = axis;
        this.split = split;
        this.length = length;
    }

    private int getSplitCount(int length) {

        if (makeQuadrats) {

            int maxSplitCount = 0;
            int currentSplitCount = 0;

            for (VoxelFileChart file : voxelFiles) {

                switch (axis) {
                    case X_AXIS:
                        currentSplitCount = (int) (file.reader.getHeader().getDimension().x / length);
                        break;
                    case Y_AXIS:
                        currentSplitCount = (int) (file.reader.getHeader().getDimension().y / length);
                        break;
                    case Z_AXIS:
                        currentSplitCount = (int) (file.reader.getHeader().getDimension().z / length);
                        break;
                }

                if (currentSplitCount > maxSplitCount) {
                    maxSplitCount = currentSplitCount;
                }
            }

            return maxSplitCount;

        } else {
            return 0;
        }
    }

    private int[] getIndiceRange(VoxelFileChart voxelFile, int quadratIndex) {

        int quadLength = 0; //longueur en voxels
        Point3d resolution = voxelFile.reader.getHeader().getVoxelSize();

        switch (axis) {
            case X_AXIS:
                if (split != -1) {
                    quadLength = voxelFile.reader.getHeader().getDimension().x / split;
                } else if (length != -1) {
                    quadLength = (int) (length / resolution.x);
                }
                break;
            case Y_AXIS:
                if (split != -1) {
                    quadLength = voxelFile.reader.getHeader().getDimension().y / split;
                } else if (length != -1) {
                    quadLength = (int) (length / resolution.y);
                }
                break;
            case Z_AXIS:
                if (split != -1) {
                    quadLength = voxelFile.reader.getHeader().getDimension().z / split;
                } else if (length != -1) {
                    quadLength = (int) (length / resolution.z);
                }
                break;
        }

        int indiceMin = (int) (quadratIndex * (quadLength - 1));
        int indiceMax = (int) ((quadratIndex + 1) * (quadLength - 1));

        return new int[]{indiceMin, indiceMax};
    }

    private int getQuadratNumber(int split, int length) {

        if (split == -1) {
            return getSplitCount(length);
        } else {
            return split;
        }
    }

    public JFreeChart[] getAttributProfileCharts(String attribut, LayerReference reference) {

        boolean inverseRangeAxis;

        inverseRangeAxis = !(reference == LayerReference.FROM_ABOVE_GROUND);

        int quadratNumber = getQuadratNumber(split, length);

        JFreeChart[] charts = new JFreeChart[quadratNumber];

        for (int i = 0; i < quadratNumber; i++) {

            XYSeriesCollection dataset = new XYSeriesCollection();

            for (VoxelFileChart voxelFile : voxelFiles) {

                int[] indices = getIndiceRange(voxelFile, i);

                XYSeries serie = createAttributeProfileSerie(voxelFile.reader, attribut, voxelFile.getSeriesParameters().getLabel(), indices[0], indices[1], reference);
                dataset.addSeries(serie);
            }

            String title = attribut + " profile";
            if (quadratNumber > 1) {
                title += " - quadrat " + (i + 1);
            }
            charts[i] = createChart(title, dataset, attribut, reference.getLabel());
            ((XYPlot) charts[i].getPlot()).getRangeAxis().setInverted(inverseRangeAxis);

        }

        return charts;
    }

    public JFreeChart createChart(String title, XYSeriesCollection dataset, String xAxisLabel, String yAxisLabel) {

        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        String fontName = "Palatino";
        chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));
        XYPlot plot = (XYPlot) chart.getPlot();

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.getDomainAxis().setLowerBound(0.0);
        plot.getRangeAxis().setLowerBound(0.0);

        plot.getDomainAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        chart.getLegend().setFrame(BlockBorder.NONE);

        LegendTitle subtitle = (LegendTitle) chart.getSubtitles().get(0);
        subtitle.setHorizontalAlignment(HorizontalAlignment.LEFT);

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setDefaultShapesVisible(true);

            Ellipse2D.Float shape = new Ellipse2D.Float(-2.5f, -2.5f, 5.0f, 5.0f);

            for (int i = 0; i < voxelFiles.length; i++) {
                renderer.setSeriesShape(i, shape);
                renderer.setSeriesPaint(i, voxelFiles[i].getSeriesParameters().getColor());
                renderer.setLegendTextPaint(i, voxelFiles[i].getSeriesParameters().getColor());
            }
        }

        return chart;
    }

    private boolean doQuadratFiltering(VoxelFileVoxel voxel, int indiceMin, int indiceMax) {

        if (makeQuadrats) {

            switch (axis) {
                case X_AXIS:
                    if (voxel.i < indiceMin || voxel.i > indiceMax) {
                        return true;
                    }
                    break;
                case Y_AXIS:
                    if (voxel.j < indiceMin || voxel.j > indiceMax) {
                        return true;
                    }
                    break;
                case Z_AXIS:
                    if (voxel.k < indiceMin || voxel.k > indiceMax) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    private XYSeries createAttributeProfileSerie(VoxelFileReader reader, String attributName, String key, int indiceMin, int indiceMax, LayerReference reference) {

        // column indices in voxel file
        int indexNPulse = reader.getHeader().findColumnIndex(OutputVariable.NUMBER_OF_SHOTS) - 3;
        int indexNHit = reader.getHeader().findColumnIndex(OutputVariable.NUMBER_OF_ECHOES) - 3;
        int indexGroundDistance = reader.getHeader().findColumnIndex(OutputVariable.GROUND_DISTANCE) - 3;
        // voxel iterator
        Iterator<VoxelFileVoxel> iterator;

        // maximal ground distance in voxel space
        double maxHeight = -1.d;
        // canopy index
        int[][] canopeeArray = new int[reader.getHeader().getDimension().x][reader.getHeader().getDimension().y];
        // loop over voxels
        iterator = reader.iterator();
        int nz = reader.getHeader().getDimension().z;
        while (iterator.hasNext()) {
            VoxelFileVoxel voxel = iterator.next();
            // max ground distance
            float height = Float.valueOf(voxel.variables[indexGroundDistance]);
            if ((voxel.k == nz - 1) && (height > maxHeight)) {
                maxHeight = height;
            }
            // canopy index
            int npulse = Integer.valueOf(voxel.variables[indexNPulse]);
            int nhit = Integer.valueOf(voxel.variables[indexNHit]);
            if (npulse > 0 && nhit > 0 && (voxel.k > canopeeArray[voxel.i][voxel.j])) {
                canopeeArray[voxel.i][voxel.j] = voxel.k;
            }
        }

        double vz = reader.getHeader().getVoxelSize().z;
        int nLayer = (int) Math.ceil(maxHeight / vz);

        double[] meanValue = new double[nLayer];
        int[] nValue = new int[nLayer];

        int indexField = reader.getHeader().findColumnIndex(attributName) - 3;

        iterator = reader.iterator();
        while (iterator.hasNext()) {

            VoxelFileVoxel voxel = iterator.next();
            double value = Double.valueOf(voxel.variables[indexField]);

            if (!Double.isNaN(value)) {
                if (!doQuadratFiltering(voxel, indiceMin, indiceMax)) {

                    int iLayer = -1;
                    switch (reference) {
                        case FROM_BELOW_CANOPEE:
                            iLayer = canopeeArray[voxel.i][voxel.j] - voxel.k;
                            break;
                        case FROM_ABOVE_GROUND:
                            double groundDistance = Double.valueOf(voxel.variables[indexGroundDistance]);
                            iLayer = (int) (groundDistance / vz);
                    }
                    if (iLayer >= 0 & iLayer < nLayer) {
                        meanValue[iLayer] += value;
                        nValue[iLayer]++;
                    }
                }
            }
        }

        int iTopLayer = nLayer - 1;
        for (int i = nLayer - 1; i > 0; i--) {
            if (meanValue[i] != 0) {
                iTopLayer = i;
                break;
            }
        }
        int iBottomLayer = 0;
        for (int i = 0; i < nLayer; i++) {
            if (meanValue[i] != 0) {
                iBottomLayer = i;
                break;
            }
        }

        final XYSeries series = new XYSeries(key, false);
        double integratedValue = 0.d;
        for (int i = iBottomLayer; i <= iTopLayer; i++) {
            meanValue[i] = meanValue[i] / nValue[i];
            series.add(meanValue[i], i * vz);
            if (!Double.isNaN(meanValue[i])) {
                integratedValue += meanValue[i];
            }

        }

        // for PAD profile, computes PAI
        integratedValue *= vz; // simplification of PAD * vox_volume / vox_ground_surface
        if (OutputVariable.find(attributName).equals(OutputVariable.PLANT_AREA_DENSITY)) {
            series.setKey(key + " PAI = " + (Math.round(integratedValue * 10)) / 10.0);
        } else {
            series.setKey(key);
        }
        return series;
    }

}
