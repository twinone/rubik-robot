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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.twinone.rubiksolver.model.AlgorithmMove;
import org.twinone.rubiksolver.model.SimpleRobotMapper;
import org.twinone.rubiksolver.model.comm.MoveRequest;
import org.twinone.rubiksolver.model.comm.Packet;
import org.twinone.rubiksolver.model.comm.Request;
import org.twinone.rubiksolver.model.comm.WriteRequest;

/**
 *
 * @author xavier
 */
public class SimpleController {

    OutputStream output;
    InputStream input;
    SimpleRobotMapper mapper = new SimpleRobotMapper();
    
    JFrame frame;
    
    // Global controls
    JToggleButton sendUpdatesButton;
    JButton resendButton;
    JTextField algorithmField;
    
    // Servo control
    GripPanel[] gripPanels = new GripPanel[4];
    RotationPanel[] rotationPanels = new RotationPanel[4];
    
    // Axis control
    GripPanel[] axisGripPanels = new GripPanel[4];
    RotationPanel[] axisRotationPanels = new RotationPanel[4];
    
    // Robot communication
    boolean sendingUpdates = true;
    int positions[] = new int[] { -1, -1, -1, -1, -1, -1, -1, -1 };
    static public interface MotorChangeListener {
        void onMotorChanged(int m, int p);
    }
    public void setMotor(int m, int p) {
        if (positions[m] != p) {
            positions[m] = p;
            if (sendingUpdates) {
                try {
                    Packet.write(output, new WriteRequest(m >> 1, m & 1, positions[m]));
                } catch (IOException ex) {
                    Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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
            if (positions[m] == -1) continue;
            try {
                Packet.write(output, new WriteRequest(m >> 1, m & 1, positions[m]));
            } catch (IOException ex) {
                Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void setMotorHighLevel(int m, int position) {
        try {
            positions[m] = -1;
            // FIXME: move logic to construct motor from side, to model
            Packet.write(output, new MoveRequest(m >> 1, m & 1, position));
        } catch (IOException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
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
        resendButton = new JButton("Send");
        resendButton.setSelected(true);
        resendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                resendAll();
            }
        });
        JLabel algorithmFieldLabel = new JLabel("Algorithm:");
        algorithmField = new JTextField();
        algorithmField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runAlgorithm();
            }
        });
        algorithmField.setColumns(15);
        JPanel globalControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        globalControls.add(sendUpdatesButton);
        globalControls.add(resendButton);
        globalControls.add(algorithmFieldLabel);
        globalControls.add(algorithmField);
        // FIXME: allow mapper delays to be changed
        
        JLabel servoControlLabel = new JLabel("Individual servo control");
        JPanel servoControlBody = new JPanel(new GridLayout(4, 2, 5, 5));
        for (int s = 0; s < 4; s++) {
            gripPanels[s] = new GripPanel(this, new int[] { s });
            rotationPanels[s] = new RotationPanel(this, new int[] { s });
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
            axisGripPanels[s] = new GripPanel(this, new int[] { s, s+2 });
            axisRotationPanels[s] = new RotationPanel(this, new int[] { s, s+2 });
            axisControlBody.add(axisGripPanels[s]);
            axisControlBody.add(axisRotationPanels[s]);
        }
        JPanel axisControlPanel = new JPanel(new BorderLayout(5, 5));
        axisControlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        axisControlPanel.add(axisControlLabel, BorderLayout.NORTH);
        axisControlPanel.add(axisControlBody, BorderLayout.CENTER);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(globalControls);
        mainPanel.add(servoControlPanel);
        mainPanel.add(axisControlPanel);
        
        frame = new JFrame("Backend controller");
        frame.add(mainPanel);
        frame.setMinimumSize(new Dimension(700, 400));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    protected void runAlgorithm() {
        try {
            String alg = algorithmField.getText();
            List<AlgorithmMove> moves = AlgorithmMove.parse(alg);

            List<AlgorithmMove> preMappedMoves = new ArrayList<>();
            for (AlgorithmMove move : moves)
                Collections.addAll(preMappedMoves, SimpleRobotMapper.preMap(move));
            System.out.println("Performing algorithm: " + AlgorithmMove.format(moves));
            System.out.println("Pre-mapped algorithm: " + AlgorithmMove.format(preMappedMoves));

            List<Request> requests = mapper.map(moves);
            for (Request request : requests)
                Packet.write(output, request);
            System.out.println("Sending "+requests.size()+" requests.");
            algorithmField.setText("");

            // FIXME: do actual sending in separate class+thread
            //        wait for ok
            //        send N ops before reading ok
            //        send continue at the end
            //        disable UI while algorithm running
        } catch (IOException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            OutputStream output = new FileOutputStream("/dev/ttyACM0");
            InputStream input = new FileInputStream("/dev/ttyACM0");
            SimpleController c = new SimpleController(output, input);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
