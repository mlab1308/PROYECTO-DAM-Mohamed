package iesmm.pmdm.autolabibscan.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class OCRProcessor {
    private TessBaseAPI tessBaseAPI;

    public OCRProcessor(Context context) {
        tessBaseAPI = new TessBaseAPI();
        String dataPath = context.getFilesDir() + "/tesseract/";
        File tessDataFile = new File(dataPath + "tessdata/spa.traineddata");

        if (!tessDataFile.exists()) {
            Log.e("OCRProcessor", "Tesseract data file not found at: " + tessDataFile.getPath());
            return;
        }

        tessBaseAPI.init(dataPath, "spa"); // "spa" para español, "eng" para inglés, etc.
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }

    public String getOCRResult(Bitmap bitmap) {
        if (tessBaseAPI == null) {
            return "Tesseract initialization failed.";
        }
        tessBaseAPI.setImage(bitmap);
        return tessBaseAPI.getUTF8Text();
    }

    public void stop() {
        if (tessBaseAPI != null) {
            tessBaseAPI.stop();
            tessBaseAPI.end();
        }
    }
}
