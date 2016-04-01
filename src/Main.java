import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import spacecore.PlayerShip;
import spacecore.UserInterface;
import spacecore.World;

// Simple main application entry point
public class Main {
	
	// Default settings
	public static final int DISPLAY_HEIGHT = 900;
	public static final int DISPLAY_WIDTH = 1400;

	// Renderable items
	PlayerShip testShip;
	World testWorld;
	UserInterface ui;

	// Debug var
	float time;

	// Ship / camera variables
	Vector3f cameraPos = new Vector3f();
	Vector3f cameraTarget = new Vector3f();
	Vector3f cameraUp = new Vector3f();

	// Camera state
	boolean cameraType = false;

	public static void main(String[] args) {
		Main main = null;
		try {
			main = new Main();
			main.create();
			main.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (main != null)
				main.destroy();
		}
	}

	public Main() {}

	public void create() throws LWJGLException {

		// Display
		Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
		Display.setVSyncEnabled(true);
		Display.setFullscreen(false);
		Display.setTitle("SpaceCore 0.1 - CoreS2 Software Solutions");
		Display.create();

		// Keyboard
		Keyboard.create();

		// Mouse
		Mouse.setGrabbed(false);
		Mouse.create();

		// OpenGL
		initGL();
		resizeGL();

		// Create our world and ships
		testWorld = new World();
		testShip = new PlayerShip();
		ui = new UserInterface();

		// Setup fog
		glFogi(GL_FOG_MODE, GL_EXP);
		glFogf(GL_FOG_DENSITY, 0.0025f);
		glHint(GL_FOG_HINT, GL_DONT_CARE);
		glFogf(GL_FOG_START, World.skyboxSize);
		glFogf(GL_FOG_END, World.skyboxSize * 4);
		glEnable(GL_FOG);
	}

	public void destroy() {
		// Methods already check if created before destroying.
		Mouse.destroy();
		Keyboard.destroy();
		Display.destroy();
	}

	public void initGL() {
		// 2D Initialization
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black
		glDisable(GL_DEPTH_TEST);
	}

	// 2D mode
	public void resizeGL2D() {
		// 2D Scene
		glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluOrtho2D(0.0f, (float) DISPLAY_WIDTH, (float) DISPLAY_HEIGHT, 0.0f);
		glMatrixMode(GL_MODELVIEW);

		// Set depth buffer elements
		glDisable(GL_DEPTH_TEST);
	}

	// 3D mode
	public void resizeGL() {
		// 3D Scene
		glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(45.0f, ((float) DISPLAY_WIDTH / (float) DISPLAY_HEIGHT), 0.1f, 100.0f);
		glMatrixMode(GL_MODELVIEW);

		// Set depth buffer elements
		glClearDepth(1.0f);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
	}

	public void run() {
		// Keep looping until we hit a quit event
		while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			if (Display.isVisible()) {
				update();
				render();
			} else {
				if (Display.isDirty())
					render();
				try {Thread.sleep(100);} 
				catch (InterruptedException ex) {}
			}
			Display.update();
			Display.sync(60);
		}
	}

	public void render() {
		// Clear screen and load up the 3D matrix state
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();

		// 3D render
		resizeGL();

		// Move camera to right behind the ship
		// public static void gluLookAt(float eyex, float eyey, float eyez,
		// float centerx, float centery, float centerz, float upx, float upy,
		// float upz)
		time += 0.001f;
		float cDist = 6;

		// Set the camera on the back of the
		testShip.GetCameraVectors(cameraPos, cameraTarget, cameraUp);

		// Tail-plane camera
		if (cameraType) {
			// Extend out the camera by length
			Vector3f dir = new Vector3f();
			Vector3f.sub(cameraPos, cameraTarget, dir);
			dir.normalise();
			dir.scale(4);
			dir.y += 0.1f;
			Vector3f.add(cameraPos, dir, cameraPos);
			cameraPos.y += 1;

			// Little error correction: always make the camera above ground
			if (cameraPos.y < 0.01f)
				cameraPos.y = 0.01f;

			GLU.gluLookAt(cameraPos.x, cameraPos.y, cameraPos.z, cameraTarget.x, cameraTarget.y, cameraTarget.z,
					cameraUp.x, cameraUp.y, cameraUp.z);
		}
		// Overview
		else {
			GLU.gluLookAt(cDist * (float) Math.cos(time), cDist, cDist * (float) Math.sin(time), cameraPos.x,
					cameraPos.y, cameraPos.z, 0, 1, 0);
		}

		// Always face forward
		float yaw = (float) Math.toDegrees(testShip.GetYaw());

		// Render all elements
		testWorld.Render(cameraPos, yaw);
		testShip.Render();

		// 2D GUI
		resizeGL2D();
		ui.Render(testShip.GetRealVelocity(), testShip.GetTargetVelocity(), PlayerShip.VEL_MAX);
	}

	public void update() {
		// Did the camera change?
		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
			cameraType = !cameraType;

		testShip.Update();
	}
}
