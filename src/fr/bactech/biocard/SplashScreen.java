package fr.bactech.biocard;


import fr.repele.biokey.R;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.VideoView;

public class SplashScreen extends Activity {


	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}


	private VideoView splashScreenVideo;
    boolean pausing = false;
    public static String filepath;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		splashScreenVideo = (VideoView) findViewById(R.id.SplashScreenVideo);
	    MediaController mediaController = new MediaController(this);
	    mediaController.setAnchorView(splashScreenVideo);
	    splashScreenVideo.setBackgroundColor(Color.TRANSPARENT);
	    splashScreenVideo.setKeepScreenOn(true);
	    splashScreenVideo.setVideoPath("android.resource://" + getPackageName() + "/" 
				+ R.raw.intro);
	    splashScreenVideo.start();
	    splashScreenVideo.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Intent intent = new Intent(getApplicationContext(), SelectAction.class);
				startActivity(intent);	
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}


	@Override
	protected void onResume() {
		super.onResume();
		splashScreenVideo.start();
	}
	
	

}
