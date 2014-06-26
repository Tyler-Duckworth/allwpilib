/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.							  */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in $(WIND_BASE)/WPILib.  */
/*----------------------------------------------------------------------------*/
#pragma once

#include "simulation/SimEncoder.h"
#include "CounterBase.h"
#include "SensorBase.h"
#include "Counter.h"
#include "PIDSource.h"
#include "LiveWindow/LiveWindowSendable.h"

/**
 * Class to read quad encoders.
 * Quadrature encoders are devices that count shaft rotation and can sense direction. The output of
 * the QuadEncoder class is an integer that can count either up or down, and can go negative for
 * reverse direction counting. When creating QuadEncoders, a direction is supplied that changes the
 * sense of the output to make code more readable if the encoder is mounted such that forward movement
 * generates negative values. Quadrature encoders have two digital outputs, an A Channel and a B Channel
 * that are out of phase with each other to allow the FPGA to do direction sensing.
 */
class Encoder : public SensorBase, public CounterBase, public PIDSource, public LiveWindowSendable
{
public:

	Encoder(uint32_t aChannel, uint32_t bChannel, bool reverseDirection = false,
			EncodingType encodingType = k4X);
	// TODO: [Not Supported] Encoder(DigitalSource *aSource, DigitalSource *bSource, bool reverseDirection=false, EncodingType encodingType = k4X);
	// TODO: [Not Supported] Encoder(DigitalSource &aSource, DigitalSource &bSource, bool reverseDirection=false, EncodingType encodingType = k4X);
	virtual ~Encoder();

	// CounterBase interface
	void Start();
	int32_t Get();
	int32_t GetRaw();
	void Reset();
	void Stop();
	double GetPeriod();
	void SetMaxPeriod(double maxPeriod);
	bool GetStopped();
	bool GetDirection();
	double GetDistance();
	double GetRate();
	void SetMinRate(double minRate);
	void SetDistancePerPulse(double distancePerPulse);
	void SetReverseDirection(bool reverseDirection);
	void SetSamplesToAverage(int samplesToAverage);
	int GetSamplesToAverage();
	void SetPIDSourceParameter(PIDSourceParameter pidSource);
	double PIDGet();

	void UpdateTable();
	void StartLiveWindowMode();
	void StopLiveWindowMode();
	std::string GetSmartDashboardType();
	void InitTable(ITable *subTable);
	ITable * GetTable();

private:
	void InitEncoder(int channelA, int channelB, bool _reverseDirection, EncodingType encodingType);
	double DecodingScaleFactor();

	// TODO: [Not Supported] DigitalSource *m_aSource;		// the A phase of the quad encoder
	// TODO: [Not Supported] DigitalSource *m_bSource;		// the B phase of the quad encoder
    // TODO: [Not Supported] bool m_allocatedASource;		// was the A source allocated locally?
    // TODO: [Not Supported] bool m_allocatedBSource;		// was the B source allocated locally?
	int channelA, channelB;
	double m_distancePerPulse;		// distance of travel for each encoder tick
	EncodingType m_encodingType;	// Encoding type
	PIDSourceParameter m_pidSource;	// Encoder parameter that sources a PID controller
	bool reversed;
	SimEncoder* impl;

	ITable *m_table;
};