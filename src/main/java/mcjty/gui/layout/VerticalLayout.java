package mcjty.gui.layout;

import mcjty.gui.widgets.Widget;

import java.util.Collection;

public class VerticalLayout extends AbstractLayout<VerticalLayout> {
    @Override
    public void doLayout(Collection<Widget> children, int width, int height) {
        int otherHeight = calculateDynamicSize(children, height, Widget.Dimension.DIMENSION_HEIGHT);

        int top = getVerticalMargin();
        for (Widget child : children) {
            int h = child.getDesiredHeight();
            if (h == Widget.SIZE_UNKNOWN) {
                h = otherHeight;
            }
            child.setBounds(align(getHorizontalMargin(), top, width-getHorizontalMargin()*2, h, child));
            top += h;
            top += getSpacing();
        }
    }
}
