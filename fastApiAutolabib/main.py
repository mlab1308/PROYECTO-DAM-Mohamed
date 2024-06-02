from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import cv2
from easyocr import Reader
import numpy as np

app = FastAPI()

def process_image(image_bytes):
    """
    Función para procesar una imagen y extraer el texto de la matrícula.
    :param image_bytes: bytes de la imagen
    :return: texto detectado de la matrícula
    """
    # Convertir bytes de imagen a un array de numpy
    nparr = np.frombuffer(image_bytes, np.uint8)
    car = cv2.imdecode(nparr, cv2.IMREAD_COLOR)  # Decodificar la imagen a color

    if car is None:
        raise ValueError("Image file not found")  # Si la imagen no se puede cargar, lanzar un error

    # Redimensionar la imagen para mantener la relación de aspecto
    car = cv2.resize(car, (800, int(car.shape[0] * 800 / car.shape[1])))
    # Convertir la imagen a escala de grises
    gray = cv2.cvtColor(car, cv2.COLOR_BGR2GRAY)

    # Mejorar el contraste de la imagen
    gray = cv2.equalizeHist(gray)

    # Aplicar umbralización adaptativa
    thresh = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 11, 2)

    # Aplicar operaciones morfológicas para limpiar la imagen
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5))
    morph = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)

    # Aplicar el detector de bordes de Canny
    edged = cv2.Canny(morph, 50, 200)

    # Encontrar contornos en la imagen
    contours, _ = cv2.findContours(edged, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    # Ordenar los contornos por área en orden descendente y seleccionar los 10 más grandes
    contours = sorted(contours, key=cv2.contourArea, reverse=True)[:10]

    plate_cnt = None  # Inicializar variable para almacenar el contorno de la matrícula
    for c in contours:
        arc = cv2.arcLength(c, True)  # Calcular el perímetro del contorno
        # Aproximar el contorno a un polígono con una precisión del 2% del perímetro
        approx = cv2.approxPolyDP(c, 0.02 * arc, True)
        if len(approx) == 4:
            plate_cnt = approx  # Si el polígono tiene 4 lados, asumir que es la matrícula
            break  # Detener el bucle una vez encontrada la matrícula

    if plate_cnt is None:
        raise ValueError("No license plate contour found")  # Si no se encuentra un contorno de matrícula, lanzar un error

    # Calcular el rectángulo delimitador de la matrícula y extraer la región de la matrícula en escala de grises
    (x, y, w, h) = cv2.boundingRect(plate_cnt)
    plate = gray[y:y + h, x:x + w]

    # Inicializar el lector de EasyOCR con soporte para inglés, sin GPU y sin verbosidad
    reader = Reader(['en'], gpu=False, verbose=False)
    detection = reader.readtext(plate)  # Leer el texto de la región de la matrícula

    if len(detection) == 0:
        raise ValueError("Impossible to read the text from the license plate")  # Si no se puede leer el texto, lanzar un error

    return detection[0][1]  # Devolver el texto detectado de la matrícula

@app.post("/detect_plate/")
async def detect_plate(file: UploadFile = File(...)):
    """
    Endpoint para detectar la matrícula en una imagen subida.
    :param file: archivo subido
    :return: JSON con el texto de la matrícula o un mensaje de error
    """
    try:
        contents = await file.read()  # Leer el contenido del archivo subido
        plate_text = process_image(contents)  # Procesar la imagen y obtener el texto de la matrícula
        return JSONResponse(content={"plate": plate_text})  # Devolver el texto de la matrícula en una respuesta JSON
    except ValueError as e:
        return JSONResponse(content={"error": str(e)}, status_code=400)  # Devolver un mensaje de error si ocurre un ValueError
    except Exception as e:
        return JSONResponse(content={"error": "An unexpected error occurred"}, status_code=500)  # Devolver un mensaje de error genérico si ocurre una excepción inesperada

# Para correr la aplicación, usa el siguiente comando:
# uvicorn main:app --reload
