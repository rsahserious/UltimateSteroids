package com.serious.ultimatesteroids;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.initializer.RotationParticleInitializer;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ColorParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.RotationParticleModifier;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

public class SteroidsMain extends SimpleBaseGameActivity implements IOnSceneTouchListener
{
	// ====================================================
	// CONSTANTS
	// ====================================================
	
	public static final int CAMERA_WIDTH = 1280;
	public static final int CAMERA_HEIGHT = 720;
	
	private static final int BUTTON_WIDTH = 174;
	private static final int BUTTON_HEIGHT = 173;
	
	private static final int TELEPORT_OFFSET = 40;
	private static final double PROJECTILE_VELOCITY = 10;
	private static final int PROJECTILE_INTERVAL = 350;
	private static final int MAX_ROCKS = 2;
	
	private static final FixtureDef FIXTURE_DEF_SHIP = 			PhysicsFactory.createFixtureDef(2, 0.5f, 0, false, (short)1, (short)(1+2+4), 	(short)0);
	private static final FixtureDef FIXTURE_DEF_ROCK = 			PhysicsFactory.createFixtureDef(1, 0.5f, 0, false, (short)2, (short)(1+4), 		(short)0);
	private static final FixtureDef FIXTURE_DEF_PROJECTILE = 	PhysicsFactory.createFixtureDef(1, 0.5f, 0, false, (short)4, (short)2, 			(short)0);

	
	// ====================================================
	// TEXTURES
	// ====================================================
	
	private BitmapTextureAtlas mFirstBitmapTextureAtlas;
	private BitmapTextureAtlas mSecondBitmapTextureAtlas;
	private BitmapTextureAtlas mBackgroundBitmapTextureAtlas;
	
	/* Background */
	private TextureRegion mBackgroundTextureRegion;
	
	/* Ship */
	private TextureRegion mShipTextureRegion;
	private TextureRegion mButtonRotateLeftTextureRegion;
	private TextureRegion mButtonRotateRightTextureRegion;
	private TextureRegion mButtonAccelerateTextureRegion;
	
	/* Rocks */
	private TextureRegion[] mRockTextureRegion = new TextureRegion[MAX_ROCKS];
	private static final String[] szRockTextures = {
		"rock_2.png", 
		"rock_2.png"
	};
	
	/* Projectiles */
	private TextureRegion mProjectileTextureRegion;
	
	/* Particles */
	private TextureRegion mParticleShipFireTextureRegion;
	
	
	// ====================================================
	// SOUNDS
	// ====================================================
	
	private Sound mShipEngineSound;
	private Sound mProjectileFireSound;
	private Sound mRockHitSound;
	
	
	// ====================================================
	// SPRITES
	// ====================================================
	
	private Ship mShip;
	private ButtonSprite mRotateLeftButton;
	private ButtonSprite mRotateRightButton;
	private ButtonSprite mAccelerateButton;
	
	
	// ====================================================
	// PARTICLES
	// ====================================================
	
	private PointParticleEmitter shipFireParticleEmitter;
	private SpriteParticleSystem shipFireParticleSystem;
	private VelocityParticleInitializer<Sprite> shipFireParticleVelocity;
	private AccelerationParticleInitializer<Sprite> shipFireParticleAcceleration;
	
	
	// ====================================================
	// OTHER
	// ====================================================
	
	private Scene mMainScene;
	
	private PhysicsWorld mPhysicsWorld;
	private Body mShipBody;

	List<Projectile> mProjectile = new ArrayList<Projectile>();
	List<Rock> mRock = new ArrayList<Rock>();
	List<SpriteParticleSystem> mProjectileParticleSystem = new ArrayList<SpriteParticleSystem>();

	long lastProjectileFire;
	
	boolean mShooting = false;
	
	List<RockNextToSpawn> 	nextToSpawnRock 			= new ArrayList<RockNextToSpawn>();
	List<Projectile> 		nextToSpawnProjectile 		= new ArrayList<Projectile>();
	List<Rock> 				nextToDestroyRock 			= new ArrayList<Rock>();
	List<Projectile> 		nextToDestroyProjectile 	= new ArrayList<Projectile>();

	@Override
	public EngineOptions onCreateEngineOptions()
	{
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		engineOptions.getAudioOptions().setNeedsSound(true);
		
		return engineOptions;
	}

	@Override
	public void onCreateResources()
	{
		// ====================================================
		// TEXTURES
		// ====================================================
		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mFirstBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mSecondBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mBackgroundBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), CAMERA_WIDTH, CAMERA_HEIGHT);
		
		
		/* Background */
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundBitmapTextureAtlas, this, "background.png", 0, 0);

		/* The ship */
		this.mShipTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFirstBitmapTextureAtlas, this, "ship.png", 0, 0);
		
		/* Rocks */
		for(int i = 0; i < MAX_ROCKS; i++)
			this.mRockTextureRegion[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mSecondBitmapTextureAtlas, this, szRockTextures[i], i * 253, 0);
		
		/* Control buttons */
		this.mButtonRotateLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFirstBitmapTextureAtlas, this, "button_rotate_left.png", BUTTON_WIDTH, 0);
		this.mButtonRotateRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFirstBitmapTextureAtlas, this, "button_rotate_right.png", BUTTON_WIDTH * 2, 0);
		this.mButtonAccelerateTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFirstBitmapTextureAtlas, this, "button_accelerate.png", BUTTON_WIDTH * 3, 0);
		
		/* Projectiles */
		this.mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFirstBitmapTextureAtlas, this, "projectile.png", 0, BUTTON_HEIGHT);
		
		/* Particles */
		this.mParticleShipFireTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mFirstBitmapTextureAtlas, this, "particle_fire.png", BUTTON_WIDTH * 4, 0);
		
		this.mFirstBitmapTextureAtlas.load();
		this.mSecondBitmapTextureAtlas.load();
		this.mBackgroundBitmapTextureAtlas.load();
		
		
		// ====================================================
		// SOUNDS
		// ====================================================
		
		SoundFactory.setAssetBasePath("mfx/");
		try
		{
			this.mShipEngineSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "ship_engine.ogg");
			this.mProjectileFireSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "projectile_fire.ogg");
			this.mRockHitSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "rock_hit.ogg");
		} 
		catch (final IOException e) 
		{
			Debug.e(e);
		}
		
		mShipEngineSound.setLooping(true);
	}

	@Override
	public Scene onCreateScene()
	{
		this.mEngine.registerUpdateHandler(new FPSLogger());

		mMainScene = new Scene();

		/* Setting the background (earth) */
		Sprite background = new Sprite(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, this.mBackgroundTextureRegion, this.getVertexBufferObjectManager());
		background.setAlpha(0.3f);
		mMainScene.setBackground(new SpriteBackground(background));
		
		/* Other scene settings */
		mMainScene.setTouchAreaBindingOnActionDownEnabled(true);
		mMainScene.setOnSceneTouchListener(this);
		
		/* The ship */
		float shipX = (CAMERA_WIDTH - this.mShipTextureRegion.getWidth()) / 2;
		float shipY = (CAMERA_HEIGHT - this.mShipTextureRegion.getHeight()) / 2;
		
        mShip = new Ship(shipX, shipY, this.mShipTextureRegion, this.getVertexBufferObjectManager());
        mMainScene.attachChild(mShip);
        
        /* Create all buttons which will control the ship */
        createControlButtons();

        /* Register the areas of buttons to the scene so it can detect that you're touching it */
        mMainScene.registerTouchArea(mRotateLeftButton);
        mMainScene.registerTouchArea(mRotateRightButton);
        mMainScene.registerTouchArea(mAccelerateButton);
        
        /* Set the opacity of every single button to the default (untouched) value */
        mRotateLeftButton.setAlpha(0.28f);
        mRotateRightButton.setAlpha(0.28f);
        mAccelerateButton.setAlpha(0.28f);
        
        /* Attach created buttons to our scene */
        mMainScene.attachChild(mRotateLeftButton);
        mMainScene.attachChild(mRotateRightButton);
        mMainScene.attachChild(mAccelerateButton);
        
        /* The main scene update function */
        mMainScene.registerUpdateHandler(new IUpdateHandler()
		{
			@Override
			public void reset() { }
			
			@Override
			public void onUpdate(float pSecondsElapsed)
			{
				onSceneUpdate();
			}
		});
        
        
        /* Particle system */
        final float MIN_RATE = 60.0f;
        final float MAX_RATE = 80.0f;
        final int PARTICLES = 150;

        shipFireParticleEmitter = new PointParticleEmitter(mShip.getCenterX(), mShip.getCenterY());
        shipFireParticleSystem = new SpriteParticleSystem(0, 0, shipFireParticleEmitter, MIN_RATE, MAX_RATE, PARTICLES, this.mParticleShipFireTextureRegion, this.getVertexBufferObjectManager());

        shipFireParticleSystem.addParticleInitializer(shipFireParticleVelocity = new VelocityParticleInitializer<Sprite>(0, 0, 0, 0));
        shipFireParticleSystem.addParticleInitializer(shipFireParticleAcceleration = new AccelerationParticleInitializer<Sprite>(0, 0));
        shipFireParticleSystem.addParticleInitializer(new RotationParticleInitializer<Sprite>(0.0f, 360.0f));
        shipFireParticleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(2.0f));
        
		shipFireParticleSystem.addParticleModifier(new ScaleParticleModifier<Sprite>(0, 1.5f, 0.7f, 6.0f));
        shipFireParticleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0.0f, 0.3f, 0, 1.0f));
        shipFireParticleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0.5f, 1.0f, 1.0f, 0));
        shipFireParticleSystem.addParticleModifier(new RotationParticleModifier<Sprite>(0, 1.0f, 0, 360.0f));
        shipFireParticleSystem.addParticleModifier(new ColorParticleModifier<Sprite>(0, 1.0f, 	0.4f, 1.0f, 	0.8f, 0.4f, 	1.0f, 0));

        shipFireParticleSystem.registerUpdateHandler(new IUpdateHandler()
        {
        	@Override
			public void reset() { }
			
			@Override
			public void onUpdate(float pSecondsElapsed)
			{
				final float SPREAD = 0.5f;
				final float OFFSET = 25.0f;
				
				final float pEmitterX = mShip.getCenterX() + (float) (Math.sin(mShip.angle + Math.PI) * OFFSET) - 7.0f;
				final float pEmitterY = mShip.getCenterY() + (float) -(Math.cos(mShip.angle + Math.PI) * OFFSET) - 10.0f;
				
				Vector2 velocity = mShipBody.getLinearVelocity();
				
				final float pMinVelocityX = (float) (Math.sin(mShip.angle + Math.PI - SPREAD) * 120.0) + velocity.x * PIXEL_TO_METER_RATIO_DEFAULT;
				final float pMaxVelocityX = (float) (Math.sin(mShip.angle + Math.PI + SPREAD) * 120.0) + velocity.x * PIXEL_TO_METER_RATIO_DEFAULT;
				final float pMinVelocityY = (float) -(Math.cos(mShip.angle + Math.PI - SPREAD) * 120.0) + velocity.y * PIXEL_TO_METER_RATIO_DEFAULT;
				final float pMaxVelocityY = (float) -(Math.cos(mShip.angle + Math.PI + SPREAD) * 120.0) + velocity.y * PIXEL_TO_METER_RATIO_DEFAULT;
				
				final float pAccelerationX = (float) (Math.sin(mShip.angle + Math.PI) * -60.0);
				final float pAccelerationY = (float) -(Math.cos(mShip.angle + Math.PI) * -60.0);

				shipFireParticleEmitter.setCenter(pEmitterX, pEmitterY);
				shipFireParticleVelocity.setVelocity(pMinVelocityX, pMaxVelocityX, pMinVelocityY, pMaxVelocityY);
				shipFireParticleAcceleration.setAccelerationX(pAccelerationX);
				shipFireParticleAcceleration.setAccelerationX(pAccelerationY);
			}
        });
        
        shipFireParticleSystem.setParticlesSpawnEnabled(false);
        
        mMainScene.attachChild(shipFireParticleSystem);
        

        /* Physics */
        this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
        
        {
        	Vector2[] vertices_L = {
        			new Vector2(0.0f, -37.0f / PIXEL_TO_METER_RATIO_DEFAULT),
    	        	new Vector2(39.0f / PIXEL_TO_METER_RATIO_DEFAULT, 34.0f / PIXEL_TO_METER_RATIO_DEFAULT),
    	        	new Vector2(0.0f / PIXEL_TO_METER_RATIO_DEFAULT, 25.0f / PIXEL_TO_METER_RATIO_DEFAULT),
    	        	new Vector2(-40.0f / PIXEL_TO_METER_RATIO_DEFAULT, 34.0f / PIXEL_TO_METER_RATIO_DEFAULT)
        	};
        	Vector2[] vertices_R = {
    	       	new Vector2(0 / PIXEL_TO_METER_RATIO_DEFAULT, -35 / PIXEL_TO_METER_RATIO_DEFAULT),
	        	new Vector2(0 / PIXEL_TO_METER_RATIO_DEFAULT, 25 / PIXEL_TO_METER_RATIO_DEFAULT),
	        	new Vector2(38 / PIXEL_TO_METER_RATIO_DEFAULT, 34 / PIXEL_TO_METER_RATIO_DEFAULT)
        	};
	        
        	mShipBody = PhysicsFactory.createPolygonBody(this.mPhysicsWorld, mShip, vertices_L, BodyType.DynamicBody, FIXTURE_DEF_SHIP);

	       	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mShip, mShipBody, true, true));
	       	
	       	mShipBody.setFixedRotation(true);
	       	mShipBody.setUserData(mShip);
        }
        
        this.mPhysicsWorld.setContactListener(new ContactListener() 
        {
			@Override
			public void beginContact(Contact contact)
			{
				Object objectA = contact.getFixtureA().getBody().getUserData();
				Object objectB = contact.getFixtureB().getBody().getUserData();

				/* A projectile meets a rock */
				if((objectA != null && objectB != null)
					&& (((objectA.getClass() == Rock.class) && (objectB.getClass() == Projectile.class)) 
					|| ((objectB.getClass() == Rock.class) && (objectA.getClass() == Projectile.class))))
                {
					Rock rock = (objectA.getClass() == Rock.class) ? (Rock) objectA : (Rock) objectB;
					Projectile projectile = (objectB.getClass() == Projectile.class) ? (Projectile) objectB : (Projectile) objectA;
					
					Random rand = new Random();
					final float energy = projectile.getEnergy();
					
					rock.giveDamage(energy / 2);
					if(rock.life <= 0)
					{
						destroyRock(rock);
					}
					
					emitProjectileParticles(projectile, contact.getFixtureA().getBody(), energy);
					
					if(energy > 0)
					{
						mRockHitSound.setVolume((energy + 0.15f) > 1.0f ? 1.0f : (energy + 0.15f)); // The volume is proportional to the projectile's energy (+0.15 offset)
				       	mRockHitSound.setRate((rand.nextFloat() * 0.4f) + 0.8f); // Let's change the rate (speed) of this sound with a little +/- offset
				       	mRockHitSound.play();
					}
					
					destroyProjectile(projectile);
                }
			}

			@Override
			public void endContact(Contact contact) { }

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) { }

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) { }
        });
        

        /* Rocks */
        for(int i = 0; i < 3; i++)
        	spawnRandomRock(Rock.SPAWN_TYPE_RANDOM, Rock.BODY_SIZE_LARGE, 0, 0);

        
        this.mMainScene.registerUpdateHandler(this.mPhysicsWorld);
        
		return mMainScene;
	}
	
	private void createControlButtons()
	{
		/* Button that changes the rotation of the ship into left */
        mRotateLeftButton = new ButtonSprite(80, 510, this.mButtonRotateLeftTextureRegion, this.getVertexBufferObjectManager())
        {
            @Override
            public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
            {
                if(pTouchEvent.isActionDown())
                {
                    this.setAlpha(0.5f);
                    mShip.rotation = Ship.ROTATE_LEFT;
                    
                    //spawnRandomRock(Rock.SPAWN_TYPE_FROM_PARENT, Rock.BODY_SIZE_SMALL, mShip.getX() - 90, mShip.getY()- 90);
                }
                else if(pTouchEvent.isActionUp())
                {
                	this.setAlpha(0.28f);
                	mShip.rotation = Ship.NO_ROTATION;
                }
                
                //mShooting = false;
                
                return super.onAreaTouched(pTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        
        /* Button that changes the rotation of the ship into right */
        mRotateRightButton = new ButtonSprite(270, 510, this.mButtonRotateRightTextureRegion, this.getVertexBufferObjectManager())
        {
            @Override
            public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
            {
            	if(pTouchEvent.isActionDown())
                {
            		this.setAlpha(0.5f);
            		mShip.rotation = Ship.ROTATE_RIGHT;
	            }
	            else if(pTouchEvent.isActionUp())
	            {
            		this.setAlpha(0.28f);
            		mShip.rotation = Ship.NO_ROTATION;
                }

            	//mShooting = false;
            	
                return super.onAreaTouched(pTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
        
        /* Button that accelerates the ship forwards */
        mAccelerateButton = new ButtonSprite(1050, 510, this.mButtonAccelerateTextureRegion, this.getVertexBufferObjectManager())
        {
            @Override
            public boolean onAreaTouched(TouchEvent pTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
            {
            	if(pTouchEvent.isActionDown())
                {
                    this.setAlpha(0.5f);
                    accelerateShip(true);
                }
                else if(pTouchEvent.isActionUp())
                {
                	this.setAlpha(0.28f);
                	accelerateShip(false);
                }
            	
            	mShooting = false;
                
                return super.onAreaTouched(pTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
            }
        };
	}
	
	private void onSceneUpdate()
	{
		/* Create new waiting objects */
		for(int i = 0; i < nextToSpawnRock.size(); i++)
        {
        	RockNextToSpawn nextRock = nextToSpawnRock.get(i);
        	spawnRandomRock(Rock.SPAWN_TYPE_FROM_PARENT, nextRock.size, nextRock.pX, nextRock.pY);
        	
        	nextToSpawnRock.remove(i);
        }
		
		for(int i = 0; i < nextToSpawnProjectile.size(); i++)
        {
        	createProjectile();
        	
        	nextToSpawnProjectile.remove(i);
        }
		

		/* Destroy objects which are on their dead list */
        for(int i = 0; i < nextToDestroyRock.size(); i++)
        {
        	Rock rock = nextToDestroyRock.get(i);
        	Body body = (Body) rock.getUserData();
    		
    		PhysicsConnector physicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(rock);
    		mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
    		mPhysicsWorld.destroyBody(body);
    		mMainScene.detachChild(rock);
    		mProjectile.remove(rock);

        	nextToDestroyRock.remove(i);
        }

        for(int i = 0; i < nextToDestroyProjectile.size(); i++)
        {
        	Projectile proj = nextToDestroyProjectile.get(i);
	        Body body = (Body) proj.getUserData();
	        
			PhysicsConnector physicsConnector = this.mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(proj);
			mPhysicsWorld.unregisterPhysicsConnector(physicsConnector);
			mPhysicsWorld.destroyBody(body);
			mMainScene.detachChild(proj);
			mProjectile.remove(proj);
			
			nextToDestroyProjectile.remove(i);
        }

		/* Teleport on edge - Ship */
		{
			float sX = mShip.getCenterX();
			float sY = mShip.getCenterY();

			if(sX < -TELEPORT_OFFSET)
				mShipBody.setTransform((CAMERA_WIDTH + TELEPORT_OFFSET) / PIXEL_TO_METER_RATIO_DEFAULT, sY / PIXEL_TO_METER_RATIO_DEFAULT, mShip.angle);
			else if(sX > CAMERA_WIDTH + TELEPORT_OFFSET)
				mShipBody.setTransform(-TELEPORT_OFFSET / PIXEL_TO_METER_RATIO_DEFAULT, sY / PIXEL_TO_METER_RATIO_DEFAULT, mShip.angle);
			
			else if(sY < -TELEPORT_OFFSET)
				mShipBody.setTransform(sX / PIXEL_TO_METER_RATIO_DEFAULT, (CAMERA_HEIGHT + TELEPORT_OFFSET) / PIXEL_TO_METER_RATIO_DEFAULT, mShip.angle);
			else if(sY > CAMERA_HEIGHT + TELEPORT_OFFSET)
				mShipBody.setTransform(sX / PIXEL_TO_METER_RATIO_DEFAULT, -TELEPORT_OFFSET / PIXEL_TO_METER_RATIO_DEFAULT, mShip.angle);
		}
		
		/* Teleport on edge - Rock */
		for(int i = 0; i < mRock.size(); i++)
		{
			Rock rock = mRock.get(i);
			Body rockBody = (Body) rock.getUserData();
			
			float sX = rock.getCenterX();
			float sY = rock.getCenterY();
			float torque = rockBody.getAngularVelocity();

			if(sX < -rock.teleportOffset)
				rockBody.setTransform((CAMERA_WIDTH + rock.teleportOffset) / PIXEL_TO_METER_RATIO_DEFAULT, sY / PIXEL_TO_METER_RATIO_DEFAULT, rock.getRotation());
			else if(sX > CAMERA_WIDTH + rock.teleportOffset)
				rockBody.setTransform(-rock.teleportOffset / PIXEL_TO_METER_RATIO_DEFAULT, sY / PIXEL_TO_METER_RATIO_DEFAULT, rock.getRotation());
			
			else if(sY < -rock.teleportOffset)
				rockBody.setTransform(sX / PIXEL_TO_METER_RATIO_DEFAULT, (CAMERA_HEIGHT + rock.teleportOffset) / PIXEL_TO_METER_RATIO_DEFAULT, rock.getRotation());
			else if(sY > CAMERA_HEIGHT + rock.teleportOffset)
				rockBody.setTransform(sX / PIXEL_TO_METER_RATIO_DEFAULT, -rock.teleportOffset / PIXEL_TO_METER_RATIO_DEFAULT, rock.getRotation());
			
			rockBody.setAngularVelocity(torque);
		}
		
		/* Ship's engine sound soft start/stop */
		if(mShip.accelerates)
		{
			if(!mShip.engineSoundPlaying)
			{
				mShipEngineSound.play();
				mShip.engineSoundPlaying = true;
			}
			else if(mShipEngineSound.getVolume() < 1.0f)
			{
				mShipEngineSound.setVolume(mShipEngineSound.getVolume() + 0.08f);
			}
		}
		else
		{
			if(mShipEngineSound.getVolume() > 0.1f)
			{
				mShipEngineSound.setVolume(mShipEngineSound.getVolume() - 0.03f);
			}
			else if(mShip.engineSoundPlaying)
			{
				mShipEngineSound.stop();
				mShip.engineSoundPlaying = false;
			}
		}
		
		/* Destroy projectiles when they exceed the screen area */
		for(int i = 0; i < mProjectile.size(); i++)
		{
			Projectile proj = mProjectile.get(i);
			
			if(proj.isOutOfScreen())
			{
				destroyProjectile(proj);
			}
		}
		
		/* Ship's acceleration */
		Vector2 velocity = mShipBody.getLinearVelocity();
        
        if(mShip.accelerates)
        {
            float aX = (float) Math.sin(mShip.angle) * Ship.ACCELERATION_OFFSET;
	        float aY = -(float) Math.cos(mShip.angle) * Ship.ACCELERATION_OFFSET;

	        float vX = velocity.x + aX;
	        float vY = velocity.y + aY;
	        
	        if(vX > Ship.MAX_VELOCITY)	vX = Ship.MAX_VELOCITY;
	        if(vX < -Ship.MAX_VELOCITY)	vX = -Ship.MAX_VELOCITY;
	        if(vY > Ship.MAX_VELOCITY)	vY = Ship.MAX_VELOCITY;
	        if(vY < -Ship.MAX_VELOCITY)	vY = -Ship.MAX_VELOCITY;
	        
	        mShipBody.setLinearVelocity(vX, vY);
        }
        
        /* Ship's rotation */
        if(mShip.rotation == Ship.ROTATE_LEFT)
        {
        	mShip.angle -= Ship.ROTATION_SPEED;
        	mShipBody.setTransform(mShipBody.getPosition(), mShip.angle);
        }
        else if(mShip.rotation == Ship.ROTATE_RIGHT)
        {
        	mShip.angle += Ship.ROTATION_SPEED;
        	mShipBody.setTransform(mShipBody.getPosition(), mShip.angle);
        }
        
        /* Fire the projectile */
        if(mShooting && System.currentTimeMillis() - lastProjectileFire >= PROJECTILE_INTERVAL)
        {
	        fireProjectile();
			lastProjectileFire = System.currentTimeMillis();
        }
        
        /* Projectile particle system - TODO? */ 
        for(int i = 0; i < mProjectileParticleSystem.size(); i++)
        {
        	final SpriteParticleSystem particleSystem = mProjectileParticleSystem.get(i);
        	final long pStartTime = (Integer) particleSystem.getUserData();

        	if((int) (System.currentTimeMillis() / 100) - pStartTime >= 1)
        	{
        		particleSystem.setParticlesSpawnEnabled(false);
        		mProjectileParticleSystem.remove(i);
        	}
        }
	}

	private void destroyProjectile(Projectile proj)
	{
		nextToDestroyProjectile.add(proj);
	}
	
	private void destroyRock(Rock rock)
	{
		d("DESTROY ROCK BEGIN");
		/* Destroy the rock */
		nextToDestroyRock.add(rock);
		
		/* Create smaller rocks - pieces - if it wasn't the smallest */
		if(rock.bodySize > Rock.BODY_SIZE_SMALL)
		{
			float newBodySize = 0;
			
			if(rock.bodySize == Rock.BODY_SIZE_LARGE)
				newBodySize = Rock.BODY_SIZE_MEDIUM;
			else if(rock.bodySize == Rock.BODY_SIZE_MEDIUM)
				newBodySize = Rock.BODY_SIZE_SMALL;

			for(int i = 0; i < 3; i++)
				nextToSpawnRock.add(new RockNextToSpawn(newBodySize, rock.getX(), rock.getY()));
		}
		d("DESTROY ROCK END");
	}

	private void spawnRandomRock(short spawnType, float bodySize, float posX, float posY)
	{
		d("rock start");
		Random rand = new Random();
		int textureId = rand.nextInt(MAX_ROCKS - 1);

		Vector2[] vertices = {
			new Vector2(-21 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, -128 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT),
			new Vector2(74 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, -114 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT),
			new Vector2(118 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, -46 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT),
			new Vector2(71 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, 83 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT),
			new Vector2(-59 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, 131 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT),
			new Vector2(-115 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, 79 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT),
			new Vector2(-106 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT, -44 * bodySize / PIXEL_TO_METER_RATIO_DEFAULT)
        };
		
		float pX = 0;
		float pY = 0;

		if(spawnType == Rock.SPAWN_TYPE_RANDOM)
		{
			pX = rand.nextFloat() * CAMERA_WIDTH;
			pY = rand.nextFloat() * CAMERA_HEIGHT;
		}
		else if(spawnType == Rock.SPAWN_TYPE_FROM_PARENT)
    	{
    		pX = posX;
    		pY = posY;
    	}
		
		Rock rock = new Rock(bodySize, pX, pY, mRockTextureRegion[textureId], this.getVertexBufferObjectManager());
		d("1");
		Body rockBody = PhysicsFactory.createPolygonBody(this.mPhysicsWorld, rock, vertices, BodyType.DynamicBody, FIXTURE_DEF_ROCK);
		d("2");
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rock, rockBody, true, true));
		
    	rock.setScale(bodySize);

    	/* Give it some random movement */
    	if(spawnType == Rock.SPAWN_TYPE_RANDOM)
    	{
	    	rockBody.setLinearVelocity(-50f + (rand.nextFloat() * 100f), -50f + (rand.nextFloat() * 100f));
	    	rockBody.setAngularVelocity(-0.5f + (rand.nextFloat() * 1.0f));
    	}
    	/* Give it an omnidirectional movement from the previous bigger rock */
    	else if(spawnType == Rock.SPAWN_TYPE_FROM_PARENT)
    	{
    		float randomAngle = (float) (rand.nextFloat() * 2 * Math.PI);
    		float vX = (float) Math.sin(randomAngle) * (rand.nextFloat() * 5f);
    		float vY = (float) Math.cos(randomAngle) * (rand.nextFloat() * 5f);

    		rockBody.setLinearVelocity(vX, vY);
    		rockBody.setAngularVelocity(-0.5f + (rand.nextFloat() * 1.0f));
    	}
    	
    	rockBody.setUserData(rock);
    	rock.setUserData(rockBody);

		mRock.add(rock);
		mMainScene.attachChild(rock);
		d("rock stop");
	}

	private void accelerateShip(boolean state)
	{
		if(state)
		{
            mShip.accelerates = true;

            shipFireParticleSystem.setParticlesSpawnEnabled(true);
        }
		else
		{
			mShip.accelerates = false;
			
        	shipFireParticleSystem.setParticlesSpawnEnabled(false);
		}
	}
	
	private void fireProjectile()
	{
		nextToSpawnProjectile.add(null);
	}
	
	private void createProjectile()
	{
		d("start");
		final float OFFSET = 33.0f;
		final float pX = mShip.getCenterX() + (float) (Math.sin(mShip.angle) * OFFSET);
		final float pY = mShip.getCenterY() + (float) (-Math.cos(mShip.angle) * OFFSET);
		
		final Projectile projectile = new Projectile(pX, pY, this.mProjectileTextureRegion, this.getVertexBufferObjectManager());

		projectile.registerEntityModifier(new AlphaModifier(0.2f, 0, 1.0f));
		projectile.registerEntityModifier(new ScaleModifier(0.1f, 0, 1.0f));
		projectile.registerEntityModifier(new AlphaModifier(2, 1, 0));
		projectile.registerEntityModifier(new ScaleModifier(2, 1, 0.5f));

		Vector2 projectileVelocity = new Vector2(
			(float) (Math.sin(mShip.angle) * PROJECTILE_VELOCITY), 
			(float) -(Math.cos(mShip.angle) * PROJECTILE_VELOCITY)
		);

		mMainScene.attachChild(projectile);
		mProjectile.add(projectile);

		Vector2[] vertices = {
	       	new Vector2(0.0f, -22.0f / PIXEL_TO_METER_RATIO_DEFAULT),
        	new Vector2(7.0f / PIXEL_TO_METER_RATIO_DEFAULT, -19.0f / PIXEL_TO_METER_RATIO_DEFAULT),
        	new Vector2(5.0f / PIXEL_TO_METER_RATIO_DEFAULT, -7.0f / PIXEL_TO_METER_RATIO_DEFAULT),
        	new Vector2(-4.0f / PIXEL_TO_METER_RATIO_DEFAULT, -7.0f / PIXEL_TO_METER_RATIO_DEFAULT),
	       	new Vector2(-6.0f / PIXEL_TO_METER_RATIO_DEFAULT, -19.0f / PIXEL_TO_METER_RATIO_DEFAULT)
        };
		
		d("1");
    	//Body projectileBody = PhysicsFactory.createPolygonBody(this.mPhysicsWorld, projectile, vertices, BodyType.DynamicBody, FIXTURE_DEF_PROJECTILE);
		Body projectileBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, 0, 16, 7, BodyType.DynamicBody, FIXTURE_DEF_PROJECTILE);
    	d("2");
       	this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(projectile, projectileBody, true, true));
       	
       	projectileBody.setLinearVelocity(projectileVelocity);
       	projectileBody.setTransform(pX / PIXEL_TO_METER_RATIO_DEFAULT, pY / PIXEL_TO_METER_RATIO_DEFAULT, mShip.angle);
       	projectileBody.setBullet(true);
       	
       	projectile.setUserData(projectileBody);
       	projectileBody.setUserData(projectile);
       	
       	Random rand = new Random();
       	mProjectileFireSound.setRate((rand.nextFloat() * 0.5f) + 0.75f); // Let's change the rate (actually speed) of this sound with a little +/- offset
		mProjectileFireSound.play();
		d("stop");
	}
	
	public void emitProjectileParticles(Projectile proj, Body rock, float energy)
	{
		if(energy <= 0)
			return;
	
		Body projBody = (Body) proj. getUserData();
		float angle = projBody.getAngle();
		
		final float pX = (float) (proj.getX() + (Math.sin(angle) * 15));
		final float pY = (float) (proj.getY() + (Math.cos(angle) * 15));
		
		Vector2 velocity = rock.getLinearVelocity();
		final float vX = velocity.x * PIXEL_TO_METER_RATIO_DEFAULT;
		final float vY = velocity.y * PIXEL_TO_METER_RATIO_DEFAULT;
		final float vOffset = 100 * energy;
		
		final float MIN_RATE = 500 * energy;
        final float MAX_RATE = 500 * energy;
        final int PARTICLES = (int) (500 * energy);
        
        PointParticleEmitter particleEmitter = new PointParticleEmitter(pX, pY);
        SpriteParticleSystem particleSystem = new SpriteParticleSystem(pX, pY, particleEmitter, MIN_RATE, MAX_RATE, PARTICLES, this.mParticleShipFireTextureRegion, this.getVertexBufferObjectManager());

        particleSystem.addParticleInitializer(new VelocityParticleInitializer<Sprite>(vX + -vOffset, vX + vOffset, vY + -vOffset, vY + vOffset));
        particleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(1.0f));
        particleSystem.addParticleModifier(new ScaleParticleModifier<Sprite>(0, 1, 0.3f, 0));
        particleSystem.addParticleModifier(new AlphaParticleModifier<Sprite>(0.4f, 1, 1, 0));
        
        mMainScene.attachChild(particleSystem);
        mProjectileParticleSystem.add(particleSystem);
        
        particleSystem.setUserData((int) (System.currentTimeMillis() / 100));
	}

	/* Fire "button" - a whole screen */
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
	{
		if(pSceneTouchEvent.isActionDown())
		{
			if(!isPointInArea(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), mAccelerateButton.getX(), mAccelerateButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT)
				&& !isPointInArea(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), mRotateLeftButton.getX(), mRotateLeftButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT)
				&& !isPointInArea(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), mRotateRightButton.getX(), mRotateRightButton.getY(), BUTTON_WIDTH, BUTTON_HEIGHT)
				&& System.currentTimeMillis() - lastProjectileFire > PROJECTILE_INTERVAL)
			{
				mShooting = true;
			}
		}
		else if(pSceneTouchEvent.isActionUp())
		{
			mShooting = false;
		}
		
		return false;
	}
	
	public boolean isPointInCircleArea(float pX, float pY, float cX, float cY, float radius)
	{
		if(Math.sqrt(Math.pow((double) (cX - pX), 2.0) + Math.pow((double) (cY - pY), 2)) <= radius)
			return true;

		return false;
	}
	
	public boolean isPointInArea(float pX, float pY, float aX, float aY, float width, float height)
	{
		if(pX > aX && pX < width && pY > aY && pY < height)
			return true;
	
		return false;
	}
	
	public void d(String msg)
	{
		Log.d("srs", msg);
	}
}