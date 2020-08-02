package com.ncsu.dotscan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //Permission REQUEST_CODE
    private static final int PERMISSION_REQUEST_CODE = 23333;
    //Count
    private int count = 0;
    //Bitmap
    private Bitmap bitmap;

    // Declare
    private ImageView mIvDot;
    private Button mBtnImport;
    private Button mBtnScan;
    private Button mBtnAnalyze;
    private TextView mTvResult;

    //    Request Codes
    private final int REQUEST_IMAGE_CAPUTRE = 1;
    private final int REQUEST_IMAGE_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Request permission
        request();

    // Find
        //imageview
        mIvDot = findViewById(R.id.im_dot);
        mIvDot.setDrawingCacheEnabled(true);
        //Import Button asking user to import images either from camera or gallery
        mBtnImport = findViewById(R.id.btn_import);
        mBtnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select:");//Title
                builder.setMessage("Select images from:");//Message
                builder.setIcon(R.drawable.icon);//Icon
                builder.setCancelable(true);//Cancelable

                // Set Positive button for launching camera
                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // what happens if the user click camera
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                            mIvDot.destroyDrawingCache();
                            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPUTRE);
                            mIvDot.buildDrawingCache(true);
                            Toast.makeText(getApplicationContext(), "Please take pciture then click Import to import", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Set negative button for launching gallery
                builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mIvDot.destroyDrawingCache();
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryIntent.setType("image/^");
                        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
                        mIvDot.buildDrawingCache(true);
                        Toast.makeText(getApplicationContext(), "Please select pciture then click Import to import", Toast.LENGTH_LONG).show();
                    }
                });

                // Set Neutral Button for cancellation
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // Create the actual dialog
                AlertDialog dialog = builder.create();

                // Show the dialog;
                dialog.show();


            }
        });

        //Scan Chambers
        mBtnScan = findViewById(R.id.btn_scan);
        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bitmap = mIvDot.getDrawingCache();
                        getDot(bitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mIvDot.setImageBitmap(bitmap);
                                count =  getDot(bitmap);
                                mTvResult = findViewById(R.id.tv_result);
                                mTvResult.setText("Positive Chambers: " + count);
                            }
                        });
                    }
                }).start();
            }
        });

        //Analyze
        mBtnAnalyze = findViewById(R.id.btn_analyze);
        mBtnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("count", count);
                startActivity(intent);
            }
        });

    }



    //Display
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPUTRE) {
                bitmap = (Bitmap) data.getExtras().get("data");
                mIvDot.setImageBitmap(bitmap);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    mIvDot.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

//Request permission
    private void request() {
        boolean isAllGranted = checkPermissionAllGranted(
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );
        // All permissions have been granted
        if (isAllGranted) {
            Toast.makeText(getApplicationContext(), "Storage permission has been obtained!", Toast.LENGTH_LONG).show();
            return;
        }
        //Request permissions
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSION_REQUEST_CODE
        );
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Not all permissions have been granted
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // Determine whether all permissions have been granted
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // All permissions have been granted
                Toast.makeText(getApplicationContext(), "Storage permission has been obtained!", Toast.LENGTH_LONG).show();

            } else {
                // Not all permissions have been granted
                Toast.makeText(getApplicationContext(), "Please set app permissions on the settings page!", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Native_lib
    native int getDot(Object bitmap);
}
