package com.gfjalar.fiftytwo;


import android.graphics.Color;


public class Settings {
    public int mColor;
    public String mFrequencyString;
    public String mDifferenceString;
    public String mNoteString;

    private int red = Color.rgb(200, 90, 0);
    private int green = Color.rgb(90, 200, 90);
    private int blue = Color.rgb(0, 90, 200);

    private static double[] noteFrequencies = new double[]{
            16.352, 17.324, 18.354, 19.445, 20.602, 21.827, 23.125, 24.500, 25.957, 27.500,
            29.135, 30.868, 31.7855
    };
    private static String[] noteNames = new String[]{
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };

    public Settings(double pitch) {
        int octave = 0, multi = 1, note = 1;
        while(multi * noteFrequencies[noteFrequencies.length - 1] <= pitch && multi < 13) {
            octave++;
            multi *= 2;
        }
        while(note != noteFrequencies.length - 1 && noteFrequencies[note] * multi < pitch) {
            note++;
        }
        double toLower = pitch - noteFrequencies[note - 1] * multi,
                toHigher = pitch - noteFrequencies[note] * multi;
        if(toLower < -toHigher || note == noteNames.length) {
            set(pitch, noteNames[note - 1], octave, toLower);
        } else {
            set(pitch, noteNames[note], octave, toHigher);
        }
    }

    private void set(double frequency, String note, int octave, double difference) {
        mColor = formatColor(difference);
        mFrequencyString = formatFrequency(frequency);
        mDifferenceString = formatDifference(difference);
        mNoteString = formatNote(note, octave);
    }

    private int slide(int from, int to, int index, int scale) {
        if(index <= 0) {
            return from;
        }
        if(index >= scale) {
            return to;
        }
        double f = (double)index / (double)scale;
        return Color.rgb(
                (int)(Color.red(from) + f * (Color.red(to) - Color.red(from))),
                (int)(Color.green(from) + f * (Color.green(to) - Color.green(from))),
                (int)(Color.blue(from) + f * (Color.blue(to) - Color.blue(from)))
        );
    }

    private int formatColor(double difference) {
        int fixed = (int)(difference * 10);
        if(fixed < 0) {
            return slide(green, blue, -fixed, 100);
        } else {
            return slide(green, red, fixed, 100);
        }
    }

    private String formatFrequency(double frequency) {
        return String.format("%.3f Hz", frequency);
    }

    private String formatDifference(double difference) {
        if(difference < 0.0) {
            return String.format("%.3f cent", difference);
        }
        return String.format("+%.3f cent", difference);
    }

    private String formatNote(String note, int octave) {
        return String.format("%s%s", note, toSubscript(octave));
    }

    private String toSubscript(int value) {
        if(value == 0) {
            return "\u2080";
        }
        StringBuilder builder = new StringBuilder();
        while(value != 0) {
            switch(value%10) {
                case 0:
                    builder.append('\u2080');
                    break;
                case 1:
                    builder.append('\u2081');
                    break;
                case 2:
                    builder.append('\u2082');
                    break;
                case 3:
                    builder.append('\u2083');
                    break;
                case 4:
                    builder.append('\u2084');
                    break;
                case 5:
                    builder.append('\u2085');
                    break;
                case 6:
                    builder.append('\u2086');
                    break;
                case 7:
                    builder.append('\u2087');
                    break;
                case 8:
                    builder.append('\u2088');
                    break;
                case 9:
                    builder.append('\u2089');
                    break;
            }
            value /= 10;
        }
        return builder.reverse().toString();
    }
}