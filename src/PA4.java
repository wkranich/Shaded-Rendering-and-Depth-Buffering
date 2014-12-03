import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.*;
//import java.io.File;
//import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

//import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl

public class PA4 extends JFrame implements GLEventListener, KeyListener,
		MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WINDOW_WIDTH = 1024;
	private final int DEFAULT_WINDOW_HEIGHT = 512;
	private final float DEFAULT_LINE_WIDTH = 1.0f;

	private GLCapabilities capabilities;
	private GLCanvas canvas;
	private FPSAnimator animator;

	final private int numTestCase;
	private int testCase;
	private BufferedImage buff;
	@SuppressWarnings("unused")
	private ColorType color;
	private Random rng;

	// specular exponent for materials
	private int ns = 5;

	private ArrayList<Point2D> lineSegs;
	private ArrayList<Point2D> triangles;
	private boolean doSmoothShading;
	private boolean phong = false;
	private int Nsteps;

	/** The quaternion which controls the rotation of the world. */
	private Quaternion viewing_quaternion = new Quaternion();
	private Vector3D viewing_center = new Vector3D(
			(float) (DEFAULT_WINDOW_WIDTH / 2),
			(float) (DEFAULT_WINDOW_HEIGHT / 2), (float) 0.0);
	/** The last x and y coordinates of the mouse press. */
	private int last_x = 0, last_y = 0;
	/** Whether the world is being rotated. */
	private boolean rotate_world = false;

	public PA4() {
		capabilities = new GLCapabilities(null);
		capabilities.setDoubleBuffered(true);

		canvas = new GLCanvas(capabilities);
		canvas.addGLEventListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		canvas.setAutoSwapBufferMode(true);
		canvas.setFocusable(true);
		getContentPane().add(canvas);

		animator = new FPSAnimator(canvas, 60);

		numTestCase = 3;
		testCase = 0;
		Nsteps = 12;

		setTitle("CS480/680 PA4");
		setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setResizable(false);

		rng = new Random();
		color = new ColorType(1.0f, 0.0f, 0.0f);
		lineSegs = new ArrayList<Point2D>();
		triangles = new ArrayList<Point2D>();
		doSmoothShading = false;
	}

	public void run() {
		animator.start();
	}

	public static void main(String[] args) {
		PA4 P = new PA4();
		P.run();
	}

	// ***********************************************
	// GLEventListener Interfaces
	// ***********************************************
	public void init(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glLineWidth(DEFAULT_LINE_WIDTH);
		Dimension sz = this.getContentPane().getSize();
		buff = new BufferedImage(sz.width, sz.height,
				BufferedImage.TYPE_3BYTE_BGR);
		clearPixelBuffer();
	}

	// Redisplaying graphics
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		WritableRaster wr = buff.getRaster();
		DataBufferByte dbb = (DataBufferByte) wr.getDataBuffer();
		byte[] data = dbb.getData();

		gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
		gl.glDrawPixels(buff.getWidth(), buff.getHeight(), GL2.GL_BGR,
				GL2.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data));
		drawTestCase();
	}

	// Window size change
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		// deliberately left blank
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
		// deliberately left blank
	}

	void clearPixelBuffer() {
		lineSegs.clear();
		triangles.clear();
		Graphics2D g = buff.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
		g.dispose();
	}

	// drawTest
	void drawTestCase() {
		/* clear the window and vertex state */
		clearPixelBuffer();

		// System.out.printf("Test case = %d\n",testCase);

		switch (testCase) {
		case 0:
			phong = false;
			shadeTest(true); /* smooth shaded, sphere and torus */
			break;
		case 1:
			phong = false;
			shadeTest(false); /* flat shared, sphere and torus */
			break;
		case 2:
			phong = true;
			shadeTest(false);
			break;
		}
	}

	// ***********************************************
	// KeyListener Interfaces
	// ***********************************************
	public void keyTyped(KeyEvent key) {
		// Q,q: quit
		// C,c: clear polygon (set vertex count=0)
		// R,r: randomly change the color
		// S,s: toggle the smooth shading
		// T,t: show testing examples
		// >: increase the step number for examples
		// <: decrease the step number for examples
		// +,-: increase or decrease spectral exponent

		switch (key.getKeyChar()) {
		case 'Q':
		case 'q':
			new Thread() {
				public void run() {
					animator.stop();
				}
			}.start();
			System.exit(0);
			break;
		case 'R':
		case 'r':
			color = new ColorType(rng.nextFloat(), rng.nextFloat(),
					rng.nextFloat());
			break;
		case 'C':
		case 'c':
			clearPixelBuffer();
			break;
		case 'S':
		case 's':
			doSmoothShading = !doSmoothShading;
			break;
		case 'T':
		case 't':
			testCase = (testCase + 1) % numTestCase;
			drawTestCase();
			break;
		case '<':
			Nsteps = Nsteps < 4 ? Nsteps : Nsteps / 2;
			System.out.printf("Nsteps = %d \n", Nsteps);
			drawTestCase();
			break;
		case '>':
			Nsteps = Nsteps > 190 ? Nsteps : Nsteps * 2;
			System.out.printf("Nsteps = %d \n", Nsteps);
			drawTestCase();
			break;
		case '+':
			ns++;
			drawTestCase();
			break;
		case '-':
			if (ns > 0)
				ns--;
			drawTestCase();
			break;
		default:
			break;
		}
	}

	public void keyPressed(KeyEvent key) {
		switch (key.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			new Thread() {
				public void run() {
					animator.stop();
				}
			}.start();
			System.exit(0);
			break;
		default:
			break;
		}
	}

	public void keyReleased(KeyEvent key) {
		// deliberately left blank
	}

	// **************************************************
	// MouseListener and MouseMotionListener Interfaces
	// **************************************************
	public void mouseClicked(MouseEvent mouse) {
		// deliberately left blank
	}

	public void mousePressed(MouseEvent mouse) {
		int button = mouse.getButton();
		if (button == MouseEvent.BUTTON1) {
			last_x = mouse.getX();
			last_y = mouse.getY();
			rotate_world = true;
		}
	}

	public void mouseReleased(MouseEvent mouse) {
		int button = mouse.getButton();
		if (button == MouseEvent.BUTTON1) {
			rotate_world = false;
		}
	}

	public void mouseMoved(MouseEvent mouse) {
		// Deliberately left blank
	}

	/**
	 * Updates the rotation quaternion as the mouse is dragged.
	 * 
	 * @param mouse
	 *            The mouse drag event object.
	 */
	public void mouseDragged(final MouseEvent mouse) {
		if (this.rotate_world) {
			// get the current position of the mouse
			final int x = mouse.getX();
			final int y = mouse.getY();

			// get the change in position from the previous one
			final int dx = x - this.last_x;
			final int dy = y - this.last_y;

			// create a unit vector in the direction of the vector (dy, dx, 0)
			final float magnitude = (float) Math.sqrt(dx * dx + dy * dy);
			if (magnitude > 0.0001) {
				// define axis perpendicular to (dx,-dy,0)
				// use -y because origin is in upper lefthand corner of the
				// window
				final float[] axis = new float[] { -(float) (dy / magnitude),
						(float) (dx / magnitude), 0 };

				// calculate appropriate quaternion
				final float viewing_delta = 3.1415927f / 180.0f;
				final float s = (float) Math.sin(0.5f * viewing_delta);
				final float c = (float) Math.cos(0.5f * viewing_delta);
				final Quaternion Q = new Quaternion(c, s * axis[0],
						s * axis[1], s * axis[2]);
				this.viewing_quaternion = Q.multiply(this.viewing_quaternion);

				// normalize to counteract acccumulating round-off error
				this.viewing_quaternion.normalize();

				// save x, y as last x, y
				this.last_x = x;
				this.last_y = y;
				drawTestCase();
			}
		}

	}

	public void mouseEntered(MouseEvent mouse) {
		// Deliberately left blank
	}

	public void mouseExited(MouseEvent mouse) {
		// Deliberately left blank
	}

	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	// **************************************************
	// Test Cases
	// Nov 9, 2014 Stan Sclaroff -- removed line and triangle test cases
	// **************************************************

	void shadeTest(boolean doSmooth) {
		// the simple example scene includes one sphere and one torus
		float radius = (float) 50.0;
		Sphere sphere = new Sphere((float) 128.0, (float) 128.0, (float) 128.0,
				(float) 1.5 * radius, Nsteps, Nsteps);
		Ellipsoid ellipsoid = new Ellipsoid((float)128.0*3, (float)128.0, (float)128.0, (float)1.5*radius, (float)2*radius, (float)radius, Nsteps, Nsteps);
		Cylinder cylinder = new Cylinder(256.0f*2f, 384.0f, 128.0f, (float)1.5*radius, (float)1.5*radius, Nsteps, Nsteps, radius);
		Torus torus = new Torus((float) 256.0, (float) 384.0, (float) 128.0,
				(float) 0.8 * radius, (float) 1.25 * radius, Nsteps, Nsteps);

		// view vector is defined along z axis
		// this example assumes simple othorgraphic projection
		// view vector is used in
		// (a) calculating specular lighting contribution
		// (b) backface culling / backface rejection
		Vector3D view_vector = new Vector3D((float) 0.0, (float) 0.0,
				(float) 1.0);

		// material properties for the sphere and torus
		// ambient, diffuse, and specular coefficients
		// specular exponent is a global variable
		ColorType torus_ka = new ColorType(0.0, 0.1, 0.2);
		ColorType sphere_ka = new ColorType(0.1, 0.0, 0.1);
		ColorType torus_kd = new ColorType(0.0, 0.5, 0.9);
		ColorType sphere_kd = new ColorType(1.0, 0.0, 1.0);
		ColorType torus_ks = new ColorType(1.0, 1.0, 1.0);
		ColorType sphere_ks = new ColorType(1.0, 1.0, 1.0);
		Material[] mats = { new Material(sphere_ka, sphere_kd, sphere_ks, ns),
				new Material(torus_ka, torus_kd, torus_ks, ns), new Material(sphere_ka, sphere_kd, sphere_ks, ns),
				new Material(sphere_ka, sphere_kd, sphere_ks, ns)};

		// define one infinite light source, with color = white
		ColorType light_color = new ColorType(1.0, 1.0, 1.0);
		Vector3D light_direction = new Vector3D((float) 0.0,
				(float) (-1.0 / Math.sqrt(2.0)), (float) (1.0 / Math.sqrt(2.0)));
		Vector3D light_position = new Vector3D(0.0f, 0f, 200f);
		InfiniteLight infLight = new InfiniteLight(light_color, light_direction);
		AmbientLight ambLight = new AmbientLight(light_color, light_direction);
		Light light = new Light();
		light.addLight(infLight);
		light.addLight(ambLight);
		

		// normal to the plane of a triangle
		// to be used in backface culling / backface rejection
		Vector3D triangle_normal = new Vector3D();

		// a triangle mesh
		Mesh3D mesh,top = null,bot = null;

		int i, j, n, m;
		
		DepthBuffer depthBuff = new DepthBuffer(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT, buff);

		// temporary variables for triangle 3D vertices and 3D normals
		Vector3D v0, v1, v2, n0, n1, n2;

		// projected triangle, with vertex colors
		Point3D[] tri = { new Point3D(), new Point3D(), new Point3D() };
		
		Vector3D point = new Vector3D();

		for (int k = 0; k < 4; ++k) // loop twice: shade sphere, then torus
		{
			if (k == 0) {
				mesh = sphere.mesh;
				n = sphere.getN();
				m = sphere.getM();
			} else if (k == 1) {
				mesh = torus.mesh;
				n = torus.getN();
				m = torus.getM();
			} else if (k == 2) {
				mesh = ellipsoid.mesh;
				n = ellipsoid.getN();
				m = ellipsoid.getM();
			} else {
				mesh = cylinder.mesh;
				top = cylinder.top;
				bot = cylinder.bot;
				n = cylinder.getN();
				m = cylinder.getM();
			}

			// rotate the surface's 3D mesh using quaternion
			mesh.rotateMesh(viewing_quaternion, viewing_center);

			// draw triangles for the current surface, using vertex colors
			// this works for Gouraud and flat shading only (not Phong)
			
//			if (k == 3) { // if cylinder
//				//draw endcap triangle fans
//				TriangleFan topfan = new TriangleFan(cylinder.getCenter().x, cylinder.getCenter().y, cylinder.getCenter().z, top);
//				n0 = top
//				n1 = 
//			}
			for (i = 0; i < m - 1; ++i) {
				for (j = 0; j < n - 1; ++j) {
					v0 = mesh.v[i][j];
					v1 = mesh.v[i][j + 1];
					v2 = mesh.v[i + 1][j + 1];
					triangle_normal = computeTriangleNormal(v0, v1, v2);

					if (view_vector.dotProduct(triangle_normal) > 0.0) // front-facing
																		// triangle?
					{
						if (phong) {
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j + 1];
							n2 = mesh.n[i + 1][j + 1];
//							tri[0].c = light.applyLight(mats[k], view_vector,
//									n0, v0);
//							tri[1].c = light.applyLight(mats[k], view_vector,
//									n1, v1);
//							tri[2].c = light.applyLight(mats[k], view_vector,
//									n2, v2);
						}
						else if (doSmooth) {
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j + 1];
							n2 = mesh.n[i + 1][j + 1];
							tri[0].c = light.applyLight(mats[k], view_vector,
									n0, v0);
							tri[1].c = light.applyLight(mats[k], view_vector,
									n1, v1);
							tri[2].c = light.applyLight(mats[k], view_vector,
									n2, v2);
							
						} else {
							// flat shading: use the normal to the triangle
							// itself
							n2 = n1 = n0 = triangle_normal;
							point = new Vector3D((v0.x + v1.x + v2.x)/3, 
									(v0.y + v1.y + v2.y)/3, 
									(v0.z + v1.z + v2.z)/3);
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(
									mats[k], view_vector, triangle_normal,
									point);
						}

						tri[0].x = (int) v0.x;
						tri[0].y = (int) v0.y;
						tri[0].z = (int) v0.z;
						tri[1].x = (int) v1.x;
						tri[1].y = (int) v1.y;
						tri[1].z = (int) v1.z;
						tri[2].x = (int) v2.x;
						tri[2].y = (int) v2.y;
						tri[2].z = (int) v2.z;

						if (phong) {
							Triangle.drawTriangleWithPhong(buff, depthBuff, tri[0], tri[1], tri[2], n0, n1, n2, light, mats[k], view_vector, point);
						} else {
							Triangle.drawTriangle(buff, depthBuff, tri[0], tri[1], tri[2], doSmooth);
						}
					}

					v0 = mesh.v[i][j];
					v1 = mesh.v[i + 1][j + 1];
					v2 = mesh.v[i + 1][j];
					triangle_normal = computeTriangleNormal(v0, v1, v2);

					if (view_vector.dotProduct(triangle_normal) > 0.0) // front-facing
																		// triangle?
					{
						if (phong) {
							n0 = mesh.n[i][j];
							n1 = mesh.n[i + 1][j + 1];
							n2 = mesh.n[i + 1][j];
//							tri[0].c = light.applyLight(mats[k], view_vector,
//									n0, v0);
//							tri[1].c = light.applyLight(mats[k], view_vector,
//									n1, v1);
//							tri[2].c = light.applyLight(mats[k], view_vector,
//									n2, v2);
						}
						else if (doSmooth) {
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i + 1][j + 1];
							n2 = mesh.n[i + 1][j];
							tri[0].c = light.applyLight(mats[k], view_vector,
									n0, v0);
							tri[1].c = light.applyLight(mats[k], view_vector,
									n1, v1);
							tri[2].c = light.applyLight(mats[k], view_vector,
									n2, v2);
						} else {
							// flat shading: use the normal to the triangle
							// itself
							n2 = n1 = n0 = triangle_normal;
							point = new Vector3D((v0.x + v1.x + v2.x)/3, 
									(v0.y + v1.y + v2.y)/3, 
									(v0.z + v1.z + v2.z)/3);
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(
									mats[k], view_vector, triangle_normal,
									point);
						}

						tri[0].x = (int) v0.x;
						tri[0].y = (int) v0.y;
						tri[0].z = (int) v0.z;
						tri[1].x = (int) v1.x;
						tri[1].y = (int) v1.y;
						tri[1].z = (int) v1.z;
						tri[2].x = (int) v2.x;
						tri[2].y = (int) v2.y;
						tri[2].z = (int) v2.z;

						if (phong) {
							Triangle.drawTriangleWithPhong(buff, depthBuff, tri[0], tri[1], tri[2], n0, n1, n2, light, mats[k], view_vector, point);
						} else {
							Triangle.drawTriangle(buff, depthBuff, tri[0], tri[1], tri[2], doSmooth);
						}
					}
				}
			}
		}
	}

	// helper method that computes the unit normal to the plane of the triangle
	// degenerate triangles yield normal that is numerically zero
	private Vector3D computeTriangleNormal(Vector3D v0, Vector3D v1, Vector3D v2) {
		Vector3D e0 = v1.minus(v2);
		Vector3D e1 = v0.minus(v2);
		Vector3D norm = e0.crossProduct(e1);

		if (norm.magnitude() > 0.000001)
			norm.normalize();
		else
			// detect degenerate triangle and set its normal to zero
			norm.set((float) 0.0, (float) 0.0, (float) 0.0);

		return norm;
	}

}
