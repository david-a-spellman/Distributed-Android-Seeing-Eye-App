package com.example.myapplicationandroidseeingeyeapplication;

//import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.provider.MediaStore;
import android.widget.AdapterView;
import android.provider.DocumentsContract;


import com.example.myapplicationandroidseeingeyeapplication.databinding.ActivityFullscreenBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.*;
import java.lang.*;
//import java.net.http.*;
import java.net.*;
import javax.net.ssl.*;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.*;

import android.os.Vibrator;
import android.widget.*;
import android.graphics.*;
import android.content.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
//import com.android.example.camerax.tflite.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullScreenCameraActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    //private ActivityCameraBinding ui_binding = null;
    private Bitmap bitmapBuffer;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String[] permissions = new String[] { Manifest.permission.CAMERA };
    private Integer permissionRequestCode = null;
    private Integer fileAccessPermCode = null;
    private Integer lensFacing = new Integer(CameraSelector.LENS_FACING_BACK);
    private boolean isFrontFacing = false;
    private boolean pauseAnalysis = false;
    private Integer imageRotationDegrees = 0;
    private TensorImage tfImageBuffer = new TensorImage(DataType.UINT8);
    private ImageProcessor tfImageProcessor = null;
    private NnApiDelegate nnApiDelegate = new NnApiDelegate();
    private Interpreter tflite = null;
    private ObjectDetectionHelper detector = null;
    private Size tfInputSize = null;
    private CompletableFuture<String> response;
    private String url;
    private long send_time;
    private long taken_time;
    private long completion_time;
    private File path = null; //this.getExternalFilesDir ("images");
    private String distributed_log_file = null; //new String (path + "/distributed_test_log_1.csv");
    private String local_log_file = null; //new String (path + "/local_test_log_1.csv");

    // Request code for taking a picture
    static final int REQUEST_IMAGE_CAPTURE = 1;

    // Flag that determines whether an image should be captured
    static boolean mImageCapture = false;
    static boolean mDistributed_recognition = true;
    /*
    * number of images that will be stored at any single moment, once more images than this amount are captured
    * The application will begin to recycle storage
    * Images that are unable to be processed after a certain amount of time should be marked as invalid
    * Keep list of times when images were taken along with a boolean validation variable
    * If variable is true the image is still valid and should still be processed, but if the valid flag is set to false the image should not be processed
    * and can be overwritten
    * Images with false values will be skipped over when selecting images to send to the server to be processed
    * And these same images will be skipped over when selecting images to run object recognition on locally
    * An extra feature that can be added is to use the light sensor every once in a while to determine whether the flash should be used
    * The boolean WITH_FLASH flag will be changed to true if the light sensor comes back with a low light reading below a certain threshold
    * Otherwise this variable will be set to false and not use flash
    */
    // variable that controls how many images can be stored at once, and how quickly memory is recycled
    static final int image_count = 20;
    // variable that controls how long an image remains valid for after it is taken in milliseconds
    static final int IMAGE_VALIDATION_LIFE_TIME = 400;
    // variable that controls about how often images should be captured in milliseconds
    static final int TIME_BETWEEN_IMAGE_CAPTURES = 5;
    // Flag that determines whether the flash should be used based on periodic light sensor readings
    static final boolean WITH_FLASH = false;
    // Object that tracks what time an image was captured by index, as well as whether that immage is valid
    static ImageValidationList image_valid_list = new ImageValidationList (image_count);

    // View for displaying the image
    ImageView imageView;

    // Buttons for starting and stopping the capturing and processing of images
    Button startButton;
    Button stopButton;
    TextView feedbackTextView;
    Vibrator vibrator;
    Spinner modeSpinner;
    Intent intent;

    static long log_count = 0;
    static String log_tag = "FullscreenActivity";
    private float ACCURACY_THRESHOLD = 0.5f;
    private final String MODEL_PATH = "coco_ssd_mobilenet_v1_1.0_quant.tflite";
    private final String LABELS_PATH = "coco_ssd_mobilenet_v1_1.0_labels.txt";
    private final String FACE_MODEL_PATH = "face_detection_back.tflite";

    private final Runnable mHidePart2Runnable = new Runnable() {
        //@SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        //public void run() { hide(); }
        public void run() {
            mVisible = false;
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
/*
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
*/
    private ActivityFullscreenBinding binding;
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;
    List<String> labels = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(log_tag + log_count, "executing onCreate method");
        log_count += 1;
        super.onCreate(savedInstanceState);

        // Code for loading tensorflow lite models for client side facial and object recognition
        // Facial recognition will be used to perform obfuscation on faces before sending to server side for object recognition
        // Other model will perform the object detection directly on the phone with tensorflow lite

        // Requesting Permission to access External Storage
        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
        //       EXTERNAL_STORAGE_PERMISSION_CODE);

        //path = new File(Environment.getExternalStorageDirectory ().getAbsolutePath () + "/Android/data");
        //openDirectory(path);
//        distributed_log_file = new String (path + "/distributed_test_log_1.csv");
//        local_log_file = new String (path + "/local_test_log_1.csv");
        distributed_log_file = new String ("distributed_test_log_1.csv");
        local_log_file = new String ("local_test_log_1.csv");

        Random randGen = new Random();
        permissionRequestCode = randGen.nextInt(10000);
        fileAccessPermCode = randGen.nextInt(10000);

        mDistributed_recognition = false;
        MappedByteBuffer modelFile = null;
        try {
            if (mDistributed_recognition)
                modelFile = FileUtil.loadMappedFile(FullScreenCameraActivity.this.getBaseContext(), FACE_MODEL_PATH);
            else
                modelFile = FileUtil.loadMappedFile(FullScreenCameraActivity.this.getBaseContext(), MODEL_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tflite = new Interpreter(modelFile, (new Interpreter.Options()).addDelegate(nnApiDelegate));

        int inputIndex = 0;
        int[] inputShape = tflite.getInputTensor(inputIndex).shape();
        tfInputSize = new Size(inputShape[2], inputShape[1]);

        try {
            labels = FileUtil.loadLabels(this, LABELS_PATH);
        } catch (IOException e) {
            Log.println(Log.ERROR, "IO Error", e.getMessage());
        }
        detector = new ObjectDetectionHelper(tflite, labels, ObjectDetectionHelper.Mode.FACE);

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //imageView = (ImageView) findViewById(R.id.imageView);
        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);
        feedbackTextView = (TextView)findViewById(R.id.feedbackTextView);

        modeSpinner = (Spinner) findViewById(R.id.modeSpinner);
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter
                .createFromResource(this, R.array.mode_array,
                        R.layout.spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modeAdapter);

        startButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(log_tag + log_count, "executing onClick method for start of collection");
                log_count += 1;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                if (!mImageCapture) {
                    mImageCapture = true;
                }
                if (!hasPermissions(FullScreenCameraActivity.this)) {
                    ActivityCompat.requestPermissions(FullScreenCameraActivity.this, permissions, permissionRequestCode.intValue());
                } else {
                    bindCameraUseCases();
                }
            }
        });

        stopButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(log_tag + log_count, "executing onClick method for stopping image collection");
                log_count += 1;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                mImageCapture = false;
                //image.close ();
            }
        });

/*
        ui_binding.cameraCaptureButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Event", "executing onClick method for start of camera");
                v.setEnabled(false);
                if (pauseAnalysis) {
                    pauseAnalysis = false;
                    ui_binding.imagePredicted.setVisibility(View.GONE);
                } else {
                    // Otherwise, pause image analysis and freeze image
                    pauseAnalysis = true;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(imageRotationDegrees.floatValue());
                    if (isFrontFacing) {
                        matrix.postScale(-1f, 1f);
                    }
                    Bitmap uprightImage = Bitmap.createBitmap(
                        bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);
                    ui_binding.imagePredicted.setImageBitmap(uprightImage);
                    ui_binding.imagePredicted.setVisibility(View.VISIBLE);
                }
                // Re-enable camera controls
                v.setEnabled(true);
            }
        });
*/

        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //binding.buttonStart.setOnTouchListener(mDelayHideTouchListener);

        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String option = (String) parent.getItemAtPosition(position);
                Log.d(log_tag + log_count, "executing onItemSelected method on " + option);
                log_count += 1;
                switch (option){
                    case "Face recognition only":
                        mDistributed_recognition = true;
                        break;
                    case "Full object recognition":
                        mDistributed_recognition = false;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid app mode.");
                }

                // Load the appropriate model
                MappedByteBuffer modelFile = null;
                try {
                    if (mDistributed_recognition) {
                        modelFile = FileUtil.loadMappedFile(FullScreenCameraActivity.this.getBaseContext(), FACE_MODEL_PATH);
                        detector = new ObjectDetectionHelper(tflite, labels, ObjectDetectionHelper.Mode.FACE);

                    }
                    else {
                        modelFile = FileUtil.loadMappedFile(FullScreenCameraActivity.this.getBaseContext(), MODEL_PATH);
                        detector = new ObjectDetectionHelper(tflite, labels, ObjectDetectionHelper.Mode.OBJECT);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tflite = new Interpreter(modelFile, (new Interpreter.Options()).addDelegate(nnApiDelegate));
				
				int inputIndex = 0;
				int[] inputShape = tflite.getInputTensor(inputIndex).shape();
				tfInputSize = new Size(inputShape[2], inputShape[1]);

				List<String> labels = null;
				try {
					labels = FileUtil.loadLabels(FullScreenCameraActivity.this, LABELS_PATH);
				} catch (IOException e) {
					Log.println(Log.ERROR, "IO Error", e.getMessage());
				}
				if (mDistributed_recognition)
				    detector = new ObjectDetectionHelper(tflite, labels, ObjectDetectionHelper.Mode.FACE);
				else
				    detector = new ObjectDetectionHelper(tflite, labels, ObjectDetectionHelper.Mode.OBJECT);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // TODO: clear out the model?
            }
            });

        // Setting the static variable on EventTime class for how long images are valid
        EventTime.setLifeTime (400);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    public void openDirectory(Uri uriToLoad) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

        startActivityForResult(intent, fileAccessPermCode);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onDestroy()
    {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tflite.close();
        nnApiDelegate.close();
        super.onDestroy();
    }

    private void bindCameraUseCases() {
        binding.viewFinder.post(new Runnable() {
            @Override
            public void run() {
                ListenableFuture<ProcessCameraProvider> camFuture = ProcessCameraProvider.getInstance(binding.viewFinder.getContext());
                    camFuture.addListener(new Runnable() {
                        @Override
                        public void run() {
                            ProcessCameraProvider cameraProvider = null;
                            try {
                                cameraProvider = camFuture.get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Preview.Builder preview_builder = new Preview.Builder();
                            preview_builder.setTargetAspectRatio(AspectRatio.RATIO_4_3);
                            preview_builder.setTargetRotation(binding.viewFinder.getDisplay().getRotation());
                            Preview preview = preview_builder.build();

                            ImageAnalysis.Builder analysis_builder = new ImageAnalysis.Builder();
                            analysis_builder.setTargetAspectRatio(AspectRatio.RATIO_4_3);
                            analysis_builder.setTargetRotation(binding.viewFinder.getDisplay().getRotation());
                            analysis_builder.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST);
                            analysis_builder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888);
                            ImageAnalysis imageAnalysis = analysis_builder.build();

                            AtomicInteger frameCounter = new AtomicInteger(0);
                            AtomicLong lastFpsTimestamp = new AtomicLong(System.currentTimeMillis());
                            ImageAnalysis.Analyzer analyzer = (ImageProxy image) ->
                            {
                                if (bitmapBuffer == null)
                                {
                                    // The image rotation and RGB image buffer are initialized only once
                                    // the analyzer has started running
                                    imageRotationDegrees = image.getImageInfo().getRotationDegrees();
                                    bitmapBuffer = Bitmap.createBitmap(
                                            image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                                }
                                if (!mImageCapture) {
                                    image.close();
                                    return;
                                }
                                taken_time = System.currentTimeMillis();
                                bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
                                tfImageBuffer.load(bitmapBuffer);
                                if (tfImageProcessor == null) {
                                    int cropSize = Math.min(bitmapBuffer.getWidth(), bitmapBuffer.getHeight());
                                    ImageProcessor.Builder ip_builder = new ImageProcessor.Builder();
                                    ip_builder.add(new ResizeWithCropOrPadOp(cropSize, cropSize));
                                    ip_builder.add(new ResizeOp(tfInputSize.getHeight(), tfInputSize.getWidth(), ResizeOp.ResizeMethod.NEAREST_NEIGHBOR));
                                    ip_builder.add(new Rot90Op(-imageRotationDegrees / 90));
                                    ip_builder.add(new NormalizeOp(0f, 1f));
                                    ip_builder.add(new CastOp(DataType.FLOAT32));
                                    tfImageProcessor = ip_builder.build();
                                }

                                // Run the model on the image, in the case of distributed operation, this only
                                // does facial recognition
                                send_time = System.currentTimeMillis();
                                TensorImage tfImage = tfImageProcessor.process(tfImageBuffer);
                                Map<Integer, ObjectDetectionHelper.ObjectPrediction> predictions =
                                        detector.predict(tfImage);
                                reportPrediction(predictions.get(new Integer(0)));
                                if (mDistributed_recognition) {
                                    for (Map.Entry<Integer, ObjectDetectionHelper.ObjectPrediction> entry : predictions.entrySet())
                                    {
                                        RectF face = entry.getValue().getLocation();
                                        int left = (int) face.left * image.getWidth();
                                        int top = (int) face.top * image.getHeight();
                                        int right = (int) face.right * image.getWidth();
                                        int bottom = (int) face.bottom * image.getHeight();
                                        for (int x = left; x >= left && x <= right; x++) {
                                            for (int y = top; y <= top && y >= bottom; y++) {
                                                bitmapBuffer.setPixel(x, y, 0);
                                            }
                                        }
                                    }
                                }
                                completion_time = System.currentTimeMillis();
                                try {
                                    if (predictions.size() > 0)
                                        writeToLog (predictions.get(new Integer(0)).getLabel ());
                                    else
                                        writeToLog ("None");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // If in distributed mode, send image to server for additional processing
                                if (mDistributed_recognition) {
                                    //send_time = sendImage (bitmapBuffer);
                                }

                                Integer frameCount = new Integer(10);
                                if (frameCounter.incrementAndGet() % TIME_BETWEEN_IMAGE_CAPTURES == 0) {
                                    frameCounter.set(0);
                                    long now = System.currentTimeMillis();
                                    long delta = now - lastFpsTimestamp.get();
                                    float fps = 1000 * frameCount.floatValue() / delta;
                                    lastFpsTimestamp.set(now);
                                }
                                image.close();
                            };
                            imageAnalysis.setAnalyzer(executor, analyzer);
                            CameraSelector.Builder camBuilder = new CameraSelector.Builder();
                            camBuilder.requireLensFacing(lensFacing);
                            CameraSelector cameraSelector = camBuilder.build();
                            cameraProvider.unbindAll();

                            LifecycleOwner owner = FullScreenCameraActivity.this;
                            cameraProvider.bindToLifecycle(owner, cameraSelector, preview, imageAnalysis);
                            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
                    }
                }, ContextCompat.getMainExecutor(binding.viewFinder.getContext()));
            }
        } );
    }

    private long sendImage(Bitmap image) {
        Log.i(log_tag + log_count, "!!!! sending image !!!!");
        ByteArrayOutputStream stream = new ByteArrayOutputStream ();
        image.compress (Bitmap.CompressFormat.PNG, 100, stream);
        byte [] png = stream.toByteArray ();
        //image.recycle (); // This causes an exception;
        stream.reset ();
        Base64.Encoder encoder = Base64.getEncoder ();
        String request = encoder.encodeToString (png);
//        HttpClient client = HttpClient.newHttpClient ();
//        HttpRequest req = HttpRequest.newBuilder ()
//            .uri (URI.create (url))
//            .POST (BodyPublishers.ofString (request))
//            .build ();
//        response = client.sendAsync (req, BodyHandlers.ofString ())
//            .thenApply (HttpResponse::body);
        String returned_label = null;
        try {
            XMLRPCClient client = new XMLRPCClient(new URL("http://192.168.40.114:8000"));

            returned_label = (String)client.call("send", request);
            char[] text_chars = new char[returned_label.length()];
            returned_label.getChars(0, returned_label.length(), text_chars, 0);
            binding.feedbackTextView.setText(text_chars, 0, returned_label.length());

        } catch(XMLRPCServerException ex) {
            // The server throw an error.
            Log.println(Log.ERROR, "XmlRpc Error", ex.getMessage());
        } catch(XMLRPCException ex) {
            // An error occured in the client.
            Log.println(Log.ERROR, "XmlRpc Error", ex.getMessage());
        } catch(Exception ex) {
            // Any other exception
            Log.println(Log.ERROR, "XmlRpc Error", ex.getMessage());
        }
        return System.currentTimeMillis();
    }

    private void writeToLog (String classification) throws IOException {
        if (mDistributed_recognition) {
            // internal
            File directory = getBaseContext().getFilesDir();
            File file = new File(directory, distributed_log_file);
            FileWriter write = new FileWriter (file, true);
            String time1 = "unknown";
            String time2 = "unknown";
            if (taken_time > send_time) {
                time1 = new String (String.valueOf (send_time));
            } else {
                time1 = new String (String.valueOf (send_time - taken_time));
            }
            if (send_time > completion_time) {
                time2 = new String (String.valueOf (completion_time));
            } else {
                time2 = new String (String.valueOf (completion_time - send_time));
            }
            String time3 = new String (String.valueOf(completion_time - taken_time));
            String written = new String (classification + "," + time1 + "," + time2 + "," + time3 + "\n");
            write.write (written);
            write.close ();
        } else {
            File directory = getBaseContext().getFilesDir();
            File file = new File(directory, local_log_file);
            FileWriter write = new FileWriter (file, true);
            String time1 = "unknown";
            String time2 = "unknown";
            if (taken_time > send_time) {
                time1 = new String (String.valueOf (send_time));
            } else {
                time1 = new String (String.valueOf (send_time - taken_time));
            }
            if (send_time > completion_time) {
                time2 = new String (String.valueOf (completion_time));
            } else {
                time1 = new String (String.valueOf (completion_time - send_time));
            }
            String time3 = new String (String.valueOf(completion_time - taken_time));
            String written = new String (classification + "," + time1 + "," + time2 + "," + time3 + "\n");
            write.write (written);
            write.close ();
        }
    }

    private void reportPrediction(ObjectDetectionHelper.ObjectPrediction prediction) {
        binding.viewFinder.post(new Runnable() {
            @Override
            public void run() {
                if (prediction == null || prediction.getScore() < ACCURACY_THRESHOLD) {
                    binding.boxPrediction.setVisibility(View.GONE);
                    //binding.feedbackTextView.setVisibility(View.GONE);
                    return;
                }
                RectF location = mapOutputCoordinates(prediction.getLocation());

                FrameLayout.LayoutParams layoutParams =
                        new FrameLayout.LayoutParams((int)(location.right - location.left), (int)(location.bottom - location.top));
                layoutParams.topMargin = (int)location.top;
                layoutParams.leftMargin = (int)location.left;
                binding.boxPrediction.setLayoutParams(layoutParams);

                String text = prediction.getLabel() + prediction.getScore().toString();
                char[] text_chars = new char[text.length()];
                text.getChars(0, text.length(), text_chars, 0);
                binding.feedbackTextView.setText(text_chars, 0, text.length());

                binding.boxPrediction.setVisibility(View.VISIBLE);
                binding.feedbackTextView.setVisibility(View.VISIBLE);
            }
        });
    }
    private RectF mapOutputCoordinates(RectF location) {
        RectF previewLocation = new RectF(
        location.left * binding.viewFinder.getWidth(),
        location.top * binding.viewFinder.getHeight(),
        location.right * binding.viewFinder.getWidth(),
        location.bottom * binding.viewFinder.getHeight());
        boolean isFrontFacing = (lensFacing == CameraSelector.LENS_FACING_FRONT);
        RectF correctedLocation = previewLocation;
        if (isFrontFacing)
        {
            correctedLocation = new RectF(
                    binding.viewFinder.getWidth() - previewLocation.right,
                    previewLocation.top,
                    binding.viewFinder.getWidth() - previewLocation.left,
                    previewLocation.bottom);
        }
        float margin = 0.1f;
        float requestedRatio = 4f / 3f;
        float midX = (correctedLocation.left + correctedLocation.right) / 2f;
        float midY = (correctedLocation.top + correctedLocation.bottom) / 2f;
        if (binding.viewFinder.getWidth() < binding.viewFinder.getHeight()) {
            correctedLocation = new RectF(
                    midX - (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                    midY - (1f - margin) * correctedLocation.height() / 2f,
                    midX + (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
                    midY + (1f - margin) * correctedLocation.height() / 2f);
        } else {
            correctedLocation = new RectF(
                midX - (1f - margin) * correctedLocation.width() / 2f,
                midY - (1f + margin) * requestedRatio * correctedLocation.height() / 2f,
                midX + (1f - margin) * correctedLocation.width() / 2f,
                midY + (1f + margin) * requestedRatio * correctedLocation.height() / 2f);
        }
        return correctedLocation;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions,
                                         int[] grantResults)
    {
        super.onRequestPermissionsResult(reqCode, permissions, grantResults);
        if (reqCode == permissionRequestCode.intValue() && hasPermissions(this)) {
            bindCameraUseCases();
        } else {
            finish();
        }
    }

    private boolean hasPermissions(Context context) {
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    /*
    * To capture full size images the images must be saved to file as bmp
    * Otherwise the images captured will be more like thumbnails
    * To make sure that the image data is private and cannot be accessed by other applications use the function getExternalFilesDir()
    * To get a directory to save the bmp files to that will be private to this application
    * because this application will be taking so many images a list of files should be kept that are being
    * recycled by getting overwritten by a new image once they have been processed by the cloud or a lite mobile model
    * Make sure that the set of file names are collision resistant
    * Perhaps begin recycling image storage every 1,000 to 10,000 photos, if not begin recycling sooner
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
        startButton.setEnabled(true);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                // the time should be a plain int in milliseconds
                Date date = new Date ();
                int time_taken = (int) date.getTime();
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String file_name = image_valid_list.addImageEventForID (time_taken);
                File image = File.createTempFile(
                        file_name,      /* prefix */
                        ".jpg",   /* suffix */
                        storageDir      /* directory */
                );

            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null && mImageCapture) {
                Uri photoURI = FileProvider.getUriForFile(this,
                                                      "com.example.android.fileprovider",
                                                      photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                //registerForActivityResult();
            }
        }
    }
/*
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }
*/
}