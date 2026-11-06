# Match Teleop controllers

Match Teleop uses two controllers. Gamepad 1 is the driver. Gamepad 2 is the operator, who runs the intake and the nectar and pollen shooters. Neither person uses the other controller.

Select **Match Teleop** on the Driver Station. The button names below follow the labels in the robot code: Cross, Circle, Square, Triangle, L1, L2, R1, and R2. On an Xbox-style pad those are A, B, X, Y, LB, LT, RB, and RT.

A control that is not listed does nothing. That includes stick clicks, Share, Options, and the touchpad on both controllers. The D-pad is used only while shooter trim is on. D-pad left and right do nothing.

## Gamepad 1: driver

The driver controls a mecanum drive. The left stick moves the robot, and the right stick turns it. Forward, strafe, and turn can be used together. If a combined command would ask the wheels for more than full power, every wheel is scaled down by the same amount so the direction stays the same. A saved max-speed percentage is then applied to all four wheels. It starts at 100%.

Pushing a stick farther from center asks for more speed. Letting the stick return to center stops that part of the motion.

Holding R2 at least halfway replaces the max-speed percentage with the saved slow-speed percentage. Slow speed is not multiplied by the max-speed percentage. It starts at 30%. Releasing R2 returns to the max-speed percentage.

| Control | What it does |
| --- | --- |
| Left stick up | Drive forward |
| Left stick down | Drive backward |
| Left stick right | Strafe right |
| Left stick left | Strafe left |
| Right stick right | Rotate clockwise |
| Right stick left | Rotate counterclockwise |
| Right stick up / down | Unused |
| R2 (right trigger) | Drive at the slow-speed percentage while held |

## Gamepad 2: intake and shooters

The operator's face buttons run the intake. The shoulder buttons and triggers run the two shooters. While trim is on, the right stick changes the one value selected with the D-pad.

The intake keeps running after its button is released. A shooter trigger runs one cycle and then stops that wheel.

### Intake

| Control | What it does |
| --- | --- |
| Circle | Run the intake forward at its saved speed |
| Square | Run the intake in reverse at half of that speed |
| Cross | Stop the intake |

Cross wins if it is held at the same time as Circle or Square. Holding Circle and Square together does nothing, so the two directions cannot fight.

Changing between forward and reverse does not slam the motor from one direction to the other. The intake stops for a brief moment, then starts in the new direction. Stopping with Cross is immediate.

### Shooters

L2 and L1 control the nectar shooter. R2 and R1 control the pollen shooter. The two shooters are independent. Each trigger runs one cycle for that shooter:

1. The wheel spins up at its saved speed while the gate stays closed.
2. The gate opens and stays open long enough for the balls to roll into the wheel.
3. The gate closes while the wheel is still spinning.
4. The wheel stops, and the gate stays closed.

| Control | What it does |
| --- | --- |
| L2 (left trigger) | Start one nectar shot |
| L1 (left bumper) | Cancel nectar, close its gate, and stop its wheel |
| R2 (right trigger) | Start one pollen shot |
| R1 (right bumper) | Cancel pollen, close its gate, and stop its wheel |

A trigger counts as pressed once it reaches halfway down. A light rest on the trigger does not start a shot. The trigger has to be released and pressed again before that shooter will run another cycle. Holding it through the end of a cycle does not fire again. A press during a cycle is ignored. If a bumper and its trigger are held together, the bumper wins and that cycle is cancelled.

Before any of these values have been trimmed, each shooter uses full speed, a gate block position of 0.15, an open position of 0.55, a spin-up of 0.75 seconds, a release of 1.00 seconds, and a close of 0.40 seconds. The intake starts at full forward speed. Drive max speed starts at 100% and drive slow speed starts at 30%. Trimmed values are stored on the Control Hub as they change. The next Match Teleop run, a later autonomous run, and a Control Hub reboot all start from those saved values. Autonomous driving uses the max-speed percentage.

The Driver Hub configuration needs a servo named `nectar_gate` and a servo named `pollen_gate`, one on each shooter. Both are goBILDA 2000-0025-0002 servos left in their factory positional mode.

### Trim

Trim is how the operator changes one saved value at a time during a match.

1. Hold Triangle for 3 seconds. Trim turns on. Releasing Triangle after that hold does not turn trim off.
2. D-pad up and down choose the value. The list wraps. In order, the choices are nectar speed, pollen speed, intake speed, drive max, drive slow, nectar block, nectar open, pollen block, pollen open, nectar spin-up, nectar release, nectar close, pollen spin-up, pollen release, and pollen close.
3. Use the right stick for the selected value. Only the vertical axis matters, and a stick barely off center is ignored. The left stick does not change trim values.
4. Press Triangle once to leave trim.

| Control while trim is on | What it does |
| --- | --- |
| D-pad up | Choose the previous value |
| D-pad down | Choose the next value |
| Right stick up | Raise the selected value |
| Right stick down | Lower the selected value |
| Right stick left / right | Unused |
| Left stick | Unused |

At full deflection, a speed or gate position changes by 20 percentage points per second. A wait time changes by 0.5 seconds per second. A smaller push changes the value more slowly. Speeds and gate positions stay between 0% and 100%. A wait stays between 0.05 and 5 seconds.

Choosing one gate's block or open value while that shooter is idle moves only that gate to the position being edited. Choosing another value, or leaving trim, returns an idle gate to its blocking position. A cycle that is already running keeps control of its gate. A shooter speed change takes effect immediately when that wheel is spinning. The right stick does nothing to these mechanisms while trim is off.
