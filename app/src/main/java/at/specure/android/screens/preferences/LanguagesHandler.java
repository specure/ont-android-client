package at.specure.android.screens.preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.specure.opennettest.R;

public class LanguagesHandler {


    public static void showSupportedLanguages(final Activity context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = context.getLayoutInflater();
        View inflate = layoutInflater.inflate(R.layout.languages_list, null);
        RecyclerView recycler = inflate.findViewById(R.id.recycle_view);
        LanguageAdapter measurementServerArrayAdapter = new LanguageAdapter(context);

        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(measurementServerArrayAdapter);
        builder.setView(inflate);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }
}
