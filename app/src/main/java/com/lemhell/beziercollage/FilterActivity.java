package com.lemhell.beziercollage;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.lemhell.beziercollage.bezier.utils.GridManager;
import com.lemhell.beziercollage.filter.gestures.SandboxView;
import com.lemhell.beziercollage.filter.utils.FilterCreator;
import com.lemhell.beziercollage.filter.utils.ImageButtonFactory;
import com.lemhell.beziercollage.tourguide.Overlay;
import com.lemhell.beziercollage.tourguide.Pointer;
import com.lemhell.beziercollage.tourguide.ToolTip;
import com.lemhell.beziercollage.tourguide.TourGuide;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;

@EActivity
public class FilterActivity extends AppCompatActivity {

    public static int imageToSaveID = 1;
    public static boolean isPictureLoaded = false;

    private SandboxView ivImage;
    private HorizontalScrollView hsvFilters;
    private Bitmap image;
    private ProgressBar progressBar;
    private SeekBar seekBarBrightness, seekBarContrast;
    private String picturePath;
    private FilterCreator filterCreator;
    private LinearLayout llFilters;
    private int focusedId = -1, currentFilterId, initWidth, initHeight;
    private float brightness = 0.0f, contrast = 1.0f;
    private TourGuide guide;

    @ViewById ImageButton ibtnFilters;
    @ViewById ImageButton ibtnBrightness;
    @ViewById ImageButton ibtnContrast;

    private boolean isTutorialOn = false, isTutorialOnFilters = false, isTutorialOnBrightness = false,
            isTutorialOnContrast = false, isTutorialOnBack = false, isTutorialOnClose = false,
            isTutorialOnShare = false;
    private boolean isSaveBlocked = false, isCloseBlocked = false, isShareBlocked = false, isTutorialBlocked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_filter);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            picturePath = null;
        } else {
            picturePath = extras.getString("StringPicturePath");
            focusedId = extras.getInt("IntFocusedId");
        }
        init();
        setFilterView(scaleDown(image, 300, false));

        if (isPictureLoaded) {
            hsvFilters.setVisibility(View.VISIBLE);
        } else {
            hsvFilters.setVisibility(View.INVISIBLE);
        }
    }

    private void init() {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayoutFilter);
        hsvFilters = (HorizontalScrollView)findViewById(R.id.hsvFilters);
        image = getPicture();
        progressBar = new ProgressBar(this);
        seekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
        seekBarContrast = (SeekBar) findViewById(R.id.seekBarContrast);
        filterCreator = new FilterCreator(getApplicationContext());
        llFilters = (LinearLayout) findViewById(R.id.llFilters);
        if (ivImage == null) {
            ivImage = new SandboxView(this, image);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(initWidth, initHeight);
            params.gravity = LinearLayout.HORIZONTAL;
            ivImage.setLayoutParams(params);
            setProgressBarParams();
            frameLayout.addView(ivImage);
            frameLayout.addView(progressBar);
        } else {
            ivImage.setBitmap(image);
        }
        initBottom();
    }

    private void initBottom() {
        isPictureLoaded = true;
        ibtnFilters.setBackgroundDrawable(null);
        ibtnBrightness.setBackgroundDrawable(null);
        ibtnContrast.setBackgroundDrawable(null);

        seekBarBrightness.setMax(100);
        seekBarBrightness.setProgress(50);
        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness = (float) ((progress - 50) / 100.0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                processPic();
            }
        });

        seekBarContrast.setMax(200);
        seekBarContrast.setProgress(100);
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                contrast = (float) ((progress) / 100.0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                processPic();
            }
        });
    }

    @Background
    void processPic() {
        GPUImageFilterGroup group = new GPUImageFilterGroup();
        if (currentFilterId != -1)
            group.addFilter(filterCreator.getFilterByID(currentFilterId));
        group.addFilter(new GPUImageBrightnessFilter(brightness));
        group.addFilter(new GPUImageContrastFilter(contrast));

        GPUImage tempImage = new GPUImage(this);
        tempImage.setImage(image);
        tempImage.setFilter(group);
        tempImage.requestRender();
        updatePic(tempImage.getBitmapWithFilterApplied());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void updatePic(Bitmap bitmap) {
        ivImage.setBitmap(bitmap);
    }

    private Bitmap getPicture() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 4;
        Bitmap image;
        if (picturePath != null)
            image = BitmapFactory.decodeFile(picturePath, opts);
        else
            image = BitmapFactory.decodeResource(getResources(), R.drawable.abc_btn_rating_star_on_mtrl_alpha);
        image = scaleDown(image, GridManager.STEPX * 4, false);
        initWidth = image.getWidth();
        initHeight = image.getHeight();
        return image;
    }

    private void setProgressBarParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Uri uri = intent.getData();
            String filePathColumn[] = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            Bitmap image = BitmapFactory.decodeFile(picturePath, opts);
            ivImage.setBitmap(image);
            setFilterView(scaleDown(image, 300, false));
        }
    }

    private void setFilterView(Bitmap image) {
        HorizontalScrollView hsvFilters = (HorizontalScrollView)findViewById(R.id.hsvFilters);
        ImageButtonFactory factory = new ImageButtonFactory(getApplicationContext());
        llFilters = (LinearLayout)findViewById(R.id.llFilters);
        ImageButton btn = factory.createButton(image, null, getOnClickListener(-1));
        llFilters.addView(btn);
        for (int i = 0; i < filterCreator.NUMBER_OF_FILTERS; i++) {
            createButtonAndSetup(factory, image, i);
        }
        hsvFilters.setVisibility(View.VISIBLE);
    }

    @Background
    public void createButtonAndSetup(ImageButtonFactory factory, Bitmap image, int i) {
        View.OnClickListener listener = getOnClickListener(i);
        ImageButton btn = factory.createButton(image, filterCreator.getFilterByID(i), listener);
        addButtonToFilterView(btn);
    }

    @UiThread
    public void addButtonToFilterView(ImageButton btn) {
        llFilters.addView(btn);
    }

    private View.OnClickListener getOnClickListener(final int i) {
        return v -> {
            currentFilterId = i;
            processPic();
        };
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    public void menuShareOnClick(MenuItem item) {
        if (!isShareBlocked) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";

            Bitmap currImage = ivImage.getBitmap();
            currImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + imageFileName + ".jpg";
            File f = new File(path);
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            addImageToGallery(path, this);
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            startActivity(Intent.createChooser(share, "Share Image"));
        }
        if (isTutorialOn && isTutorialOnShare) {
            guide.cleanUp();
            resetTutorial();
        }
    }

    public void ibtnFilters(View view) {
        if (isTutorialOn && isTutorialOnFilters) {
            guide.cleanUp();
            guide.setToolTip(
                    new ToolTip()
                            .setTitle("Adjust brightness")
                            .setDescription("Tap on a brightness button and swipe the slider to adjust brightness of the picture")
                            .setGravity(Gravity.BOTTOM))
                    .playOn(ibtnBrightness);
            ibtnBrightness.getViewTreeObserver().dispatchOnGlobalLayout();
            isTutorialOnFilters = false;
            isTutorialOnBrightness = true;
            blockActionBar();
        }
        seekBarContrast.setVisibility(View.INVISIBLE);
        ibtnContrast.setImageDrawable(getResources().getDrawable(R.drawable.contrast_off2x));
        seekBarBrightness.setVisibility(View.INVISIBLE);
        ibtnBrightness.setImageDrawable(getResources().getDrawable(R.drawable.brightness_off2x));
        hsvFilters.setVisibility(View.VISIBLE);
        ibtnFilters.setImageDrawable(getResources().getDrawable(R.drawable.btn_effects_on2x));
    }


    public void ibtnBrightness(View view) {
        if (isTutorialOn && isTutorialOnBrightness) {
            guide.cleanUp();
            guide.setToolTip(
                    new ToolTip()
                            .setTitle("Adjust contrast")
                            .setDescription("Tap on a contrast button and swipe the slider to adjust contrast of the picture")
                            .setGravity(Gravity.BOTTOM))
                    .playOn(ibtnContrast);
            ibtnContrast.getViewTreeObserver().dispatchOnGlobalLayout();
            isTutorialOnBrightness = false;
            isTutorialOnContrast = true;
            blockActionBar();
        }
        seekBarContrast.setVisibility(View.INVISIBLE);
        ibtnContrast.setImageDrawable(getResources().getDrawable(R.drawable.contrast_off2x));
        seekBarBrightness.setVisibility(View.VISIBLE);
        ibtnBrightness.setImageDrawable(getResources().getDrawable(R.drawable.brightness_on2x));
        hsvFilters.setVisibility(View.INVISIBLE);
        ibtnFilters.setImageDrawable(getResources().getDrawable(R.drawable.btn_effects_off2x));
    }


    public void ibtnContrast(View view) {
        if (isTutorialOn && isTutorialOnContrast) {
            View menuItemGoToMain = findViewById(R.id.menuItemGoToMain);
            guide.cleanUp();
            guide.setToolTip(
                    new ToolTip()
                            .setTitle("Save and return to collage")
                            .setDescription("Tap on a save and return button to save picture and return to the collage")
                            .setGravity(Gravity.BOTTOM))
                    .setToolTipMargins(0, 130)
                    .setPointerMargins(0, 130)
                    .setPointerType(TourGuide.PointerType.Arrow)
                    .playOn(menuItemGoToMain);
            menuItemGoToMain.getViewTreeObserver().dispatchOnGlobalLayout();
            isTutorialOnContrast = false;
            isTutorialOnBack = true;
            blockActionBar();
        }
        seekBarContrast.setVisibility(View.VISIBLE);
        ibtnContrast.setImageDrawable(getResources().getDrawable(R.drawable.contrast_on2x));
        seekBarBrightness.setVisibility(View.INVISIBLE);
        ibtnBrightness.setImageDrawable(getResources().getDrawable(R.drawable.brightness_off2x));
        hsvFilters.setVisibility(View.INVISIBLE);
        ibtnFilters.setImageDrawable(getResources().getDrawable(R.drawable.btn_effects_off2x));
    }

    public void menuGoToMain(MenuItem item) {
        if (isTutorialOn && isTutorialOnBack) {
            View menuItemGoToMainWithoutChanges = findViewById(R.id.menuItemGoToMainWithoutChanges);
            guide.cleanUp();
            guide.setToolTip(
                    new ToolTip()
                            .setTitle("Return to collage without save")
                            .setDescription("Tap on a return button to return to the collage without save")
                            .setGravity(Gravity.BOTTOM))
                    .setToolTipMargins(0, 130)
                    .setPointerMargins(0, 130)
                    .setPointerType(TourGuide.PointerType.Arrow)
                    .playOn(menuItemGoToMainWithoutChanges);
            menuItemGoToMainWithoutChanges.getViewTreeObserver().dispatchOnGlobalLayout();
            isTutorialOnBack = false;
            isTutorialOnClose = true;
            blockActionBar();
        }
        if (!isSaveBlocked) {
            if (imageToSaveID != -1) {
                Bitmap image = ivImage.getBitmap();
                MainActivity.updatePic(focusedId, image);
                finish();
            }
        }
    }

    public void menuGoToMainWithoutChanges(MenuItem item) {
        if (isTutorialOn && isTutorialOnClose) {
            View menuItemShare = findViewById(R.id.menuItemShare);
            guide.cleanUp();
            guide.setToolTip(
                    new ToolTip()
                            .setTitle("Share edited picture")
                            .setDescription("Tap on a share button to share your picture. Thank you for visiting tutorial!")
                            .setGravity(Gravity.BOTTOM))
                    .setToolTipMargins(0, 130)
                    .setPointerMargins(0, 130)
                    .setPointerType(TourGuide.PointerType.Arrow)
                    .playOn(menuItemShare);
            menuItemShare.getViewTreeObserver().dispatchOnGlobalLayout();
            isTutorialOnClose = false;
            isTutorialOnShare = true;
            blockActionBar();
        }
        if (!isCloseBlocked) {
            finish();
        }
    }

    public void btnStartTutorialFilter(MenuItem item) {
        if (!isTutorialBlocked) {
            if (!isTutorialOn) {
                isTutorialOn = true;
                isTutorialOnFilters = true;
                guide = TourGuide.init(this)
                        .with(TourGuide.Technique.Click)
                        .setToolTip(new ToolTip()
                                .setTitle("Apply filters to the picture")
                                .setDescription("Tap filter button and choose filter to apply it to the picture"))
                        .setOverlay(new Overlay()
                                .disableClick(true))
                        .setPointer(new Pointer())
                        .playOn(ibtnFilters);
                ibtnFilters.getViewTreeObserver().dispatchOnGlobalLayout();
                blockActionBar();
            }
        }
    }

    private void resetTutorial() {
        isTutorialOn = false;
        isTutorialOnFilters = false;
        isTutorialOnBack = false;
        isTutorialOnBrightness = false;
        isTutorialOnContrast = false;
        isTutorialOnClose = false;
        isTutorialOnShare = false;
        isCloseBlocked = false;
        isSaveBlocked = false;
        isShareBlocked = false;
        isTutorialBlocked = false;
    }

    private void blockActionBar() {
        isSaveBlocked = true;
        isCloseBlocked = true;
        isShareBlocked = true;
        isTutorialBlocked = true;
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
