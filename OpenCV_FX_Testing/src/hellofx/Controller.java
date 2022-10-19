package hellofx;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import Utils.Utils;
import wolf_vision.Buoy;
import wolf_vision.ImagePrep;
import wolf_vision.Path;
// https://github.com/opencv-java/getting-started/blob/master/FXHelloCV/src/it/polito/elite/teaching/cv/FXHelloCVController.java

public class Controller {

    @FXML
    private Button button_2;

    @FXML
    private ImageView botleft_2;

    @FXML
    private Button start_btn;

    @FXML
    private ImageView topleft;

    @FXML
    private ImageView topright;

    @FXML
    private ImageView botright;

    @FXML
    private ImageView topleft_2;

    @FXML
    private ImageView botright_2;

    @FXML
    private ImageView topright_2;

    @FXML
    private ImageView botleft;
    
    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private VideoCapture capture2 = new VideoCapture();
    private boolean cameraActive = false;
    //private static int cameraId = 0;
    private static String video_path_footage = "/home/xing/TesterCodes/APRData/manual_path_edited1.mp4";
    private static String out_path_footage = "/home/xing/TesterCodes/APRData/path_output1.avi";

    private static String tommygun_imgs_dir = "/home/xing/TesterCodes/APRData/tommy_guns-20221014T181830Z-001/tommy_guns";
    private int tommygun_img_id = 0;
    private File[] tommygun_imgs = new File(tommygun_imgs_dir).listFiles();
    
    private boolean footageOpened = false;
    private Path path_process = new Path();
    private ImagePrep path_prep = path_process;
    private Buoy buoy_process = new Buoy();
    private ImagePrep buoy_prep = buoy_process;
    //private Path path_process2 = new Path();
    //private ImagePrep path_prep2 = path_process2;
    @FXML
    void startCamera(ActionEvent event) {
    	if (!this.cameraActive) {
    		if (!this.footageOpened) {
    			this.capture.open(video_path_footage);
    			this.capture2.open(out_path_footage);
    			this.footageOpened = true;
    		}
    		if(this.capture.isOpened()) {
    			this.cameraActive = true;
    			
    			Runnable frameGrabber = new Runnable() {
    				@Override
    				public void run() {
    					Mat frame = grabFrame(capture);
    					Mat outputCV = grabFrame(capture2);
    					
    					path_prep.setFrame(frame);
    					//path_prep2.setFrame(frame);
    					path_prep.sliceSize(25, 25);
    					//path_prep2.sliceSize(25, 25);
    					path_prep.localKmeans(2,4);
    					//path_prep2.localKmeans(2,2);
    					Mat local_kmeans = path_prep.resultImg;
    					//Mat test = path_prep2.resultImg;
    					Mat pca_draw = path_process.iteratePathBinaryPCAAndDraw(local_kmeans);
    					//= path_process.drawPCA(frame);
    					Image imageToShow = Utils.mat2Image(frame);
    					Image outputImage = Utils.mat2Image(local_kmeans);
    					Image debug1 = Utils.mat2Image(outputCV);
    					Image results = Utils.mat2Image(pca_draw);
    					path_process.debug();
    					updateImageView(topleft, imageToShow);
    					updateImageView(topright, outputImage);
    					updateImageView(botleft, debug1);
    					updateImageView(botright, results);
    				}
    			};
    			this.timer = Executors.newSingleThreadScheduledExecutor();
    			this.timer.scheduleAtFixedRate(frameGrabber, 0, 100, TimeUnit.MILLISECONDS);
    			
    			this.start_btn.setText("Stop Footage");
    		} else {
    			System.err.println("cannot open camera connection");
    		}
    	} else {
    		this.cameraActive = false;
    		this.start_btn.setText("Start Footage");
    		
    		this.stopAcquisition();
    	}
    }

    @FXML
    void cycleImage(ActionEvent event) {
    	String currentFrame = tommygun_imgs[tommygun_img_id].toString();
    	System.out.println(currentFrame);
    	
    	Mat frame = Imgcodecs.imread(currentFrame);
    	
    	buoy_prep.setFrame(frame);
    	buoy_prep.sliceSize(25, 25);
    	buoy_prep.localKmeans(2, 4);
    	Mat local_kmeans = buoy_prep.resultImg;
    	Image imageToShow = Utils.mat2Image(frame);
    	Image image2 = Utils.mat2Image(local_kmeans);
    	updateImageView(topleft_2, imageToShow);
    	updateImageView(topright_2, image2);
    	tommygun_img_id++;
    }
    /*
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame(VideoCapture capture)
	{
		// init everything
		Mat frame = new Mat();
		
		// check if the capture is open
		if (capture.isOpened())
		{
			try
			{
				// read the current frame
				capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
					//Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				}
				
			}
			catch (Exception e)
			{
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return frame;
	}
	
	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			//this.capture.release();
		}
	}
	
	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
	
	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed()
	{
		this.stopAcquisition();
	}
	
}