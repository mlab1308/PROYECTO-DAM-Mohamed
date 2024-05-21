package iesmm.pmdm.autolabibscan.Utils;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class TessDataManager {
    // Método para copiar los archivos de datos de Tesseract al directorio de archivos de la aplicación
    public static void copyTessDataFiles(Context context) {
        // Directorio de destino para los archivos de datos de Tesseract
        File dir = new File(context.getFilesDir() + "/tessdata/");

        // Crear el directorio si no existe
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // Listar los archivos en el directorio "tessdata" en los assets de la aplicación
            String[] fileList = context.getAssets().list("tessdata");

            // Iterar sobre cada archivo en la lista
            for (String fileName : fileList) {
                File file = new File(dir, fileName);

                // Si el archivo no existe en el directorio de destino, copiarlo desde los assets
                if (!file.exists()) {
                    InputStream in = context.getAssets().open("tessdata/" + fileName);
                    OutputStream out = new FileOutputStream(file);

                    // Buffer para leer y escribir los datos del archivo
                    byte[] buffer = new byte[1024];
                    int read;

                    // Leer y escribir el archivo en bloques
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    // Cerrar los flujos de entrada y salida
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
