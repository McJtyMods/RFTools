package mcjty.rftools.apideps;

import com.google.common.base.Function;
import mcjty.rftoolsdim.api.dimension.IDimensionManager;

import javax.annotation.Nullable;

/**
 * Check if a given dimension is an RFTools dimension
 */
public class RFToolsDimensionChecker {
    public static IDimensionManager dimensionManager;

    public static boolean isRFToolsDimension(int id) {
        return dimensionManager.isRFToolsDimension(id);
    }

    public static class GetDimensionManager implements Function<IDimensionManager, Void> {
        @Nullable
        @Override
        public Void apply(IDimensionManager manager) {
            dimensionManager = manager;
            return null;
        }
    }

}
