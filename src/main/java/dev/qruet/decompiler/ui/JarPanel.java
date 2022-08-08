package dev.qruet.decompiler.ui;

import dev.qruet.decompiler.Main;
import dev.qruet.decompiler.java.io.Class;
import dev.qruet.decompiler.java.io.Jar;
import dev.qruet.decompiler.java.io.Package;
import dev.qruet.decompiler.ui.util.JavaStyler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;

public class JarPanel extends Panel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 1000;
    private static final FontData PACKAGE_FONT_DATA = new FontData("Arial", 9, SWT.BOLD);
    private static final FontData CLASS_FONT_DATA = new FontData("Arial", 9, SWT.NORMAL);
    private static final Color PANEL_BACKGROUND_COLOR = new Color(Display.getCurrent(), new RGB(43, 43, 43));
    private static final Color LINE_ALTERNATE_COLOR = new Color(Display.getCurrent(), new RGB(55, 55, 55));
    private final Font packageFont;
    private final Font classFont;

    private final Group treeGroup;
    private final Group textGroup;

    public JarPanel(Shell parent, File file) {
        super(parent, PANEL_WIDTH, PANEL_HEIGHT);

        parent.setText("Decompiled  " + file.getAbsolutePath());

        Main.getApplication().setResizable(true);

        parent.setLayout(new GridLayout(2, false));
        parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

        this.treeGroup = new Group(parent, SWT.NONE);
        this.treeGroup.setLayout(new FillLayout());
        this.treeGroup.setBackground(PANEL_BACKGROUND_COLOR);

        this.textGroup = new Group(parent, SWT.NONE);
        this.textGroup.setLayout(new FillLayout());
        this.textGroup.setBackground(PANEL_BACKGROUND_COLOR);

        final GridData treeData = new GridData(SWT.FILL, SWT.FILL, true, true);
        final GridData bodyData = new GridData(SWT.FILL, SWT.FILL, true, true);

        treeGroup.setLayoutData(treeData);
        textGroup.setLayoutData(bodyData);

        StyledText text = new StyledText(textGroup, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        text.setWrapIndent(60);
        text.setAlwaysShowScrollBars(false);
        text.setFont(new Font(textGroup.getDisplay(), "Arial", 10, SWT.NORMAL));
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setCursor(null);
        text.setEditable(false);
        text.setBackground(PANEL_BACKGROUND_COLOR);
        text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
        text.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
        text.addLineStyleListener(new JavaStyler());
        text.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent paintEvent) {
                for(int i = 1; i < text.getLineCount(); i+=2) {
                    text.setLineBackground(i, 1, LINE_ALTERNATE_COLOR);
                }
             }
        });
        text.addLineStyleListener(event -> {
            StyleRange styleRange = new StyleRange();
            styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
            int maxLine = text.getLineCount();
            int bulletLength = Integer.toString(maxLine).length();
            int bulletWidth = (bulletLength + 1) * text.getLineHeight() / 2;
            styleRange.metrics = new GlyphMetrics(0, 0, bulletWidth);
            event.bullet = new Bullet(ST.BULLET_TEXT, styleRange);
            int bulletLine = text.getLineAtOffset(event.lineOffset) + 1;
            event.bullet.text = String.format("%" + bulletLength + "s", bulletLine);
        });

        this.packageFont = new Font(parent.getDisplay(), PACKAGE_FONT_DATA.getName(), PACKAGE_FONT_DATA.getHeight(), PACKAGE_FONT_DATA.getStyle());
        this.classFont = new Font(parent.getDisplay(), CLASS_FONT_DATA.getName(), CLASS_FONT_DATA.getHeight(), CLASS_FONT_DATA.getStyle());

        Jar jar = Jar.getJarFromFile(file);

        //FlatScrollBarTree scroll = new FlatScrollBarTree(treeGroup, (adapter) -> {
            final Tree tree =  new Tree(treeGroup/*adapter*/, SWT.NONE);
            tree.setBackground(PANEL_BACKGROUND_COLOR);
            tree.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
            tree.addListener(SWT.Selection, event -> {
                if(event.item == null)
                    return;
                Object val = event.item.getData("path");
                if(val == null)
                    return;

                Class clazz = jar.getClass(val.toString());
                if(clazz == null) {
                    System.err.println("Failed to load class, " + val.toString() + ".");
                } else {
                    text.setText(clazz.decompile());
                }
            });

            for (Package p : jar.getSubpackagesFromPath("")) {
                TreeItem p_item = new TreeItem(tree, 0);
                p_item.setText("📁 " + p.getPath());
                p_item.setFont(packageFont);
                buildSubtree(p_item, p);
            }

            //return tree;
        //});

        //scroll.setThumbColor(new Color(Display.getCurrent(), new RGB(255, 255, 255)));
        //scroll.setPageIncrementColor(new Color(Display.getCurrent(), new RGBA(40, 40, 40, 100)));
    }

    private void buildSubtree(TreeItem root, Package pack) {
        for (Package sub : pack.getSubpackages()) {
            TreeItem parent = root;
            if (!sub.getClasses().isEmpty()) {
                TreeItem p_item = new TreeItem(root, 0);
                p_item.setText("📁 " + sub.getPath());
                p_item.setFont(packageFont);
                parent = p_item;
            }
            buildSubtree(parent, sub);
        }

        Collection<Class> classes = pack.getClasses();
        for (Class clazz : classes) {
            TreeItem item = new TreeItem(root, 0);
            item.setText(clazz.getName());
            item.setFont(classFont);
            item.setData("path", clazz.getPath());
            Collection<Class> subclasses = clazz.getSubclasses();
            for (Class sub : subclasses) {
                TreeItem s_item = new TreeItem(item, 0);
                s_item.setText(sub.getName());
                s_item.setFont(classFont);
                s_item.setData("path", clazz.getPath());
            }
        }
    }

    @Override
    protected void onPost() {
        Point size = shell.getSize();
        ((GridData) treeGroup.getLayoutData()).widthHint = (int) (size.x * 0.35);
        ((GridData) textGroup.getLayoutData()).widthHint = size.x - ((GridData) treeGroup.getLayoutData()).widthHint;
    }

    @Override
    void close() {

    }
}
