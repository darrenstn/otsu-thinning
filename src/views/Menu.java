/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package views;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import controller.Controller;
import java.text.DecimalFormat;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Darren
 */
public class Menu extends JFrame {

    private final JTextField frameTitle = new JTextField("Menu");
    BufferedImage imgSelected, imgInput, imgGray, imgOtsu, imgThinning;
    JSlider gammaSlider = new JSlider(0, 1000, 22);
    JButton reProcessBtn = new JButton("Reprocess");
    JCheckBox negativeCB = new JCheckBox("Make Selected Image Negative");
    BufferedImage imgEdit;
    
    public Menu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(0, 1, 2, 0));

        JPanel uploadPanel = new JPanel();
        JLabel uploadLabel = new JLabel("Image Processor");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 2, 2, 2); // Adjust the insets to minimize the gap
        gbc.anchor = GridBagConstraints.WEST;

        uploadPanel.setLayout(new GridBagLayout());
        uploadPanel.add(uploadLabel, gbc);
        
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridBagLayout());
        JLabel sliderLabel = new JLabel("Gamma Value");
        sliderPanel.add(gammaSlider, gbc);
        gbc.gridx++;
        sliderPanel.add(sliderLabel, gbc);
        gbc.gridx--;
        
        
        JPanel imgInputPanel = new JPanel(); 
        JPanel imgGrayScaleOutputPanel = new JPanel();
        JPanel imgOtsuOutputPanel = new JPanel();
        JPanel imgThinningOutputPanel = new JPanel();
        
        JFileChooser jUploadFile = new JFileChooser();
        jUploadFile.setCurrentDirectory(new File(System.getProperty("user.dir")));
        jUploadFile.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png"));
        JButton chooseBtn = new JButton("Choose Image");
        chooseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jUploadFile.showOpenDialog(null);
                
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jUploadFile.getSelectedFile();
                    String jFileName = selectedFile.getName();
                    if (jFileName.endsWith(".jpg") || jFileName.endsWith(".png") || jFileName.endsWith(".JPG") || jFileName.endsWith(".PNG")) {
                        try {
                            imgInputPanel.removeAll();
                            imgGrayScaleOutputPanel.removeAll();
                            imgOtsuOutputPanel.removeAll();
                            imgThinningOutputPanel.removeAll();
                            
                            imgSelected = ImageIO.read(selectedFile);
                            
                            int defaultGamma = 18;
                            gammaSlider.setValue(defaultGamma);
                            sliderLabel.setText("Gamma Value : " + new DecimalFormat ("#.00").format ((((float)gammaSlider.getValue())/10)));
                            
                            imgInput = Controller.deepCopy(imgSelected);
                            ImageIcon iconInput = new ImageIcon(imgInput);
                            JLabel imgInputLabel = new JLabel();
                            imgInputLabel.setIcon(iconInput);
                            imgInputPanel.add(imgInputLabel);
                            
                            Controller.makeGray(imgSelected, defaultGamma);
                            if(negativeCB.isSelected()) {
                                Controller.makeImageNegative(imgSelected);
                            }
                            imgGray = Controller.deepCopy(imgSelected);
                            ImageIcon iconGray = new ImageIcon(imgGray);
                            JLabel imgGrayLabel = new JLabel();
                            imgGrayLabel.setIcon(iconGray);
                            imgGrayScaleOutputPanel.add(imgGrayLabel);

                            Controller.otsuThreshold(imgSelected);
                            imgOtsu = Controller.deepCopy(imgSelected);
                            ImageIcon iconOtsu = new ImageIcon(imgOtsu);
                            JLabel imgOtsuLabel = new JLabel();
                            imgOtsuLabel.setIcon(iconOtsu);
                            imgOtsuOutputPanel.add(imgOtsuLabel);
                            
                            Controller.thinning(imgSelected);
                            imgThinning = Controller.deepCopy(imgSelected);
                            ImageIcon iconThinning = new ImageIcon(imgThinning);
                            JLabel imgThinningLabel = new JLabel();
                            imgThinningLabel.setIcon(iconThinning);
                            imgThinningOutputPanel.add(imgThinningLabel);
                            
                            gammaSlider.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    sliderLabel.setText("Gamma Value : " + new DecimalFormat ("#.00").format ((((float)gammaSlider.getValue())/10)));                                 
                                }
                            });
                            sliderLabel.setText("Gamma Value : " + new DecimalFormat ("#.00").format ((((float)defaultGamma)/10)));
                            reProcessBtn.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        imgEdit = ImageIO.read(jUploadFile.getSelectedFile());
                                        imgInputPanel.removeAll();
                                        imgInput = Controller.deepCopy(imgEdit);
                                        ImageIcon iconInput = new ImageIcon(imgInput);
                                        JLabel imgInputLabel = new JLabel();
                                        imgInputLabel.setIcon(iconInput);
                                        
                                        imgInputPanel.add(imgInputLabel);

                                        Controller.makeGray(imgEdit, gammaSlider.getValue());
                                        if(negativeCB.isSelected()) {
                                            Controller.makeImageNegative(imgEdit);
                                        }
                                        imgGrayScaleOutputPanel.removeAll();
                                        imgGray = Controller.deepCopy(imgEdit);
                                        ImageIcon iconGray = new ImageIcon(imgGray);
                                        JLabel imgGrayLabel = new JLabel();
                                        imgGrayLabel.setIcon(iconGray);
                                        
                                        imgGrayScaleOutputPanel.add(imgGrayLabel);

                                        Controller.otsuThreshold(imgEdit);
                                        imgOtsuOutputPanel.removeAll();
                                        imgOtsu = Controller.deepCopy(imgEdit);
                                        ImageIcon iconOtsu = new ImageIcon(imgOtsu);
                                        JLabel imgOtsuLabel = new JLabel();
                                        imgOtsuLabel.setIcon(iconOtsu);
                                        
                                        imgOtsuOutputPanel.add(imgOtsuLabel);

                                        Controller.thinning(imgEdit);
                                        imgThinningOutputPanel.removeAll();
                                        imgThinning = Controller.deepCopy(imgEdit);
                                        ImageIcon iconThinning = new ImageIcon(imgThinning);
                                        JLabel imgThinningLabel = new JLabel();
                                        imgThinningLabel.setIcon(iconThinning);                    
                                        imgThinningOutputPanel.add(imgThinningLabel);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                    imgInputPanel.revalidate();
                                    imgGrayScaleOutputPanel.revalidate();
                                    imgOtsuOutputPanel.revalidate();
                                    imgThinningOutputPanel.revalidate();
                                    
                                    imgInputPanel.repaint();
                                    imgGrayScaleOutputPanel.repaint();
                                    imgOtsuOutputPanel.repaint();
                                    imgThinningOutputPanel.repaint();
                                }
                            });
                            
                            sliderPanel.setVisible(true);
                            imgInputPanel.revalidate();
                            imgGrayScaleOutputPanel.revalidate();
                            imgOtsuOutputPanel.revalidate();
                            imgThinningOutputPanel.revalidate();

                            imgInputPanel.repaint();
                            imgGrayScaleOutputPanel.repaint();
                            imgOtsuOutputPanel.repaint();
                            imgThinningOutputPanel.repaint();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        
        gbc.gridx++;
        uploadPanel.add(chooseBtn, gbc);
        gbc.gridx--;
        gbc.gridx+=2;
        uploadPanel.add(negativeCB, gbc);
        gbc.gridx-=2;
        
        sliderPanel.add(reProcessBtn);
        sliderPanel.setVisible(false);
        gbc.gridy++;
        uploadPanel.add(sliderPanel);
        
        menuPanel.add(uploadPanel);
        menuPanel.add(imgInputPanel);
        menuPanel.add(imgGrayScaleOutputPanel);
        menuPanel.add(imgOtsuOutputPanel);
        menuPanel.add(imgThinningOutputPanel);
        JScrollPane scroll = new JScrollPane(menuPanel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scroll);
        this.setTitle(frameTitle.getText());
        this.setSize(1200, 800);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }
}
