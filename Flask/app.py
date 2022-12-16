from flask import Flask
from transformers import AutoProcessor
from transformers import AutoModelForTokenClassification
from datasets import load_dataset
import os
import torch
from PIL import Image, ImageDraw, ImageFont
import json
import cv2
import layoutparser as lp
import numpy as np
import base64
import io

DIR = os.getcwd()

# flask run --host=0.0.0.0
app = Flask(__name__)

labels = ['Ignore',
 'Store_name_value',
 'Store_name_key',
 'Store_addr_value',
 'Store_addr_key',
 'Tel_value',
 'Tel_key',
 'Date_value',
 'Date_key',
 'Time_value',
 'Time_key',
 'Prod_item_value',
 'Prod_item_key',
 'Prod_quantity_value',
 'Prod_quantity_key',
 'Prod_price_value',
 'Prod_price_key',
 'Subtotal_value',
 'Subtotal_key',
 'Tax_value',
 'Tax_key',
 'Tips_value',
 'Tips_key',
 'Total_value',
 'Total_key',
 'Others']

id2label = {v: k for v, k in enumerate(labels)}
label2color = {
    "Date_key": 'red',
    "Date_value": 'blue',
    "Ignore": 'orange',
    "Others": 'orange',
    "Prod_item_key": 'red',
    "Prod_item_value": 'green',
    "Prod_price_key": 'red',
    "Prod_price_value": 'blue',
    "Prod_quantity_key": 'red',
    "Prod_quantity_value": 'green',
    "Store_addr_key": 'red',
    "Store_addr_value": 'blue',
    "Store_name_key": 'red',
    "Store_name_value": 'blue',
    "Subtotal_key": 'red',
    "Subtotal_value": 'blue',
    "Tax_key": 'red',
    "Tax_value": 'green',
    "Tel_key": 'red',
    "Tel_value": 'green',
    "Time_key": 'red',
    "Time_value": 'green',
    "Tips_key": 'red',
    "Tips_value": 'green',
    "Total_key": 'red',
    "Total_value": 'blue'
  }

model = AutoModelForTokenClassification.from_pretrained("Theivaprakasham/layoutlmv3-finetuned-wildreceipt")
processor = AutoProcessor.from_pretrained("Theivaprakasham/layoutlmv3-finetuned-wildreceipt", apply_ocr=False)
ocr_agent = lp.GCVAgent.with_credential(os.path.join(DIR,'gcv_credential.json'),languages = ['id'])

@app.route('/')
def index():
    process_image(os.path.join(DIR,'Nota_Segmented/20221123_230408.jpg'), model, processor)
    return "<p>Hello, World!</p>"

@app.route('/detect', methods=['POST'])
def detect(request):
    data = json.loads(request.data)
    bytearrimg = decodeB64(data)
    byteimg = bytearray(bytearrimg)
    pil_image = Image.open(io.BytesIO(byteimg))
    cv_img = np.array(pil_image) 
    # Convert RGB to BGR 
    cv_img = cv_img[:, :, ::-1].copy()
    process_image(cv_img, model, processor)
    return "<p>detect</p>"

def decodeB64(data):
    image_64_decode = base64.b64decode(data) # base64.decode(image_64_encode)
    return image_64_decode

def normalize_box(bbox,size):
    
     return [
        int(1000 * (bbox[0] / size[1])),
        int(1000 * (bbox[1] / size[0])),
        int(1000 * (bbox[2] / size[1])),
        int(1000 * (bbox[3] / size[0])),
     ]

def unnormalize_box(bbox,size):
    
     return [
         size[1] * (bbox[0] / 1000),
         size[0] * (bbox[1] / 1000),
         size[1] * (bbox[2] / 1000),
         size[0] * (bbox[3] / 1000),
     ]


def iob_to_label(label):
    return label



def process_image(filepath, model, processor):
    filename = os.path.basename(filepath).split('.')[0]
    image_result = get_receipt(filepath)
    height, width, _ = image_result.shape
    res = ocr_agent.detect(image_result, return_response=True)
    texts  = ocr_agent.gather_text_annotations(res)
    
    inference_words = []
    for words_bbox in texts:
        inference_words.append(words_bbox.text)

    inference_boxes = []
    for words_bbox in texts:
        h = np.min(words_bbox.block.points, axis=0)
        w = np.max(words_bbox.block.points, axis=0)
        inference_boxes.append([h[0],h[1],w[0],w[1]])

    inf_img = Image.fromarray(image_result)
    inf_words, inf_bboxes = inference_words, [normalize_box(box, (height, width)) for box in inference_boxes]

    # encode
    # encoding = processor(image, truncation=True, return_offsets_mapping=True, return_tensors="pt")
    encoding = processor(inf_img, inf_words, boxes=inf_bboxes, return_tensors="pt",truncation=True,padding=True)

    # forward pass
    with torch.no_grad():
        outputs = model(**encoding)

    # get predictions
    predictions = outputs.logits.argmax(-1).squeeze().tolist()
    token_boxes = encoding.bbox.squeeze().tolist()

    # only keep non-subword predictions
    true_predictions = [id2label[pred] for idx, pred in enumerate(predictions)]
    true_boxes = [unnormalize_box(box, (height, width)) for idx, box in enumerate(token_boxes)]

    # draw predictions over the image
    draw = ImageDraw.Draw(inf_img)
    font = ImageFont.load_default()
    for prediction, box in zip(true_predictions, true_boxes):
        predicted_label = iob_to_label(prediction)
        draw.rectangle(box, outline=label2color[predicted_label])
        draw.text((box[0]+10, box[1]-10), text=predicted_label, fill=label2color[predicted_label], font=font)
        
    inf_img.save(os.path.join(DIR,filename+'.jpg'))
    return inf_img

def get_receipt(imgpath):
    img_read = cv2.imread(imgpath)
    img_read = cv2.cvtColor(img_read, cv2.COLOR_BGR2RGB)
    if img_read is None:
        raise Exception(f"Image {imgpath} not found")
        
    resize_ratio = 1000 / img_read.shape[0]
    if resize_ratio > 1000 / img_read.shape[1]:
        resize_ratio = 1000 / img_read.shape[1]
        
    img_rezise = resize_img(img_read, resize_ratio)
    return img_rezise

def resize_img(image, ratio):
    width = int(image.shape[1] * ratio)
    height = int(image.shape[0] * ratio)
    dim = (width, height)
    return cv2.resize(image, dim, interpolation = cv2.INTER_AREA)


if __name__ == '__main__':
    app.run(debug=True)