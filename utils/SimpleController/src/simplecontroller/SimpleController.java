package simplecontroller;

import org.twinone.rubiksolver.robot.RobotScheduler;
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
import org.twinone.rubiksolver.robot.AlgorithmMove;
import org.twinone.rubiksolver.robot.SimpleRobotMapper;
import org.twinone.rubiksolver.robot.SlightlyMoreAdvancedMapper;
import org.twinone.rubiksolver.robot.comm.DelayRequest;
import org.twinone.rubiksolver.robot.comm.DetachRequest;
import org.twinone.rubiksolver.robot.comm.Request;
import org.twinone.rubiksolver.robot.comm.Response;
import org.twinone.rubiksolver.robot.comm.WriteRequest;

/**
 *
 * @author xavier
 */
public class SimpleController {

    RobotScheduler scheduler;
    SimpleRobotMapper mapper = new SimpleRobotMapper();
    SlightlyMoreAdvancedMapper advancedMapper;
    
    JFrame frame;
    
    // Global controls
    JButton detachButton;
    JToggleButton sendUpdatesButton;
    JTextField algorithmField;
    
    // Servo control
    GripPanel[] gripPanels = new GripPanel[4];
    RotationPanel[] rotationPanels = new RotationPanel[4];
    
    // Axis control
    GripPanel[] axisGripPanels = new GripPanel[2];
    RotationPanel[] axisRotationPanels = new RotationPanel[2];
    
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
        this.advancedMapper = new SlightlyMoreAdvancedMapper();
        this.advancedMapper.mapper = mapper;
        
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
        JLabel algorithmFieldLabel = new JLabel("Algorithm:");
        algorithmField = new JTextField();
        algorithmField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runAlgorithm();
            }
        });
        algorithmField.setColumns(15);
        algorithmProgressBar = new JProgressBar();
        algorithmProgressBar.setStringPainted(true);
        algorithmProgressBar.setString("");
        JPanel globalControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        globalControls.add(detachButton);
        globalControls.add(sendUpdatesButton);
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

            //final List<Request> requests = mapper.map(moves);
            final List<Request> requests = advancedMapper.map(moves);
            if (requests.isEmpty()) return;
            
            List<AlgorithmMove> preMappedMoves = new ArrayList<>();
            for (AlgorithmMove move : moves)
                Collections.addAll(preMappedMoves, SimpleRobotMapper.preMap(move));
            System.out.println("Performing algorithm: " + AlgorithmMove.format(moves));
            System.out.println("Pre-mapped algorithm: " + AlgorithmMove.format(preMappedMoves));
            
            int totalTime = 0;
            for (Request r : requests)
                if (r instanceof DelayRequest) totalTime += ((DelayRequest)r).getDelay();
            int totalSeconds = (int)Math.round(totalTime / 1000.0);
            System.out.printf("Theorical time:\n - total: %dms (%02d:%02d)\n - per move: %dms\n - per pre-mapped move: %dms\n", totalTime, (int)Math.floor(totalTime/60), (int)totalTime % 60, totalTime/moves.size(), totalTime/preMappedMoves.size());
            
            algorithmProgressBar.setValue(0);
            algorithmProgressBar.setMaximum(requests.size());
            final int finalTotalTime = totalTime;
            RobotScheduler.ChunkListener listener = new RobotScheduler.ChunkListener() {
                int elapsedTime;
                @Override
                public void requestComplete(int i, Request req) {
                    if (req instanceof DelayRequest)
                        elapsedTime += ((DelayRequest)req).getDelay();
                    int remainingTime = (int)Math.round((finalTotalTime - elapsedTime) / 1000.0);
                    algorithmProgressBar.setString(String.format("%d/%d (%02d:%02d)", (i+1), requests.size(), (int)Math.floor(remainingTime/60), (int)remainingTime % 60));
                    algorithmProgressBar.setValue(i+1);
                    System.out.printf("%4d/%d: %s\n", i+1, requests.size(), req);
                }

                @Override
                public void chunkFailed(int i, Request req, Response res) {
                    System.err.println("Chunk failed at: " + i + " (" + req.getId() + ") " + req + " with " + res.getId());
                    setGuiEnabled(true);
                }

                @Override
                public void chunkComplete() {
                    System.out.println("Chunk completed.");
                    algorithmField.setText("");
                    setGuiEnabled(true);
                }
            };
            scheduler.put(requests, listener);
            System.out.println("Sending "+requests.size()+" requests.");
            setGuiEnabled(false);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setGuiEnabled(boolean enabled) {
        algorithmField.setEditable(enabled);
        detachButton.setEnabled(enabled);
        sendUpdatesButton.setEnabled(enabled);
        for (GripPanel p : gripPanels)
            p.setEnabled(enabled);
        for (RotationPanel p : rotationPanels)
            p.setEnabled(enabled);
        for (GripPanel p : axisGripPanels)
            p.setEnabled(enabled);
        for (RotationPanel p : axisRotationPanels)
            p.setEnabled(enabled);
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
            Packet.write(output, new ResumeRequest());
            output.flush();
            Packet.checkResponse(input);
            System.out.println("Robot is alive and speaking to us.");
            
            RobotScheduler scheduler = new RobotScheduler(input, output, 1);
            SimpleController c = new SimpleController(scheduler);
        } catch (IOException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FailedResponseException ex) {
            Logger.getLogger(SimpleController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
