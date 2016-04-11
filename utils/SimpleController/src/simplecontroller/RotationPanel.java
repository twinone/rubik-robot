/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplecontroller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author xavier
 */
public class RotationPanel extends JPanel {
    
    int[] sides;
    SimpleController cnt;
    
    JTextField field;
    JSlider slider;
    
    public RotationPanel(SimpleController cnt, int[] sides) {
        this.sides = sides;
        this.cnt = cnt;
        field = new JTextField("0");
        field.setColumns(3);
        slider = new JSlider(0, 180, 0);
        
        JButton vertical = new JButton("V");
        JButton horizontal = new JButton("H");
        JPanel shortcutsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        shortcutsPanel.add(vertical);
        shortcutsPanel.add(horizontal);
        
        setLayout(new BorderLayout(5, 5));
        add(field, BorderLayout.EAST);
        add(slider, BorderLayout.CENTER);
        add(shortcutsPanel, BorderLayout.WEST);
        
        field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setPosition(Integer.parseInt(field.getText()));
            }
        });
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setPosition(slider.getValue());
            }
        });
        vertical.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMove(0);
                //setPosition(0);
            }
        });
        horizontal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMove(1);
                //setPosition(8);
            }
        });
        
        cnt.addMotorListener(new SimpleController.MotorChangeListener() {
            public void onMotorChanged(int m, int p) {
                handleUpdate(m, p);
            }
        });
    }
    
    protected void sendMove(int position) {
        for (int s : sides)
            cnt.setMotorHighLevel((s << 1) | 1, position);
    }
    
    protected void setPosition(int pos) {
        for (int s : sides)
            cnt.setMotor((s << 1) | 1, pos);
    }
    
    protected void handleUpdate(int m, int pos) {
        for (int s : sides) {
            if (s != (m >> 1)) continue;
            update(pos);
            return;
        }
    }
    
    protected void update(int pos) {
        for (int s : sides)
            if (cnt.positions[(s << 1) | 1] != pos) return;
        
        field.setText(String.valueOf(pos));
        slider.setValue(pos);
    }
}
