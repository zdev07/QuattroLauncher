package com.ipaulpro.afilechooser;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import net.kdt.pojavlaunch.*; // Keeping this for R file access until we rename the package

/**
 * List adapter for Files.
 * Updated for Quattro Launcher
 */
public class FileListAdapter extends BaseAdapter {
    private final static int ICON_FOLDER = R.drawable.ic_folder;
    private final static int ICON_FILE = R.drawable.ic_file;
    private final LayoutInflater mInflater;
    private List<File> mData = new ArrayList<File>();

    public FileListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }
    // ... (rest of logic remains same for file handling)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null)
            row = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        TextView view = (TextView) row;
        final File file = getItem(position);
        view.setText(file.getName());
        int icon = file.isDirectory() ? ICON_FOLDER : ICON_FILE;
        view.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        view.setCompoundDrawablePadding(20);
        return row;
    }
}
