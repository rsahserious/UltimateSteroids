package com.serious.ultimatesteroids;

import java.util.Random;

public class Utils
{
	public static boolean isPointInCircleArea(float pX, float pY, float cX, float cY, float radius)
	{
		if(Math.sqrt(Math.pow((double) (cX - pX), 2.0) + Math.pow((double) (cY - pY), 2)) <= radius)
			return true;

		return false;
	}
	
	public static boolean isPointInArea(float pX, float pY, float aX, float aY, float width, float height)
	{
		if(pX > aX && pX < width && pY > aY && pY < height)
			return true;
	
		return false;
	}
}
