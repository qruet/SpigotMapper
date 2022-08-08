package dev.qruet.decompiler.ui;

import dev.qruet.decompiler.Main;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import java.io.File;

public class UploadPanel extends Panel {

    private static final int DRAG_OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;

    private static final Color PANEL_BACKGROUND_COLOR = new Color(Display.getCurrent(), new RGB(43, 43, 43));
    private final Group bound;

    public UploadPanel(Shell shell) {
        super(shell);

        Main.getApplication().setResizable(false);

        shell.setBackground(PANEL_BACKGROUND_COLOR);

        this.bound = new Group(shell, SWT.SHADOW_NONE);
        this.bound.setLayout(new FillLayout());
        this.bound.setBackground(PANEL_BACKGROUND_COLOR);

        CLabel label = new CLabel(bound, SWT.CENTER | SWT.SHADOW_NONE);
        label.setFont(new Font(Display.getCurrent(), "Arial", 8, SWT.NORMAL));
        label.setBackground(PANEL_BACKGROUND_COLOR);
        label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        label.setTopMargin(shell.getBounds().height - 150);
        label.setText("DRAG AND DROP FILES HERE");

        DropTarget target = new DropTarget(bound, DRAG_OPERATIONS);

        Transfer[] types = new Transfer[]{FileTransfer.getInstance()};
        target.setTransfer(types);

        target.addDropListener(new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetEvent dropTargetEvent) {

            }

            @Override
            public void dragLeave(DropTargetEvent dropTargetEvent) {

            }

            @Override
            public void dragOperationChanged(DropTargetEvent dropTargetEvent) {

            }

            @Override
            public void dragOver(DropTargetEvent dropTargetEvent) {

            }

            @Override
            public void drop(DropTargetEvent event) {
                if (event.data instanceof String[] array) {
                    String path = array[0];
                    File file = new File(path);
                    System.out.println("Opening file " + file.getAbsolutePath());
                    Main.getApplication().setCurrentPanel(new JarPanel(shell, file));
                }
            }

            @Override
            public void dropAccept(DropTargetEvent dropTargetEvent) {
            }
        });
    }

    @Override
    public void onPost() {
    }

    @Override
    void close() {
        bound.dispose();
    }


}
