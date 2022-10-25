package oriedita.editor.swing.dialog;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleDialog extends JDialog {
    private JPanel contentPane;
    private JTextPane console;
    private JScrollPane scroll;

    public ConsoleDialog() {
        super((JFrame) null, "Console");

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setAlwaysOnTop(false);

        setContentPane(contentPane);
        pack();

        StyleContext context = new StyleContext();
        Style blackForegroundStyle = context.addStyle("fg_black", context.getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setForeground(blackForegroundStyle, Color.black);

        Style redForegroundStyle = context.addStyle("fg_red", context.getStyle(StyleContext.DEFAULT_STYLE));
        StyleConstants.setForeground(redForegroundStyle, Color.red);

        PrintStream out = new PrintStream(new BufferedOutputStream(new TeeOutputStream(System.out, new TextPaneOutputStream(console, blackForegroundStyle))), true);
        PrintStream err = new PrintStream(new BufferedOutputStream(new TeeOutputStream(System.err, new TextPaneOutputStream(console, redForegroundStyle))), true);

        System.setOut(out);
        System.setErr(err);
    }

    private static class TeeOutputStream extends OutputStream {

        private final OutputStream out;
        private final OutputStream tee;

        public TeeOutputStream(OutputStream out, OutputStream tee) {
            if (out == null)
                throw new NullPointerException();
            else if (tee == null)
                throw new NullPointerException();

            this.out = out;
            this.tee = tee;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            tee.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
            tee.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            tee.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out.flush();
            tee.flush();
        }

        @Override
        public void close() throws IOException {
            try {
                out.close();
            } finally {
                tee.close();
            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setMinimumSize(new Dimension(300, 100));
        contentPane.setPreferredSize(new Dimension(700, 200));
        final JPanel spacer1 = new JPanel();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        contentPane.add(spacer4, gbc);
        scroll = new JScrollPane();
        scroll.setHorizontalScrollBarPolicy(31);
        scroll.setMinimumSize(new Dimension(300, 100));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(scroll, gbc);
        console = new JTextPane();
        console.setBackground(new Color(-1));
        console.setContentType("text/html");
        console.setEditable(false);
        scroll.setViewportView(console);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class TextPaneOutputStream extends OutputStream {
        private final Style style;
        private final JTextPane textPane;

        public TextPaneOutputStream(JTextPane textPane, Style style) {
            this.textPane = textPane;
            this.style = style;
        }

        public void append(String s) {
            StyledDocument doc = textPane.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), s, style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            textPane.select(doc.getLength(), doc.getLength());
        }

        @Override
        public void write(byte[] b, int off, int len) {
            append(new String(b, off, len));
        }

        @Override
        public void write(int b) {
            append(String.valueOf((char) b));
        }
    }
}
