
import torchvision
from torchvision.models import detection
import numpy as np
import argparse
import pickle
import torch
import cv2
import base64
from xmlrpc.server import SimpleXMLRPCServer

count = 0

DEVICE = torch.device("cuda" if torch.cuda.is_available () else "cpu")
#MODELS = {#
#	#"frcnn-resnet": detection.fasterrcnn_resnet50_fpn,
#	"retinanet": detection.retinanet_resnet50_fpn
#}

# COCO dataset classes
#CLASSES = pickle.loads(open("coco_classes.pickle", "rb").read())

CLASSES = np.asarray([
    '__background__', 'person', 'bicycle', 'car', 'motorcycle', 'airplane', 'bus',
                'train', 'truck', 'boat', 'traffic light', 'fire hydrant',
                'stop sign', 'parking meter', 'bench', 'bird', 'cat', 'dog',
                'horse', 'sheep', 'cow', 'elephant', 'bear', 'zebra',
                'giraffe', 'backpack', 'umbrella', 'handbag', 'tie',
                'suitcase', 'frisbee', 'skis', 'snowboard', 'sports ball',
                'kite', 'baseball bat', 'baseball glove', 'skateboard',
                'surfboard', 'tennis racket', 'bottle', 'wine glass', 'cup',
                'fork', 'knife', 'spoon', 'bowl', 'banana', 'apple',
                'sandwich', 'orange', 'broccoli', 'carrot', 'hot dog', 'pizza',
                'donut', 'cake', 'chair', 'couch', 'potted plant', 'bed',
                'dining table', 'toilet', 'tv', 'laptop', 'mouse', 'remote',
                'keyboard', 'cell phone', 'microwave oven', 'toaster', 'sink',
                'refrigerator', 'book', 'clock', 'vase', 'scissors',
                'teddy bear', 'hair drier', 'toothbrush'
])


# Random colors per class for on image rects
COLORS = np.random.uniform(0, 255, size=(len(CLASSES), 3))

print("loading model...")
# load model and send to device
model = torchvision.models.detection.retinanet_resnet50_fpn(pretrained=True, progress=True, num_classes=91, pretrained_backbone=True)
model.to(DEVICE)
model.eval()
print("retinanet model loaded!")


def send(image):
    global count, model, COLORS, CLASSES
    print("image send...")
    
    image_encode = bytes(image, 'ascii')
    im_bytes = base64.decodebytes(image_encode)
    im_bytes = np.frombuffer(im_bytes, dtype=np.uint8)
    
    #image = cv2.imread("Bananavarieties.jpg")
    image = cv2.imdecode(im_bytes, flags=1)
    
    filename = "image" + str(count) + ".jpg"
    #cv2.imwrite(filename, image)
    
    orig_im = image.copy()

    # swap around color channels and reorder to "channels first"
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    image = image.transpose((2, 0, 1))

    # add the batch dimension, scale the raw pixel intensities to the
    # range [0, 1], and convert the image to a floating point tensor
    image = np.expand_dims(image, axis=0)
    image = image / 255.0
    image = torch.FloatTensor(image)

    # send the input to the device and pass the it through the network to
    # get the detections and predictions
    image = image.to(DEVICE)
    detections = model(image)[0]

    print("image processed")
    
    firstLabel = ""
    
    # loop over the detections
    for i in range(0, len(detections["boxes"])):

        # extract the confidence (i.e., probability) associated with the
        # prediction
        confidence = detections["scores"][i]
        
        # filter out weak detections by ensuring the confidence is
        # greater than the minimum confidence
        if confidence > 0.75:
        
            # extract the index of the class label from the detections,
            # then compute the (x, y)-coordinates of the bounding box
            # for the object
            idx = int(detections["labels"][i])
            box = detections["boxes"][i].detach().cpu().numpy()
            (startX, startY, endX, endY) = box.astype("int")
            
            # display the prediction to our terminal
            label = "{}: {:.2f}%".format(CLASSES[idx], confidence * 100)
            print("[INFO] {}".format(label))
            
            if firstLabel == "":
                firstLabel = label
                
            # draw the bounding box and label on the image
            cv2.rectangle(orig_im, (startX, startY), (endX, endY),
                COLORS[idx], 2)
            y = startY - 15 if startY - 15 > 15 else startY + 15
            cv2.putText(orig_im, label, (startX, y),
                cv2.FONT_HERSHEY_SIMPLEX, 0.5, COLORS[idx], 2)
                
    # show the output image
    #cv2.imshow("Output", orig_im)
    #cv2.waitKey(0)
    #filename = "imageDetect" + str(count) + ".jpg"
    #cv2.imwrite(filename, orig_im)
    count += 1
    print("ML done.")
    return firstLabel
    
    
def isServerOk():
    print("Server ok called.")
    return true
    
server = SimpleXMLRPCServer(("192.168.40.114", 8000))
print("Listening on port 8000...")
server.register_function(send, "send")
server.register_function(isServerOk, "isServerOk")
server.serve_forever()



