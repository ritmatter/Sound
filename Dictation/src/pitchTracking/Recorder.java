package pitchTracking;
import java.io.IOException;
import java.io.File;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioFileFormat;

public class Recorder extends Thread {
	
	private TargetDataLine line;
	private AudioFileFormat.Type targetType;
	private AudioInputStream audioInputStream;
	private File outputFile;
	
	public Recorder(TargetDataLine tLine, AudioFileFormat.Type type, File file) {
		line = tLine;
		audioInputStream = new AudioInputStream(line);
		targetType = type;
		outputFile = file;
	}
	
	/*
	 * a method that starts the recording
	 */
	public void start() {
		/*
		 * this portion starts the TargetDataLine.  This means that we want to read 
		 * data from it
		 */
		line.start();
		
		/*
		 * We also need to start the thread.  This call will make the run() method get
		 * called.  In run, the data is actually read from the line
		 */
		super.start();
	}
	
	/** Stops the recording.

    Note that stopping the thread explicitely is not necessary. Once
    no more data can be read from the TargetDataLine, no more data
    be read from our AudioInputStream. And if there is no more
    data from the AudioInputStream, the method 'AudioSystem.write()'
    (called in 'run()' returns. Returning from 'AudioSystem.write()'
    is followed by returning from 'run()', and thus, the thread
    is terminated automatically.

    It's not a good idea to call this method just 'stop()'
    because stop() is a (deprecated) method of the class 'Thread'.
    And we don't want to override this method.
*/
	public void stopRecording() {
		line.stop();
		line.close();
	}
	
	/** Main working method.
    You may be surprised that here, just 'AudioSystem.write()' is
    called. But internally, it works like this: AudioSystem.write()
    contains a loop that is trying to read from the passed
    AudioInputStream. Since we have a special AudioInputStream
    that gets its data from a TargetDataLine, reading from the
    AudioInputStream leads to reading from the TargetDataLine. The
    data read this way is then written to the passed File. Before
    writing of audio data starts, a header is written according
    to the desired audio file type. Reading continues untill no
    more data can be read from the AudioInputStream. In our case,
    this happens if no more data can be read from the TargetDataLine.
    This, in turn, happens if the TargetDataLine is stopped or closed
    (which implies stopping). (Also see the comment above.) Then,
    
    the file is closed and 'AudioSystem.write()' returns.
*/
	public void run() {
		try {
			AudioSystem.write(audioInputStream, targetType, outputFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printUsageAndExit() {
		out("Recorder: usage: ");
		out("\tjava Recorder -h");
		out("\tjava Recorder <audioFile>");
		System.exit(0);
	}
	
	private static void out(String strMessage) {
		System.out.println(strMessage);
	}
	
	public static void main(String[] args) {
		
		if (args.length != 1 || args[0].equals("-h")) {
			printUsageAndExit();
		}
		
		/*
		 * We have made sure that there is only one command line argument.  This is taken as the 
		 * filename of the soundfile to store to
		 */
		
		String strFilename = args[0];
		File outputFile = new File(strFilename);
		
		/*
		 * For simplicity, the audio format used for recording is hardcoded here.  
		 * We use PCM 44.1 kHz, 16 bit signed, stereo
		 */
		AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				44100.0F, 16, 2, 4, 44100.0F, false);
		
		/*
		 * Now, we try to get a TargetDataLine.  The TargetDataLine is used later to read 
		 * audio data from it.  If requesting the line was successful, we open it.
		 * (IMPORTANT!
		 */
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine targetDataLine = null;
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		}
		catch (LineUnavailableException e) {
			
			out("Unable to get a recording line");
			e.printStackTrace();
			System.exit(1);
		}
		
		/*
		 * Again for simplicity, we've hardcoded the audio file type, too
		 */
		AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;
		
		/*
		 * Now, we are creating the Recorder object.  It contains the logic of starting
		 * and stopping the recording, reading audio data from the TargetDataLine
		 * and writing the data to a file
		 */
		Recorder recorder = new Recorder(
				targetDataLine,
				targetType,
				outputFile);
		
		/*
		 * We are waiting for the user to press ENTER to start the recording. (You might
		 * find it inconvenient if recording starts immediately).
		 */
		out("Press ENTER to start the recording.");
		try {
			System.in.read();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * Here, the recording actually starts.
		 */
		recorder.start();
		out("Recording...");
		
		/*
		 * And now, we are waiting again for the user to press ENTER,
		 * this time to signal that the recording should be stopped.
		 */
		out("Press ENTER to stop the recording.");
		try {
			System.in.read();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * Here, the recording is actually stopped.
		 */
		recorder.stopRecording();
		out("Recording stopped.");
	}
}