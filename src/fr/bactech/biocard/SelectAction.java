package fr.bactech.biocard;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;
import fr.repele.biokey.R;

public class SelectAction extends Activity {

	private ImageButton buttonAuthentify;
	private ImageButton buttonRegister;
	private VideoView menuVideo;
	private boolean backAlreadyPressed = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_action_activity);

		menuVideo = (VideoView) findViewById(R.id.menuVideo);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(menuVideo);
		menuVideo.setBackgroundColor(Color.TRANSPARENT);
		menuVideo.setKeepScreenOn(true);
		menuVideo.setVideoPath("android.resource://" + getPackageName() + "/" 
				+ R.raw.menu);
		menuVideo.start();
		menuVideo.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.start();	
			}
		});

		buttonAuthentify = (ImageButton) findViewById(R.id.buttonAuthentify);
		buttonAuthentify.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Authentify.class);
				startActivity(intent);
			}
		});
		

		buttonRegister = (ImageButton) findViewById(R.id.buttonRegister);
		buttonRegister.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), Register.class);
				startActivity(intent);	
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		menuVideo.start();
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}


	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
	

	

}
