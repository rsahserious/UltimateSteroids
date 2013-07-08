package com.serious.ultimatesteroids;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;

public class Rock extends Sprite
{
	public static final float BODY_SIZE_LARGE = 1.0f;
	public static final float BODY_SIZE_MEDIUM = 0.65f;
	public static final float BODY_SIZE_SMALL = 0.3f;
	
	public static final short SPAWN_TYPE_RANDOM = 0;
	public static final short SPAWN_TYPE_FROM_PARENT = 1;
	
	public float sizeX;
	public float sizeY;
	public float teleportOffset;
	public float bodySize;
	public float life;
	
	public Rock(float bodySize, float pX, float pY, TextureRegion pTextureRegion, VertexBufferObjectManager vertexBufferObjectManager)
	{
		super(pX, pY, pTextureRegion, vertexBufferObjectManager);

		this.bodySize = bodySize;
		sizeX = pTextureRegion.getWidth();
		sizeY = pTextureRegion.getHeight();
		teleportOffset = ((((sizeX + sizeY) / 2) / 2) * bodySize) + 10.0f;
		life = 1.0f;
	}
	
	public float getCenterX()
	{
		return getX() + (sizeX / 2);
	}
	
	public float getCenterY()
	{
		return getY() + (sizeY / 2);
	}
	
	public void setCenterX(float pX)
	{
		setX(pX - (sizeX / 2));
		Log.d("srs", "setCenterX: " + pX);
	}
	
	public void setCenterY(float pY)
	{
		setY(pY - (sizeY / 2));
		Log.d("srs", "setCenterY: " + pY);
	}

	public void giveDamage(float dmg)
	{
		life -= dmg / bodySize;
	}
}
