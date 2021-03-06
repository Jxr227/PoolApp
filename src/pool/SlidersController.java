package pool;


import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;

/**
 * Controller for the sliders view
 */
public class SlidersController {
    // Start Camera button
    @FXML
    private Button cameraButton;
    // Current frame
    @FXML
    private ImageView originalFrame;
    // Current frames mask
    @FXML
    private ImageView maskImage;
    // Current frame with all operations applied
    @FXML
    private ImageView morphImage;
    //Sliders for setting ranges
    @FXML
    private Slider hueStart;
    @FXML
    private Slider hueStop;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;

    // Current values of the sliders label
    @FXML
    private Label hsvCurrentValues;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    // the OpenCV object that performs the video capture
    private VideoCapture capture = new VideoCapture();


    // a flag to change the button behavior
    private boolean cameraActive;

    // property for object binding
    private ObjectProperty<String> hsvValuesProp;


    private Mat image = null;

    /**
     * Start camera action
     */
    @FXML
    private void startCamera() {
        // bind a text property with the string containing the current range of
        // HSV values for object detection
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("res/pool2.jpeg"));
        } catch (IOException e) {
            System.out.println("EX");
        }
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer())
                .getData();
        image = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        image.put(0, 0, pixels);

        updateImageView(originalFrame, Utils.mat2Image(image));

        hsvValuesProp = new SimpleObjectProperty<>();
        this.hsvCurrentValues.textProperty().bind(hsvValuesProp);

        // set a fixed width for all the image to show and preserve image ratio
        this.imageViewProperties(this.originalFrame, 400);
        this.imageViewProperties(this.maskImage, 200);
        this.imageViewProperties(this.morphImage, 200);
        grabFrame();
        this.cameraActive = true;


        // grab a frame every 33 ms (30 frames/sec)
        Runnable frameGrabber = new Runnable() {

            @Override
            public void run() {
                // effectively grab and process a single frame
                Mat frame = grabFrame();
                // convert and show the frame
                Image imageToShow = Utils.mat2Image(frame);
                updateImageView(originalFrame, imageToShow);
            }
        };

        this.timer = Executors.newSingleThreadScheduledExecutor();
        this.timer.scheduleAtFixedRate(frameGrabber, 0, 200, TimeUnit.MILLISECONDS);


        // update the button content
        this.cameraButton.setText("Stop Camera");
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     */
    private Mat grabFrame() {
        Mat frame = image;

            try {
                // if the frame is not empty, process it
                if (!frame.empty()) {
                    // init
                    Mat blurredImage = new Mat();
                    Mat hsvImage = new Mat();
                    Mat mask = new Mat();
                    Mat morphOutput = new Mat();

                    // remove some noise
                    Imgproc.blur(frame, blurredImage, new Size(7, 7));

                    // convert the frame to HSV
                    Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

                    // get thresholding values from the UI
                    // remember: H ranges 0-180, S and V range 0-255

                    String colour = "yellow";

                    HueSaturationValues values = getHueSaturationValues(colour);

                    //Default values
                    //Scalar minValues = new Scalar(this.hueStart.getValue(), this.saturationStart.getValue(), this.valueStart.getValue());
                    //Scalar maxValues = new Scalar(this.hueStop.getValue(), this.saturationStop.getValue(), this.valueStop.getValue());

                    //Setting hue saturation values for colour
                    Scalar minValues = new Scalar(values.hue.start, values.saturation.start, values.value.start);
                    Scalar maxValues = new Scalar(values.hue.max, values.saturation.max, values.value.max);

                    // show the current selected HSV range
                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
                            + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
                            + minValues.val[2] + "-" + maxValues.val[2];
                    Utils.onFXThread(this.hsvValuesProp, valuesToPrint);

                    // threshold HSV image to select tennis balls
                    Core.inRange(hsvImage, minValues, maxValues, mask);
                    // show the partial output
                    this.updateImageView(this.maskImage, Utils.mat2Image(mask));

                    //Dilate / erode values, helps make sensible sized ball in the mask
                    Double dilateValue = 10.0;
                    Double erodeValue = 5.0;

                    // morphological operators
                    // dilate with large element, erode with small ones
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(dilateValue, dilateValue));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(erodeValue, erodeValue));

                    Imgproc.erode(mask, morphOutput, erodeElement);
                    Imgproc.erode(morphOutput, morphOutput, erodeElement);

                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);
                    Imgproc.dilate(morphOutput, morphOutput, dilateElement);

                    // show the partial output
                    this.updateImageView(this.morphImage, Utils.mat2Image(morphOutput));

                    // find the tennis ball(s) contours and show them
                    frame = this.findAndDrawBalls(morphOutput, frame);

                }

            } catch (Exception e) {
                // log the (full) error
                System.err.print("Exception during the image elaboration...");
                e.printStackTrace();
            }

        return frame;
    }

    public class HueSaturationValues {

        ColourRange hue;
        ColourRange saturation;
        ColourRange value;

        public HueSaturationValues(ColourRange hue, ColourRange saturation, ColourRange value){
            this.hue = hue;
            this.saturation = saturation;
            this.value = value;
        }
    }

    public class ColourRange {

        Double start;
        Double max;

        public ColourRange(Double start, Double max){
            this.start = start;
            this.max = max;
        }
    }

    private HueSaturationValues getHueSaturationValues(String colour) {

        //Defaults for ranges
        ColourRange defaultHue = new ColourRange(25.0, 37.0);
        ColourRange defaultSaturation = new ColourRange(75.0, 255.0);
        ColourRange defaultValue = new ColourRange(0.0, 255.0);

        switch (colour) {
            case "red":

                ColourRange redHue = new ColourRange(0.0, 5.0);
                ColourRange redSaturation = new ColourRange(75.0, 255.0);
                ColourRange redValue = new ColourRange(0.0, 255.0);

                return new HueSaturationValues(redHue,redSaturation, redValue);

            case "yellow":

                ColourRange yellowHue = new ColourRange(20.0, 37.0);
                ColourRange yellowSaturation = new ColourRange(90.0, 255.0);
                ColourRange yellowValue = new ColourRange(0.0, 255.0);

                return new HueSaturationValues(yellowHue, yellowSaturation, yellowValue);

            case "white":

                ColourRange whiteHue = new ColourRange(0.0, 0.0);
                ColourRange whiteSaturation = new ColourRange(0.0, 0.0);
                ColourRange whiteValue = new ColourRange(0.0, 255.0);

                return new HueSaturationValues(whiteHue, whiteSaturation, whiteValue);

            case "black":

                return new HueSaturationValues(defaultHue, defaultSaturation, defaultValue);

            default: return new HueSaturationValues(defaultHue, defaultSaturation, defaultValue);
        }
    }

    /**
     * Given a binary image containing one or more closed surfaces, use it as a
     * mask to find and highlight the objects contours
     *
     * @param maskedImage the binary image to be used as a mask
     * @param frame       the original {@link Mat} image to be used for drawing the
     *                    objects contours
     * @return the {@link Mat} image with the objects contours framed
     */
    private Mat findAndDrawBalls(Mat maskedImage, Mat frame) {
        // init
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }

        return frame;
    }

    /**
     * Set typical {@link ImageView} properties: a fixed width and the
     * information to preserve the original image ration
     *
     * @param image     the {@link ImageView} to use
     * @param dimension the width of the image to set
     */
    private void imageViewProperties(ImageView image, int dimension) {
        // set a fixed width for the given ImageView
        image.setFitWidth(dimension);
        // preserve the image ratio
        image.setPreserveRatio(true);
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed() {
        this.stopAcquisition();
    }

}