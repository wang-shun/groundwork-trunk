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

Function PrintQueue( strComputer)
On Error Resume Next

    Dim objWmi, objPrintQueue, collPrintQueue
    Dim strQuery

    set objWmi = GetObject("winmgmts:\\" & strComputer )

    If( Err.Number <> 0 ) Then
        PrintQueue = retvalUnknown
		EXPLANATION = "Unable to connect to WMI service on computer [" & strComputer & "]. Possible reasons: remote computer is down, has no WMI installed, or requires other credentials to access WMI"
        Exit Function 
    End If

    strQuery = "Select Jobs from Win32_PerfRawData_Spooler_PrintQueue"
    set collPrintQueue = objWmi.ExecQuery( strQuery ) 
    For Each objPrintQueue in collPrintQueue
      PrintQueue=objPrintQueue.Jobs
    Next    
End Function
'**********************
'Extend Function
'**********************
Function PrintQueueEx( strComputer, strUser,strPassword)
On Error Resume Next

	Dim objWmi, objPrintQueue, collPrintQueue
    Dim strQuery
    Dim objSWbemLocator

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objwmi = objSWbemLocator.ConnectServer _
    (strComputer, "root\cimv2",strUser, strPassword)
    objwmi.Security_.ImpersonationLevel = 3

    If( Err.Number <> 0 ) Then
        PrintQueueEx = retvalUnknown
		EXPLANATION = "Unable to connect to WMI service on computer [" & strComputer & "]. Possible reasons: remote computer is down, has no WMI installed, or requires other credentials to access WMI"
        Exit Function 
    End If

    strQuery = "Select Jobs from Win32_PerfRawData_Spooler_PrintQueue"
    set collPrintQueue = objWmi.ExecQuery( strQuery ) 
    For Each objPrintQueue in collPrintQueue
      PrintQueueEx=objPrintQueue.Jobs
    Next    
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
  str="Check Jobs in Printer Queue. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript check_printque.vbs -h hostname -w warning_level -c critical_level [-user username -pass password]"
  str=str&vbCrlF
  str=str&"-h hostname                   Host name."&vbCrlF  
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level                Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: cscript check_printque.vbs -h Ser1 -w 2 -c 5 [-user Ser1\Administrator -pass password] "
  wscript.echo str
end function


'******************************
'** Exit Function
'******************************
Function f_exit(arg)

  Dim refix
  Dim sComputer
  Dim result

  On Error Resume Next

  result=0
  If (Err.Number<>0) Then
    wscript.echo "adfadf"
    f_Error()
  Else
    if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
      f_help()
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (Wscript.Arguments.Count=6) then
        sComputer = arg(1)
        result=PrintQueue(sComputer)

        if (Err.Number > 0) Then		
	      f_Error()
    	else 
          if (result < CLng(arg(3))) then
            prefix = "OK - "
            Wscript.Echo prefix & " Jobs are "& result & " | 'Print Jobs'=" & result & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          else 
            if (result < CLng(arg(5))) then
              prefix = "Warning - "
              Wscript.Echo prefix & " Jobs are "& result & " | 'Print Jobs'=" & result & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
            else 
              prefix = "Critical - "
              Wscript.Echo prefix & " Jobs are "& result & " | 'Print Jobs'=" & result & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
            end if
          end if
        end if
      else
        if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (UCase(arg(6))="-USER") And (UCase(arg(8))="-PASS") And (Wscript.Arguments.Count=10) then
          sComputer = arg(1)
          result=PrintQueueEx(sComputer, arg(7), arg(9))
          
          if (Err.Number > 0) Then		
	        f_Error()
    	  else 
            if (result < CLng(arg(3))) then
              prefix = "OK - "
              Wscript.Echo prefix & " Jobs are "& result & " | 'Print Jobs'=" & result & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
            else 
              if (result < CLng(arg(5))) then
                prefix = "Warning - "
                Wscript.Echo prefix & " Jobs are "& result & " | 'Print Jobs'=" & result & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
              else 
                prefix = "Critical - "
                Wscript.Echo prefix & " Jobs are "& result & " | 'Print Jobs'=" & result & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
              end if
            end if
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