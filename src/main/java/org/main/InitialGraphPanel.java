package org.main;

import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import net.miginfocom.swing.MigLayout;

public class InitialGraphPanel extends JPanel {
    public enum GraphType {PRIMARY, SECONDARY}
    private ArrayList<Dataset> graphDatasets = new ArrayList<>();
    private JFrame popupFrame;
    private GraphType graphType;
    private Graph graph;
    private GroupLayout layout;
    private JLayeredPane container;
    private GLJPanel graphPanel;
    private JLabel datasetLabel;
    private JLabel graphLabel;

    private JCheckBox timeDomain;
    private JCheckBox dial;

    public InitialGraphPanel(JLayeredPane container, GLJPanel graphPanel, GraphType type) {
        super();

        this.container = container;
        this.graphPanel = graphPanel;
        this.graphType = type;

        datasetLabel = new JLabel("Datasets", SwingConstants.CENTER);
        graphLabel = new JLabel("Graph Type", SwingConstants.CENTER);
        datasetLabel.setOpaque(true);
        graphLabel.setOpaque(true);
        datasetLabel.setBackground(Color.white);
        graphLabel.setBackground(Color.white);

        String columnConstraints = 50 + "[]" + Theme.graphPadding + "[]" + 50;
        StringBuilder rowConstraints = new StringBuilder(Theme.graphPadding);
        for (int i = 0; i < DatasetController.getDatasets().size(); i++)
            rowConstraints.append("[]").append(Theme.graphPadding);

        setLayout(new MigLayout("fill", columnConstraints, rowConstraints.toString()));

        setBackground(Theme.canvasBackground);

        add(graphLabel, "cell 0 0, grow");
        add(datasetLabel, "cell 1 0, grow");

        if (graphType == GraphType.PRIMARY) {
            for (int i = 0; i < DatasetController.getDatasets().size(); i++) {
                Dataset dataset = DatasetController.getDataset(i);
                String label = dataset.getLabel().isEmpty() ? dataset.getName() : dataset.getLabel();
                JCheckBox jCheckBox = new JCheckBox(label);
                jCheckBox.addActionListener(event -> {
                    if (jCheckBox.isSelected()) graphDatasets.add(dataset);
                    else graphDatasets.remove(dataset);
                });
                int row = i + 1;
                add(jCheckBox, "cell 1 " + row + ", grow");
            }

            timeDomain = new JCheckBox("Time Domain");
            timeDomain.addActionListener(event -> graph = new OpenGLTimeDomain(0, 0, 0, 0));
            add(timeDomain, "cell 0 1, grow");

        } else if (graphType == GraphType.SECONDARY) {
            Dataset[] datasetArr = DatasetController.getDatasets().toArray(new Dataset[0]);
            JComboBox<Dataset> datasetComboBox = new JComboBox<>(datasetArr);
            graphDatasets.add((Dataset) datasetComboBox.getSelectedItem());
            datasetComboBox.addActionListener(event -> {
                graphDatasets.clear();
                graphDatasets.add((Dataset) datasetComboBox.getSelectedItem());
            });
            add(datasetComboBox, "cell 1 1, grow");

            dial = new JCheckBox("Dial");
            dial.addActionListener(event -> graph = new DialWithBuffer());
            add(dial, "cell 0 1, grow");
        }

        buildFrame();
    }

    public void confirmChanges(){
        if(graphType == GraphType.SECONDARY){
            ((SecondaryGraph) graph).setDataset(graphDatasets.get(0));
            graph.toggleAutoDetectMaxMin();
        }
        else {
            graph.setDatasets(graphDatasets);
            graph.toggleAutoDetectMaxMin();
        }

        graphPanel.addGLEventListener(graph);
        graphPanel.addMouseListener(graph);

        Animator animator = new Animator(graphPanel);
        animator.setUpdateFPSFrames(1, null);
        animator.start();

        graphPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        container.removeAll();
        container.add(graphPanel, Theme.GraphLayer);
        CanvasPanel.instance.setupCanvasLayout();

        popupFrame.dispose();
    }

    public void hideFrame(){
        popupFrame.dispose();
    }

    public void buildFrame(){
        popupFrame = new JFrame("Initialize Graph");
        BorderLayout layoutFrame = new BorderLayout();
        popupFrame.setLayout(layoutFrame);

        Dimension parentFrameDimension = CanvasPanel.instance.getParent().getSize();
        Dimension popupDimension = new Dimension((int) (parentFrameDimension.getWidth() / 2), (int) (parentFrameDimension.getHeight() / 2));
        popupFrame.setSize(popupDimension);

        Point parentFrameLocation = CanvasPanel.instance.getParent().getLocation();
        Point popupLocation = new Point((int) (parentFrameLocation.getX() + (parentFrameDimension.getWidth() / 2) - (popupDimension.getWidth() / 2)), (int) (parentFrameLocation.getY() + (parentFrameDimension.getHeight() / 2) - (popupDimension.getHeight() / 2)));

        popupFrame.setLocation(popupLocation);

        JScrollPane scrollPane = new JScrollPane(this);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        popupFrame.add(scrollPane, BorderLayout.CENTER);
        popupFrame.add(new InitialGraphToolbarPanel(this), BorderLayout.SOUTH);
        popupFrame.setVisible(true);
    }
}