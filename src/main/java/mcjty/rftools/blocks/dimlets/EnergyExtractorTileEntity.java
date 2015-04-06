package mcjty.rftools.blocks.dimlets;

import cofh.api.energy.IEnergyConnection;
import mcjty.entity.GenericEnergyProviderTileEntity;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.varia.EnergyTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyExtractorTileEntity extends GenericEnergyProviderTileEntity {

    public EnergyExtractorTileEntity() {
        super(DimletConfiguration.EXTRACTOR_MAXENERGY, DimletConfiguration.EXTRACTOR_SENDPERTICK);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int energyStored = getEnergyStored(ForgeDirection.DOWN);

        if (energyStored < DimletConfiguration.EXTRACTOR_MAXENERGY) {
            // Get energy out of the dimension.
            DimensionStorage storage = DimensionStorage.getDimensionStorage(worldObj);
            int dimensionEnergy = storage.getEnergyLevel(worldObj.provider.dimensionId);
            int needed = DimletConfiguration.EXTRACTOR_MAXENERGY - energyStored;
            if (needed > dimensionEnergy) {
                needed = dimensionEnergy;
            }

            if (needed > 0) {
                energyStored += needed;
                dimensionEnergy -= needed;
                modifyEnergyStored(needed);

                storage.setEnergyLevel(worldObj.provider.dimensionId, dimensionEnergy);
                storage.save(worldObj);
            }
        }

        if (energyStored <= 0) {
            return;
        }

        int rf = DimletConfiguration.EXTRACTOR_SENDPERTICK;
        for (int i = 0 ; i < 6 ; i++) {
            ForgeDirection dir = ForgeDirection.getOrientation(i);
            TileEntity te = worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
            if (EnergyTools.isEnergyTE(te)) {
                IEnergyConnection connection = (IEnergyConnection) te;
                ForgeDirection opposite = dir.getOpposite();
                if (connection.canConnectEnergy(opposite)) {
                    int rfToGive;
                    if (rf <= energyStored) {
                        rfToGive = rf;
                    } else {
                        rfToGive = energyStored;
                    }

                    int received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                    energyStored -= extractEnergy(ForgeDirection.DOWN, received, false);
                    if (energyStored <= 0) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }
}
