package wolf_vision;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.features2d.*;

public class Buoy extends ImagePrep {
	Mat template_img = new Mat();
	//FastFeatureDetector fd = FastFeatureDetector.create();
	SIFT template_kp_detector = SIFT.create();
	SIFT target_kp_detector = SIFT.create();
	MatOfKeyPoint template_kp = new MatOfKeyPoint();
	MatOfKeyPoint target_kp	= new MatOfKeyPoint();
	Mat template_des = new Mat();
	Mat target_des = new Mat();
	BFMatcher matcher = BFMatcher.create();
	
	public void setTemplate(Mat template) {
		this.template_img = template;
		Mat gray_img = new Mat();
		Imgproc.cvtColor(template, gray_img, Imgproc.COLOR_BGR2GRAY);
		this.template_kp_detector.detect(gray_img, this.template_kp);
		this.template_kp_detector.compute(gray_img, this.template_kp, this.template_des);
		System.out.print("Template Keypoints: ");
		System.out.println(this.template_kp.toString());
	}
	
	public void siftSetTarget(Mat img) {
		Mat gray_img = new Mat();
		Imgproc.cvtColor(img, gray_img, Imgproc.COLOR_BGR2GRAY);
		this.target_kp_detector.detect(gray_img, this.target_kp);
		this.target_kp_detector.compute(gray_img, this.target_kp, this.target_des);
		System.out.print("Target Keypoints: ");
		System.out.println(this.target_kp.toString());
	}
	public void compute() {
		List<MatOfDMatch> matches = new ArrayList<>();
		matcher.knnMatch(template_des,target_des, matches, 5);
		ArrayList<Float> distances = new ArrayList<>();
		System.out.print(matches);
		
		
	}

	public Mat drawKP(Mat img, int select) {
		Mat drawnImage = new Mat();
		if (select == 0) {
			Features2d.drawKeypoints(img, template_kp, drawnImage);
		} else if (select == 1) {
			Features2d.drawKeypoints(img, target_kp, drawnImage);			
		} else {
			//Features2d.drawKeypoints(img, descriptor, drawnImage);
		}
		return drawnImage;
	}
}