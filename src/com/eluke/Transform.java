package com.eluke;


/*
 * 	
		/*
 * TODO FlipTransform
 *  // Go to origin
  fullMatrix.setCol( Vector( -objectCenter.x(),
			     -objectCenter.y(),
			     -objectCenter.z() ),
		     3 );

  // Compose the full transformation
  fullMatrix = translateMatrix * rotateMatrix * scaleMatrix * fullMatrix;
 */
public class Transform {
	public static void rotatePoint(EndPoint point, double theta) {
		double cosTheta = Math.cos(theta);
		double sinTheta = Math.sin(theta);	
		
		double newX = point.x*cosTheta - point.y*sinTheta;
		double newY = point.x*sinTheta + point.y*cosTheta;
		point.x = (float)newX;
		point.y = (float)newY;
		point.theta += theta;
		point.theta %= 2.0*Math.PI;
		
		/*
		 * x' = xcosθ − ysinθ and y' = xsinθ + ycosθ.
		 */
		/*
		values[0][0] = cosTheta;
		values[0][1] = -sinTheta;
		values[1][0] = sinTheta;
		values[1][1] = cosTheta;
		double a1,a2,a3,a4,a5,a6;
		a1 = values[0][0] * point.x;
		a2 = values[1][0] * point.x;
		a3 = values[2][0] * point.x;
		a4 = values[0][1] * point.y;
		a5 = values[1][1] * point.y;
		a6 = values[2][1] * point.y;

		// TODO: do we need to divide by z?
		double x = a1+a4; // +a7 which is 0
		double y = a2+a5; // + a8 which is 0
		double z = a3+a6+1;
		System.out.println(a1 + " " + a4 + " =x");
		System.out.println("Z is " + z);
		point.x = x/z;
		point.y = y/z;
		point.theta += theta;
		*/
	}
	
	// NOTE: nothing needs to be kept after this line?!!
	/*
	
	private static class Matrix {
		double[][] values = null;
		static final Matrix IDENTITY_MATRIX = new Matrix(true);
		private Matrix(boolean id) {
			values = new double[][]{ {1,0,0},{0,1,0},{0,0,1}};
		}
		public Matrix() {
			values = new double[3][3];
		}
		public Matrix(double x, double y, double theta) {
			values = new double[][]{ {1,0,0},{0,1,0},{0,0,1}};
			translateTo(x,y);
			rotateTo(theta);
		}
		public Matrix(double x, double y) {
			values = new double[][]{ {1,0,0},{0,1,0},{0,0,1}};
			translateTo(x,y);
		}
		public Matrix(double theta) {
			values = new double[][]{ {1,0,0},{0,1,0},{0,0,1}};
			rotateTo(theta);
		}
		private Matrix(double[][] values) {
			this.values = values;
		}
		public Matrix(Matrix matrix) {
			values = new double[3][3];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					values[i][j] = matrix.values[i][j];
				}
			}
		}
		public void translateBy(double x, double y) {
			values[2][0] += x;
			values[2][1] += y;
		}
		public void translateTo(double x, double y) {
			values[2][0] = x;
			values[2][1] = y;
		} 
		public void rotateTo(double theta) {
			double cosTheta = Math.cos(theta);
			double sinTheta = Math.sin(theta);	
			System.out.println("Rotating to " + theta + " cos=" + cosTheta + " sin=" + sinTheta);

			values[0][0] = cosTheta;
			values[0][1] = -sinTheta;
			values[1][0] = sinTheta;
			values[1][1] = cosTheta;
		}

		public Matrix multiply(Matrix matrix) {
			double[][] newdata = new double[3][3];

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					newdata[i][j] = (double)0.0;
					for (int k = 0; k < 3; k++) {
						newdata[i][j] += values[i][k] * matrix.values[k][j];
					}
				}
			}
			return new Matrix(newdata);
		}

		public static class Pair {
			double x;
			double y;
			public Pair(double x, double y) {
				this.x = x;
				this.y = y;
			}

		}

		public void transform(Pair v) {
			double a1,a2,a3,a4,a5,a6,a7,a8,a9;
			a1 = values[0][0] * v.x;
			a2 = values[1][0] * v.x;
			a3 = values[2][0] * v.x;
			a4 = values[0][1] * v.y;
			a5 = values[1][1] * v.y;
			a6 = values[2][1] * v.y;

			// TODO: do we need to divide by z?
			double x = a1+a4; // +a7 which is 0
			double y = a2+a5; // + a8 which is 0
			double z = a3+a6+1;
			v.x = x/z;
			v.y = y/z;
		}
		
		public double getRotation() {
			return Math.acos(values[0][0]);
		}
	}

	private Matrix matrix = null;
	public static final Transform IDENTITY = new Transform();

	private Transform() {
		matrix = Matrix.IDENTITY_MATRIX;
	}
	public Transform(double theta, double x, double y) {
		matrix = new Matrix(x,y,theta);
	}


	public Transform(Transform initialTransform) {
		matrix = new Matrix(initialTransform.matrix);
	}
	
	public void translateBy(double x, double y) {
		matrix.translateBy(x, y);
	}
	public void rotateTo(double theta) {
		matrix.rotateTo(theta);
	}
	
	// Sets the transform such that it will rotate a given point by theta radians
	// around a center specified by centerX, centerY.
	public void rotateAroundCenter(double theta, double centerX, double centerY) {

		// go to origin
		matrix.translateTo(-centerX, -centerY);

		// rotate by theta
		Matrix rotation = new Matrix(theta);

		// return from origin
		Matrix returnFromOrigin = new Matrix(centerX, centerY);

		matrix = returnFromOrigin.multiply(rotation.multiply(matrix));

	}
	
	// Rotate such that the transform includes the translation and rotation of the point.
	public void rotateAndTranslate(EndPoint point) {
		Matrix rotation = new Matrix(point.theta);
		Matrix translate = new Matrix(point.x, point.y);
		matrix = translate.multiply(rotation.multiply(matrix));

	}

	public void transform(EndPoint point) {
		Matrix.Pair coords = new Matrix.Pair(point.x, point.y);
		System.out.println("Matrix rotation before is " + matrix.getRotation());
		matrix.transform(coords);
		point.theta += matrix.getRotation();

	}
	*/
}
