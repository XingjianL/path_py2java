package wolf_vision;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Path extends ImagePrep {
	private final int PATH_COLOR_LOW = 60;
	private final int PATH_COLOR_HIGH = 85;
	private final int PATH_WIDTH_LOW = 60;
	private final int PATH_WIDTH_HIGH = 400;
	private final double[] FORWARD = {0,-1};
	
	public double[] mean = new double[2]; // [x,y]
	public double[] vectors = new double[4];
	public double[] values = new double[2];
	
	public void iteratePathBinaryPCA(Mat colored_image) {
		Mat gray_image = new Mat();
		Imgproc.cvtColor(colored_image, gray_image, Imgproc.COLOR_BGR2GRAY);
		List<Integer> all_colors = uniqueColor(gray_image);
		
		for (int color = 0; color < all_colors.size(); color++) {
			Mat current_bin_image = new Mat();
			Core.inRange(gray_image, new Scalar(all_colors.get(color)), new Scalar(all_colors.get(color)), current_bin_image);;
			MatOfPoint on_points = cvtBinaryToPoints(current_bin_image);
			System.out.println(on_points.toArray().length);
			List<Mat> PCA_output = binaryPCA(on_points);
			PCA_output.get(0).get(0,0, this.mean);
			PCA_output.get(1).get(0,0, this.vectors);
			PCA_output.get(2).get(0,0, this.values);
			System.out.println(PCA_output.get(1).dump());
			System.out.println(Arrays.toString(this.vectors));
			//System.out.println(PCA_output.get(1).dump());
			//System.out.println(PCA_output.get(2).dump());
		}
	}
	public Mat iteratePathBinaryPCAAndDraw(Mat colored_image) {
		Mat draw = colored_image.clone();
		Mat gray_image = new Mat();
		Imgproc.cvtColor(colored_image, gray_image, Imgproc.COLOR_BGR2GRAY);
		List<Integer> all_colors = uniqueColor(gray_image);
		
		for (int color = 0; color < all_colors.size(); color++) {
			Mat current_bin_image = new Mat();
			Core.inRange(gray_image, new Scalar(all_colors.get(color)), new Scalar(all_colors.get(color)), current_bin_image);;
			MatOfPoint on_points = cvtBinaryToPoints(current_bin_image);
			System.out.println(on_points.toArray().length);
			List<Mat> PCA_output = binaryPCA(on_points);
			PCA_output.get(0).get(0,0, this.mean);
			PCA_output.get(1).get(0,0, this.vectors);
			PCA_output.get(2).get(0,0, this.values);
			System.out.println(PCA_output.get(1).dump());
			System.out.println(Arrays.toString(this.vectors));
			//System.out.println(PCA_output.get(1).dump());
			//System.out.println(PCA_output.get(2).dump());
			draw = drawPCA(draw);
		}
		return draw;
	}
	
	public Mat drawPCA(Mat input_image) {
		Mat output = input_image.clone();
		Point center = new Point(this.mean[0],this.mean[1]);
		Imgproc.circle(output, center, 3, new Scalar(0,0,255));
		int max = 0;
		if (this.values[1] > this.values[0]) {
			max = 1;
		}
		Point p1 = new Point(center.x + 0.02 * this.vectors[2*max]* this.values[max],
				center.y + 0.02 * this.vectors[2*max+1]* this.values[max]);
		Point p2 = new Point(center.x + 0.02 * this.vectors[2*(1-max)]* this.values[(1-max)],
				center.y + 0.02 * this.vectors[2*(1-max)+1]* this.values[(1-max)]);
		Imgproc.arrowedLine(output, center, p1, new Scalar(255,255,255));
		Imgproc.arrowedLine(output, center, p2, new Scalar(0,255,0));
		return output;
	}
	
}
