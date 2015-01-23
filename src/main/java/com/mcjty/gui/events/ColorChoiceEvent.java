package com.mcjty.gui.events;

import com.mcjty.gui.widgets.Widget;

public interface ColorChoiceEvent {
    void choiceChanged(Widget parent, Integer newColor);
}
