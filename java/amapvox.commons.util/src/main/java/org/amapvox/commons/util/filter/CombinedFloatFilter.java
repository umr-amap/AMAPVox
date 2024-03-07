
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
package org.amapvox.commons.util.filter;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class CombinedFloatFilter implements Filter<Float> {

    public final static int AND = 0;
    public final static int OR = 1;

    protected final FloatFilter filter1;
    protected final FloatFilter filter2;
    private final int type;

    public CombinedFloatFilter(FloatFilter filter1, FloatFilter filter2, int type) {

        this.filter1 = filter1;
        this.filter2 = filter2;
        this.type = type;
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public boolean accept(Float attribut) {

        if (filter2 == null) {
            return filter1.accept(attribut);
        }

        switch (type) {
            case CombinedFloatFilter.AND:
                return filter1.accept(attribut) && filter2.accept(attribut);
            case CombinedFloatFilter.OR:
                return filter1.accept(attribut) || filter2.accept(attribut);
            default:
                return false;
        }
    }

    public FloatFilter getFilter1() {
        return filter1;
    }

    public FloatFilter getFilter2() {
        return filter2;
    }

    public int getType() {
        return type;
    }
}
