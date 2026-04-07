import glob
import xml.etree.ElementTree as ET

entries = []
for f in glob.glob("target/surefire-reports/TEST-*.xml"):
    tree = ET.parse(f)
    for tc in tree.findall(".//testcase"):
        node = tc.find("failure") or tc.find("error")
        if node is None:
            continue
        class_name = tc.get("classname", "").split(".")[-1]
        method = tc.get("name", "")
        raw = (node.get("message") or (node.text or "")).strip()
        detail = next((l.strip() for l in raw.splitlines() if l.strip()), "")
        if len(detail) > 120:
            detail = detail[:117] + "..."
        if detail:
            entries.append(f"\u2022 *{class_name}.{method}*\n  `{detail}`")
        else:
            entries.append(f"\u2022 *{class_name}.{method}*")

print("\n".join(entries))
