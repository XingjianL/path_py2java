package wolf_vision;

import org.opencv.dnn.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;

public class nn extends ImagePrep{
	private static final int TARGET_IMG_WIDTH = 224;
	private static final int TARGET_IMG_HEIGHT = 224;
	private static final double SCALE_FACTOR = 1 / 255.0; 
	private static Mat inputImage = new Mat();
	
	private static String cfg_path = "/home/xing/TesterCodes/APRData/models/yolov3.cfg";
	private static String weights_path = "/home/xing/TesterCodes/APRData/models/yolov3.weights";
	private Net net;
    private static String classes = "/home/xing/TesterCodes/APRData/models/gate.names";
    List<String> outBlobNames = new ArrayList<>();
	public void preprocess(Mat image) {
		
	}
	
	public void loadModel() {
		this.net = Dnn.readNetFromDarknet(cfg_path, weights_path);
		this.outBlobNames = getOutputNames(net);
	}
	
	public Mat detect(Mat image) {
		 List<Mat> result = new ArrayList<>();
	     Mat blob = Dnn.blobFromImage(image, 1/255.0, new Size(416, 416), new Scalar(0), true, false);
		 this.net.setInput(blob);
		 this.net.forward(result,outBlobNames);
		 System.out.println(result);
		 List<Integer> clsIds = new ArrayList<>();
	     List<Float> confs = new ArrayList<>();
	     List<Rect2d> rects = new ArrayList<>();
		 for (int i = 0; i < result.size(); i++) {
			 Mat level = result.get(i);
			 for (int j = 0; j < level.rows(); j++) {
				Mat row = level.row(j);
	            Mat scores = row.colRange(5, level.cols());
				Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
				float confidence = (float)mm.maxVal;
				
                Point classIdPoint = mm.maxLoc;
                if (confidence > .5)
                {
                    int centerX = (int)(row.get(0,0)[0] * image.cols()); //scaling for drawing the bounding boxes//
                    int centerY = (int)(row.get(0,1)[0] * image.rows());
                    int width   = (int)(row.get(0,2)[0] * image.cols());
                    int height  = (int)(row.get(0,3)[0] * image.rows());
                    int left    = centerX - width  / 2;
                    int top     = centerY - height / 2;

                    System.out.println((int)classIdPoint.x);
                    System.out.println((float)confidence);
                    System.out.println(new Rect(left, top, width, height));
                    clsIds.add((int)classIdPoint.x);
                    confs.add((float)confidence);
                    rects.add(new Rect2d(left, top, width, height));
                    
                }
			 }
		 }
		 System.out.println(confs.size());
		 if (confs.size() == 0) {
			 System.out.println("Nothing");
			 return image;
		 }
		 MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
		 Rect2d[] boxesArray = rects.toArray(new Rect2d[0]);
		 MatOfRect2d boxes = new MatOfRect2d(boxesArray);
		 MatOfInt indices = new MatOfInt();
		 Dnn.NMSBoxes(boxes, confidences, .5f, .5f, indices); //We draw the bounding boxes for objects here//

		 int [] ind = indices.toArray();
		 int j=0;
		 Mat output = image.clone();
		 for (int i = 0; i < ind.length; ++i)
		 {
			 int idx = ind[i];
		     Rect2d box = boxesArray[idx];
		     Imgproc.rectangle(output, box.tl(), box.br(), new Scalar(0,0,255), 2);
		            //i=j;
		            
		     System.out.println(idx);
		 }
		 return output;
		     
	}
	private static List<String> getOutputNames(Net net) {
		List<String> names = new ArrayList<>();

        List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
        List<String> layersNames = net.getLayerNames();

        outLayers.forEach((item) -> names.add(layersNames.get(item - 1)));//unfold and create R-CNN layers from the loaded YOLO model//
        return names;
	}
}
