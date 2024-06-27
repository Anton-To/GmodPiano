package piano;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

public class GmodPiano implements Receiver, ActionListener, NativeKeyListener {
    int[] keysToRelease = {49, 50, 51, 52, 53, 53, 54, 55, 56, 57, 48, 81, 87, 69, 82, 84, 89, 85, 73, 79, 80, 65, 83, 68, 70, 71, 72, 74, 75, 76, 90, 88, 67, 86, 66, 78, 77};
    JFileChooser fileChooser;
    static JFrame frame;
    JPanel panel;
    static JTextField chosenFile;
    JTextField currentTranspose;
    JButton selectSource;
    boolean fileMode = true;
    static MidiDevice.Info[] infos;
    static MidiDevice device;
    static JComboBox<Object> deviceList;
    static JLabel statusLabel;
    static File path;
    static Sequence sequence;
    static Sequencer sequencer;
    static Transmitter transmitter;
    KeyPresser keyPresser = new KeyPresser();
    Robot robot = new Robot();
    JTextField t1F;

    public GmodPiano() throws AWTException {}

    public static void main(String[] args) throws NativeHookException, AWTException {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.out.println("Failed to register the native hook");
            throw e;
        }
        GlobalScreen.addNativeKeyListener(new GmodPiano());
        GmodPiano g = new GmodPiano();
        g.init();
        g.close();
    }

    public void init() {
        frame = new JFrame("Gmod Piano Player");
        frame.setSize(510, 115);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        panel = new JPanel();
        frame.add(panel);
        frame.setResizable(false);
        PlaceComponents(panel);
        frame.setVisible(true);
    }

    public void PlaceComponents(JPanel panel) {
        panel.setLayout(null);

        selectSource = new JButton("Select file");
        selectSource.setBounds(10, 10, 100, 25);
        selectSource.setMargin(new Insets(0, 0, 0, 0));
        panel.add(selectSource);
        selectSource.setActionCommand("selectSource");
        selectSource.addActionListener(this);

        JButton modeButton = new JButton("Mode");
        modeButton.setToolTipText("Toggle between midi file input and midi keyboard input");
        modeButton.setBounds(395, 40, 65, 25);
        panel.add(modeButton);
        modeButton.setActionCommand("toggleMode");
        modeButton.addActionListener(this);

        JButton octaveUp = new JButton("+");
        octaveUp.setToolTipText("Transpose one octave up");
        octaveUp.setBounds(366, 40, 20, 25);
        octaveUp.setMargin(new Insets(0, 0, 0, 0));
        panel.add(octaveUp);
        octaveUp.setActionCommand("octaveUp");
        octaveUp.addActionListener(this);

        JButton octaveDown = new JButton("-");
        octaveDown.setToolTipText("Transpose one octave down");
        octaveDown.setBounds(310, 40, 20, 25);
        octaveDown.setMargin(new Insets(0, 0, 0, 0));
        panel.add(octaveDown);
        octaveDown.setActionCommand("octaveDown");
        octaveDown.addActionListener(this);

        currentTranspose = new JTextField(0);
        currentTranspose.setToolTipText("Current transposition");
        currentTranspose.setBounds(335, 40, 26, 25);
        currentTranspose.setHorizontalAlignment(JTextField.CENTER);
        currentTranspose.setText(keyPresser.getTranspose());
        currentTranspose.setEditable(false);
        panel.add(currentTranspose);

        chosenFile = new JTextField(0);
        chosenFile.setBounds(120, 10, 365, 25);
        chosenFile.setEditable(false);
        panel.add(chosenFile);

        t1F = new JTextField(0);
        t1F.setBounds(465, 40, 20, 26);
        t1F.setToolTipText("Delay between pressing and releasing keys");
        t1F.setText(String.valueOf(keyPresser.getTiming()));
        t1F.setActionCommand("timingChanged");
        t1F.addActionListener(this);
        t1F.setHorizontalAlignment(JTextField.CENTER);
        panel.add(t1F);

        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Midi files", "mid", "midi"));

        statusLabel = new JLabel("Please select a file");
        statusLabel.setBounds(10, 47, 700, 15);
        panel.add(statusLabel);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command == "selectSource") {
            if (fileMode) {
                int returnValue = fileChooser.showOpenDialog(frame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    keyPresser.resetTranspose();
                    currentTranspose.setText(keyPresser.getTranspose());
                    path = fileChooser.getSelectedFile();
                    chosenFile.setText(path.getName());
                    statusLabel.setText("Press ALT + UP to play and ALT + DOWN to stop");
                }
            } else {
                try {
                    device = MidiSystem.getMidiDevice(infos[deviceList.getSelectedIndex()]);
                    device.open();
                    transmitter = device.getTransmitter();
                    transmitter.setReceiver(this);
                    statusLabel.setText("Device " + device.getDeviceInfo() + " opened");
                } catch (MidiUnavailableException e) {
                    statusLabel.setText("Unable to open device " + device.getDeviceInfo());
                    e.printStackTrace();
                }
            }
        } else if (command == "toggleMode") {
            if (fileMode) {
                statusLabel.setText("Switched to midi input mode");
                infos = MidiSystem.getMidiDeviceInfo();
                for (MidiDevice.Info info : infos) {
                    try {
                        device = MidiSystem.getMidiDevice(info);
                    } catch (MidiUnavailableException e) {
                        e.printStackTrace();
                    }
                }
                panel.remove(chosenFile);
                deviceList = new JComboBox<>(infos);
                deviceList.setEditable(false);
                deviceList.setBounds(120, 10, 365, 25);
                deviceList.addActionListener(this);
                panel.add(deviceList);
                frame.repaint();
                selectSource.setText("Select device");
                fileMode = false;
            } else {
                if (transmitter != null) transmitter.close();
                device.close();
                statusLabel.setText("Switched to file mode");
                panel.remove(deviceList);
                panel.add(chosenFile);
                frame.repaint();
                selectSource.setText("Select file");
                fileMode = true;
            }
        } else if (command == "timingChanged") {
            keyPresser.updateTimings(Integer.parseInt(t1F.getText()));
            statusLabel.setText("Timings updated");
        } else if (command == "octaveUp") {
            keyPresser.setTranspose(true);
            currentTranspose.setText(keyPresser.getTranspose());
        } else if (command == "octaveDown") {
            keyPresser.setTranspose(false);
            currentTranspose.setText(keyPresser.getTranspose());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if (fileMode) {
            if (event.getKeyCode() == NativeKeyEvent.VC_UP && (event.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) {
                if (sequencer != null && sequencer.isOpen()) sequencer.close();
                if (transmitter != null) transmitter.close();

                try {
                    sequence = MidiSystem.getSequence(path);
                    sequencer = MidiSystem.getSequencer();
                    sequencer.open();
                    sequencer.setSequence(sequence);
                    transmitter = sequencer.getTransmitter();
                    sequencer.addMetaEventListener(metaMsg -> {
                        if (metaMsg.getType() == 0x2F) {
                            sequencer.close();
                            transmitter.close();

                            robot.keyRelease(KeyEvent.VK_SHIFT);
                            for (int key : keysToRelease) {
                                robot.keyRelease(key);
                            }

                            statusLabel.setText("Track ended");
                        }
                    });
                    transmitter.setReceiver(this);
                    Thread.sleep(500);
                    sequencer.start();
                    statusLabel.setText("Playing. Press ALT + DOWN to stop");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.getKeyCode() == NativeKeyEvent.VC_DOWN && (event.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) {
                try {
                    sequencer.close();
                    transmitter.close();
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                statusLabel.setText("Player stopped");
            }
        }
    }

    @Override
    public void send(MidiMessage message, long timestamp) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int velocity = sm.getData2();
            int note = sm.getData1();
            int command = sm.getCommand();

            if (command == ShortMessage.NOTE_ON) {
                keyPresser.press(note, velocity > 0);
            } else if (command == ShortMessage.NOTE_OFF) {
                keyPresser.press(note, false);
            }
        }
    }

    @Override
    public void close() {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}
}

