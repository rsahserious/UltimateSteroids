package com.serious.ultimatesteroids;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Ship extends Sprite
{
	public static final short NO_ROTATION = 0;
	public static final short ROTATE_LEFT = 1;
	public static final short ROTATE_RIGHT = 2;
	
	public static final float MAX_VELOCITY = 10;
	public static final float ACCELERATION_OFFSET = 0.1f;
	public static final float ROTATION_SPEED = 0.1f;
	
	public float sizeX;
	public float sizeY;
	public float angle;
	public float rotation;
	
	public boolean engineSoundPlaying = false;
	public boolean accelerates = false;
	
	public Ship(float pX, float pY, TextureRegion pTextureRegion, VertexBufferObjectManager vertexBufferObjectManager)
	{
		super(pX, pY, pTextureRegion, vertexBufferObjectManager);

		sizeX = pTextureRegion.getWidth();
		sizeY = pTextureRegion.getHeight();
	}
	
	public float getCenterX()
	{
		return getX() + (sizeX / 2);
	}
	
	public float getCenterY()
	{
		return getY() + (sizeY / 2);
	}
}
