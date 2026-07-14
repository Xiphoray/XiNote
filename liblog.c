#include <stdio.h>
#include <stdarg.h>

int __android_log_print(int prio, const char *tag,  const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    vfprintf(stderr, fmt, ap);
    fprintf(stderr, "\n");
    va_end(ap);
    return 1;
}
