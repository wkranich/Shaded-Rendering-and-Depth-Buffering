import java.util.*;
/*
 * Light.java
 * 
 * Supports point, infinite, and ambient light sources
 * 
 * History: Nov 28, 2014 Created by William Kranich
 */
public class Light {
	
	public List<Light> lights;
	public boolean lightIsOn;
	
	public Light() {
		this.lights = new ArrayList<Light>();
		lightIsOn = true;
	}
	
	public void addLight(Light newLight) {
		lights.add(newLight);
	}
	
	public void removeLight(Light l) {
		lights.remove(l);
	}
	
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Vector3D p) {
		ColorType res = new ColorType();
		ColorType temp = new ColorType();
		ColorType amb = new ColorType();
		for (Light l : lights) {
			if (l instanceof AmbientLight) {
				if (l.lightIsOn) {
					amb = l.applyLight(mat, v, n, p);
				}
			} else {
				if (l.lightIsOn) {
					temp = l.applyLight(mat, v, n, p);
					res.r += temp.r;
					res.g += temp.g;
					res.b += temp.b;
				}
			}
		}
		
		// I = Iamb + Sum[I_l,diff + I_l,spec]
		res.r = amb.r + res.r;
		res.g = amb.g + res.g;
		res.b = amb.b + res.b;
		
		// Clamp
		res.r = (float) Math.min(1.0, res.r);
		res.g = (float) Math.min(1.0, res.g);
		res.b = (float) Math.min(1.0, res.b);
		
		return res;
	}
	
	public void toggleLight() {
		lightIsOn = !lightIsOn;
	}
	
	
}
