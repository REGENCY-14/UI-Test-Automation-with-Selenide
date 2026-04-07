import glob
import xml.etree.ElementTree as ET

names = []
for f in glob.glob("target/surefire-reports/TEST-*.xml"):
    tree = ET.parse(f)
    for tc in tree.findall(".//testcase"):
        if tc.find("failure") is not None or tc.find("error") is not None:
            names.append("\u2022 " + tc.get("classname", "").split(".")[-1] + "." + tc.get("name", ""))

print("\n".join(names))
