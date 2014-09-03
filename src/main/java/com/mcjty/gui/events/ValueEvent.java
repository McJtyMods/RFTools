package com.mcjty.gui.events;

import com.mcjty.gui.widgets.Widget;

public interface ValueEvent {
    void valueChanged(Widget parent, int newValue);
}
