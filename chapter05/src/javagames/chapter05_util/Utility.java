package javagames.chapter05_util;

import java.awt.Graphics;
import java.util.List;

import javagames.chapter03_util.Matrix3x3f;
import javagames.chapter03_util.Vector2f;

public class Utility {
	
	// ViewPort matrix converts world co-ordinate to screen co-ordinate
	public static Matrix3x3f createViewPortMatrix (float worldWidth, float worldHeight, float screenWidth, float screenHeight) {
		float sx = (screenWidth - 1) / worldWidth;
		float sy = (screenHeight - 1) / worldHeight;
		float tx = (screenWidth - 1) / 2.0f;
		float ty = (screenHeight - 1) / 2.0f;
		
		Matrix3x3f viewPortMatrix = Matrix3x3f.scale(sx, -sy);
		viewPortMatrix = viewPortMatrix.mul(Matrix3x3f.translate(tx, ty));
		
		return viewPortMatrix;
 	}
	
	// Reverse viewport matrix converts screen co-ordinate to world co-ordinate. We need this to convert mouse cursor point to our world
	// co-ordinate system. 
	public static Matrix3x3f createReverseViewPortMatrix(float worldWidth, float worldHeight, float screenWidth, float screenHeight) {
		// Since it does the opposite of the ViewPort matrix, we will just flip the scaling factors
		float sx = worldWidth / (screenWidth - 1);
		float sy = worldHeight / (screenHeight - 1);
		float tx = (screenWidth - 1) / 2.0f;
		float ty = (screenHeight - 1) / 2.0f;
		
		// Now, we will apply these transformation in the reverse order (First transformation and then scaling)
		// Also we have to negate the translation factors to reverse the translation
		Matrix3x3f reverseViewPortMatrix = Matrix3x3f.translate(-tx, -ty);
		reverseViewPortMatrix = reverseViewPortMatrix.mul(Matrix3x3f.scale(sx, -sy));
		
		return reverseViewPortMatrix;
	}
	
	// Draws polygon using vector array
	public static void drawPolygon(Graphics g, Vector2f[] polygon) {
		// Draw lines from point s to point p
		Vector2f p;
		Vector2f s = polygon[polygon.length - 1];
		
		for (int i = 0; i < polygon.length; i++) {
			p = polygon[i];
			g.drawLine((int)s.x, (int)s.y, (int)p.x, (int)p.y);
			s = p;
		}
	}
	
	// Draws polygon using vector list (ArrayList, LinkedList whatever)
		public static void drawPolygon(Graphics g, List<Vector2f> polygon) {
			// Draw lines from point s to point p
			Vector2f s = polygon.get(polygon.size() - 1);
			
			for (Vector2f p : polygon) {
				g.drawLine((int)s.x, (int)s.y, (int)p.x, (int)p.y);
				s = p;
			}
		}
}
