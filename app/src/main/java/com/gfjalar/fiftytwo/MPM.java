package com.gfjalar.fiftytwo;


import java.util.Vector;


public class MPM {
    private int mSampleRate;
    private double mSmallCutoff;
    private double mCutoff;
    private double mLowerPitchCutoff;

    public MPM(int sampleRate, double smallCutoff, double cutoff, double lowerPitchCutoff) {
        mSampleRate = sampleRate;
        mSmallCutoff = smallCutoff;
        mCutoff = cutoff;
        mLowerPitchCutoff = lowerPitchCutoff;
    }

    private double[] getNSDF(short[] buffer) {
        double[] nsdf = new double[buffer.length];
        for (int tau = 0; tau < buffer.length; tau++) {
            double acf = 0, m = 0;
            for (int i = 0; i + tau < buffer.length; i++) {
                acf += buffer[i] * buffer[i + tau];
                m += buffer[i] * buffer[i] + buffer[i + tau] * buffer[i + tau];
            }
            nsdf[tau] = 2 * acf / m;
        }
        return nsdf;
    }

    private int[] getMaxima(double[] nsdf) {
        Vector<Integer> maxima = new Vector<Integer>();

        int pos = 0, maxPos = 0;

        while (pos < (nsdf.length - 1) / 3.0 && nsdf[pos] > 0.0) {
            pos++;
        }

        while (pos < nsdf.length - 1 && nsdf[pos] <= 0.0) {
            pos++;
        }

        pos = pos == 0 ? 1 : pos;

        while (pos < nsdf.length - 1) {
            if (nsdf[pos] > nsdf[pos - 1] && nsdf[pos] >= nsdf[pos + 1]) {
                if (maxPos == 0 || nsdf[pos] > nsdf[maxPos]) {
                    maxPos = pos;
                }
            }
            pos++;
            if (pos < nsdf.length - 1 && nsdf[pos] <= 0.0) {
                if (maxPos > 0) {
                    maxima.add(maxPos);
                    maxPos = 0;
                }
                while (pos < nsdf.length - 1 && nsdf[pos] <= 0.0) {
                    pos++;
                }
            }
        }

        if (maxPos > 0) {
            maxima.add(maxPos);
        }

        int[] result = new int[maxima.size()];
        for (int i = 0; i < maxima.size(); i++) {
            result[i] = maxima.elementAt(i);
        }
        return result;
    }

    private boolean isZero(double value) {
        return value >= -0.001 && value <= 0.001;
    }

    private Point estimateMaximum(double[] nsdf, int tau) {
        double delta = nsdf[tau - 1] - nsdf[tau + 1],
                bottom = 2 * nsdf[tau + 1] + nsdf[tau - 1] - 2 * nsdf[tau];
        if (isZero(bottom)) {
            return new Point(tau, nsdf[tau]);
        }
        return new Point(tau + delta / bottom, nsdf[tau] - delta * delta / (4 * bottom));
    }

    public double detectPitch(short[] buffer) {
        Vector<Point> estimations = new Vector<Point>();

        double[] nsdf = getNSDF(buffer);
        int[] maxima = getMaxima(nsdf);

        double highestAmp = Double.MIN_VALUE;

        for (int i = 0; i < maxima.length; i++) {
            int tau = maxima[i];

            highestAmp = Math.max(highestAmp, nsdf[tau]);

            if (nsdf[tau] > mSmallCutoff) {
                Point maximum = estimateMaximum(nsdf, tau);
                estimations.add(maximum);
                highestAmp = Math.max(highestAmp, maximum.y);
            }
        }

        if (estimations.size() == 0) {
            return 0.0;
        }

        double actualCutoff = mCutoff * highestAmp, period = 0.0;

        for (int i = 0; i < estimations.size(); i++) {
            Point estimation = estimations.elementAt(i);
            if (estimation.y >= actualCutoff) {
                period = estimation.x;
                break;
            }
        }

        double pitchEstimate = mSampleRate / period;

        if (pitchEstimate > mLowerPitchCutoff) {
            return pitchEstimate;
        }

        return 0.0;
    }

    private class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}