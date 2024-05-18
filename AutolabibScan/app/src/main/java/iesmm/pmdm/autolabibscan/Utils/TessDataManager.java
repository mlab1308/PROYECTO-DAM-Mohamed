package iesmm.pmdm.autolabibscan.Utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TessDataManager {
    public static void copyTessDataFiles(Context context) {
        File dir = new File(context.getFilesDir() + "/tessdata/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            String[] fileList = context.getAssets().list("tessdata");
            for (String fileName : fileList) {
                File file = new File(dir, fileName);
                if (!file.exists()) {
                    InputStream in = context.getAssets().open("tessdata/" + fileName);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.flush();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
