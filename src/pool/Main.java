package pool;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Sliders.fxml"));
            // store the root element so that the controllers can use it
            BorderPane root = (BorderPane) loader.load();
            // set a whitesmoke background
            root.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Object Recognition");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();

            // set the proper behavior on closing the application
            SlidersController controller = loader.getController();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)
                {
                    controller.setClosed();
                }
            }));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        launch(args);
    }
}
