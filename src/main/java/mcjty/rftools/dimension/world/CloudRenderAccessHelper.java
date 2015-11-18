package mcjty.rftools.dimension.world;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.renderer.RenderGlobal;

import java.lang.reflect.Field;

public class CloudRenderAccessHelper {
    private static Field cloudTickCounterField;

    private static void initCloudTickField() {
        if (cloudTickCounterField == null) {
            cloudTickCounterField = ReflectionHelper.findField(RenderGlobal.class, "field_72773_u", "cloudTickCounter");
        }
    }

    public static int getCloudTickCounter(RenderGlobal renderGlobal) {
        initCloudTickField();
        try {
            return cloudTickCounterField.getInt(renderGlobal);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
