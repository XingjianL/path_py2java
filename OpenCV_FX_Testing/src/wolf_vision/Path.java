package wolf_vision;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Path extends ImagePrep {
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
			//System.out.println(PCA_output.get(0).dump());
			//System.out.println(PCA_output.get(1).dump());
			//System.out.println(PCA_output.get(2).dump());
		}
	}
}
