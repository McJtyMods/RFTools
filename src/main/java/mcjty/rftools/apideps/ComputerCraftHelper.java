package mcjty.rftools.apideps;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Optional.InterfaceList(@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft"))
public class ComputerCraftHelper implements IPeripheralProvider {

    public static void register() {
        ComputerCraftAPI.registerPeripheralProvider(new ComputerCraftHelper());
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public IPeripheral getPeripheral(World world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof GenericTileEntity && te instanceof IPeripheral) {
            return (IPeripheral) te;
        } else {
            return null;
        }
    }
}
