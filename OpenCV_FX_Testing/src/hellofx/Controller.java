package hellofx;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import Utils.Utils;
import wolf_vision.ImagePrep;
// https://github.com/opencv-java/getting-started/blob/master/FXHelloCV/src/it/polito/elite/teaching/cv/FXHelloCVController.java

public class Controller {

    @FXML
    private Button start_btn;

    @FXML
    private ImageView currentFrame;
    @FXML
    private ImageView outputFrame;
    
    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private VideoCapture capture2 = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;
    private static String video_footage = "C:\\Users\\lixin\\Downloads\\manual_path_edited1.mp4";
    private static String out_footage = "C:\\Users\\lixin\\Downloads\\path_output1.avi";

    private boolean footageOpened = false;
    private ImagePrep imgPrep = new ImagePrep();
    @FXML
    void startCamera(ActionEvent event) {
    	if (!this.cameraActive) {
    		if (!this.footageOpened) {
    			this.capture.open(video_footage);
    			this.capture2.open(out_footage);
    			this.footageOpened = true;
    		}
    		if(this.capture.isOpened()) {
    			this.cameraActive = true;
    			
    			Runnable frameGrabber = new Runnable() {
    				@Override
    				public void run() {
    					Mat frame = grabFrame(capture);
    					Mat outputCV = grabFrame(capture2);
    					
    					imgPrep.set_frame(frame);
    					imgPrep.slice_by_size(25, 25);
    					imgPrep.local_kmeans(2,4);
    					
    					Image imageToShow = Utils.mat2Image(frame);
    					Image outputImage = Utils.mat2Image(imgPrep.resultImg);
    					
    					imgPrep.debug();
    					updateImageView(currentFrame, imageToShow);
    					updateImageView(outputFrame, outputImage);
    				}
    			};
    			this.timer = Executors.newSingleThreadScheduledExecutor();
    			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
    			
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