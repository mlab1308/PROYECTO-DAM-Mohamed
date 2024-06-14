import cv2
import numpy as np
import pytesseract
from flask import Flask, request, jsonify

# Inicializa la aplicación Flask
app = Flask(__name__)

# Configura Tesseract OCR
pytesseract.pytesseract.tesseract_cmd = "C:/Program Files/Tesseract-OCR/tesseract.exe"

# Carga el clasificador Haarcascade para detectar placas
cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_russian_plate_number.xml')


# Función para extraer la matrícula de una imagen
def extraer_matricula(nombre_imagen):
    # Carga la imagen
    imagen = cv2.imread(nombre_imagen)


    # Convierte la imagen a escala de grises
    gris = cv2.cvtColor(imagen, cv2.COLOR_BGR2GRAY)


    # Detecta placas en la imagen
    placas = cascade.detectMultiScale(gris, 1.1, 4)

    for (x, y, w, h) in placas:
        wT, hT, cT = imagen.shape
        a, b = (int(0.02 * wT), int(0.02 * hT))
        margen = int(w * 0.08)
        # Recorta la placa de la imagen original, excluyendo un margen izquierdo
        placa = imagen[y + a:y + h - a, x + b + margen:x + w - b, :]


        # Preprocesamiento de la imagen de la matrícula
        placa_gris = cv2.cvtColor(placa, cv2.COLOR_BGR2GRAY)


        placa_desenfoque = cv2.GaussianBlur(placa_gris, (5, 5), 0)


        placa_contraste = cv2.convertScaleAbs(placa_desenfoque, alpha=2, beta=0)


        bordes_placa = cv2.Canny(placa_contraste, 100, 200)


        # Dilata la imagen para conectar caracteres
        kernel = np.ones((3, 3), np.uint8)
        placa_dilatada = cv2.dilate(bordes_placa, kernel, iterations=1)


        # Binarización
        (umbral, placa_binaria) = cv2.threshold(placa_dilatada, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)


        # Configuración de Tesseract para excluir letras no permitidas en matrículas españolas
        caracteres_permitidos = "BCDFGHJKLMNPRSTVWXYZ0123456789"
        config = f'--psm 8 --oem 3 -c tessedit_char_whitelist={caracteres_permitidos}'

        # Lee el texto en la imagen de la matrícula usando Tesseract
        lectura = pytesseract.image_to_string(placa_binaria, config=config)
        lectura = ''.join(e for e in lectura if e.isalnum())

        # Imprimir la matrícula por consola
        if lectura:
            print("Matrícula leída:", lectura)
            return lectura

    return ""


# Ruta para la página de bienvenida
@app.route('/')
def index():
    html_content = """
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>API de Reconocimiento de Matrículas</title>
        <style>
            body { font-family: Arial, sans-serif; background-color: #f4f4f9; color: #333; text-align: center; padding: 50px; }
            h1 { color: #5a67d8; }
            p { font-size: 18px; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #fff; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }
        </style>
    </head>
    <body>
        <div class="container">
            <h1>Bienvenido a la API de Reconocimiento de Matrículas</h1>
            <p>Esta API ha sido creada por Mohamed Labib Khotbi para el reconocimiento de matrículas mediante Tesseract y OpenCV.</p>
            <p>Utiliza el endpoint <strong>/reconocer_matricula</strong> para subir una imagen y obtener el texto de la matrícula detectada.</p>
        </div>
    </body>
    </html>
    """
    return html_content


# Endpoint para subir archivos y reconocer matrículas
@app.route('/reconocer_matricula', methods=['POST'])
def cargar_archivo():
    if 'file' not in request.files:
        return jsonify({'error': 'No se encontró el archivo'}), 400
    archivo = request.files['file']
    if archivo.filename == '':
        return jsonify({'error': 'No se seleccionó ningún archivo'}), 400
    if archivo:
        ruta_archivo = 'imagen_subida.jpg'
        archivo.save(ruta_archivo)
        resultado = extraer_matricula(ruta_archivo)
        if resultado:
            return jsonify({'matricula_detectada': resultado})
        else:
            return jsonify({'error': 'No se pudo detectar la matrícula'}), 400


# Ejecuta la aplicación Flask en el puerto 8080
if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5000)
