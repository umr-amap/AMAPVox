/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.Matrix;
import org.amapvox.commons.Voxel;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.output.PathLengthOutput;
import org.amapvox.voxelisation.output.TxtVoxelFileOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import org.amapvox.shot.Echo;
import org.junit.Test;

/**
 * Test voxelisation algorithm for TLS shots.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class TLSVoxelizationTest {
    

    // write .vox files in temporary directory 
    private final boolean WRITE_OUTPUT = false;

    /**
     * Shots without echo. Basic tests: sampling variable and entering beam
     * fraction are one along shot path, specific shot angles, intercepted beam
     * fraction and number of echoes are zero everywhere,
     *
     * @throws Exception
     */
    @Test
    public void testUninterceptedShot() throws Exception {

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testUninterceptedTLSShot", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        List<Shot> shots = new ArrayList<>();
        // shot without echo following z-axis
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), null));
        // oblique shot without echo following (1, 1, 1) elementary vector
        shots.add(new Shot(1, new Point3d(1.5, 1.5, 1.5), new Vector3d(1, 1, 1), null));

        // process shots
        for (Shot shot : shots) {
            voxelisation.processOneShot(shot);
        }
        
        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        // assertions on vertical shot
        Voxel voxel;
        // assertion on firt voxel
        assert (voxelisation.getVoxel(0, 0, 0).pathLength == 0.5);
        for (int k = 1; k < 5; k++) {
            voxel = voxelisation.getVoxel(0, 0, k);
            assert (voxel.pathLength == 1);
            assert (voxel.npulse == 1);
            assert (voxel.enteringBeamSection == 1);
            assert (voxel.averagedPulseAngle == 0);
        }

        // assertions on oblique shot
        voxel = voxelisation.getVoxel(1, 1, 1);
        assert (Util.equal(voxel.pathLength, Math.sqrt(3.d) / 2.d));
        for (int n = 2; n < 5; n++) {
            voxel = voxelisation.getVoxel(n, n, n);
            assert (Util.equal(voxel.pathLength, Math.sqrt(3.d)));
            assert (Util.equal(Math.cos(Math.toRadians(voxel.averagedPulseAngle)), 1.d / Math.sqrt(3.d)));
        }

        // assertions on the whole voxel space
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 5; k++) {
                    voxel = voxelisation.getVoxel(i, j, k);
                    assert (voxel.interceptedBeamSection == 0);
                    assert (voxel.nhit == 0);
                }
            }

        }
    }

    /**
     * Shots with multiple echoes. Echoes in separate getVoxel. Echoes in
     * separate getVoxel, last one outside voxel space. Echoes in same getVoxel.
     * Echoes on voxel edges.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithEchoes() throws Exception {

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testTLSShotWithEchoes", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setEchoesWeightMatrix(VoxelizationCfg.DEFAULT_ECHOES_WEIGHT);
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        List<Shot> shots = new ArrayList<>();
        // shot without 2 echoes inside voxel space going along z-axis
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));
        // shot without 3 echoes, last one outside voxel space going along y-axis
        shots.add(new Shot(1, new Point3d(1.5, 0.5, 0.5), new Vector3d(0, 1, 0), new double[]{2.d, 3.d, 6.d}));
        // shot with 3 echoes, first two ones in same voxel going along y-axis
        shots.add(new Shot(2, new Point3d(2.5, 0.5, 0.5), new Vector3d(0, 1, 0), new double[]{2.d, 2.2d, 4.d}));
        // oblique shot with echoes at (3.5, 2.5, 1.5), (3.5, 3, 1.75) & (3.5, 3.5, 2)
        double dl = Math.sqrt(5.d) / 4.d;
        shots.add(new Shot(3, new Point3d(3.5, 0.5, 0.5), new Vector3d(0, 2, 1), new double[]{4 * dl, 5 * dl, 6 * dl}));
        // shot with source outside voxel space and first one outside voxel space
        shots.add(new Shot(4, new Point3d(4.5, -1.5, 0.5), new Vector3d(0, 1, 0), new double[]{1.d, 3.d, 5.d}));

        // process shots
        for (Shot shot : shots) {
            voxelisation.processOneShot(shot);
        }

        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        Voxel voxel;
        // first shot
        // assertion on voxel containing first echo
        voxel = voxelisation.getVoxel(0, 0, 1);
        assert (voxel.nhit == 1);
        assert (voxel.enteringBeamSection == 1);
        assert (voxel.interceptedBeamSection == 0.5);
        // assertion on voxel following first echo
        voxel = voxelisation.getVoxel(0, 0, 2);
        assert (voxel.enteringBeamSection == 0.5);
        assert (voxel.nhit == 0);
        // assertion on voxel containing second echo
        voxel = voxelisation.getVoxel(0, 0, 3);
        assert (voxel.nhit == 1);
        assert (voxel.enteringBeamSection == 0.5);
        assert (voxel.interceptedBeamSection == 0.5);
        // assertion on voxel following last echo
        voxel = voxelisation.getVoxel(0, 0, 4);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (voxel.enteringBeamSection == 0);

        // second shot
        // assertion on voxel containing second echo (out of three)
        voxel = voxelisation.getVoxel(1, 3, 0);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 2.d / 3.d));
        assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
        assert (voxel.averagedPulseAngle == 90);
        // assertions on voxel following second echo
        voxel = voxelisation.getVoxel(1, 4, 0);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (voxel.interceptedBeamSection == 0);

        // third shot
        // assertion on voxel containing the first two echoes
        voxel = voxelisation.getVoxel(2, 2, 0);
        assert (voxel.nhit == 2);
        assert (Util.equal(voxel.interceptedBeamSection, 2.d / 3.d));
        assert (voxel.enteringBeamSection == 1);
        // assertions on voxel following second echo
        voxel = voxelisation.getVoxel(2, 3, 0);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));

        // fourth shot
        // assertion on voxel containing first two echoes
        voxel = voxelisation.getVoxel(3, 2, 1);
        assert (voxel.nhit == 2);
        assert (Util.equal(voxel.averagedPulseAngle, Math.toDegrees(Math.atan(2.d / 1.d))));
        assert (Util.equal(voxel.pathLength, 2 * dl));
        // assertion on voxel containing third echo
        voxel = voxelisation.getVoxel(3, 3, 1);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.pathLength, dl));
        // assertion on voxel following third echo
        voxel = voxelisation.getVoxel(3, 3, 2);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.pathLength, 0));
        assert (Util.equal(voxel.enteringBeamSection, 0));
        // assertion on voxel following last echo

        // 5th shot
        // assertion on voxel preceding second echo
        voxel = voxelisation.getVoxel(4, 0, 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);
        assert (Util.equal(voxel.enteringBeamSection, 2.d / 3.d));
        assert (voxel.interceptedBeamSection == 0.d);
        // assertion on voxel containing second echo
        voxel = voxelisation.getVoxel(4, 1, 0);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 2.d / 3.d));
        assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
        // assertion on voxel following second echo
        voxel = voxelisation.getVoxel(4, 2, 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (voxel.interceptedBeamSection == 0.d);
        // assertion on voxel containing third echo
        voxel = voxelisation.getVoxel(4, 3, 0);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
        // assertion on voxel following second echo
        voxel = voxelisation.getVoxel(4, 4, 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (voxel.enteringBeamSection == 0.d);
    }

    /**
     * Echo weighting inflated (beam fraction reaches zero before last echo).
     * Shot path interrupted prematurely.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithOverWeightedEchoes() throws Exception {

        Matrix overweight = new Matrix(new double[][]{
            {1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1.d, 1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.5d, 0.5d, 0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1 / 3.d, 1 / 3.d, 1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN},
            {0.25d, 0.25d, 0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN},
            {0.2d, 0.2d, 0.2d, 0.2d, 0.2d, 0.2d, Double.NaN},
            {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d}});

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testTLSShotWithOverWeightedEchoes", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setEchoesWeightMatrix(overweight);
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        try {
            voxelisation.init();
        } catch (IOException ex) {
            assert (ex.getMessage().contains("Inconsistent echo weighting table"));
        }
    }

    /**
     * Echo weighting attenuated (beam fraction does not reach zero). Shot path
     * extended beyond last echo.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithUnderWeightedEchoes() throws Exception {

        Matrix underweight = new Matrix(new double[][]{
            {0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.2d, 0.2d, 0.2d, 0.2d, Double.NaN, Double.NaN, Double.NaN},
            {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, Double.NaN, Double.NaN},
            {1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, Double.NaN},
            {1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d}});

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testTLSShotWithUnderWeightedEchoes", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setEchoesWeightMatrix(underweight);
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        // one shot goinp up z-axis with 2 echoes
        voxelisation.processOneShot(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));

        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        Voxel voxel;
        // first shot
        // assertion on voxel containing first echo
        voxel = voxelisation.getVoxel(0, 0, 1);
        assert (voxel.nhit == 1);
        assert (voxel.enteringBeamSection == 1);
        assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
        // assertions on voxel following second echo
        voxel = voxelisation.getVoxel(0, 0, 4);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (voxel.interceptedBeamSection == 0);
        assert (voxel.npulse == 1);
    }

    @Test
    public void testShotMonoEcho() throws Exception {

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testTLSShotMonoEcho", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setEchoesWeightMatrix(VoxelizationCfg.DEFAULT_ECHOES_WEIGHT);
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MONO_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        // one shot goinp up z-axis without echo
        voxelisation.processOneShot(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), null));
        // one shot goinp up z-axis with 1 echo
        voxelisation.processOneShot(new Shot(0, new Point3d(0.5, 1.5, 0.5), new Vector3d(0, 0, 1), new double[]{2.d}));
        // one shot goinp up z-axis with 2 echoes
        // (non sense but voxelisation algorithm should stop propagation after first echo anyway)
        voxelisation.processOneShot(new Shot(0, new Point3d(0.5, 2.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));

        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        // assertions
        Voxel voxel;
        // first shot
        for (int k = 1; k < 5; k++) {
            voxel = voxelisation.getVoxel(0, 0, k);
            assert (voxel.nhit == 0);
            assert (voxel.enteringBeamSection == 1);
            assert (voxel.interceptedBeamSection == 0);
            assert (voxel.npulse == 1);
        }
        // second shot
        voxel = voxelisation.getVoxel(0, 1, 2);
        assert (voxel.nhit == 1);
        assert (voxel.interceptedBeamSection == 1);
        voxel = voxelisation.getVoxel(0, 1, 3);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (voxel.enteringBeamSection == 0);
        // third shot
        voxel = voxelisation.getVoxel(0, 2, 1);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.interceptedBeamSection, 1));
        voxel = voxelisation.getVoxel(0, 2, 2);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (voxel.enteringBeamSection == 0);
    }

    @Test
    public void testShotFilteredEchoes() throws Exception {

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testTLSShotFilteredEchoes", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setEchoesWeightMatrix(VoxelizationCfg.DEFAULT_ECHOES_WEIGHT);
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // list of shots
        List<Shot> shots = new ArrayList<>();
        // shot0, discard 2nd echo
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 2.d, 3.d}));
        // shot1, discard 3rd echo
        shots.add(new Shot(1, new Point3d(1.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 2.d, 3.d}));
        // shot2, discard all echoes
        shots.add(new Shot(2, new Point3d(2.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 2.d, 3.d}));
        // shot3, discard 3rd echo
        shots.add(new Shot(3, new Point3d(3.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 1.8d, 2.2d, 3.d}));
        // shot4, discard 2nd & 3rd echo
        shots.add(new Shot(4, new Point3d(4.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 1.8d, 2.2d, 3.d}));

        // custom echo filter to discard afore-mentionned echoes
        cfg.addEchoFilter(new Filter<Echo>() {
            @Override
            public void init() throws Exception {
                // nothing to do
            }

            @Override
            public boolean accept(Echo echo) throws Exception {
                switch (echo.getShot().index) {
                    case 0:
                        // discard 2nd echo
                        return echo.getRank() != 1;
                    case 1:
                        // discard 3rd echo
                        return echo.getRank() < 2;
                    case 2:
                        // discard all echoes
                        return false;
                    case 3:
                        // discard 3rd echo
                        return echo.getRank() != 2;
                    case 4:
                        return echo.getRank() == 0 || echo.getRank() > 2;
                    default:
                        return true;
                }
            }
        });

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        // process shots
        for (Shot shot : shots) {
            voxelisation.processOneShot(shot);
        }

        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        // assertions
        Voxel voxel;
        // shot0
        // voxel with discarded echo
        voxel = voxelisation.getVoxel(0, 0, 2);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);
        assert (Util.equal(voxel.enteringBeamSection, 2.d / 3.d));
        assert (voxel.interceptedBeamSection == 0);
        // following voxel containing one echo
        voxel = voxelisation.getVoxel(0, 0, 3);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (voxel.nhit == 1);
        assert (voxel.npulse == 1);
        // last voxel, unsampled
        voxel = voxelisation.getVoxel(0, 0, 4);
        assert (voxel.enteringBeamSection == 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);

        // shot1
        // third voxel with discarded echo
        voxel = voxelisation.getVoxel(1, 0, 3);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);

        // shot2
        // first voxel sampled but no echo
        voxel = voxelisation.getVoxel(2, 0, 0);
        assert (voxel.npulse == 1);
        // other voxels with discarded echoes
        double bsEntering = 1.d;
        for (int k = 1; k < 5; k++) {
            voxel = voxelisation.getVoxel(2, 0, k);
            assert (voxel.nhit == 0);
            assert (voxel.npulse == (Util.equal(bsEntering, 0) ? 0 : 1));
            assert (Util.equal(voxel.enteringBeamSection, bsEntering));
            assert (voxel.interceptedBeamSection == 0);
            bsEntering = Math.max(0.d, bsEntering - (1.d / 3.d));
        }

        // shot3
        // voxel containing 2nd & 3rd echoes, 3rd one being discarded
        voxel = voxelisation.getVoxel(3, 0, 2);
        assert (Util.equal(voxel.enteringBeamSection, 0.75));
        assert (Util.equal(voxel.interceptedBeamSection, 0.25));
        assert (voxel.npulse == 1);
        assert (voxel.nhit == 1);
        // voxel containing last echo
        voxel = voxelisation.getVoxel(3, 0, 3);
        assert (Util.equal(voxel.enteringBeamSection, 0.25));
        assert (Util.equal(voxel.interceptedBeamSection, 0.25));
        assert (voxel.nhit == 1);

        // shot4
        // voxel containing 2nd & 3rd echoes, both discarded
        voxel = voxelisation.getVoxel(4, 0, 2);
        assert (Util.equal(voxel.enteringBeamSection, 0.75d));
        assert (voxel.interceptedBeamSection == 0);
        assert (voxel.npulse == 1);
        assert (voxel.nhit == 0);
        // voxel containing last echo
        voxel = voxelisation.getVoxel(4, 0, 3);
        assert (Util.equal(voxel.enteringBeamSection, 0.25));
        assert (Util.equal(voxel.interceptedBeamSection, 0.25));
        assert (voxel.nhit == 1);

    }

    @Test
    public void testShotFilteredUnderWeightedEchoes() throws Exception {

        Matrix underweight = new Matrix(new double[][]{
            {0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.2d, 0.2d, 0.2d, 0.2d, Double.NaN, Double.NaN, Double.NaN},
            {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, Double.NaN, Double.NaN},
            {1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, Double.NaN},
            {1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d}});

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testTLSShotFilteredUnderWeightedEchoes", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setEchoesWeightMatrix(underweight);
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // list of shots
        List<Shot> shots = new ArrayList<>();
        // shot0, discard 2nd echo
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 2.d, 3.d}));
        // shot1, discard 3rd echo
        shots.add(new Shot(1, new Point3d(1.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 2.d, 3.d}));
        // shot2, discard all echoes
        shots.add(new Shot(2, new Point3d(2.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 2.d, 3.d}));
        // shot3, discard 3rd echo
        shots.add(new Shot(3, new Point3d(3.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 1.8d, 2.2d, 3.d}));
        // shot4, discard 2nd & 3rd echo
        shots.add(new Shot(4, new Point3d(4.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 1.8d, 2.2d, 3.d}));

        // custom echo filter to discard afore-mentionned echoes
        cfg.addEchoFilter(new Filter<Echo>() {
            @Override
            public void init() throws Exception {
                // nothing to do
            }

            @Override
            public boolean accept(Echo echo) throws Exception {
                switch (echo.getShot().index) {
                    case 0:
                        // discard 2nd echo
                        return echo.getRank() != 1;
                    case 1:
                        // discard 3rd echo
                        return echo.getRank() < 2;
                    case 2:
                        // discard all echoes
                        return false;
                    case 3:
                        // discard 3rd echo
                        return echo.getRank() != 2;
                    case 4:
                        return echo.getRank() == 0 || echo.getRank() > 2;
                    default:
                        return true;
                }
            }
        });

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        // process shots
        for (Shot shot : shots) {
            voxelisation.processOneShot(shot);
        }

        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        // assertions
        Voxel voxel;
        // shot0
        // voxel containing third echo
        voxel = voxelisation.getVoxel(0, 0, 3);
        assert (Util.equal(voxel.enteringBeamSection, 0.5));
        assert (Util.equal(voxel.interceptedBeamSection, 0.25));
        assert (voxel.nhit == 1);
        assert (voxel.npulse == 1);
        // last voxel sampled with residual energy
        voxel = voxelisation.getVoxel(0, 0, 4);
        assert (Util.equal(voxel.enteringBeamSection, 0.25));
        assert (voxel.nhit == 0);
        assert (voxel.interceptedBeamSection == 0);
        assert (voxel.npulse == 1);

        // shot1
        // last voxel sampled with residual energy
        voxel = voxelisation.getVoxel(1, 0, 4);
        assert (Util.equal(voxel.enteringBeamSection, 0.25));
        assert (voxel.nhit == 0);
        assert (voxel.interceptedBeamSection == 0);
        assert (voxel.npulse == 1);

        // shot2
        // voxels 1, 2 & 3 echoes discarded but voxel sampled
        double bsEntering = 1.d;
        for (int k = 1; k < 4; k++) {
            voxel = voxelisation.getVoxel(2, 0, k);
            assert (voxel.nhit == 0);
            assert (voxel.npulse == 1);
            assert (Util.equal(voxel.enteringBeamSection, bsEntering));
            assert (voxel.interceptedBeamSection == 0);
            bsEntering -= 0.25d;
        }
        // last voxel sampled with residual energy
        voxel = voxelisation.getVoxel(2, 0, 4);
        assert (Util.equal(voxel.enteringBeamSection, 0.25));
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);

        // shot3
        // last voxel sampled with residual energy
        voxel = voxelisation.getVoxel(3, 0, 4);
        assert (Util.equal(voxel.enteringBeamSection, 0.2));
        assert (voxel.nhit == 0);
        assert (voxel.interceptedBeamSection == 0);
        assert (voxel.npulse == 1);

        // shot4
        // voxel containing last echo
        voxel = voxelisation.getVoxel(4, 0, 3);
        assert (Util.equal(voxel.enteringBeamSection, 0.4));
        assert (Util.equal(voxel.interceptedBeamSection, 0.2));
        assert (voxel.nhit == 1);
        // last voxel sampled with residual energy
        voxel = voxelisation.getVoxel(4, 0, 4);
        assert (Util.equal(voxel.enteringBeamSection, 0.2));
        assert (voxel.nhit == 0);
        assert (voxel.interceptedBeamSection == 0);
        assert (voxel.npulse == 1);
    }
}
