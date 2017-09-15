package mcjty.rftools.items.builder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BlockPosState extends BlockPos {
    private final IBlockState state;

    public BlockPosState(int x, int y, int z) {
        super(x, y, z);
        state = null;
    }

    public BlockPosState(int x, int y, int z, IBlockState state) {
        super(x, y, z);
        this.state = state;
    }

    public BlockPosState(double x, double y, double z) {
        super(x, y, z);
        state = null;
    }

    public BlockPosState(Entity source) {
        super(source);
        state = null;
    }

    public BlockPosState(Vec3d vec) {
        super(vec);
        state = null;
    }

    public BlockPosState(Vec3i source) {
        super(source);
        state = null;
    }

    public IBlockState getState() {
        return state;
    }
}
