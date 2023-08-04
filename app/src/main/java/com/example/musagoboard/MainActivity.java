package com.example.musagoboard;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzix";
    public static final int REQUEST_TAKE_PHOTO = 10;
    public static final int REQUEST_PERMISSION = 11;
    public final int GET_GALLERY_IMAGE = 200;
    AlertDialog.Builder alertdialog;
    String [] selectFile;
    private String mPhotoPath;
    int serverResponseCode = 0;
    private static String IP_ADDRESS ="192.168.0.150/eunhye"; //본인 IP주소를 넣으세요.
    Button btn_save;
    Button btn_Img;

    EditText et_subject;
    EditText et_author;
    EditText et_content;
    ImageView img_file;

    String imgName = "";

    public static Context context;
    private ProgressBar progressBar;
    private File file, dir;
    private String savePath= "Download/capture";
    private String FileName = null;
    String upLoadServerUri = "http://192.168.0.67/upload.php";//서버컴퓨터의 ip주소
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getBaseContext();//앱 종료 후 메모리 유지를 피하기 위해서 getBaseContext를 사용.
        checkPermission(); //권한체크
        img_file=findViewById(R.id.img_file);
        et_subject=findViewById(R.id.et_subject);
        et_author=findViewById(R.id.et_author);
        et_content=findViewById(R.id.et_content);
        btn_save=findViewById(R.id.btn_save);
        btn_Img=findViewById(R.id.btn_Img);
        btn_Img.setOnClickListener(v -> Imgfile());
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = et_subject.getText().toString();
                String author=et_author.getText().toString();
                String content=et_content.getText().toString().replaceAll("\n","<br>");
                String fileImg=imgName;
                Log.d(TAG,"fileImg  :"+fileImg);

                InsertData task = new InsertData();
                task.execute("http://"+IP_ADDRESS+"/android_log_insert1.php",subject,author,content,fileImg);
                et_subject.setText("");
                et_author.setText("");
                et_content.setText("");
                img_file.setImageResource(0);


                upLoad();
                Intent intent = new Intent(getApplicationContext(), BoardListActivity.class);
                startActivity(intent);
            }
        });


    }

    private void upLoad() {
        new Thread(new Runnable() {

            public void run() {

               uploadFile(uploadFilePath + "" + imgName);

              //  uploadFile(uploadFilePath + "" + "tmp.jpg");
                Log.w(TAG,"log :"+ "GO" );

            }

        }).start();
    }
    private void Imgfile() {
            selectFile = getResources().getStringArray(R.array.selectFile);
            alertdialog = new AlertDialog.Builder(MainActivity.this);
            alertdialog.setTitle("파일 선택");
            //다이얼로그 리스트 담기
            alertdialog.setItems(selectFile, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG,"다이얼로그");
                    if (which == 0) {
                        Camera();
                        Log.d(TAG,"다이얼 카메라");
                    } else if (which == 1) {
                        Gallery();
                        Log.d(TAG,"다이얼 앨범");
                    } else if (which == 2) {
                        dialog.dismiss();
                        Log.d(TAG,"다이얼 취소");
                    }
                }
            });
            AlertDialog alertDialog= alertdialog.create();
            alertDialog.show();
        }
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //after capture
            switch (requestCode) {
                case GET_GALLERY_IMAGE: {
                    if (resultCode == RESULT_OK) {

                        Uri uri = data.getData();
                        img_file.setImageURI(uri);
                        Log.d(TAG, "Image :" + uri);


                        imgName = getFileName(uri);

                        Log.w(TAG, "Image :" + imgName);


                        Log.d(TAG,"캘러리에서 사진 선택됨.");


                        final String strNewFolder = "TestFolder" + File.separator;
                        final String strFileName = "tmp.jpg";

                        final String strCurPath = getFilesDir().getPath() + File.separator;
                        final String strNewPath = getFilesDir().getPath() + File.separator + strNewFolder;


                        final File fOldFileDir = new File (strCurPath);
                        final File fNewFileDir = new File(strCurPath + strNewFolder);

                        if(!fNewFileDir.exists()){
                            fNewFileDir.mkdir();
                            Log.d(TAG," strFileName :" + strFileName);
                        }
                        else{
                            String from = strCurPath + strFileName;
                            String to = strNewPath + strFileName;
                            Log.d(TAG,"확인" );
                            try{
                                filecopy(from, to);
                                //Toast.makeText(fileCopy.this, "Copy Sueccess", Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"from :" + from);
                                Log.d(TAG,"to :" + to);
                                saveImg();
                            }
                            catch(FileNotFoundException e){
                                // Toast.makeText(fileCopy.this, strFileName + "File not exist", Toast.LENGTH_SHORT).show();
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }


                    }
                } break;
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        File file = new File(mPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getContentResolver(), Uri.fromFile(file));

                        if (bitmap != null) {
                            ExifInterface ei = new ExifInterface(mPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

//                            //사진해상도가 너무 높으면 비트맵으로 로딩
//                            BitmapFactory.Options options = new BitmapFactory.Options();
//                            options.inSampleSize = 8; //8분의 1크기로 비트맵 객체 생성
//                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            Bitmap rotatedBitmap = null;
                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }


                            Log.d(TAG,"카메라에서 사진 확인함");

                            //Rotate한 bitmap을 ImageView에 저장
                            img_file.setImageBitmap(rotatedBitmap);


                            ImgSave();

                        }
                    }

                    break;
                }
            }


        } catch (Exception e) {
            Log.w(TAG, "onActivityResult Error !", e);
        }
    }

    private void filecopy(String from, String to)throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel in = null;
        FileChannel out = null;
        Log.w(TAG, "from :" + from );
        Log.w(TAG, "to :" + to );
        try{
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
            Log.w(TAG, "filecopy :" + "확인" );
            in.transferTo(0, in.size(), out);
        }

        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if(out != null)
                out.close();
            if(in != null)
                in.close();
            if(fis != null)
                fis.close();
            if(fos != null)
                fos.close();
        }
    }

    private void ImgSave() {
        try {

            BitmapDrawable drawable = (BitmapDrawable) img_file.getDrawable();
            Bitmap bitmap = drawable.getBitmap();

            //찍은 사진이 없으면
            if (bitmap == null) {
                Log.d(TAG,"저장할 사진이 없습니다.");
            } else {
                //저장
                saveImg();
//                    Log.w(TAG, "파일명"+filename);
                mPhotoPath = ""; //initialize
            }

        } catch (Exception e) {
            Log.w(TAG, "SAVE ERROR!", e);
        }
    }

    private void saveImg() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName =  "Capture_"+ timeStamp ; //ex) Capture_20230704_
       // String imageFileName =  "tmp";

        try {
            //저장할 파일 경로
            File storageDir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/Download/capture");
            if (!storageDir.exists()) //폴더가 없으면 생성.
                storageDir.mkdirs();
            String filename = imageFileName + ".jpg";
            Log.w(TAG, "(*)파일명 :"+filename);
            Log.d(TAG,"파일(save) :" + storageDir);
            imgName=filename;
/*

            String ext = Environment.getExternalStorageState();
            if (ext.equals(Environment.MEDIA_MOUNTED)) {
                apkDnPATH = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + Save_folder;
            }
*/



            // 기존에 있다면 삭제
            File file = new File(storageDir, filename);
            boolean deleted = file.delete();
            Log.w(TAG, "file : " + file);
            Log.w(TAG, "Delete Dup Check : " + deleted);
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(file);
                BitmapDrawable drawable = (BitmapDrawable) img_file.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output); //해상도에 맞추어 Compress
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {

                    assert output != null;
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w(TAG, "파일어딨어 !",e);
                }
            }

            Log.e(TAG, "Captured Saved");
            // Toast.makeText(this, "Capture Saved ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.w(TAG, "Capture Saving Error!", e);
            // Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();

        }
    }

    //카메라에 맞게 이미지 로테이션
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    


    private void Gallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent,GET_GALLERY_IMAGE);
        Log.w(TAG, "Gallery접근"+intent);
    }

    private void Camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 인텐트를 처리 할 카메라 액티비티가 있는지 확인
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // 촬영한 사진을 저장할 파일 생성
            File photoFile = null;

            try {
                //임시로 사용할 파일이므로 경로는 캐시폴더로
                File tempDir = getCacheDir();

//                //임시촬영파일 세팅
//                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                String imageFileName =  timeStamp ; //ex) Capture_20230704_
////        String imageFileName = "a"; //ex) Capture_20230704_
//
//                File tempImage = File.createTempFile(
//                        imageFileName,  /* 파일이름 */
//                        ".jpg",         /* 파일형식 */
//                        tempDir      /* 경로 */
//                );


                File tempImage = File.createTempFile(
                        "tmp",  /* 파일이름 */
                        ".jpg",         /* 파일형식 */
                        tempDir      /* 경로 */
                );

                // ACTION_VIEW 인텐트를 사용할 경로 (임시파일의 경로)
                mPhotoPath = tempImage.getAbsolutePath();

                photoFile = tempImage;
                Log.w(TAG, "파일 생성 !" );

            } catch (IOException e) {
                //에러 로그는 이렇게 관리하는 편이 좋다.
                Log.w(TAG, "파일 생성 에러!", e);
            }

            //파일이 정상적으로 생성되었다면 계속 진행
            if (photoFile != null) {
                //Uri 가져오기
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                //인텐트에 Uri담기
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.w(TAG, "파일 !" );
                //인텐트 실행
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }



    class DownloadPhotoFromURL extends AsyncTask<String, Integer, String> {
        int count;
        int lenghtOfFile = 0;
        InputStream Downlinput = null;
        OutputStream Downloutput = null;
        String tempFileName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setProgress(0);
        }

        @Override
        protected String doInBackground(String... params) {
            tempFileName = params[1];
            file = new File(dir, params[1]); // 다운로드할 파일명
            try {
                URL url = new URL(params[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                lenghtOfFile = connection.getContentLength(); // 파일 크기를 가져옴

//                if (file.exists()) {
//                    file.delete();
//                    Log.d(TAG, "file deleted...");
//                }

                Downlinput = new BufferedInputStream(url.openStream());
                Downloutput = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;

                while ((count = Downlinput.read(data)) != -1) {
                    if (isCancelled()) {
                        Downlinput.close();
                        return String.valueOf(-1);
                    }
                    total = total + count;
                    if (lenghtOfFile > 0) { // 파일 총 크기가 0 보다 크면
                        publishProgress((int) (total * 100 / lenghtOfFile));
                    }
                    Downloutput.write(data, 0, count); // 파일에 데이터를 기록
                }

                Downloutput.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (Downlinput != null) {
                    try {
                        Downlinput.close();
                    }
                    catch(IOException ioex) {
                    }
                }
                if (Downloutput != null) {
                    try {
                        Downloutput.close();
                    }
                    catch(IOException ioex) {
                    }
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // 백그라운드 작업의 진행상태를 표시하기 위해서 호출하는 메소드
            //progressBar.setProgress(progress[0]);
            //textView.setText("다운로드 : " + progress[0] + "%");
        }

        protected void onPostExecute(String result) {

            // pdLoading.dismiss();
            if (result == null) {
                // Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다.", Toast.LENGTH_LONG).show();
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                dialog.setTitle("DownLoed");
                dialog.setMessage("다운로드 완료");

                dialog.show();


                File file = new File(dir + "/" + tempFileName);
                //이미지 스캔해서 갤러리 업데이트
                Log.w(TAG,"다운완료 :"+dir);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath() );
                img_file.setImageBitmap(photoBitmap);
            } else {
                // Toast.makeText(getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void downLoad(String fileImg) {
            String imgUrl = "http://192.168.0.67/uploads/"+fileImg;
            FileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1, imgUrl.length());
            DownloadPhotoFromURL downloadPhotoFromURL = new DownloadPhotoFromURL();

            try {
                Log.d(TAG, "FileName :" + FileName);
                MakePhtoDir();
                // 동일한 파일이 있는지 검사
                if (new File(dir.getPath() + File.separator + FileName).exists() == false) {
                    downloadPhotoFromURL.execute(imgUrl, FileName);
                    Log.w(TAG, "imgUrl :" + imgUrl);
                } else {
                    Toast.makeText(context, "파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();
                    File file = new File(dir + "/" + FileName);
                    Log.d(TAG, "file :" + file);
                    Bitmap photoBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    img_file.setImageBitmap(photoBitmap);
                }
            } catch (Exception e) {
                Log.w(TAG, "ERROR!", e);
            }
        }

    private void MakePhtoDir() {

            //savePath = "/Android/data/" + getPackageName();
            //dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), savePath);
            dir = new File(Environment.getExternalStorageDirectory(), savePath );
            if (!dir.exists())
                dir.mkdirs(); // make dir
    }


    class InsertData extends AsyncTask<String,Void,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);

            Log.d(TAG,"progressDialog :" +progressDialog);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Log.d(TAG,"result : "+result);
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = (String) params[0];

            String subject=(String) params[1];
            String author = (String)params[2];
            String content = (String)params[3];
            String fileImg = (String)params[4];
            String postParameters ="subject="+subject+"&author="+ author
                    +"&content="+content+"&fileImg="+fileImg;

            Log.d(TAG,"postParameters : "+postParameters);
            String URL = "http://"+IP_ADDRESS+"/android_log_insert1.php?" + postParameters;
            try {
                URL url = new URL(URL);
                Log.i(TAG,"serverURL : "+URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000); //5초안에 응답이 오지 않으면 예외가 발생한다.

                httpURLConnection.setConnectTimeout(5000); //5초안에 연결이 안되면 예외가 발생한다.

                httpURLConnection.setRequestMethod("POST"); //요청 방식을 POST로 한다.

                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();

                //전송할 데이터가 저장된 변수를 이곳에 입력한다. 인코딩을 고려해줘야 하기 때문에 UTF-8 형식으로 넣어준다.
                outputStream.write(postParameters.getBytes("UTF-8"));

                Log.d(TAG,"php postParameters_데이터 : "+postParameters); //postParameters의 값이 정상적으로 넘어왔나 Log를 찍어줬다.

                outputStream.flush();//현재 버퍼에 저장되어 있는 내용을 클라이언트로 전송하고 버퍼를 비운다.
                outputStream.close(); //객체를 닫음으로써 자원을 반납한다.


                int responseStatusCode = httpURLConnection.getResponseCode(); //응답을 읽는다.
                Log.d(TAG, "POST response code-" + responseStatusCode);

                InputStream inputStream;

                if(responseStatusCode == httpURLConnection.HTTP_OK){ //만약 정상적인 응답 데이터 라면
                    inputStream=httpURLConnection.getInputStream();
                    Log.d(TAG,"정상 :" +inputStream);
                    Log.d(TAG,"정상적으로 출력"); //로그 메세지로 정상적으로 출력을 찍는다.
                }
                else {
                    inputStream = httpURLConnection.getErrorStream(); //만약 에러가 발생한다면
                    Log.d(TAG,"비정상적으로 출력"); // 로그 메세지로 비정상적으로 출력을 찍는다.
                }

                // StringBuilder를 사용하여 수신되는 데이터를 저장한다.
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                Log.d(TAG,"inputStreamReader :"+inputStreamReader);
                Log.d(TAG,"bufferedReader :"+bufferedReader);

                StringBuilder sb = new StringBuilder();
                String line = "";

                while ((line = bufferedReader.readLine()) !=null ) {
                    sb.append(line);
//                    sb.append("<br>");


                    Log.d(TAG,"Builder :"+sb);
                }

                bufferedReader.close();

                Log.d(TAG,"php 값 :"+sb.toString());


                //저장된 데이터를 스트링으로 변환하여 리턴값으로 받는다.
                return  sb.toString();


            }

            catch (Exception e) {

                Log.d(TAG, "InsertData: Error",e);

                return  new String("Error " + e.getMessage());

            }

        }


    }
    //권한 확인
    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        //권한이 없으면 권한 요청
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.CAMERA)) {
                //  Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // 권한이 취소되면 result 배열은 비어있다.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "권한확인");

                } else {
                    Log.w(TAG, "권한없음");

                }
            }
        }
    }
    /**********  File Path *************/
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "Capture_"+timeStamp ; //ex) Capture_20230704_
    String filename = imageFileName + ".jpg";


    final String uploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/capture/";
    final String uploadFileName = filename ; //전송하고자하는 파일 이름




    public int uploadFile(String sourceFileUri) {

        Log.d(TAG, "파일 : " + sourceFileUri);
        Log.d(TAG, "파일이름 : " + uploadFileName);
        String fileName1 = sourceFileUri;


        HttpURLConnection conn = null;

        DataOutputStream dos = null;

        String lineEnd = "\r\n";

        String twoHyphens = "--";

        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;

        byte[] buffer;

        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(sourceFileUri);
        Log.w(TAG, "소스파일 : " + sourceFile);


        if (!sourceFile.isFile()) {


//            dialog.dismiss();


            Log.e("uploadFile", "Source File not exist :"

                    + uploadFilePath + "" + uploadFileName);





            return 0;


        } else

            try {


                // open a URL connection to the Servlet

                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                URL url = new URL(upLoadServerUri);
                Log.w(TAG, "uploadurl : " + url);
                fileName1 =uploadFilePath+uploadFileName;
                // Open a HTTP  connection to  the URL

                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true); // Allow Inputs

                conn.setDoOutput(true); // Allow Outputs

                conn.setUseCaches(false); // Don't use a Cached Copy

                conn.setRequestMethod("POST");

                conn.setRequestProperty("Connection", "Keep-Alive");

                conn.setRequestProperty("ENCTYPE", "multipart/form-data");

                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                conn.setRequestProperty("uploaded_file", fileName1);
                Log.w(TAG, "uploaded_file : " + fileName1);

                dos = new DataOutputStream(conn.getOutputStream());


                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""

                        + fileName1 + "\"" + lineEnd);


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


                Log.w("uploadFile", "HTTP Response is : "

                        + serverResponseMessage + ": " + serverResponseCode);


                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {

                        public void run() {


                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"

                                    + uploadFileName;

                            Log.w("uploadFile", "HTTP Response is : "

                                    +  uploadFileName );


                        }

                    });





                    //close the streams //

                    fileInputStream.close();

                    dos.flush();

                    dos.close();

                }


                //close the streams //

                fileInputStream.close();

                dos.flush();

                dos.close();


            } catch (MalformedURLException ex) {


//                dialog.dismiss();

                ex.printStackTrace();


                runOnUiThread(new Runnable() {

                    public void run() {



                    }

                });


                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);

            } catch (Exception e) {




                e.printStackTrace();


                runOnUiThread(new Runnable() {

                    public void run() {


                    }

                });

                Log.e("Upload file to server Exception", "Exception : "

                        + e.getMessage(), e);

            }

//            dialog.dismiss();

        return serverResponseCode;


    } // End else block

   //       return 0;

}


