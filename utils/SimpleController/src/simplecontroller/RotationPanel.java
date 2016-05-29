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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.twinone.rubiksolver.robot.comm.Request;

/**
 *
 * @author xavier
 */
public class RotationPanel extends JPanel {
    
    int[] sides;
    SimpleController cnt;
    
    JButton detach;
    JTextField field;
    JSlider slider;
    JButton vertical;
    JButton horizontal;
    
    public RotationPanel(SimpleController cnt, int[] sides) {
        this.sides = sides;
        this.cnt = cnt;
        
        detach = new JButton("D");
        field = new JTextField("0");
        field.setColumns(3);
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 0));
        fieldPanel.add(detach, BorderLayout.EAST);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        slider = new JSlider(0, 180, 0);
        
        vertical = new JButton("V");
        horizontal = new JButton("H");
        JPanel shortcutsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        shortcutsPanel.add(vertical);
        shortcutsPanel.add(horizontal);
        
        setLayout(new BorderLayout(5, 5));
        add(fieldPanel, BorderLayout.EAST);
        add(slider, BorderLayout.CENTER);
        add(shortcutsPanel, BorderLayout.WEST);
        
        detach.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                detach();
            }
        });
        field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int position = Integer.parseInt(field.getText());
                    if (position < 0 || position > 180) return;
                    setPosition(position);
                } catch (NumberFormatException ex) {}
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
        if (!isEnabled()) return;
        for (int s : sides)
            cnt.setMotorHighLevel(Request.getMotor(s, Request.MOTOR_ROTATION), position);
    }
    
    protected void setPosition(int pos) {
        if (!isEnabled()) return;
        for (int s : sides)
            cnt.setMotor(Request.getMotor(s, Request.MOTOR_ROTATION), pos);
    }
    
    protected void handleUpdate(int m, int pos) {
        for (int s : sides) {
            if (s != (m >> 1)) continue;
            update(pos);
            return;
        }
    }
    
    protected void detach() {
        if (!isEnabled()) return;
        for (int s : sides)
            cnt.detachMotor(Request.getMotor(s, Request.MOTOR_ROTATION));
    }
    
    protected void update(int pos) {
        for (int s : sides)
            if (cnt.positions[Request.getMotor(s, Request.MOTOR_ROTATION)] != pos) return;
        
        field.setText(String.valueOf(pos));
        slider.setValue(pos);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(false);
        detach.setEnabled(enabled);
        slider.setEnabled(enabled);
        field.setEditable(enabled);
        vertical.setEnabled(enabled);
        horizontal.setEnabled(enabled);
        super.setEnabled(enabled);
    }
    
}
