import os
import cv2.cv2 as cv2
import numpy as np
import pydicom as dicom


def convert_file(dcm_file_path, jpg_file_path):
    print("lalala")
    dicom_img = dicom.read_file(dcm_file_path)
    img = dicom_img.pixel_array
    scaled_img = cv2.convertScaleAbs(
        img-np.min(img), alpha=(255.0 / min(np.max(img)-np.min(img), 10000)))
    cv2.imwrite(jpg_file_path, scaled_img)


#convert_file('D:/影子/合并/1.3.46.670589.30.1.3.114374073544144.1.3200.1495871926015.dcm','D:/test/111.jpg')
rootdir='D:/影子/合并'
list=os.listdir(rootdir)
for i in range(0,len(list)):
    path = os.path.join(rootdir,list[i])
    if os.path.isfile(path):
       filepath,file=os.path.split(path)
       file,dcm=os.path.splitext(file)
       print(file)
       convert_file(path,"D:/test/" + file + ".jpg")

