package android.code.editor.utils;

import android.code.editor.R;
import android.code.editor.files.utils.FileManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

public class ProjectCreatorDialog {
    private String filePath;

    private Context context;

    private BottomSheetDialog projectTemplateBottomSheetDialog;

    private MaterialCardView html_template;

    private onProjectListUpdate onProjectListUpdateListener;

    public ProjectCreatorDialog(
            Context context, String filePath, onProjectListUpdate onProjectListUpdateListener) {
        this.context = context;
        this.filePath = filePath;
        this.onProjectListUpdateListener = onProjectListUpdateListener;
        projectTemplateBottomSheetDialog = new BottomSheetDialog(context);
        projectTemplateBottomSheetDialog.setContentView(
                R.layout.layout_project_template_bottomsheet);
        html_template = projectTemplateBottomSheetDialog.findViewById(R.id.html_template);
        init();
        projectTemplateBottomSheetDialog.create();
    }

    public void show() {
        projectTemplateBottomSheetDialog.show();
    }

    public interface onProjectListUpdate {
        public void onRefresh();
    }

    public void init() {
        html_template.setOnClickListener(
                (view) -> {
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
                    dialog.setTitle("Create new project");
                    dialog.setMessage("Enter project name to create");
                    TextInputLayout nameCont = new TextInputLayout(context);
                    TextInputEditText path = new TextInputEditText(context);
                    path.setHint("Enter project name");
                    nameCont.addView(path);
                    path.addTextChangedListener(
                            new TextWatcher() {
                                @Override
                                public void afterTextChanged(Editable arg0) {
                                    // TODO: Implement this method
                                }

                                @Override
                                public void onTextChanged(
                                        CharSequence arg0, int arg1, int arg2, int arg3) {
                                    // TODO: Implement this method
                                    if (path.getText().length() == 0) {
                                        path.setError("Please enter a project name");
                                    } else if (new File(
                                                    filePath.concat(File.separator)
                                                            .concat(path.getText().toString()))
                                            .exists()) {
                                        path.setError(
                                                "Please enter a project that does not exists");
                                    } else if (path.getText().toString().contains("/")) {
                                        path.setError("Please do not enter / in project name");
                                    } else {
                                        path.setError(null);
                                    }
                                }

                                @Override
                                public void beforeTextChanged(
                                        CharSequence arg0, int arg1, int arg2, int arg3) {
                                    // TODO: Implement this method
                                }
                            });
                    path.setFocusable(true);
                    dialog.setView(nameCont);
                    dialog.setPositiveButton(
                            "Create",
                            (param0, param1) -> {
                                if (path.getText().length() == 0) {
                                    Toast.makeText(
                                                    context,
                                                    "Please enter a project name",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                } else if (new File(
                                                filePath.concat(File.separator)
                                                        .concat(path.getText().toString()))
                                        .exists()) {
                                    Toast.makeText(
                                                    context,
                                                    "File path already exists",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                } else if (path.getText().toString().contains("/")) {
                                    Toast.makeText(
                                                    context,
                                                    "Please do not enter / in project name",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    _save_assets_folder(
                                            "Templates/html_01/${Project_Name}",
                                            filePath.concat(File.separator)
                                                    .concat(path.getText().toString()));
                                    _FindAndReplace(
                                            filePath.concat(File.separator)
                                                    .concat(path.getText().toString()),
                                            "${Project_Name}",
                                            path.getText().toString());
                                    projectTemplateBottomSheetDialog.dismiss();
                                    onProjectListUpdateListener.onRefresh();
                                }
                            });
                    dialog.setNegativeButton(
                            "Cancel",
                            (param0, param1) -> {
                                dialog.create().dismiss();
                            });
                    dialog.create().show();
                });
    }

    public void _FindAndReplace(final String _path, final String _find, final String _replace) {
        ArrayList<String> findAndReplace = new ArrayList<String>();
        FileManager.listDir(_path, findAndReplace);
        try {
            JSONArray FindAndReplace = new JSONArray(new Gson().toJson(findAndReplace));
            for (int position = 0; position < (int) (FindAndReplace.length()); position++) {
                if (new File(FindAndReplace.getString(position)).exists()) {
                    if (new File(FindAndReplace.getString(position)).isDirectory()) {
                        if (FindAndReplace.getString(position).contains(_find)) {
                            File before = new File(FindAndReplace.getString(position));
                            File after =
                                    new File(
                                            FindAndReplace.getString(position)
                                                    .replace(_find, _replace));
                            before.renameTo(after);
                        }
                        _FindAndReplace(FindAndReplace.getString(position), _find, _replace);
                    } else {
                        if (FileManager.readFile(FindAndReplace.getString(position))
                                .contains(_find)) {
                            FileManager.writeFile(
                                    FindAndReplace.getString(position),
                                    FileManager.readFile(FindAndReplace.getString(position))
                                            .replace(_find, _replace));
                        }
                        if (FindAndReplace.getString(position).contains(_find)) {
                            File before = new File(FindAndReplace.getString(position));
                            File after =
                                    new File(
                                            FindAndReplace.getString(position)
                                                    .replace(_find, _replace));
                            before.renameTo(after);
                        }
                    }
                }
            }
        } catch (JSONException e) {

        }
    }

    public void _save_assets_folder(final String _path, final String _save_path) {
        new CustomAssetsManager(context).saveFolder(_path, _save_path);
    }

    public static class CustomAssetsManager {

        private Context myContext;

        public CustomAssetsManager(Context c) {
            myContext = c;
        }

        public CustomAssetsManager(Fragment f) {
            myContext = f.getActivity();
        }

        public CustomAssetsManager(DialogFragment df) {
            myContext = df.getActivity();
        }

        public void saveFile(String path, String pathTo) {
            copyFile(path, pathTo);
        }

        public void saveFolder(String path, String pathTo) {
            copyAssets(path, pathTo);
        }

        private void copyAssets(final String _folder, final String _to) {
            AssetManager assetManager = myContext.getAssets();
            String[] files = null;
            try {
                files = assetManager.list(_folder);
            } catch (java.io.IOException e) {
                Log.e("tag", "Failed to get asset file list", e);
            }
            if (files != null)
                for (String filename : files) {
                    java.io.InputStream in = null;
                    java.io.OutputStream out = null;
                    try {
                        in = assetManager.open(_folder + "/" + filename);
                        if (!new java.io.File(_to).exists()) {
                            new java.io.File(_to).mkdir();

                            java.io.File outFile = new java.io.File(_to, filename);
                            if (!(outFile.exists())) { // File does not exist...
                                out = new java.io.FileOutputStream(outFile);
                                copyFile(in, out);
                            }

                        } else {

                            java.io.File outFile = new java.io.File(_to, filename);
                            if (!(outFile.exists())) { // File does not exist...
                                out = new java.io.FileOutputStream(outFile);
                                copyFile(in, out);
                            }
                        }
                    } catch (java.io.IOException e) {
                        Log.e("tag", "Failed to copy asset file: " + filename, e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (java.io.IOException e) {
                                // NOOP
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (java.io.IOException e) {
                                // NOOP
                            }
                        }
                    }
                }
        }

        private void copyFile(java.io.InputStream in, java.io.OutputStream out)
                throws java.io.IOException {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        private void copyFile(String filename, String outPath) {
            AssetManager assetManager = myContext.getAssets();

            java.io.InputStream in;
            java.io.OutputStream out;
            try {
                in = assetManager.open(filename);
                String newFileName = outPath + "/" + filename;
                out = new java.io.FileOutputStream(newFileName);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (Exception e) {
            }
        }
    }
}
