package dev.qruet.decompiler.ui;

import org.eclipse.swt.widgets.Shell;

public abstract class Panel {

    protected final Shell shell;
    private int new_width, new_height;

    protected Panel(Shell shell) {
        this.shell = shell;
    }

    protected Panel(Shell shell, int width, int height) {
        this(shell);

        this.new_width = width;
        this.new_height = height;
    }

    /**
     * Called after the panel is created or is updated (i.e. scaled)
     * See {@link Application#setCurrentPanel(Panel)} and {@link Application#launch()}
     */
    protected final void postDraw() {
        if (new_width > 0 || new_height > 0) {
            this.shell.setBounds(shell.getBounds().x, shell.getBounds().y,
                    new_width > 0 ? new_width : this.shell.getBounds().width,
                    new_height > 0 ? new_height : this.shell.getBounds().height);
            new_width = 0;
            new_height = 0;
        }
        onPost();
    }

    /**
     * Called after the panel is created or is updated (i.e. scaled)
     * See {@link Panel#postDraw()}
     */
    protected void onPost() {
        // TODO Implement me (optional)
    }

    abstract void close();

    protected void redraw() {
        shell.layout(true, true);
    }

}
