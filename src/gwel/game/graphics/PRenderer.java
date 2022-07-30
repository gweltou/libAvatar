package gwel.game.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import processing.core.PApplet;
import processing.core.PConstants;


/**
 * This is a template class and can be used to start a new processing Library.
 * Make sure you rename this class as well as the name of the example package 'template' 
 * to your own Library naming convention.
 * 
 * (the tag example followed by the name of an example included in folder 'examples' will
 * automatically include the example in the javadoc.)
 *
 * @example Hello 
 */

public class PRenderer extends Renderer {
	public final static String VERSION = "##library.prettyVersion##";
	// parent is a reference to the parent sketch
	private final PApplet parent;

	private ComplexShape selected;
	static public final Color selectedColor = new Color(0.0f, 1.0f, 0.0f, 0.6f);
	private boolean wireframe = false;


	public PRenderer(PApplet parent) {
		super();
		this.parent = parent;
		System.out.println("Game renderer initiated..." + " Version " + VERSION);
	}


	/**
	 * Delete if outside SgAnimator
	 */
	public void setSelected(ComplexShape selected) {
		this.selected = selected;
	}

	/**
	 * Delete if outside SgAnimator
	 */
	public ComplexShape getSelected() {
		return selected;
	}


	public void triangles(float[] vertices) {
		Vector2 point = new Vector2();

		parent.noStroke();
		parent.fill(Color.argb8888(color));
		parent.beginShape();

		for (int i = 0; i < vertices.length; i += 2) {
			point.set(vertices[i], vertices[i+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);
		}

		parent.endShape(PConstants.CLOSE);
	}


	public void triangles(float[] vertices, short[] indices) {
		if (wireframe) {
			parent.stroke(Color.argb8888(color));
			parent.strokeWeight(1);
			parent.noFill();
		} else {
			parent.noStroke();
			parent.fill(Color.argb8888(color));
			//triangles(vertices);
			//return;
		}

		parent.beginShape(PConstants.TRIANGLES);
		int idx;
		Vector2 point = new Vector2();
		for (int i = 0; i < indices.length; ) {
			idx = indices[i++]<<1;
			point.set(vertices[idx], vertices[idx+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);

			idx = indices[i++]<<1;
			point.set(vertices[idx], vertices[idx+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);

			idx = indices[i++]<<1;
			point.set(vertices[idx], vertices[idx+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);
		}
		parent.endShape();
	}


	public void coloredTriangles(float[] vertices, short[] indices) {
		parent.noStroke();
		//parent.fill(Color.argb8888(color));

		parent.beginShape(PConstants.TRIANGLES);
		int idx;
		Vector2 point = new Vector2();
		for (int i = 0; i < indices.length; ) {
			idx = indices[i++] * 6;
			parent.fill(vertices[idx+2]*255, vertices[idx+3]*255, vertices[idx+4]*255, vertices[idx+5]*255);

			point.set(vertices[idx], vertices[idx+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);

			idx = indices[i++] * 6;
			point.set(vertices[idx], vertices[idx+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);

			idx = indices[i++] * 6;
			point.set(vertices[idx], vertices[idx+1]);
			getTransform().applyTo(point);
			parent.vertex(point.x, point.y);
		}
		parent.endShape();
	}


	public void circle(float x, float y, float r) {
		Vector2 point = new Vector2(x, y);
		Vector2 radiusPoint = new Vector2(x+r, y);

		if (wireframe) {
			parent.stroke(Color.argb8888(color));
			parent.noFill();
		} else {
			parent.noStroke();
			parent.fill(Color.argb8888(color));
		}

		getTransform().applyTo(point);
		getTransform().applyTo(radiusPoint);
		r = point.dst(radiusPoint);
		parent.circle(point.x, point.y, 2*r);
	}


	/**
	 * Delete if outside SgAnimator
	 */
	public void drawAxes() {
		for (int i=1; i<=10; i++) {
			Vector2 point = new Vector2(0f, -10*i);
			getTransform().applyTo(point);
			parent.stroke(0, 127);
			if (i%5 == 0) {
				parent.strokeWeight(2);
				parent.line(point.x-8, point.y, point.x+8, point.y);
			} else {
				parent.strokeWeight(1);
				parent.line(point.x-4, point.y, point.x+4, point.y);
			}
		}
	}


	/**
	 * Delete if outside SgAnimator
	 */
	public void toggleWireframe() { wireframe = !wireframe; }


	/**
	 * return the version of the Library.
	 *
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}
}

