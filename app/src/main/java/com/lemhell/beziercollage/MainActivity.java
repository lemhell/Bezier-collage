package com.lemhell.beziercollage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lemhell.beziercollage.bezier.BezierCurve;
import com.lemhell.beziercollage.bezier.BezierView;
import com.lemhell.beziercollage.bezier.sticker.MultiTouchListener;
import com.lemhell.beziercollage.bezier.utils.DataReader;
import com.lemhell.beziercollage.bezier.utils.GridManager;
import com.lemhell.beziercollage.bezier.utils.ListHolder;
import com.lemhell.beziercollage.recycler.MyLinearLayoutManager;
import com.lemhell.beziercollage.recycler.RecyclerAdapter;
import com.lemhell.beziercollage.recycler.RecyclerItem;
import com.lemhell.beziercollage.tourguide.Overlay;
import com.lemhell.beziercollage.tourguide.Pointer;
import com.lemhell.beziercollage.tourguide.ToolTip;
import com.lemhell.beziercollage.tourguide.TourGuide;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String BROADCAST_STARTMOVE = "BRSTARTMOVE";
    public static final String BROADCAST_ENDMOVE = "BRENDMOVE";
    public static final String BROADCAST_DELETE = "BRDELETE";

    public static boolean isTutorialOn = false;

    private final int NUMBER_OF_FRAMES = 11;
    private final int NUMBER_OF_STICKERS = 10;

    private Point size;

    private static ArrayList<BezierView> views;
    private RelativeLayout rlMain;
    private GridManager manager;
    private ActionMode.Callback mActionMode;
    private RecyclerView rvMain;
    private RecyclerAdapter adapterFrame, adapterSticker;
    private int focusedId, lastX, lastY, onLongClickID = -1, focusedIdMove = -1;
    private int beforeLastX, beforeLastY, startMoveX, startMoveY, stickerIdCounter = 1337;
    private boolean isMoving = false, isMoveBorderMode = false, isCurvesEnabled = true,
            isTutorialOnViews = false, isTutorialOnFrame = false, isTutorialOnSearch = false,
            isTutorialOnBorders = false, isTutorialOnEditPic = false, isTutorialOnStickers = false;
    private ImageView ivFrame, ivButtonFrames, ivButtonStickers;
    public TourGuide mTutorialHandler;

    private ArrayList<ImageView> stickers = new ArrayList<>();

    private boolean isSearchBlocked = false, isBordersBlocked = false, isEditBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        init();

    }

    private void setUpBroadCast() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverDelete,
                new IntentFilter(BROADCAST_DELETE));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverStartMove,
                new IntentFilter(BROADCAST_STARTMOVE));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiverEndMove,
                new IntentFilter(BROADCAST_ENDMOVE));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiverFrame,
                new IntentFilter(RecyclerAdapter.BROADCAST_ONCLICK_FRAME));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiverSticker,
                new IntentFilter(RecyclerAdapter.BROADCAST_ONCLICK_STICKER));
    }

    private BroadcastReceiver mMessageReceiverDelete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("IntId", 0);
            showDeleteStickerDialog(findViewById(id));
        }
    };

    private BroadcastReceiver mMessageReceiverStartMove = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("IntId", 0);
            ImageView iv = (ImageView) findViewById(id);
            iv.setBackground(getResources().getDrawable(R.drawable.sticker_border));
        }
    };

    private BroadcastReceiver mMessageReceiverEndMove = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int id = intent.getIntExtra("IntId", 0);
            ImageView iv = (ImageView) findViewById(id);
            iv.setBackground(null);
        }
    };

    private BroadcastReceiver receiverFrame = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("IntId", 0);
            if (id == R.drawable.no_frame_eng) id = R.drawable.empty_frame;
            Picasso.with(getApplicationContext())
                    .load(id)
                    .resize(GridManager.STEPX * 4, GridManager.STEPX * 4)
                    .into(ivFrame);
            ivFrame.bringToFront();
        }
    };

    private BroadcastReceiver receiverSticker = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("IntId", 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            showDialogForStickers(params, getColorById(id));
        }
    };

    private void setUpFrameAndStickers() {
        List<RecyclerItem> recyclerItemsFrames = new ArrayList<>();
        List<RecyclerItem> recyclerItemsStickers = new ArrayList<>();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        for (int i = 0; i < NUMBER_OF_FRAMES; i++) {
            recyclerItemsFrames.add(new RecyclerItem(getDrawableFrameIdByNumber(i), size.y / 11));
        }

        for (int i = 0; i < NUMBER_OF_STICKERS; i++) {
            recyclerItemsStickers.add(new RecyclerItem(getDrawableStickerIdByNumber(i), size.y / 11));
        }

        rvMain = (RecyclerView) findViewById(R.id.rvMain);
        rvMain.setHasFixedSize(true);
        LinearLayoutManager llManagerStatic = new MyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        final ViewGroup.LayoutParams lp = rvMain.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.height = size.y / 11;
        rvMain.setLayoutParams(lp);
        rvMain.setLayoutManager(llManagerStatic);

        adapterFrame = new RecyclerAdapter(getApplicationContext(), recyclerItemsFrames, RecyclerAdapter.EVENT_TYPE.FRAME);
        adapterSticker = new RecyclerAdapter(getApplicationContext(), recyclerItemsStickers, RecyclerAdapter.EVENT_TYPE.STICKER);
        rvMain.setAdapter(adapterFrame);

        ivButtonFrames = (ImageView) findViewById(R.id.ivButtonFrames);
        Picasso.with(this)
                .load(R.drawable.frame_10)
                .resize((size.y - GridManager.STEPX * 4 - size.y / 10) / 4,
                        (size.y - GridManager.STEPX * 4 - size.y / 10) / 4)
                .into(ivButtonFrames);
        ivButtonStickers = (ImageView) findViewById(R.id.ivButtonStickers);
        Picasso.with(this)
                .load(R.drawable.edit_text)
                .resize((size.y - GridManager.STEPX * 4 - size.y / 10) / 4,
                        (size.y - GridManager.STEPX * 4 - size.y / 10) / 4)
                .into(ivButtonStickers);


    }

    private Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        try {
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawText(text, 0, baseline, paint);
            return image;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void init() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        GridManager.STEPX = (size.x - 90)/4;//2;
        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        views = new ArrayList<>();
        manager = new GridManager(this);
        ivFrame = (ImageView) findViewById(R.id.ivFrame);
        String filename = "templates/basic/template0.txt";
        Boolean isStaticFromIntent = false;
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            filename = extras.getString("StringFilename");
            isStaticFromIntent = extras.getBoolean("BooleanIsStatic");
        }
        DataReader reader = new DataReader(getAssets(), filename);
        for (ListHolder holder : reader.holderList) {
            views.add(new BezierView(this, GridManager.STEPX * 2, GridManager.STEPX * 2, holder, manager));
        }
        manager.setViews(views);
        for (BezierView v : views) {
            setListenersForViews(v);
            v.isStatic = isStaticFromIntent;
        }
        focusedId = -1;
        mActionMode = getActionMode();
        setUpRelativeLayout();
        setUpFrameAndStickers();
        setUpBroadCast();
        isTutorialOn = false;
        if (mTutorialHandler != null) {
            mTutorialHandler.cleanUp();
            mTutorialHandler = null;
        }
    }

    private void setUpRelativeLayout() {
        for (int i = 0; i < views.size(); i++) {
            BezierView view = views.get(i);
            view.mScreenWidth = GridManager.STEPX * 2;
            view.mScreenHeight = GridManager.STEPX * 2;
            view.setId(i);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rlMain.addView(view);
        }

    }

    @NonNull
    private ActionMode.Callback getActionMode() {
        return new ActionMode.Callback() {
            Toast toast;
            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.action_bar_focused_menu, menu);
                focusedId = onLongClickID;
                return true;
            }
            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_search: {
                        if (!isSearchBlocked) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, focusedId);
                        }
                        if (isTutorialOn && isTutorialOnSearch) {
                            View borderView = findViewById(R.id.action_setMoveBorder);
                            mTutorialHandler.cleanUp();
                            mTutorialHandler
                                    .setToolTip(new ToolTip()
                                            .setBackgroundColor(R.color.green)
                                            .setTitle("Move borders of collage")
                                            .setDescription("Press border button to enable of disable moving of borders")
                                            .setGravity(Gravity.BOTTOM))
                                    .setPointerMargins(0, 130)
                                    .setToolTipMargins(0, 100)
                                    .setPointerType(TourGuide.PointerType.Arrow)
                                    .playOn(borderView);
                            borderView.getViewTreeObserver().dispatchOnGlobalLayout();
                            isTutorialOnSearch = false;
                            isTutorialOnBorders = true;

                            isEditBlocked = true;
                            isSearchBlocked = false;
                            isBordersBlocked = true;
                        }
                    }
                    return true;
                    case R.id.action_setMoveBorder: {
                        if (!isBordersBlocked) {
                            isMoveBorderMode = !isMoveBorderMode;
                            if (isMoveBorderMode) {
                                toast = Toast.makeText(MainActivity.this, "You can move borders now", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                toast = Toast.makeText(MainActivity.this, "You can't move borders now", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                        if (isTutorialOn && isTutorialOnBorders) {
                            View editView = findViewById(R.id.action_editPic);
                            mTutorialHandler.cleanUp();
                            mTutorialHandler
                                    .setToolTip(new ToolTip()
                                            .setBackgroundColor(R.color.green)
                                            .setTitle("Edit picture")
                                            .setDescription("Press edit button to apply filters to picture and change" +
                                                    " brightness and contrast. Also, in this mode you can resize picture " +
                                                    "using pinch to zoom gesture.")
                                            .setGravity(Gravity.BOTTOM))
                                    .setPointerMargins(0, 130)
                                    .setToolTipMargins(0, 100)
                                    .setPointerType(TourGuide.PointerType.Arrow)
                                    .playOn(editView);
                            editView.getViewTreeObserver().dispatchOnGlobalLayout();
                            isTutorialOnBorders = false;
                            isTutorialOnEditPic = true;

                            isEditBlocked = false;
                            isSearchBlocked = true;
                            isBordersBlocked = true;
                        }

                        return true;
                    }
                    case R.id.action_editPic: {
                        if (!isEditBlocked) {
                            Intent intent = new Intent(getApplicationContext(), FilterActivity_.class);
                            String strName = views.get(focusedId).getPicturePath();
                            if (strName.equals("null")) {
                                toast = Toast.makeText(MainActivity.this, "You should load picture first", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                intent.putExtra("StringPicturePath", strName);
                                intent.putExtra("IntFocusedId", focusedId);
                                startActivity(intent);
                            }
                        }
                        if (isTutorialOn && isTutorialOnEditPic) {
                            mTutorialHandler
                                    .setToolTip(new ToolTip()
                                            .setBackgroundColor(R.color.green)
                                            .setTitle("Add frame")
                                            .setDescription("Press frame button and selected frame to add it to the collage")
                                            .setGravity(Gravity.TOP))
                                    .playOn(ivButtonFrames)
                            ;
                            mTutorialHandler.cleanUp();
                            ivButtonFrames.getViewTreeObserver().dispatchOnGlobalLayout();
                            isTutorialOnEditPic = false;
                            isTutorialOnFrame   = true;

                            isEditBlocked       = false;
                            isSearchBlocked     = false;
                            isBordersBlocked    = false;
                        }
                    }
                    return true;
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isMoveBorderMode = false;
                focusedId = -1;
                mActionMode = null;

                for (ImageView v: stickers) {
                    if (v != null) {
                        v.bringToFront();
                    }
                }
                if (mTutorialHandler != null) {
                    mTutorialHandler.cleanUp();
                    isTutorialOn = false;
                    isTutorialOnSearch = false;
                    isTutorialOnBorders = false;
                    isTutorialOnEditPic = false;
                }
            }
        };
    }

    public static void updatePic(int focusedId, Bitmap image) {
        views.get(focusedId).setImageToClip(image);
    }

    private void setListenersForViews(BezierView v) {
        v.setOnTouchListener((v1, ev) -> {
            int id, shiftX = 0, shiftY = 0;
            final float x = ev.getX() + shiftX, y = ev.getY() + shiftY;
            beforeLastX = lastX;
            beforeLastY = lastY;
            lastX = (int) x;
            lastY = (int) y;
            id = getIdContainingPoint((int) x, (int) y);
            if (focusedIdMove != -1) {
                id = focusedIdMove;
            }
            if (focusedId != -1) {
                id = focusedId;
            }

            if (id != -1) {
                v1 = views.get(id);
                focusedIdMove = id;
                final int action = ev.getActionMasked();
                final float dx = x - ((BezierView) v1).mLastTouchX, dy = y - ((BezierView) v1).mLastTouchY;
                Iterator<BezierCurve> iterator = ((BezierView) v1).curveHolder.curves.iterator();
                switch (action) {
                    case MotionEvent.ACTION_DOWN: {
                        startMoveX = (int) ev.getX();
                        startMoveY = (int) ev.getY();
                        ((BezierView) v1).mLastTouchX = x;
                        ((BezierView) v1).mLastTouchY = y;
                        if (!((BezierView) v1).isStatic) {
                            for (; iterator.hasNext(); ) {
                                BezierCurve curve = iterator.next();
                                if (!((BezierView) v1).sthSelected) {
                                    ((BezierView) v1).sthSelected = curve.checkPoints(x, y);
                                } else {
                                    break;
                                }
                            }
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        isMoving = true;
                        for (BezierCurve curve : ((BezierView) v1).curveHolder.curves) {
                            if (!((BezierView) v1).sthSelected && !((BezierView) v1).isStatic) {
                                ((BezierView) v1).sthSelected = curve.checkPoints(x, y);
                            }
                        }
                        if (!((BezierView) v1).sthSelected) {

                            ((BezierView) v1).mPosX += dx;
                            ((BezierView) v1).mPosY += dy;
                            if (((BezierView) v1).mPosX > GridManager.STEPX * 4 + 100)
                                ((BezierView) v1).mPosX = GridManager.STEPX * 4 + 100;
                            if (((BezierView) v1).mPosY > GridManager.STEPX * 4 + 100)
                                ((BezierView) v1).mPosY = GridManager.STEPX * 4 + 100;
                            if (((BezierView) v1).mPosX < -300) ((BezierView) v1).mPosX = -300;
                            if (((BezierView) v1).mPosY < -300) ((BezierView) v1).mPosY = -300;
                        }
                        int xt = (int) x, yt = (int) y, t1 = 0;
                        for (; iterator.hasNext(); t1++) {
                            BezierCurve curve = iterator.next();
                            ((BezierView) v1).polygon.setPoly(2 * t1 + 1, (int) curve.controlPoint.getX(), (int) curve.controlPoint.getY());
                            if (x > GridManager.STEPX * 4) xt = GridManager.STEPX * 4;
                            if (y > GridManager.STEPX * 4) yt = GridManager.STEPX * 4;
                            if (x < 0) xt = 0;
                            if (y < 0) yt = 0;
                            if (isMoveBorderMode && ((BezierView) v1).getSelectedCurveId() == t1)
                                curve.moveBorder(dx, dy);
                            else if (focusedId != -1)
                                curve.move(xt, yt);
                        }
                        v1.invalidate();
                        ((BezierView) v1).mLastTouchX = xt;
                        ((BezierView) v1).mLastTouchY = yt;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        for (; iterator.hasNext(); ) {
                            BezierCurve curve = iterator.next();
                            curve.resetSelected();
                        }

                        float length = (float) Math.sqrt(Math.pow((beforeLastX - startMoveX), 2) + Math.pow((beforeLastY - startMoveY), 2));
                        if (!isMoving || length <= 30) {
                            int id1;
                            final float x1 = lastX;
                            final float y1 = lastY;
                            id1 = getIdContainingPoint((int) x1, (int) y1);
                            onLongClickID = id1;
                            if (mActionMode == null) {
                                mActionMode = getActionMode();
                                startActionMode(mActionMode);
                            } else {
                                mActionMode = null;
                            }
                            if (isTutorialOnViews && isTutorialOn && mTutorialHandler != null) {
                                mActionMode = getActionMode();
                                startActionMode(mActionMode);
                                View searchView = findViewById(R.id.action_resetPoints);
                                mTutorialHandler.cleanUp();
                                mTutorialHandler.setToolTip(
                                        new ToolTip()
                                                .setBackgroundColor(R.color.green)
                                                .setTitle("Add photo")
                                                .setDescription("Press search button to add a photo to the collage")
                                                .setGravity(Gravity.BOTTOM))
                                        .setPointerMargins(0, 130)
                                        .setToolTipMargins(0, 100)
                                        .setPointerType(TourGuide.PointerType.Arrow)
                                        .playOn(searchView);
                                searchView.getViewTreeObserver().dispatchOnGlobalLayout();
                                isTutorialOnViews = false;
                                isTutorialOnSearch = true;
                                isTutorialOn = true;
                            }
                        }
                        ((BezierView) v1).sthSelected = false;
                        isMoving = false;
                        focusedIdMove = -1;
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;
                }
            }
            v1.invalidate();
            manager.updateViews();
            return false;
        });
        v.setOnLongClickListener(v1 -> {
            if (!isMoving) {
                int id;
                final float x = lastX;
                final float y = lastY;
                id = getIdContainingPoint((int) x, (int) y);
                if (id == -1) id = getSimpleIdContainingPoint(x, y);
                onLongClickID = id;
                if (mActionMode == null) {
                    mActionMode = getActionMode();
                }
                startActionMode(mActionMode);
            }
            return true;
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_resetPoints) {
            for (BezierView v : views) {
                v.resetPath();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent != null) {
            if (isInRange(0, views.size() - 1, requestCode) && resultCode == RESULT_OK) {
                Uri uri = intent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                try {
                    Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPurgeable = true;
                    opts.inInputShareable = true;
                    opts.inSampleSize = 2;

                    Bitmap image = BitmapFactory.decodeFile(picturePath, opts);
                    views.get(requestCode).setImageToClip(image);
                    views.get(requestCode).picturePath = picturePath;


                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed to load picture", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private boolean isInRange(int start, int end, int value) {
        return value >= start && value <= end;
    }

    private int getIdContainingPoint(int x, int y) {
        for (BezierView v : views) {
            if (v.polygon.contains(x, y))
                return v.getId();
        }
        return -1;
    }
    private int getSimpleIdContainingPoint(float x, float y) {
        int id;
        if (x <= GridManager.STEPX * 2) {
            if (y <= GridManager.STEPX * 2)
                id = 0;
            else
                id = 2;
        } else {
            if (y <= GridManager.STEPX * 2)
                id = 1;
            else
                id = 3;
        }
        return id;
    }

    public void menuShareOnClick(MenuItem item) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        if (isCurvesEnabled) {
            for (BezierView v : views) {
                v.isBoldStrokeWidth = true;
                v.isBallsEnabled = false;
                v.invalidate();
            }
        }
        rlMain.setDrawingCacheEnabled(true);
        Bitmap currImage = rlMain.getDrawingCache();

        if (isCurvesEnabled) {
            for (BezierView v : views) {
                v.isBoldStrokeWidth = false;
                v.isBallsEnabled = true;
                v.invalidate();
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        currImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        final String path = Environment.getExternalStorageDirectory() + File.separator + imageFileName + ".jpg";
        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaStore.Images.Media.insertImage(getContentResolver(), currImage, "", "");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        startActivity(Intent.createChooser(share, "Share Image"));
    }

    public void btnAddFrame(View view) {
        rvMain.setAdapter(adapterFrame);
        if (isTutorialOnFrame && isTutorialOn) {
            mTutorialHandler.cleanUp();

            mTutorialHandler.cleanUp();
            mTutorialHandler.setToolTip(new ToolTip()
                    .setBackgroundColor(R.color.green)
                    .setTitle("Add text stickers")
                    .setDescription("Press text sticker button and select color to add colored text sticker to the collage." +
                            "\nLater you can move and resize it with your fingers or delete it by tapping on it and pressing delete button.")
                    .setGravity(Gravity.TOP))
                    .playOn(ivButtonStickers);
            ivButtonStickers.getViewTreeObserver().dispatchOnGlobalLayout();
            isTutorialOnFrame = false;
            isTutorialOnStickers = true;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mMessageReceiverDelete);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mMessageReceiverEndMove);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mMessageReceiverStartMove);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(receiverFrame);
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(receiverSticker);
        for (BezierView view : views) {
            view.destroyDrawingCache();
        }
        ivFrame = null;
    }

    public void btnAddTextSticker(View view) {
        rvMain.setAdapter(adapterSticker);
        rvMain.getLayoutParams().width = size.x;
        if (isTutorialOn && isTutorialOnStickers) {
            mTutorialHandler.cleanUp();
            isTutorialOn = false;
            mTutorialHandler = null;
        }
    }

    private void showDialogForStickers(final RelativeLayout.LayoutParams params, final int color) {
        final String[] result = new String[1];
        result[0] = "";
        runOnUiThread(() -> {
            LayoutInflater li = LayoutInflater.from(MainActivity.this);
            View promptsView = li.inflate(R.layout.text_sticker_dialog, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    MainActivity.this, R.style.StickerDialog);
            alertDialogBuilder.setView(promptsView);

            final EditText userInput = (EditText) promptsView
                    .findViewById(R.id.editTextDialogUserInput);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            (dialog, id) -> {
                                result[0] = userInput.getText().toString();
                                if (result[0].trim().length() > 0 || result[0].length() < 30) {
                                    ImageView sticker = new ImageView(getApplicationContext());
                                    RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlMain);
                                    Bitmap b = textAsBitmap(result[0], 200, color);
                                    if (b != null) {
                                        sticker.setImageBitmap(b);
                                        sticker.setId(stickerIdCounter);
                                        stickerIdCounter++;
                                        sticker.setOnClickListener(this::showDeleteStickerDialog);
                                        sticker.setOnTouchListener(new MultiTouchListener(getApplicationContext(), sticker.getId()));
                                        stickers.add(sticker);
                                        rl.addView(sticker, params);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Invalid text input", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (result[0].length() > 30) {
                                    Toast.makeText(getApplicationContext(), "The text is too long", Toast.LENGTH_SHORT).show();
                                } else if (result[0].trim().length() <= 0) {
                                    Toast.makeText(getApplicationContext(), "Enter something", Toast.LENGTH_SHORT).show();
                                }
                            })
                    .setNegativeButton("Cancel", (dialog, id) -> {
                        dialog.cancel();
                    })
                    .create()
                    .show();
        });
    }

    private void showDeleteStickerDialog(final View v) {
        runOnUiThread(() -> {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
            adBuilder
                    .setTitle("Delete sticker")
                    .setCancelable(false)
                    .setPositiveButton("Delete",
                            (dialog, which) -> {
                                ((RelativeLayout) v.getParent()).removeView(v);
                            })
                    .setNegativeButton("Cancel",
                            (dialog, which) -> {
                                dialog.cancel();
                            })
                    .create()
                    .show();
        });
    }

    public void menuGoToTemplate(MenuItem item) {
        Intent intent = new Intent(this, TemplateActivity.class);
        finish();
        if (mTutorialHandler != null) {
            mTutorialHandler.cleanUp();
            mTutorialHandler = null;
            isTutorialOn = false;
        }
        startActivity(intent);
    }

    private int getColorById(int id) {
        switch (id) {
            case R.drawable.blank : return Color.BLACK;
            case R.drawable.blue : return Color.BLUE;
            case R.drawable.cyan : return Color.CYAN;
            case R.drawable.green : return Color.GREEN;
            case R.drawable.magenta : return Color.MAGENTA;
            case R.drawable.red: return Color.RED;
            case R.drawable.white : return Color.WHITE;
            case R.drawable.yellow : return Color.YELLOW;
            case R.drawable.grey : return Color.GRAY;
            case R.drawable.orange : return Color.argb(255, 255, 153, 0);
            default: return Color.WHITE;
        }
    }

    private int getDrawableFrameIdByNumber(int id) {
        switch (id) {
            case 0: return R.drawable.frame_1;
            case 1: return R.drawable.frame_2;
            case 2: return R.drawable.frame_3;
            case 3: return R.drawable.frame_4;
            case 4: return R.drawable.frame_5;
            case 5: return R.drawable.frame_6;
            case 6: return R.drawable.frame_7;
            case 7: return R.drawable.frame_8;
            case 8: return R.drawable.frame_9;
            case 9 : return R.drawable.frame_10;
            case 10: return R.drawable.no_frame_eng;
            default : return R.drawable.frame_1;
        }
    }

    private int getDrawableStickerIdByNumber(int id) {
        switch (id) {
            case 0: return R.drawable.black;
            case 1: return R.drawable.white;
            case 2: return R.drawable.red;
            case 3: return R.drawable.green;
            case 4: return R.drawable.blue;
            case 5: return R.drawable.cyan;
            case 6: return R.drawable.yellow;
            case 7: return R.drawable.magenta;
            case 8: return R.drawable.grey;
            case 9 : return R.drawable.orange;
            default : return R.drawable.white;
        }
    }

    public void btnStartTutorial(MenuItem item) {
        if (!isTutorialOn) {
            isTutorialOn = true;
            mTutorialHandler = TourGuide.init(this)
                    .setPointer(new Pointer())
                    .setToolTip(new ToolTip()
                            .setTitle("Focus image place")
                            .setDescription("Tap on an image place to focus it and open " +
                                    "editing menu. When picture is focused, you can move " +
                                    "it's borders by moving the dots. Also, you can reset " +
                                    "borders with reset button in the top right corner. " +
                                    "Later you can save your whole collage into Pictures folder " +
                                    "by pressing save button above the collage.")
                            .setGravity(Gravity.CENTER))
                    .setOverlay(new Overlay());
            isTutorialOnViews = true;

            isEditBlocked = true;
            isBordersBlocked = true;

            mTutorialHandler = mTutorialHandler.playOn(rlMain);
            rlMain.getViewTreeObserver().dispatchOnGlobalLayout();
        }
    }

    public void btnSaveCollage(MenuItem item) {
        rlMain.setDrawingCacheEnabled(true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        Bitmap currImage = rlMain.getDrawingCache();
        currImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        final String path = Environment.getExternalStorageDirectory() + File.separator + imageFileName + ".jpg";
        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaStore.Images.Media.insertImage(getContentResolver(), currImage, "", "");
    }


}
