with open("app/build.gradle.kts", "r") as f:
    lines = f.readlines()
with open("app/build.gradle.kts", "w") as f:
    for line in lines:
        if 'implementation("net.java.dev.jna:jna:5.13.0@aar")' in line:
            f.write('  implementation("net.java.dev.jna:jna:5.2.0@aar")\n')
        else:
            f.write(line)
