
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class StackedFloatFilters implements Filter<Float> {

    public List<CombinedFloatFilter> filters;

    public StackedFloatFilters() {
        filters = new ArrayList();
    }

    public void addFilter(CombinedFloatFilter filter) {
        filters.add(filter);
    }

    public void setFilters(Set<CombinedFloatFilter> newFilters) {

        if (newFilters != null) {
            filters.clear();
            filters.addAll(filters);
        }
    }

    @Override
    public void init() {
        // nothing to do
    }

    /**
     * Do filtering
     *
     * @param value
     * @return true if the value is filtered, false otherwise
     */
    @Override
    public boolean accept(Float value) {

        for (CombinedFloatFilter filter : filters) {
            if (filter.accept(value)) {
                return true;
            }
        }
        return false;
    }

}
