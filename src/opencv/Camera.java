package opencv;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import exception.MyException;
import javafx.scene.image.Image;

public final class Camera {
	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	public static Mat grabFrame(VideoCapture capture) {
		Mat frame = new Mat();
		if (capture.isOpened()) {
			try {
				capture.read(frame);
				Core.flip(frame, frame, 1);
			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the frame elaboration: " + e);
			}
		}
		return frame;
	}
	
	
	/**
	 * Method for face detection and tracking
	 * 
	 * @param frame
	 * 		it looks for faces in this frame
	 */
	public static void detectAndDisplay(Mat frame) {
		// TODO Auto-generated method stub
		FaceDetector faceDetector = new FaceDetector();
		try {
			faceDetector.findFaces(frame);
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	}
	
	
	/**
	 * method to release the camera if close the primary stage
	 */

	public static void stopAcquisition(ScheduledExecutorService timer, VideoCapture capture) {
		// TODO Auto-generated method stub
		if (timer != null && !timer.isShutdown()) {
			try {
				// stop the timer
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (capture.isOpened()) {
			// release the camera
			capture.release();
		}
	}

}
