package gmodPiano;

import java.io.File;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.AWTException;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GmodPiano implements Receiver, ActionListener, NativeKeyListener{
	private static final Logger log = Logger.getLogger(GlobalScreen.class.getPackage().getName());
	static int[] letters = new int[127]; //an array that maps notes to a button
	int note = 0;
	int velocity = 0;
	int returnValue = 1;
	int t1 = 8;  //delay between pressing shift and playing a note is played without shift - delay after playing a note
	int t2 = 16; //delay between releasing shift and note
	static int transpose = 0;
	Robot robot = null;
	JFileChooser fileChooser;
	static JFrame frame;
	JPanel panel;
	static JTextField chosenFile;
	JTextField t1F;
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
	
	public static void main(String[] args) {
		log.setUseParentHandlers(false);
		log.setLevel(Level.OFF);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.OFF);
		log.addHandler(handler);        
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		GlobalScreen.addNativeKeyListener(new GmodPiano());
		GmodPiano g = new GmodPiano();
		g.init();
		g.close();
	}
	
	public void init() {
		//mapping note ids to button id's
		letters[24] = 49;   //1
		letters[25] = 49;      //! 
		letters[26] = 50;   //2
		letters[27] = 50;      //@ 
		letters[28] = 51;   //3
		letters[29] = 52;   //4
		letters[30] = 52;      //$ 
		letters[31] = 53;   //5
		letters[32] = 53;      //%
		letters[33] = 54;   //6 
		letters[34] = 54;      //^ 
		letters[35] = 55;   //7
		letters[36] = 56;   //8
		letters[37] = 56;      //* 
		letters[38] = 57;   //9
		letters[39] = 57;      //[ 
		letters[40] = 48;   //0
		letters[41] = 81;   //q
		letters[42] = 81;      //Q
		letters[43] = 87;   //w
		letters[44] = 87;      //W
		letters[45] = 69;   //e
		letters[46] = 69;      //E
		letters[47] = 82;   //r
		letters[48] = 84;   //t
		letters[49] = 84;      //T
		letters[50] = 89;   //y
		letters[51] = 89;      //Y
		letters[52] = 85;   //u
		letters[53] = 73;   //i
		letters[54] = 73;      //I
		letters[55] = 79;   //o
		letters[56] = 79;      //O
		letters[57] = 80;   //p
		letters[58] = 80;      //P
		letters[59] = 65;   //a
		letters[60] = 83;   //s
		letters[61] = 83;      //S
		letters[62] = 68;   //d
		letters[63] = 68;      //D
		letters[64] = 70;   //f
		letters[65] = 71;   //g
		letters[66] = 71;      //G
		letters[67] = 72;   //h
		letters[68] = 72;      //H
		letters[69] = 74;   //j
		letters[70] = 74;      //J
		letters[71] = 75;   //k
		letters[72] = 76;   //l
		letters[73] = 76;      //L
		letters[74] = 90;   //z
		letters[75] = 90;      //Z
		letters[76] = 88;   //x
		letters[77] = 67;   //c
		letters[78] = 67;      //C
		letters[79] = 86;   //v
		letters[80] = 86;      //V
		letters[81] = 66;   //b
		letters[82] = 66;      //B
		letters[83] = 78;   //n
		letters[84] = 77;   //m
				
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
		selectSource.setBounds(10,10,100,25);
		selectSource.setMargin(new Insets(0,0,0,0));
		panel.add(selectSource);
		selectSource.setActionCommand("selectSource");
		selectSource.addActionListener(this);
		
		JButton modeButton = new JButton("Mode");
		modeButton.setToolTipText("Toggle between midi file input and midi keyboard input");
		modeButton.setBounds(395,40,65,25);
		panel.add(modeButton);
		modeButton.setActionCommand("toggleMode");
		modeButton.addActionListener(this);
		
		JButton octaveUp = new JButton("+");
		octaveUp.setToolTipText("Transpose one octave up");
		octaveUp.setBounds(366,40,20,25);
		octaveUp.setMargin(new Insets(0,0,0,0));
		panel.add(octaveUp);
		octaveUp.setActionCommand("octaveUp");
		octaveUp.addActionListener(this);
				
		JButton octaveDown = new JButton("-");
		octaveDown.setToolTipText("Transpose one octave down");
		octaveDown.setBounds(310,40,20,25);
		octaveDown.setMargin(new Insets(0,0,0,0));
		panel.add(octaveDown);
		octaveDown.setActionCommand("octaveDown");
		octaveDown.addActionListener(this);
		
		currentTranspose = new JTextField(0);
		currentTranspose.setToolTipText("Current transposition");
		currentTranspose.setBounds(335,40,26,25);
		currentTranspose.setHorizontalAlignment(JTextField.CENTER);
		currentTranspose.setText((transpose > 0 ? "+" : "" ) + transpose);
		currentTranspose.setEditable(false);
		panel.add(currentTranspose);
			
		chosenFile = new JTextField(0);
		chosenFile.setBounds(120,10,365,25);
		chosenFile.setEditable(false);
		panel.add(chosenFile);
				
		t1F = new JTextField(0);
		t1F.setBounds(465,40,20,26);
		t1F.setToolTipText("Delay between pressing and releasing keys");
		t1F.setText(Integer.toString(t1));
		t1F.setActionCommand("t1Changed");
		t1F.addActionListener(this);
		t1F.setHorizontalAlignment(JTextField.CENTER);
		panel.add(t1F);
				
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileFilter(new FileNameExtensionFilter("Midi files", "mid", "midi"));
		
		statusLabel = new JLabel("Please select a file");
		statusLabel.setBounds(10,47,700,15);
		panel.add(statusLabel);
	}
		
	
	@Override
	public void actionPerformed(ActionEvent event) {
		switch(event.getActionCommand()) {	
		case "selectSource": // selecting midi source
				if(fileMode) {
					returnValue = fileChooser.showOpenDialog(frame);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						path = fileChooser.getSelectedFile();
						chosenFile.setText(path.getName());
						statusLabel.setText("Press ALT + UP to play and ALT + DOWN to stop");
					}
				}else{
					try {
						device = MidiSystem.getMidiDevice(infos[deviceList.getSelectedIndex()]);
						device.open();
						transmitter = device.getTransmitter();
						transmitter.setReceiver(this);
						statusLabel.setText("Device " + device.getDeviceInfo() + " opened");
					}catch (MidiUnavailableException e) {
						statusLabel.setText("Unable to open device " + device.getDeviceInfo());
						e.printStackTrace();
					}
				}
				break;
				
			case "toggleMode": //switching between device mode and file mode
				if(fileMode){
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
					deviceList.setBounds(120,10,365,25);
			        deviceList.addActionListener(this);
			        panel.add(deviceList);
			        frame.repaint();
			        selectSource.setText("Select device");
			        fileMode = false;		        		        		     		        
				}else{
					try {
		        	transmitter.close();
					}catch(Exception e) {
						e.printStackTrace();
					}
		        	device.close();
					statusLabel.setText("Switched to file mode");
					panel.remove(deviceList);
					panel.add(chosenFile);
					frame.repaint();
					selectSource.setText("Select file");
					fileMode = true;
				}
				break;
				
			case "octaveUp": //transposing one octave up
				if(transpose != 12) {
					transpose = transpose + 12;					
				}
				currentTranspose.setText((transpose > 0 ? "+" : "" ) + transpose);	
				break;
				
			case "octaveDown": //transposing one octave down
				if(transpose != -12) {
					transpose = transpose-12;
				}
				currentTranspose.setText((transpose > 0 ? "+" : "" ) + transpose);
				break;
				
			case "t1Changed": //changing timings for key presses
				try {
					t1 = Integer.parseInt(t1F.getText());
					t2 = t1*2;
					statusLabel.setText("Timings changed");
				}catch(NumberFormatException e) {
					statusLabel.setText("Only numbers are allowed");
				}
				break;	
		}
	}	
	
	@Override
	public void nativeKeyPressed(NativeKeyEvent event) {
		if(fileMode) {
			if (event.getKeyCode() == NativeKeyEvent.VC_UP && (event.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) { // Alt + arrow up to start playing			
				try {
		        	sequencer.close();
		        	transmitter.close();
				}catch(Exception e) {
					e.printStackTrace();
				}
				try {	
					sequence = MidiSystem.getSequence(path);
					sequencer = MidiSystem.getSequencer();
			        sequencer.open();
			        sequencer.setSequence(sequence);
			        transmitter = sequencer.getTransmitter();
			        sequencer.addMetaEventListener(metaMsg -> {
						if (metaMsg.getType() == 0x2F) { //closing everything when track is ended
							sequencer.close();
							transmitter.close();
							statusLabel.setText("Track ended");
						}
					});
			        Thread.sleep(500);
			        transmitter.setReceiver(this);
			        sequencer.start();
			        statusLabel.setText("Playing. Press ALT + DOWN to stop");
				}catch(Exception e) {
					e.printStackTrace();
				}				
			}else if(event.getKeyCode() == NativeKeyEvent.VC_DOWN && (event.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) { //Alt + arrow down to stop playing
				try {
					sequencer.close();
					transmitter.close();
				}catch(Exception e) {
					e.printStackTrace();
				}
				statusLabel.setText("Player stopped");	
			}
		}		
	}
	

	@Override
	public void send(MidiMessage message, long timestamp) {
		if(robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	    if(message instanceof ShortMessage) {
	        ShortMessage sm = (ShortMessage) message;
			velocity = sm.getData2();
	        if (sm.getCommand() == ShortMessage.NOTE_ON) {
	        	if(letters[note] != 0) {
	        		robot.keyRelease(KeyEvent.VK_SHIFT);
	        		robot.delay(t2);	        		
	        		robot.keyRelease(letters[note]);
	        	}
	        	note = sm.getData1() + transpose;
	        	if(letters[note] != 0 && velocity != 0) {
	        		if(note == 25 || note == 27 || note == 30 || note == 32 || note == 34 || note == 37 || note == 39 || note == 42 || note == 44 || note == 46 || note == 49 || note == 51 || note == 54 || note == 56 || note == 58 || note == 61 || note == 63 || note == 66 || note == 68 || note == 70 || note == 73 || note == 75 || note == 78 || note == 80 || note == 82) {
	        			robot.keyPress(KeyEvent.VK_SHIFT); // some notes need to be played with shift on
	        			robot.delay(t1);
	        			robot.keyPress(letters[note]);
	        		}else { 
	        			robot.keyPress(letters[note]); //playing notes without shift on
	        			robot.delay(t1);

	        		}
	        	}
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