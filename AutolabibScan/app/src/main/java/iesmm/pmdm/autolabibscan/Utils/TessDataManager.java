package iesmm.pmdm.autolabibscan.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TessDataManager {
    private static final String TAG = "TessDataManager";
    private static final String TESS_DIR = "tesseract";
    private static final String SUB_DIR = "tessdata";
    private static final String[] FILE_NAMES = {"eng.traineddata", "spa.traineddata"};

    public static String getTesseractFolder(Context context) {
        return context.getFilesDir() + "/" + TESS_DIR;
    }

    public static void initTessData(Context context) {
        File tessDir = new File(getTesseractFolder(context));
        if (!tessDir.exists() && !tessDir.mkdirs()) {
            Log.e(TAG, "Could not create directory: " + tessDir.getPath());
            return;
        }

        File subDir = new File(tessDir, SUB_DIR);
        if (!subDir.exists() && !subDir.mkdirs()) {
            Log.e(TAG, "Could not create directory: " + subDir.getPath());
            return;
        }

        AssetManager assetManager = context.getAssets();
        for (String fileName : FILE_NAMES) {
            File outFile = new File(subDir, fileName);
            if (!outFile.exists()) {
                try (InputStream in = assetManager.open(TESS_DIR + "/" + SUB_DIR + "/" + fileName);
                     FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error copying file " + fileName, e);
                }
            }
        }
    }
}
