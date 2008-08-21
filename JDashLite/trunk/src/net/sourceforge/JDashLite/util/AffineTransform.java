/*********************************************************
 * 
 * @author spowell
 * AffineTransform.java
 * Aug 18, 2008
 *
Copyright (C) 2008 Shane Powell

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *********************************************************/

package net.sourceforge.JDashLite.util;

import waba.fx.Coord;
import waba.fx.Rect;
import waba.sys.Convert;

/*********************************************************
 * Basic 2d Matrix Math Affine Transforms.
 *
 *********************************************************/
public class AffineTransform
{

	private static final int AFFINE_MATRIX_SIZE = 3;
	
	private Matrix matrix_ = null;
	
	/******************************************************
	 * Private because the only way to make an AffineTransform is with 
	 * one of the static xxxInstance methods.
	 *******************************************************/
	private AffineTransform(Matrix m)
	{
		this.matrix_ = m;
	}
	
	
	/*********************************************************
	 * Output a standard matrix format string.
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 ********************************************************/
	public String toString()
	{
		return this.matrix_.toString();
	}
	
	
	/*******************************************************
	 * @param r
	 ********************************************************/
	public void apply(Rect r)
	{
		PointVector v1 = new PointVector(r.x, r.y);
		PointVector v2 = new PointVector(r.x + r.width, r.y + r.height);
		v1 = multiply(v1, this.matrix_);
		v2 = multiply(v2, this.matrix_);
		r.x = v1.getX();
		r.y = v1.getY();
		r.width = v2.getX() - v1.getX();
		r.height = v2.getY() - v1.getY();
	}
	
	/*******************************************************
	 * Apply this transform to the provided Coord object
	 * @param c
	 * @return
	 ********************************************************/
	public void apply(Coord point)
	{
		PointVector v = new PointVector(point.x, point.y);
		v = multiply(v, this.matrix_);
		point.x = v.getX();
		point.y = v.getY();
	}
	
	
	/*******************************************************
	 * Apply this transform to the array of points.
	 * @param points
	 ********************************************************/
	public void apply(Coord[] points)
	{
		for (int index = 0; index < points.length; index++)
		{
			apply(points[index]);
		}
	}
	
	/*******************************************************
	 * add to this transform, the provided translate values.
	 * @param x
	 * @param y
	 ********************************************************/
	public void addTranslate(int x, int y)
	{
		Matrix m = createTranslateMatrix(x, y);
		this.matrix_ = multiply(m, this.matrix_);
	}
	
	
	/*******************************************************
	 * add to this transform, a rotate transform.
	 * @param theta
	 ********************************************************/
	public void addRotate(double theta)
	{
		Matrix m = createRotateMatrix(theta);
		this.matrix_ = multiply(m, this.matrix_);
	}
	
	
	/******************************************************
	 * Add to this transform, a scale transform.
	 * @param x
	 * @param y
	 ********************************************************/
	public void addScale(double x, double y)
	{
		Matrix m = createScaleMatrix(x, y);
		this.matrix_ = multiply(m, this.matrix_);
	}
	
	/******************************************************
	 * Create a new Translate Transform instance.
	 * @param x
	 * @param y
	 * @return
	 ********************************************************/
	public static AffineTransform translateInstance(int x, int y)
	{
		return new AffineTransform(createTranslateMatrix(x, y));
	}
	
	/******************************************************
	 * Create a new Rotate Transform Instance.
	 * @param x
	 * @param y
	 * @return
	 ********************************************************/
	public static AffineTransform rotateInstance(double theta)
	{
		return new AffineTransform(createRotateMatrix(theta));
	}
	
	/******************************************************
	 * Create a new Scale Transform Instance.
	 * @param x
	 * @param y
	 * @return
	 ********************************************************/
	public static AffineTransform scaleInstance(int x, int y)
	{
		return new AffineTransform(createScaleMatrix(x, y));
	}
	
	
	/*******************************************************
	 * Used to apply a Matrix to a Vector.
	 * @param m
	 * @param v
	 * @return
	 ********************************************************/
	private static PointVector multiply(PointVector v, Matrix m)
	{
		PointVector r = new PointVector(0,0);
		
		double ax; 	// accumulator for matrix multiplication
		
		
		for (int rIndex = 0; rIndex < m.getSize(); rIndex++)
		{
			ax = 0;
			
			for (int vIndex = 0; vIndex < m.getSize(); vIndex++)
			{
				ax += ((double)v.m_[vIndex]) * m.m_[rIndex][vIndex]; 
			}
			
			r.m_[rIndex] = (int)Math.round(ax);
		}
		
		
		return r;
	}
	
	
	
	/*******************************************************
	 * @param x
	 * @param y
	 * @return
	 ********************************************************/
	private static Matrix createScaleMatrix(double x, double y)
	{
		
		Matrix t = new Matrix();
		t.m_[0][0] = x; t.m_[0][1] = 0; t.m_[0][2] = 0;
		t.m_[1][0] = 0; t.m_[1][1] = y; t.m_[1][2] = 0;
		t.m_[2][0] = 0; t.m_[2][1] = 0; t.m_[2][2] = 1;
		return t;
	}
	
	/*******************************************************
	 * @param theta IN - the degrees in Radians to rotate
	 * @return
	 ********************************************************/
	private static Matrix createRotateMatrix(double theta)
	{
		Matrix t = new Matrix();
		t.m_[0][0] = Math.cos(theta); t.m_[0][1] = Math.sin(theta)*-1; t.m_[0][2] = 0;
		t.m_[1][0] = Math.sin(theta); t.m_[1][1] = Math.cos(theta);    t.m_[1][2] = 0;
		t.m_[2][0] = 0;               t.m_[2][1] = 0;                  t.m_[2][2] = 1;
		return t;
	}

	
	
	/*******************************************************
	 * @param x
	 * @param y
	 * @return
	 ********************************************************/
	private static Matrix createTranslateMatrix(int x, int y)
	{
		Matrix t = new Matrix();
		t.m_[0][0] = 1; t.m_[0][1] = 0; t.m_[0][2] = x;
		t.m_[1][0] = 0; t.m_[1][1] = 1; t.m_[1][2] = y;
		t.m_[2][0] = 0; t.m_[2][1] = 0; t.m_[2][2] = 1;
		return t;
	}
	
	
	/*******************************************************
	 * Adds Matrix a and b.  Assumes both matrix are the same dimension size.
	 * @param a
	 * @param b
	 * @return
	 ********************************************************/
	private static Matrix add(Matrix a, Matrix b)
	{
		Matrix c = new Matrix();
		
		for (int i = 0;i < a.getSize(); i++)
		{
			for(int j = 0;j < a.getSize(); j++)
			{
				c.m_[i][j] = a.m_[i][j] + b.m_[i][j];
			}
		}
		
		return c;
	}

	
	
	/*******************************************************
	 * Multiply matrix a by matrix b. Assumes both matrix are the same dimension size.
	 * The order is important in the case of translates with rotates.
	 * Think of it this way, matrix "a" is multiplied INTO matrix "b".
	 * It's kinda funny for some to think about, but it's formatted 
	 * simmilar to the way C does operations.  op0 into op1
	 * 
	 * @param a
	 * @param b
	 * @return
	 ********************************************************/
	private static Matrix multiply(Matrix a, Matrix b)
	{
		double ax; 	// accumulator for matrix multiplication
		
		Matrix c = new Matrix();
		for (int i = 0;i < a.getSize(); i++)
		{
			for(int j = 0;j < a.getSize(); j++)
			{ 
				ax = 0; 	// reset accumulator
				for(int k = 0;k < a.getSize(); k++)
				{
					ax = ax + (a.m_[i][k] * b.m_[k][j]);
				}
				c.m_[i][j] = ax;
			}
		}
		
		return c;

	}
	
	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static class Matrix 
	{
		protected double[][] m_ = null;
		
		/******************************************************
		 * @param size
		 *******************************************************/
		protected Matrix()
		{
			this.m_ = new double[AFFINE_MATRIX_SIZE][AFFINE_MATRIX_SIZE];
		}
		
		/*******************************************************
		 * @return
		 ********************************************************/
		public int getSize()
		{
			return this.m_.length;
		}

		
		/*********************************************************
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 ********************************************************/
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append("\n");
			for (int i = 0; i < getSize(); i++)
			{
				sb.append("|");
				for (int j = 0; j < getSize(); j++)
				{
					sb.append(Convert.toString(m_[i][j]));
					sb.append(" ");
				}
				sb.append("|\n");
			}
			
			return sb.toString();
		}
		
	}
	
	
	/********************************************************
	 * 
	 *
	 *********************************************************/
	private static class PointVector
	{
		public int[] m_ = null;
		
		/********************************************************
		 * @param size
		 *******************************************************/
		protected PointVector(int x, int y)
		{
			this.m_ = new int[AFFINE_MATRIX_SIZE];
			this.m_[0] = x;
			this.m_[1] = y;
			this.m_[2] = 1;
		}

		/*******************************************************
		 * @return
		 ********************************************************/
		public int getSize()
		{
			return this.m_.length;
		}
		
		/********************************************************
		 * @return
		 ********************************************************/
		public int getX()
		{
			return this.m_[0];
		}
		
		/*******************************************************
		 * @return
		 ********************************************************/
		public int getY()
		{
			return this.m_[1];
		}
		
		/*********************************************************
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 ********************************************************/
		public String toString()
		{
			StringBuffer sb = new StringBuffer(15);
			sb.append("\n");
			for (int i = 0; i < getSize(); i++)
			{
				sb.append("|");
				sb.append(Convert.toString(m_[i]));
				sb.append("|\n");
			}
			
			return sb.toString();
		}
	}
	
	
}
