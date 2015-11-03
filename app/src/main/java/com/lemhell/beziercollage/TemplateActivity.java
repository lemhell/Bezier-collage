package com.lemhell.beziercollage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lemhell.beziercollage.recycler.RecyclerAdapter;

public class TemplateActivity extends AppCompatActivity {

    public static boolean isStatic = false;

    private final int NUMBER_OF_TEMPLATES = 14;
    private Point size;
    private LinearLayout llButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_template);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        TextView tvCollageMaker = (TextView) findViewById(R.id.tvBezierCollage);
        int mult = size.x > 1100 ? 2 : 1;
        tvCollageMaker.setTextSize((float) ((float) size.x / 48.0 * mult));

        init();
    }

    private void init() {
        llButtons = (LinearLayout) findViewById(R.id.llButtons);

        for (int i = 0; i < NUMBER_OF_TEMPLATES; i++) {
            Button b = new Button(this);
            b.setText(String.valueOf(i));
            b.setId(100 + i);
            b.setOnClickListener(view -> onClickInvocation(view.getId() - 100, TemplateActivity.isStatic));
            llButtons.addView(b);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(RecyclerAdapter.BROADCAST_ONCLICK_DYNAMIC));
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("IntId", -1);
            onClickInvocation(id, TemplateActivity.isStatic);
        }
    };

    private void onClickInvocation(int id, boolean isStatic) {
        Log.d("TA", String.valueOf(id));
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        String strName = getFilenameByDrawableId(id);
        Log.d("@", strName);
        intent.putExtra("StringFilename", strName);
        intent.putExtra("BooleanIsStatic", isStatic);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_template, menu);
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

    private String getFilenameBasic(int id) {
        switch (id) {
            case 0: return "templates/basic/template0.txt";
            case 1: return "templates/basic/template1.txt";
            case 2: return "templates/basic/template2.txt";
            case 3: return "templates/basic/template3.txt";
            case 4: return "templates/basic/template4.txt";
            case 5: return "templates/basic/template5.txt";
            case 6: return "templates/basic/template6.txt";
            case 7: return "templates/basic/template7.txt";
        }
        return "templates/basic/template1.txt";
    }

    private String getFilenameAdvanced(int id) {
        switch (id) {
            case 0: return "templates/advanced/template0.txt";
            case 1: return "templates/advanced/template1.txt";
            case 2: return "templates/advanced/template2.txt";
            case 3: return "templates/advanced/template3.txt";
            case 4: return "templates/advanced/template4.txt";
            case 5: return "templates/advanced/template5.txt";
        }
        return "templates/advanced/template1.txt";
    }

    private String getFilenameByDrawableId(int id) {
        switch (id) {
            case 0:     return getFilenameBasic(0);
            case 1:     return getFilenameBasic(1);
            case 2:     return getFilenameBasic(2);
            case 3:     return getFilenameBasic(3);
            case 4:     return getFilenameBasic(4);
            case 5:     return getFilenameBasic(5);
            case 6:     return getFilenameBasic(6);
            case 7:     return getFilenameBasic(7);
            case 8:  return getFilenameAdvanced(0);
            case 9:  return getFilenameAdvanced(1);
            case 10:  return getFilenameAdvanced(2);
            case 11:  return getFilenameAdvanced(3);
            case 12:  return getFilenameAdvanced(4);
            case 13:  return getFilenameAdvanced(5);
        }
        return "templates/advanced/template1.txt";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        System.gc();
    }
}
