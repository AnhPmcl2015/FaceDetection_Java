package application;

import java.io.File;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import exception.MyException;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import opencv.Camera;

public class CameraController implements Initializable {

	// id of Accordition pane contain title panes
	@FXML
	private Accordion accorditionPane;

	// id of title panes
	@FXML
	private TitledPane titlePaneFeatures, titlePaneWhiteBalance, titlePaneTimer, titlePaneBrightness,
			tiltilePaneOtherSetting;

	// id of check box to add logo
	@FXML
	private CheckBox chkLogo;

	// id of slider timer to set time delay and text to display time delay
	@FXML
	private Slider sliderTimer;
	@FXML
	private Text txtTimer;

	// id of slider brightness to set the brightness of image view and text to
	// display brightness index, white balance index, check box grayscale
	@FXML
	private Slider sliderBrightness, sliderWhiteBalance, sliderHue;
	@FXML
	private Text txtBrightness, txtWhiteBalance, txtHue;
	@FXML
	private CheckBox chkGrayscale;

	// id of image view current frame to display wriable image from webcam
	@FXML
	private ImageView imgCurrentFrame;

	// id of button screenshot to take a photo or record a video
	@FXML
	private Button btnScreenshot;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;

	// the OpenCV object that realizes the video capture
	private VideoCapture capture;

	// Mat logo to load the logo
	private Mat logo;

	// cascade classifer to detect faces and eyes from video stream
	private CascadeClassifier faceCascade;
	private CascadeClassifier eyesCascade;

	// the counter of screenshot event
	private IntegerProperty counter;
	private IntegerProperty timeCountdown;

	private Timer timerCountdown;
	private TimerTask timerTask;

	private boolean flagForScreenshot = false;
	private Thread threadForScreenshot;
	private static int iForScreenshot = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		initControls();

		addEvents();

		this.startWebcam();
	}

	/**
	 * Init all control of this stage
	 */
	private void initControls() {
		// init the VideoCapture
		this.capture = new VideoCapture();

		// init cascade classifier
		this.faceCascade = new CascadeClassifier();
		this.faceCascade.load("resources/openCVXMLFile/haarcascade_frontalface_alt.xml");

		this.eyesCascade = new CascadeClassifier();
		this.eyesCascade.load("resources/openCVXMLFile/haarcascade_lefteye_2splits.xml");

		// preserve image ratio
		this.imgCurrentFrame.setPreserveRatio(true);

		// set default counter
		this.counter = new SimpleIntegerProperty(0);
		this.timeCountdown = new SimpleIntegerProperty();
		this.timeCountdown.bind(counter);

		// expander titlePaneFeartures
		this.accorditionPane.setExpandedPane(titlePaneFeatures);

	}

	/**
	 * add events to all control
	 */
	private void addEvents() {
		this.sliderTimer.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				if (newValue.intValue() == 0) {
					txtTimer.setText("Timer off");
				} else
					txtTimer.setText(newValue.intValue() + " s");
			}
		});

		this.sliderBrightness.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				int n = (int) Math.round(newValue.doubleValue());
				txtBrightness.setText(n + "");
				ColorAdjust monochrome = new ColorAdjust();
				monochrome.setBrightness(0);
				if (n == -1) {
					monochrome.setBrightness(-0.5);
				} else if (n == 1) {
					monochrome.setBrightness(0.3);
				} else if (n == 2) {
					monochrome.setBrightness(0.5);
				}
				sliderWhiteBalance.setValue(0);
				sliderHue.setValue(0);
				imgCurrentFrame.setEffect(monochrome);
			}
		});

		this.timeCountdown.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				// System.out.println("Value of timeCountdown: " + newValue);
				if (newValue.longValue() == sliderTimer.valueProperty().longValue()) {
					System.out.println("Time up");
					takePhoto();
				}

			}
		});

		this.sliderWhiteBalance.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				int n = (int) Math.round(newValue.doubleValue());
				txtWhiteBalance.setText(n + "");
				ColorAdjust monochrome = new ColorAdjust();
				monochrome.setSaturation(-1);
				Blend blush = null;
				if (n == 1) {
					blush = new Blend(BlendMode.MULTIPLY, monochrome,
							new ColorInput(0, 0, 390, 293, Color.web("#FFCC66")));
				} else if (n == 2) {
					blush = new Blend(BlendMode.MULTIPLY, monochrome,
							new ColorInput(0, 0, 390, 293, Color.web("#FFCCFF")));
				} else if (n == 3) {
					blush = new Blend(BlendMode.MULTIPLY, monochrome,
							new ColorInput(0, 0, 390, 293, Color.web("9933CC")));
				}

				sliderBrightness.setValue(0);
				sliderHue.setValue(0);
				imgCurrentFrame.setEffect((Effect) blush);
			}
		});

		this.sliderHue.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				// TODO Auto-generated method stub
				int n = (int) Math.round(newValue.doubleValue());
				txtHue.setText(n + "");
				ColorAdjust monochrome = new ColorAdjust();
				monochrome.setHue(0);
				if (n == -1) {
					monochrome.setHue(-1);
				} else if (n == 1) {
					monochrome.setHue(1);
				}

				sliderBrightness.setValue(0);
				sliderWhiteBalance.setValue(0);
				imgCurrentFrame.setEffect(monochrome);

			}
		});

		btnScreenshot.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			this.flagForScreenshot = true;
			int second = LocalTime.now().getSecond();
			threadForScreenshot = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (flagForScreenshot) {
						if (LocalTime.now().getSecond() - second > 1) {
							System.out.println(LocalTime.now().getSecond() - second);
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									takePhoto();
								}
							});
							try {
								Thread.sleep(250);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
			threadForScreenshot.start();
		});

		btnScreenshot.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
			this.flagForScreenshot = false;
		});
	}

	/**
	 * method to take a photo or record a video
	 * 
	 * 
	 */
	@FXML
	protected void screenShot(ActionEvent event) {
		if (this.sliderTimer.valueProperty().longValue() > 0) {
			this.counter.set(0);
			this.timerCountdown = new Timer();
			timerTask = new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Platform.runLater(() -> counter.set(counter.get() + 1));
				}
			};

			timerCountdown.scheduleAtFixedRate(timerTask, 1000, 1000);
		} else
			takePhoto();
	}

	protected void takePhoto() {
		if (timerCountdown != null) {
			timerTask.cancel();
			timerCountdown.cancel();
			timerCountdown.purge();

		}
		WritableImage image = imgCurrentFrame.snapshot(new SnapshotParameters(), null);
		String userHomeFolder = System.getProperty("user.home");
		userHomeFolder += "\\Desktop\\";
		File dir = new File(userHomeFolder, "JavaTest");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		userHomeFolder += "JavaTest\\";
		File file = new File(userHomeFolder, "test.png");
		while (file.exists()) {
			iForScreenshot++;
			file = new File(userHomeFolder, "test" + iForScreenshot + ".png");
		}
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			System.out.println(userHomeFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * method to load a Mat of logo
	 */
	@FXML
	protected void addLogo() {
		if (chkLogo.isSelected()) {
			this.logo = Imgcodecs.imread("resources/features/test.png");
			Imgproc.resize(logo, logo, new Size(100, 100));

		}
	}

	/**
	 * method to disabel slider brightness if check box grayscale is selected
	 */
	@FXML
	protected void selectGrayscale() {
		if (chkGrayscale.isSelected()) {
			sliderBrightness.setValue(0);
			sliderWhiteBalance.setValue(0);
			sliderHue.setValue(0);
		}
	}

	/**
	 * method support to open the webcam
	 */

	private void startWebcam() {
		// open the webcam index
		this.capture.open(0);

		// check if video stream is available?
		if (this.capture.isOpened()) {
			// grab a frame every 33ms (30 frames/sec)
			Runnable frameGrabber = new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Mat frame = Camera.grabFrame(capture);
					// convert and show the frame
					if (chkGrayscale.isSelected()) {
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					}

					Image image = Utils.mat2Image(frame);
					WritableImage imageToShow = (WritableImage) image;

					if (chkLogo.isSelected()) {
						try {
							Image lg = Utils.mat2Image(logo);
							PixelReader reader = lg.getPixelReader();
							PixelWriter writer = imageToShow.getPixelWriter();

							int width = (int) lg.getWidth();
							int height = (int) lg.getHeight();
							System.out.println(width + " " + height);
							for (int y = 0; y < height; y++) {
								for (int x = 0; x < width; x++) {
									Color color = reader.getColor(x, y);
									if(!(color.getRed()==0 && color.getGreen()==0 && color.getBlue()==0)) {
										writer.setColor(x, y, color);
									}
								}
							}
						} catch (Exception e) {
						}
					}

					updateImageView(imgCurrentFrame, imageToShow);
					frame.release();
				}
			};

			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * 
	 * @param imgCurrentFrame
	 *            the {@link ImageView} to update
	 * @param imageToShow
	 *            the {@link Image} to show
	 */

	private void updateImageView(ImageView imgCurrentFrame, WritableImage imageToShow) {
		// TODO Auto-generated method stub
		Utils.onFXThread(imgCurrentFrame.imageProperty(), imageToShow);
	}

	public void setStop() {
		Camera.stopAcquisition(this.timer, this.capture);
	}
}
