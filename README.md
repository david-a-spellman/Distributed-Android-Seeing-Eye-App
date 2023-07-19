  # Distributed-Android-Seeing-Eye-App

  ... In progress...
  
*** Proof of concept worked on as a class project for CS2510 Distributed Systems at University of Pittsburgh while working towards MS in CS ***
* Uses camera to capture images at a configurable running rate once a button is pressed
* Uses the newer Android CameraX interface in order to provide better control of camera
* Uses an onboard model with tensorflow lite in order to recognize faces in frames
* Obfuscates the pixels within the bounding boxes of faces identified
* Does this in order to avoid sensitive data going out to cloud
* This is a sample of only one type of feature that might want to be Obfuscated in practice however
* Compresses the image data into an encoded base-64 form before sending the image data to a server for further classification with ML models
* A simple ressnet model from google was used for this proof of concept on server side
* will classify some basic classes such as house hold objects, building features, room features, common objects, street side objects, and some animals
* Once these classifications were made they are sent back to the client side as strings
* Both the runnable android app code and runnable Python server code are provided
* On the phone any tf-lite models can be used for recognizing entities related to privacy concerns that need Obfuscated
* The model deployed on the server side can also be changed out as long as it can take the image provided as input
* Otherwise the image should just be reshaped to complement a new model

### Other Important Details
* This app does not implement accessibility since it is a proof of concept
* Rather it is a starting point of what could be turned into a fully functional accessibility app
* Some of the code may now be slightly out of date as the last time this was actively worked on was April of 2022
* If interested in contributing you can contact me through my Pitt email at <das320@pitt.edu>

### Some info related to tensorflow lite and using Android Studios

# Using tensorflow lite <a href="https://github.com/tensorflow/examples/tree/master/lite/examples/model_personalization">TFlite Transfer Learning Pipeline</a>. The corresponding blog post is available <a href="https://aqibsaeed.github.io/on-device-activity-recognition">here</a>.

# Tools Required
* [Python 3.5+](https://www.python.org)
* [Tensorflow 2.0.0rc0](https://www.tensorflow.org)
* [Numpy](https://numpy.org/)
* [Pillow](https://pypi.org/project/Pillow/)
* [Scipy](https://scipy.org)
* [Android Studio](https://developer.android.com/studio/install)

#### Getting Started

Begin by installing Android Studio for your device and download the source code from this repository. For all other softwares and APIs, read appropriate documentation linked above to get a base level of understanding for this project.

#### Using Android Studio

Below are steps to follow to set up Android Studio Environment
1. Download Android Studio
2. Launch Android Studio
3. Click "Open Folder"
    * Ensure you open the folder from "android" in the project tree 
4. To build the application, Navigate to Build -> Make Project
5. To run the application, Navigate to Run -> Run '$NAME'
    * Running the application can be accomplished through the use of an Android Virtual Device (AVD) or a physical device.

#### Establishing an AVD

1. Navigate to Tools -> AVD Manager
2. Select "Create Virtual Device"
3. Select desired Android Device and click "Next"
4. Select desired Android build for device and click "Next"
5. Modify advanced settings as necessary, then click "Finish"

#### Using App on mobile device Over USB
1. Ensure that the phone's developers mode is activated
2. download the  appropriate driver in your phone to allow UDB debugging
   *provide more link to specific manufactures, link to google sites, samsung package
3.*edit AVD environment
Wireless debugging, provide links and provide commands to configure over network