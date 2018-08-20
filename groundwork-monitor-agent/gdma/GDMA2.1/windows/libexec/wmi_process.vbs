' Copyright 2004-2005 GroundWork OpenSource Solutions
'
' This program is free software; you can redistribute it and/or
' modify it under the terms of the GNU General Public License
' as published by the Free Software Foundation; either version 2
' of the License, or (at your option) any later version.
'
' This program is distributed in the hope that it will be useful,
' but WITHOUT ANY WARRANTY; without even the implied warranty of
' MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
' GNU General Public License for more details.
'
'
' Author Pham Van Hung of ITSP Company (info@itspco.com) 

Function CheckProcess( strComputer, strProcess )
On Error Resume Next

    Dim objWmi, objProcess, collProcesses
    Dim strQuery

    set objWmi = GetObject("winmgmts://" & strComputer )

    If( Err.Number <> 0 ) Then
        CheckProcess = retvalUnknown
	EXPLANATION = "Unable to connect to WMI service on computer [" & strComputer & "]. Possible reasons: remote computer is down, has no WMI installed, or requires other credentials to access WMI"
        Exit Function 
    End If

    strQuery = "select * from win32_process "
    set collProcesses = objWmi.ExecQuery( strQuery ) 

    If( collProcesses.Count = 0 ) Then
        CheckProcess = retvalUnknown
        EXPLANATION = "Unable to list processes on computer [" & strComputer & "]"
        Exit Function			    
    End If

    For Each objProcess in collProcesses
        If( Err.Number <> 0 ) Then
            CheckProcess = retvalUnknown
            EXPLANATION = "Unable to list processes on computer [" & strComputer & "]"
            Exit Function 
        End If

	If UCase( objProcess.Name ) = UCase( strProcess ) Then
            CheckProcess = True
    	    EXPLANATION = "Process [" & strProcess & "] is running on computer [" & strComputer & "]"
            Exit Function
        End If
    Next

    CheckProcess = False
    EXPLANATION = "Process [" & strProcess & "] is not running on computer [" & strComputer & "]"

End Function
'**********************
'Extend Function
'**********************
Function CheckProcessEx( strComputer, strUser,strPassword,strProcess )
On Error Resume Next
    Dim objWmi, objProcess, collProcesses
    Dim strQuery

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objwmi = objSWbemLocator.ConnectServer _
    (strComputer, "root\cimv2",strUser, strPassword)
    objwmi.Security_.ImpersonationLevel = 3

    strQuery = "select * from win32_process "
    set collProcesses = objWmi.ExecQuery( strQuery ) 

    For Each objProcess in collProcesses
	If UCase( objProcess.Name ) = UCase( strProcess ) Then
            CheckProcessEx = True
            Exit Function
        End If
    Next
    CheckProcessEx = False
End Function
'**********************
'Error Function
'**********************
Function f_Error()
  Wscript.echo "Error!"
  wscript.Quit(3)
End Function
'**********************
'Help Function
'**********************
Function f_help()
Dim str
str="Verify the existence of processes.If your Local Machine have the same Administrator account and password as Remote Machine,you don't have to use two last parameters."&vbCrlF&vbCrlF
str=str&"cscript check_process.vbs -h hostname -p process_names [-user username -pass password]"
str=str&vbCrlF
str=str&"-h hostname                   Host name."&vbCrlF
str=str&"-p process_names         Processes names,multiple processes will be enclosed in multiple quotes and separated by commas."&vbCrlF
str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
str=str&vbCrlF
str=str&"Example: cscript check_process.vbs -h Ser1 -p ""explorer.exe,svchost.exe,wmplayer.exe"" [-user SER1\Administrator -pass password] "
wscript.echo str
end function


'******************************
'** Exit Function
'******************************
Function f_exit(arg)

  Dim i,j,tam1,tam2,first
  Dim refix
  Dim sComputer,sProcess
  Dim result

  On Error Resume Next

  arg(3)=Replace(arg(3)," ","")

  result=0


  If (Err.Number<>0) Then
    f_Error()
  Else
    if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
      f_help()
    else
      If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-P") And (Wscript.Arguments.Count=4) then
        sComputer = arg(1)
        sProcess = arg(3)
        first=1
        tam1=10
        if (len(arg(3))>0) then
          do while (tam1>0)
            tam1=InStr(first,arg(3),",")
            if (tam1>0) then
              tam2=Mid(arg(3),first,tam1-first)
              first=tam1+1
              sProcess = tam2
	      i=CheckProcess(sComputer,sProcess)
	      if i=True then
                refix= refix & tam2 & " exists"
    	      else
                result=2
                refix= refix & tam2 & " not exists"
	      end if
              refix=refix&vbCrLf
  	    end if
          Loop
          tam2=Mid(arg(3),first,len(arg(3)))
          sProcess = tam2
          i=CheckProcess(sComputer,sProcess)
          if i=True then            
            refix= refix & tam2 & " exists"
          else            
            result=2
            refix= refix & tam2 & " not exists"
          end if
        end if
        If (Err.Number<>0) Then
          f_Error()
        Else
          Wscript.echo refix
        end if
      else
        If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-P") And (UCase(arg(4))="-USER") And (UCase(arg(6)) = "-PASS") And (Wscript.Arguments.Count=8) then
          sComputer = arg(1)
          sProcess = arg(3)
          first=1
          tam1=10
          if (len(arg(3))>0) then
            do while (tam1>0)
              tam1=InStr(first,arg(3),",")
              if (tam1>0) then
                tam2=Mid(arg(3),first,tam1-first)
                first=tam1+1
                sProcess = tam2
  	        i=CheckProcessEx(sComputer,arg(5),arg(7),sProcess)
    	        if i=True then
                  refix= refix & tam2 & " exists"
  	        else
                  result=2
                  refix= refix & tam2 & " not exists"
	        end if
                refix=refix&vbCrLf
	      end if
            Loop
            tam2=Mid(arg(3),first,len(arg(3)))
            sProcess = tam2
            i=CheckProcessEx(sComputer,arg(5),arg(7),sProcess)
            if i=True then
              refix= refix & tam2 & " exists"
            else
              result=2
              refix= refix & tam2 & " not exists"
            end if
          end if
          If (Err.Number<>0) Then
            f_Error()
          Else
            Wscript.echo refix
          end if
        else
          f_Error()
        end if
      end if
    end if
  end if
  f_exit=result
end function

'******************************
'** Main Function
'******************************

Dim i
Dim arg(20)
Dim result
On Error Resume Next

For i=0 to WScript.Arguments.Count-1
  arg(i)=WScript.Arguments( i )
Next

result=f_exit(arg)
wscript.quit(result)