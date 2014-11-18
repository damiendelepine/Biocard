package fr.bactech.biocard;

import java.io.IOException;

import fr.repele.biokey.R;
import fr.repele.helpers.FDxSDKHelper;
import fr.repele.helpers.NFCHelper;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import SecuGen.FDxSDKPro.SGFDxTemplateFormat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.media.MediaPlayer;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

public class Register extends Activity {

	public static Tag tag;
	private NfcAdapter mNfcAdapter;
	private NFCHelper mNFCHelper;
	private byte[] templateFingerprint;
	private static boolean templateFingerPrintOk;
	private static ImageView imageHaut;
	private static ImageView imageBas;
	private ProgressBar progressBar;
	private ImageView enregistrementOk;
	private VideoView registerVideo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		mNFCHelper = new NFCHelper(this, getApplicationContext(), mNfcAdapter);
		
		registerVideo = (VideoView) findViewById(R.id.RegisterVideo);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(registerVideo);
		registerVideo.setBackgroundColor(Color.TRANSPARENT);
		registerVideo.setKeepScreenOn(true);
		registerVideo.setVideoPath("android.resource://" + getPackageName() + "/" 
				+ R.raw.background);
		registerVideo.start();
		registerVideo.getCurrentPosition();
		registerVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				registerVideo.start();
			}
		});
		
		enregistrementOk = (ImageView) findViewById(R.id.enregistrementOk);
		imageHaut = (ImageView) findViewById(R.id.imageHaut);
		imageBas = (ImageView) findViewById(R.id.imageBas);
		imageBas.setVisibility(View.VISIBLE);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

	}

	@Override
	public void onPause() {
		mNFCHelper.stopForegroundDispatch(this, mNfcAdapter);
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
	
	private Handler myHandler;
	private Runnable myRunnable = new Runnable() {

		@Override
		public void run() {

			// Code �  éxécuter de façon périodique
			templateFingerprint = new byte[FDxSDKHelper
			                                     .getTemplateSize()];
			long codeDeRetour = FDxSDKHelper.setTemplate(
					templateFingerprint);
			if (codeDeRetour == SGFDxErrorCode.SGFDX_ERROR_NONE) {
				templateFingerprint  = FDxSDKHelper.cutByteArray(templateFingerprint, 400);
				Register.templateFingerPrintOk = true;
				imageBas.setVisibility(View.GONE);
				imageHaut.setVisibility(View.VISIBLE);
			} else {
				System.out.println("Code de retour : " + codeDeRetour);
				myHandler.postDelayed(this, 500);
			}
		}

	};


	@SuppressLint("ShowToast")
	@Override
	protected void onNewIntent(Intent intent) {
		boolean tagHandelable = false;
		tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		for(int i = 0; i < tag.getTechList().length; i++){
			System.out.println(tag.getTechList()[i].toString());
			if(tag.getTechList()[i].toString().equals("android.nfc.tech.Ndef")){
				tagHandelable = true;
			}
		}
		
		if (tagHandelable) {
			if (Register.templateFingerPrintOk) {
				try {
					NFCHelper.writeTemplate(templateFingerprint, tag);
					progressBar.setVisibility(View.GONE);
					imageHaut.setVisibility(View.GONE);
					enregistrementOk.setVisibility(View.VISIBLE);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (FormatException e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Use fingerprint-reader before", Toast.LENGTH_SHORT);
			}
		}
		else{
			Toast.makeText(getApplicationContext(),
					"Not a good tag", Toast.LENGTH_SHORT);
		}
		super.onNewIntent(intent);
	}

	
	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}
	
	
}
