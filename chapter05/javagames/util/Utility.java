package javagames.util;

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
		Matrix3x3f reverseViewPortMatrix = Matrix3x3f.translate(tx, ty);
		reverseViewPortMatrix = reverseViewPortMatrix.mul(Matrix3x3f.scale(sx, sy));
		
		return reverseViewPortMatrix;
	}
}
