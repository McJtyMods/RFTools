package mcjty.rftools;

import mcjty.rftools.blocks.powercell.PowerCellISBM;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandlers {

    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        Object object =  event.modelRegistry.getObject(PowerCellISBM.modelResourceLocation);
        if (object != null) {
            PowerCellISBM customModel = new PowerCellISBM();
            event.modelRegistry.putObject(PowerCellISBM.modelResourceLocation, customModel);
        }
    }

}
