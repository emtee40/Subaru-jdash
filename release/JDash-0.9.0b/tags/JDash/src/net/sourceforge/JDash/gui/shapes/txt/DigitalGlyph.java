/*******************************************************
 * 
 *  @author spowell
 *  DigitalGlyph.java
 *  Aug 11, 2006
 *  $Id: DigitalGlyph.java,v 1.3 2006/09/14 02:03:44 shaneapowell Exp $
 *
Copyright (C) 2006  Shane Powell

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
 ******************************************************/
package net.sourceforge.JDash.gui.shapes.txt;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*******************************************************
 * This class represents a single glyph to draw a sequence
 * of pollygons to a GC for the purpose of creating
 * a single text character.  Each available text
 * character is made from one of these. To
 * create a character glyph, create an instance of
 * one of these, then using the methods.... describe
 * how to build the font using the segments provided.
 * The sequence of polygons returned from this
 * glyph all have an origin of the upper left corner
 * of the character.
 ******************************************************/
public class DigitalGlyph implements Glyph
{

	
	
	/* Each glyph is made up of a list of shapes */
	private List<Shape> glyphShapes_ = null;
	
	
	/* A deign grid and final grid value represents the number
	 of points to the left,rigth, up and down from the origin. So
	 the total grid size is each value / 2.  Right? These are
	 not the total widths and height, but instead the length
	 of an axis into a single grid space */
	
	/* Setup the segment constants */
	private static final double DESIGN_WIDTH 	= 18D;	/* 18 cells make up the total width */
	private static final double DESIGN_HEIGHT 	= 34D; /* 34 cells make up the total height */
	private static final double SEGMENT_WIDTH   = 4D; /* The number of cells wide a segment is */
	private static final double CELL_SIZE		= 100D;
	
	/* The points to make up the outer segment.  This segment has an origin in it's center. 
	 * Also, this segment is defined as the top segment. So, to make other segments
	 * from this you, you need to apply rotate and translate transformations */
	private static final ArrayList<Point2D> OUTTER_SEGMENT = new ArrayList<Point2D>();
	private static final ArrayList<Point2D> EXTENDED_OUTTER_SEGMENT = new ArrayList<Point2D>();
	
	/* Some characters use a 45 angle segment like the X, but is not connected by a middle segment */
	private static final ArrayList<Point2D> RIGHT_ANGLE_SEGMENT = new ArrayList<Point2D>();
	private static final ArrayList<Point2D> LEFT_ANGLE_SEGMENT = new ArrayList<Point2D>();
	
	/* The points to make up the middle segment.  This segment has an origin in it's center */
	private static final ArrayList<Point2D> MIDDLE_SEGMENT = new ArrayList<Point2D>();

	/* This is the same as a middle segment, but 1/2 it's length and still the same width */
	private static final ArrayList<Point2D> HALF_MIDDLE_SEGMENT = new ArrayList<Point2D>();
	
	/* Just like the normal outter segment, except the ends are pointy to accomodate the letter D or R */
	private static final ArrayList<Point2D> LEFT_POINT_OUTTER_SEGMENT = new ArrayList<Point2D>();
	private static final ArrayList<Point2D> RIGHT_POINT_OUTTER_SEGMENT = new ArrayList<Point2D>();

	private static final ArrayList<Point2D> LEFT_FLAT_OUTTER_SEGMENT = new ArrayList<Point2D>();
	private static final ArrayList<Point2D> RIGHT_FLAT_OUTTER_SEGMENT = new ArrayList<Point2D>();
	
	/* Holder for the segments that make up this glyph */
	private ArrayList<Segment> segments_ = new ArrayList<Segment>();
	
	/**
	 * This static initializer will build each of the segment point array lists
	 * Using these segments, an "8" will be exactly 18 wide and 34 high
	 */
	static
	{
		/* The length of a segment is calculated as the width of the 
		 * design area minus 1/2 of the segment width at each end. */
		
		OUTTER_SEGMENT.add(createPoint(-7D * CELL_SIZE, -1D * CELL_SIZE));
		OUTTER_SEGMENT.add(createPoint(-6D * CELL_SIZE, -2D * CELL_SIZE));
		OUTTER_SEGMENT.add(createPoint(+6D * CELL_SIZE, -2D * CELL_SIZE));
		OUTTER_SEGMENT.add(createPoint(+7D * CELL_SIZE, -1D * CELL_SIZE));
		OUTTER_SEGMENT.add(createPoint(+4D * CELL_SIZE, +2D * CELL_SIZE));
		OUTTER_SEGMENT.add(createPoint(-4D * CELL_SIZE, +2D * CELL_SIZE));
		
		EXTENDED_OUTTER_SEGMENT.add(createPoint(-8D * CELL_SIZE, -1D * CELL_SIZE));
		EXTENDED_OUTTER_SEGMENT.add(createPoint(-7D * CELL_SIZE, -2D * CELL_SIZE));
		EXTENDED_OUTTER_SEGMENT.add(createPoint(+7D * CELL_SIZE, -2D * CELL_SIZE));
		EXTENDED_OUTTER_SEGMENT.add(createPoint(+8D * CELL_SIZE, -1D * CELL_SIZE));
		EXTENDED_OUTTER_SEGMENT.add(createPoint(+5D * CELL_SIZE, +2D * CELL_SIZE));
		EXTENDED_OUTTER_SEGMENT.add(createPoint(-5D * CELL_SIZE, +2D * CELL_SIZE));
		
		LEFT_POINT_OUTTER_SEGMENT.add(createPoint(-8D * CELL_SIZE, -2D * CELL_SIZE));
		LEFT_POINT_OUTTER_SEGMENT.add(createPoint(+6D * CELL_SIZE, -2D * CELL_SIZE));
		LEFT_POINT_OUTTER_SEGMENT.add(createPoint(+7D * CELL_SIZE, -1D * CELL_SIZE));
		LEFT_POINT_OUTTER_SEGMENT.add(createPoint(+4D * CELL_SIZE, +2D * CELL_SIZE));
		LEFT_POINT_OUTTER_SEGMENT.add(createPoint(-4D * CELL_SIZE, +2D * CELL_SIZE));
		
		LEFT_FLAT_OUTTER_SEGMENT.add(createPoint(-4D * CELL_SIZE, -2D * CELL_SIZE));
		LEFT_FLAT_OUTTER_SEGMENT.add(createPoint(+6D * CELL_SIZE, -2D * CELL_SIZE));
		LEFT_FLAT_OUTTER_SEGMENT.add(createPoint(+7D * CELL_SIZE, -1D * CELL_SIZE));
		LEFT_FLAT_OUTTER_SEGMENT.add(createPoint(+4D * CELL_SIZE, +2D * CELL_SIZE));
		LEFT_FLAT_OUTTER_SEGMENT.add(createPoint(-4D * CELL_SIZE, +2D * CELL_SIZE));

		RIGHT_POINT_OUTTER_SEGMENT.add(createPoint(-7D * CELL_SIZE, -1D * CELL_SIZE));
		RIGHT_POINT_OUTTER_SEGMENT.add(createPoint(-6D * CELL_SIZE, -2D * CELL_SIZE));
		RIGHT_POINT_OUTTER_SEGMENT.add(createPoint(+8D * CELL_SIZE, -2D * CELL_SIZE));
		RIGHT_POINT_OUTTER_SEGMENT.add(createPoint(+4D * CELL_SIZE, +2D * CELL_SIZE));
		RIGHT_POINT_OUTTER_SEGMENT.add(createPoint(-4D * CELL_SIZE, +2D * CELL_SIZE));
		
		RIGHT_FLAT_OUTTER_SEGMENT.add(createPoint(-7D * CELL_SIZE, -1D * CELL_SIZE));
		RIGHT_FLAT_OUTTER_SEGMENT.add(createPoint(-6D * CELL_SIZE, -2D * CELL_SIZE));
		RIGHT_FLAT_OUTTER_SEGMENT.add(createPoint(+4D * CELL_SIZE, -2D * CELL_SIZE));
		RIGHT_FLAT_OUTTER_SEGMENT.add(createPoint(+4D * CELL_SIZE, +2D * CELL_SIZE));
		RIGHT_FLAT_OUTTER_SEGMENT.add(createPoint(-4D * CELL_SIZE, +2D * CELL_SIZE));
				
		MIDDLE_SEGMENT.add(createPoint(-7D * CELL_SIZE, -0D * CELL_SIZE));
		MIDDLE_SEGMENT.add(createPoint(-5D * CELL_SIZE, -2D * CELL_SIZE));
		MIDDLE_SEGMENT.add(createPoint(+5D * CELL_SIZE, -2D * CELL_SIZE));
		MIDDLE_SEGMENT.add(createPoint(+7D * CELL_SIZE, +0D * CELL_SIZE));
		MIDDLE_SEGMENT.add(createPoint(+5D * CELL_SIZE, +2D * CELL_SIZE));
		MIDDLE_SEGMENT.add(createPoint(-5D * CELL_SIZE, +2D * CELL_SIZE));
		
		HALF_MIDDLE_SEGMENT.add(createPoint(-4D * CELL_SIZE, -0D * CELL_SIZE));
		HALF_MIDDLE_SEGMENT.add(createPoint(-2.5D * CELL_SIZE, -2D * CELL_SIZE));
		HALF_MIDDLE_SEGMENT.add(createPoint(+2.5D * CELL_SIZE, -2D * CELL_SIZE));
		HALF_MIDDLE_SEGMENT.add(createPoint(+4D * CELL_SIZE, +0D * CELL_SIZE));
		HALF_MIDDLE_SEGMENT.add(createPoint(+2.5D * CELL_SIZE, +2D * CELL_SIZE));
		HALF_MIDDLE_SEGMENT.add(createPoint(-2.5D * CELL_SIZE, +2D * CELL_SIZE));
		
		RIGHT_ANGLE_SEGMENT.add(createPoint(-4D * CELL_SIZE, +7D * CELL_SIZE));
		RIGHT_ANGLE_SEGMENT.add(createPoint(-4D * CELL_SIZE, +3.5D * CELL_SIZE));
		RIGHT_ANGLE_SEGMENT.add(createPoint(+2.5D * CELL_SIZE, -7D * CELL_SIZE));
		RIGHT_ANGLE_SEGMENT.add(createPoint(+4D * CELL_SIZE, -7D * CELL_SIZE));
		RIGHT_ANGLE_SEGMENT.add(createPoint(+4D * CELL_SIZE, -3.5D * CELL_SIZE));
		RIGHT_ANGLE_SEGMENT.add(createPoint(-2.5D * CELL_SIZE, +7D * CELL_SIZE));
		
		LEFT_ANGLE_SEGMENT.add(createPoint(-4D * CELL_SIZE, -7D * CELL_SIZE));
		LEFT_ANGLE_SEGMENT.add(createPoint(-2.5D * CELL_SIZE, -7D * CELL_SIZE));
		LEFT_ANGLE_SEGMENT.add(createPoint(+4D * CELL_SIZE, 3.5D * CELL_SIZE));
		LEFT_ANGLE_SEGMENT.add(createPoint(+4D * CELL_SIZE, +7D * CELL_SIZE));
		LEFT_ANGLE_SEGMENT.add(createPoint(+2.5D * CELL_SIZE, +7D * CELL_SIZE));
		LEFT_ANGLE_SEGMENT.add(createPoint(-4D * CELL_SIZE, -3.5D * CELL_SIZE));
	
	}
	
	/******************************************************
	 * Setup this glyph to represent the request
	 * character.  If the given character is not supported
	 * then an exception will be thrown.
	 * @param c
	 ******************************************************/
	protected DigitalGlyph(char c,int charWidth, int charHeight)
	{

		
		double gridX = (DESIGN_WIDTH * CELL_SIZE) / 2;
		double gridY = DESIGN_HEIGHT * CELL_SIZE / 2;
		double segmentWidth = SEGMENT_WIDTH * CELL_SIZE;
		double segmentLength = DESIGN_WIDTH * CELL_SIZE; 
		
		
		
		/* Period Dot */
		if (inArray(new char[]{'.'}, c))
		{
			addEllipseSegment(segmentWidth * 1.5D, segmentWidth * 1.5D, 0, 0, (+1 * gridY) - (segmentWidth));
		}
		
		/* Lower : dot */
		if (inArray(new char[]{':'}, c))
		{
			addEllipseSegment(segmentWidth * 1.5D, segmentWidth * 1.5D, 0, 0, segmentLength / 2);
		}
		
		/* Upper : dot */
		if (inArray(new char[]{':'}, c))
		{
			addEllipseSegment(segmentWidth * 1.5D, segmentWidth * 1.5D, 0, 0, -1 * segmentLength / 2);
		}
		
		/* STANDARD TOP */
		if (inArray(new char[]{'0','2','3','5','6','7','8','9','A','C','E','F','G','M','O','P','Q','R','S','Z',']','[','(',')'}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, 0, 0,    (-1 * gridY) + (segmentWidth / 2));
		}
		
		/* EXTENDED TOP */
		if (inArray(new char[]{'T'}, c))
		{
			addPolygonSegment(EXTENDED_OUTTER_SEGMENT, 0, 0,    (-1 * gridY) + (segmentWidth / 2));
		}
		
		/* LEFT POINTED TOP */
		if (inArray(new char[]{'B','D'}, c))
		{
			addPolygonSegment(LEFT_POINT_OUTTER_SEGMENT, 0, 0,    (-1 * gridY) + (segmentWidth / 2));
		}
		
		/* STANDARD BOTTOM */
		if (inArray(new char[]{'0','2','3','5','6','8','C','E','G','J','L','O','Q','S','U','W','Z',']','[','(',')'}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, 180, 0,  (+1 * gridY) - (segmentWidth / 2));
		}
		
		/* LEFT POINTED BOTTOM */
		if (inArray(new char[]{'B','D'}, c))
		{
			addPolygonSegment(RIGHT_POINT_OUTTER_SEGMENT, 180, 0,  (+1 * gridY) - (segmentWidth / 2));
		}
		
		/* STANDARD TOP LEFT */
		if (inArray(new char[]{'0','4','5','6','8','9','A','C','E','F','G','H','K','L','M','N','O','P','Q','R','S','U','V','W','Y','[','('}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, -90, (-1 * gridX) + (segmentWidth / 2) , (-1 * gridY) + (segmentLength / 2));
		}
		
		/* TOP POINTED TOP LEFT */
		if (inArray(new char[]{'B','D'}, c))
		{
			addPolygonSegment(RIGHT_POINT_OUTTER_SEGMENT, -90, (-1 * gridX) + (segmentWidth / 2) , (-1 * gridY) + (segmentLength / 2));
		}
		
		/* Extended top right for the Q */
		if (inArray(new char[]{'Q'}, c))
		{
			addPolygonSegment(EXTENDED_OUTTER_SEGMENT, +90, (+1 * gridX) - (segmentWidth / 2) , (-1 * gridY) + (segmentLength / 2) + (segmentWidth / 4));
		}
		
		/* STANDARD TOP RIGHT */
		if (inArray(new char[]{'0','2','3','4','7','8','9','A','B','D','H','J','M','N','O','P','R','U','V','W','Y',']',')'}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, +90, (+1 * gridX) - (segmentWidth / 2) , (-1 * gridY) + (segmentLength / 2));
		}
		
		/* EXTTENDED LEFT ANGLE UPPER RIGHT LEG */
		if (inArray(new char[]{'K'}, c))
		{
			addPolygonSegment(EXTENDED_OUTTER_SEGMENT, -45, (segmentLength / 4) , (-1 * gridY) + (segmentLength / 2));
		}

		
		/* BOTTOM POINTED BOTTOM LEFT */
		if (inArray(new char[]{'B','D'}, c))
		{
			addPolygonSegment(LEFT_POINT_OUTTER_SEGMENT, -90, (-1 * gridX) + (segmentWidth / 2) , (+1 * gridY) - (segmentLength / 2));
		}
		
		/* STANDARD BOTTOM LEFT */
		if (inArray(new char[]{'0','2','6','8','A','C','E','F','G','H','J', 'K','L','M','N','O','P','Q','R','U','W','[','('}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, -90, (-1 * gridX) + (segmentWidth / 2) , (+1 * gridY) - (segmentLength / 2));
		}
		
		/* STANDARD BOTTOM RIGHT */
		if (inArray(new char[]{'0','3','4','5','6','7','8','9','A','B','D','G','H','J','K','M','N','O','S','U','W',']',')'}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, +90, (+1 * gridX) - (segmentWidth / 2) , (+1 * gridY) - (segmentLength / 2));
		}

		
		/* Upper right angle of X and Z */
		if (inArray(new char[]{'X','Z','<'}, c))
		{
			addPolygonSegment(RIGHT_ANGLE_SEGMENT, 0, (segmentLength / 4) , (-1 * gridY) + (segmentLength / 2) + (segmentWidth / 8));
		}
		
		/* Lower right angle of X and Z */
		if (inArray(new char[]{'X','Q','<'}, c))
		{
			addPolygonSegment(LEFT_ANGLE_SEGMENT, 0, (segmentLength / 4) , (gridY) - (segmentLength / 2) - (segmentWidth / 8));
		}
		
		/* Upper left angle of X */
		if (inArray(new char[]{'X','>'}, c))
		{
			addPolygonSegment(LEFT_ANGLE_SEGMENT, 0, -1 * (segmentLength / 4) , (-1 * gridY) + (segmentLength / 2) + (segmentWidth / 8));
		}
		
		/* Lower left angle of X */
		if (inArray(new char[]{'X','Z','>'}, c))
		{
			addPolygonSegment(RIGHT_ANGLE_SEGMENT, 0, -1 * (segmentLength / 4) , (gridY) - (segmentLength / 2) - (segmentWidth / 8));
		}
		
		/* Angle of N */
		if ('N' == c)
		{
			addPolygonSegment(LEFT_ANGLE_SEGMENT, 0, 0, 0);
		}
		
		/* bottom V segments */
		if ('V' == c)
		{
			addPolygonSegment(RIGHT_ANGLE_SEGMENT, 0, (segmentLength / 4) , (gridY) - (segmentLength / 2) - (segmentWidth / 8));
			addPolygonSegment(LEFT_ANGLE_SEGMENT, 0, -1 * (segmentLength / 4) , (gridY) - (segmentLength / 2) - (segmentWidth / 8));
		}
				
		/* STANDARD MIDDLE */
		if (inArray(new char[]{'2','3','4','5','6','8','9','A','B','E','F','H','K','P','R','S','Y'}, c))
		{
			addPolygonSegment(MIDDLE_SEGMENT, 0,   0 , 0);
		}
		
		/* SHORT MIDDLE RIGHT OF CENTER */
		if (inArray(new char[]{'G'}, c))
		{
			addPolygonSegment(HALF_MIDDLE_SEGMENT, 0,   (segmentLength / 6) , 0);
		}
		
		/* UPPER HALF VERTICAL IN CENTER*/
		if (inArray(new char[]{'1','I'}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, +90, 0 , (-1 * gridY) + (segmentLength / 2));
		}
		
		/* UPPER HALF VERTICAL IN CENTER WITH FLAT TOP */
		if (inArray(new char[]{'M','T'}, c))
		{
			addPolygonSegment(LEFT_FLAT_OUTTER_SEGMENT, +90, 0 , (-1 * gridY) + (segmentLength / 2));
		}

		/* LOWER HALF VERTICAL IN CENTER*/
		if (inArray(new char[]{'1','I','T'}, c))
		{
			addPolygonSegment(OUTTER_SEGMENT, +90, 0 , (+1 * gridY) - (segmentLength / 2));
		}
		
		/* LOWER HALF VERTICAL IN CENTER WITH FLAT TOP */
		if (inArray(new char[]{'Y'}, c))
		{
			addPolygonSegment(LEFT_FLAT_OUTTER_SEGMENT, +90, 0 , (+1 * gridY) - (segmentLength / 2));
		}
		
		/* Lower HALF VERTICLE IN CENTER WITH FLAT BOTTOM */
		if (inArray(new char[]{'W'}, c))
		{
			addPolygonSegment(RIGHT_FLAT_OUTTER_SEGMENT, +90, 0 , (+1 * gridY) - (segmentLength / 2));
		}
		
		/* Make sure there is Something in the segment array.  If not, then an unsupported character
		 * has been asked for.  But.. watch for the ' ' character.  It obviously has no segments */
		if ((c != ' ') && (this.segments_.size() == 0))
		{
			throw new RuntimeException("Unable to make a glyph for the character \'" + c + "\'.  It does not appear to be supported");
		}
		
		
		/* Now, generate the glyph shapes */
		this.glyphShapes_ = generateGlyphShapes(charWidth, charHeight);
	}
	
	
	/*******************************************************
	 * Returns a true if character c is in the array.
	 * @param array
	 * @param c
	 * @return
	 *******************************************************/
	private boolean inArray(char[] array, char c)
	{
		for (char cc : array)
		{
			if (cc == c)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/********************************************************
	 * This method will create a translated set of point 
	 * coords that are in an 14 x 4 sized grid into a 2 x 1 grid.
	 * The grid is 14 wide, but it's center is at 7. That is
	 * the actual coords of each 4 corner is
	 * (-7,-2) to (+7,+2).  The actual numbers
	 * are controlled by the constants DESIGN_GRID_n and FINAL_GRID_n 
	 * @param x
	 * @param y
	 * @return
	 *******************************************************/
	private static Point2D createPoint(double x, double y)
	{
	
		if ((x < -1 * (DESIGN_WIDTH / 2) * CELL_SIZE) || (x > (DESIGN_WIDTH / 2) * CELL_SIZE))
		{
			throw new RuntimeException("Attempt to make glyph with an X point outside the range of (" + -1 * (DESIGN_WIDTH / 2) + "," + (DESIGN_HEIGHT / 2) + "): " + x);
		}
		
		if ((y < -1 * (DESIGN_HEIGHT / 2) * CELL_SIZE) || (y > (DESIGN_HEIGHT / 2) * CELL_SIZE))
		{
			throw new RuntimeException("Attempt to make glyph with a Y point outside the range of (" + -1 * (DESIGN_HEIGHT / 2) + "," + (DESIGN_HEIGHT / 2) + "): " + y);
		}
		
		return new Point2D.Double(x,y);
		
	}
	
	
	/********************************************************
	 * Create a polygon segment
	 * 
	 * @param points IN -the points that make up the polygon.
	 * @param degrees IN - this will be converted to radians.
	 * @param x IN - the translation amount along the X axis.
	 * @param y IN - the translation amount along the Y axis.
	 *******************************************************/
	private void addPolygonSegment(ArrayList<Point2D> points, double degrees, double x, double y)
	{
		Segment seg = new Segment(Math.toRadians(degrees), (int)x, (int)y);
		seg.setupPolygon(points);
		this.segments_.add(seg);
	}
	
	
	/*******************************************************
	 * Create an ellipse segment
	 * 
	 * @param width IN - the width of the ellipse.
	 * @param height IN - the height of the ellipse.
	 * @param degrees IN - the rotation of the ellipse.
	 * @param x IN - the translation amount along the X axis.
	 * @param y IN - the translation amount along the Y axis.
	 *******************************************************/
	private void addEllipseSegment(double width, double height, double degrees, double x, double y)
	{
		Segment seg = new Segment(Math.toRadians(degrees), (int)x, (int)y);
		seg.setupEllipse(width, height);
		this.segments_.add(seg);
	}
	
	
	/*******************************************************
	 * Override
	 * @see net.sourceforge.JDash.gui.shapes.txt.Glyph#getGlyphShapes()
	 *******************************************************/
	public List<Shape> getGlyphShapes()
	{
		return Collections.unmodifiableList(this.glyphShapes_);
	}
	
	/********************************************************
	 * return each polygon making up this character.  The x and y
	 * values represent the desired upper left corner of the resulting
	 * character.
	 * 
	 * @param charWidth IN - the desired character width in pixels.
	 * @param charHeight IN - the desired character height in pixels.
	 * @return
	 *******************************************************/
	private List<Shape> generateGlyphShapes(int charWidth, int charHeight)
	{
		

		/* The list of transformed segments returned */
		ArrayList<Shape> xformedSegements = new ArrayList<Shape>();

		/* The scale transform is to adjust the design size in pixels into the requested size in pixels */
		double xScale = charWidth  / (DESIGN_WIDTH * CELL_SIZE);
		double yScale = charHeight / (DESIGN_HEIGHT * CELL_SIZE);
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(xScale, yScale);
		
		/* Setup the translate transform */
		double xTrans = ((DESIGN_WIDTH * CELL_SIZE) / 2);
		double yTrans = ((DESIGN_HEIGHT * CELL_SIZE) / 2);
		AffineTransform moveTransform = AffineTransform.getTranslateInstance(xTrans, yTrans);


		/* Get each segments shape */
		for (Segment segment : this.segments_)
		{
			Shape segmentShape = segment.createSegment();
	
			segmentShape = moveTransform.createTransformedShape(segmentShape);
			segmentShape = scaleTransform.createTransformedShape(segmentShape);
			xformedSegements.add(segmentShape);
		}
		
		
		return xformedSegements;
		
	}

	
	
	/*******************************************************
	 * This class simply describes the segment, it's rotation
	 * and it's position within the original design grid.  Once
	 * a segment is created in this class, it's Shape can
	 * be retreived. The shape returned by each call to
	 * createShape is a new instance of this Segment.  Changes
	 * made to that shape will NOT affect the segment itself.
	 * NOte also, that this class was designed to hold
	 * a polygon, but there are different constructors
	 * for different shape types.  Like an ellipse.
	 ******************************************************/
	private static class Segment
	{
		
		private ArrayList<Point2D> points_ = null;
		private Ellipse2D elipse_ = null;
		
		private double radians_ = 0;
		private double x_ = 0;
		private double y_ = 0;
		
		/*******************************************************
		 * Create the segment with the given points, 
		 * Then rotate the segment by the radians given, and finaly
		 * translate the segment by x and y from the origin.
		 * @param segmentPoints
		 * @param radians IN - the degrees to rotate this segment
		 * @param x IN - after rotation, translate this value.
		 * param y IN - after rotation, trnaslate this value.
		 ******************************************************/
		public Segment(double radians, int x, int y)
		{
			this.radians_ = radians;
			this.x_ = x;
			this.y_ = y;
		}
		
		
		/********************************************************
		 * Setup this segment as a polygon
		 * 
		 * @param polygonPoints
		 *******************************************************/
		public void setupPolygon(ArrayList<Point2D> polygonPoints)
		{
			this.points_ = polygonPoints;
		}
		
		/********************************************************
		 * Setup this segment as an ellipse.
		 * 
		 * @param x
		 * @param y
		 * @param width
		 * @param height
		 *******************************************************/
		public void setupEllipse(double width, double height)
		{
			this.elipse_ = new Ellipse2D.Double(-1* (width / 2), -1 * (height / 2), width, height);
		}
		
		/********************************************************
		 * @return
		 *******************************************************/
		public Shape createSegment() 
		{
			
			// TODO
//			System.out.println("Creating Shape from scratch!  Where is the cache?");
			
			/* Create the shape */
			Shape shape =  null;
			
			/* Polygon? */
			if (this.points_ != null)
			{
				shape = createPolygon();
			}
			else if (this.elipse_ != null)
			{
				shape = this.elipse_;
			}
			else
			{
				throw new RuntimeException("this segment has not been correctly setup.  None of the setupXXXX() methods have been called");
			}
			
			/* Now, rotate the shape */
			AffineTransform rotate = AffineTransform.getRotateInstance(this.radians_);
			shape = rotate.createTransformedShape(shape);
			
			/* Finally translate the shape */
			AffineTransform translate = AffineTransform.getTranslateInstance(this.x_, this.y_);
			shape = translate.createTransformedShape(shape);

			/* If any of the shapes points lie outside the design grid, then we have
			 * a setup problem */
			int width = (int)(DESIGN_WIDTH * CELL_SIZE);
			int height = (int)(DESIGN_HEIGHT * CELL_SIZE);
			int x = -1 * (int)((width / 2) * CELL_SIZE);
			int y = -1 * (int)((height / 2) * CELL_SIZE);
			
			
			Rectangle shapeBounds = shape.getBounds();
			if (
					(shapeBounds.x < x) || (shapeBounds.width > width) ||
					(shapeBounds.y < y) || (shapeBounds.height > height)
				)
			{
				throw new RuntimeException ("Segment is outside design grid. " +
									" Design Grid [" + x + "," + y + "/" + width + "," + height + "]" +
									"  Segment [" + shapeBounds.x + "," + shapeBounds.y + "/" + shapeBounds.width + "," + shapeBounds.height + "]" +
									" Translate[" + this.x_ + "," + this.y_ + "]"); 
			}
			
			
			/* done, return it */
			return shape;
		}
		
		
		/********************************************************
		 * @return
		 *******************************************************/
		private Shape createPolygon()
		{
			Polygon polygon = new Polygon();
			
			/* for each point in this segment */
			for (Point2D p : this.points_)
			{
				/* Add the point to the pollygon */
				polygon.addPoint((int)p.getX(), (int)p.getY());
			}
			
			return polygon;
		}
	}
	
}
