import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
 
public class FftTest {
    public static void main(String[] args) {
        double[] input = new double[]{
                0.0176,
                -0.0620,
                0.2467,
                0.4599,
                -0.0582,
                0.4694,
                0.0001,
                -0.2873};
        DoubleFFT_1D fftDo = new DoubleFFT_1D(input.length);
        double[] fft = new double[input.length * 2];
        System.arraycopy(input, 0, fft, 0, input.length);
        fftDo.realForwardFull(fft);
 
        for(double d: fft) {
            System.out.println(d);
        }
    }
}