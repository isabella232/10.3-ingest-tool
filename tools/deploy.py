from json import loads
from getpass import getpass

import sys

with open(sys.argv[1]) as f:
        chi = f.read()

json = loads(chi)

files = []
folders = []
for data in json:
        files.append(json[data]["ingestPropertyFile"])
        folders.append(json[data]["eadFileLocation"])
import pysftp
username = raw_input("Production username: ")
password = getpass("Production password: ")

srv = pysftp.Connection(host="portal.ehri-project.eu", username=username,
        password=password)

srv.makedirs(sys.argv[1].rpartition("/")[0])
srv.put(sys.argv[1], remotepath = sys.argv[1])


for localFile in files:
        print "uploading " + localFile
        srv.makedirs(localFile.rpartition("/")[0])
        srv.put(localFile, remotepath = localFile)
for folder in folders:
        print "uploading " + folder
        srv.makedirs(folder)
        srv.put_r(folder, remotepath = folder)
