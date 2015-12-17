package mcjty.rftools.blocks.generator;


import mcjty.lib.entity.GenericEnergyProviderTileEntity;
import net.minecraft.util.ITickable;

public class CoalGeneratorTileEntity extends GenericEnergyProviderTileEntity implements ITickable {

    public CoalGeneratorTileEntity() {
        super(1000000, 2000);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {

        }
    }
}
