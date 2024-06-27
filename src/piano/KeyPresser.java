package piano;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;


public class KeyPresser implements Runnable {
    static Robot robot;
    static int[] letters = new int[127];
    static Set<Integer> notesRequiringShift;
    static int previousNote;
    static boolean shiftPressed = false;
    int timing = 8;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

        Integer[] notesRequiringShiftArr = {25, 27, 30, 32, 34, 37, 39, 42, 44, 46, 49, 51, 54, 56, 58, 61, 63, 66, 68, 70, 73, 75, 78, 80, 82};
        notesRequiringShift = new TreeSet<>(Arrays.asList(notesRequiringShiftArr));

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
    }

    public void run() {}

    public void press(int note) {
        int noteKey = letters[note];
        if (noteKey != 0) {
            if (notesRequiringShift.contains(note)) {
                if (!shiftPressed) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    shiftPressed = true;
                }
            } else if (shiftPressed) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
                shiftPressed = false;
            }

            if (previousNote != 0) {
                robot.keyRelease(previousNote);
            }
            robot.delay(timing);
            robot.keyPress(noteKey);
            previousNote = noteKey;
        }
    }

    public void updateTimings(int timing) {
        this.timing = timing;
    }

    public int getTiming() {
        return this.timing;
    }
}

