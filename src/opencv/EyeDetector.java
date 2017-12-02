package opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class EyeDetector {
	// get eye detector xml file from opencv
	private CascadeClassifier eyeCascade;
	
	// tick Rects of eye
	private MatOfRect eyesMat;
	
	// array contain eyes location
	private Rect[] eyesArray;
	public EyeDetector() {
		this.eyeCascade = new CascadeClassifier();
		this.eyeCascade.load("resources/openCVXMLFile/haarcascade_lefteye_2splits.xml");
		
		this.eyesMat = new MatOfRect();
	}
	
	public void findEyes(Mat frame) {
	/*	eyeCascade.detectMultiScale(initialFrame, eyesMat);
		for(Rect eye : eyesMat.toArray()){
			
			
			croppedEyes.add(new Mat(initialFrame,eye));
			System.out.println("eye");
		}*/
		eyeCascade.detectMultiScale(frame, eyesMat);
		
		eyesArray = eyesMat.toArray();
		for(int i = 0; i < eyesArray.length; i++) {
			Imgproc.rectangle(frame, eyesArray[i].tl(), eyesArray[i].br(), new Scalar(0,64,56));
		}
	}
	
	public Rect[] getEyesArray() {
		return this.eyesArray;
	}
	
}
