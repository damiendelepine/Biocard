package fr.bactech.biocard;

import fr.repele.biokey.R;
import fr.repele.helpers.FDxSDKHelper;
import fr.repele.helpers.NFCHelper;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;


public class Authentify extends Activity {
	public static Tag tag;
	private NfcAdapter mNfcAdapter;
	private NFCHelper mNFCHelper;
	private static VideoView authentifyVideo;
	private static boolean templateNFCOK = false;
	private static boolean templateFingerprintReaderOK = false;
	private static byte[] templateFingerprintReader;
	private static ImageView imageHaut;
	private static ImageView imageBas;
	private ProgressBar progressBar;
	private static ImageView idOk;
	private static ImageView idFailed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentify);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		mNFCHelper = new NFCHelper(this, getApplicationContext(), mNfcAdapter);

		authentifyVideo = (VideoView) findViewById(R.id.AuthentifyVideo);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(authentifyVideo);
		authentifyVideo.setBackgroundColor(Color.TRANSPARENT);
		authentifyVideo.setKeepScreenOn(true);
		authentifyVideo.setVideoPath("android.resource://" + getPackageName() + "/" 
				+ R.raw.background);
		authentifyVideo.start();
		authentifyVideo.getCurrentPosition();
		authentifyVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				authentifyVideo.start();
			}
		});
	

		idOk = (ImageView) findViewById(R.id.idOK);
		idFailed = (ImageView) findViewById(R.id.idFailed);
		imageHaut = (ImageView) findViewById(R.id.imageHaut);
		imageBas = (ImageView) findViewById(R.id.imageBas);
		imageBas.setVisibility(View.VISIBLE);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

	}

	@Override
	public void onPause() {
		mNFCHelper.stopForegroundDispatch(this, mNfcAdapter);

//		templateNFCOK = false;
//		templateFingerprintReaderOK = false;
//		templateFingerprintReader = null;
//		
		super.onPause();
	}

	@Override
	public void onResume() {
		FDxSDKHelper.getFDxSDKHelper(getApplicationContext());
		super.onResume();
		long error = FDxSDKHelper.sgfplib.Init( SGFDxDeviceName.SG_DEV_AUTO);
		if (error != SGFDxErrorCode.SGFDX_ERROR_NONE){
			AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
			if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
				dlgAlert.setMessage("A supported device must be attached or the attached fingerprint device is not supported on Android");
			else
				dlgAlert.setMessage("Fingerprint device initialization failed !");
			dlgAlert.setTitle("BachTech");
			dlgAlert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton){
					finish();
					return;        		    	  
				}        			
			}
					);
			dlgAlert.setCancelable(false);
			dlgAlert.create().show();        	
		}
		else {
			UsbDevice usbDevice = FDxSDKHelper.sgfplib.GetUsbDevice();
			if (usbDevice == null){
				AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
				dlgAlert.setMessage("SDU04P or SDU03P fingerprint sensor not found!");
				dlgAlert.setTitle("SecuGen Fingerprint SDK");
				dlgAlert.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int whichButton){
						finish();
						return;        		    	  
					}        			
				}
						);
				dlgAlert.setCancelable(false);
				dlgAlert.create().show();
			}
			else {
				FDxSDKHelper.sgfplib.GetUsbManager().requestPermission(usbDevice, FDxSDKHelper.mPermissionIntent);
				error = FDxSDKHelper.sgfplib.OpenDevice(0);
				SecuGen.FDxSDKPro.SGDeviceInfoParam deviceInfo = new SecuGen.FDxSDKPro.SGDeviceInfoParam();
				error = FDxSDKHelper.sgfplib.GetDeviceInfo(deviceInfo);
				FDxSDKHelper.sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ANSI378);
			}
		}
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		mNFCHelper.setupForegroundDispatch(this, mNfcAdapter);

		myHandler = new Handler();
		myHandler.postDelayed(myRunnable,500);
	}


	@Override
	protected void onNewIntent(Intent intent) {
		tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		templateNFCOK = NFCHelper.read(intent);

		if(templateNFCOK && templateFingerprintReaderOK){
			progressBar.setVisibility(View.GONE);
			imageHaut.setVisibility(View.GONE);
			testMatchingScore();
		}

		super.onNewIntent(intent);

	}

	private static Handler myHandler;
	private static Runnable myRunnable = new Runnable() {
		@SuppressLint("ShowToast")
		@Override
		public void run()  {
			if(!templateFingerprintReaderOK){

				templateFingerprintReader = new byte[FDxSDKHelper
				                                     .getTemplateSize()];
				long codeDeRetour = FDxSDKHelper.setTemplate(
						templateFingerprintReader);
				if (codeDeRetour == SGFDxErrorCode.SGFDX_ERROR_NONE) {
					idOk.setVisibility(View.GONE);
					idFailed.setVisibility(View.GONE);

					templateFingerprintReaderOK = true;
					imageBas.setVisibility(View.GONE);
					imageHaut.setVisibility(View.VISIBLE);
					System.out.println("Template leteur d'empreintes OK");
				} else {
					System.out.println("Code de retour : " + codeDeRetour);
				}

			} 

			if(templateNFCOK && templateFingerprintReaderOK){
				testMatchingScore();
			}

			myHandler.postDelayed(this,500);
		}
	};



	@SuppressLint("ShowToast")
	private static void testMatchingScore(){
		int[] score = FDxSDKHelper.getMatchingScore(templateFingerprintReader, NFCHelper.template_read);
		System.out.println("Matching score : " + score[0]);
		if(score[0] > 100){
			System.out.println("Authentify");
			idOk.setVisibility(View.VISIBLE);
		}
		else{
			System.out.println("Identification failed");
			idFailed.setVisibility(View.VISIBLE);
		}

		myHandler.removeCallbacks(myRunnable);
		
		templateNFCOK = false;
		templateFingerprintReaderOK = false;
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}
	
	
}
