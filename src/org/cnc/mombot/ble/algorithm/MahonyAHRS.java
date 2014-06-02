package org.cnc.mombot.ble.algorithm;

/**
 * MahonyAHRS class. Madgwick's implementation of Mayhony's AHRS algorithm. See:
 * http://www.x-io.co.uk/node/8#open_source_ahrs_and_imu_algorithms
 * 
 * @author thanhle
 * 
 */
public class MahonyAHRS {
	/**
	 * Gets or sets the sample period.
	 */
	public float samplePeriod;

	/**
	 * Gets or sets the algorithm proportional gain.
	 */
	public float kp;

	/**
	 * Gets or sets the algorithm integral gain.
	 */
	public float ki;

	/**
	 * Gets or sets the Quaternion output.
	 */
	public float[] quaternion;

	/**
	 * Gets or sets the integral error.
	 */
	private float[] eInt;

	/**
	 * Initializes a new instance of the MahonyAHRS class.
	 * 
	 * @param samplePeriod
	 *            Sample period.
	 */
	public MahonyAHRS(float samplePeriod) {
		this(samplePeriod, 1f, 0f);
	}

	/**
	 * Initializes a new instance of the MahonyAHRS class.
	 * 
	 * @param samplePeriod
	 *            Sample period.
	 * @param kp
	 *            Algorithm proportional gain.
	 */
	public MahonyAHRS(float samplePeriod, float kp) {
		this(samplePeriod, kp, 0f);
	}

	/**
	 * Initializes a new instance of the MahonyAHRS class.
	 * 
	 * @param samplePeriod
	 *            Sample period.
	 * @param kp
	 *            Algorithm proportional gain.
	 * @param ki
	 *            Algorithm integral gain.
	 */
	public MahonyAHRS(float samplePeriod, float kp, float ki) {
		this.samplePeriod = samplePeriod;
		this.kp = kp;
		this.ki = ki;
		this.quaternion = new float[] { 1f, 0f, 0f, 0f };
		this.eInt = new float[] { 0f, 0f, 0f };
	}

	/**
	 * Algorithm AHRS update method. Requires only gyroscope and accelerometer data. Optimised for minimal arithmetic.
	 * 
	 * @param gx
	 *            Gyroscope x axis measurement in radians/s.
	 * @param gy
	 *            Gyroscope y axis measurement in radians/s.
	 * @param gz
	 *            Gyroscope z axis measurement in radians/s.
	 * @param ax
	 *            Accelerometer x axis measurement in any calibrated units.
	 * @param ay
	 *            Accelerometer y axis measurement in any calibrated units.
	 * @param az
	 *            Accelerometer z axis measurement in any calibrated units.
	 * @param mx
	 *            Magnetometer x axis measurement in any calibrated units.
	 * @param my
	 *            Magnetometer y axis measurement in any calibrated units.
	 * @param mz
	 *            Magnetometer z axis measurement in any calibrated units.
	 */
	public void update(float gx, float gy, float gz, float ax, float ay, float az, float mx, float my, float mz) {
		float q1 = quaternion[0], q2 = quaternion[1], q3 = quaternion[2], q4 = quaternion[3]; // short name local
																								// variable for
																								// readability
		float norm;
		float hx, hy, bx, bz;
		float vx, vy, vz, wx, wy, wz;
		float ex, ey, ez;
		float pa, pb, pc;

		// Auxiliary variables to avoid repeated arithmetic
		float q1q1 = q1 * q1;
		float q1q2 = q1 * q2;
		float q1q3 = q1 * q3;
		float q1q4 = q1 * q4;
		float q2q2 = q2 * q2;
		float q2q3 = q2 * q3;
		float q2q4 = q2 * q4;
		float q3q3 = q3 * q3;
		float q3q4 = q3 * q4;
		float q4q4 = q4 * q4;

		// Normalise accelerometer measurement
		norm = (float) Math.sqrt(ax * ax + ay * ay + az * az);
		if (norm == 0f)
			return; // handle NaN
		norm = 1 / norm; // use reciprocal for division
		ax *= norm;
		ay *= norm;
		az *= norm;

		// Normalise magnetometer measurement
		norm = (float) Math.sqrt(mx * mx + my * my + mz * mz);
		if (norm == 0f)
			return; // handle NaN
		norm = 1 / norm; // use reciprocal for division
		mx *= norm;
		my *= norm;
		mz *= norm;

		// Reference direction of Earth's magnetic field
		hx = 2f * mx * (0.5f - q3q3 - q4q4) + 2f * my * (q2q3 - q1q4) + 2f * mz * (q2q4 + q1q3);
		hy = 2f * mx * (q2q3 + q1q4) + 2f * my * (0.5f - q2q2 - q4q4) + 2f * mz * (q3q4 - q1q2);
		bx = (float) Math.sqrt((hx * hx) + (hy * hy));
		bz = 2f * mx * (q2q4 - q1q3) + 2f * my * (q3q4 + q1q2) + 2f * mz * (0.5f - q2q2 - q3q3);

		// Estimated direction of gravity and magnetic field
		vx = 2f * (q2q4 - q1q3);
		vy = 2f * (q1q2 + q3q4);
		vz = q1q1 - q2q2 - q3q3 + q4q4;
		wx = 2f * bx * (0.5f - q3q3 - q4q4) + 2f * bz * (q2q4 - q1q3);
		wy = 2f * bx * (q2q3 - q1q4) + 2f * bz * (q1q2 + q3q4);
		wz = 2f * bx * (q1q3 + q2q4) + 2f * bz * (0.5f - q2q2 - q3q3);

		// Error is cross product between estimated direction and measured direction of gravity
		ex = (ay * vz - az * vy) + (my * wz - mz * wy);
		ey = (az * vx - ax * vz) + (mz * wx - mx * wz);
		ez = (ax * vy - ay * vx) + (mx * wy - my * wx);
		if (ki > 0f) {
			eInt[0] += ex; // accumulate integral error
			eInt[1] += ey;
			eInt[2] += ez;
		} else {
			eInt[0] = 0.0f; // prevent integral wind up
			eInt[1] = 0.0f;
			eInt[2] = 0.0f;
		}

		// Apply feedback terms
		gx = gx + kp * ex + ki * eInt[0];
		gy = gy + kp * ey + ki * eInt[1];
		gz = gz + kp * ez + ki * eInt[2];

		// Integrate rate of change of quaternion
		pa = q2;
		pb = q3;
		pc = q4;
		q1 = q1 + (-q2 * gx - q3 * gy - q4 * gz) * (0.5f * samplePeriod);
		q2 = pa + (q1 * gx + pb * gz - pc * gy) * (0.5f * samplePeriod);
		q3 = pb + (q1 * gy - pa * gz + pc * gx) * (0.5f * samplePeriod);
		q4 = pc + (q1 * gz + pa * gy - pb * gx) * (0.5f * samplePeriod);

		// Normalise quaternion
		norm = (float) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);
		norm = 1.0f / norm;
		quaternion[0] = q1 * norm;
		quaternion[1] = q2 * norm;
		quaternion[2] = q3 * norm;
		quaternion[3] = q4 * norm;
	}

	/**
	 * Algorithm IMU update method. Requires only gyroscope and accelerometer data.
	 * 
	 * @param gx
	 *            Gyroscope x axis measurement in radians/s.
	 * @param gy
	 *            Gyroscope y axis measurement in radians/s.
	 * @param gz
	 *            Gyroscope z axis measurement in radians/s.
	 * @param ax
	 *            Accelerometer x axis measurement in any calibrated units.
	 * @param ay
	 *            Accelerometer y axis measurement in any calibrated units.
	 * @param az
	 *            Accelerometer z axis measurement in any calibrated units.
	 */
	public void update(float gx, float gy, float gz, float ax, float ay, float az) {
		float q1 = quaternion[0], q2 = quaternion[1], q3 = quaternion[2], q4 = quaternion[3]; // short name local
																								// variable for
																								// readability
		float norm;
		float vx, vy, vz;
		float ex, ey, ez;
		float pa, pb, pc;

		// Normalise accelerometer measurement
		norm = (float) Math.sqrt(ax * ax + ay * ay + az * az);
		if (norm == 0f)
			return; // handle NaN
		norm = 1 / norm; // use reciprocal for division
		ax *= norm;
		ay *= norm;
		az *= norm;

		// Estimated direction of gravity
		vx = 2.0f * (q2 * q4 - q1 * q3);
		vy = 2.0f * (q1 * q2 + q3 * q4);
		vz = q1 * q1 - q2 * q2 - q3 * q3 + q4 * q4;

		// Error is cross product between estimated direction and measured direction of gravity
		ex = (ay * vz - az * vy);
		ey = (az * vx - ax * vz);
		ez = (ax * vy - ay * vx);
		if (ki > 0f) {
			eInt[0] += ex; // accumulate integral error
			eInt[1] += ey;
			eInt[2] += ez;
		} else {
			eInt[0] = 0.0f; // prevent integral wind up
			eInt[1] = 0.0f;
			eInt[2] = 0.0f;
		}

		// Apply feedback terms
		gx = gx + kp * ex + ki * eInt[0];
		gy = gy + kp * ey + ki * eInt[1];
		gz = gz + kp * ez + ki * eInt[2];

		// Integrate rate of change of quaternion
		pa = q2;
		pb = q3;
		pc = q4;
		q1 = q1 + (-q2 * gx - q3 * gy - q4 * gz) * (0.5f * samplePeriod);
		q2 = pa + (q1 * gx + pb * gz - pc * gy) * (0.5f * samplePeriod);
		q3 = pb + (q1 * gy - pa * gz + pc * gx) * (0.5f * samplePeriod);
		q4 = pc + (q1 * gz + pa * gy - pb * gx) * (0.5f * samplePeriod);

		// Normalise quaternion
		norm = (float) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4);
		norm = 1.0f / norm;
		quaternion[0] = q1 * norm;
		quaternion[1] = q2 * norm;
		quaternion[2] = q3 * norm;
		quaternion[3] = q4 * norm;
	}
}
