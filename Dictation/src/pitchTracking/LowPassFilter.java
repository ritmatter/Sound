package pitchTracking;
//DETERMINE A CORRECT RC VALUE
//TEST THE FREQUENCIES VIA PRINTING TO SEE WHAT'S GOING ON

/*
 * Implements a low-pass filter for a discrete array of sound values
 */
public class LowPassFilter {
	
	public double SAMPLERATE;
	public final double DT; //the time between samples
	
	public double CUTOFF_FREQ;	//cutoff frequency for low-pass filter
	public final double RC;	//the value RC of the circuit
	
	public final double ALPHA ;	//the alpha smoothing constant
										//smaller alpha means stronger low-pass filter
	public double[] samples;
	
	public LowPassFilter(double[] samples, double samplerate, double cutoff)
	{
		this.CUTOFF_FREQ = cutoff;
		this.SAMPLERATE = samplerate;
		this.samples = samples;
		
		DT = 1.0/SAMPLERATE;
		RC = 1.0/(2 * (float) Math.PI * CUTOFF_FREQ);
		ALPHA = DT/(RC + DT);
	}
	
	/*
	 * Low-pass filters the sound samples
	 */
	public double[] filter()
	{
		System.out.println("SAMPLERATE is " + SAMPLERATE);
		System.out.println("Manual division gives " + 1.0/44100.0);
		System.out.println("DT is " + DT);
		System.out.println("CUTOFF_FREQ is " + CUTOFF_FREQ);
		System.out.println("RC is " + RC);
		System.out.println("ALPHA is " + ALPHA);
		double[] filteredSamples = new double[samples.length];
		filteredSamples[0] = samples[0];
		
		int finish = samples.length-1;
		int i;
		for (i = 1; i < finish; i++)
		{
			filteredSamples[i] = filteredSamples[i-1] + ALPHA * (samples[i] - filteredSamples[i-1]);
		}
		
		samples = filteredSamples;
		return samples;
	}
}