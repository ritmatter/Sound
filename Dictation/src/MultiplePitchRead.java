import java.io.*;

import edu.emory.mathcs.jtransforms.fft.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class MultiplePitchRead {
	public float sampleRate;
	public float frameRate;
    public final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767
    public double sampleTime = 0.3; //how much time between checking for most important note, seconds
    public final double MAX_FREQ = 5000; //highest note to be tolerated
	
	public MultiplePitchRead(float sample, float frame) {
		this.sampleRate = sample;
		this.frameRate = frame;
	}
	
	public double[] simpleFilter(double[] maxFreqs) {
		double[] filteredFreqs = new double[maxFreqs.length];
		
		if (filteredFreqs[0] > MAX_FREQ) {
			filteredFreqs[0] = 0;
		}
		
		for (int i = 1; i < maxFreqs.length; i++) {
			if (maxFreqs[i] > MAX_FREQ) {
				maxFreqs[i] = maxFreqs[i-1];
			}
			filteredFreqs[i] = maxFreqs[i]; 
		}
		filteredFreqs[0] = maxFreqs[0];
		return filteredFreqs;
	}
	
	public double[] getMaxFreqData(double[][] soundBlocks) {
		double[] maxFreqs = new double[soundBlocks.length];
		for (int i = 0; i < soundBlocks.length; i++) {
			maxFreqs[i] = getMaxFreq(soundBlocks[i]);
		}
		return maxFreqs;
	}
	
	public double getMaxFreq(double[] sound) {
		DoubleFFT_1D fftDo = new DoubleFFT_1D(sound.length);
		double[] fft = new double[sound.length * 2];
		System.arraycopy(sound, 0, fft, 0, sound.length);
		fftDo.realForwardFull(fft);
		

	    int max_i = -1;
	    double max_fftval = -1;
	    for (int i = 0; i < fft.length; i += 2) { // we are only looking at the half of the spectrum

         // complex numbers -> vectors, so we compute the length of the vector, which is sqrt(realpart^2+imaginarypart^2)
         double vlen = Math.sqrt(fft[i] * fft[i] + fft[i + 1] * fft[i + 1]);

                if (max_fftval < vlen) {
                    // if this length is bigger than our stored biggest length
                    max_fftval = vlen;
                    max_i = i;
                }
            }

            double dominantFreq = ((max_i / 2.0) / fft.length) * sampleRate * 2;
            return dominantFreq;
	}
	
	public double[][] getSamples(File soundFile) {
		double[] totalSamples = read(soundFile);
		int blockSize = (int) (sampleTime * sampleRate);

		int blocks = (int) (totalSamples.length/blockSize);
		double[][] sampleBlocks = new double[blocks][blockSize];
		
		int index = 0;
		for (int i = 0; i < blocks; i++) {
			double[] block = new double[blockSize];
			System.arraycopy(totalSamples, index, block, 0, blockSize);
			
			sampleBlocks[i] = block;
			index += blockSize;
		}
		
		return sampleBlocks;
	}
	
	public void detectPitches(double[][] soundBlocks) {
		
		for (int i = 0; i < soundBlocks.length; i++) {
			detectPitch(soundBlocks[i]);
		}
	}
	
	/*
	 * A method that gives a wav file as an array of audio bytes
	 * @param the soundfile
	 * @return the audio bytes
	 */
	public byte[] readByte(File soundFile) throws IOException {
		int read;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(soundFile));
			
		byte[] buff = new byte[1024];
		while ((read = in.read(buff)) > 0) {
				out.write(buff, 0, read);
			}
		
		out.flush();
		in.close();
		byte[] audioBytes = out.toByteArray();
			
		return audioBytes;
	}
	
	/*
	 * A method that takes a wav file and returns it as a double array
	 * @param the soundfile
	 * @return an array of doubles
	 */
	public double[] read(File soundfile) {
	        byte[] data;
			try {
				data = readByte(soundfile);
			
	        int N = data.length;
	        double[] d = new double[N/2];
	        for (int i = 0; i < N/2; i++) {
	            d[i] = ((short) (((data[2*i+1] & 0xFF) << 8) + (data[2*i] & 0xFF))) / ((double) MAX_16_BIT);
	        }
	        return d;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
	    }
	 
	 
	public void detectPitch(double[] input) {
		DoubleFFT_1D fftDo = new DoubleFFT_1D(input.length);
		double[] fft = new double[input.length * 2];
		System.arraycopy(input, 0, fft, 0, input.length);
		fftDo.realForwardFull(fft);
		

	    int max_i = -1;
	    double max_fftval = -1;
	    for (int i = 0; i < fft.length; i += 2) { // we are only looking at the half of the spectrum

         // complex numbers -> vectors, so we compute the length of the vector, which is sqrt(realpart^2+imaginarypart^2)
         double vlen = Math.sqrt(fft[i] * fft[i] + fft[i + 1] * fft[i + 1]);

                if (max_fftval < vlen) {
                    // if this length is bigger than our stored biggest length
                    max_fftval = vlen;
                    max_i = i;
                }
            }

            double dominantFreq = ((max_i / 2.0) / fft.length) * sampleRate * 2;
            System.out.println("Dominant frequency: " + dominantFreq);
	}
	
	public static void main(String[] args) throws IOException
	{
		if (args.length != 1)
		{
			printUsageAndExit();
		}

		String strFilename = args[0];
		
		File soundFile = new File(strFilename);
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		AudioFormat	audioFormat = audioInputStream.getFormat();	
		MultiplePitchRead pitchReader = new MultiplePitchRead(audioFormat.getSampleRate(), audioFormat.getFrameRate());
		
		double[][] soundBlocks = pitchReader.getSamples(soundFile);
		double[] maxFreqs = pitchReader.getMaxFreqData(soundBlocks);
		double[] filteredFreqs = pitchReader.simpleFilter(maxFreqs);
		
		for (int i = 0; i < filteredFreqs.length; i++) {
			System.out.println(filteredFreqs[i]);
		}
		

		/*
		  There is a bug in the jdk1.3/1.4.
		  It prevents correct termination of the VM.
		  So we have to exit ourselves.
		*/
		System.exit(0);
	}


	private static void printUsageAndExit()
	{
		out("SimpleAudioPlayer: usage:");
		out("\tjava SimpleAudioPlayer <soundfile>");
		System.exit(1);
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}


