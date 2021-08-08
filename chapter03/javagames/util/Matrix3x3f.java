package javagames.util;

public class Matrix3x3f {
	private float[][] m = new float[3][3];
	
	public Matrix3x3f() {}
	
	public Matrix3x3f(float[][] m) {
		setMatrix(m);
	}
	
	public void setMatrix(float[][] m) {
		this.m = m;
	}
	
	public Matrix3x3f add(Matrix3x3f m1) {
		return new Matrix3x3f(new float[][] {
			{
			this.m[0][0] + m1.m[0][0],
			this.m[0][1] + m1.m[0][1],
			this.m[0][2] + m1.m[0][2],
			},
			{
			this.m[1][0] + m1.m[1][0],
			this.m[1][1] + m1.m[1][1],
			this.m[1][2] + m1.m[1][2],
			},
			{
			this.m[2][0] + m1.m[2][0],
			this.m[2][1] + m1.m[2][1],
			this.m[2][2] + m1.m[2][2],
			}
		});
	}
	
	public Matrix3x3f sub(Matrix3x3f m1) {
		return new Matrix3x3f(new float[][] {
			{
			this.m[0][0] - m1.m[0][0],
			this.m[0][1] - m1.m[0][1],
			this.m[0][2] - m1.m[0][2],
			},
			{
			this.m[1][0] - m1.m[1][0],
			this.m[1][1] - m1.m[1][1],
			this.m[1][2] - m1.m[1][2],
			},
			{
			this.m[2][0] - m1.m[2][0],
			this.m[2][1] - m1.m[2][1],
			this.m[2][2] - m1.m[2][2],
			}
		});
	}
	
	// Multiplication with another 3x3 matrix
	public Matrix3x3f mul(Matrix3x3f m1) {
		return new Matrix3x3f(new float[][] {
			{
			this.m[0][0] * m1.m[0][0] +
			this.m[0][1] * m1.m[1][0] +
			this.m[0][2] * m1.m[2][0],
			this.m[0][0] * m1.m[0][1] +
			this.m[0][1] * m1.m[1][1] +
			this.m[0][2] * m1.m[2][1],
			this.m[0][0] * m1.m[0][2] +
			this.m[0][1] * m1.m[1][2] +
			this.m[0][2] * m1.m[2][2]
			},
			{
			this.m[1][0] * m1.m[0][0] +
			this.m[1][1] * m1.m[1][0] +
			this.m[1][2] * m1.m[2][0],
			this.m[1][0] * m1.m[0][1] +
			this.m[1][1] * m1.m[1][1] +
			this.m[1][2] * m1.m[2][1],
			this.m[1][0] * m1.m[0][2] +
			this.m[1][1] * m1.m[1][2] +
			this.m[1][2] * m1.m[2][2]
			},
			{
			this.m[2][0] * m1.m[0][0] +
			this.m[2][1] * m1.m[1][0] +
			this.m[2][2] * m1.m[2][0],
			this.m[2][0] * m1.m[0][1] +
			this.m[2][1] * m1.m[1][1] +
			this.m[2][2] * m1.m[2][1],
			this.m[2][0] * m1.m[0][2] +
			this.m[2][1] * m1.m[1][2] +
			this.m[2][2] * m1.m[2][2]
			}
		});
	}
	
	// Note: For multiplication with a vector, we're using the row major format.
	// It can be also done in column major format where we use a 3x1 vector shape. But then, we have to reverse the order of the different operations to get the same result
	// For instance, translate then rotate will become rotate and then translate.
	public Vector2f mul(Vector2f vec) {
		return new Vector2f(
				vec.x * this.m[0][0] +
				vec.y * this.m[1][0] +
				vec.w * this.m[2][0],
				vec.x * this.m[0][1] +
				vec.y * this.m[1][1] +
				vec.w * this.m[2][1],
				vec.x * this.m[0][0] +
				vec.y * this.m[1][0] +
				vec.w * this.m[2][0]
		);
	}

	
	public static Matrix3x3f zero() {
		return new Matrix3x3f(new float[][] {
			{0.0f, 0.0f, 0.0f},
			{0.0f, 0.0f, 0.0f},
			{0.0f, 0.0f, 0.0f}
		});
	}
	
	public static Matrix3x3f identity() {
		return new Matrix3x3f(new float[][] {
			{1.0f, 0.0f, 0.0f},
			{0.0f, 1.0f, 0.0f},
			{0.0f, 0.0f, 1.0f}
		});
	}
	
	public static Matrix3x3f translate(Vector2f v) {
		return translate(v.x, v.y);
	}
	
	public static Matrix3x3f translate(float x, float y) {
		return new Matrix3x3f(new float[][] {
			{1.0f, 0.0f, 0.0f},
			{0.0f, 1.0f, 0.0f},
			{	x,	y, 1.0f}
		});
	}
	
	public static Matrix3x3f scale(Vector2f v) {
		return scale(v.x, v.y);
	}
	
	public static Matrix3x3f scale(float x, float y) {
		return new Matrix3x3f(new float[][] {
			{	x, 0.0f, 0.0f},
			{0.0f,	y, 0.0f},
			{0.0f, 0.0f, 1.0f}
		});
	}
	
	public static Matrix3x3f shear(Vector2f v) {
		return shear(v.x, v.y);
	}
	
	public static Matrix3x3f shear(float x, float y) {
		return new Matrix3x3f(new float[][] {
			{1.0f,	y, 0.0f},
			{	x, 1.0f, 0.0f},
			{0.0f, 0.0f, 1.0f}
		});
	}
	
	public static Matrix3x3f rotate(float rad) {
		return new Matrix3x3f(new float[][] {
			{(float) Math.cos(rad), (float) Math.sin(rad), 0.0f},
			{(float) -Math.sin(rad), (float) Math.cos(rad), 0.0f},
			{					0.0f,					0.0f, 1.0f}
		});
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 3; i++) {
			sb.append("[");
			sb.append(m[i][0]);
			sb.append(",\t");
			sb.append(m[i][1]);
			sb.append(",\t");
			sb.append(m[i][2]);
			sb.append("]\n");
		}
		
		return sb.toString();
	}
}
