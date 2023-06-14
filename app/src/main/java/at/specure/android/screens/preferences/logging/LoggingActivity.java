package at.specure.android.screens.preferences.logging;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hypertrack.hyperlog.HyperLog;
import com.specure.opennettest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import at.specure.android.screens.main.BasicActivity;

public class LoggingActivity extends BasicActivity {
    int batchNumber = 1;
    int count = 0;
    List<String> logsList = new ArrayList<>();
    private Toast toast;
    private LogAdapter listAdapter;
    private RecyclerView recycler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);
        androidx.appcompat.app.ActionBar supportActionBar = getDelegate().getSupportActionBar();
        supportActionBar.setTitle(R.string.logging_preference_title);
        if (supportActionBar != null) {
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
//        endPointUrl = (EditText) findViewById(R.id.end_point_url);
        Button getFileButton = findViewById(R.id.get_log_file_button);
        getFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile(v);
            }
        });
        Button showLogsButton = findViewById(R.id.show_logs_button);
        showLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogs(v);
            }
        });
        Button deleteLogsButton = findViewById(R.id.delete_logs_button);
        deleteLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLogs(v);
            }
        });
        recycler = findViewById(R.id.log_recycler_view);


        listAdapter = new LogAdapter(this, logsList);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(listAdapter);
    }

    public void showLogs(View view) {
        logsList.clear();
        logsList.addAll(HyperLog.getDeviceLogsAsStringList(false));
        listAdapter.setData(logsList);
        int itemCount = recycler.getAdapter().getItemCount();
        itemCount = itemCount == 0 ? 1 : itemCount;
        recycler.scrollToPosition(itemCount - 1);
        batchNumber = 1;
    }

    public void getFile(View view) {
        File file = HyperLog.getDeviceLogsInFile(this, false);
        if (file != null && file.exists()) {
            showToast("File Created at: " + file.getAbsolutePath());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    Uri path = Uri.fromFile(file);
                    Uri path = FileProvider.getUriForFile(LoggingActivity.this, LoggingActivity.this.getApplicationContext().getPackageName() + ".logprovider", file);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
// set the type to 'email'
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    emailIntent.setType("vnd.android.cursor.dir/email");
                    String to[] = {"YOUR_OWN_EMAIL_ADDRESS"};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
// the mail subject
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android logs");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            }, 500);
        }


    }

    private void showToast(String message) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void deleteLogs(View view) {
        showToast("Logs deleted");
        HyperLog.deleteLogs();
        logsList.clear();
        listAdapter.setData(logsList);
    }
}
