adb backup -f myAndroidBackup.ab net.avh4.outline
dd if=data.ab bs=1 skip=24 | python -c "import zlib,sys;sys.stdout.write(zlib.decompress(sys.stdin.read()))" > myAndroidBackup.tar
