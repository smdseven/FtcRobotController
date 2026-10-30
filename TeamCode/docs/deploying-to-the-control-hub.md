# Deploy team code to the REV Control Hub

You can deploy new FTC OpModes from Android Studio on a Mac to the REV Control Hub in either of two ways:

1. **USB.** Connect the Mac directly to the Control Hub with a USB-C cable.
2. **Wi-Fi.** Connect the Mac to the Control Hub's wireless network and deploy using Android Debug Bridge (ADB).

Both methods install the same updated Robot Controller app, including the Java OpModes in this project. Neither requires disconnecting motors or sensors.

In this project, the run configuration to launch is **TeamCode**. That module is the Android app. `FtcRobotController` is a library it uses, so it does not appear as a runnable app.

## 1. Deploy via USB

Use USB for the first setup and when a wireless connection is misbehaving.

What you need:

- A Mac running Android Studio
- A USB-C data cable (USB-C to USB-C, or USB-A to USB-C with an adapter)
- A REV Control Hub with its normal 12V power supply

### Step-by-step

1. Turn on the Control Hub using the robot battery.
2. Connect the Mac to the Control Hub's USB-C port with a data-capable cable.
3. Open this FTC Robot Controller project in Android Studio.
4. Wait for Android Studio to recognize the Control Hub as an Android device.
5. Select the Control Hub in Android Studio's device dropdown.
6. Select the **TeamCode** run configuration and click the green Run button.
7. Android Studio builds the application, installs it onto the Control Hub, and launches the updated Robot Controller app.

Reconnect the Driver Hub, or keep using it, and select the new OpMode.

If the Control Hub does not appear, open Android Studio's Terminal and run:

```bash
adb devices
```

You should see a device serial number followed by `device`. If nothing appears, try another cable. Some USB-C cables charge only and do not carry data.

## 2. Deploy via Wi-Fi

Use Wi-Fi for everyday development so the robot can stay assembled.

What you need:

- A Mac running Android Studio
- A powered-on REV Control Hub
- The Control Hub Wi-Fi network name and password
- Android Debug Bridge (ADB), included with Android Studio

The Control Hub listens for wireless ADB on port `5555`. You do not need a USB cable first to turn that on.

### Step-by-step

**Step 1. Connect the Mac to the robot's Wi-Fi**

Open macOS Wi-Fi settings and connect to the network broadcast by the Control Hub. The name is usually something like `12345-RC`, using the hub's serial number.

**Step 2. Connect ADB to the hub**

From the project root, in Android Studio's Terminal or the Mac Terminal, run:

```bash
./TeamCode/scripts/connect-control-hub.sh
```

The script runs `adb connect 192.168.43.1:5555`, which is the Control Hub's address on its own Wi-Fi network. A successful run looks like this:

```text
connected to 192.168.43.1:5555

List of devices attached
192.168.43.1:5555    device
```

**Step 3. Deploy the updated OpModes**

In Android Studio:

1. Select the REV Control Hub from the device dropdown.
2. Select the **TeamCode** run configuration.
3. Click Run.
4. Wait for the build and installation to finish.
5. Use the Driver Hub to select the new OpMode.

On later visits, reconnect the Mac to the Control Hub Wi-Fi and run the script again.

### Mac troubleshooting

If the script reports that `adb` was not found, Android Studio's copy is normally here:

```text
~/Library/Android/sdk/platform-tools/adb
```

The script looks in that location automatically. You can also run it by hand:

```bash
~/Library/Android/sdk/platform-tools/adb devices
```

If Wi-Fi ADB gets stuck or reports `offline`, run:

```bash
adb disconnect
adb kill-server
adb start-server
./TeamCode/scripts/connect-control-hub.sh
```

If that still fails, confirm the Mac is joined to the Control Hub Wi-Fi, then restart the hub.

## 3. USB and Wi-Fi compared

| | USB | Wi-Fi |
| --- | --- | --- |
| Initial setup | Very easy | A few extra steps |
| Connection reliability | Excellent | Good, subject to interference |
| Deployment speed | Usually faster | May be slower |
| Robot mobility | Limited by the cable | Robot can stay untethered |
| Internet on the Mac | Unaffected | May lose internet over Wi-Fi |
| Debugging with Logcat | Yes | Yes |
| Repeated deployments | Convenient | Very convenient |
| Best use | Initial setup and troubleshooting | Everyday development and testing |

## 4. Recommendation for Teddy's Tech Team

Set up both methods, and use Wi-Fi as the primary workflow.

During practice, leave the robot assembled, connect the Mac to the robot's Wi-Fi, edit code in Android Studio, and deploy without unplugging the Control Hub. Keep a USB-C data cable in the pit for connection problems.

While the Mac is joined to the Control Hub's Wi-Fi, it may not have internet access unless it has another connection, such as a phone hotspot. That can block Android Studio dependency downloads and GitHub.

Official reference: [Managing a Control Hub](https://ftc-docs.firstinspires.org/en/latest/programming_resources/shared/managing_control_hub/Managing-a-Control-Hub.html).
