package com.chin.bbdb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class NetworkDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Error getting familiar list. Make sure you're connected to the internet and try again.")
               .setPositiveButton("Quit", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   System.exit(0);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}