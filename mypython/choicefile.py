import os
"""
按照文件里的文件名列表，删除文件夹里相应名称的文件，得到正确删除的数据个数，及未能删除的文件名称
"""

rootdir = 'D:/vscode/SQ0625_阳_YJZ'
ok=0
key=0
faildelete=[]

def deletefile(rootdir, name,ok):
    list = os.listdir(rootdir)
    for it in list:
        #print(it)
        if it == name:
            print('yes')
            ok=ok+1
            os.remove(rootdir+'/'+it)
            key=1
            return key,ok
        

file_object = open('D:/vscode/check-test.text', 'r', encoding='UTF-8')

for line in file_object:
    line=line.strip('\n')
    key,ok=deletefile(rootdir, line,ok)
    if key==0:
        faildelete.append(line)
    key=0
print(faildelete)
print('ok: '+str(ok))

#deletefile('D:\\vscode\\test','陈旧结核 (1).jpg')
