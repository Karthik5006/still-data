#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
still_sdk="${STILL_ANDROID_SDK:-/workspace/android/toolchain/sdk}"
still_ecj="${STILL_ECJ_JAR:-/workspace/android/toolchain/ecj.jar}"
still_tools="$still_sdk/build-tools/35.0.0"
still_platform="$still_sdk/platforms/android-35/android.jar"
still_variant="${1:-release}"
mkdir -p build/generated build/classes build/dex releases .signing
rm -rf build/generated/* build/classes/* build/dex/*
python3 - "$still_variant" <<'PY'
import sys,xml.etree.ElementTree as E
E.register_namespace('android','http://schemas.android.com/apk/res/android')
tree=E.parse('app/src/main/AndroidManifest.xml');root=tree.getroot();root.set('package','app.stilldata.guard')
if sys.argv[1]=='debug':root.find('application').set('{http://schemas.android.com/apk/res/android}debuggable','true')
tree.write('build/AndroidManifest.xml',encoding='utf-8',xml_declaration=True)
PY
"$still_tools/aapt2" compile --dir app/src/main/res -o build/resources.zip
"$still_tools/aapt2" link -I "$still_platform" --manifest build/AndroidManifest.xml --java build/generated --version-code 2 --version-name 1.1.0 -o build/resources.apk build/resources.zip
find app/src/main/java build/generated -name '*.java' > build/sources.txt
java -jar "$still_ecj" -source 1.8 -target 1.8 -proc:none -nowarn -bootclasspath "$still_platform" -classpath "$still_tools/core-lambda-stubs.jar" -d build/classes @build/sources.txt
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
with ZipFile('build/classes.jar','w',ZIP_DEFLATED) as z:
 for path in Path('build/classes').rglob('*.class'):z.write(path,path.relative_to('build/classes'))
PY
java -cp "$still_tools/lib/d8.jar" com.android.tools.r8.D8 --min-api 29 --lib "$still_platform" --output build/dex build/classes.jar
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile,ZIP_STORED
from shutil import copyfile
copyfile('build/resources.apk','build/unaligned.apk')
with ZipFile('build/unaligned.apk','a') as z:
 for path in Path('build/dex').glob('*.dex'):z.write(path,path.name,ZIP_STORED)
PY
if [ ! -f .signing/still-local.jks ]; then
 keytool -genkeypair -keystore .signing/still-local.jks -alias still-local -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Still Data Local Build' -storepass android -keypass android >/dev/null 2>&1
fi
"$still_tools/zipalign" -f -P 16 4 build/unaligned.apk build/aligned.apk
java -jar "$still_tools/lib/apksigner.jar" sign --ks .signing/still-local.jks --ks-key-alias still-local --ks-pass pass:android --key-pass pass:android --out "releases/StillData-1.1-$still_variant.apk" build/aligned.apk
java -jar "$still_tools/lib/apksigner.jar" verify --verbose "releases/StillData-1.1-$still_variant.apk"
"$still_tools/aapt2" dump badging "releases/StillData-1.1-$still_variant.apk" > build/badging.txt
sed -n '1,4p' build/badging.txt
printf 'APK built: releases/StillData-1.1-%s.apk\n' "$still_variant"
