/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008-2014. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.can;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.junittoolbox.PollingWait;
import com.googlecode.junittoolbox.RunnableAssert;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Timer;

/**
 * @author jonathanleitschuh
 *
 */
public class CANDefaultTest extends AbstractCANTest{
	private static final Logger logger = Logger.getLogger(CANDefaultTest.class.getName());
	
	private static final double kSpikeTime = .5;
	
	@Override
	protected Logger getClassLogger() {
		return logger;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		getME().getMotor().enableControl();
		getME().getMotor().set(0.0f);
		/* The motor might still have momentum from the previous test. */
		Timer.delay(kStartupTime);
	}
	
	@Test
	public void testDefaultGet(){
		assertEquals("CAN Jaguar did not initilize stopped", 0.0, getME().getMotor().get(), .01f);
	}
	
	@Test
	public void testDefaultBusVoltage(){
		assertEquals("CAN Jaguar did not start at 14 volts", 14.0f, getME().getMotor().getBusVoltage(), 2.0f);
	}
	
	@Test
	public void testDefaultOutputVoltage(){
		assertEquals("CAN Jaguar did not start with an output voltage of 0", 0.0f, getME().getMotor().getOutputVoltage(), 0.3f);

	}
	
	@Test
	public void testDefaultOutputCurrent(){
		assertEquals("CAN Jaguar did not start with an output current of 0", 0.0f, getME().getMotor().getOutputCurrent(), 0.3f);
	}
	
	@Test
	public void testDefaultTemperature(){
		double room_temp = 18.0f;
		assertThat("CAN Jaguar did not start with an initial temperature greater than " + room_temp, getME().getMotor().getTemperature(), is(greaterThan(room_temp)));
	}
	
	@Test
	public void testDefaultForwardLimit(){
		getME().getMotor().configLimitMode(CANJaguar.LimitMode.SwitchInputsOnly);
		assertTrue("CAN Jaguar did not start with the Forward Limit Switch Off", getME().getMotor().getForwardLimitOK());
	}
	
	@Test
	public void testDefaultReverseLimit(){
		getME().getMotor().configLimitMode(CANJaguar.LimitMode.SwitchInputsOnly);
		assertTrue("CAN Jaguar did not start with the Reverse Limit Switch Off", getME().getMotor().getReverseLimitOK());
	}
	
	@Test
	public void testDefaultNoFaults(){
		assertEquals("CAN Jaguar initialized with Faults", 0, getME().getMotor().getFaults());
	}
	
	
	
	@Test
	public void testFakeLimitSwitchForwards() {
		//Given
		getME().getMotor().configLimitMode(CANJaguar.LimitMode.SwitchInputsOnly);
		getME().getMotor().enableControl();
		assertTrue("[TEST SETUP] CANJaguar did not start with the Forward Limit Switch low", getME().getMotor().getForwardLimitOK());
		
		//When
		getME().getForwardLimit().set(true);
		
		//Then
		PollingWait wait = new PollingWait().timeoutAfter((long)kLimitSettlingTime, TimeUnit.SECONDS).pollEvery(1, TimeUnit.MILLISECONDS);
		wait.until(new RunnableAssert("Setting the CANJAguar forward limit switch high") {
			@Override
			public void run() throws Exception {
				getME().getMotor().set(0);
				assertFalse("Setting the forward limit switch high did not cause the forward limit switch to trigger", getME().getMotor().getForwardLimitOK());
			}
		});
	}

	
	@Test
	public void testFakeLimitSwitchReverse() {
		//Given
		getME().getMotor().configLimitMode(CANJaguar.LimitMode.SwitchInputsOnly);
		getME().getMotor().enableControl();
		assertTrue("[TEST SETUP] CANJaguar did not start with the Reverse Limit Switch low", getME().getMotor().getReverseLimitOK());
		//When
		getME().getReverseLimit().set(true);
		
		//Then
		PollingWait wait = new PollingWait().timeoutAfter((long)kLimitSettlingTime, TimeUnit.SECONDS).pollEvery(1, TimeUnit.MILLISECONDS);
		wait.until(new RunnableAssert("Setting the CANJAguar reverse limit switch high") {
			@Override
			public void run() throws Exception {
				getME().getMotor().set(0);
				assertFalse("Setting the reverse limit switch high did not cause the forward limit switch to trigger", getME().getMotor().getReverseLimitOK());
			}
		});
	}
	
	@Ignore("Brown out not working needs further testing")
	@Test
	public void testPositionModeVerifiesOnBrownOut() {
		final double setpoint = 10.0;
		
		//Given
		getME().getMotor().setPositionMode(CANJaguar.kQuadEncoder, 360, 10.0, 0.1, 0.0);
		getME().getMotor().enableControl();
		setCANJaguar(kMotorTime, 0.0);
		
		getME().powerOn();
		
		//When
		/* Turn the spike off and on again */
		
		getME().powerOff();
		Timer.delay(kSpikeTime);
		getME().powerOn();
		Timer.delay(kSpikeTime);
		
		PollingWait wait = new PollingWait().timeoutAfter(15, TimeUnit.SECONDS).pollEvery(1, TimeUnit.MILLISECONDS);
		/* The jaguar should automatically get set to quad encoder position mode,
			so it should be able to reach a setpoint in a couple seconds. */
		
		wait.until(new RunnableAssert("Waiting for CANJaguar to reach set-point") {
			@Override
			public void run() throws Exception {
				getME().getMotor().set(setpoint);
				assertEquals("CANJaguar should have resumed PID control after power cycle",
						setpoint, getME().getMotor().getPosition(), kEncoderPositionTolerance);
			}
		});
	}
}