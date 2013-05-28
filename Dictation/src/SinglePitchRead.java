import java.io.*;

import edu.emory.mathcs.jtransforms.fft.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
/*
 *	SimpleAudioPlayer.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class SinglePitchRead {
	public float sampleRate;
	public float frameRate;
    public final double MAX_16_BIT = Short.MAX_VALUE;     // 32,767

	
	public SinglePitchRead(float sample, float frame) {
		this.sampleRate = sample;
		this.frameRate = frame;
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
		
		try {         
			    // writing the values to a txt file
	            BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "UTF-8"));

	            int max_i = -1;
	            double max_fftval = -1;
	            for (int i = 0; i < fft.length; i += 2) { // we are only looking at the half of the spectrum
	                double hz = ((i / 2.0) / fft.length) * sampleRate;
	                outputStream.write(i + ".\tr:" + Double.toString((Math.abs(fft[i]) > 0.1 ? fft[i] : 0)) + " i:" + Double.toString((Math.abs(fft[i + 1]) > 0.1 ? fft[i + 1] : 0)) + "\t\t" + hz + "hz\n");

	                // complex numbers -> vectors, so we compute the length of the vector, which is sqrt(realpart^2+imaginarypart^2)
	                double vlen = Math.sqrt(fft[i] * fft[i] + fft[i + 1] * fft[i + 1]);

	                if (max_fftval < vlen) {
	                    // if this length is bigger than our stored biggest length
	                    max_fftval = vlen;
	                    max_i = i;
	                }
	            }

	            double dominantFreq = ((max_i / 2.0) / fft.length) * sampleRate * 2;
	            System.out.println("Dominant frequency: " + dominantFreq + "hz (output.txt line no. " + max_i + ")");

	            outputStream.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		
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
		SinglePitchRead pitchReader = new SinglePitchRead(audioFormat.getSampleRate(), audioFormat.getFrameRate());
		
		double[] soundDoubles = pitchReader.read(soundFile);
		pitchReader.detectPitch(soundDoubles);
		
		
		

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



/*** SimpleAudioPlayer.java ***/