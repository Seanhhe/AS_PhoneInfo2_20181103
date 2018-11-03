package tw.sean.as_phoneinfo2_20181103;

/*
 * 01.取得權限
 *      <uses-permission android:name="android.permission.ACTION_MANAGE_OVERLAY_PERMISSION" />
 *      <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
 * 02.
 *      0931313992布來得手機
 * 03.讀取系統設定
 *      在init()加入if判斷
 * 04.
 */

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private TelephonyManager tmgr;
    private ContentResolver contentResolver;
    private ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                //Manifest.permission.READ_CONTACTS)
                //Manifest.permission.READ_CALL_LOG)
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS},
                    1);

            } else {
                init();
            }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void init() {
        tmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);

        contentResolver = getContentResolver();

        //搭配Write Setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.System.canWrite(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package: " + getPackageName()));
                startActivity(intent);
            }
        }
    }

    public void test1(View view){
        //安卓的Uri
        Uri uri = CallLog.Calls.CONTENT_URI;
        //String[] projection代表資料庫語法的欄位
        Cursor cursor = contentResolver.query(
                uri,null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            //String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            Long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
            String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

            //

            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            Date cdate = new Date(date);
            String calldate = sdf.format(cdate);

            Log.v("brad", name + ":" + number + ":" + type + ":" + date + ":" + duration);
        }

    }

    //聯絡人Uri
    public void test2(View view) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = ContentResolver.query(
                uri,null,null,null,null);
        //可找出前人留下的資料庫欄位
        String[] fields = cursor.getColumnNames();
        for (String field: fields){
            Log.v("brad",field);

        }
        //Log.v("brad", ContactsContract.CommonDataKinds.Phone.NUMBER);

        while (cursor.moveToNext()){
            String name =
                    cursor.getString(
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String number =
                   cursor.getString(
                           cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
    }

    public void test3(View view) {
        Uri uri = Settings.System.CONTENT_URI;
        //Log.v("brad", uri.toString());
        //把content uri物件撈出來
        //Uri uri2 = Uri.parse("content://settings/system");

        Cursor cursor = contentResolver.query(uri,
                null,null,null,null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String value = cursor.getString(cursor.getColumnIndex("value"));
            Log.v("brad", name + " => " + value);
        }

        Log.v("brad", Settings.System.FONT_SCALE);

    }
    //配合Settings.System.FONT_SCALE的值為String type
    //讀取系統的設定(還可延伸讀取來電紀錄，聯絡人等)
    private String getSettingValue(String name){
        String ret = "";

        Uri uri = Settings.System.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,
                new String[]{"name", "value"},
                "name = ?", new String[]{name},
                null);
        if (cursor.getCount()>0){
            cursor.moveToNext();
            ret = cursor.getString(cursor.getColumnIndex("value"));
        }
        //Log.v("brad",getSettingValue(Settings.System.FONT_SCALE));
        return ret;

    }

    //系統設定改亮度
    public void test4(View view) {
        Settings.System.putInt(
                contentResolver,Settings.System.SCREEN_BRIGHTNESS,127);
        contentResolver.notifyChange(Settings.System.CONTENT_URI,null);
        Log.v("brad",getSettingValue(Settings.System.SCREEN_BRIGHTNESS));
    }

    //讀取相片資料(及相關屬性)
    //權限開了，照片就能被傳到遠端(包含經緯度)
    public void test5(View view) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//取得外部URI
        Cursor cursor =contentResolver.query(uri,null,null,null,null);
        if (cursor.getCount()>0){
            cursor.moveToNext();
            String data = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.v("brad", data);

            Bitmap bmp = BitmapFactory.decodeFile(data);
            img.setImageBitmap(bmp);

        }

    }

    private class MyPhoneStateListener extends PhoneStateListener{

    }
}






