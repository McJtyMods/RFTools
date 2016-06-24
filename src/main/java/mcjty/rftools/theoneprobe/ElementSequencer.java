package mcjty.rftools.theoneprobe;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import net.minecraft.client.gui.Gui;

public class ElementSequencer implements IElement {

    private final long bits;

    public ElementSequencer(long bits) {
        this.bits = bits;
    }

    public ElementSequencer(ByteBuf buf) {
        bits = buf.readLong();
    }

    @Override
    public void render(int x, int y) {
        for (int row = 0 ; row < 8 ; row++) {
            for (int col = 0; col < 8; col++) {
                final int bit = row * 8 + col;
                Gui.drawRect(6+x+col*3, y+row*3, 6+x+col*3+2, y+row*3+2,
                    ((bits >> bit) & 1) == 1 ? 0xffffffff : 0xff000000);
            }
        }
    }

    @Override
    public int getWidth() {
        return 36;
    }

    @Override
    public int getHeight() {
        return 24;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(bits);
    }

    @Override
    public int getID() {
        return TheOneProbeSupport.ELEMENT_SEQUNCER;
    }
}
