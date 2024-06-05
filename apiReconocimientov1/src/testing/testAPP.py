import cv2
import numpy as np
import pytesseract
import matplotlib.pyplot as plt

pytesseract.pytesseract.tesseract_cmd = "C:/Program Files/Tesseract-OCR/tesseract.exe"

cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_russian_plate_number.xml')

def extract_num(img_filename):
    img = cv2.imread(img_filename)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    nplate = cascade.detectMultiScale(gray, 1.1, 4)

    for (x, y, w, h) in nplate:
        # Ajustar las coordenadas para evitar la parte izquierda de la matrícula
        wT, hT, cT = img.shape
        a, b = (int(0.02 * wT), int(0.02 * hT))
        margin = int(w * 0.08)  # Margen para evitar la parte izquierda
        plate = img[y + a:y + h - a, x + b + margin:x + w - b, :]

        # Preprocesamiento de la imagen de la matrícula
        plate_gray = cv2.cvtColor(plate, cv2.COLOR_BGR2GRAY)
        plate_blur = cv2.GaussianBlur(plate_gray, (5, 5), 0)

        # Aumentar el contraste
        plate_contrast = cv2.convertScaleAbs(plate_blur, alpha=2, beta=0)

        # Aplicar un filtro de bordes
        plate_edges = cv2.Canny(plate_contrast, 100, 200)

        # Dilatar la imagen para conectar caracteres
        kernel = np.ones((3, 3), np.uint8)
        plate_dilate = cv2.dilate(plate_edges, kernel, iterations=1)

        # Binarización
        (thresh, plate_bin) = cv2.threshold(plate_dilate, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

        # Configuración de Tesseract para excluir letras no permitidas en matrículas españolas
        allowed_chars = "BCDFGHJKLMNPRSTVWXYZ0123456789"
        config = f'--psm 8 --oem 3 -c tessedit_char_whitelist={allowed_chars}'

        # Lee el texto en la imagen de la matrícula usando Tesseract
        read = pytesseract.image_to_string(plate_bin, config=config)
        read = ''.join(e for e in read if e.isalnum())

        # Imprimir la matrícula por consola
        print("Matrícula leída:", read)

        # Dibuja el rectángulo y el texto en la imagen original
        cv2.rectangle(img, (x + margin, y), (x + w, y + h), (51, 51, 255), 2)
        cv2.rectangle(img, (x + margin - 1, y - 40), (x + w + 1, y), (51, 51, 255), -1)
        cv2.putText(img, read, (x + margin, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.9, (255, 255, 255), 2)

        # Mostrar la imagen de la matrícula y el resultado de la detección
        plt.figure(figsize=(10, 6))
        plt.subplot(121), plt.imshow(cv2.cvtColor(plate_bin, cv2.COLOR_BGR2RGB)), plt.title('Matrícula procesada')
        plt.subplot(122), plt.imshow(cv2.cvtColor(img, cv2.COLOR_BGR2RGB)), plt.title('Resultado')
        plt.show()

    # Guardar la imagen con las matrículas detectadas
    output_path = img_filename.split('.')[0] + '_detected.jpg'
    cv2.imwrite(output_path, img)

extract_num("coches/3183knd.jpeg")
