/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplicationandroidseeingeyeapplication;

import android.graphics.RectF;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import java.util.*;

/**
 * Helper class used to communicate between our app and the TF object detection model
 */
//class ObjectDetectionHelper(private val tflite: Interpreter, private val labels: List<String>) {
class ObjectDetectionHelper {
    private Interpreter tflite = null;
    private List<String> labels = null;

    /** Abstraction object that wraps a prediction output in an easy to parse way */
    //data class ObjectPrediction(val location: RectF, val label: String, val score: Float)
    public class ObjectPrediction {
        private RectF location;
        private String label;
        private Float score;
        public ObjectPrediction(RectF loc, String l, Float sc) {
            location = loc;
            label = l;
            score = sc;
        }
        public RectF getLocation() { return location; }
        public String getLabel() { return label; }
        public Float getScore() { return score; }
    }

    //private val locations = arrayOf(Array(OBJECT_COUNT) { FloatArray(4) })
    private float[][][] locations = new float[1][OBJECT_COUNT][4];
    //private val labelIndices =  arrayOf(FloatArray(OBJECT_COUNT))
    private float[][] labelIndices = new float[1][OBJECT_COUNT];
    //private val scores =  arrayOf(FloatArray(OBJECT_COUNT))
    private float[][] scores = new float[1][OBJECT_COUNT];

    private float[][][] face_pts = new float[1][896][16];
    private float[][][] face_scores = new float[1][896][1];

    /*
    private val outputBuffer = mapOf(
        0 to locations,
        1 to labelIndices,
        2 to scores,
        3 to FloatArray(1)
    )*/
    private Map<Integer, Object> outputBuffer = new HashMap<Integer, Object>();

/*
    val predictions get() = (0 until OBJECT_COUNT).map {
        ObjectPrediction(

            // The locations are an array of [0, 1] floats for [top, left, bottom, right]
            location = locations[0][it].let {
                RectF(it[1], it[0], it[3], it[2])
            },

            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes + 1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            label = labels[1 + labelIndices[0][it].toInt()],

            // Score is a single value of [0, 1]
            score = scores[0][it]
        )
    }*/
    public Map<Integer, ObjectPrediction> getPredictions(int num_detections)
    {
        Map<Integer, ObjectPrediction> predictions = new HashMap<Integer, ObjectPrediction>();
        for (int i = 0; i < num_detections; ++i)
        {
            String label = labels.get((int)labelIndices[0][i]);
            predictions.put(i, new ObjectPrediction(
                    new RectF(locations[0][i][0], locations[0][i][1], locations[0][i][2], locations[0][i][3]),
                    label, scores[0][i]));
        }
        return predictions;
    }

    public enum Mode { FACE, OBJECT};
    private Mode my_mode = Mode.FACE;

    public ObjectDetectionHelper(Interpreter interpreter, List<String> l, Mode mode) {
        tflite = interpreter;
        labels = l;
        my_mode = mode;
        switch (my_mode)
        {
            case FACE:
                outputBuffer.put( 0, face_pts);
                outputBuffer.put( 1, face_scores);
                break;
            case OBJECT:
                outputBuffer.put(0, locations);
                outputBuffer.put(1, labelIndices);
                outputBuffer.put(2, scores);
                outputBuffer.put(3, new float[1]);
                break;
        }
    }

    /*
    fun predict(image: TensorImage): List<ObjectPrediction> {
        tflite.runForMultipleInputsOutputs(arrayOf(image.buffer), outputBuffer)
        return predictions
    }*/
    public Map<Integer, ObjectPrediction> predict(TensorImage image)
    {
        Object[] inputs = new Object[1];
        inputs[0] = image.getBuffer();

        tflite.runForMultipleInputsOutputs(inputs, outputBuffer);
	
        switch (my_mode)
        {
            case FACE: {
                float left = 0;
                float right = 0;
                float top = 0;
                float bottom = 0;
                float x = 0;
                float y = 0;
                int size = 0;
                for (int i = 0; i < 896; i++) {
                    if (face_scores[0][i][0] > 0.5) {
                        size++;
                    }
                }
                locations = new float[1][size][4];
                labelIndices = new float[1][size];
                scores = new float[1][size];
                int s = -1;
                for (int i = 0; i < 896; i++) {
                    if (face_scores[0][i][0] > 0.5) {
                        s++;
                        left = 640;
                        right = 0;
                        top = 0;
                        bottom = 640;
                        for (int j = 0; j < 16; j += 2) {
                            x = face_pts[0][i][j];
                            y = face_pts[0][i][j + 1];
                            if (x < left) {
                                left = x;
                            }
                            if (y < top) {
                                top = x;
                            }
                            if (x > right) {
                                right = x;
                            }
                            if (y > bottom) {
                                bottom = y;
                            }
                        }
                        locations[0][s][0] = left;
                        locations[0][s][1] = top;
                        locations[0][s][2] = right;
                        locations[0][s][3] = bottom;
                        labelIndices[0][s] = 0;
                        scores[0][s] = face_scores[0][i][0];
                    }
                }
                return getPredictions(size);
            }
        }
 
        return getPredictions(OBJECT_COUNT);
    }

//    companion object {
//        const val OBJECT_COUNT = 10
//    }
    final static int OBJECT_COUNT = 10;
}