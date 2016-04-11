package simplecontroller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author xavier
 */
public class GripPanel extends JPanel {
    
    int[] sides;
    SimpleController cnt;
    
    JTextField field;
    JSlider slider;
    JToggleButton toggle;
    
    public GripPanel(SimpleController cnt, int[] sides) {
        this.sides = sides;
        this.cnt = cnt;
        field = new JTextField("0");
        field.setColumns(3);
        slider = new JSlider(0, 180, 0);
        toggle = new JToggleButton("Grip");
        setLayout(new BorderLayout(5, 5));
        add(field, BorderLayout.WEST);
        add(slider, BorderLayout.CENTER);
        add(toggle, BorderLayout.EAST);
        
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
        for (int s : sides)
            cnt.setMotorHighLevel((s << 1) | 0, toggle.isSelected() ? 1 : 0);
    }
    
    protected void setPosition(int pos) {
        for (int s : sides)
            cnt.setMotor((s << 1) | 0, pos);
    }
    
    protected void handleUpdate(int m, int pos) {
        for (int s : sides) {
            if (s != (m >> 1)) continue;
            update(pos);
            return;
        }
    }
    
    protected void update(int pos) {
        for (int side : sides)
            if (cnt.positions[(side << 1) | 0] != pos) return;
        
        field.setText(String.valueOf(pos));
        slider.setValue(pos);
        if (sides.length == 1) toggle.setSelected(pos == 180);
    }
    
}
