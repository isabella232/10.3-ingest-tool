import shutil, os

directory = raw_input("What is the assembly version: ")

dir_path = os.path.dirname(os.path.realpath(__file__))
print dir_path

if not os.path.exists(directory):
    os.makedirs(directory)

# shutil.copytree("../doc/", directory + "/doc")
os.makedirs(directory + "/conf")
shutil.copytree("../css/", directory + "/conf/css/")
shutil.copytree("../rng/", directory + "/conf/rng/")
shutil.copytree("../static/", directory + "/conf/static/")
shutil.copytree("../templates/", directory + "/conf/templates/")
shutil.copytree("../tsv/", directory + "/conf/tsv/")
shutil.copytree("../xquery/", directory + "/conf/xquery/")

shutil.copy2(dir_path + "/../xslt/iso639-2.xml", dir_path + "/" + directory +  "/conf/iso639-2.xml")
shutil.copy2(dir_path + "/../xslt/report.xsl", dir_path + "/" + directory +  "/conf/report.xsl")
shutil.copy2(dir_path + "/../xslt/v1to02.xsl", dir_path + "/" + directory +  "/conf/v1to02.xsl")
shutil.copy2(dir_path + "/../ead2002.struct", dir_path + "/" + directory +  "/conf/ead2002.struct")

shutil.copy2(dir_path + "/../start_scripts/run.bash", dir_path + "/" + directory +  "/run.bash")
shutil.copy2(dir_path + "/../start_scripts/run.bat", dir_path + "/" + directory +  "/run.bat")

shutil.copy2(dir_path + "/../google_key/google-sheet-accessor.key", dir_path + "/" + directory +  "/google-sheet-accessor.key")



os.makedirs(directory + "/lib")
os.makedirs(directory + "/input")
os.makedirs(directory + "/mapping")
os.makedirs(directory + "/output")
os.makedirs(directory + "/xquery")

shutil.copy2(dir_path + "/../../../../target/demo-0.0.1-SNAPSHOT.jar", dir_path + "/" + directory +  "/lib/demo-0.0.1-SNAPSHOT.jar")
# shutil.copy2(dir_path + "/../basex_conf/.basex", dir_path + "/" + directory +  "/lib/.basex")

shutil.make_archive(directory, 'zip', directory)
shutil.move(directory + ".zip", dir_path + "/../../../../target/")
shutil.move(directory, dir_path + "/../../../../target/")