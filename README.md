Billion Hearts - Face Detector
==============================

## Summary
This is an application that browses through the Android device gallery (DCIM folder) and detects faces of people in them.

The face detection is done with MediaPipe SDK, and uses the BlazeFace (short-range) model.

The detected images are displayed in a horizontally swipeable gallery and faces are highlighted with bounding boxes. The confidence level of the detection is displayed below the box.

Users can optionally add names of the people as tags by tapping on the bounding box, names will be displayed below the boxes after saving.

## Architecture
The app follows a simple architecture with classes separated into UI, Domain and Data packages
**UI** : Consists of UI elements from Android and the Viewmodel which controls the UI logic
* *MainActivity*, *DetectedImageFragment*, and *ImageListAdapter* controls the gallery functionality
* FaceDetectorViewModel - manages the state of the MainActivity, and runs the logic with the classes from Domain layer

**Domain**: Consists of business logic managed by following important classes
* FileDataHelper - manages reading the images from gallery
* FaceDetectorHelper - performs the actual face detection using MediaPipe

**Data** - The data model represents the different states of the Detector screen

## Building & running the app
* Download the latest Android Studio
* Clone the project from Github and Open the project from Android Studio
* Wait for Gradle sync to complete
* With your Android device connected or with an Emulator created with the AVD, and developer mode enabled, click on the green Run arrow in Android Studio.

## Limitations / Assumptions
1. To limit the scope of the gallery read, this app only scans images from the past 7 days
2. The names of the detected faces are not persisted, and will be refreshed on each app launch

## Scope for improvements
1. Processing of images could be optimised for large galleries with parallel processing for each image
2. Images can be compressed before processing to reduce the memory required for larger sized photos
3. The detections and names of detected images can be stored locally across user sessions
4. Can add Android tests which test UI flows end-to-end
5. Dependencies could be injected with Hilt