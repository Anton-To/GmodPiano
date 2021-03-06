package gmodPiano;

import java.io.File;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
	int[] letters = new int[127]; //an array that maps notes to a button
	int returnValue = 1;
	int t1 = 8;
	int t2 = 8;
	int t3 = 8;
	Robot robot = null;
	JFrame frame;
	JFileChooser fileChooser;
	JTextField choosenFile;
	JTextField t1F;
	JTextField t2F;
	JTextField t3F;
	JLabel statusLabel;
	File path;
	Sequence sequence;
	Sequencer sequencer;
	Transmitter transmitter;
	
	public static void main(String args[]) throws Exception {
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
	
	public void init() throws Exception {
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
		JPanel panel = new JPanel();
		frame.add(panel);
		frame.setResizable(false);
		PlaceComponents(panel);
		frame.setVisible(true);
	}
	
	public void PlaceComponents(JPanel panel) {
		panel.setLayout(null);
				
		JButton choosefile = new JButton("Choose file");
		choosefile.setBounds(10,10,100,25);
		panel.add(choosefile);
		choosefile.setActionCommand("chooseFile");
		choosefile.addActionListener(this);
		
		JButton playButton = new JButton("Play");
		playButton.setBounds(10,40,100,25);
		panel.add(playButton);
		playButton.setActionCommand("play");
		playButton.addActionListener(this);
		
		JButton stopButton = new JButton("Stop");
		stopButton.setBounds(120,40,100,25);
		panel.add(stopButton);
		stopButton.setActionCommand("stop");
		stopButton.addActionListener(this);
		
		JButton okButton = new JButton("");
		okButton.setToolTipText("Apply timings");
		okButton.setBounds(465,40,20,25);
		panel.add(okButton);
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
			
		choosenFile = new JTextField(0);
		choosenFile.setBounds(120,10,365,25);
		choosenFile.setEditable(false);
		panel.add(choosenFile);
		
		t1F = new JTextField(0);
		t1F.setBounds(390,40,20,25);
		t1F.setToolTipText("Timing 1");
		t1F.setText(Integer.toString(t1));
		t1F.setHorizontalAlignment(JTextField.CENTER);
		panel.add(t1F);
		
		t2F = new JTextField(0);
		t2F.setBounds(415,40,20,25);
		t2F.setToolTipText("Timing 2");
		t2F.setText(Integer.toString(t2));
		t2F.setHorizontalAlignment(JTextField.CENTER);
		panel.add(t2F);
		
		t3F = new JTextField(0);
		t3F.setBounds(441,40,20,25);
		t3F.setToolTipText("Timing 3");
		t3F.setText(Integer.toString(t3));
		t3F.setHorizontalAlignment(JTextField.CENTER);
		panel.add(t3F);
				
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileFilter(new FileNameExtensionFilter("Midi files", "mid", "midi"));
		
		statusLabel = new JLabel("Please select a file");
		statusLabel.setBounds(225,45,700,15);
		panel.add(statusLabel);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand() == "chooseFile"){
			returnValue = fileChooser.showOpenDialog(frame);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				path = fileChooser.getSelectedFile();
				choosenFile.setText(path.getName());	
			}
			
		

		}else if(event.getActionCommand() == "play") {
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				try {
		        	sequencer.close();
		        	transmitter.close();
				}catch(Exception e) {}
				try {	
					robot = new Robot();
					sequence = MidiSystem.getSequence(path);
			        sequencer = MidiSystem.getSequencer();
			        sequencer.open();
			        sequencer.setSequence(sequence);
			        transmitter = sequencer.getTransmitter();
			        sequencer.addMetaEventListener(new MetaEventListener() {
			            @Override
			            public void meta(MetaMessage metaMsg) {
			                if (metaMsg.getType() == 0x2F) { //closing everything when track is ended
			                	sequencer.close();
			                	transmitter.close();
			                	statusLabel.setText("Track ended");	
			                }
			            }
			        }); 
			        Thread.sleep(3000); //delay to let user tab back to the game
			        transmitter.setReceiver(this);
			        sequencer.start();
			        statusLabel.setText("Playing");
				}catch(Exception e) {}
			}
			
		}else if(event.getActionCommand() == "stop") {
			try {
				robot.keyRelease(KeyEvent.VK_SHIFT);
	        	sequencer.close();
	        	transmitter.close();
			}catch(Exception e) {}
			statusLabel.setText("Player stopped");	
			
		}else if(event.getActionCommand() == "ok") { //setting custom timings between key presses
			try {
				t1 = Integer.parseInt(t1F.getText());
				t2 = Integer.parseInt(t2F.getText());
				t3 = Integer.parseInt(t3F.getText());
				statusLabel.setText("Timings changed");
			}catch(NumberFormatException e) {
				statusLabel.setText("Only numbers are allowed");
			}
		}		
	}	

	@Override
	public void send(MidiMessage message, long timestamp) {
	    if(message instanceof ShortMessage) {
	        ShortMessage sm = (ShortMessage) message;
	        if (sm.getCommand() == ShortMessage.NOTE_ON) {
	        	int note = sm.getData1();
	        	if(letters[note] != 0) {
	        		try {
	        			// some notes need to be played with shift on
		        		if(note == 25 || note == 27 || note == 30 || note == 32 || note == 34 || note == 37 || note == 39 || note == 42 || note == 44 || note == 46 || note == 49 || note == 51 || note == 54 || note == 56 || note == 58 || note == 61 || note == 63 || note == 66 || note == 68 || note == 70 || note == 73 || note == 75 || note == 78 || note == 80 || note == 82) {
		        			robot.keyPress(KeyEvent.VK_SHIFT);
							Thread.sleep(t1);
		        			robot.keyPress(letters[note]);
							Thread.sleep(t2);
		        			robot.keyRelease(KeyEvent.VK_SHIFT);
		        			robot.keyRelease(letters[note]);
		        		}else { //playing notes without shift on
		        			robot.keyPress(letters[note]);
							Thread.sleep(t3);
							robot.keyRelease(letters[note]);
		        		}
	        		}catch (InterruptedException e1) {}
	        	}
	        }
	    }
	}

	@Override
	public void close() {}



	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_UP && (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) { // Alt + arrow up to start playing			
			System.out.println("Play");
			
		}else if(e.getKeyCode() == NativeKeyEvent.VC_DOWN && (e.getModifiers() & NativeKeyEvent.ALT_MASK) != 0) { //Alt + arrow downt to stop playing
			System.out.println("Stop");
		}	
		
	}
	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}