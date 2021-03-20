package com.htetznaing.boycottchina.activities;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.esafirm.imagepicker.model.Image;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.custom_interface.FuckDismissListener;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.htetznaing.boycottchina.Constants;
import com.htetznaing.boycottchina.ImagePickerProxy;
import com.htetznaing.boycottchina.MyApplication;
import com.htetznaing.boycottchina.R;
import com.htetznaing.boycottchina.async.TaskRunner;
import com.htetznaing.boycottchina.dialogs.MyMaterialDialog;
import com.htetznaing.boycottchina.dialogs.MyProgressDialog;
import org.jetbrains.annotations.NotNull;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class FullScannerActivity extends BaseActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String FIX_HUAWEI = "FIX_HUAWEI";
    private boolean mFlash,fixHuawei;
    private boolean mAutoFocus;
    private CoordinatorLayout snackContainer;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if(state != null) {
            fixHuawei = state.getBoolean(FIX_HUAWEI,false);
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
        } else {
            fixHuawei = false;
            mFlash = false;
            mAutoFocus = true;
        }

        setContentView(R.layout.activity_full_scanner);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        snackContainer = findViewById(R.id.snack_container);
        ViewGroup contentFrame = findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        setFormats();
        contentFrame.addView(mScannerView);

        // Option
        Toolbar option_toolbar = findViewById(R.id.option_toolbar);
        option_toolbar.inflateMenu(R.menu.camera_options);
        option_toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_flash:
                        mFlash = !mFlash;
                        if(mFlash) {
                            item.setIcon(R.drawable.ic_flash_on);
                        } else {
                            item.setIcon(R.drawable.ic_flash_off);
                        }
                        mScannerView.setFlash(mFlash);
                        return true;
                    case R.id.menu_auto_focus:
                        mAutoFocus = !mAutoFocus;
                        if(mAutoFocus) {
                            item.setIcon(R.drawable.ic_center_focus_on);
                        } else {
                            item.setIcon(R.drawable.ic_center_focus_off);
                        }
                        mScannerView.setAutoFocus(mAutoFocus);
                        return true;
                    case R.id.fix:
                        fixHuawei = !fixHuawei;
                        if (fixHuawei)
                            item.setIcon(R.drawable.ic_auto_fix_on);
                        else
                            item.setIcon(R.drawable.ic_auto_fix_off);
                        mScannerView.setAspectTolerance(fixHuawei ? 0.5f : 0.1f);
                        return true;
                    case R.id.insert:
                        startActivity(new Intent(getApplicationContext(), ImagePickerProxy.class));
                        return true;
                }
                return false;
            }
        });
    }

    private void setFormats(){
        List<BarcodeFormat> ALL_FORMATS = new ArrayList<>();
        ALL_FORMATS.add(BarcodeFormat.AZTEC);
        ALL_FORMATS.add(BarcodeFormat.CODABAR);
        ALL_FORMATS.add(BarcodeFormat.CODE_39);
        ALL_FORMATS.add(BarcodeFormat.CODE_93);
        ALL_FORMATS.add(BarcodeFormat.CODE_128);
        ALL_FORMATS.add(BarcodeFormat.DATA_MATRIX);
        ALL_FORMATS.add(BarcodeFormat.EAN_8);
        ALL_FORMATS.add(BarcodeFormat.EAN_13);
        ALL_FORMATS.add(BarcodeFormat.ITF);
        ALL_FORMATS.add(BarcodeFormat.MAXICODE);
        ALL_FORMATS.add(BarcodeFormat.PDF_417);
        ALL_FORMATS.add(BarcodeFormat.RSS_14);
        ALL_FORMATS.add(BarcodeFormat.RSS_EXPANDED);
        ALL_FORMATS.add(BarcodeFormat.UPC_A);
        ALL_FORMATS.add(BarcodeFormat.UPC_E);
        ALL_FORMATS.add(BarcodeFormat.UPC_EAN_EXTENSION);
        if(mScannerView != null) {
            mScannerView.setFormats(ALL_FORMATS);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        mScannerView.setFlash(mFlash);
        mScannerView.setAspectTolerance(fixHuawei ? 0.5f : 0.1f);
        mScannerView.setAutoFocus(mAutoFocus);
        if (Constants.selectedImage!=null){
            mScannerView.stopCameraPreview();
            Image cur = Constants.selectedImage;
            Constants.selectedImage = null;
            MyProgressDialog progressDialog = new MyProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.searching_barcode_from_photo));
            progressDialog.show();
            new TaskRunner().executeAsync(new Callable<String>() {
                @Override
                public String call() {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(cur.getUri());
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        return scanBarcodeImage(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }, new TaskRunner.Callback<String>() {
                @Override
                public void onTaskCompleted(String result) {
                    if (result!=null)
                        showResult(result);
                    else
                    {
                        cantRead();
                        tryAgain();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onTaskFailure(String error) {
                    progressDialog.dismiss();
                    cantRead();
                    tryAgain();
                }
            });

        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(FIX_HUAWEI, fixHuawei);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        showResult(rawResult.getText());
    }

    private void cantRead(){
        showCenterSnackBar(getString(R.string.no_barcode_from_photo));
        Constants.vibrateDevice(this);
    }

    private void showResult(String number){
        number = number.trim();
        if (!number.isEmpty() && number.matches("[0-9]+")) {
            boolean isChina = Constants.isChinaProducts(number);
            final boolean[] showIt = {true};
            String finalNumber = number;
            new MyMaterialDialog(this)
                    .setIcon(isChina ? R.drawable.ic_dangerous : R.drawable.ic_checked)
                    .setHeaderColorInt(isChina ? ContextCompat.getColor(this, R.color.main) : Constants.getAttr(this, R.attr.colorPrimary))
                    .setTitle(isChina ? R.string.is_china_product : R.string.is_not_china_product)
                    .withDialogAnimation(true)
                    .setMessage(MyApplication.markwon.toMarkdown(getString(isChina ? R.string.is_china_product_msg : R.string.is_not_china_product_msg)))
                    .onDismissed(new FuckDismissListener() {
                        @Override
                        public void dismissed() {
                            if (showIt[0])
                                tryAgain();
                        }
                    })
                    .setPositiveButton(R.string.ok, new MyMaterialDialog.OnClickedListener() {
                        @Override
                        public void clicked(@NotNull MaterialStyledDialog dialog) {

                        }
                    })
                    .setNegativeButton(R.string.how_to_detect, new MyMaterialDialog.OnClickedListener() {
                        @Override
                        public void clicked(@NotNull MaterialStyledDialog dialog) {
                            showIt[0] = false;
                            howIsDetect(isChina, finalNumber);
                        }
                    })
                    .show();
        }else notThisBarcode();
    }

    private void showCenterSnackBar(String msg){
        Snackbar snackbar = Snackbar.make(snackContainer, msg,Snackbar.LENGTH_SHORT);
        TextView txtView = snackbar.getView().findViewById(R.id.snackbar_text);
        if (txtView == null)
            txtView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);

        if (txtView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                txtView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            } else {
                txtView.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }
        snackbar.show();
    }

    private void notThisBarcode() {
        showCenterSnackBar(getString(R.string.not_this_barcode));
        Constants.vibrateDevice(this);
        tryAgain();
    }

    private void howIsDetect(boolean isChina,String barcode){
        new MyMaterialDialog(this)
                .onDismissed(new FuckDismissListener() {
                    @Override
                    public void dismissed() {
                        tryAgain();
                    }
                })
                .setHeaderColorInt(isChina ? ContextCompat.getColor(this,R.color.main) : Constants.getAttr(this,R.attr.colorPrimary))
                .withDialogAnimation(true)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setTitle(R.string.how_to_detect)
                .setMessage(MyApplication.markwon.toMarkdown(getString(R.string.why_is_china_product,barcode,getString(isChina ? R.string.is_china_product : R.string.is_not_china_product))))
                .setPositiveButton(R.string.ok, new MyMaterialDialog.OnClickedListener() {
                    @Override
                    public void clicked(@NotNull MaterialStyledDialog dialog) {

                    }
                })
                .show();
    }

    private void tryAgain() {
        mScannerView.resumeCameraPreview(this);
    }

    public static String scanBarcodeImage(Bitmap bMap) {
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            return result.getBarcodeFormat()!=BarcodeFormat.QR_CODE ? result.getText() : null;
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE,1,0,R.string.help);
        item.setIcon(R.drawable.ic_help);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==1){
            showScanTip();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void showScanTip() {
        mScannerView.stopCameraPreview();
        new MyMaterialDialog(this)
                .setTitle(R.string.help)
                .setHeaderDrawable(R.drawable.ic_help)
                .setMessage(MyApplication.markwon.toMarkdown(getString(R.string.scan_tip)))
                .withDialogAnimation(true)
                .onDismissed(new FuckDismissListener() {
                    @Override
                    public void dismissed() {
                        tryAgain();
                    }
                })
                .setPositiveButton(R.string.ok, new MyMaterialDialog.OnClickedListener() {
                    @Override
                    public void clicked(@NotNull MaterialStyledDialog dialog) {

                    }
                })
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}