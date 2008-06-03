/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.openstreetmap.josmng.osm.DataSet;
import org.openstreetmap.josmng.ui.actions.OpenAction;
import org.openstreetmap.josmng.ui.actions.OpenGpxAction;
import org.openstreetmap.josmng.ui.actions.ReverseWayAction;
import org.openstreetmap.josmng.ui.actions.SetProjectionAction;
import org.openstreetmap.josmng.ui.actions.UndoAction;
import org.openstreetmap.josmng.ui.mode.TheOnlyMode;
import org.openstreetmap.josmng.view.EditMode;
import org.openstreetmap.josmng.view.MapView;
import org.openstreetmap.josmng.view.Projection;
import org.openstreetmap.josmng.view.osm.OsmLayer;

/**
 *
 * @author  nenik
 */
public class Main extends javax.swing.JFrame {
    public static Main main;
    
    private ButtonGroup modeGroup = new ButtonGroup();
    
    /** Creates new form Main */
    public Main() {
        main = this;
        initComponents();
        initProjections();
        modesBar.add(createModeButton(new TheOnlyMode(mapView1)));
        
        addStatusComponent(new Position(mapView1));
        addStatusComponent(StatusBar.getDefault());

        setSize(800, 600);
    }
    
    private static final Border BORDER = 
        BorderFactory.createCompoundBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,UIManager.getDefaults().getColor("control")),   // NOI18N
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,1,1,UIManager.getDefaults().getColor("controlHighlight")),   // NOI18N
                BorderFactory.createLineBorder(UIManager.getDefaults().getColor("controlDkShadow"))   // NOI18N
            )
        ),
        BorderFactory.createEmptyBorder(0, 2, 0, 2)
        );

    
    private void addStatusComponent(JComponent comp) {
        comp.setBorder(BORDER);
        statusBar.add(comp);
    }
    
    private AbstractButton createModeButton(EditMode mode) {
        JToggleButton button = new JToggleButton(mode);
        button.setText("");
        button.putClientProperty("hideActionText", Boolean.TRUE);
//      String tooltip = (String)mode.getValue(Action.SHORT_DESCRIPTION);
//	button.setToolTipText(tooltip);

        modeGroup.add(button);
        if (modeGroup.getButtonCount() == 1) {
            button.getModel().setSelected(true);
            mode.actionPerformed(new ActionEvent(button, 0, ""));
        }
        return button;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mapView1 = new org.openstreetmap.josmng.view.MapView();
        toolBar = new javax.swing.JToolBar();
        modesBar = new javax.swing.JToolBar();
        statusBar = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        projection = new javax.swing.JMenu();
        editMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        reverse = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().add(mapView1, java.awt.BorderLayout.CENTER);

        toolBar.setRollover(true);
        getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

        modesBar.setFloatable(false);
        modesBar.setOrientation(1);
        modesBar.setRollover(true);
        getContentPane().add(modesBar, java.awt.BorderLayout.LINE_START);

        statusBar.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0, 0, 0)));
        statusBar.setLayout(new javax.swing.BoxLayout(statusBar, javax.swing.BoxLayout.LINE_AXIS));
        getContentPane().add(statusBar, java.awt.BorderLayout.PAGE_END);

        fileMenu.setText("File");

        jMenuItem1.setAction(new OpenAction());
        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        fileMenu.add(jMenuItem1);

        jMenuItem4.setAction(new OpenGpxAction());
        fileMenu.add(jMenuItem4);

        jMenuBar1.add(fileMenu);

        viewMenu.setText("View");

        projection.setText("Switch projection");
        viewMenu.add(projection);

        jMenuBar1.add(viewMenu);

        editMenu.setText("Edit");

        jMenuItem2.setAction(new UndoAction());
        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        editMenu.add(jMenuItem2);

        jMenuItem3.setText("Redo");
        editMenu.add(jMenuItem3);

        jMenuBar1.add(editMenu);

        toolsMenu.setText("Tools");

        reverse.setAction(new ReverseWayAction());
        toolsMenu.add(reverse);

        jMenuBar1.add(toolsMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public MapView getMapView() {
        return mapView1;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        java.awt.EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
        
        MapView view = Main.main.mapView1;
        if (args.length == 1 && new File(args[0]).exists()) {
            DataSet ds = DataSet.fromStream(new FileInputStream(args[0]));
            view.addLayer(new OsmLayer(view, args[0], ds));
        } else {
            view.addLayer(new OsmLayer(view, "new", DataSet.empty()));
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private org.openstreetmap.josmng.view.MapView mapView1;
    private javax.swing.JToolBar modesBar;
    private javax.swing.JMenu projection;
    private javax.swing.JMenuItem reverse;
    private javax.swing.JPanel statusBar;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

    private void initProjections() {
        ButtonGroup bg = new ButtonGroup();
        
        for (Projection proj : Projection.getAvailableProjections()) {
            JRadioButtonMenuItem jrb = new JRadioButtonMenuItem(new SetProjectionAction(proj));
            bg.add(jrb);
            if (proj.equals(mapView1.getProjection())) jrb.setSelected(true);
            projection.add(jrb);
        }
    }
    
}
