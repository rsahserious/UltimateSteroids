package com.serious.ultimatesteroids;


import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.util.Log;

public class Projectile extends Sprite
{
	private long fireTime;
	
	public Projectile(float pX, float pY, TextureRegion pTextureRegion, VertexBufferObjectManager vertexBufferObjectManager)
	{
		super(pX, pY, pTextureRegion, vertexBufferObjectManager);

		fireTime = System.currentTimeMillis();
	}
	
	public boolean isOutOfScreen()
	{
		final float pX = getX();
		final float pY = getY();
		
		if(pX > SteroidsMain.CAMERA_WIDTH + 100
			|| pX < -100
			|| pY > SteroidsMain.CAMERA_HEIGHT + 100
			|| pY < -100)
			return true;

		return false;
	}
	
	public float getEnergy()
	{
		float timeElapsed = (float) (System.currentTimeMillis() - fireTime) / 2000;

		if(timeElapsed <= 0)
			return 0;

		return (1 - timeElapsed);
	}
}