/**
 *
 */
package org.amapvox.commons.raytracing.voxel;

import javax.vecmath.Point3i;
import javax.vecmath.Point3d;

/**
 *
 * @author DAUZAT/Cresson sept.2012
 *
 */
public class VoxelSpace {
    
    private final Point3d min;
    private final Point3d max;
    private final Point3d size;
    private final Point3d voxelSize;
    private final Point3i dimension;
    private final boolean toric;
    private final boolean finite;
    
    public VoxelSpace(Point3d min, Point3d max, Point3i dimension, double margin, VoxelManager.Topology topology) {
        
        this.min = min;
        this.max = max;
        if (margin != 0.d) {
            this.min.sub(new Point3d(margin, margin, margin));
            this.max.add(new Point3d(margin, margin, margin));
        }
        this.size = new Point3d(this.max);
        this.size.sub(this.min);
        this.dimension = dimension;
        this.voxelSize = new Point3d(
                size.x / dimension.x,
                size.y / dimension.y,
                size.z / dimension.z);
        switch (topology) {
            default:
            case NON_TORIC_FINITE_BOX_TOPOLOGY:
                toric = false;
                finite = true;
                break;
            case TORIC_FINITE_BOX_TOPOLOGY:
                toric = true;
                finite = false;
                break;
            case TORIC_INFINITE_BOX_TOPOLOGY:
                toric = true;
                finite = false;
                break;
        }
    }
    
    public VoxelSpace(Point3d min, Point3d max, Point3i dimension, VoxelManager.Topology topology) {
        this(min, max, dimension, 0.d, topology);
    }
    
    public VoxelSpace(Point3d min, Point3d max, Point3i dimension) {
        this(min, max, dimension, 0.d, VoxelManager.Topology.NON_TORIC_FINITE_BOX_TOPOLOGY);
    }

    /**
     * Returns whether a voxel is on edge of the voxel space.
     *
     * @param index, index of the voxel.
     * @return true if the voxel is on edge of the voxel space.
     */
    public boolean isOnEdge(Point3i index) {
        return index.x == 0 || index.y == 0 || index.z == 0
                || index.x == dimension.x - 1
                || index.y == dimension.y - 1
                || index.z == dimension.z - 1;
    }

    /**
     * @param coordinates 3d point coordinates
     * @return the (i, j, k) index of the voxel including the point under the
     * Point3i format or null if the point is outside the voxel space except if
     * the voxel space is toric. In such case the returned index is modulo the
     * dimension along each coordinate.
     */
    public Point3i getVoxelIndex(Point3d coordinates) {

        // shift to scene Min
        Point3d pt = new Point3d(coordinates);
        pt.sub(min);
        
        if ((pt.z < 0) || (pt.z >= size.z)) {
            
            return null;

            //System.out.println("deltaZ:"+Math.abs(pt.z-boundingBoxSize.z));
        }
        
        if (toric == false) {
            if ((pt.x < 0) || (pt.x >= size.x)) {
                
                return null;
                //System.out.println("deltaC:"+Math.abs(pt.x-boundingBoxSize.x));

            }
            if ((pt.y < 0) || (pt.y >= size.y)) {
                
                return null;
                //System.out.println("deltaY:"+Math.abs(pt.y-boundingBoxSize.y));

            }
        }
        pt.x /= voxelSize.x;
        pt.y /= voxelSize.y;
        pt.z /= voxelSize.z;
        
        Point3i index = new Point3i();

        // voxel indexes
        index.x = (int) Math.floor((double) (pt.x % dimension.x));
        if (index.x < 0) {
            index.x += dimension.x;
        }
        index.y = (int) Math.floor((double) (pt.y % dimension.y));
        if (index.y < 0) {
            index.y += dimension.y;
        }
        index.z = (int) Math.min(pt.z, dimension.z - 1);
        
        return index;
        
    }
    
    public boolean contains(Point3d coordinates) {
        
        return (coordinates.x >= min.x && coordinates.x <= max.x
                && coordinates.y >= min.y && coordinates.y <= max.y
                && coordinates.z >= min.z && coordinates.z <= max.z);
    }

    /**
     * @param index (i, j, k) voxel index
     * @return the coordinates (x, y, z) of the inf corner of the specified
     * voxel
     */
    public Point3d getVoxelInfCorner(Point3i index) {
        Point3d corner = new Point3d(index.x * voxelSize.x, index.y * voxelSize.y, index.z * voxelSize.z);
        corner.add(min);
        return corner;
    }

    /**
     * @param index	specified voxel
     * @return the sup corner of the specified voxel
     */
    public Point3d getVoxelSupCorner(Point3i index) {
        Point3d corner = new Point3d((index.x + 1) * voxelSize.x, (index.y + 1) * voxelSize.y, (index.z + 1) * voxelSize.z);
        corner.add(min);
        return corner;
    }

    /**
     * Get the splitting of the voxel space
     *
     * @return splitting of the voxel space
     */
    public Point3i getDimension() {
        return dimension;
    }

    /**
     * Get the toricity of the voxel space
     *
     * @return toricity
     */
    public boolean isToric() {
        return toric;
    }

    /**
     * Returns if the voxelspace is finite
     *
     * @return finite
     */
    public boolean isFinite() {
        return finite;
    }

    /**
     * Get bounding box size
     *
     * @return bounding box size
     */
    public Point3d getSize() {
        return size;
    }
    
    public Point3d getMin() {
        return min;
    }
    
    public Point3d getMax() {
        return max;
    }

    /**
     * Get voxel size.F
     *
     * @return voxel size
     */
    public Point3d getVoxelSize() {
        return voxelSize;
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("VOXELSPACE").append('\n');
        sb.append("  min coordinates\t").append(min).append('\n');
        sb.append("  max coordinates\t").append(max).append('\n');
        sb.append("  dimension\t").append(dimension).append('\n');
        sb.append("  size\t").append(size).append('\n');
        sb.append("  voxel size\t").append(voxelSize).append('\n');
        return sb.toString();
    }
}
