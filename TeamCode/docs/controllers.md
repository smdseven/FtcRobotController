# Match Teleop controllers

Match Teleop uses two controllers. Gamepad 1 is the driver. Gamepad 2 is the operator, who runs the intake and the nectar and pollen shooters. Neither person uses the other controller.

Select **Match Teleop** on the Driver Station. The button names below follow the labels in the robot code: Cross, Circle, Square, Triangle, L1, L2, R1, and R2. On an Xbox-style pad those are A, B, X, Y, LB, LT, RB, and RT.

A control that is not listed does nothing. That includes the D-pad, stick clicks, Share, Options, and the touchpad on both controllers.

## Gamepad 1: driver

The driver controls a mecanum drive. The left stick moves the robot, and the right stick turns it. Forward, strafe, and turn can be used together. If a combined command would ask the wheels for more than full power, every wheel is scaled down by the same amount so the direction stays the same.

Pushing a stick farther from center asks for more speed. Letting the stick return to center stops that part of the motion. There is no slow mode and no drive buttons.

| Control | What it does |
| --- | --- |
| Left stick up | Drive forward |
| Left stick down | Drive backward |
| Left stick right | Strafe right |
| Left stick left | Strafe left |
| Right stick right | Rotate clockwise |
| Right stick left | Rotate counterclockwise |
| Right stick up / down | Unused |

## Gamepad 2: intake and shooters

The operator's face buttons run the intake. The shoulder buttons and triggers run the two shooters. The sticks change shooter speed only while speed trim is on.

Intake and shooter commands stay on after the button or trigger is released. Press the matching off control to stop that mechanism.

### Intake

| Control | What it does |
| --- | --- |
| Circle | Run the intake forward at full power |
| Square | Run the intake in reverse at half power |
| Cross | Stop the intake |

Cross wins if it is held at the same time as Circle or Square. Holding Circle and Square together does nothing, so the two directions cannot fight.

Changing between forward and reverse does not slam the motor from one direction to the other. The intake stops for a brief moment, then starts in the new direction. Stopping with Cross is immediate.

### Shooters

L2 and L1 control the nectar shooter. R2 and R1 control the pollen shooter. The two shooters are independent.

| Control | What it does |
| --- | --- |
| L2 (left trigger) | Turn the nectar shooter on |
| L1 (left bumper) | Turn the nectar shooter off |
| R2 (right trigger) | Turn the pollen shooter on |
| R1 (right bumper) | Turn the pollen shooter off |

A trigger counts as pressed once it is more than halfway down. A light rest on the trigger does not start a shooter. If a bumper and its trigger are held together, the bumper wins and that shooter turns off.

Each shooter runs at its saved speed. A new robot starts both speeds at 100%. Speeds that have been trimmed are remembered on the Control Hub and are used the next time an OpMode starts.

### Shooter speed trim

Trim is how the operator changes those saved speeds during a match.

1. Hold Triangle for 3 seconds. Speed trim turns on. Releasing Triangle after that hold does not turn trim off.
2. While trim is on, use the sticks. Only the vertical axis matters, and a stick barely off center is ignored.
3. Press Triangle once to leave trim. The speeds are saved when trim turns off, and again when Match Teleop ends.

| Control while trim is on | What it does |
| --- | --- |
| Left stick up | Raise nectar speed |
| Left stick down | Lower nectar speed |
| Right stick up | Raise pollen speed |
| Right stick down | Lower pollen speed |
| Left stick left / right | Unused |
| Right stick left / right | Unused |

At full deflection, a speed changes by 20 percentage points per second. A smaller push changes it more slowly. Speed stays between 0% and 100%. If that shooter is already running, the new speed takes effect immediately. The sticks do nothing to the shooters while trim is off.
