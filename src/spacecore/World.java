package spacecore;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

// Star point
class StarPoint {
	Vector3f pt = new Vector3f();
	Vector3f color = new Vector3f();
	float scale;
}

// Models to render
class Models {
	Vector3f pt = new Vector3f();
	Model model;
	float yaw;
}

/**
 * @author jbridon A very simple world that is always rendered at Y = 0
 */
public class World {
	// Box size
	public static float skyboxSize = 64.0f;
	public static float worldSize = 1024.0f;

	// List of stars (list of Vector3f)
	ArrayList<StarPoint> starList;

	// Load a bunch of objects
	ArrayList<Models> modelList;

	public World() {
		// Create a list and fill with a bunch of stars
		starList = new ArrayList<StarPoint>();
		for (int i = 0; i < 1000; i++) {
			// New star
			StarPoint Star = new StarPoint();

			// New position
			double u = 2f * Math.random() - 1f;
			double v = Math.random() * 2 * Math.PI;

			Star.pt.x = (float) (Math.sqrt(1f - Math.pow(u, 2.0)) * Math.cos(v));
			Star.pt.z = (float) (Math.sqrt(1f - Math.pow(u, 2.0)) * Math.sin(v));
			Star.pt.y = (float) Math.abs(u);
			Star.pt.scale(skyboxSize / 2); // Scale out from the center

			// Scale up
			Star.scale = 3f * (float) Math.random();

			// Color
			float Gray = 0.5f + 0.5f * (float) Math.random();
			Star.color.x = Gray;
			Star.color.y = Gray;
			Star.color.z = Gray;

			// Push star into list
			starList.add(Star);
		}

		// Load a bunch of models
		modelList = new ArrayList<Models>();

		// Load road strip
		Models model = new Models();
		model.model = OBJLoader.load("src/Road.obj");
		model.pt.y = 0.1f;
		model.yaw = 0f;
		modelList.add(model);

		// Load a bunch of rocks..
		for (int i = 0; i < 1000; i++) {
			int Index = (int) (Math.random() * 5f) + 1;

			Models newModel = new Models();
			newModel.model = OBJLoader.load("src/Rock" + Index + ".obj");
			newModel.yaw = (float) (Math.random() * 2.0 * Math.PI);

			newModel.pt.x = (float) (Math.random() * 2.0 - 1.0) * worldSize;
			newModel.pt.z = (float) (Math.random() * 2.0 - 1.0) * worldSize;
			newModel.pt.y = 0f;

			modelList.add(newModel);
		}
	}

	// Render the ship
	public void Render(Vector3f pos, float yaw) {
		// Rotate (yaw) as needed so the player always faces non-corners
		GL11.glPushMatrix();

		// Rotate and translate
		GL11.glTranslatef(pos.x, pos.y, pos.z);
		GL11.glRotatef(yaw, 0f, 1f, 0f);

		// Render the skybox and stars
		RenderSkybox();

		// Be done
		GL11.glPopMatrix();

		// Render out the stars
		GL11.glPushMatrix();

		// Show stars
		GL11.glTranslatef(pos.x, pos.y * 0.99f, pos.z);
		RenderStars();

		// Be done
		GL11.glPopMatrix();

		// Draw stars
		GL11.glPushMatrix();

		// Render ground and right below
		GL11.glTranslatef(pos.x, 0, pos.z);

		Vector3f Color = new Vector3f(115.0f / 255.0f, 200.0f / 255.0f, 125.0f / 255.0f);
		RenderGround(worldSize, Color);

		GL11.glPopMatrix();

		// Render all the objects
		for (Models model : modelList) {
			GL11.glPushMatrix();

			// Render ground and right below
			GL11.glTranslatef(model.pt.x, model.pt.y, model.pt.z);
			GL11.glRotatef((float) Math.toDegrees(model.yaw), 0, 1, 0);
			RenderModel(model.model);

			GL11.glPopMatrix();
		}
	}

	// Draw the bottom level
	public void RenderGround(float worldLength, Vector3f color) {
		// Translate to position
		GL11.glPushMatrix();

		// Set the ship color to red for now
		GL11.glColor3f(color.x, color.y, color.z);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex3f(-worldLength, 0, -worldLength);
		GL11.glVertex3f(worldLength, 0, -worldLength);
		GL11.glVertex3f(worldLength, 0, worldLength);
		GL11.glVertex3f(-worldLength, 0, worldLength);
		GL11.glEnd();

		// Done
		GL11.glPopMatrix();
	}

	// Render the
	public void RenderSkybox() {
		// Define the top and bottom color
		Vector3f topColor = new Vector3f(85f / 255f, 225f / 255f, 255f / 255f);
		Vector3f bottomColor = new Vector3f(155f / 255f, 245f / 255f, 255f / 255f);

		// Save matrix
		glPushMatrix();

		// Draw out top side
		glBegin(GL_QUADS);

		// Polygon & texture map
		// Top has one constant color
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(-skyboxSize, skyboxSize, -skyboxSize);
		glVertex3f(skyboxSize, skyboxSize, -skyboxSize);
		glVertex3f(skyboxSize, skyboxSize, skyboxSize);
		glVertex3f(-skyboxSize, skyboxSize, skyboxSize);

		glEnd();

		// Draw out the left side
		glBegin(GL_QUADS);

		// Polygon & texture map
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(skyboxSize, skyboxSize, -skyboxSize);
		glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
		glVertex3f(skyboxSize, -skyboxSize, -skyboxSize);
		glVertex3f(skyboxSize, -skyboxSize, skyboxSize);
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(skyboxSize, skyboxSize, skyboxSize);

		glEnd();

		// Draw out the right side
		glBegin(GL_QUADS);

		// Polygon & texture map
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(-skyboxSize, skyboxSize, skyboxSize);
		glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
		glVertex3f(-skyboxSize, -skyboxSize, skyboxSize);
		glVertex3f(-skyboxSize, -skyboxSize, -skyboxSize);
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(-skyboxSize, skyboxSize, -skyboxSize);

		glEnd();

		// Draw out the front side
		glBegin(GL_QUADS);

		// Polygon & texture map
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(skyboxSize, skyboxSize, skyboxSize);
		glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
		glVertex3f(skyboxSize, -skyboxSize, skyboxSize);
		glVertex3f(-skyboxSize, -skyboxSize, skyboxSize);
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(-skyboxSize, skyboxSize, skyboxSize);

		glEnd();

		// Draw out the back side
		glBegin(GL_QUADS);

		// Polygon & texture map
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(-skyboxSize, skyboxSize, -skyboxSize);
		glColor3f(bottomColor.x, bottomColor.y, bottomColor.z);
		glVertex3f(-skyboxSize, -skyboxSize, -skyboxSize);
		glVertex3f(skyboxSize, -skyboxSize, -skyboxSize);
		glColor3f(topColor.x, topColor.y, topColor.z);
		glVertex3f(skyboxSize, skyboxSize, -skyboxSize);

		glEnd();

		// Place back matrix
		glPopMatrix();
	}

	// Render the stars
	public void RenderStars() {
		// Render all stars
		for (StarPoint Star : starList) {
			glPointSize(Star.scale);
			glColor3f(Star.color.x, Star.color.y, Star.color.z);
			glBegin(GL_POINTS);
			glVertex3f(Star.pt.x, Star.pt.y, Star.pt.z);
			glEnd();
		}
	}

	// Render a model or shape
	public void RenderModel(Model model) {
		// Set width to a single line
		GL11.glLineWidth(0);

		// Change rendermode
		for (int i = 0; i < 2; i++) {
			if (i == 0)
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			else
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

			// Randomize surface color a bit
			Random SurfaceRand = new Random(123456);

			GL11.glBegin(GL11.GL_TRIANGLES);
			for (Face face : model.faces) {
				// Always make black when in line mode)
				if (i == 0)
					GL11.glColor3f(0.8f, 0.8f, 0.5f + 0.5f * (SurfaceRand.nextFloat()));
				else
					GL11.glColor3f(0.4f, 0.4f, 0.2f + 0.2f * (SurfaceRand.nextFloat()));

				// Randomize the color a tiny bit
				Vector3f v1 = model.vertices.get((int) face.vertex.x - 1);
				GL11.glVertex3f(v1.x, v1.y, v1.z);
				Vector3f v2 = model.vertices.get((int) face.vertex.y - 1);
				GL11.glVertex3f(v2.x, v2.y, v2.z);
				Vector3f v3 = model.vertices.get((int) face.vertex.z - 1);
				GL11.glVertex3f(v3.x, v3.y, v3.z);
			}
			GL11.glEnd();
		}
	}
}
