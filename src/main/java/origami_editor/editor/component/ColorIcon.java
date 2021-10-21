package origami_editor.editor.component;

import origami_editor.editor.Colors;

import javax.swing.*;
import java.awt.*;

public class ColorIcon implements Icon {
    private final Color color;

    public ColorIcon(Color color) {
        this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
        g.setColor(Colors.get(Color.black));
        g.drawRect(x, y, getIconWidth(), getIconHeight());
    }

    @Override
    public int getIconWidth() {
        return 16;
    }

    @Override
    public int getIconHeight() {
        return 16;
    }
}
