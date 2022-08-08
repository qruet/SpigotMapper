package dev.qruet.decompiler.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Application {

    private final Shell shell;
    private Panel currentPanel;

    private static final int DEFAULT_WIDTH = 600;
    private static final int DEFAULT_HEIGHT = 300;

    private static final int WS_SIZEBOX = 0x00040000;

    public Application() {
        this.shell = new Shell(new Display());
        this.shell.setLayout(new FillLayout());
        this.shell.setBounds(shell.getBounds().x, shell.getBounds().y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void launch() {
        shell.addListener(SWT.Resize, arg0 -> {
            if(currentPanel != null)
                currentPanel.postDraw();
        });

        setCurrentPanel(new UploadPanel(shell));

        this.shell.open();

        OS.AllowDarkModeForWindow(shell.handle, true);

        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();

                OS.SetWindowPos(shell.handle, OS.HWND_TOPMOST, shell.getLocation().x, shell.getLocation().y, shell.getSize().x, shell.getSize().y, 0);

            }
        }
        shell.dispose();
    }

    public void setResizable(boolean resizable) {
        int current = OS.GetWindowLong(shell.handle, OS.GWL_STYLE);

        final int newStyle;
        if(resizable)
            newStyle = current | WS_SIZEBOX;
        else
            newStyle = current & ~WS_SIZEBOX;

        OS.SetWindowLong(shell.handle, OS.GWL_STYLE, newStyle);
    }

    public void setCurrentPanel(Panel panel) {
        if(this.currentPanel != null)
            currentPanel.close();
        this.currentPanel = panel;
        this.currentPanel.postDraw();
        shell.layout(true, true);
    }

}
