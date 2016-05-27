package simplecontroller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
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
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.twinone.rubiksolver.model.AlgorithmMove;
import org.twinone.rubiksolver.model.SimpleRobotMapper;
import org.twinone.rubiksolver.model.SlightlyMoreAdvancedMapper;
import org.twinone.rubiksolver.model.comm.DelayRequest;
import org.twinone.rubiksolver.model.comm.DetachRequest;
import org.twinone.rubiksolver.model.comm.FailedResponseException;
import org.twinone.rubiksolver.model.comm.Packet;
import org.twinone.rubiksolver.model.comm.Request;
import org.twinone.rubiksolver.model.comm.Response;
import org.twinone.rubiksolver.model.comm.ResumeRequest;
import org.twinone.rubiksolver.model.comm.WriteRequest;

/**
 *
 * @author xavier
 */
public class SimpleController {

    RobotScheduler scheduler;
    SimpleRobotMapper mapper = new SimpleRobotMapper();
    SlightlyMoreAdvancedMapper mapper2 = new SlightlyMoreAdvancedMapper();
    
    JFrame frame;
    
    // Global controls
    JButton detachButton;
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
    private final JProgressBar algorithmProgressBar;
    static public interface MotorChangeListener {
        void onMotorChanged(int m, int p);
    }
    public void setMotor(int m, int p) {
        if (positions[m] != p) {
            positions[m] = p;
            if (sendingUpdates) {
                try {
                    scheduler.put(new WriteRequest(m, positions[m]));
                } catch (InterruptedException ex) {
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
                scheduler.put(new WriteRequest(m, positions[m]));
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void setMotorHighLevel(int m, int position) {
        //FIXME: ugly hack here
        WriteRequest r;
        if ((m & 1) == 0)
            r = mapper.gripSide(m >> 1, position != 0, 0);
        else
            r = mapper.rotateSide(m >> 1, position, 0);
        setMotor(m, r.getPosition());
    }
    public void detachMotor(int m) {
        try {
            positions[m] = -1;
            scheduler.put(new DetachRequest(m));
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public SimpleController(RobotScheduler scheduler) {
        this.scheduler = scheduler;
        
        detachButton = new JButton("Detach all");
        detachButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < 8; i++) detachMotor(i);
            }
        });
        sendUpdatesButton = new JToggleButton("Updates");
        sendUpdatesButton.setSelected(true);
        sendUpdatesButton.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                sendingUpdates = sendUpdatesButton.isSelected();
            }
        });
        resendButton = new JButton("Send");
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
        algorithmProgressBar = new JProgressBar();
        JPanel globalControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        globalControls.add(detachButton);
        globalControls.add(sendUpdatesButton);
        globalControls.add(resendButton);
        globalControls.add(algorithmFieldLabel);
        globalControls.add(algorithmField);
        globalControls.add(algorithmProgressBar);
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

            final List<Request> requests = mapper.map(moves);
            
            int totalTime = 0;
            for (Request r : requests)
                if (r instanceof DelayRequest) totalTime += ((DelayRequest)r).getDelay();
            System.out.printf("Theorical time:\n - total: %dms\n - per move: %dms\n - per pre-mapped move: %dms\n", totalTime, totalTime/moves.size(), totalTime/preMappedMoves.size());
            totalTime = (int) Math.round(totalTime / 1000.0);
            System.out.printf("Theorical time: %d:%02d\n", (int)Math.floor(totalTime/60), (int)totalTime % 60);
            
            algorithmProgressBar.setMaximum(requests.size());
            RobotScheduler.ChunkListener listener = new RobotScheduler.ChunkListener() {
                @Override
                public void requestComplete(int i, Request req) {
                    algorithmProgressBar.setValue(i+1);
                    System.out.printf("%4d/%d: %s\n", i+1, requests.size(), req);
                }

                @Override
                public void chunkFailed(int i, Request req, Response res) {
                    algorithmProgressBar.setValue(i+1);
                    System.err.println("Chunk failed at: " + i + " (" + req.getId() + ") " + req + " with " + res.getId());
                }

                @Override
                public void chunkComplete() {
                    algorithmProgressBar.setValue(0);
                    System.out.println("Chunk completed.");
                }
            };
            scheduler.put(requests, listener);
            System.out.println("Sending "+requests.size()+" requests.");
            algorithmField.setText("");

            // FIXME: disable UI while algorithm running
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Make sure to do `stty -F /dev/ttyUSB0 raw -echo 9600`
            String dev = "/dev/ttyUSB0";
            InputStream input = new FileInputStream(dev);
            OutputStream output = new FileOutputStream(dev);
            
            System.out.println("Sending probe to the robot...");
            //hPacket.write(output, new ResumeRequest());
            //output.flush();
            //Packet.checkResponse(input);
            System.out.println("Robot is alive and speaking to us.");
            
            RobotScheduler scheduler = new RobotScheduler(input, output, 1);
            SimpleController c = new SimpleController(scheduler);
        } catch (IOException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        //} catch (FailedResponseException ex) {
        //    Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
