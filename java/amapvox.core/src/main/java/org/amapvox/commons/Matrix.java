package org.amapvox.commons;

import javax.vecmath.Matrix4d;
import org.jdom2.Element;

/**
 * Matrix object for AMAPVox XML configuration file.
 *
 * <p>
 * A matrix is a two-dimensional double array with fixed number of rows and
 * columns.
 *
 * XML representation of the matrix
 * {@code <matrix nrow=2 ncol=3 id="matrix_id">m00 m01 m02 ... </matrix>}.
 * Attributes {@code nrow} and {@code ncol} are optional. If they are not
 * provided the matrix is considered to be a square matrix. {@code id} attribute
 * is optional.
 *
 * @author Philippe VERLEY (philippe.verley@ird.fr)
 */
public class Matrix {

    // XML attribute names
    private final static String MATRIX = "matrix";
    private final static String NROW = "nrow";
    private final static String NCOL = "ncol";
    private final static String ID = "id";

    // matrix final variables
    private final int nrow;
    private final int ncol;
    private final double data[][];
    // id not final
    private String id;

    /**
     * Creates a new {@code Matrix} object holding the {@code double[][] data}
     * value.
     *
     * @param data a non null {@code double[][]} array
     */
    public Matrix(double[][] data) {

        // matrix cannot be null
        if (null == data) {
            throw new NullPointerException("matrix cannot be null");
        }
        this.data = data;
        this.nrow = data.length;
        int nc = data[0].length;
        for (double[] row : data) {
            // matrix row cannot be null
            if (null == row) {
                throw new NullPointerException("matrix row cannot be null");
            }
            // every row must have same number of columns
            if (nc != row.length) {
                throw new IllegalArgumentException("every matrix row must have length");
            }
            nc = row.length;
        }
        this.ncol = data[0].length;
    }

    public int getNRow() {
        return nrow;
    }

    public int getNCol() {
        return ncol;
    }

    public double[][] getData() {
        return data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns a nrow x ncol {@code Matrix} object represented by the argument
     * string {@code s}.
     *
     * <p>
     * If {@code s} is {@code null}, then a {@code NullPointerException} is
     * thrown.
     *
     * @param nrow the number of rows.
     * @param ncol the number of columns.
     * @param s the string to be parsed.
     * @return a nrow * ncol {@code Matrix} object.
     * @throws NumberFormatException if the string contains any number that are
     * not parsable.
     * @throws IllegalArgumentException if the matrix dimension is not nrow *
     * ncol.
     */
    static public Matrix valueOf(int nrow, int ncol, String s) throws NumberFormatException {

        double[][] mat = new double[nrow][ncol];

        String[] values = cleanup(s).split(" ");
        if (values.length != nrow * ncol) {
            throw new IllegalArgumentException("matrix expected " + nrow + "*" + ncol + " elements but found " + values.length);
        }
        int index = 0;
        for (int r = 0; r < nrow; r++) {
            for (int c = 0; c < ncol; c++) {
                mat[r][c] = Double.valueOf(values[index]);
                index++;
            }
        }

        return new Matrix(mat);
    }

    /**
     * Returns a square {@code Matrix} object represented by the argument string
     * {@code s}.
     *
     * <p>
     * If {@code s} is {@code null}, then a {@code NullPointerException} is
     * thrown.
     * </p>
     *
     * @param s the string to be parsed.
     * @return a square {@code Matrix} object.
     * @throws NumberFormatException if the string contains any number that are
     * not parsable.
     * @throws IllegalArgumentException if the matrix is not square.
     */
    static public Matrix valueOf(String s) throws NumberFormatException {

        if (null == s || s.isEmpty()) {
            throw new NullPointerException();
        }

        String ms = cleanup(s);

        int n = ms.split(" ").length;
        if ((int) Math.sqrt(n) != Math.sqrt(n)) {
            throw new IllegalArgumentException("not a square matrix, cannot parse it");
        }
        n = (int) Math.sqrt(n);

        return valueOf(n, n, ms);
    }

    /**
     * Creates a {@code Matrix} object represented by the XML element.
     *
     * <p>
     * XML matrix element is expected to be
     * {@code <matrix nrow=2 ncol=3 id="sop">m00 m01 m02 ... </matrix>}. If
     * attributes {@code nrow} and {@code ncol} are missing the matrix is parsed
     * as a square matrix.
     * </p>
     *
     * @param element the XML element representing the nrow x ncol matrix.
     * @return a {@code Matrix} object represented by the XML element.
     */
    static public Matrix valueOf(Element element) {

        if (!MATRIX.equals(element.getName())) {
            throw new IllegalArgumentException("not a matrix element");
        }

        int nrow = -1;
        if (null != element.getAttribute(NROW)) {
            nrow = Integer.valueOf(element.getAttributeValue(NROW));
        }

        int ncol = -1;
        if (null != element.getAttribute(NCOL)) {
            nrow = Integer.valueOf(element.getAttributeValue(NCOL));
        }

        String s = element.getText();

        Matrix matrix = (nrow == -1 || ncol == -1)
                ? Matrix.valueOf(s) // nrow and/or ncol attribute missing, assuming square matrix
                : Matrix.valueOf(nrow, ncol, s);

        // type_id
        if (null != element.getAttribute(ID)) {
            matrix.id = element.getAttributeValue(ID);
        }
        return matrix;
    }

    /**
     * Converts a {@code javax.vecmath.Matrix4d} into a {@code Matrix} object.
     *
     * @param mat4d the {@code javax.vecmath.Matrix4d} matrix.
     * @return the 4x4 {@code Matrix} object.
     */
    static public Matrix valueOf(Matrix4d mat4d) {

        double[][] data = new double[4][4];
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                data[r][c] = mat4d.getElement(r, c);
            }
        }

        return new Matrix(data);
    }

    /**
     * Returns a string made of double separated by space characters.
     *
     * @param s
     * @return
     */
    private static String cleanup(String s) {
        String cs = s;
        // replace line return by space
        cs = cs.replaceAll("\n", " ");
        // replace coma by space
        cs = cs.replaceAll(",", " ");
        // replace 2 or more spaces with single space
        return cs.trim().replaceAll(" +", " ");
    }

    /**
     * Transforms matrix into XML element.
     *
     * @return an XML representation of the matrix.
     */
    public Element toElement() {
        Element element = new Element(MATRIX);
        if (null != id && !id.isEmpty()) {
            element.setAttribute(ID, id);
        }
        element.setAttribute(NROW, String.valueOf(nrow));
        element.setAttribute(NCOL, String.valueOf(ncol));
        element.setText(this.toString());
        return element;
    }

    /**
     * Converts a 4x4 {@code Matrix} into a {@code javax.vecmath.Matrix4d}
     * object.
     *
     * @return a {@code javax.vecmath.Matrix4d} matrix.
     * @throws UnsupportedOperationException if matrix dimension is not 4x4.
     */
    public Matrix4d toMatrix4d() {

        if (nrow != 4 || ncol != 4) {
            throw new UnsupportedOperationException("only for 4x4 matrix");
        }

        Matrix4d mat = new Matrix4d();
        for (int r = 0; r < nrow; r++) {
            for (int c = 0; c < ncol; c++) {
                mat.setElement(r, c, data[r][c]);
            }
        }
        return mat;
    }

    /**
     * Returns a human readable representation of the matrix. One line per row
     * and column separated by space character.
     *
     * @return a human readable representation of the matrix.
     */
    public String toExternalString() {

        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < nrow - 1; r++) {
            for (int c = 0; c < ncol; c++) {
                sb.append(data[r][c]).append(" ");
            }
            sb.append("\n");
        }
        // last row
        for (int c = 0; c < ncol; c++) {
            sb.append(data[nrow - 1][c]).append(" ");
        }
        return sb.toString();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nrow; i++) {
            for (int j = 0; j < ncol; j++) {
                sb.append(data[i][j]).append(" ");
            }
        }
        return sb.toString().trim();
    }

}
