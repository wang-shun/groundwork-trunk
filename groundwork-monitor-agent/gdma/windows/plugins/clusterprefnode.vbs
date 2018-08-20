' Copyright 2008 GroundWork Open Source Inc.
'
' This program is free software; you can redistribute it and/or
' modify it under the terms of the GNU General Public License
' as published by the Free Software Foundation; version 2
' of the License.
'
' This program is distributed in the hope that it will be useful,
' but WITHOUT ANY WARRANTY; without even the implied warranty of
' MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
' GNU General Public License for more details.

'*************************************************************************************************
'*************************************************************************************************
Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3
Const intUnknown = 3

Dim argcountcommand
Dim arg(20)

Dim strClustername, strResourcegroup, strExpectednode, strClustercmd

'*************************************************************************************************
'                                        Functions and Subs
'*************************************************************************************************

'-------------------------------------------------------------------------------------------------
'Function Name:     f_Error.
'Descripton:        Display an error notice include : Error Number and Error Description.
'Input:				No.
'Output:			No.
'-------------------------------------------------------------------------------------------------
Function f_Error()

	nbrError = err.number
	if (nbrError <> 0 ) then
		Select Case nbrError
			Case 462, -2147023174
				strExitMsg = "Timeout connecting to WMI on this host! Error Number: " & nbrError & " Description: " & err.description
			Case -2147024891
				strExitMsg = "Authentication failure to remote host! Error Number: " & nbrError & " Description: " & err.description
			Case -2147217392
				strExitMsg = "Error! Number: " & nbrError & " Description: Invalid Class"
			Case Else
				strExitMsg = "Error! Number: " & nbrError & " Description: " & err.description
		End Select
		wscript.echo strExitMsg
		wscript.quit(intUnknown)
	end if

End Function


'-------------------------------------------------------------------------------------------------
'Function Name:     f_Help.
'Descripton:        Display help of command include : Description, Arguments, Examples
'Input:				No.
'Output:			No.
'-------------------------------------------------------------------------------------------------
Function f_Help()

	Dim str
        str="This script checks if a resource group is attached to a specified node name or not." &vbcrlf
	str = str & "If the resource group is not owned by the specified node name, a warning will be" &vbcrlf
	str = str & "produced. " &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "Resource group ownership for a cluster can be determined through the 'cluster' command " &vbcrlf
	str = str & "using the 'group' subcommand. For example :" &vbcrlf
        str = str & vbcrlf & vbcrlf
        str = str & "cluster /cluster:ATLPRODSQLC01 group" &vbcrlf
        str = str & vbcrlf & vbcrlf
        str = str & "This command produces the following output :" &vbcrlf
        str = str & vbcrlf & vbcrlf
        str = str & "     Listing status for all available resource groups:" &vbcrlf
        str = str & vbcrlf & vbcrlf
        str = str & "     Group                Node            Status" &vbcrlf
        str = str & "     -------------------- --------------- ------" &vbcrlf
        str = str & "     ATLPRODSQLC01V1      ATLPRODSQLC01N1 Online" &vbcrlf
        str = str & "     ATLPRODSQLC01V2      ATLPRODSQLC01N2 Online" &vbcrlf
        str = str & "     Cluster Group        ATLPRODSQLC01N1 Online" &vbcrlf
        str = str & vbcrlf & vbcrlf
        str = str & "This script also produces a warning if the 'Cluster Group' resource group is not owned " &vbcrlf
	str = str & "by a node with name ending in a 1. This special resource group contains metadata about " &vbcrlf
	str = str & "the cluster itself and PeopleClick typically configure this to be owned by node 1." &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "This script returns a critical status on the following two conditions :" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "   1. the specified resource group was not found" &vbcrlf
	str = str & "   2. the special 'Cluster Group' resource group is not found" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "Options" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "   -clustername <clustername, eg ATLPRODSQLC01>  - name of cluster to check" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "   -resourcegroup <resgrp, eg ATLPRODSQLC01V1>   - name of resource group to check" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "   -expectednode <nodename, eg ATLPRODSQLC01N1>  - name of expected owner node of resource group" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "   -clustercmd <fully qualified cluster command, eg \\hostname\admin$\system32\cluster.exe> - fully qualified" &vbcrlf
	str = str & "       name of the cluster exe - unc paths ok." &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "   -h - show this help" &vbcrlf
        str = str & vbcrlf & vbcrlf
        str = str & "Example:" &vbcrlf
        str = str & vbcrlf & vbcrlf
	str = str & "cscript //nologo cluster.vbs " &vbcrlf
	str = str & "     -clustername <clustername, eg ATLPRODSQLC01>   " &vbcrlf
	str = str & "     -resourcegroup <resgrp, eg ATLPRODSQLC01V1>  " &vbcrlf
	str = str & "     -expectednode <nodename, eg ATLPRODSQLC01N1>" &vbcrlf
	str = str & "     -clustercmd <fully qualified cluster command, eg c:\windows\cluster.exe>" &vbcrlf
	
  	wscript.echo str
End Function

'-------------------------------------------------------------------------------------------------
'Function Name:     f_GetAllArg.
'Descripton:        Get all of arguments from command.
'Input:				No.
'Output:			No.
'-------------------------------------------------------------------------------------------------
Function f_GetAllArg()

	On Error Resume Next
	
	Dim i
	
	argcountcommand=WScript.Arguments.Count
	
	for i=0 to argcountcommand-1
  			arg(i)=WScript.Arguments(i)
	next
	
End Function

'-------------------------------------------------------------------------------------------------
'Function Name:     f_GetOneArg.
'Descripton:        Get an argument from command.
'Input:				Yes.
'						strName: Name of argument
'Output:			Value.
'-------------------------------------------------------------------------------------------------
Function f_GetOneArg(strName)

	On Error Resume Next
	
	Dim i
	for i=0 to argcountcommand-1
		if (Ucase(arg(i))=Ucase(strName)) then
			f_GetOneArg=arg(i+1)
			Exit Function
		end if
	next
	
End Function

'-------------------------------------------------------------------------------------------------
function shellExec(strCommand)
   Dim WSHShell, execObj, output
   set WSHShell = CreateObject("Wscript.shell")
   set execObj= WshShell.Exec(strcommand)
   shellExec = execObj.StdOut.ReadAll
End function

'-------------------------------------------------------------------------------------------------
Function f_checkcluster()

	On Error Resume Next

        'run cluster command
        'check output for matching resource group - error out if not found
        'get the node for the resource group 
        'WARNING if the found node doesn't match the expected node
        '
        'check for a "Cluster Group" group - critical error out if not found
        'check that the Cluster Group node ends in a 1 - WARNING if not

	Dim strClustercmdOutput, strCmd
	Dim arrayOutput, element, stringRow, strfirstword, intgroupfound, strnode

	strCmd = strClustercmd & " /cluster:" & strClustername & " group" ' EG: cluster /cluster:ATLPRODSQLC01 group

        strClustercmdOutput = shellExec (strCmd)
	if ( isempty(strClustercmdOutput) ) then
		wscript.echo "Error - command '" & strCmd & "' produced no output"
		wscript.quit (intError)
	end if
        'wscript.echo "output was :" & vbcrlf & strClustercmdOutput

        'check output for matching resource group - error out if not found
        arrayOutput = Split(strClustercmdOutput,vbCrLf)
        intgroupfound = 0
        intCGgroupfound = 0
        for element = 0 To UBound(arrayOutput)
            stringRow = ucase( arrayOutput(element) )

            'Group                Node            Status
	    ' get the first word from this line
	    strfirstword = left(stringRow, instr(stringRow," ")-1)

	    if ( strfirstword = strResourcegroup ) then
		 intgroupfound = 1
                 'get the node for the resource group 
	         strnode = ltrim( mid(stringRow, instr(stringRow," "), len(stringRow)) )
	         strnode = left(strnode, instr(strnode," ")-1)

                 'WARNING if the found node doesn't match the expected node
		 if ( strnode <> strExpectednode ) then
			 wscript.echo "WARNING - cluster resource group " & strResourcegroup & " is not owned by node " & strExpectednode & _
			  " (owned by node " & strnode & " instead)"
			 wscript.quit ( intWarning )
	         
		 end if
	    end if

	    ' check that there's a "Cluster Group " group too starting in posn 1
	    intCG = instr(stringRow, "CLUSTER GROUP ")
	    if ( intCG = 1 ) then
		 intCGgroupfound = 1
		 ' make a note of the node owning this special res grp
		 stringRow = ltrim(replace(stringRow,"CLUSTER GROUP ",""))
	         strGCnode = left(stringRow, instr(stringRow," ")-1)
            end if

        next

        if  intgroupfound = 0  then
	        wscript.echo "CRITICAL - Resource group " & strResourcegroup & " not found"
		wscript.quit (intCritical)
        end if

        if  intCGgroupfound = 0  then
		' error if there's no resource group called 'Cluster Group'
	        wscript.echo "CRITICAL - Resource group 'Cluster Group' not present"
		wscript.quit (intCritical)
	else 
		' resource group 'Cluster Group' found, check cg node ends in 1
		strlastletter = mid( strGCnode, len(strGCnode), len(strGCnode) )
		if ( strlastletter <> "1" ) then
			wscript.echo "Resource group 'Cluster Group' node name should end in a '1' - found '" & strGCnode & "'"
			wscript.quit ( intWarning )
		end if

        end if
      
	' if got this far then ok
	wscript.echo "OK - Resource Group " & strResourcegroup & " is owned by node " & strExpectednode & _
	             ". 'Cluster Group' resource group is owned by node " & strGCnode


End Function


'*************************************************************************************************
'                                        Main Function
'*************************************************************************************************

f_GetAllArg()

if ((UCase(arg(0))="-H")) and (argcountcommand=1) then
	f_help()
	wscript.quit(intError)
end if

strClustername = f_GetOneArg("-clustername") 
if IsEmpty(strClustername) then
	wscript.echo "Incorrect arguments - supply a cluster name- use -h for help"
	wscript.quit(intError)
end if
strClustername = ucase(strClustername)

strResourcegroup = f_GetOneArg("-resourcegroup") 
if IsEmpty(strResourcegroup) then
	wscript.echo "Incorrect arguments - supply a resource group- use -h for help"
	wscript.quit(intError)
end if
strResourcegroup = ucase(strResourcegroup)

strExpectednode = f_GetOneArg("-expectednode") 
if IsEmpty(strExpectednode) then
	wscript.echo "Incorrect arguments - supply an expected node name- use -h for help"
	wscript.quit(intError)
end if
strExpectednode = ucase(strExpectednode)

strClustercmd = f_GetOneArg("-clustercmd") 
'strClustercmd = "\perl\bin\perl cluster.pl"   
if IsEmpty(strClustercmd) then
	wscript.echo "Incorrect arguments - supply a fully qualified cluster command- use -h for help"
	wscript.quit(intError)
end if


f_checkcluster()


