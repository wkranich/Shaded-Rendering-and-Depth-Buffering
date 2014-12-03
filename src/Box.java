/*
 * Box.java
 * 
 * Class for a 3D box
 * 
 * History: Nov 27, 2014 Created by William Kranich
 */
public class Box {
	private Vector3D center, vec, uvec, vvec;
	private float r;
	private int m, n;
	public Mesh3D[] meshes;
	
	private float umax, umin, vmax, vmin;
	
	public Box(float x, float y, float z, float r, int m, int n) {
		this.center = new Vector3D(x, y, z);
		this.r = r;
		this.uvec = new Vector3D(0, 1*this.r, 0);
		this.vvec = new Vector3D(0, 0, 1*this.r);
		this.m = m;
		this.n = n;
		this.umin = -1;
		this.umax = 1;
		this.vmin = -1;
		this.vmax = 1;
		this.vec = center.plus(new Vector3D(-this.r, 0, 0));
		initMesh();
	}
	
	public void setCenter(float x, float y, float z) {
		this.center.x = x;
		this.center.y = y;
		this.center.z = z;
		fillMesh();
	}
	
	public void setRadiusX(float r) {
		this.r = r;
	}
	
	public void setM(int m) {
		this.m = m;
		initMesh();
	}
	
	public void setN(int n) {
		this.n = n;
		initMesh();
	}
	
	public Vector3D getCenter() {
		return this.center;
	}
	
	public float getRadius() {
		return this.r;
	}
	
	public int get_m(){
		return this.m;
	}
	
	public int get_n(){
		return this.n;
	}
	
	private void initMesh() {
		meshes = new Mesh3D[6];
		fillMesh();
	}
	
	private void fillMesh() {
		int i, j;
		float theta, phi;
		float d_phi = (umax-umin)/((float)n-1);
		float d_theta = (vmax-vmin)/((float)m-1);
		
		for (int side = 0; side < 6; ++side) {
			meshes[side] = new Mesh3D(m,n);
			if(side == 1) {
				vec = center.plus(new Vector3D(this.r, 0, 0));
				uvec = uvec.scale(-1); // negative for normal
			} else if (side == 2) {
				vec = center.plus(new Vector3D(0, -this.r, 0));
				uvec = new Vector3D(1*r, 0, 0);
				vvec = new Vector3D(0, 0, 1*r);
			} else if (side == 3) {
				vec = center.plus(new Vector3D(0, this.r, 0));
				// Swap for normals
				Vector3D temp = uvec;
				uvec = vvec;
				vvec = temp; 
			} else if (side == 4) {
				vec = center.plus(new Vector3D(0, 0, -this.r));
				uvec = new Vector3D(-1*r, 0, 0);
				vvec = new Vector3D(0, 1*r, 0);
			} else if (side == 5) {
				vec = center.plus(new Vector3D(0, 0, this.r));
				uvec = uvec.scale(-1); // negative for normal
			}
			
			for (i = 0, theta = vmin; i < m; ++i, theta += d_theta) {
				for (j = 0, phi = umin; j < n; ++j, phi += d_phi) {
					
					// Compute vertex
					meshes[side].v[i][j] = vec.plus(uvec.scale(theta).plus(vvec.scale(phi)));
					
					// Computer normals
					meshes[side].n[i][j] = uvec.crossProduct(vvec);
					meshes[side].n[i][j].normalize();
				}
			}
		}
	}
}
