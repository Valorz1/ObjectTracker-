# ObjectTracker

Real-time multi-colour object tracking for Java and OpenCV. The project detects several coloured objects in a webcam feed, highlights them on screen, and estimates pan/tilt movement for a servo-controlled camera platform.

## Features
- Tracks multiple colours at the same time, including red, orange, yellow, green, blue, and purple
- Draws labelled, styled bounding boxes around the strongest detections
- Shows object status and tracking HUD information in the live camera window
- Calculates center-of-target movement for pan and tilt control
- Includes a portable OpenCV loader so it is easier to run across operating systems

## Requirements
- Java 17+ recommended
- OpenCV 4.12.0 or another compatible Java OpenCV build
- A webcam or compatible video source

## Setup
1. Install OpenCV and make sure the Java bindings are available.
2. Set the environment variable that points to your OpenCV installation:
   - Linux/macOS: `export OPENCV_HOME=/usr/local/share/opencv4`
   - Windows: `set OPENCV_HOME=C:\opencv\opencv`
3. Add the OpenCV Java jar to the build environment and make sure the native library can be loaded.
4. Compile and run the project from the workspace root.

## Run
```bash
./run.sh
```

If you are using an IDE such as Eclipse or VS Code, ensure `OPENCV_HOME` is set before launching the app.

## Tune color detection
You can override the HSV filter ranges without editing the source code by passing JVM properties at launch time.

Example:
```bash
./run.sh -Dobjecttracker.color.GREEN="40,70,70,85,255,255" \
  -Dobjecttracker.color.BLUE="95,120,70,130,255,255" \
  -Dobjecttracker.color.RED="0,120,70,10,255,255;170,120,70,180,255,255"
```

This is useful when lighting conditions change or the object colours are slightly different from the defaults.

## Controls
- `Q` or `Esc`: quit
- `H`: toggle HUD
- `P` or `Space`: pause tracking

## Notes
This version improves the project by removing the Windows-only classpath path, adding a more flexible OpenCV native library loader, and making colour tuning configurable without changing the Java source.
