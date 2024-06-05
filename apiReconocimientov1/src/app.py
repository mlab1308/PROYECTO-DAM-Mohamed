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

    resultado = []

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
        print("Matrícula leída:", lectura)
        resultado.append(lectura)

    return resultado

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
        return jsonify({'matricula_detectada': resultado})

# Ejecuta la aplicación Flask en el puerto 8080
if __name__ == '__main__':
    app.run(debug=True, port=8080)
