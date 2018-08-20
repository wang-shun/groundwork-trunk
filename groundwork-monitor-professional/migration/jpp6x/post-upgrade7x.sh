#!/bin/bash
#####################################################################################
#
#    Copyright (C) 2013-2018 GroundWork Inc. (www.groundworkopensource.com)
#
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of version 2 of the GNU General Public License
#    as published by the Free Software Foundation.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
#	$Id: post_upgrade7x.sh 04/22/2014 Arul Shanmugam$
#
# 	Simple script to upgrade portal objects. This script should be only run on
#   groundwork 7x systems.Especially on upgrades. This should be the last step
#	after final GW server restart is done. Requires GW server up in running.
#	This script use GW root user privileges to change portal
#   components. Make sure the root credentials are already validated
#   before calling this script. This script doesnt validate it. Also the credentials
#	are unencrypted.
#
#####################################################################################

## Usage function
print_usage () {
    echo "usage: ./post-upgrade7x.sh <GW root user> <GW root password>"
}

if [ $# != 2 ]; then
    print_usage
    exit 1
fi

## The following credential are already validated by caller by calling http[s]://localhost/api/auth/validateRootCredentials
root_user=$1
root_password=$2

# Figure out the correct protocol to use for curl calls below.
if [ `grep secure.access.enabled /usr/local/groundwork/config/status-viewer.properties | cut -f 2 -d=` == 'true' ]; then
    protocol="https"
else
    protocol="http"
fi

failed=0

portal_object_dir_paths=( \
    /usr/local/groundwork/core/migration/portal-objects-702 \
    /usr/local/groundwork/core/migration/portal-objects-710 \
    /usr/local/groundwork/core/migration/portal-objects-721 \
)

for portal_objects_dir_path in "${portal_object_dir_paths[@]}"
do
    echo "Processing the $portal_objects_dir_path directory ..."
    portal_objects_paths=$portal_objects_dir_path/*
    for portal_objects_path in $portal_objects_paths
    do
	echo "Processing the $portal_objects_path file ..."
	portal_objects_file=`basename $portal_objects_path`
	if [ "$portal_objects_file" = "navigational-nodes-to-delete" ]; then
	    echo "Deleting nodes listed in the $portal_objects_path file:"
	    tmpdir=/usr/local/groundwork/tmp
	    navigation_xml=$tmpdir/navigation.xml
	    ## First, we extract just the portion that we need, not the full portal setup, into the $navigation_xml file.
	    ## (If in the future we decide we might also want to delete something in portal/classic/pages.xml then we can
	    ## reach back into an older copy of this script and see how we pulled back the entire portal setup, not just
	    ## the one file we currently need here.)
	    echo "Retrieving the portal navigational setup ..."
	    rm -f $navigation_xml
	    /usr/local/groundwork/common/bin/curl --insecure --user $root_user:$root_password \
		$protocol://localhost/rest/private/managed-components/mop/portalsites/classic/navigation.xml -o $navigation_xml
	    if [ $? -ne 0 -o ! -f $navigation_xml -o ! -s $navigation_xml ]; then
		echo "ERROR:  Skipping menu-item deletion due to navigation retrieval failure noted above."
	    else
		failed_to_edit=0
		## Save the original navigation tree for comparison purposes, so we can easily see what changes get made.
		cp -p $navigation_xml $navigation_xml.orig
		## We would use the bash "readarray" command to pull in the data, except that it apparently did not exist until bash 4.
		## Since we have some old test machines still using bash 3, we somewhat reluctantly opt for the clumsier approach.
		## Here's one construction that should work in bash 3, as long as there is no extra whitespace in the file:
		##     while IFS= read -r line; do [ -n "$line" ] && nodes_to_delete+=("$line"); done < $portal_objects_path
		## Here's a simple alternate construction that should work in bash 3, as long as the input contains no
		## EOT (Ctrl-D) characters.  I like this one better because it is insensitive to extra whitespace in the file,
		## only extracting full words not containing any internal whitespace.
		read -a nodes_to_delete -d $'\cD' -r < $portal_objects_path
		# The "read" command will have supposedly failed (as reflected in its exit status) even when it successfully
		# read the input file, so we cannot usefully check its exit status.
		for node_to_delete in "${nodes_to_delete[@]}"
		do
		    ## We quote the node name so as to prove that it contains no extra leading or trailing whitespace.
		    echo "Deleting node from the navigation.xml file:  '$node_to_delete'"
		    ## Call modify_navigation_objects to edit the $navigation_xml content to delete each node.
		    ## This just does a local file edit; it doesn't upload anything back to the portal at this stage.
		    ## It is perhaps somewhat inefficient to make multiple passes this way, only deleting one node
		    ## per pass, but this is a rare invocation so we're not terribly concerned about efficiency.
		    /usr/local/groundwork/core/migration/modify_navigation_objects $navigation_xml $node_to_delete DELETE $navigation_xml
		    if [ $? -ge 2 ]; then
			echo "FATAL:  Cannot modify navigation.xml to delete the $node_to_delete menu item."
			failed_to_edit=1
			failed=1
			break
		    fi
		done
		## Finally, pump the final $navigation_xml data back into the portal,
		## completely replacing the full previous set of navigational data.
		if [ $failed_to_edit -ne 0 ]; then
		    echo "ERROR:  Skipping all node deletions in the $portal_objects_dir_path directory, due to earlier failure."
		else
		    echo "Redefining the portal setup ..."

		    ## Prepare the stuff we will import back into the JBoss portal.  Note that we are careful
		    ## to first clean up any debris that may have been left behind by previous attempts, so as
		    ## not to confuse ourselves.
		    rm -rf   $tmpdir/portal
		    mkdir -p $tmpdir/portal/classic

		    ## Stuff our updated navigation-menu data into a file tree that the JBoss portal will
		    ## recognize once we zip it up and send it in for importing.
		    cp -p $navigation_xml $tmpdir/portal/classic/

		    ## Name the zip-file that we will pass to the JBoss portal.
		    portal_objects_dir=`basename $portal_objects_dir_path`
		    new_portal_objects_file="$tmpdir/${portal_objects_dir}-deletions.zip"

		    ## Before we go creating the zip-file, we must remove any previous copy.
		    ## That's because the zip command will alter an existing file instead of
		    ## creating an entirely new file containing only the files you specify
		    ## during this run.  Leftovers from some previous run could therefore
		    ## considerably confuse the situation in this run.
		    rm -f "$new_portal_objects_file"

		    ## Create the zipfile packaging that JBoss accepts.
		    echo "Creating a zipfile of updated setup for uploading to the JBoss portal ..."
		    (cd $tmpdir; /usr/local/groundwork/common/bin/zip -r "$new_portal_objects_file" portal)
		    if [ $? -ne 0 ]; then
			echo "ERROR:  Could not create a portal configuration zipfile."
			echo "FATAL:  Ending all portal updating, due to earlier failure."
			exit 1;
		    fi

		    ## The "?importMode=overwrite" argument is reqired to allow node deletions to be recognized.
		    ## Otherwise, the portal will only process node additions.
		    curl_output=`/usr/local/groundwork/common/bin/curl --insecure --user "$root_user:$root_password" \
			-H 'Content-Type: application/zip' --upload-file "$new_portal_objects_file" \
			"$protocol://localhost/rest/private/managed-components/mop?importMode=overwrite"`
		    ## This is unlikely to detect an actual failure, because the return value from the curl
		    ## command essentially tells whether or not it interacted with the remote side, not whether 
		    ## the remote side experienced a problem with the uploaded data.
		    if [ $? -ne 0 ]; then
			echo "ERROR:  Could not update the JBoss portal site configuration:"
			echo "        $curl_output"
			echo "FATAL:  Ending all portal updating, due to earlier failure."
			exit 1;
		    fi

		    ## Here are possible error outputs we have seen in testing, from that last curl command.
		    ## These are shown here to indicate why we are scanning for the word "failure" below to
		    ## see if there was some problem.  Logically, we could decode this JSON and look at the
		    ## one field, but our simple string matching is adequate for now.
		    ##
		    ##   {"operationName":"import-resource","failure":"Exception reading data for import."}
		    ##   {"operationName":"import-resource","failure":"Error during import. Tasks successfully rolled back. Portal should be back to consistent state."}
		    ##
		    ## If you do get a failure, look in the foundation/container/jpp/standalone/log/framework.log
		    ## file to find details.  In particular, look for the related "Caused by" line, which
		    ## provides the necessary extra detail. 
		    if [[ $curl_output =~ 'failure' ]]; then
			echo "ERROR:  Could not update the JBoss portal site configuration:"
			echo "        $curl_output"
			# This filtering might catch some other system error that just happened to come along
			# as we were performing our work, not the one we're looking for.  But it's worth trying, 
			# anyway, as a debugging convenience.
			fgrep 'Caused by' /usr/local/groundwork/foundation/container/jpp/standalone/log/framework.log | tail -1
			failed=1
		    elif [ -n "$curl_output" ]; then
			# Here we dump out the curl output to make it visible regardless of whether we were
			# able to detect that an actual error occurred.  We do this because our simplistic
			# error analysis above might not be comprehensive, and we don't want any completely
			# silent failures.
			echo "NOTICE:  Output when updating the JBoss portal site configuration:"
			echo "         $curl_output"
		    fi
		fi
	    fi
	    echo "Done with deleting nodes listed in the $portal_objects_path file."
	else
	    # NOTES
	    # * we use "--insecure" to avoid cert issues
	    # * we might try --noproxy '*' to avoid proxy issues, but that doesn't work on SLES 11; will have a technote to deal with proxies
	    /usr/local/groundwork/common/bin/curl --insecure --user $root_user:$root_password \
		-H 'Content-Type: application/zip' $protocol://localhost/rest/private/managed-components/mop --upload-file $portal_objects_path

	    # NOTES
	    # * the next if/then tests the return status of curl; note that this is zero if curl works,
	    #   and that includes if the content returned is full of errors
	    # * validation of the --user creds should have been done earlier in the installation/upgrade
	    if [ $? -ne 0 ]; then
		echo "FATAL:  Ending all portal updating, due to earlier failure."
		exit 1;
	    fi
	fi
    done
done

if [ $failed -eq 0 ]; then
    echo "Updated portal objects successfully."
else
    echo "ERROR:  Failed to update all portal objects."
    exit 1
fi
