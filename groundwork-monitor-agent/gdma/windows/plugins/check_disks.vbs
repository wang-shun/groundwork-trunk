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

Function GetTotalDisk( strComputer,strDisk)

On Error Resume Next
    Dim objWMIService, colOS,objOS
    Set objWMIService = GetObject("winmgmts:\\" & strComputer)

    Set colOS = objWMIService.InstancesOf("Win32_LogicalDisk")

	 For Each objOS In colOS
		if objOS.DeviceID=strDisk then
		GetTotalDisk=objOS.Size
				Exit Function
		end if	

         Next

End Function


Function GetFreeDisk( strComputer,strDisk)

On Error Resume Next
    Dim objWMIService, colOS,objOS
    Set objWMIService = GetObject("winmgmts:\\" & strComputer)

    Set colOS = objWMIService.InstancesOf("Win32_LogicalDisk")

	 For Each objOS In colOS
		if objOS.DeviceID=strDisk then
		GetFreeDisk=objOS.FreeSpace
				Exit Function
		end if	

         Next

End Function
Function GetFreeDiskEx( strComputer,strUser,strPassword,strDisk)

On Error Resume Next
    Dim objWMIService, colOS,objOS

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objWMIService = objSWbemLocator.ConnectServer _
    (strComputer, "root\cimv2",strUser, strPassword)
    objWMIService.Security_.ImpersonationLevel = 3

    Set colOS = objWMIService.InstancesOf("Win32_LogicalDisk")

	 For Each objOS In colOS
		if objOS.DeviceID=strDisk then
		GetFreeDiskEx=objOS.FreeSpace
				Exit Function
		end if	

         Next

End Function
'**********************
'Extend Function
'**********************
Function GetTotalDiskEx( strComputer,strUser,strPassword,strDisk)

On Error Resume Next
    Dim objWMIService, colOS,objOS

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objWMIService = objSWbemLocator.ConnectServer _
    (strComputer, "root\cimv2",strUser, strPassword)
    objWMIService.Security_.ImpersonationLevel = 3

    Set colOS = objWMIService.InstancesOf("Win32_LogicalDisk")

	 For Each objOS In colOS
		if objOS.DeviceID=strDisk then
		GetTotalDiskEx=objOS.Size
				Exit Function
		end if	

         Next

End Function
'**********************
'Error Function
'**********************
Function f_Error()
  Wscript.echo "Error!"
  wscript.quit(3)
End Function

'**********************
'Help Function
'**********************
Function f_help()
Dim str
str="Check available and freespace disk.If your Local Machine have the same Administrator account and password as Remote Machine,you don't have to use two last parameters."&vbCrlF&vbCrlF
str=str&"cscript check_disk.vbs -h hostname -d disk_drive -w warning_level -c critical_level [-user username -pass password]"
str=str&vbCrlF
str=str&"-h hostname                   Host name."&vbCrlF
str=str&"-d disk_drive                   Disk drive."&vbCrlF
str=str&"-w warning_level            Warning threshold."&vbCrlF
str=str&"-c critical_level                Critical threshold."&vbCrlF
str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
str=str&vbCrlF
str=str&"Example: cscript check_disks.vbs -h Ser1 -d C: -w 30 -c 70 [-user SER1\Administrator -pass password] "&vbCrlF&vbCrlF
str=str&"Note: If you want to check all disk, use"&vbCrlF
str=str&"cscript check_disks.vbs -h hostname -w warning_level -c critical_level [-user username -pass password]"&vbCrlF
str=str&"(OR) cscript check_disks.vbs -h hostname -d * -w warning_level -c critical_level [-user username -pass password]"&vbCrlF
wscript.echo str

end function

'******************************
'** Exit Function
'******************************
Function f_exit(arg)

  Dim argw,argc
  Dim arguser,argp
  Dim i
  Dim refix,refix1
  Dim sComputer
  Dim Total,Free,percent
  Dim objWMIService, colOS,objOS

  Dim result

  On Error Resume Next

  result=0

  If (Err.Number<>0) Then
    f_Error()
  Else
    if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
      f_help()
    else
      if ((UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (arg(3)="*") AND (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (Wscript.Arguments.Count=8)) OR ((UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (Wscript.Arguments.Count=6)) then
        if (Wscript.Arguments.Count=8) then
          argw=arg(5)
          argc=arg(7)
        else
          argw=arg(3)
          argc=arg(5)
        end if
      
        SComputer=arg(1)    
        Set colDisks = GetObject("winmgmts:\\" & SComputer).ExecQuery("Select * from Win32_PerfFormattedData_PerfDisk_LogicalDisk")
        For Each objDisk in colDisks
          Percent=Int(100- objDisk.PercentFreeSpace)
          If (Err.Number=0) Then
            refix1=refix1 & "'" & objDisk.Name & " Disk Utilization'=" & Percent & "%" & ";" & Int(argw) & ";" & Int(argc) & ";" & ";   "
            if (percent<Int(argw)) then
              refix= refix & "OK - "
            else
              if (percent <Int(argc)) then
                if result=0 then
                  result=1
                end if
                refix= refix & "Warning - "                
              else
                result=2
                refix= refix & "Critical - "
              end if
            end if
            refix=refix & objDisk.Name &" Disk Utilization "& percent &"%  "
          end if
        Next
        If (Err.Number<>0) then
          f_error()
        else
          Wscript.Echo refix & "|   " & refix1
        end if
      else    
        if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (Wscript.Arguments.Count=8) then
          sComputer = arg(1)
          Total=GetTotalDisk(sComputer,UCase(arg(3)))
          Free=GetFreeDisk(sComputer,UCase(arg(3)))
          percent=Int(FormatNumber((Total-Free)*100/Total,0,0,0,0))
          If (Err.Number<>0) Then
            f_Error()
          Else
            if (percent<Int(arg(5))) then
              refix= "OK - "
            else
              if (percent <Int(arg(7))) then
                if result=0 then
                  result=1
                end if
                refix= "Warning - "
              else
                result=2
                refix= "Critical - "
              end if
            end if
            Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk Utilization'=" & percent & "%" & ";" & arg(5) & ";" & arg(7) & ";" & ";"
          end if
        else
  

          if ((UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (arg(3)="*") AND (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10))="-PASS") And (Wscript.Arguments.Count=12)) OR ((UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (UCase(arg(6))="-USER") And (UCase(arg(8))="-PASS") And (Wscript.Arguments.Count=10)) then
            if (Wscript.Arguments.Count=12) then
              argw=arg(5)
              argc=arg(7)
              arguser=arg(9)
              argp=arg(11)
            else
              argw=arg(3)
              argc=arg(5)
              arguser=arg(7)
              argp=arg(9)
            end if

            SComputer= arg(1)
            Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
	   		Set objWMIService = objSWbemLocator.ConnectServer (SComputer, "root\cimv2",arguser, argp)
    	    objWMIService.Security_.ImpersonationLevel = 3

            Set colDisks = objWMIService.ExecQuery("Select * from Win32_PerfFormattedData_PerfDisk_LogicalDisk")

            For Each objDisk in colDisks
              Percent=Int(100- objDisk.PercentFreeSpace)
              If (Err.Number=0) Then
              	refix1=refix1 & "'" & objDisk.Name & " Disk Utilization'=" & Percent & "%" & ";" & Int(argw) & ";" & Int(argc) & ";" & ";   "
                if (percent<Int(argw)) then
                  refix= refix & "OK - "
                else
                  if (percent <Int(argc)) then
                    if result=0 then
                      result=1
                    end if
                    refix= refix & "Warning - "
                  else
                    result=2
                    refix= refix & "Critical - "
                  end if
                end if
                refix=refix & objDisk.Name &" Disk Utilization "& percent &"%   "
              end if
            Next
            If (Err.Number<>0) then
              f_error()
            else
              Wscript.Echo refix & "|   " & refix1
            end if
          else    
            If (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10))="-PASS") And (Wscript.Arguments.Count=12) then
              sComputer = arg(1)
              Total=GetTotalDiskEx(sComputer,arg(9),arg(11),UCase(arg(3)))
              Free=GetFreeDiskEx(sComputer,arg(9),arg(11),UCase(arg(3)))
              percent=Int(FormatNumber((Total-Free)*100/Total,0,0,0,0))              
              If (Err.Number<>0) Then
                f_Error()
              Else
                if (percent<Int(arg(5))) then
                  refix= "OK - "
                else
                  if (percent <Int(arg(7))) then
                    if result=0 then
                      result=1
                    end if
                    refix= "Warning - "
                  else
                    result=2
                    refix= "Critical - "
                  end if
                end if
                Wscript.Echo refix & arg(3) &" Disk Utilization "& percent &"%" & " | 'Disk Utilization'=" & percent & "%" & ";" & Int(arg(5)) & ";" & Int(arg(7)) & ";" & ";"
              end if
            else
              f_Error()
            end if
          end if
        end if
      end if    
    end if
  end if
  f_exit=result
end function
'******************************
'** Main program
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