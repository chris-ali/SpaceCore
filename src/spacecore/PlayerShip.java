package spacecore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * @author jbridon A very simple player ship that they can move around...
 */
public class PlayerShip {
	// Global position and local vectors
	private Vector3f position;
	private Vector3f forward, up, right;

	// Pitch and rolls
	@SuppressWarnings("unused")
	private float pitch, roll;

	// TEST VARIABLE
	Quaternion qResult;

	// Ship variable
	//Model model = null;

	// Player ship has a current velocity and target velocity
	float realVelocity, targetVelocity;

	// Velocities
	public static float VEL_dMAX = 0.005f;
	public static float VEL_MAX = 0.15f;

	// Constructor does nothing
	public PlayerShip() {
		// Default data
		InitShip();

//		try {
//			model = OBJLoader.loadModel(new File("src/Sample.obj"));
//		} catch (FileNotFoundException e) {
//			System.exit(1);
//		} catch (IOException e) {
//			System.exit(1);
//		}
	}

	public void InitShip() {
		// Default position slight above ground
		position = new Vector3f(0, 0.1f, 0);

		// Set forward to Z+
		forward = new Vector3f(0, 0, 1);
		up = new Vector3f(0, 1, 0);
		right = new Vector3f(-1, 0, 0);

		// Blah testing...
		qResult = new Quaternion();

		// Default velocities to zero
		realVelocity = targetVelocity = 0;
		pitch = roll = 0.0f;
	}

	// Check for user events
	public void Update() {
		// Possible angle change
		float dPitch = 0;
		float dRoll = 0;

		// Changing pitch and roll (Pitch is on Z axis)
		if (Keyboard.isKeyDown(Keyboard.KEY_W))
			dPitch -= 0.03;
		if (Keyboard.isKeyDown(Keyboard.KEY_S))
			dPitch += 0.03;

		// Roll is on post-pitch X acis
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
			dRoll += 0.05;
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
			dRoll -= 0.05;

		// Update velocities
		if (Keyboard.isKeyDown(Keyboard.KEY_R))
			targetVelocity += VEL_dMAX;
		if (Keyboard.isKeyDown(Keyboard.KEY_F))
			targetVelocity -= VEL_dMAX;

		// Bounds check the target velocity
		if (targetVelocity > VEL_MAX)
			targetVelocity = VEL_MAX;
		else if (targetVelocity < 0.0f)
			targetVelocity = 0;

		// Update the real velocity over time
		// NOTE: The delta has to be smaller than the target velocity
		if (targetVelocity > realVelocity)
			realVelocity += VEL_dMAX * 0.5f;
		else if (targetVelocity < realVelocity)
			realVelocity -= VEL_dMAX * 0.5f;

		// Save the total pitch and roll
		pitch += dPitch;
		roll += dRoll;

		/*** EULER APPROACH with pure angles (bad) ***/

		// forward = unit(forward * cos(angle) + up * sin(angle));
		// up = right.cross(forward);
		forward.scale((float) Math.cos(dPitch));
		up.scale((float) Math.sin(dPitch));
		forward = Vector3f.add(forward, up, null);
		up = Vector3f.cross(right, forward, null);

		// Normalize
		forward.normalise();
		up.normalise();

		// right = unit(right * cos(angle) + up * sin(angle));
		// up = right.cross(forward);
		right.scale((float) Math.cos(dRoll));
		up.scale((float) Math.sin(dRoll));
		right = Vector3f.add(right, up, null);
		up = Vector3f.cross(right, forward, null);

		// Normalize
		right.normalise();
		up.normalise();

		// Position changes over time based on the forward vector
		// Note we have a tiny bit of lift added
		Vector3f ForwardCopy = new Vector3f(forward);
		ForwardCopy.scale(realVelocity);

		// Gravity factor and normalized velocity
		float gravity = 0.05f;
		float nVelocity = Math.min((realVelocity / VEL_MAX) * 3, 1); // Note: 4
																		// is to
																		// make
																		// 1/4
																		// the
																		// "total
																		// lift"
																		// point

		// Computer the "up" force that attempts to counter gravity
		Vector3f totalUp = new Vector3f(up);
		totalUp.scale(nVelocity * gravity); // Linear relationship: the faster,
											// the more lift
		totalUp.y -= gravity;

		// Add the lift component to the forward vector
		// Vector3f.add(ForwardCopy, TotalUp, ForwardCopy);
		Vector3f.add(position, ForwardCopy, position);

		// Build two quats, for a global roll then pitch
		Quaternion qRoll = new Quaternion();
		qRoll.setFromAxisAngle(new Vector4f(forward.x, forward.y, forward.z, dRoll));
		Quaternion qPitch = new Quaternion();
		qPitch.setFromAxisAngle(new Vector4f(right.x, right.y, right.z, -dPitch));

		// Note: we must explicitly multiply out each dQ, not just the total
		Quaternion.mul(qResult, qRoll, qResult);
		Quaternion.mul(qResult, qPitch, qResult);
		qResult.normalise();
	}

	// Render the ship
	public void Render() {
//		// Translate to position
//		GL11.glPushMatrix();
//		GL11.glTranslatef(position.x, position.y, position.z);
//
//		// Why isn't this a built-in feature of LWJGL
//		float[] qMatrix = new float[16];
//		createMatrix(qMatrix, qResult);
//
//		FloatBuffer Buffer = BufferUtils.createFloatBuffer(16);
//		Buffer.put(qMatrix);
//		Buffer.position(0);
//
//		GL11.glMultMatrix(Buffer);
//
//		GL11.glLineWidth(2.0f);
//		GL11.glBegin(GL11.GL_LINES);
//		GL11.glColor3f(1, 0.5f, 0.5f);
//		GL11.glVertex3f(0, 0, 0);
//		GL11.glVertex3f(1, 0, 0);
//
//		GL11.glColor3f(0.5f, 1, 0.5f);
//		GL11.glVertex3f(0, 0, 0);
//		GL11.glVertex3f(0, 1, 0);
//
//		GL11.glColor3f(0.5f, 0.5f, 1);
//		GL11.glVertex3f(0, 0, 0);
//		GL11.glVertex3f(0, 0, 1);
//		GL11.glEnd();
//
//		// Set width to a single line
//		GL11.glLineWidth(1);
//
//		// Change rendermode
//		for (int i = 0; i < 2; i++) {
//			if (i == 0)
//				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
//			else
//				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
//
//			// Randomize surface color a bit
//			Random SurfaceRand = new Random(123456);
//
//			GL11.glBegin(GL11.GL_TRIANGLES);
//			for (Face face : model.faces) {
//				// Always make black when in line mode)
//				if (i == 0)
//					GL11.glColor3f(0.8f, 0.8f, 0.5f + 0.5f * (SurfaceRand.nextFloat()));
//				else
//					GL11.glColor3f(0.4f, 0.4f, 0.2f + 0.2f * (SurfaceRand.nextFloat()));
//
//				// Randomize the color a tiny bit
//				Vector3f v1 = model.vertices.get((int) face.vertex.x - 1);
//				GL11.glVertex3f(v1.x, v1.y, v1.z);
//				Vector3f v2 = model.vertices.get((int) face.vertex.y - 1);
//				GL11.glVertex3f(v2.x, v2.y, v2.z);
//				Vector3f v3 = model.vertices.get((int) face.vertex.z - 1);
//				GL11.glVertex3f(v3.x, v3.y, v3.z);
//			}
//			GL11.glEnd();
//		}

		// Reset back to regular faces
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		// Done
		GL11.glPopMatrix();

		// Render the shadow (view-volume)
		// Note: we render the shadow independant of the model's translation and
		// rotation
		// THOUGH NOTE: we do translate the shadow up a tiny bit off the ground
		// so it doesnt z-fight
//		GL11.glPushMatrix();
//
//		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
//		GL11.glPolygonOffset(-1.0f, -1.0f);
//
//		GL11.glTranslatef(0, 0.001f, 0);
//		renderShadow(position);
//
//		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
//
//		GL11.glPopMatrix();
	}

//	public void renderShadow(Vector3f Translation) {
//		// Explicitly copy...
//		List<Vector3f> vertices = new ArrayList<Vector3f>();
//		for (Vector3f Vertex : model.vertices) {
//			// Apply rotation then translation
//			Vector3f vt = new Vector3f(Vertex);
//			vt = ApplyQuatToPoint(qResult, vt);
//			Vector3f.add(vt, Translation, vt);
//			vertices.add(vt);
//		}
//
//		// NOTE: WE DO THIS COLLISION TEST HERE SINCE WE HAVE THE
//		// TRANSLATION MODEL (i.e. global data)
//
//		// Make sure the model never goes below the surface, and if
//		// it does, push it back up, but if it does too much, crash ship
//		float maxD = 0.0f;
//		for (Vector3f Vertex : vertices) {
//			if (Vertex.y < 0.0f && Vertex.y < maxD)
//				maxD = Vertex.y;
//		}
//
//		// Assume the light source is just high above
//		Vector3f LightPos = new Vector3f(0, 1000, 0);
//
//		// For each triangle, project onto the plane XZ-plane
//		GL11.glBegin(GL11.GL_TRIANGLES);
//		for (Face face : model.faces) {
//			// Per-face color
//			GL11.glColor3f(0.4f, 0.4f, 0.4f);
//
//			// Draw the mode components
//			Vector3f v1 = getPlaneIntersect(vertices.get((int) face.vertex.x - 1), LightPos);
//			GL11.glVertex3f(v1.x, v1.y, v1.z);
//			Vector3f v2 = getPlaneIntersect(vertices.get((int) face.vertex.y - 1), LightPos);
//			GL11.glVertex3f(v2.x, v2.y, v2.z);
//			Vector3f v3 = getPlaneIntersect(vertices.get((int) face.vertex.z - 1), LightPos);
//			GL11.glVertex3f(v3.x, v3.y, v3.z);
//		}
//		GL11.glEnd();
//	}

	public Vector3f ApplyQuatToPoint(Quaternion Q, Vector3f vt) {
		// Just multiply the point against the matrix
		float[] qMatrix = new float[16];
		createMatrix(qMatrix, qResult);

		Vector3f vert = new Vector3f();
		vert.x = qMatrix[0] * vt.x + qMatrix[4] * vt.y + qMatrix[8] * vt.z;
		vert.y = qMatrix[1] * vt.x + qMatrix[5] * vt.y + qMatrix[9] * vt.z;
		vert.z = qMatrix[2] * vt.x + qMatrix[6] * vt.y + qMatrix[10] * vt.z;
		return vert;
	}

	// Returns the intersection point of the vector (described as two points)
	// onto the y=0 plane (or simply the XZ plane)
	public Vector3f getPlaneIntersect(Vector3f vf, Vector3f vi) {
		Vector3f lineDir = Vector3f.sub(vf, vi, null);
		lineDir.normalise();

		Vector3f planeNormal = new Vector3f(0, 1, 0);
		Vector3f neg_Vi = new Vector3f(-vi.x, -vi.y, -vi.z);

		float d = Vector3f.dot(neg_Vi, planeNormal) / Vector3f.dot(lineDir, planeNormal);
		Vector3f pt = new Vector3f(lineDir);
		pt.scale(d);
		Vector3f.add(pt, vi, pt);

		return pt;
	}

	public void createMatrix(float[] pMatrix, Quaternion q) {
		// Fill in the rows of the 4x4 matrix, according to the quaternion to
		// matrix equations

		// First row
		pMatrix[0] = 1.0f - 2.0f * (q.y * q.y + q.z * q.z);
		pMatrix[1] = 2.0f * (q.x * q.y - q.w * q.z);
		pMatrix[2] = 2.0f * (q.x * q.z + q.w * q.y);
		pMatrix[3] = 0.0f;

		// Second row

		pMatrix[4] = 2.0f * (q.x * q.y + q.w * q.z);
		pMatrix[5] = 1.0f - 2.0f * (q.x * q.x + q.z * q.z);
		pMatrix[6] = 2.0f * (q.y * q.z - q.w * q.x);
		pMatrix[7] = 0.0f;

		// Third row

		pMatrix[8] = 2.0f * (q.x * q.z - q.w * q.y);
		pMatrix[9] = 2.0f * (q.y * q.z + q.w * q.x);
		pMatrix[10] = 1.0f - 2.0f * (q.x * q.x + q.y * q.y);
		pMatrix[11] = 0.0f;

		// Fourth row

		pMatrix[12] = 0;
		pMatrix[13] = 0;
		pMatrix[14] = 0;
		pMatrix[15] = 1.0f;
		// Now pMatrix[] is a 4x4 homogeneous matrix that can be applied to an
		// OpenGL Matrix
	}

	// Get the look vectors for the camera
	public void GetCameraVectors(Vector3f cameraPos, Vector3f cameraTarget, Vector3f cameraUp) {
		// Copy all vectors as needed for the camera
		cameraPos.set(position.x, position.y, position.z);
		cameraTarget.set(forward.x + position.x, forward.y + position.y, forward.z + position.z);
		cameraUp.set(up.x, up.y, up.z);
	}

	// Get yaw of ship
	public float GetYaw() {
		// Cast down the forward and right vectors onto the XZ plane
		Vector3f fFlat = new Vector3f(forward.x, 0f, forward.z);
		Vector3f rFlat = new Vector3f(1f, 0f, 0f);

		// Angle between
		float ang = Vector3f.angle(rFlat, fFlat);
		if (Vector3f.cross(rFlat, fFlat, null).y < 0)
			ang = (float) (Math.PI * 2.0) - ang;
		return ang;
	}

	// Get velocity
	public float GetRealVelocity() {
		return realVelocity;
	}

	public float GetTargetVelocity() {
		return targetVelocity;
	}
}
