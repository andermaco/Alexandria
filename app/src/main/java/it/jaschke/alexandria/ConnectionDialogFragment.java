package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by andermaco on 12/11/15.
 */
public class ConnectionDialogFragment extends DialogFragment {
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getTitle());
        builder.setMessage(R.string.connetion_settings)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent it = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        it.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(it);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });
        return builder.create();
    }
}
