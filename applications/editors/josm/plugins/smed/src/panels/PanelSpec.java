package panels;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import messages.Messages;
import seamarks.SeaMark;
import seamarks.SeaMark.Cat;
import seamarks.SeaMark.Col;
import seamarks.SeaMark.Ent;
import seamarks.SeaMark.Obj;
import seamarks.SeaMark.Pat;
import seamarks.SeaMark.Shp;
import seamarks.SeaMark.Top;
import smed.SmedAction;

public class PanelSpec extends JPanel {

    private SmedAction dlg;
    public JLabel categoryLabel;
    public JComboBox<String> categoryBox;
    public EnumMap<Cat, Integer> categories = new EnumMap<>(Cat.class);
    private ActionListener alCategoryBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : categories.keySet()) {
                int idx = categories.get(cat);
                if (dlg.node != null && (idx == categoryBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                }
            }
        }
    };
    public JComboBox<String> mooringBox;
    public EnumMap<Cat, Integer> moorings = new EnumMap<>(Cat.class);
    private ActionListener alMooringBox = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (Cat cat : moorings.keySet()) {
                int idx = moorings.get(cat);
                if (dlg.node != null && (idx == mooringBox.getSelectedIndex())) {
                    SmedAction.panelMain.mark.setCategory(cat);
                    if ((cat == Cat.INB_CALM) || (cat == Cat.INB_SBM)) {
                        SmedAction.panelMain.mark.setObject(Obj.BOYINB);
                        SmedAction.panelMain.mark.setShape(Shp.UNKSHP);
                    } else {
                        SmedAction.panelMain.mark.setObject(Obj.MORFAC);
                        if (cat != Cat.MOR_BUOY) {
                            SmedAction.panelMain.mark.setShape(Shp.UNKSHP);
                        }
                    }
                }
            }
            if (dlg.node != null) {
                syncPanel();
            }
        }
    };
    public ButtonGroup shapeButtons = new ButtonGroup();
    public JRadioButton pillarButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/PillarButton.png")));
    public JRadioButton sparButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SparButton.png")));
    public JRadioButton canButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CanButton.png")));
    public JRadioButton coneButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/ConeButton.png")));
    public JRadioButton sphereButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SphereButton.png")));
    public JRadioButton barrelButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BarrelButton.png")));
    public JRadioButton superButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/SuperButton.png")));
    public JRadioButton floatButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/FloatButton.png")));
    public JRadioButton beaconButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/BeaconButton.png")));
    public JRadioButton towerButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/TowerButton.png")));
    public JRadioButton stakeButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/StakeButton.png")));
    public JRadioButton cairnButton = new JRadioButton(new ImageIcon(getClass().getResource("/images/CairnButton.png")));
    public EnumMap<Shp, JRadioButton> shapes = new EnumMap<>(Shp.class);
    public EnumMap<Shp, Obj> objects = new EnumMap<>(Shp.class);
    public ActionListener alShape = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if ((SmedAction.panelMain.mark.getObject() != Obj.MORFAC) || (SmedAction.panelMain.mark.getCategory() == Cat.MOR_BUOY)) {
                for (Shp shp : shapes.keySet()) {
                    JRadioButton button = shapes.get(shp);
                    if (button.isSelected()) {
                        SmedAction.panelMain.mark.setShape(shp);
                        if (SeaMark.EntMAP.get(SmedAction.panelMain.mark.getObject()) != Ent.MOORING) {
                            SmedAction.panelMain.mark.setObject(objects.get(shp));
                            if (SmedAction.panelMain.mark.getObjColour(0) == Col.UNKCOL) {
                                SmedAction.panelMain.mark.setObjPattern(Pat.NOPAT);
                                SmedAction.panelMain.mark.setObjColour(Col.YELLOW);
                            }
                            if (button == cairnButton) {
                                SmedAction.panelMain.mark.setObjPattern(Pat.NOPAT);
                                SmedAction.panelMain.mark.setObjColour(Col.UNKCOL);
                            }
                            topmarkButton.setVisible(SmedAction.panelMain.mark.testValid());
                        }
                        button.setBorderPainted(true);
                    } else {
                        button.setBorderPainted(false);
                    }
                }
                SmedAction.panelMain.panelMore.syncPanel();
            }
        }
    };
    public JToggleButton topmarkButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/SpecTopButton.png")));
    private ActionListener alTop = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (topmarkButton.isSelected()) {
                SmedAction.panelMain.mark.setTopmark(Top.X_SHAPE);
                SmedAction.panelMain.mark.setTopPattern(Pat.NOPAT);
                SmedAction.panelMain.mark.setTopColour(Col.YELLOW);
                topmarkButton.setBorderPainted(true);
            } else {
                SmedAction.panelMain.mark.setTopmark(Top.NOTOP);
                SmedAction.panelMain.mark.setTopPattern(Pat.NOPAT);
                SmedAction.panelMain.mark.setTopColour(Col.UNKCOL);
                topmarkButton.setBorderPainted(false);
            }
            SmedAction.panelMain.panelTop.syncPanel();
        }
    };
    public JToggleButton noticeButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/NoticeButton.png")));
    private ActionListener alNotice = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            SmedAction.panelMain.mark.clrMark();
            if (noticeButton.isSelected()) {
                SmedAction.panelMain.mark.setObject(Obj.NOTMRK);
                noticeButton.setBorderPainted(true);
            } else {
                SmedAction.panelMain.mark.setObject(Obj.UNKOBJ);
                noticeButton.setBorderPainted(false);
            }
            SmedAction.panelMain.syncPanel();
        }
    };
    public JToggleButton mooringButton = new JToggleButton(new ImageIcon(getClass().getResource("/images/MooringButton.png")));
    private ActionListener alMooring = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            SmedAction.panelMain.mark.setObject(Obj.UNKOBJ);
            SmedAction.panelMain.mark.setCategory(Cat.NOCAT);
            SmedAction.panelMain.mark.setTopmark(Top.NOTOP);
            if (mooringButton.isSelected()) {
                SmedAction.panelMain.mark.setObject(Obj.MORFAC);
                categoryBox.setVisible(false);
                mooringBox.setVisible(true);
                pillarButton.setEnabled(false);
                sparButton.setEnabled(false);
                beaconButton.setEnabled(false);
                towerButton.setEnabled(false);
                stakeButton.setEnabled(false);
                cairnButton.setEnabled(false);
                mooringButton.setBorderPainted(true);
            } else {
                mooringBox.setVisible(false);
                categoryBox.setVisible(true);
                pillarButton.setEnabled(true);
                sparButton.setEnabled(true);
                beaconButton.setEnabled(true);
                towerButton.setEnabled(true);
                stakeButton.setEnabled(true);
                cairnButton.setEnabled(true);
                mooringButton.setBorderPainted(false);
            }
            syncPanel();
        }
    };

    public PanelSpec(SmedAction dia) {
        dlg = dia;
        setLayout(null);
        add(getShapeButton(pillarButton, 0, 0, 34, 32, "Pillar", Shp.PILLAR, Obj.BOYSPP));
        add(getShapeButton(sparButton, 34, 0, 34, 32, "Spar", Shp.SPAR, Obj.BOYSPP));
        add(getShapeButton(canButton, 68, 0, 34, 32, "Can", Shp.CAN, Obj.BOYSPP));
        add(getShapeButton(coneButton, 102, 0, 34, 32, "Cone", Shp.CONI, Obj.BOYSPP));
        add(getShapeButton(sphereButton, 0, 32, 34, 32, "Sphere", Shp.SPHERI, Obj.BOYSPP));
        add(getShapeButton(barrelButton, 34, 32, 34, 32, "Barrel", Shp.BARREL, Obj.BOYSPP));
        add(getShapeButton(superButton, 68, 32, 34, 32, "Super", Shp.SUPER, Obj.BOYSPP));
        add(getShapeButton(floatButton, 102, 32, 34, 32, "Float", Shp.FLOAT, Obj.LITFLT));
        add(getShapeButton(beaconButton, 0, 64, 34, 32, "Beacon", Shp.BEACON, Obj.BCNSPP));
        add(getShapeButton(towerButton, 34, 64, 34, 32, "TowerB", Shp.TOWER, Obj.BCNSPP));
        add(getShapeButton(stakeButton, 68, 64, 34, 32, "Stake", Shp.STAKE, Obj.BCNSPP));
        add(getShapeButton(cairnButton, 102, 64, 34, 32, "CairnB", Shp.CAIRN, Obj.BCNSPP));

        categoryLabel = new JLabel(Messages.getString("Category"), SwingConstants.CENTER);
        categoryLabel.setBounds(new Rectangle(5, 125, 160, 18));
        add(categoryLabel);
        categoryBox = new JComboBox<>();
        categoryBox.setBounds(new Rectangle(5, 142, 160, 18));
        add(categoryBox);
        categoryBox.setVisible(true);
        categoryBox.addActionListener(alCategoryBox);
        addCatItem("", Cat.NOCAT);
        addCatItem(Messages.getString("UKPurpose"), Cat.SPM_UNKN);
        addCatItem(Messages.getString("Warning"), Cat.SPM_WARN);
        addCatItem(Messages.getString("ChanSeparation"), Cat.SPM_CHBF);
        addCatItem(Messages.getString("Yachting"), Cat.SPM_YCHT);
        addCatItem(Messages.getString("Cable"), Cat.SPM_CABL);
        addCatItem(Messages.getString("Outfall"), Cat.SPM_OFAL);
        addCatItem(Messages.getString("ODAS"), Cat.SPM_ODAS);
        addCatItem(Messages.getString("RecreationZone"), Cat.SPM_RECN);
        addCatItem(Messages.getString("Mooring"), Cat.SPM_MOOR);
        addCatItem(Messages.getString("LANBY"), Cat.SPM_LNBY);
        addCatItem(Messages.getString("Leading"), Cat.SPM_LDNG);
        addCatItem(Messages.getString("Notice"), Cat.SPM_NOTC);
        addCatItem(Messages.getString("TSS"), Cat.SPM_TSS);
        addCatItem(Messages.getString("FoulGround"), Cat.SPM_FOUL);
        addCatItem(Messages.getString("Diving"), Cat.SPM_DIVE);
        addCatItem(Messages.getString("FerryCross"), Cat.SPM_FRRY);
        addCatItem(Messages.getString("Anchorage"), Cat.SPM_ANCH);
        mooringBox = new JComboBox<>();
        mooringBox.setBounds(new Rectangle(5, 142, 160, 18));
        add(mooringBox);
        mooringBox.setVisible(false);
        mooringBox.addActionListener(alMooringBox);
        addMorItem("", Cat.NOCAT);
        addMorItem(Messages.getString("Dolphin"), Cat.MOR_DLPN);
        addMorItem(Messages.getString("DevDolphin"), Cat.MOR_DDPN);
        addMorItem(Messages.getString("Bollard"), Cat.MOR_BLRD);
        addMorItem(Messages.getString("Wall"), Cat.MOR_WALL);
        addMorItem(Messages.getString("Post"), Cat.MOR_POST);
        addMorItem(Messages.getString("Chain"), Cat.MOR_CHWR);
        addMorItem(Messages.getString("Rope"), Cat.MOR_ROPE);
        addMorItem(Messages.getString("Automatic"), Cat.MOR_AUTO);
        addMorItem(Messages.getString("MooringBuoy"), Cat.MOR_BUOY);
        addMorItem(Messages.getString("CALM"), Cat.INB_CALM);
        addMorItem(Messages.getString("SBM"), Cat.INB_SBM);

        topmarkButton.setBounds(new Rectangle(136, 0, 34, 32));
        topmarkButton.setToolTipText(Messages.getString("Topmark"));
        topmarkButton.setBorder(BorderFactory.createLoweredBevelBorder());
        topmarkButton.addActionListener(alTop);
        add(topmarkButton);

        //        noticeButton.setBounds(new Rectangle(136, 32, 34, 32));
        //        noticeButton.setToolTipText(Messages.getString("Notice"));
        //        noticeButton.setBorder(BorderFactory.createLoweredBevelBorder());
        //        noticeButton.addActionListener(alNotice);
        //        add(noticeButton);

        mooringButton.setBounds(new Rectangle(136, 64, 34, 32));
        mooringButton.setToolTipText(Messages.getString("Mooring"));
        mooringButton.setBorder(BorderFactory.createLoweredBevelBorder());
        mooringButton.addActionListener(alMooring);
        add(mooringButton);
    }

    public void syncPanel() {
        if (SeaMark.EntMAP.get(SmedAction.panelMain.mark.getObject()) == Ent.MOORING) {
            mooringButton.setBorderPainted(true);
            categoryBox.setVisible(false);
            mooringBox.setVisible(true);
            pillarButton.setEnabled(false);
            sparButton.setEnabled(false);
            beaconButton.setEnabled(false);
            towerButton.setEnabled(false);
            stakeButton.setEnabled(false);
            cairnButton.setEnabled(false);
            noticeButton.setEnabled(false);
            topmarkButton.setVisible(false);
            for (Cat cat : moorings.keySet()) {
                int item = moorings.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    mooringBox.setSelectedIndex(item);
                }
            }
        } else {
            mooringButton.setBorderPainted(false);
            mooringBox.setVisible(false);
            categoryBox.setVisible(true);
            pillarButton.setEnabled(true);
            sparButton.setEnabled(true);
            beaconButton.setEnabled(true);
            towerButton.setEnabled(true);
            stakeButton.setEnabled(true);
            cairnButton.setEnabled(true);
            noticeButton.setEnabled(true);
            topmarkButton.setBorderPainted(SmedAction.panelMain.mark.getTopmark() != Top.NOTOP);
            topmarkButton.setSelected(SmedAction.panelMain.mark.getTopmark() != Top.NOTOP);
            topmarkButton.setVisible(SmedAction.panelMain.mark.testValid());
            for (Cat cat : categories.keySet()) {
                int item = categories.get(cat);
                if (SmedAction.panelMain.mark.getCategory() == cat) {
                    categoryBox.setSelectedIndex(item);
                }
            }
        }
        for (Shp shp : shapes.keySet()) {
            JRadioButton button = shapes.get(shp);
            if (SmedAction.panelMain.mark.getShape() == shp) {
                button.setBorderPainted(true);
            } else {
                button.setBorderPainted(false);
            }
        }
        noticeButton.setBorderPainted(false);
        SmedAction.panelMain.mark.testValid();
    }

    private void addCatItem(String str, Cat cat) {
        categories.put(cat, categoryBox.getItemCount());
        categoryBox.addItem(str);
    }

    private void addMorItem(String str, Cat cat) {
        moorings.put(cat, mooringBox.getItemCount());
        mooringBox.addItem(str);
    }

    private JRadioButton getShapeButton(JRadioButton button, int x, int y, int w, int h, String tip, Shp shp, Obj obj) {
        button.setBounds(new Rectangle(x, y, w, h));
        button.setBorder(BorderFactory.createLoweredBevelBorder());
        button.setToolTipText(Messages.getString(tip));
        button.addActionListener(alShape);
        shapeButtons.add(button);
        shapes.put(shp, button);
        objects.put(shp, obj);
        return button;
    }

}
