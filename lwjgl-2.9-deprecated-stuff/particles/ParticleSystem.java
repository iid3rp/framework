package framework.particles;

import framework.hardware.DisplayManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Random;

public class ParticleSystem {

	private float pps, averageSpeed, gravityCompliant, averageLifeLength, averageScale;

	private float speedError, lifeError, scaleError = 0;
	private boolean randomRotation = false;
	private Vector3f direction;
	private float directionDeviation = 0;
	private ParticleTexture texture;

	private Random random = new Random();

	public ParticleSystem(float pps, float speed, float gravityCompliant, float lifeLength, float scale, ParticleTexture texture) {
		this.texture = texture;
		this.pps = pps;
		this.averageSpeed = speed;
		this.gravityCompliant = gravityCompliant;
		this.averageLifeLength = lifeLength;
		this.averageScale = scale;
	}

	public ParticleSystem(float pps, float speed, float gravityCompliant, float lifeLength, float scale) {
		this.pps = pps;
		this.averageSpeed = speed;
		this.gravityCompliant = gravityCompliant;
		this.averageLifeLength = lifeLength;
		this.averageScale = scale;
	}

	/**
	 * @param direction - The average direction in which particles are emitted.
	 * @param deviation - A value between zero and one indicating how far from the chosen direction particles can deviate.
	 */
	public void setDirection(Vector3f direction, float deviation) {
		this.direction = new Vector3f(direction);
		this.directionDeviation = (float) (deviation * Math.PI);
	}

	public void randomizeRotation() {
		randomRotation = true;
	}

	/**
	 * @param error
	 *            - A number between 0 and one, where 0 means no error margin.
	 */
	public void setSpeedError(float error) {
		this.speedError = error * averageSpeed;
	}

	/**
	 * @param error
	 *            - A number between 0 and one, where 0 means no error margin.
	 */
	public void setLifeError(float error) {
		this.lifeError = error * averageLifeLength;
	}

	/**
	 * @param error
	 *            - A number between 0 and one, where 0 means no error margin.
	 */
	public void setScaleError(float error) {
		this.scaleError = error * averageScale;
	}

	public void generateParticles(Vector3f systemCenter) {
		float delta = DisplayManager.getDeltaInSeconds();
		float particlesToCreate = pps * delta;
		int count = (int) Math.floor(particlesToCreate);
		float partialParticle = particlesToCreate % 1;
		for (int i = 0; i < count; i++) {
			emitParticle(systemCenter);
		}
		if (Math.random() < partialParticle) {
			emitParticle(systemCenter);
		}
	}

	private void emitParticle(Vector3f center) {
		Vector3f velocity;
		if(direction!=null){
			velocity = generateRandomUnitVectorWithinCone(direction, directionDeviation);
		}else{
			velocity = generateRandomUnitVector();
		}
		velocity.normalize();
		velocity.mul(generateValue(averageSpeed, speedError));
		float scale = generateValue(averageScale, scaleError);
		float lifeLength = generateValue(averageLifeLength, lifeError);
		new Particle(new Vector3f(center), velocity, gravityCompliant, lifeLength, generateRotation(), scale, texture);
	}

	private float generateValue(float average, float errorMargin) {
		float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
		return average + offset;
	}

	private float generateRotation() {
		if (randomRotation) {
			return random.nextFloat() * 360f;
		} else {
			return 0;
		}
	}

	private static Vector3f generateRandomUnitVectorWithinCone(Vector3f coneDirection, float angle) {
		float cosAngle = (float) Math.cos(angle);
		Random random = new Random();
		float theta = (float) (random.nextFloat() * 2f * Math.PI);
		float z = cosAngle + (random.nextFloat() * (1 - cosAngle));
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));

		Vector3f direction = new Vector3f(x, y, z);

		if (coneDirection.x != 0 || coneDirection.y != 0 || (coneDirection.z != 1 && coneDirection.z != -1)) {
			Vector3f rotateAxis = new Vector3f();
			coneDirection.cross(new Vector3f(0, 0, 1), rotateAxis);
			rotateAxis.normalize();
			float rotateAngle = (float) Math.acos(coneDirection.dot(new Vector3f(0, 0, 1)));
			Matrix4f rotationMatrix = new Matrix4f().rotate(-rotateAngle, rotateAxis);
			rotationMatrix.transformDirection(direction);
		} else if (coneDirection.z == -1) {
			direction.z *= -1;
		}

		return new Vector3f(direction.x, direction.y, direction.z);
	}
	
	private Vector3f generateRandomUnitVector() {
		float theta = (float) (random.nextFloat() * 2f * Math.PI);
		float z = (random.nextFloat() * 2) - 1;
		float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
		float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		float y = (float) (rootOneMinusZSquared * Math.sin(theta));
		return new Vector3f(x, y, z);
	}

}
