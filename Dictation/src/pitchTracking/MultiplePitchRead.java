package pitchTracking;

import java.io.*;

import edu.emory.mathcs.jtransforms.fft.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MultiplePitchRead {
	
	//local variables
	public float sampleRate;	//44,100Hz by default, determined by Java package
	public float frameRate;		//also determined by Java package
	public String graphName;	//name for the graph 

    public final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767
    public final float MAX_FREQ = 1000; //highest note to be tolerated
    public static int BLOCKSIZE = 4410; //effectively FFT's 0.1 seconds of sound
    public static int BLOCK_INC = BLOCKSIZE/5;	//how much to slide between FFT's
	
	public MultiplePitchRead(float sample, float frame, String graphName) {
		this.sampleRate = sample;
		this.frameRate = frame;
		this.graphName = graphName;
	}
	
	/*
	 * Performs the Hamming Window
	 * Returns the given value of the Window (0 through 1) at a given time
	 */
	public double hamming(double t)
	{
		double window = 0.54 - 0.46 * Math.cos((2 * Math.PI * t)/BLOCKSIZE);
		//System.out.println("Value is " + window);
		return window;
	}
	
	/*
	 * Takes a block of sound samples from the main string of sound samples
	 * Windows the samples
	 * Returns the windowed samples
	 */
	public double[] window(double[] samples)
	{
		int finish = samples.length;
		double[] windowedSamples = new double[finish];
		int i;
		for (i = 0; i < finish; i++)
		{
			windowedSamples[i] = samples[i] * hamming(i);
		}
		return windowedSamples;
	}
	
	/*
	 * Returns a double array of each sample in a wav file
	 * Takes the sound file
	 * Each sample is a sine wave sample (-1 to 1)
	 */
	public double[] getSampleData(File soundFile)
	{
		return read(soundFile);
	}
	
	/*
	 * Takes a double of sound samples (should be one sliding block)
	 * Performs FFT on the block 
	 * Returns the maximum frequency
	 */
	public double maxFrequency(double[] sound) {
		DoubleFFT_1D fftDo = new DoubleFFT_1D(sound.length);
		double[] fft = new double[sound.length * 2];
		System.arraycopy(sound, 0, fft, 0, sound.length);
		fftDo.realForwardFull(fft);
		
	    int max_i = -1;
	    double max_fftval = -1;
	    for (int i = 0; i < fft.length; i += 2) { // we are only looking at the half of the spectrum

	    	// complex numbers -> vectors, so we compute the length of the vector, which is sqrt(realpart^2+imaginarypart^2)
	    	double vlen = Math.sqrt(fft[i] * fft[i] + fft[i + 1] * fft[i + 1]);
	    	
	    	//
         	double currFreq = ((i / 2.0) / fft.length) * sampleRate * 2;
         	if (currFreq != 0)
         		{
         			if (max_fftval < vlen) 
         			{
         				// if this length is bigger than our stored biggest length
         				max_fftval = vlen;
         				max_i = i;
         			}
         		}
            }

            double dominantFreq = ((max_i / 2.0) / fft.length) * sampleRate * 2;
            return dominantFreq;     
	}
	
	/*
	 * Returns an array of FFT frequencies
	 * Takes an array of samples from a sound file
	 * Takes sliding samples of the array with a Hanning Window applied
	 * For each block in the sliding set, it windows them, ffts them, 
	 * and adds a frequency to the return array
	 */
	public double[] frequencyAnalysis(double[] samples)
	{		
	
		//the number of sliding samples
		int blocks = (samples.length - BLOCKSIZE)/BLOCK_INC + 1; 
		//System.out.println("Number of blocks is " + blocks);
		
		//an array to hold the frequencies
		double[] frequencies = new double[blocks];
		
		double[] currSamp = new double[BLOCKSIZE];
		int finish = BLOCK_INC * (blocks - 1);
		//System.out.println("Sample size is " + samples.length);
		//System.out.println("Finish is " + finish);
		
		System.out.println("Generating FFT Data...");
		int i;	//keeps track of the current sample size
		for (i = 0; i < finish; i += BLOCK_INC)
		{
			//System.out.println("i is " + i);
			System.arraycopy(samples, i, currSamp, 0, BLOCKSIZE);
			frequencies[i/BLOCK_INC] = maxFrequency(currSamp);
		}
		return frequencies;
	}
	
	/*
	 * Gets the array of max frequencies for a sound file
	 * Gets the samples and performs frequency analysis
	 * Simply calls the other the other functions above
	 * Returns the array of frequencies
	 */
	public double[] getFrequencyData(File soundfile)
	{
		return frequencyAnalysis(getSampleData(soundfile));
	}
	
	/*
	 * Graphs a double array
	 * Takes the filename, title,
	 */
	public void graph(String filename, double[] data, String yaxis, String xaxis)
	{
//		 Create a chart of the XY frequencies
		 XYSeries series = new XYSeries("Data");
		 int i;
		 for (i = 0; i < data.length; i++)
		 {
			 series.add(i, data[i]);
		 }
		 
		 // Add the series to your data set
		 XYSeriesCollection dataset = new XYSeriesCollection();
		 dataset.addSeries(series);
		 
		 // Generate the graph
		 JFreeChart chart = ChartFactory.createXYLineChart(
		 filename, // Title
		 xaxis, // x-axis Label
		 yaxis, // y-axis Label
		 dataset, // Dataset
		 PlotOrientation.VERTICAL, // Plot Orientation
		 true, // Show Legend
		 true, // Use tooltips
		 false // Configure chart to generate URLs?
		 );
		 try {
		 ChartUtilities.saveChartAsJPEG(new File("../images/" + filename + ".jpg"), chart, 500, 300);
		 } catch (IOException e) {
			 System.err.println("Problem occurred creating chart.");
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
	 

	
	
	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			printUsageAndExit();
		}

		String strFilename = args[0];
		String imgFilename = args[1];
		
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
		MultiplePitchRead pitchReader = new MultiplePitchRead(audioFormat.getSampleRate(), audioFormat.getFrameRate(), imgFilename);
		
		//get the samples
		double[] samples = pitchReader.getSampleData(soundFile);
		
		//instantiate the frequencies without filtering
		
		//filter the sound
		//filter.filter();
		//samples = filter.samples;
		
		//instantiate the frequencies
		double[] frequencies = pitchReader.frequencyAnalysis(samples);
		System.out.println("Graphing FFT Data Before Filtering...");
		pitchReader.graph(pitchReader.graphName, frequencies, "Frequency", "Index");
		
		//instantiate the frequencies after filtering
		LowPassFilter filter = new LowPassFilter(samples, pitchReader.sampleRate, pitchReader.MAX_FREQ);
		samples = filter.filter();
		
		frequencies = pitchReader.frequencyAnalysis(samples);
		System.out.println("Graphing FFT Data After Filtering...");
		pitchReader.graph(pitchReader.graphName + " Filtered", frequencies, "Frequency", "Index");

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



