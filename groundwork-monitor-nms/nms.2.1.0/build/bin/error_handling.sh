# This script is here to be sourced from other build scripts, so we only need to
# maintain a single copy of the error-handling functions defined here.

# This function may be called either as "command || bomb_out command" or as:
#     command
#     bomb_out command
# The second form may be more convenient in some circumstances, and it is why
# we include the status-code checking within the function that would be redundant
# if the function were invoked via the first form.

export NMSDIR=/usr/local/groundwork/nms
export GWDIR=/usr/local/groundwork

bomb_out() {
    if [ $? -ne 0 ]; then
	echo "ERROR:  $1 failed; exiting!"
        exit 1
    fi
}
