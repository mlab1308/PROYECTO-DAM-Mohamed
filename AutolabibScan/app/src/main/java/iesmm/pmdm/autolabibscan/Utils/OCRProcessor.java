package iesmm.pmdm.autolabibscan.Utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

public class OCRProcessor {

    private TessBaseAPI tessBaseAPI;

    public OCRProcessor(Context context) {
        tessBaseAPI = new TessBaseAPI();
        String dataPath = context.getFilesDir() + "/";
        tessBaseAPI.init(dataPath, "spa"); 

    }

    public String getTextFromBitmap(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        return tessBaseAPI.getUTF8Text();
    }

    public void close() {
        tessBaseAPI.end();
    }
}
