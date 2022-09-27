package wolf_vision;

import org.opencv.core.*;
import org.opencv.imgproc.*;

public class ImagePrep {
	private final int IMG_WIDTH = 200;
	private final int IMG_HEIGHT = 200;
	private int slice_size = 0;
	private int id_width, id_height = 0;
	private double max_width, max_height = 0;
	private Size image_size;
	
	public Mat inputImg = new Mat();
	public Mat resultImg = new Mat();
	public Mat block = new Mat();
	/**
	 * Constructor of this class
	 * @param slice_size, width of each slices
	 */
	public ImagePrep(int slice_size) {
		this.slice_size = slice_size;
		//this.inputImg = new Mat(ImageSize,CvType.);
	}
	// set input image
	public void set_frame(Mat frame) {
		set_frame(frame, true);
	}
	public void set_frame(Mat frame, boolean resize) {
		if (resize) {
			System.out.println("resize1");
			try {
				Imgproc.resize(frame, this.inputImg, new Size(IMG_WIDTH, IMG_HEIGHT));
			} catch (Exception e){
				e.printStackTrace();
			}
			System.out.println("resize2");
		} else {
			this.inputImg = frame;
		}
		this.image_size = frame.size();
	}
	
	public void slice(int num_x, int num_y) {
		this.max_width = this.image_size.width / num_x;
		this.max_height = this.image_size.height / num_y;
	}
	//https://forum.opencv.org/t/opencv-kmeans-java/285/3
	public void kmeans(int n_clusters) {
		Mat img = this.inputImg;
		Mat data = img.reshape(1,(int)img.total());
		Mat data_32f = new Mat();
		data.convertTo(data_32f, CvType.CV_32F);
		Mat bestLabels = new Mat();
		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT,100,1);
		int attempts = 1;
		int flags = Core.KMEANS_PP_CENTERS;
		Mat center = new Mat();
		System.out.println(img.size());

		Core.kmeans(data_32f, n_clusters, bestLabels, criteria, attempts, flags, center);
		Mat draw = new Mat((int)img.total(),1,CvType.CV_32FC3);
		Mat colors = center.reshape(3,n_clusters);
		
		for (int i = 0; i < n_clusters; i++) {
			Mat mask = new Mat();
			Core.compare(bestLabels,new Scalar(i), mask, Core.CMP_EQ);
			Mat col = colors.row(i);
			double d[] = col.get(0,0);
			draw.setTo(new Scalar(d[0],d[1],d[2]), mask);
		}
		
		draw = draw.reshape(3, img.rows());
		draw.convertTo(draw, CvType.CV_8U);
		this.resultImg = draw;
	}
	
	public void debug() {
		System.out.println(this.image_size.height);
		System.out.println(this.inputImg.type());
	}
}
