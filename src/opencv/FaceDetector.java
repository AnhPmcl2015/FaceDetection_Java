package opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import exception.MyException;

public class FaceDetector {
	private Mat grayFrame;
	private Mat equalizedFrame;
	private EyeDetector eyeDetector;
	
	// get face detector xml file from opencv
	private CascadeClassifier faceCascade;
	
	// tick Rects of face
	private MatOfRect faceMat;

	// Rect array contains face location
	private Rect[] facesArray;
	
	public FaceDetector() {
		this.grayFrame = new Mat();
		this.equalizedFrame = new Mat();

		this.faceCascade = new CascadeClassifier();
		this.faceCascade.load("resources/openCVXMLFile/haarcascade_frontalface_alt.xml");

		this.faceMat = new MatOfRect();
		
		
		this.eyeDetector = new EyeDetector();
	}

	public void findFaces(Mat frame) throws MyException {
		// convert the frame in grayscale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, equalizedFrame);
		
		this.detectFace(frame);
	}
	
	private void detectFace(Mat frame) {
		faceCascade.detectMultiScale(this.equalizedFrame,faceMat, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(50,50),new Size());
		facesArray = faceMat.toArray();
		for(Rect i : facesArray) {
			Imgproc.rectangle(frame, i.tl(), i.br(), new Scalar(45,32,45));
		}
		
		this.associateEyess(eyeDetector, frame);
	}
	
	private void associateEyess(EyeDetector eyeDetector, Mat frame) {
		eyeDetector.findEyes(frame);
	}
	
	public Rect[] getFaceArray() {
		return this.facesArray;
	}
	
	public EyeDetector getEyeDetector() {
		return this.eyeDetector;
	}
}
