/****************************************
 * 
 *	STEAMWORKS
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	//These are values used throughout the code
	//You may change the speed of the things marked double
	static final Value out = DoubleSolenoid.Value.kForward;
	static final Value in = DoubleSolenoid.Value.kReverse;
	static final edu.wpi.first.wpilibj.Relay.Value on = Relay.Value.kForward;
	static final edu.wpi.first.wpilibj.Relay.Value off = Relay.Value.kOff;
	double intakeSpeed = 0.6;
	double shooterSpeed = 0.9;
	double climbSpeed = -0.5;//This MUST be negative
	//Auton code
	final String defaultAuto = "Default";//For a standard auton
	final String redAuton = "redAuton";//For auton on the red team
	final String blueAuton = "blueAuton";//For auton on the blue team
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	//Controllers
	Joystick joy1 = new Joystick(0);//Big joystick
	Joystick joy2 = new Joystick(1);//Afterglow xbox controller
	
	// Drive Talons
	CANTalon frontLeft = new CANTalon(1);
	CANTalon frontRight = new CANTalon(2);
    CANTalon backLeft = new CANTalon(3);
    CANTalon backRight = new CANTalon(4);
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    // Shooter and ball feeder
    Victor shooter = new Victor(0);//Shooter motor
    Victor intake = new Victor(1);//Intake motor
    Victor climb = new Victor(2);//Climbing motor
    Relay spike = new Relay(0);//Spoting light
    
    //Sensors
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();//Gyroscope
	//ADXL362 accel = new ADXL362(Range.k8G);//Accelerometer
	Timer timer = new Timer();//Timer
	Timer s = new Timer();//Special timer -don't touch
	Encoder fle = new Encoder(1);
	PressureSensor pres = new PressureSensor(0);
	
	//Solenoids
    DoubleSolenoid blocker = new DoubleSolenoid(7, 0);//Shooter solenoid
    DoubleSolenoid gearA = new DoubleSolenoid(1, 6);//One side of the gear mechanism
    DoubleSolenoid gearB =new DoubleSolenoid(2, 5);//Other side of gear mechanism
    Compressor compressor = new Compressor(0);//Air compressor
    
    //Vision
    CameraServer camera = CameraServer.getInstance();//Smartdashboard camera
    //Vision2017 vision = new Vision2017(0);
    
    //Drive stuff-don't touch
    public double x;
	public double y;
	public double rot;
	
	//Toggles
	boolean vac = false;//Intake toggle boolean
	int front = 0;//Angle for the front- 0 is gear side, 180 is shooter side
	boolean f = true;//Front toggle boolean
	boolean light = false;//Spike toggle boolean
	boolean shoot = false;//Shooter toggle boolean
	boolean reader = false;
	
	public void robotInit() {
		frontLeft.setInverted(true);//Invert the left motors
		backLeft.setInverted(true);
		gyro.calibrate();//Callibrate the gyroscope
		fle.callibrate();
		
    	chooser.addDefault("Default Auto", defaultAuto);//Add the autons to the smart dashboard
		chooser.addObject("Red Autonomous Code", redAuton);
		chooser.addObject("Blue Autonomous Code", blueAuton);
		SmartDashboard.putData("Auton choices", chooser);
		
		compressor.start();//Start the compressor
		camera.startAutomaticCapture("cam0", 0);//Start the camera
		s.start();//Special timer
		read();
		
		frontLeft.enableBrakeMode(false);
    }
    
	public void autonomousInit() {
		autoSelected = chooser.getSelected();//Select the auton
    }
    public void autonomousPeriodic() {
    	timer.reset();
    	while(isAutonomous() && isEnabled() && timer.get() <= 15) {
	    	switch(autoSelected) {
	    	case defaultAuto:
	    		DefaultAuto();
	    		break;
	    	case redAuton:
	    		RedAuton();
	    		break;
	    	case blueAuton:
	    		BlueAuton();
	    		break;
	    	}
    	}
    }
    
	public void teleopPeriodic() {
		timer.reset();
    	while(isOperatorControl() && isEnabled()) {
    		//If nothing is being read by a controller, stop.
    		if(joy1.getRawButton(1) || joy1.getRawButton(2) || joy1.getRawButton(3) || joy1.getRawButton(4) || joy1.getRawButton(5) || joy1.getRawButton(6) || joy1.getRawButton(7) || joy1.getRawButton(8) || joy1.getRawButton(9) || joy1.getRawButton(10) ||  joy2.getRawButton(1) || joy2.getRawButton(2) || joy2.getRawButton(3) || joy2.getRawButton(4) || joy2.getRawButton(5) || joy2.getRawButton(6) || joy2.getRawButton(7) || joy2.getRawButton(8) || joy2.getRawButton(9) || joy2.getRawButton(10) || joy1.getRawAxis(0) >= 0.05 || joy1.getRawAxis(1) >= 0.05 || joy1.getRawAxis(2) >= 0.05 || joy1.getRawAxis(3) >= 0.05 || joy1.getRawAxis(4) >= 0.05 || joy1.getRawAxis(5) >= 0.05 || joy1.getRawAxis(6) >= 0.05 || joy2.getRawAxis(0) >= 0.05 || joy2.getRawAxis(1) >= 0.05 || joy2.getRawAxis(2) >= 0.05 || joy2.getRawAxis(3) >= 0.05 || joy2.getRawAxis(4) >= 0.05 || joy2.getRawAxis(5) >= 0.05 || joy2.getRawAxis(6) >= 0.05 || joy1.getRawAxis(0) <= -0.05 || joy1.getRawAxis(1) <= -0.05 || joy1.getRawAxis(2) <= -0.05 || joy1.getRawAxis(3) <= -0.05 || joy1.getRawAxis(4) <= -0.05 || joy1.getRawAxis(5) <= -0.05 || joy1.getRawAxis(6) <= -0.05 || joy2.getRawAxis(0) <= -0.05 || joy2.getRawAxis(1) <= -0.05 || joy2.getRawAxis(2) <= -0.05 || joy2.getRawAxis(3) <= -0.05 || joy2.getRawAxis(4) <= -0.05 || joy2.getRawAxis(5) <= -0.05 || joy2.getRawAxis(6) <= -0.05) {
    			/***********************
	    		 *** DRIVER CONTROLS ***
	    		 ***********************/
    			//Brake
	    		while(joy1.getRawButton(1)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, front);
	    			read();//Contunue reading from sensors
	    		}
	    		
	    		//Toggle the light on/off with a boolean
	    		if(joy1.getRawButton(3) && !light) {
	    			light = true;
	    			while(joy1.getRawButton(3)) {}
	    		}
	    		if(joy1.getRawButton(3) && light) {
	    			light = false;
	    			while(joy1.getRawButton(3)) {}
	    		}
	    		if(light || shoot) {
	    			spike.set(on);
	    			reader = true;
	    		} else {
	    			spike.set(off);
	    			reader = false;
	    		}
	    		
    			//Changing the front with a boolean
    			if(joy1.getRawButton(4) && !f) {
	    			f = true;
	    			while(joy1.getRawButton(4)) {}
	    		}
	    		if(joy1.getRawButton(4) && f) {
	    			f = false;
	    			while(joy1.getRawButton(4)) {}
	    		}
	    		if(f) {
	    			front = 180;
	    		} else {
	    			front = 0;
	    		}
	    		
	    		//Climbing code
	    		if(joy1.getRawButton(7)) {//press button 7
    				climb.set(climbSpeed);
    			} else {
    				climb.set(0);
    			}
	    		
	    		//Pressing button 2 gives you half speeds
	    		x = Math.pow(joy1.getRawAxis(0), 3);
	    		y = Math.pow(joy1.getRawAxis(1), 3);
	    		rot = -Math.pow(joy1.getRawAxis(2), 3)/2;
	    		
	    		//Drive w/ joystick
	    		if((Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) && joy1.getRawButton(2)) {
	    			mainDrive.mecanumDrive_Cartesian(x/2, y/2, rot/2, front);
	    		} else if(Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, front);
	    		}
	    		
	    		//POV side-to-side
	    		while(joy1.getPOV()!=-1 && !joy1.getRawButton(1)) {
	    			int pov = joy1.getPOV();
	    			double a = 1;
	    			if(joy1.getRawButton(2)) {//Half speeds
	    				a = 0.5;
	    			} else {
	    				a = 1;
	    			}
	    			if(pov >= 45 && pov <= 135) {
	    				mainDrive.mecanumDrive_Cartesian(0.5*a, 0, 0, front);
	    			} 
	    			if(pov >= 225 && pov <= 305) {
	    				mainDrive.mecanumDrive_Cartesian(-0.5*a, 0, 0, front);
	    			}
	    			read();
	    		}
	    		
	    		while(joy1.getRawButton(9)) {
	    			double z = 50 * joy1.getRawAxis(3);
	    			frontLeft.set(z);
	    		}
	    		
	    		if(joy1.getRawButton(10)) {
	    			frontLeft.setEncPosition(0);
	    		}
	    		
	    		/************************
	    		 * MANIPULATOR CONTROLS *
	    		 ************************/
	    		/*
	    		//Shooter code
	    		if(joy2.getRawButton(1) && !shoot) {
	    			shoot = true;
	    			while(joy2.getRawButton(1)) {}
	    		}
	    		if(joy2.getRawButton(1) && shoot) {
	    			shoot = false;
	    			while(joy2.getRawButton(1)) {}
	    		}
	    		if(shoot) {
	    			if(s.get() > 6) {
	    				s.reset();
	    				s.start();
	    			} else if(s.get() <= 2) {
	    				shooter.set(shooterSpeed);
	    				blocker.set(out);
	    			} else if(s.get() > 2 && s.get() <= 4) {
	    				shooter.set(shooterSpeed);
	    				blocker.set(in);
	    			} else if(s.get() > 4 && s.get() <= 6) {
	    				shooter.set(shooterSpeed);
	    				blocker.set(out);
	    			}
	    		} else {
	    			shooter.set(0);
	    			blocker.set(out);
	    		}
	    		*/
	    		
	    		if(joy2.getRawButton(1)) {
	    			shooter.set(shooterSpeed);//Turn on motor
	    			blocker.set(in);//unblock
	    		} else {
	    			shooter.set(0);//Turn off motor
	    			blocker.set(out);//continue blocking
	    		}
	    		
	    		
	    		//Ball picker system toggle with boolean
	    		if(joy2.getRawButton(3) && !vac) {
	    			vac = true;
	    			while(joy2.getRawButton(3)) {}
	    		}
	    		if(joy2.getRawButton(3) && vac) {
	    			vac = false;
	    			while(joy2.getRawButton(3)) {}
	    		}
	    		if(vac) {
	    			intake.set(intakeSpeed);
	    		} else {
	    			intake.set(0);
	    		}
	    		
	    		//Drop gear
    			if(joy2.getRawButton(2)) {
    				gearA.set(in);//Open gear pistons
    				gearB.set(in);
    			} else {
    				gearA.set(out);//Close gear pistons
    				gearB.set(out);
    			}
	    		
    		} else {
    			//Stop driving if nothing is being read from the controllers
    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    		}
    		read();
    	}
    }
    public void testPeriodic() {
    }
    
   
    void read() {//Read from the sensors
    	SmartDashboard.putBoolean("Front side green=gear red=shooter", f);//Tell which side is front
    	SmartDashboard.putBoolean("Shooter green=on red=off", shoot);//Tell if shooter is on
    	SmartDashboard.putBoolean("Light red=off green=on", reader);//Tell if the light is on
    	SmartDashboard.putNumber("Pressure Sensor", pres.getPres());
    	SmartDashboard.putNumber("encoder", frontLeft.getEncPosition()/4000 * Math.PI);
    }

    
	private void BlueAuton() {
		while(timer.get() <= 5) {
			mainDrive.mecanumDrive_Cartesian(0, 0.75, 0, gyro.getAngle());
		}
		while(timer.get() <= 6 && gyro.getAngle() < 25) {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0.5, 0);
		}
		while(timer.get() <= 15) {
			//Vision then shoot
		}
	}

	private void RedAuton() {
		while(timer.get() <= 5) {
			mainDrive.mecanumDrive_Cartesian(0, 0.75, 0, gyro.getAngle());
		}
		while(timer.get() <= 6 && gyro.getAngle() > -25) {
			mainDrive.mecanumDrive_Cartesian(0, 0, -0.5, 0);
		}
		while(timer.get() <= 15) {
			//Vision then shoot
		}
	}
	private void DefaultAuto() {
		if(timer.get() < 15 && fle.getEncPos() < 100) {
			mainDrive.mecanumDrive_Cartesian(0, 0.5, 0, gyro.getAngle());
		} else {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		}
	}
}
