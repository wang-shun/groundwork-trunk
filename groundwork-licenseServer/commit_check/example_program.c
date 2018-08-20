#include <stdlib.h>
#include <stdio.h>
#include "extern.h"

int OBFN_function ()
    {
    return 0;
    }

char *OBFN_second_function ()
    {
    return OBFS_string("foobarf\n");
    }

int main (int argc, char *argv[])
    {
    OBFN_function();
    int foo = (int) OBFF_external_function(123);
    printf (OBFN_second_function());
    printf (OBFS_string("Another string encoded.\n"));
    printf (OBFS_string("Not to mention another.\n"));
    return EXIT_SUCCESS;
    }
