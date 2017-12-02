package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Camera.fxml"));
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			primaryStage.setMaxHeight(450);
			primaryStage.setMaxWidth(600);
			primaryStage.setMinHeight(450);
			primaryStage.setMinWidth(600);
			
			CameraController controller = new CameraController();
			primaryStage.setOnCloseRequest(e->{
				Platform.exit();
				System.exit(0);
				controller.setStop();
				primaryStage.close();
			});
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
