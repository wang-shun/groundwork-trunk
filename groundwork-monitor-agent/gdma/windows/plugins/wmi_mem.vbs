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

Function GetTotalMem( strComputer)

On Error Resume Next
    Dim objWMIService, colOS,objOS
    Set objWMIService = GetObject("winmgmts:\\" & strComputer)

    Set colOS = objWMIService.InstancesOf("CIM_OperatingSystem")

	 For Each objOS In colOS
        	GetTotalMem= objOS.TotalVisibleMemorySize
		Exit Function	
         Next

End Function


Function GetFreeMem( strComputer)

On Error Resume Next

    Dim objWMIService, colOS,objOS

    Set objWMIService = GetObject("winmgmts:\\" & strComputer)

    Set colOS = objWMIService.InstancesOf("CIM_OperatingSystem")

	For Each objOS In colOS
        	GetFreeMem = objOS.FreePhysicalMemory
		Exit Function	
	Next

End Function

'**********************
'Extend Function
'**********************
Function GetTotalMemEx( strComputer,strUser,strPassword)
On Error Resume Next
    Dim objWMIService, colOS,objOS
    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objWMIService = objSWbemLocator.ConnectServer _
    (strComputer, "root\cimv2",strUser, strPassword)
    objWMIService.Security_.ImpersonationLevel = 3

    Set colOS = objWMIService.InstancesOf("CIM_OperatingSystem")

	 For Each objOS In colOS
        	GetTotalMemEx= objOS.TotalVisibleMemorySize
		Exit Function	
         Next

End Function


Function GetFreeMemEx( strComputer,strUser,strPassword)
On Error Resume Next

    Dim objWMIService, colOS,objOS

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objWMIService = objSWbemLocator.ConnectServer _
    (strComputer, "root\cimv2",strUser, strPassword)
    objWMIService.Security_.ImpersonationLevel = 3

    Set colOS = objWMIService.InstancesOf("CIM_OperatingSystem")

	For Each objOS In colOS
        	GetFreeMemEx = objOS.FreePhysicalMemory
		Exit Function	
	Next

End Function

'**********************
'Error Function
'**********************
Function f_Error()
  Wscript.echo "Error!"
  Wscript.Quit(intError)
End Function

'**********************
'Help Function
'**********************
Function f_help()
Dim str
str="Check Memory Utilization.If your Local Machine have the same Administrator account and password as Remote Machine,you don't have to use two last parameters."&vbCrlF&vbCrlF
str=str&"cscript check_mem.vbs -h hostname -w warning_level -c critical_level [-user username -pass password]"
str=str&vbCrlF
str=str&"-h hostname                   Host name."&vbCrlF
str=str&"-w warning_level            Warning threshold."&vbCrlF
str=str&"-c critical_level                Critical threshold."&vbCrlF
str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
str=str&vbCrlF
str=str&"Example: cscript check_mem.vbs -h Ser1 -w 30 -c 70 [-user SER1\Administrator -pass password] "
wscript.echo str
end function
'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim Total,Free,percent
Dim sComputer
Dim refix

'Cons for return val's
Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3

On Error Resume Next
For i=0 to WScript.Arguments.Count-1
  arg(i)=WScript.Arguments( i )
Next

If (Err.Number<>0) Then
  f_Error()
Else
  if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
	f_help()
  else
    If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-W") And (UCase(arg(4)) = "-C") And (Wscript.Arguments.Count=6) then
      sComputer = arg(1)
      Total=GetTotalMem(sComputer)
      Free=GetFreeMem(sComputer)
      percent=Int(FormatNumber((Total-Free)*100/Total,0,0,0,0))
      If (Err.Number<>0) Then
        f_Error()
      Else
        if (percent<Int(arg(3))) then
          refix= "OK - "
          Wscript.Echo refix & "Memory Utilization "&percent&"%" & " | 'Memory'=" & percent & "%" & ";" & arg(3) & ";" & arg(5) & ";" & ";"
          Wscript.Quit(intOK)
        else
          if (percent <Int(arg(5))) then
           refix= "Warning - "
           Wscript.Echo refix & "Memory Utilization "&percent&"%" & " | 'Memory'=" & percent & "%" & ";" & arg(3) & ";" & arg(5) & ";" & ";"
           Wscript.Quit(intWarning)
          else
            refix= "Critical - "
            Wscript.Echo refix & "Memory Utilization "&percent&"%" & " | 'Memory'=" & percent & "%" & ";" & arg(3) & ";" & arg(5) & ";" & ";"
            Wscript.Quit(intCritical)
          end if
        end if
      end if
    else
      If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-W") And (UCase(arg(4)) = "-C") And (UCase(arg(6)) = "-USER") And (UCase(arg(8)) = "-PASS") And (Wscript.Arguments.Count=10) then
        sComputer = arg(1)
        Total=GetTotalMemEx(sComputer,arg(7),arg(9))
        Free=GetFreeMemEx(sComputer,arg(7),arg(9))
        percent=Int(FormatNumber((Total-Free)*100/Total,0,0,0,0))
        If (Err.Number<>0) Then
          f_Error()
        Else        
          if (percent<Int(arg(3))) then
            refix= "OK - "
            Wscript.Echo refix & "Memory Utilization "&percent&"%" & " | 'Memory'=" & percent & "%" & ";" & arg(3) & ";" & arg(5) & ";" & ";"
            Wscript.Quit(intOK)
          else
            if (percent <Int(arg(5))) then
              refix= "Warning - "
              Wscript.Echo refix & "Memory Utilization "&percent&"%" & " | 'Memory'=" & percent & "%" & ";" & arg(3) & ";" & arg(5) & ";" & ";"
              Wscript.Quit(intWarning)
            else
              refix= "Critical - "
              Wscript.Echo refix & "Memory Utilization "&percent&"%" & " | 'Memory'=" & percent & "%" & ";" & arg(3) & ";" & arg(5) & ";" & ";"
              Wscript.Quit(intCritical)
            end if
          end if    
        End if
      else
        f_error()
      end if
    end if
  end if
end if