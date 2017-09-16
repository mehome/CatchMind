package com.catchmind.catchmind;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.StringSignature;
import com.catchmind.catchmind.AppRTC.CallActivity;
import com.catchmind.catchmind.AppRTC.ConnectActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by sonsch94 on 2017-07-27.
 */

public class ProfileActivity extends AppCompatActivity {


    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private static final int Profile_Change = 4292;
    private static final String TAG = "ProfileActivity_openCV";
    private Uri mImageCaptureUri;
    private String absolutePath;
//    ProgressDialog dialog = null;
    int serverResponseCode = 0;
    final String upLoadServerUri = "http://vnschat.vps.phps.kr/UploadToServer.php";

    public TextView profileTitle;
    public Button profilebtn;
    public Button talkbtn;
    public Button videoBtn;
    public ImageView profileIV;
    public Bitmap photo;
    public File sourceFile ;
    public FileOutputStream fOut;
    Bitmap out;

    private ChatService mService;

    public String userId;
    public String nickname;
    private Mat matResult;

    public SharedPreferences mPref;
    public String myNickname;

    public static native long loadCascade(String cascadeFileName );
    public static native int detect(long cascadeClassifier_face,
                                     long cascadeClassifier_eye, long matAddrInput, long matAddrResult);
    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;


    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }


    //////////////// Connect Part ///////////////////////

    public static final int sendVideoCall = 235711;
    private static final int CONNECTION_REQUEST = 1;
    private static final int REMOVE_FAVORITE_INDEX = 0;
    private static boolean commandLineRun = false;

    private ImageButton connectButton;
    private ImageButton addFavoriteButton;
    private EditText roomEditText;
    private ListView roomListView;
    private SharedPreferences sharedPref;
    private String keyprefVideoCallEnabled;
    private String keyprefScreencapture;
    private String keyprefCamera2;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefCaptureQualitySlider;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefVideoCodec;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefAudioCodec;
    private String keyprefHwCodecAcceleration;
    private String keyprefCaptureToTexture;
    private String keyprefFlexfec;
    private String keyprefNoAudioProcessingPipeline;
    private String keyprefAecDump;
    private String keyprefOpenSLES;
    private String keyprefDisableBuiltInAec;
    private String keyprefDisableBuiltInAgc;
    private String keyprefDisableBuiltInNs;
    private String keyprefEnableLevelControl;
    private String keyprefDisableWebRtcAGCAndHPF;
    private String keyprefDisplayHud;
    private String keyprefTracing;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;
    private ArrayList<String> roomList;
    private ArrayAdapter<String> adapter;
    private String keyprefEnableDataChannel;
    private String keyprefOrdered;
    private String keyprefMaxRetransmitTimeMs;
    private String keyprefMaxRetransmits;
    private String keyprefDataProtocol;
    private String keyprefNegotiated;
    private String keyprefDataId;

    ///////////////// End Connect Part ///////////////////////

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileTitle = (TextView)findViewById(R.id.ProfileTitle);
        profileIV = (ImageView)findViewById(R.id.ProfileImage);
        profilebtn = (Button)findViewById(R.id.profilebtn);
        talkbtn = (Button)findViewById(R.id.talkbtn);
        videoBtn = (Button)findViewById(R.id.VideoCallBtn);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position",0);
        if(position == 1){
            profilebtn.setVisibility(View.VISIBLE);
            talkbtn.setVisibility(View.GONE);
            videoBtn.setVisibility(View.GONE);
        }
        nickname = intent.getStringExtra("nickname");
        userId = intent.getStringExtra("userId");
        String profile = intent.getStringExtra("profile");
        profileTitle.setText(nickname+"님의 프로필");
        if(position ==1){
            if(profile.equals("none")){
                profileIV.setImageResource(R.drawable.default_profile_image);
            }else {
                Glide.with(this).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into(profileIV);
            }
        }else {
//            Toast.makeText(this,profile,Toast.LENGTH_SHORT).show();
            if(profile.equals("none")){
                profileIV.setImageResource(R.drawable.default_profile_image);
            }else {
                Glide.with(this).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new StringSignature(profile))
                        .into(profileIV);
            }
        }
        profileIV.setBackgroundResource(R.drawable.profile_border);



        read_cascade_file();

        mPref = getSharedPreferences("login",MODE_PRIVATE);

        myNickname = mPref.getString("nickname","메세지없음");

        /////////////////// Connect Part //////////////////////

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
        keyprefScreencapture = getString(R.string.pref_screencapture_key);
        keyprefCamera2 = getString(R.string.pref_camera2_key);
        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefVideoCodec = getString(R.string.pref_videocodec_key);
        keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
        keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
        keyprefFlexfec = getString(R.string.pref_flexfec_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
        keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
        keyprefAecDump = getString(R.string.pref_aecdump_key);
        keyprefOpenSLES = getString(R.string.pref_opensles_key);
        keyprefDisableBuiltInAec = getString(R.string.pref_disable_built_in_aec_key);
        keyprefDisableBuiltInAgc = getString(R.string.pref_disable_built_in_agc_key);
        keyprefDisableBuiltInNs = getString(R.string.pref_disable_built_in_ns_key);
        keyprefEnableLevelControl = getString(R.string.pref_enable_level_control_key);
        keyprefDisableWebRtcAGCAndHPF = getString(R.string.pref_disable_webrtc_agc_and_hpf_key);
        keyprefDisplayHud = getString(R.string.pref_displayhud_key);
        keyprefTracing = getString(R.string.pref_tracing_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);
        keyprefEnableDataChannel = getString(R.string.pref_enable_datachannel_key);
        keyprefOrdered = getString(R.string.pref_ordered_key);
        keyprefMaxRetransmitTimeMs = getString(R.string.pref_max_retransmit_time_ms_key);
        keyprefMaxRetransmits = getString(R.string.pref_max_retransmits_key);
        keyprefDataProtocol = getString(R.string.pref_data_protocol_key);
        keyprefNegotiated = getString(R.string.pref_negotiated_key);
        keyprefDataId = getString(R.string.pref_data_id_key);

        final Intent intent_2 = getIntent();
        if ("android.intent.action.VIEW".equals(intent_2.getAction()) && !commandLineRun) {
            boolean loopback = intent_2.getBooleanExtra(CallActivity.EXTRA_LOOPBACK, false);
            int runTimeMs = intent_2.getIntExtra(CallActivity.EXTRA_RUNTIME, 0);
            boolean useValuesFromIntent =
                    intent_2.getBooleanExtra(CallActivity.EXTRA_USE_VALUES_FROM_INTENT, false);
            String room = sharedPref.getString(keyprefRoom, "");
            connectToRoom(room, true, loopback, useValuesFromIntent, runTimeMs);
        }


        Intent serviceIntent = new Intent(this, ChatService.class);
        serviceIntent.putExtra("FromLogin",false);
        bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);

//        Calendar calendar = Calendar.getInstance();
//
//        int hour = calendar.get(Calendar.HOUR);
//        int minute = calendar.get(Calendar.MINUTE);
//        connectToRoom(""+hour+""+minute, false, false, false, 0);
//        finish();
//


        ////////////////End Connect Part //////////////////////

    }

//    public void imageDefault(View v){
//        profileIV.setImageResource(R.drawable.default_profile_image);
//        ImageDefaultThread idt = new ImageDefaultThread(userId);
//        idt.start();
//    }

    public void imageSend(View v){

//        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which){
//                doTakePhotoAction();
//            }
//
//        };
//
//        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which){
//                doTakeAlbumAction();
//
//            }
//
//        };
//
//
//        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){
//
//            @Override public void onClick(DialogInterface dialog, int which){
//                dialog.dismiss();
//            }
//
//        };
//
//
//
//
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("업로드할 이미지 선택")
//                .setPositiveButton("사진촬영", cameraListener)
//                .setNeutralButton("취소", cancelListener)
//                .setNegativeButton("앨범선택", albumListener)
//                .create();
//
//        dialog.show();
//
//        Button pbtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        pbtn.setTextColor(Color.BLACK);
//        Button neubtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        neubtn.setTextColor(Color.BLACK);
//        Button negbtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        negbtn.setTextColor(Color.BLACK);

        Intent ICintent = new Intent(this,ProfileChangeActivity.class);
        startActivityForResult(ICintent,Profile_Change);


    }

    public void doTakePhotoAction(){

        Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);

        String url = "tmp" + ".png";
//        mImageCaptureUri = Uri.fromFile ( new File(Environment.getExternalStorageDirectory(), url));
        mImageCaptureUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName()+".provider", new File(Environment.getExternalStorageDirectory(), url));

        Log.d("사진_doTakePhoto", Environment.getExternalStorageState().toString());
        Log.d("사진_doTakePhoto", mImageCaptureUri.toString());


        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    public void doTakeAlbumAction(){

        Intent intent = new Intent (Intent.ACTION_PICK);
//        intent.setType (MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType ("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        switch ( requestCode ){

            case PICK_FROM_CAMERA: {

                if(resultCode != RESULT_OK){

                    return;
                }

                try {

                    String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String imgpath = ex_storage + "/tmp.png";
//                    photo = BitmapFactory.decodeFile(imgpath);
//                    profileIV.setImageBitmap(photo);

                    Log.d("이미지경로",imgpath);

//                    dialog = ProgressDialog.show(this, "", "Uploading file...", true);


                    Bitmap bitmap = BitmapFactory.decodeFile(imgpath);

                    Mat matInput = new Mat (bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
                    Utils.bitmapToMat(bitmap, matInput);

                    if ( matResult != null ) matResult.release();
                    matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());


                    int faceNum = detect(cascadeClassifier_face,cascadeClassifier_eye, matInput.getNativeObjAddr(),
                            matResult.getNativeObjAddr());

                    Log.d("found","디텍트결과###"+faceNum);

                    if(faceNum >0) {


                        ImageSendThread ist = new ImageSendThread(imgpath);
                        ist.start();

                        ist.join();

                        Glide.with(this).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                .into(profileIV);
                    }else{

                        Toast.makeText(this,"얼굴 사진만 등록할 수 있습니다",Toast.LENGTH_SHORT).show();

                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

//                Log.d("피곤해좆같앙",mImageCaptureUri.getPath());
//                try {
//                    String ex_storage =Environment.getExternalStorageDirectory().getAbsolutePath();
//                    String imgpath = ex_storage+"/tmp.jpg";
//
//                }catch (Exception e){
//
//                }



                break;
            }

            case PICK_FROM_ALBUM: {

                if(data == null){
                    return;
                }
                Log.d("이미지1",data.toString());
                Log.d("이미지2",getPath(data.getData()));

                if(resultCode != RESULT_OK){
                    Log.d("이미지픽프롬앨범실패",getPath(data.getData()));
                    return;
                }

//                photo = BitmapFactory.decodeFile(getPath(data.getData()));
//                profileIV.setImageBitmap(photo);

//                try {
//                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                    Log.d("이미지external",MediaStore.Images.Media.getContentUri("external").toString());
//                    profileIV.setImageBitmap(photo);
//                }catch(IOException e){
//                    e.printStackTrace();
//                }
                Log.d("이미지픽프롬앨범",getPath(data.getData()));
//                dialog = ProgressDialog.show(this, "", "Uploading file...", true);




                Bitmap bitmap = BitmapFactory.decodeFile(getPath(data.getData()));

                Mat matInput = new Mat (bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
                Utils.bitmapToMat(bitmap, matInput);

                if ( matResult != null ) matResult.release();
                matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());


                int faceNum = detect(cascadeClassifier_face,cascadeClassifier_eye, matInput.getNativeObjAddr(),
                        matResult.getNativeObjAddr());

                Log.d("found","디텍트결과###"+faceNum);

                if(faceNum >0) {


                    ImageSendThread ist = new ImageSendThread(getPath(data.getData()));
                    ist.start();
                    try {

                        ist.join();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Glide.with(this).load("http://vnschat.vps.phps.kr/profile_image/" + userId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                            .into(profileIV);

                    mImageCaptureUri = data.getData();
                    Log.d("이미지앨범onactivityresult", mImageCaptureUri.toString());

                }else{
                    Toast.makeText(this,"얼굴 사진만 등록할 수 있습니다",Toast.LENGTH_SHORT).show();
                }

//                Intent intent = new Intent ("com.android.camera.action.CROP");
//                intent.setDataAndType(mImageCaptureUri, "image/*");
//                intent.putExtra("outputX", 200);
//                intent.putExtra("outputY", 200);
//                intent.putExtra("aspectX", 1);
//                intent.putExtra("aspectY", 1);
//                intent.putExtra("scale",true);
//                intent.putExtra("return-data", true);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
//
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//                startActivityForResult(intent, CROP_FROM_IMAGE);


                break;

            }


            case Profile_Change: {

                if(resultCode == RESULT_OK) {
                    String type = data.getExtras().getString("IC");

                    if (type.equals("album")) {
                        doTakeAlbumAction();
                    } else if (type.equals("camera")) {
                        doTakePhotoAction();
                    } else {
                        profileIV.setImageResource(R.drawable.default_profile_image);
                        ImageDefaultThread idt = new ImageDefaultThread(userId);
                        idt.start();
                    }
                }
            }


//            case CROP_FROM_IMAGE: {
//
//                if ( resultCode != RESULT_OK){
//                    return;
//                }
//
//                final Bundle extras = data.getExtras();
//
//                if (extras != null){
//
//                    photo = extras.getParcelable("data");
//                    profileIV.setImageBitmap(photo);
//                    dialog = ProgressDialog.show(this, "", "Uploading file...", true);
//                    ImageSendThread ist = new ImageSendThread(getPath(data.getData()));
//                    ist.start();
//
//                }
//
//                break;
//
//            }


        }

        if (requestCode == CONNECTION_REQUEST && commandLineRun) {
            Log.d(TAG, "Return: " + resultCode);
            Log.d("별이", "Return: " + resultCode);
            setResult(resultCode);
            commandLineRun = false;
            finish();
        }


   }


    public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }


    public int uploadFile(String sourceFileUri) {

        Log.d("이미지업로딩시작",sourceFileUri);
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
//        File sourceFile = new File(sourceFileUri);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp.png";

        Bitmap b= BitmapFactory.decodeFile(sourceFileUri);
        Bitmap out = Bitmap.createScaledBitmap(b, 400, 400, false);


        sourceFile = new File(path);

        try {
            Log.d("이미지새파일경로1",sourceFile.getAbsolutePath());
            fOut = new FileOutputStream(sourceFile);
            Log.d("이미지새파일경로2",sourceFile.getAbsolutePath());
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//            Log.d("이미지새파일경로3",sourceFile.getAbsolutePath());
//            fOut.flush();
//            Log.d("이미지새파일경로4",sourceFile.getAbsolutePath());
//            fOut.close();
            Log.d("이미지새파일경로5",sourceFile.getAbsolutePath());

        } catch (Exception e) {}



        if (!sourceFile.isFile()) {

            Log.d("이미지경로에없음","경로에없나?");
//            dialog.dismiss();

            return 0;

        }else{

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + userId+".png" + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.d("이미지uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

//                if(serverResponseCode == 200){
//
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//
//
//                            Toast.makeText(UploadToServer.this, "File Upload Complete.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }


                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


                InputStream is = null;
                BufferedReader in = null;


                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                String data = buff.toString().trim();
                Log.d("이미지성공실패",data);


            } catch (MalformedURLException ex) {

//                dialog.dismiss();
                ex.printStackTrace();

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        messageText.setText("MalformedURLException Exception : check script url.");
//                        Toast.makeText(UploadToServer.this, "MalformedURLException",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

//                dialog.dismiss();
                e.printStackTrace();

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        messageText.setText("Got Exception : see logcat ");
//                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//                Log.e("Upload file to server Exception", "Exception : "
//                        + e.getMessage(), e);
            }

//            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }


    public class ImageSendThread extends Thread {

        public String filePath;

        public ImageSendThread (String uri){
            this.filePath = uri;
        }

        @Override
        public void run() {

            uploadFile(filePath);

        }


    }

    public class ImageDefaultThread extends Thread {

        String sUserId;

        public ImageDefaultThread (String userId){
            this.sUserId = userId;
        }

        @Override
        public void run() {
            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+ sUserId;
            try {
            /* 서버연결 */
                URL url = new URL("http://vnschat.vps.phps.kr/defaultImage.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

            /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("디폴트스레드결과",data.toString());


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void talk(View v){

        Intent intent = new Intent();
        intent.putExtra("friendId",userId);
        intent.putExtra("nickname",nickname);
        intent.putExtra("no",0);
        setResult(RESULT_OK, intent);
        finish();

    }

    //////////////////////////////////////




    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    private void read_cascade_file(){

        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");

    }


    public void videoCall(View v){
//        Intent videoIntent = new Intent(this, ConnectActivity.class);
//        startActivity(videoIntent);

//        Calendar calendar = Calendar.getInstance();
//
//        int hour = calendar.get(Calendar.HOUR);
//        int minute = calendar.get(Calendar.MINUTE);

        try {

            Random random = new Random();

            String roomId = userId + random.nextInt(100000);

            connectToRoom(roomId, false, false, false, 0);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("roomId", roomId);
            jsonObject.put("nickname", myNickname);

            mService.sendVideoCall(userId, jsonObject.toString());

        }catch (JSONException e){
            e.printStackTrace();
        }

    }



    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴

        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    ///////////////Connect Part ////////////////


    private String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.valueOf(getString(defaultId));
        if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    private int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }

    private void connectToRoom(String roomId, boolean commandLineRun, boolean loopback,
                               boolean useValuesFromIntent, int runTimeMs) {
        this.commandLineRun = commandLineRun;

        // roomId is random for loopback.
        if (loopback) {
            roomId = Integer.toString((new Random()).nextInt(100000000));
        }

        String roomUrl = sharedPref.getString(
                keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                CallActivity.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                CallActivity.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, CallActivity.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);

        // Get default codecs.
        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                CallActivity.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);
        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                CallActivity.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);

        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                CallActivity.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                CallActivity.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                CallActivity.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                CallActivity.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                CallActivity.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Enable level control.
        boolean enableLevelControl = sharedPrefGetBoolean(R.string.pref_enable_level_control_key,
                CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, R.string.pref_enable_level_control_key,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);

        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
                CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(CallActivity.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        // Check statistics display option.
        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
                CallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);

        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, CallActivity.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                CallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);
        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, CallActivity.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                CallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                CallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, CallActivity.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, CallActivity.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                CallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);

        // Start AppRTCMobile activity.
        Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
        if (validateUrl(roomUrl)) {
            Uri uri = Uri.parse(roomUrl);
            Intent intent = new Intent(this, CallActivity.class);
            intent.setData(uri);
            intent.putExtra(CallActivity.EXTRA_ROOMID, roomId);
            intent.putExtra(CallActivity.EXTRA_LOOPBACK, loopback);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(CallActivity.EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(CallActivity.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(CallActivity.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(CallActivity.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(CallActivity.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(CallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, captureQualitySlider);
            intent.putExtra(CallActivity.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(CallActivity.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(CallActivity.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(CallActivity.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(CallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(CallActivity.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(CallActivity.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(CallActivity.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(CallActivity.EXTRA_ENABLE_LEVEL_CONTROL, enableLevelControl);
            intent.putExtra(CallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(CallActivity.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(CallActivity.EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(CallActivity.EXTRA_DISPLAY_HUD, displayHud);
            intent.putExtra(CallActivity.EXTRA_TRACING, tracing);
            intent.putExtra(CallActivity.EXTRA_CMDLINE, commandLineRun);
            intent.putExtra(CallActivity.EXTRA_RUNTIME, runTimeMs);

            intent.putExtra(CallActivity.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(CallActivity.EXTRA_ORDERED, ordered);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(CallActivity.EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(CallActivity.EXTRA_PROTOCOL, protocol);
                intent.putExtra(CallActivity.EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(CallActivity.EXTRA_ID, id);
            }

            if (useValuesFromIntent) {

                if (getIntent().hasExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA)) {
                    String videoFileAsCamera =
                            getIntent().getStringExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA);
                    intent.putExtra(CallActivity.EXTRA_VIDEO_FILE_AS_CAMERA, videoFileAsCamera);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)) {
                    String saveRemoteVideoToFile =
                            getIntent().getStringExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE, saveRemoteVideoToFile);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH)) {
                    int videoOutWidth =
                            getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, videoOutWidth);
                }

                if (getIntent().hasExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT)) {
                    int videoOutHeight =
                            getIntent().getIntExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0);
                    intent.putExtra(CallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, videoOutHeight);
                }

            }

            startActivityForResult(intent, CONNECTION_REQUEST);

        }
    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }

    ///////////////// End Connect Part/////

}
