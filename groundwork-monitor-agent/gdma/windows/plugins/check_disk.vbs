' Copyright 2004-2006 GroundWork Open Source, Inc.
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
' Author Dave Blunt of GroundWork Open Source, Inc. 

Function GetPercentDisk(strComputer,strDisk,Total,Free)
  On Error Resume Next
  Dim objWMIService, colOS,objOS, FreeDisk, TotalDisk, PercentDisk
  Set objWMIService = GetObject("winmgmts:\\" & strComputer)

  Set colOS = objWMIService.ExecQuery("select FreeSpace,Size,DeviceID From Win32_LogicalDisk where DeviceID='" & strDisk & "'")

  if (colOS.count = 0) then
    f_Error("Error! Can't get instance!")
  end if
  for Each objOS In colOS
    if objOS.DeviceID=strDisk then
      if (IsNumeric(objOS.FreeSpace) AND IsNumeric(objOS.Size)) then
        FreeDisk=CDBl(objOS.FreeSpace)
        TotalDisk=CDBl(objOS.Size)
      else
	f_Error("Error! Data for disk is invalid - maybe a CDROM or floppy?")
      end if
    end if  
  next

  if (IsNumeric(TotalDisk) AND TotalDisk > 0) then
     PercentDisk=(TotalDisk-FreeDisk)*100/TotalDisk
     if (PercentDisk >= 0.5) then
       GetPercentDisk=Int(FormatNumber(PercentDisk,0,0,0,0))
     else
       GetPercentDisk=0
     end if
     Total=TotalDisk
     Free=FreeDisk
  else
    f_Error("Error! No such disk!")
  end if
End Function




Function GetPercentDiskEx(strComputer,strUser,strPassword,strDisk,Total,Free)
  On Error Resume Next
  Dim objWMIService, colOS,objOS, FreeDisk, TotalDisk
  Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")

  Set objWMIService = objSWbemLocator.ConnectServer _
  (strComputer, "root\cimv2",strUser, strPassword)

  objWMIService.Security_.ImpersonationLevel = 3

  Set colOS = objWMIService.ExecQuery("select FreeSpace,Size,DeviceID From Win32_LogicalDisk where DeviceID='" & strDisk & "'")

  if (colOS.count = 0) then
    f_Error("Error! Can't get instance!")
  end if

  for Each objOS In colOS
    if objOS.DeviceID=strDisk then
      if (IsNumeric(objOS.FreeSpace) AND IsNumeric(objOS.Size)) then
        FreeDisk=CDBl(objOS.FreeSpace)
        TotalDisk=CDBl(objOS.Size)
      else
	f_Error("Error! Data for disk is invalid - maybe a CDROM or floppy?")
      end if
    end if  
  next
  if (IsNumeric(TotalDisk) AND TotalDisk > 0) then
     PercentDisk=(TotalDisk-FreeDisk)*100/TotalDisk
     if (PercentDisk >= 0.5) then
       GetPercentDiskEx=Int(FormatNumber(PercentDisk,0,0,0,0))
     else
       GetPercentDiskEx=0
     end if
     Total=TotalDisk
     Free=FreeDisk
  else
    f_Error("Error! No such disk!")
  end if
End Function


'**********************
'Error Function
'**********************
Function f_Error(message)
  if(message <> "") then
    Wscript.echo message
	Wscript.Quit(3)
  end if

  if (err.number<>0) then
  	if err.number=-2147217392 then
  		Wscript.echo "Error! | Error Number : -2147217392 | Description : Invalid Class"
		Wscript.Quit(3)
	else
    	Wscript.echo "Error! | Error Number : " & err.number & " | Description : " & err.description
		Wscript.Quit(3)
	end if
  end if
End Function


'**********************
'Help Function
'**********************
Function f_help()
  Dim str
  str="Check free disk space. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"check_disk -h hostname -d disk_drive -w warning_level -c critical_level [-user username -pass password]"
  str=str&vbCrlF
  str=str&"-h hostname                   Host name."&vbCrlF
  str=str&"-d disk_drive                   Disk drive."&vbCrlF
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level                Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: check_disk -h corpServer -d C: -w 30 -c 70 [-user corpServer\Administrator -pass password] "
  wscript.echo str
end function


'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim refix
Dim sComputer
Dim Total,Free,percent

'Cons for return val's
Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3

On Error Resume Next
for i=0 to WScript.Arguments.Count-1
  arg(i)=WScript.Arguments( i )
next

if (Err.Number<>0) Then
  f_Error("Could not read arguments.")
else 
  if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
    f_help()
  else 
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (Wscript.Arguments.Count=8) then
      sComputer = arg(1)
      Total=0
      Free=0
      percent=GetPercentDisk(sComputer,UCase(arg(3)),Total,Free)
      if (Err.Number<>0) Then
        f_Error("Could not get percent disk utilization.")
      else 
        if (percent<Int(arg(5))) then
          refix= "OK - "
          Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk_Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
          WScript.Quit(intOK)
        else 
          if (percent <Int(arg(7))) then
            refix= "Warning - "
            Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk_Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
            Wscript.Quit(intWarning)
          else 
            refix= "Critical - "
            Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk_Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
            Wscript.Quit(intCritical)
          end if
        end if
        
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10))="-PASS") And (Wscript.Arguments.Count=12) then
        sComputer = arg(1)
	Total=0
	Free=0
        percent=GetPercentDiskEx(sComputer,arg(9),arg(11),UCase(arg(3)),Total,Free)
        if (Err.Number<>0) Then
          f_Error("Could not get percent disk utilization.")
        else
          if (percent<Int(arg(5))) then
            refix= "OK - "
            Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk_Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
            WScript.Quit(intOK)
          else
            if (percent <Int(arg(7))) then
              refix= "Warning - "
              Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk_Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
              Wscript.Quit(intWarning)
            else
              refix= "Critical - "
              Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk_Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
              Wscript.Quit(intCritical)
            end if
          end if
        end if
      else
        f_Error("Incorrect number of arguments.")
      end if
    end if    
  end if
end if



