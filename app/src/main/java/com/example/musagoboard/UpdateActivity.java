package com.example.musagoboard;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class UpdateActivity extends AppCompatActivity {
    private static final String TAG = "zzix";
    public static final int REQUEST_TAKE_PHOTO = 10;
    public static final int REQUEST_PERMISSION = 11;
    public final int GET_GALLERY_IMAGE = 200;
    AlertDialog.Builder alertdialog;
    String[] selectFile;
    private String mPhotoPath;
    int serverResponseCode = 0;
    private static String IP_ADDRESS = "192.168.0.150/eunhye"; //본인 IP주소를 넣으세요.
    Button btn_UpdateSave;
    Button btn_Img;
    EditText et_subject;
    EditText et_author;
    EditText et_content;
    ImageView img_file;

    String imgName = "";
    int seqNo = 0;


    public static Context context;
    private ProgressBar progressBar;
    private File file, dir;
    private String savePath = "Download/capture";
    private String FileName = null;
    String upLoadServerUri = "http://192.168.0.67/upload.php";//서버컴퓨터의 ip주소


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        context = this.getBaseContext();//앱 종료 후 메모리 유지를 피하기 위해서 getBaseContext를 사용.
        seqNo = getIntent().getIntExtra("seqNo",0);
        Log.i(TAG,"seqNo  :"+seqNo);
        imgName=getIntent().getStringExtra("fileImg");
         selectData();
        img_file=findViewById(R.id.img_file);
        btn_Img=findViewById(R.id.btn_Img);
        btn_Img.setOnClickListener(v -> Imgfile());
        et_subject=findViewById(R.id.et_subject);
        et_author=findViewById(R.id.et_author);
        et_content=findViewById(R.id.et_content);

        btn_UpdateSave=findViewById(R.id.btn_UpdateSave);
        btn_UpdateSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(TAG,"수정후 저장하기");
//                String subject = et_subject.getText().toString();
//                String author=et_author.getText().toString();
                String content=et_content.getText().toString().replaceAll("\n","<br>");
//                String fileImg=imgName;
               Log.d(TAG,"content  :"+content);

                updateDate task = new updateDate();
                task.execute(Integer.toString(seqNo), content);
                upDate();
                Intent intent = new Intent(getApplicationContext(), BoardListActivity.class);
                startActivity(intent);

            }
        });
    }

    private void upDate() {
        new Thread(new Runnable() {

            public void run() {

                upDateFile(uploadFilePath + "" + imgName);

                //  uploadFile(uploadFilePath + "" + "tmp.jpg");
                Log.w(TAG,"log :"+ "GO" );

            }

        }).start();
    }


    private void Imgfile() {
        selectFile = getResources().getStringArray(R.array.selectFile);
        alertdialog = new AlertDialog.Builder(UpdateActivity.this);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), BoardListActivity.class);
        startActivity(intent);
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


    private void selectData() {
        UpdateActivity.LoadBoard loadBoard = new UpdateActivity.LoadBoard();
        loadBoard.execute(String.valueOf(seqNo));
    }
    class LoadBoard extends AsyncTask<String, Void, String> {
        public LoadBoard() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "확인");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {


                // 결과값이 JSONArray 형태로 넘어오기 때문에
                // JSONArray, JSONObject 를 사용해서 파싱
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(result);
                String board = jsonObject.optString("board");
                Log.d(TAG, "****** board :" + board);

                JSONArray jsonArray = null;
//                jsonArray = new JSONArray(jsonObject.optString("board"));
                jsonArray = new JSONArray(board);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                    // Database 의 데이터들을 변수로 저장한 후 해당 TextView 에 데이터 입력
                    String subject = jsonObject1.optString("subject");
                    Log.d(TAG, "subject :" + subject);
                    String author = jsonObject1.optString("author");
                    Log.d(TAG, "author :" + author);
                    String content = jsonObject1.optString("content");
                    Log.d(TAG, "content :" + content);
                    String fileImg = jsonObject1.optString("fileImg");
                    Log.d(TAG, "fileImg :" + fileImg);

                    et_subject.setText(subject);
                    et_author.setText(author);
                    et_content.setText(content);
//
                    downLoad(fileImg);
//            }
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String seqNo = params[0];
            Log.i(TAG,"sqeNo : "+seqNo);

// 호출할 php 파일 경로
            String server_url = "http://"+IP_ADDRESS+"/android_log_select.php?seqNo="+seqNo;


            URL url;
            String response = "";
            try {
                url = new URL(server_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("seqNo", String.valueOf(seqNo));
                Log.i(TAG,"seqNo : "+seqNo);
                Log.i(TAG,"url : "+url);
                String query = builder.build().getEncodedQuery();

                Log.i(TAG,"builder : "+builder);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                Log.i(TAG,"query : "+query);

                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.i(TAG,"responseCode : "+responseCode);
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                        Log.i(TAG," response : "+ response);
                    }
                } else {
                    response = "";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(UpdateActivity.this);

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
        UpdateActivity.DownloadPhotoFromURL downloadPhotoFromURL = new UpdateActivity.DownloadPhotoFromURL();

        try {
            Log.d(TAG, "FileName :" + FileName);
            MakePhtoDir();
            // 동일한 파일이 있는지 검사
            if (new File(dir.getPath() + File.separator + FileName).exists() == false) {
                downloadPhotoFromURL.execute(imgUrl, FileName);
                Log.w(TAG, "imgUrl :" + imgUrl);
            } else {
                // Toast.makeText(context, "파일이 이미 존재합니다", Toast.LENGTH_SHORT).show();
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



    /**********  File Path *************/
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "Capture_"+timeStamp ; //ex) Capture_20230704_
    String filename = imageFileName + ".jpg";


    final String uploadFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/capture/";
    final String uploadFileName = filename ; //전송하고자하는 파일 이름




    public int upDateFile(String sourceFileUri) {

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

    class updateDate extends AsyncTask<String,Void,String> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(UpdateActivity.this,
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


            String content = "";
            String seqNo = "";

            try {
                seqNo = (String) params[0];
                content = (String)params[1];

            } catch (Exception e) {

            }


            Log.d("zzix","SEQ: " + seqNo + "         CONTENT: " + content);


            String postParameters ="&content="+content;

            Log.d(TAG,"postParameters : "+postParameters);
            String URL = "http://"+IP_ADDRESS+"/android_log_update.php?" + postParameters +"&seqNo="+seqNo;
            try {
                URL url = new URL(URL);
                Log.i("zzix","serverURL : "+URL);
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




}




