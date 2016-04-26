package com.jiaohuan.jiaohuan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateAccountCard extends Activity {

    private ImageView mTop;
    private ImageView mBottom;
    private TextView mBack;
    private TextView mNext;
    int REQUEST_CAMERA = 0;
    int SELECT_FILE = 1;
    private PopupWindow mPopupWindow;
    private LayoutInflater mLayoutInflater;
    private LinearLayout mLinearLayout;
    private Button mPreview;
    private Button mCamera;
    private Button mLocal;
    private Drawable mStartTop;
    private Drawable mStartBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_card);

        // Connect layouts and IDs
        mLinearLayout = (LinearLayout) findViewById(R.id.start_page);
        mTop = (ImageView) findViewById(R.id.top);
        mBack = (TextView) findViewById(R.id.back);
        mBottom = (ImageView) findViewById(R.id.bottom);
        mNext = (TextView) findViewById(R.id.next);
        mPreview = (Button) findViewById(R.id.pview);

        // Initialize PopUp window
        mLayoutInflater = (LayoutInflater) CreateAccountCard.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup mContainer = (ViewGroup) mLayoutInflater.inflate(R.layout.card_select_menu, null);

        // Buttons within the PopUp window
        mCamera = (Button) mContainer.findViewById(R.id.camera);
        mLocal = (Button) mContainer.findViewById(R.id.local);

        // These are the values of 'nothing'. They are used to see if 2 pictures are selected
        mStartTop = mTop.getDrawable();
        mStartBottom = mBottom.getDrawable();

        // Back button...
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Advance to next menu, checks to see if 2 pictures have been uploaded
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTop.getDrawable() == mStartBottom || mBottom.getDrawable() == mStartBottom){
                    Toast.makeText(getApplicationContext(), "You did not upload two pictures", Toast.LENGTH_SHORT).show();
                } else{
                    Intent intent = new Intent(getApplicationContext(), EnterDetailsActivity.class);
                    startActivityForResult(intent, 2);
                }
            }
        });

        // Open window for top card
        mTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTopWindow(mContainer);
            }
        });

        // Open window for bottom card
        mBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomWindow(mContainer);
            }
        });
    }

    // This will run after a picture is selected
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            // if picture is taken from camera
            if (requestCode == 1 || requestCode == 3) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.wtf("REQUEST", "" + requestCode);

                if(requestCode == 1){
                    mTop.setImageBitmap(thumbnail);
                } else if(requestCode == 3){
                    mBottom.setImageBitmap(thumbnail);
                }

                mPopupWindow.dismiss();
            }

            // if picture is selected from gallery
            else if (requestCode == 2 || requestCode == 4) {
                Uri selectedImageUri = data.getData();
                String[] projection = { MediaStore.MediaColumns.DATA };
                CursorLoader cursorLoader = new CursorLoader(this,selectedImageUri, projection, null, null,
                        null);
                Cursor cursor =cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);

                bm = Bitmap.createBitmap(bm, 0, 0, 400, 400);

                if(requestCode == 2){
                    mTop.setImageBitmap(bm);
                } else if(requestCode == 4){
                    mBottom.setImageBitmap(bm);
                }
                mPopupWindow.dismiss();

            }
        }
    }

    // TODO: Merge openTopWindow and openBottomWindow somehow

    public void openTopWindow(ViewGroup v){
        mPopupWindow = new PopupWindow(v, 1000, 400, true);

        mPopupWindow.showAtLocation(mLinearLayout, Gravity.CENTER_HORIZONTAL, 0, 0);

        mCamera.setBackgroundColor(Color.WHITE);
        mLocal.setBackgroundColor(Color.WHITE);

        // When anywhere is tapped, the pop up dismisses
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPopupWindow.dismiss();
                return true;
            }
        });

        // Listener for camera button
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int permissionCheck = ContextCompat.checkSelfPermission(CreateAccountCard.this,
                        Manifest.permission.CAMERA);

                Log.wtf("Perms", "" + permissionCheck);

                Boolean thing = hasPermissionInManifest(getApplicationContext(), MediaStore.ACTION_IMAGE_CAPTURE);

                Log.wtf("PERMISSIONS", "" + thing);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);

            }
        });

        // Listener for select button
        mLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select File"),
                        2);
            }
        });
    }

    public void openBottomWindow(ViewGroup v){
        mPopupWindow = new PopupWindow(v, 1000, 400, true);

        mPopupWindow.showAtLocation(mLinearLayout, Gravity.CENTER_HORIZONTAL, 0, 0);

        mCamera.setBackgroundColor(Color.WHITE);
        mLocal.setBackgroundColor(Color.WHITE);

        // When anywhere is tapped, the pop up dismisses, it also resumes the shaker
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPopupWindow.dismiss();
                return true;
            }
        });

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 3);
                Box box = new Box(CreateAccountCard.this);
                addContentView(box, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
            }
        });
        mLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select File"),
                        4);
            }
        });
    }
    public boolean hasPermissionInManifest(Context context, String permissionName) {
        final String packageName = context.getPackageName();
        try {
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermisisons = packageInfo.requestedPermissions;
            if (declaredPermisisons != null && declaredPermisisons.length > 0) {
                for (String p : declaredPermisisons) {
                    if (p.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return false;
    }
}

