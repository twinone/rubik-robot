package simplecontroller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author xavier
 */
public class SimpleController {

    OutputStream output;
    InputStream input;
    
    JFrame frame;
    
    // Global controls
    JToggleButton sendUpdatesButton;
    JButton resendButton;
    
    // Servo control
    GripPanel[] gripPanels;
    RotationPanel[] rotationPanels;
    
    // Axis control
    GripPanel[] axisGripPanels;
    RotationPanel[] axisRotationPanels;
    
    // Robot communication
    boolean sendingUpdates = false;
    int positions[] = new int[8];
    static public interface MotorChangeListener {
        void onMotorChanged(int m, int p);
    }
    public void setMotor(int m, int p) {
        if (positions[m] != p) {
            if (sendingUpdates) {
                // TODO: write
            }
            positions[m] = p;
            for (MotorChangeListener listener : listeners) {
                listener.onMotorChanged(m, p);
            }
        }
    }
    List<MotorChangeListener> listeners = new ArrayList<>();
    public void addMotorListener(MotorChangeListener listener) {
        listeners.add(listener);
    }
    public void resendAll() {
        for (int m = 0; m < 8; m++) {
            //TODO: write
        }
    }
    
    public SimpleController(OutputStream output, InputStream input) {
        this.output = output;
        this.input = input;
        
        sendUpdatesButton = new JToggleButton("Updates");
        sendUpdatesButton.setSelected(true);
        sendUpdatesButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                sendingUpdates = sendUpdatesButton.isSelected();
            }
        });
        resendButton = new JButton("Updates");
        resendButton.setSelected(true);
        resendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                resendAll();
            }
        });
        JPanel globalControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        globalControls.add(sendUpdatesButton);
        globalControls.add(resendButton);
        
        JLabel servoControlLabel = new JLabel("Individual servo control");
        JPanel servoControlBody = new JPanel(new GridLayout(4, 2, 5, 5));
        for (int s = 0; s < 4; s++) {
            gripPanels[s] = new GripPanel(new int[] { s });
            rotationPanels[s] = new RotationPanel(new int[] { s });
            servoControlBody.add(gripPanels[s]);
            servoControlBody.add(rotationPanels[s]);
        }
        JPanel servoControlPanel = new JPanel(new BorderLayout(5, 5));
        servoControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        servoControlPanel.add(servoControlLabel, BorderLayout.NORTH);
        servoControlPanel.add(servoControlBody, BorderLayout.CENTER);
        
        JLabel axisControlLabel = new JLabel("Whole axis control");
        JPanel axisControlBody = new JPanel(new GridLayout(2, 2, 5, 5));
        for (int s = 0; s < 2; s++) {
            axisGripPanels[s] = new GripPanel(new int[] { s, s+2 });
            axisRotationPanels[s] = new RotationPanel(new int[] { s, s+2 });
            axisControlBody.add(axisGripPanels[s]);
            axisControlBody.add(axisRotationPanels[s]);
        }
        JPanel axisControlPanel = new JPanel(new BorderLayout(5, 5));
        axisControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        axisControlPanel.add(axisControlLabel, BorderLayout.NORTH);
        axisControlPanel.add(axisControlBody, BorderLayout.CENTER);
        
        frame = new JFrame("Backend controller");
        frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));
        frame.add(globalControls);
        frame.add(servoControlPanel);
        frame.add(axisControlPanel);
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(600, 480));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            OutputStream output = new FileOutputStream("/dev/ttyAMC0");
            InputStream input = new FileInputStream("/dev/ttyAMC0");
            SimpleController c = new SimpleController(output, input);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
