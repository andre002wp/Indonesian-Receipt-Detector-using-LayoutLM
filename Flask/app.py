from flask import Flask
from flask import request
from flask import jsonify
from flask import make_response
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
import regex as re

DIR = os.getcwd()
if DIR == "D:\Andre\TA\Indonesian-Receipt-Detector-using-LayoutLM":
    DIR = os.path.join(DIR, "Flask")

# flask run --host=0.0.0.0
app = Flask(__name__)

labels = ['Ignore', 
          'Store_name_value',
          'Date_value',
          'Time_value',
          'Prod_item_key',
          'Prod_item_value',
          'Prod_quantity_key',
          'Prod_quantity_value',
          'Prod_price_key',
          'Prod_price_value',
          'Subtotal_key',
          'Subtotal_value',
          'Total_key',
          'Total_value',
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
    "Time_value": 'blue',
    "Tips_key": 'red',
    "Tips_value": 'green',
    "Total_key": 'red',
    "Total_value": 'red'
  }

IMPORTANT_LABELS = ['Store_name_value',
                    'Date_value',
                    'Time_value',
                    'Prod_item_value',
                    'Prod_quantity_value',
                    'Prod_price_value',
                    'Total_value']

model = AutoModelForTokenClassification.from_pretrained(os.path.join(os.path.dirname(DIR),'Saved_model','all_data_4epochs'))
processor = AutoProcessor.from_pretrained(os.path.join(os.path.dirname(DIR),'Saved_model','Processor','all_data_4epochs'), apply_ocr=False)
ocr_agent = lp.GCVAgent.with_credential(os.path.join(os.path.dirname(DIR),'gcv_credential.json'),languages = ['id'])

@app.route('/')
def index():
    # inf_img,img_info = process_image(model, processor, filepath = os.path.join(os.path.dirname(DIR),'Nota_Segmented/20221123_230906.jpg'))
    return "<p>Hello, World!</p>"

@app.route('/detect', methods=['POST'])
def detect():
    try:
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

        img_byte_arr = io.BytesIO()
        inf_img.save(img_byte_arr, format='PNG')
        img_byte_arr = img_byte_arr.getvalue()
        img_str = base64.b64encode(img_byte_arr).decode('utf-8')
        print(img_str)

        return successResponse(singleReceipt(img_info),img_str,"success")
    except Exception as e:
        return badRequest(e,"error")

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
    imginfo['Total'] = {}

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
        if predicted_label in IMPORTANT_LABELS:
            draw.rectangle(box, outline=label2color[predicted_label])
            draw.text((box[0]+10, box[1]-10), text=predicted_label, fill=label2color[predicted_label], font=font)

    # Save for future reference
    inf_img.save(os.path.join(os.path.dirname(DIR),"Result","Android",filename+f"_{curtime[2]}-{curtime[1]}-{curtime[0]} '{curtime[3]}.{curtime[4]}.{curtime[5]}"+'.jpg'))

    # Beautify the info
    beautyinfo = BeautifyInfo(imginfo)

    green_flag = checkGreenFlag(beautyinfo)
    # if at least 1 product have parallel info of name, price and quantity then we can say that the info is correct if not
    # then we try the 2nd method
    if green_flag < 1:
        pop_idx = []
        for idx,prod_item in enumerate(beautyinfo["Products"].values()):
            # if its not the first item
            if idx > 0:
                # if it have no name but has price and quantity
                # print(f"if {len(prod_item['name'])} < 1 and {len(prod_item['quantity'])} > 1 and {len(prod_item['price'])} > 1:")
                if len(prod_item["name"]) < 1 and len(prod_item['quantity']) > 0 and len(prod_item['price']) > 0:
                    # then the name is the previous item
                    beautyinfo["Products"][idx]["name"] = beautyinfo["Products"][idx-1]["name"]
                    pop_idx.append(idx-1)
        # remove the previous item
        for idx in pop_idx:
            beautyinfo["Products"].pop(idx)

    # check again
    green_flag = checkGreenFlag(beautyinfo)

    # if still no green flag then we try the 3rd method
    if green_flag < 1:
        pop_idx = []
        for idx,prod_item in enumerate(beautyinfo["Products"].values()):
            # if its not the first item
            if idx > 0:
                # if it has name but dont have price and quantity
                # print(f"if {len(prod_item['name'])} < 1 and {len(prod_item['quantity'])} > 1 and {len(prod_item['price'])} > 1:")
                if len(prod_item["name"]) > 0 and len(prod_item['quantity']) < 1 and len(prod_item['price']) < 1:
                    if len(beautyinfo["Products"][idx-1]["name"]) < 1 and len(beautyinfo["Products"][idx-1]['quantity']) > 0 and len(beautyinfo["Products"][idx-1]['price']) > 0:
                        # then the name is the previous item
                        beautyinfo["Products"][idx]["name"] = beautyinfo["Products"][idx-1]["name"]
                        pop_idx.append(idx-1)
        # remove the previous item
        for idx in pop_idx:
            beautyinfo["Products"].pop(idx)

    # check again
    green_flag = checkGreenFlag(beautyinfo)

    # if still no green flag then we can't say that the info is correct
    if green_flag < 1:
        #raise error
        print(beautyinfo)
        raise Exception("The info is not correct")
    else:
        return inf_img,beautyinfo

def checkGreenFlag(imginfo):
    # additional check for the products
    green_flag = 0
    for prod_item in imginfo["Products"].values():
        red_flag = 0
        if len(prod_item["name"]) < 1:
            red_flag+=1
        if len(prod_item["price"]) < 1:
            red_flag+=1
        if len(prod_item["quantity"]) < 1:
            red_flag+=1
        # print("red :",red_flag)
        # means at least 1 product has parallel info of name, price and quantity
        if red_flag < 1:
            green_flag+=1
    return green_flag


def checkGreenFlag(imginfo):
    # additional check for the products
    green_flag = 0
    for prod_item in imginfo["Products"].values():
        red_flag = 0
        if len(prod_item["name"]) < 1:
            red_flag+=1
        if len(prod_item["price"]) < 1:
            red_flag+=1
        if len(prod_item["quantity"]) < 1:
            red_flag+=1
        # print("red :",red_flag)
        # means at least 1 product has parallel info of name, price and quantity
        if red_flag < 1:
            green_flag+=1
    return green_flag

def checkPredictedLabels(info,predicted_labels,box,words):
    # middle point of the y axis
    mid_y = int(box[1]+(box[3]-box[1]))

    box_tolerance = abs(int(box[3]-box[1])) # 10 pixels ? | auto tolerance
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

    # check if the box is already in the info dict with some tolerance
    if info["Total"].keys() is not None:
        if len(info["Total"].keys()) < 1:
            tolerated_total = mid_y

        # add tolerance to the y axis
        for key in info["Total"].keys():
            
            # adding for performance and easier debugging
            if mid_y == key:
                tolerated_total = mid_y
                break

            if mid_y < key+box_tolerance and mid_y > key-box_tolerance:
                # print("tolerating: ", mid_y , " to ", key)
                tolerated_total = key
                break
            else:
                # print("not allowing: ", mid_y , " to ", key)
                tolerated_total = mid_y
    else:
        tolerated_total = mid_y
        
    # addes the words to the right key in the info dict
    if predicted_labels == 'Date_value':
        info["Date"].append(words)
    elif predicted_labels == 'Time_value':
        info["Time"].append(words)
    elif predicted_labels == 'Store_name_value':
        info["Store"].append(words)
    elif predicted_labels == 'Total_value':
        if tolerated_total not in info["Total"].keys():
            info["Total"][tolerated_total] = {"total":[]}
        
        info["Total"][tolerated_box]['total'].append(words)
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
    newinfo["Store"] = "".join(imginfo["Store"])
    newinfo["Store"] = newinfo["Store"][1:]
    newinfo["Date"] = "".join(imginfo["Date"])
    newinfo["Date"] = newinfo["Date"][1:]
    newinfo["Time"] = "".join(imginfo["Time"])
    newinfo["Time"] = newinfo["Time"][1:]
    try:
        newinfo["Total"] = "".join(list(imginfo["Total"].values())[0]['total'])
    except:
        newinfo["Total"] = "".join(imginfo["Total"])
    newinfo["Total"] = newinfo["Total"][1:]
    newinfo["Products"] = {}
    for idx,item in enumerate(imginfo["Products"].values()):
        newinfo["Products"][idx] = {}
        newinfo["Products"][idx]["name"] = "".join(item["name"])
        newinfo["Products"][idx]["name"] = newinfo["Products"][idx]["name"][1:]
        newinfo["Products"][idx]["quantity"] = "".join(item["quantity"])
        newinfo["Products"][idx]["quantity"] = newinfo["Products"][idx]["quantity"][1:]
        newinfo["Products"][idx]["price"] = "".join(item["price"])
        newinfo["Products"][idx]["price"] = newinfo["Products"][idx]["price"][1:]

    datetime_regex = [
        r"^([0-9]{1,2})-([0-9]{1,2})-([0-9]{4})-([0-9]{1,2}:[0-9]{1,2})", # 01-01-2020-12:00
        r"^([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})-([0-9]{1,2}:[0-9]{1,2})", # 2020-01-01-12:00
        r"^([0-9]{1,2})-([0-9]{1,2})-([0-9]{2})-([0-9]{1,2}:[0-9]{1,2})", # 01-01-20-12:00
        r"([0-9]{1,2})-([0-9]{1,2})-([0-9]{2})-([0-9]{1,2}-:[0-9]{1,2})" # 01-01-20-12-:00 # this is for indomaret
    ]
    datetime_str = newinfo['Date']+"-"+newinfo['Time']
    datetime_str = datetime_str.replace("/","-").replace(".","-").replace(" ","").replace(",","-").replace(".","-")

    ## beautify date and time
    for idx,regex in enumerate(datetime_regex):
        match = re.match(regex,datetime_str)
        if match:
            match_idx = idx
            break
        else:
            match_idx = -1

    if match_idx == 0:
        newinfo['Date'] = match.group(3)+"-"+match.group(2)+"-"+match.group(1)
        newinfo['Time'] = match.group(4)
    elif match_idx == 1:    
        newinfo['Date'] = match.group(1)+"-"+match.group(2)+"-"+match.group(3)
        newinfo['Time'] = match.group(4)
    elif match_idx == 2:
        newinfo['Date'] = match.group(3)+"-"+match.group(2)+"-20"+match.group(1)
        newinfo['Time'] = match.group(4)
    elif match_idx == 3:
        newinfo['Date'] = match.group(3)+"-"+match.group(2)+"-20"+match.group(1)
        newinfo['Time'] = match.group(4).replace("-","")
    else:
        print("No match found")
        print(datetime_str)

    return newinfo

#if it aint broken dont fix it  ###LMAO 
def singleReceipt(data):
    #products integer formatting
    products = data['Products'].copy()
    for item in list(products.values()):
        if len(item['price']) > 0:
            price = []
            corrected_price = ""
            for it in item['price'].split('.'):
                for it2 in it.split(','):
                    price.append(it2)
            for idx,it in enumerate(price):
                if idx != 0:
                    if len(it) == 3:
                        corrected_price += it
                else:
                    corrected_price += it
                        

            item['price'] = int(corrected_price)
        else:
            item['price'] = 0

        if len(item['quantity']) > 0:
            item['quantity'] = int(item['quantity'].replace(",",".").split('.')[0])
        else:
            item['quantity'] = 0

    try:
        #total integer formatting
        strtotal = data['Total'].replace(',','-').replace('.','-')
        total = "".join(strtotal.split('-')[0])+ strtotal.split('-')[1][:3]
        total = int(total)
    except:
        print(data)
        try:
            if type(data['Total']) == str:
                total = int(data['Total'])
            else:
                total = 0
        except:
            total = 0


    data = {
        'store_name': data["Store"],
        'date': data["Date"],
        'time': data['Time'],
        'total': total,
        'products': list(products.values())
    }

    print(data)
    return data

def successResponse(values,image,message='success'):
    # can only concatenate str (not "bytes") to str
    res = {
        'data' : values,
        'image' : str(image),
        'message' : message,
    }
    return make_response(jsonify(res), 200)

def badRequest(values,message='error'):
    res = {
        'data' : values,
        'message' : message,
    }
    return make_response(jsonify(res), 400)

if __name__ == '__main__':
    app.run(debug=True)