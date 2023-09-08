# Indonesian-Receipt-Detector-using-LayoutLM
Finetuned on the LayoutLMv3 model to detect and read indonesian receipt

### App Prototype Preview :

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/560dc8be-dada-4a48-9ca4-40bf1108bbb1" width="300" height="500" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/b5ba51f5-bca3-41b5-8921-34bd7d2c9811" width="300" height="500" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/793c60a4-c2c4-41c8-b457-70dbfbea0e8e" width="300" height="500" />

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/8e37a0c4-1822-46f9-9272-1823496f6e7a" width="300" height="500" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/bca24552-8f88-4273-8e52-463c0f7661a1" width="300" height="500" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/e3c49c84-71db-4faf-b2fa-89d38567e65f" width="300" height="500" />


<!---
![293477](https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/560dc8be-dada-4a48-9ca4-40bf1108bbb1=100x20)
![293476](https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/b5ba51f5-bca3-41b5-8921-34bd7d2c9811)
![293478](https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/793c60a4-c2c4-41c8-b457-70dbfbea0e8e)
-->

## How it works :

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/fdc71d0e-91e9-4f06-ab87-d154b55c0fb9" width="400" height="400" />


  1. The user scans a receipt with the app and sends the image to a WebServer for Google OCR processing and LayoutLMv3 inference
  2. The Web Server then sends back the aquired data to the Android apps for the user to validate
  3. The data can then be saved in SQLite format on the Android App to calculate monthly expenses

### Server Inference :

A.	Data Gathering

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/d9278119-6d67-48e4-84a4-50b78c57b20f" width="300" height="500" />

B.	Data Annotation

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/2af8d763-653d-4ebe-87bb-63097ad20008" width="800" height="500" />

C.	LayoutLMv3 Inference

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/6e5de1db-6860-44d6-88df-35c5b3161d6b" width="300" height="500" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/53982b97-150a-46d9-94ab-557c8c2ec37a" width="300" height="500" />

D.	Information Extraction

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/529a3e88-832d-49a4-b823-13905b7937b5" width="300" height="500" />

Finetuning Result:

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/951cfc3f-211c-4f47-8e72-e914a3a1fd04" width="500" height="150" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/bd42be59-65b8-49bc-9b03-b7bcfc2f7d6f" width="500" height="150" />

Training Result:

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/41ee142b-4e86-4963-9839-7724870de526" width="300" height="300" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/82efdbf5-e50f-476e-97e4-d60ae33c1d8e" width="300" height="300" />

Evaluation Result:

<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/2f84e190-9f88-4b64-a2d9-c8487f196974" width="300" height="300" />
<img src="https://github.com/andre002wp/Indonesian-Receipt-Detector-using-LayoutLM/assets/24908637/4b317087-e267-4846-9cbc-3df29de0f394" width="300" height="300" />

Literature Reference:
>Y. Huang, T. Lv, L. Cui, Y. Lu, F. Wei, LayoutLMv3: Pre-training for Document AI with Unified Text and Image Masking, in: Association for Computing Machinery (ACM), 2022: pp. 4083–4091. https://doi.org/10.1145/3503161.3548112.












