package simplecontroller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.twinone.rubiksolver.robot.comm.Request;

/**
 *
 * @author xavier
 */
public class GripPanel extends JPanel {
    
    int[] sides;
    SimpleController cnt;
    
    JButton detach;
    JTextField field;
    JSlider slider;
    JToggleButton toggle;
    
    public GripPanel(SimpleController cnt, int[] sides) {
        this.sides = sides;
        this.cnt = cnt;
        
        detach = new JButton("D");
        field = new JTextField("0");
        field.setColumns(3);
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 0));
        fieldPanel.add(detach, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);
        
        slider = new JSlider(0, 180, 0);
        
        toggle = new JToggleButton("Grip");
        setLayout(new BorderLayout(5, 5));
        add(fieldPanel, BorderLayout.WEST);
        add(slider, BorderLayout.CENTER);
        add(toggle, BorderLayout.EAST);
        
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
        toggle.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                sendMove();
            }
        });
        
        cnt.addMotorListener(new SimpleController.MotorChangeListener() {
            public void onMotorChanged(int m, int p) {
                handleUpdate(m, p);
            }
        });
    }
    
    protected void sendMove() {
        if (!isEnabled()) return;
        for (int s : sides)
            cnt.setMotorHighLevel(Request.getMotor(s, Request.MOTOR_GRIP), toggle.isSelected() ? 1 : 0);
    }
    
    protected void setPosition(int pos) {
        if (!isEnabled()) return;
        for (int s : sides)
            cnt.setMotor(Request.getMotor(s, Request.MOTOR_GRIP), pos);
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
            cnt.detachMotor(Request.getMotor(s, Request.MOTOR_GRIP));
    }
    
    protected void update(int pos) {
        for (int s : sides)
            if (cnt.positions[Request.getMotor(s, Request.MOTOR_GRIP)] != pos) return;
        
        field.setText(String.valueOf(pos));
        slider.setValue(pos);
        //if (sides.length == 1) toggle.setSelected(pos == 180);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(false);
        detach.setEnabled(enabled);
        field.setEditable(enabled);
        slider.setEnabled(enabled);
        toggle.setEnabled(enabled);
        super.setEnabled(enabled);
    }
    
}
