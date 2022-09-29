package wolf_vision;

import org.opencv.core.*;
import org.opencv.imgproc.*;

public class ImagePrep {
	private int IMG_WIDTH = 200;
	private int IMG_HEIGHT = 200;
	private int id_width, id_height = 0;
	private int max_width_id, max_height_id = 0;
	private int block_width, block_height = 0;
	private Size image_size;
	
	public Mat inputImg = new Mat();
	public Mat processImg = new Mat();
	public Mat resultImg = new Mat();
	public Mat block = new Mat();
	public Rect ROI = new Rect();
	/**
	 * Constructor of this class
	 * @param slice_size width of each slices
	 * @param resize_width configures width of the inputImg
	 * @param resize_height configures height of the inputImg
	 */
	public ImagePrep(int resize_width, int resize_height) {
		this.IMG_WIDTH = resize_width;
		this.IMG_HEIGHT = resize_height;
		//this.inputImg = new Mat(ImageSize,CvType.);
	}
	/**
	 * Default constructor
	 */
	public ImagePrep() {
	}
	
	public void set_input_to_result() {
		this.inputImg = this.resultImg;
	}
	
	/**
	 * auto resizes inputImg to IMG_WIDTH and IMG_HEIGHT
	 * @param frame inputImg
	 */
	public void set_frame(Mat frame) {
		set_frame(frame, true);
	}
	
	/**
	 * optional resize, set the inputImg
	 * @param frame inputImg
	 * @param resize boolean
	 */
	public void set_frame(Mat frame, boolean resize) {
		if (resize) {
			//System.out.println("resize1");
			try {
				Imgproc.resize(frame, this.inputImg, new Size(IMG_WIDTH, IMG_HEIGHT));
			} catch (Exception e){
				e.printStackTrace();
			}
			//System.out.println("resize2");
		} else {
			this.inputImg = frame;
		}
		this.image_size = this.inputImg.size();
	}
	
	private boolean checkBounds() {
		if (max_width_id < id_width || max_height_id < id_height) {
			return false;
		}
		return true;
	}
	
	private void set_block() {
		if (checkBounds()) {
			this.ROI = new Rect(block_width*id_width,block_height*id_height,block_width,block_height);
			this.block = new Mat(this.inputImg, this.ROI).clone();
		} else {
			System.out.println("ImagePrep:set_block(): Block out of bounds");
		}
	}
	//private void replace_block(Mat small_img) {
	//	small_img.copyTo(this.processImg .ROI);
	//}
	
	/**
	 * Configures the coordinates of the top left corner of each block using number of desired blocks
	 * @param num_x number of blocks in the horizontal direction
	 * @param num_y number of blocks in the vertical direction
	 */
	public void slice_by_number(int num_x, int num_y) {
		this.block_width = (int)this.image_size.width / num_x;
		this.block_height = (int)this.image_size.height / num_y;
		this.max_width_id = num_x - 1;
		this.max_height_id = num_y - 1;
		this.IMG_WIDTH = num_x * this.block_width;
		this.IMG_HEIGHT = num_y * this.block_height;
		set_frame(this.inputImg);
	}

	/**
	 * Configures the coordinates of the top left corner of each block using size of each block
	 * @param block_width
	 * @param block_height
	 */
	public void slice_by_size(int block_width, int block_height) {
		
		this.block_width = block_width;
		this.block_height = block_height;
		this.max_width_id = (int)(this.image_size.width / this.block_width - 1);
		this.max_height_id = (int)(this.image_size.height / this.block_height - 1);
		this.IMG_WIDTH = (this.max_height_id + 1) * this.block_width;
		this.IMG_HEIGHT = (this.max_width_id + 1) * this.block_height;
		System.out.println(this.max_width_id);
		set_frame(this.inputImg);
	}
	
	/**
	 * https://forum.opencv.org/t/opencv-kmeans-java/285/3
	 * changes the resultImg to kmeans of the block
	 * @param n_clusters num of colors
	 * @param img input image
	 */
	public Mat kmeans(int n_clusters, Mat img) {
		Mat data = img.reshape(1,(int)img.total());
		Mat data_32f = new Mat();
		data.convertTo(data_32f, CvType.CV_32F);
		Mat bestLabels = new Mat();
		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT,100,1);
		int attempts = 1;
		int flags = Core.KMEANS_PP_CENTERS;
		Mat center = new Mat();

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
		return draw;
	}
	/**
	 * perform local kmeans block by block
	 * @param intermediate_k num of colors in each block
	 * @param global_k num of colors in entire image, <=0 for off
	 */
	public void local_kmeans(int intermediate_k, int global_k) {
		this.inputImg.copyTo(this.processImg);
		for (int row = 0; row <= this.max_width_id; row++) {
			this.id_width = row;
			for (int col = 0; col <= this.max_height_id; col++) {
				set_block();
				//Mat submat = this.processImg.submat(this.ROI);
				// contMat = submat.clone();
				kmeans(intermediate_k, this.block).copyTo(this.processImg.submat(this.ROI));
				//this.processImg = submat.clone();
				//System.out.println(submat);
				//System.out.println("local_kmeans");
				//System.out.printf("row:%d, col:%d\n", row, col);
				this.id_height = col;
			}
		}
		if (global_k > 0) {
			this.resultImg = kmeans(global_k, this.processImg);
		} else {
			this.resultImg = this.processImg;
		}
	}
	
	
	
	public void debug() {
		//System.out.printf("\nImagePrep:debug(): %d, %d",this.max_height_id, this.block_height);
		//System.out.println(this.inputImg.type());
	}
}
