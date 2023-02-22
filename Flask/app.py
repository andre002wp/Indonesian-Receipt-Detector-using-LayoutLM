from flask import Flask
from flask import request
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
import time

DIR = os.getcwd()
if DIR == "D:\Andre\TA\Indonesian-Receipt-Detector-using-LayoutLM":
    DIR = os.path.join(DIR, "Flask")

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
ocr_agent = lp.GCVAgent.with_credential(os.path.join(os.path.dirname(DIR),'gcv_credential.json'),languages = ['id'])

@app.route('/')
def index():
    inf_img,img_info = process_image(model, processor, filepath = os.path.join(os.path.dirname(DIR),'Nota_Segmented/20221123_230906.jpg'))
    print(img_info)
    return "<p>Hello, World!</p>"

@app.route('/detect', methods=['POST'])
def detect():
    data = json.loads(request.data)
    bytearrimg = decodeB64(data)
    byteimg = bytearray(bytearrimg)
    pil_image = Image.open(io.BytesIO(byteimg))
    cv_img = np.array(pil_image) 
    # Convert RGB to BGR 
    cv_img = cv_img[:, :, ::-1].copy()

    path = os.path.join(os.path.dirname(DIR),'android_img.jpg')
    cv2.imwrite(path, cv_img)

    inf_img,img_info = process_image(model, processor,filepath = path)
    print(img_info)
    return "<p>detect</p>"

def decodeB64(data):
    image_64_decode = base64.b64decode(data) # base64.decode(image_64_encode)
    return image_64_decode

def resize_img(image, ratio):
    width = int(image.shape[1] * ratio)
    height = int(image.shape[0] * ratio)
    dim = (width, height)
    return cv2.resize(image, dim, interpolation = cv2.INTER_AREA)

def get_receipt(imgpath):
    img_read = cv2.imread(imgpath)
    if img_read is None:
        return None

    img_read = cv2.cvtColor(img_read, cv2.COLOR_BGR2RGB)
    if img_read is None:
        raise Exception(f"Image {imgpath} not found")
        
    resize_ratio = 1000 / img_read.shape[0]
    if resize_ratio > 1000 / img_read.shape[1]:
        resize_ratio = 1000 / img_read.shape[1]
        
    img_rezise = resize_img(img_read, resize_ratio)
    return img_rezise

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



def process_image(model, processor,filepath = None, image = None):
    curtime = time.localtime()
    if filepath is not None:
        filename = os.path.basename(filepath).split('.')[0]
        image_result = get_receipt(filepath)
    elif image is not None:
        filename = 'android_inference'
        image_result = image
    else:
        AssertionError("No image or filepath provided")
        return

    # Generating Receipt Info
    imginfo = {}
    imginfo['Store'] = []
    imginfo['Date'] = []
    imginfo['Time'] = []
    imginfo["Products"] = {}
    imginfo['Total'] =[]

    if image_result is None:
        return None

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
    words_decoded = encoding["input_ids"].squeeze().tolist()

    # only keep non-subword predictions
    true_predictions = [id2label[pred] for idx, pred in enumerate(predictions)]
    true_boxes = [unnormalize_box(box, (height, width)) for idx, box in enumerate(token_boxes)]

    # draw predictions over the image
    draw = ImageDraw.Draw(inf_img)
    font = ImageFont.load_default()
    for prediction, box, words in zip(true_predictions, true_boxes, words_decoded):
        predicted_label = prediction
        checkPredictedLabels(imginfo,predicted_label,box,processor.tokenizer.decode(words))
        draw.rectangle(box, outline=label2color[predicted_label])
        draw.text((box[0]+10, box[1]-10), text=predicted_label, fill=label2color[predicted_label], font=font)
        
    inf_img.save(os.path.join(os.path.dirname(DIR),"Result","Android",filename+f"_{curtime[2]}-{curtime[1]}-{curtime[0]} '{curtime[3]}.{curtime[4]}.{curtime[5]}"+'.jpg'))

    return inf_img,BeautifyInfo(imginfo)

def checkPredictedLabels(info,predicted_labels,box,words):
    # middle point of the y axis
    mid_y = int(box[1]+(box[3]-box[1]))

    box_tolerance = 10 # 10 pixels ?
    # check if the box is already in the info dict with some tolerance
    if info["Products"].keys() is not None:
        if len(info["Products"].keys()) < 1:
            tolerated_box = mid_y

        # add tolerance to the y axis
        for key in info["Products"].keys():
            
            # adding for performance and easier debugging
            if mid_y == key:
                tolerated_box = mid_y
                break

            if mid_y < key+box_tolerance and mid_y > key-box_tolerance:
                # print("tolerating: ", mid_y , " to ", key)
                tolerated_box = key
                break
            else:
                # print("not allowing: ", mid_y , " to ", key)
                tolerated_box = mid_y
    else:
        tolerated_box = mid_y
        
    # addes the words to the right key in the info dict
    if predicted_labels == 'Date_value':
        info["Date"].append(words)
    elif predicted_labels == 'Time_value':
        info["Time"].append(words)
    elif predicted_labels == 'Store_name_value':
        info["Store"].append(words)
    elif predicted_labels == 'Total_value':
        info["Total"].append(words)   
    elif predicted_labels == 'Prod_item_value':
        if tolerated_box not in info["Products"].keys():
            info["Products"][tolerated_box] = {"name":[],"quantity":[],"price":[]}
        
        info["Products"][tolerated_box]["name"].append(words)
    elif predicted_labels == 'Prod_price_value':
        if tolerated_box not in info["Products"].keys():
            info["Products"][tolerated_box] = {"name":[],"quantity":[],"price":[]}
        
        info["Products"][tolerated_box]["price"].append(words)  
    elif predicted_labels == 'Prod_quantity_value':
        if tolerated_box not in info["Products"].keys():
            info["Products"][tolerated_box] = {"name":[],"quantity":[],"price":[]}
        
        info["Products"][tolerated_box]["quantity"].append(words)

def BeautifyInfo(imginfo):
    newinfo = {}
    newinfo["Store"] = " ".join(imginfo["Store"])
    newinfo["Date"] = " ".join(imginfo["Date"])
    newinfo["Time"] = " ".join(imginfo["Time"])
    newinfo["Total"] = " ".join(imginfo["Total"])
    newinfo["Products"] = {}
    for idx,item in enumerate(imginfo["Products"].values()):
        newinfo["Products"][idx] = {}
        newinfo["Products"][idx]["name"] = "".join(item["name"])
        newinfo["Products"][idx]["quantity"] = "".join(item["quantity"])
        newinfo["Products"][idx]["price"] = "".join(item["price"])

    return newinfo

if __name__ == '__main__':
    app.run(debug=True)