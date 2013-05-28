import java.io.*;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SineFFT {
    private final static int SAMPLERATE = 8000;

    public static void main(String[] args) {
        int frequency = 3560; // freq of our sine wave
        double lengthInSecs = 1;
        int samplesNum = (int) Math.round(lengthInSecs * SAMPLERATE);

        System.out.println("Samplesnum: " + samplesNum);

        double[] audioData = new double[samplesNum];
        int samplePos = 0;

        // http://en.wikibooks.org/wiki/Sound_Synthesis_Theory/Oscillators_and_Wavetables
        for (double phase = 0; samplePos < lengthInSecs * SAMPLERATE && samplePos < samplesNum; phase += (2 * Math.PI * frequency) / SAMPLERATE) {
            audioData[samplePos++] = Math.sin(phase);

            if (phase >= 2 * Math.PI)
                phase -= 2 * Math.PI;
        }

        // we compute the fft of the whole sine wave
        DoubleFFT_1D fft = new DoubleFFT_1D(samplesNum);

        // we need to initialize a buffer where we store our samples as complex numbers. first value is the real part, second is the imaginary.
        double[] fftData = new double[samplesNum * 2];
        for (int i = 0; i < samplesNum; i++) {
            // copying audio data to the fft data buffer, imaginary part is 0
            fftData[2 * i] = audioData[i];
            fftData[2 * i + 1] = 0;
        }

        // calculating the fft of the data, so we will have spectral power of each frequency component
        // fft resolution (number of bins) is samplesNum, because we initialized with that value
        fft.complexForward(fftData);

        try {
            // writing the values to a txt file
            BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "UTF-8"));

            int max_i = -1;
            double max_fftval = -1;
            for (int i = 0; i < fftData.length; i += 2) { // we are only looking at the half of the spectrum
                double hz = ((i / 2.0) / fftData.length) * SAMPLERATE;
                outputStream.write(i + ".\tr:" + Double.toString((Math.abs(fftData[i]) > 0.1 ? fftData[i] : 0)) + " i:" + Double.toString((Math.abs(fftData[i + 1]) > 0.1 ? fftData[i + 1] : 0)) + "\t\t" + hz + "hz\n");

                // complex numbers -> vectors, so we compute the length of the vector, which is sqrt(realpart^2+imaginarypart^2)
                double vlen = Math.sqrt(fftData[i] * fftData[i] + fftData[i + 1] * fftData[i + 1]);

                if (max_fftval < vlen) {
                    // if this length is bigger than our stored biggest length
                    max_fftval = vlen;
                    max_i = i;
                }
            }

            double dominantFreq = ((max_i / 2.0) / fftData.length) * SAMPLERATE;
            System.out.println("Dominant frequency: " + dominantFreq + "hz (output.txt line no. " + max_i + ")");

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}