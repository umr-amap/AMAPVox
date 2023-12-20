/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.voxelisation.LaserSpecification;
import org.amapvox.voxelisation.Voxelization;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.voxelisation.VoxelSpace;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.Voxel;
import org.amapvox.shot.Shot;
import org.amapvox.shot.filter.EchoRangeFilter;
import org.amapvox.voxelisation.output.PathLengthOutput;
import org.amapvox.voxelisation.output.TxtVoxelFileOutput;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import org.amapvox.shot.weight.EqualEchoWeight;
import org.junit.Test;

/**
 * Test voxelisation algorithm for ALS shots.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class ALSVoxelelizationTest {

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
        cfg.setOutputFile(java.io.File.createTempFile("testUninterceptedALSShot", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MONO_ECHO);
        
        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        List<Shot> shots = new ArrayList<>();
        // shot without echo following z-axis
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 9.5), new Vector3d(0, 0, -1), null));
        // oblique shot without echo following (1, 1, 1) elementary vector
        shots.add(new Shot(1, new Point3d(1.5, 1.5, 9.5), new Vector3d(1, 0, -5), null));

        // process shots
        for (Shot shot : shots) {
            voxelisation.processOneShot(shot);
        }
        // compute plant area 
        //voxelisation.postProcess();

        // write voxel file in temporary directory
        if (WRITE_OUTPUT) {
            TxtVoxelFileOutput output = new TxtVoxelFileOutput(null, cfg, voxelisation.getVoxelSpace(), true);
            output.write();
        }

        // assertions on vertical shot
        for (int k = 4; k > 0; k--) {
            Voxel voxel = voxelisation.getVoxel(0, 0, k);
            assert (Util.equal(voxel.pathLength, 1.d));
            assert (voxel.npulse == 1);
            assert (voxel.enteringBeamSection == 1);
            assert (voxel.averagedPulseAngle == 180.f);
        }

        // assertions on oblique shot
        Voxel[] getVoxel = new Voxel[]{voxelisation.getVoxel(2, 1, 4),
            voxelisation.getVoxel(2, 1, 3),
            voxelisation.getVoxel(2, 1, 2),
            voxelisation.getVoxel(3, 1, 1),
            voxelisation.getVoxel(3, 1, 0)
        };
        for (Voxel voxel : getVoxel) {
            assert (Util.equal(voxel.pathLength, Math.sqrt(26.d) / 5.d));
            assert (Util.equal(Math.cos(Math.toRadians(voxel.averagedPulseAngle)), -5.d / Math.sqrt(26.d)));
        }

        // assertions on the whole voxel space
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 5; k++) {
                    Voxel voxel = voxelisation.getVoxel(i, j, k);
                    assert (voxel.interceptedBeamSection == 0);
                    assert (voxel.nhit == 0);
                }
            }
        }
    }

    /**
     * Shots with multiple echoes.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithEchoes() throws Exception {
        
        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testALSShotWithEchoes", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.addEchoWeight(new EqualEchoWeight(true));
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        List<Shot> shots = new ArrayList<>();
        // shot without 2 echoes inside voxel space going along z-axis
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 9.5), new Vector3d(0, 0, -1), new double[]{6.d, 8.d}));
        // shot without 3 echoes, first two ones outside voxel sapce going along z-axis
        shots.add(new Shot(1, new Point3d(1.5, 0.5, 9.5), new Vector3d(0, 0, -1), new double[]{2.d, 3.d, 5.d, 7.d}));
        // shot with 3 echoes, first two ones in same voxel going along z-axis
        shots.add(new Shot(2, new Point3d(2.5, 0.5, 9.5), new Vector3d(0, 0, -1), new double[]{6.d, 6.2d, 8.d}));
        // oblique shot with echoes in getVoxel [3, 1, 4], [3, 1, 3] & [3, 2, 1]
        double dl = Math.sqrt(26.d) / 5.d;
        shots.add(new Shot(3, new Point3d(3.5, 0.5, 9.5), new Vector3d(0, 1, -5), new double[]{5 * dl, 6 * dl, 8 * dl}));

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
        voxel = voxelisation.getVoxel(0, 0, 3);
        assert (voxel.nhit == 1);
        assert (voxel.enteringBeamSection == 1);
        assert (voxel.interceptedBeamSection == 0.5);
        // assertion on voxel following first echo
        voxel = voxelisation.getVoxel(0, 0, 2);
        assert (voxel.enteringBeamSection == 0.5);
        assert (voxel.nhit == 0);
        // assertion on voxel containing second echo
        voxel = voxelisation.getVoxel(0, 0, 1);
        assert (voxel.nhit == 1);
        assert (voxel.enteringBeamSection == 0.5);
        assert (voxel.interceptedBeamSection == 0.5);
        // assertion on voxel following last echo
        voxel = voxelisation.getVoxel(0, 0, 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (voxel.enteringBeamSection == 0);

        // second shot
        // assertion on voxel containing third echo (out of four)
        voxel = voxelisation.getVoxel(1, 0, 4);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.5));
        assert (Util.equal(voxel.interceptedBeamSection, 0.25));
        assert (voxel.averagedPulseAngle == 180.d);
        // assertions on voxel following third echo
        voxel = voxelisation.getVoxel(1, 0, 3);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.enteringBeamSection, 0.25d));
        assert (voxel.interceptedBeamSection == 0);
        // assertions on voxel containing fourth echo 
        voxel = voxelisation.getVoxel(1, 0, 2);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.25d));
        assert (voxel.interceptedBeamSection == 0.25d);
        // assertions on voxel following third echo
        voxel = voxelisation.getVoxel(1, 0, 1);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (Util.equal(voxel.enteringBeamSection, 0));

        // third shot
        // assertion on voxel containing the first two echoes
        voxel = voxelisation.getVoxel(2, 0, 3);
        assert (voxel.nhit == 2);
        assert (Util.equal(voxel.interceptedBeamSection, 2.d / 3.d));
        assert (voxel.enteringBeamSection == 1);
        // assertions on voxel following second echo
        voxel = voxelisation.getVoxel(2, 0, 2);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        // assertions on voxel containing third echo 
        voxel = voxelisation.getVoxel(2, 0, 1);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
        assert (voxel.interceptedBeamSection == 1.d / 3.d);
        // assertions on voxel following third echo
        voxel = voxelisation.getVoxel(2, 0, 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (Util.equal(voxel.enteringBeamSection, 0));

        // fourth shot
        // assertion on voxel containing first echo
        voxel = voxelisation.getVoxel(3, 1, 4);
        assert (voxel.nhit == 1);
        assert (Util.equal(Math.cos(Math.toRadians(voxel.averagedPulseAngle)), -5.d / Math.sqrt(26.d)));
        assert (Util.equal(voxel.pathLength, dl));
        // assertion on voxel containing second echo
        voxel = voxelisation.getVoxel(3, 1, 3);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.pathLength, dl));
        // assertion on voxel following second echo
        voxel = voxelisation.getVoxel(3, 1, 2);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.pathLength, dl));
        // assertion on voxel containing third echo
        voxel = voxelisation.getVoxel(3, 2, 1);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.pathLength, dl));
        // assertion on voxel following last echo
        voxel = voxelisation.getVoxel(3, 2, 0);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (Util.equal(voxel.enteringBeamSection, 0));
    }

    @Test
    public void testIncompleteALSShots() throws Exception {
        
        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testIncompleteALSShots", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.addEchoWeight(new EqualEchoWeight(true));
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();

        List<Shot> shots = new ArrayList<>();
        // shot with 4 echoes, first two ones missing
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 9.5), new Vector3d(0, 0, -1), new double[]{0.d, 0.d, 5.d, 7.d}));
        // oblique shot with 3 echoes, last one missing
        double dl = Math.sqrt(45.d) / 6.d;
        shots.add(new Shot(1, new Point3d(1.5, 2.5, 6.5), new Vector3d(0, 3, -6), new double[]{2 * dl, 3 * dl, 0}));
        // oblique shots with 5 echoes, first two ones and last two ones missing
        dl = Math.sqrt(2.d);
        shots.add(new Shot(2, new Point3d(2.5, 0.5, 6.5), new Vector3d(0, 6, -6), new double[]{0.d, 0.d, 3 * dl, 0.d, 0.d}));

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
        // assertion on voxel containing third echo (out of four)
        voxel = voxelisation.getVoxel(0, 0, 4);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.5d));
        assert (Util.equal(voxel.interceptedBeamSection, 0.25d));
        assert (voxel.averagedPulseAngle == 180);
        // assertions on voxel following third echo
        voxel = voxelisation.getVoxel(0, 0, 3);
        assert (voxel.nhit == 0);
        assert (Util.equal(voxel.enteringBeamSection, 0.25d));
        assert (voxel.interceptedBeamSection == 0);
        // assertions on voxel containing fourth echo 
        voxel = voxelisation.getVoxel(0, 0, 2);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.25d));
        assert (voxel.interceptedBeamSection == 0.25d);
        // assertions on voxel following third echo
        voxel = voxelisation.getVoxel(0, 0, 1);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 0);
        assert (Util.equal(voxel.enteringBeamSection, 0));

        // second shot
        // assertion on voxel containing first echo
        voxel = voxelisation.getVoxel(1, 3, 4);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 1.d));
        assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
        assert (Util.equal(Math.cos(Math.toRadians(voxel.averagedPulseAngle)), -6.d / Math.sqrt(45.d)));
        assert (Util.equal(voxel.pathLength, Math.sqrt(45.d) / 6.d));
        // assertion on voxel containing second echo
        voxel = voxelisation.getVoxel(1, 3, 3);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 2.d / 3.d ));
        assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
        // assertion on following voxel on the trajectory
        voxel = voxelisation.getVoxel(1, 4, 3);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);
        assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));

        // third shot
        // assertion on first voxel crossed by the ray
        voxel = voxelisation.getVoxel(2, 2, 4);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.6d));
        assert (Util.equal(voxel.interceptedBeamSection, 0.d));
        assert (Util.equal(Math.cos(Math.toRadians(voxel.averagedPulseAngle)), -1.d / Math.sqrt(2.d)));
        assert (Util.equal(voxel.pathLength, Math.sqrt(2.d)));
        // assertion on voxel crossed by third echo (only echo in voxel space)
        voxel = voxelisation.getVoxel(2, 3, 3);
        assert (voxel.nhit == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.6d));
        assert (Util.equal(voxel.interceptedBeamSection, 0.2d));
        // assertion on voxel following third echo
        // we expect propagation to carry on since there are two more echoes outside voxel space
        voxel = voxelisation.getVoxel(2, 4, 2);
        assert (voxel.nhit == 0);
        assert (voxel.npulse == 1);
        assert (Util.equal(voxel.enteringBeamSection, 0.4d));
        assert (Util.equal(voxel.interceptedBeamSection, 0.d));
    }

    @Test
    public void testInconsistentALSShots() throws Exception {
        
        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.setVoxelsFormat(VoxelizationCfg.VoxelsFormat.VOXEL);
        cfg.setOutputFile(java.io.File.createTempFile("testInconsistentALSShots", ".vox"));
        cfg.setMinCorner(new Point3d(0, 0, 0));
        cfg.setMaxCorner(new Point3d(5, 5, 5));
        cfg.setDimension(new Point3i(5, 5, 5));
        cfg.setVoxelSize(new Point3d(1.d, 1.d, 1.d));
        cfg.addEchoWeight(new EqualEchoWeight(true));
        cfg.setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MULTI_ECHO);

        // create new voxel analysis
        Voxelization voxelisation = new Voxelization(cfg, "[Voxelisation]",
                null, // dtm
                new VoxelSpace(cfg.getDimension(), cfg.getMinCorner(), cfg.getVoxelSize(), null),
                new PathLengthOutput(null, cfg, null, false));
        voxelisation.init();
        
        // check whether the shot integrity filter is enabled
        boolean shotIntegrityFilterEnabled = false;
        for (Filter f : voxelisation.getShotFilters()) {
            if (f instanceof EchoRangeFilter) {
                shotIntegrityFilterEnabled = true;
                break;
            }
        }

        List<Shot> shots = new ArrayList<>();
        // inconsistent shot, with third echo missing though fourth echo is provided
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 9.5), new Vector3d(0, 0, -1), new double[]{1.d, 5.d, 0.d, 7.d}));
        // inconsistent shot, echo ranges not ascending
        shots.add(new Shot(1, new Point3d(1.5, 0.5, 9.5), new Vector3d(0, 0, -1), new double[]{5.d, 7.d, 6.d}));

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

        // first shot, inconsistent missing echoes
        if (shotIntegrityFilterEnabled) {
            for (int k = 4; k > 0; k--) {
                voxel = voxelisation.getVoxel(0, 0, k);
                assert (voxel.nhit == 0);
                assert (voxel.npulse == 0);
                assert (Util.equal(voxel.enteringBeamSection, 0));
            }
        } else {
            // assertion on voxel containing second echo
            voxel = voxelisation.getVoxel(0, 0, 4);
            assert (voxel.nhit == 1);
            assert (voxel.npulse == 1);
            assert (Util.equal(voxel.enteringBeamSection, 0.75d));
            assert (Util.equal(voxel.interceptedBeamSection, 0.25d));
            // assertion on following getVoxel (missing echo so it propagates without interception
            for (int k = 3; k > 0; k--) {
                voxel = voxelisation.getVoxel(0, 0, k);
                assert (voxel.nhit == 0);
                assert (voxel.npulse == 1);
                assert (Util.equal(voxel.enteringBeamSection, 0.5d));
            }
        }
        // second shot, descending echoes
        if (shotIntegrityFilterEnabled) {
            for (int k = 4; k > 0; k--) {
                voxel = voxelisation.getVoxel(1, 0, k);
                assert (voxel.nhit == 0);
                assert (voxel.npulse == 0);
                assert (Util.equal(voxel.enteringBeamSection, 0));
            }
        } else {
            // assertion on voxel containing first echo
            voxel = voxelisation.getVoxel(1, 0, 4);
            assert (voxel.nhit == 1);
            assert (Util.equal(voxel.enteringBeamSection, 1.d));
            assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
            // assertion on voxel containing second echo
            voxel = voxelisation.getVoxel(1, 0, 2);
            assert (voxel.nhit == 1);
            assert (Util.equal(voxel.enteringBeamSection, 2.d / 3.d));
            assert (Util.equal(voxel.interceptedBeamSection, 1.d / 3.d));
            // assertion on following getVoxel (echo closer than second one 
            // so it is never found)
            for (int k = 1; k > 0; k--) {
                voxel = voxelisation.getVoxel(1, 0, k);
                assert (voxel.nhit == 0);
                assert (voxel.npulse == 1);
                assert (Util.equal(voxel.enteringBeamSection, 1.d / 3.d));
            }
        }
    }
}
