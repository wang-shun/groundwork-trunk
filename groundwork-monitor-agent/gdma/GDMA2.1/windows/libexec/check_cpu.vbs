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
' Author Pham Phu Du Hao at ITSP Company (info@itspco.com) 

dim arrayname(20)
dim arrayvalue(20)
dim longarray
longarray=0
Function f_excute(colOS)
	dim i,tam
	For Each objOS In colOS
		longarray=longarray+1
		arrayname(longarray)=objOS.DeviceID
		arrayvalue(longarray)=objOS.LoadPercentage
	next
	tam=0
	if (longarray>0) then
		for i=1 to longarray
			tam=tam+arrayvalue(i)
		next
		longarray=longarray+1
		arrayname(longarray)="_Total"
		arrayvalue(longarray)=tam/(longarray-1)
	end if
end function

Function f_getreturn()

	Dim i
	refix=""
    return=0
    dem=0
    total=0
    percent=0
	if (cpuID="*") then
		For i=1 to longarray
			percent= Int(arrayvalue(i))
			dem=dem+1
			total=total+arrayvalue(i)
       		if (percent<Int(warning)) then
       			refix= refix &  "OK - "
        	else
          	if (percent <Int(critical)) then
           		refix= refix & "Warning - "
           		if (return<1) then
           			return=1
           		end if
          	else
            	refix= refix & "Critical - "
            	if (return<2) then
            		return=2
            	end if
          	end if
        	end if
                if (not IsNumeric(percent)) then
	          refix= "UNKNOWN - "
	          Wscript.Echo refix & "CPU Utilization ("&percent&") could not be determined."
	          Wscript.Quit(intError)
                end if
        	refix= refix & arrayname(i) & " Utilization "&percent&"%" & " | 'CPU_Utilization'=" & percent & "%" & ";" & warning & "%;" & critical & "%;" & ";    "
    	Next
    	exit function
    else
    if (Ucase(cpuID)="_TOTAL") then
		percent=percent+Int(arrayvalue(longarray))
	   	if (percent<Int(warning)) then
       			refix= refix &  "OK - "
        	else
          	if (percent <Int(critical)) then
           		refix= refix & "Warning - "
           		if (return<1) then
           			return=1
           		end if
          	else
            	refix= refix & "Critical - "
            	if (return<2) then
            		return=2
            	end if
          	end if
        	end if
                if (not IsNumeric(percent)) then
	          refix= "UNKNOWN - "
	          Wscript.Echo refix & "CPU Utilization could not be determined."
	          Wscript.Quit(intError)
                end if
        refix= refix & "Total CPU Utilization "&percent&"%" & " | 'CPU_Utilization'=" & percent & "%" & ";" & warning & "%;" & critical & "%;" & ";    "
        exit function
    else
    	For i=1 to longarray
    		if (Ucase(cpuID)=Ucase(arrayname(i))) then
	   			percent= Int(arrayvalue(i))
       			if (percent<Int(warning)) then
       				refix= refix &  "OK - "
        		else
          			if (percent <Int(critical)) then
           			refix= refix & "Warning - "
           			if (return<1) then
           				return=1
           			end if
          		else
            		refix= refix & "Critical - "
            		if (return<2) then
	            		return=2
    	        	end if
        	  	end if
        		end if
                        if (not IsNumeric(percent)) then
	                  refix= "UNKNOWN - "
	                  Wscript.Echo refix & "CPU Utilization ("&percent&") could not be determined."
	                  Wscript.Quit(intError)
                        end if
        		refix= refix & arrayname(i) & " Utilization "&percent&"%" & " | 'CPU_Utilization'=" & percent & "%" & ";" & warning & "%;" & critical & "%;" & ";    "
        		exit function
        	end if
    	Next
    end if
    end if
    return=3
end function
Function GetCPU(strComputer)

    Dim objWMIService, colOS,objOS
    Set objWMIService = GetObject("winmgmts:\\" & strComputer)
    Set colOS = objWMIService.InstancesOf("Win32_Processor")
    
    if (colOS.count = 0) then
	    f_Error("Error! Can't get instance!")
    end if
    f_excute(colOS)
End Function

'**********************
'Extend Function
'**********************
Function GetCPUEx( strComputer,strUser,strPassword,strDomain)
    Dim objWMIService, colOS,objOS

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    
    if (strDomain="") then    	
    	Set objWMIService = objSWbemLocator.ConnectServer _
    	(strComputer, "\root\cimv2",strUser, strPassword)
    else
    	Set objWMIService = objSWbemLocator.ConnectServer _
			(strComputer, "\root\cimv2" , strUser, strPassword,"MS_409","ntlmdomain:" & strDomain )	
    end if
    objWMIService.Security_.ImpersonationLevel = 3
    Set colOS = objWMIService.InstancesOf("Win32_Processor")
    
    if (colOS.count = 0) then
	    f_Error("Error! Can't get instance!")
    end if
    f_excute(colOS)
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
  		Wscript.echo "Error! " & Err.number & " | " & Err.description
  		Wscript.Quit(intError)
  	end if

End Function

'**********************
'Help Function
'**********************
Function f_help()
Dim str
str="Check CPU Utilization.If your Local Machine have the same Administrator account and password as Remote Machine,you don't have to use two last parameters."&vbCrlF&vbCrlF
str=str&"cscript check_cpu.vbs -h hostname -id deviceID -w warning_level -c critical_level [-user username -pass password -domain domain]"
str=str&vbCrlF
str=str&"-h hostname                   Host name."&vbCrlF
str=str&"-id deviceID                   CpuID."&vbCrlF
str=str&"-w warning_level            Warning threshold."&vbCrlF
str=str&"-c critical_level                Critical threshold."&vbCrlF
str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
str=str&"-domain domain              Net Bios Domain Name of Remote Machine."&vbCrlF
str=str&vbCrlF
str=str&"Example: cscript check_cpu.vbs -h Ser1 -id CPU0 -w 30 -c 70 [-user SER1\Administrator -pass password -domain ITSP] "
wscript.echo str
end function

Function f_getid()
	Dim first,tam1,tam2
	first=1
	lengthid=0
	tam1=10
	do while (tam1>0)
		tam1=InStr(first,arg(3),",")
		if (tam1>0) then
			tam2=Mid(cpuID,first,tam1-first)
			lengthid=lengthid+1
			arrayid(lengthid)=tam2
			first=tam1+1
		end if
	loop
	if (first<=len(cpuID)) then
		tam2=Mid(cpuID,first,len(cpuID))
		lengthid=lengthid+1
		arrayid(lengthid)=tam2
	end if
end function
'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim return,refix,percent,dem,total
Dim sComputer
Dim warning
Dim critical
Dim cpuID
Dim arrayid(20)
Dim lengthid
Dim mainRefix
Dim mainReturn

'Cons for return val's
Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3
mainReturn=0
mainRefix=""

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
    If ((UCase(arg(0))="-H")  And (UCase(arg(2)) = "-W") And (UCase(arg(4)) = "-C") And (Wscript.Arguments.Count=6))_
    	or ((UCase(arg(0))="-H") And (UCase(arg(2)) = "-ID") And (UCase(arg(4)) = "-W") And (UCase(arg(6)) = "-C") And (Wscript.Arguments.Count=8)) then
      sComputer = arg(1)
      if (Wscript.Arguments.Count=6) then
      	cpuID="_total"
      	warning=arg(3)
      	critical=arg(5)
      	call(GetCPU(sComputer))

		if (longarray=0) then
			f_Error("Error!  Couldn't access WMI!")
		end if

      	f_getreturn()
      	f_Error()
      	mainRefix=mainRefix & refix
      	if return>mainReturn then
      		mainReturn=return
      	end if
      else
      	cpuID=arg(3)
      	warning=arg(5)
      	critical=arg(7)
      	f_getid()
      	call(GetCPU(sComputer))

		if (longarray=0) then
			f_Error("Error!  Couldn't access WMI!")
		end if

      	for i=1 to lengthid
      		cpuID=arrayid(i)
      		f_getreturn()
      		f_Error()
      		mainRefix=mainRefix & refix
      		if return>mainReturn then
      			mainReturn=return
      		end if
      	next
      end if
      wscript.echo mainRefix
      wscript.quit mainReturn
    Else
      If ((UCase(arg(0))="-H") And (UCase(arg(2)) = "-W") And (UCase(arg(4)) = "-C") And (UCase(arg(6)) = "-USER") And (UCase(arg(8)) = "-PASS") And ((Wscript.Arguments.Count=10) Or ((UCase(arg(10)) = "-DOMAIN") And (Wscript.Arguments.Count=12))))_
      	or ((UCase(arg(0))="-H") And (UCase(arg(2)) = "-ID") And (UCase(arg(4)) = "-W") And (UCase(arg(6)) = "-C") And (UCase(arg(8)) = "-USER") And (UCase(arg(10)) = "-PASS") And ((Wscript.Arguments.Count=12) Or ((UCase(arg(12)) = "-DOMAIN") And (Wscript.Arguments.Count=14)))) then
      	
      	sComputer = arg(1)
      	if ((UCase(arg(2)) = "-ID")) then
	        cpuID=arg(3)
    	  	warning=arg(5)
      		critical=arg(7)
      		
      		f_getid()
      		if (Wscript.Arguments.Count=12) then
        		call((GetCPUEx(sComputer,arg(9),arg(11),"")))
        	else
        		call((GetCPUEx(sComputer,arg(9),arg(11),arg(13))))
        	end if
		if (longarray=0) then
			f_Error("Error!  Couldn't access WMI!")
		end if

      		for i=1 to lengthid
      			cpuID=arrayid(i)
      			f_getreturn()      			
      			f_Error()
      			mainRefix=mainRefix & refix
      			if return>mainReturn then
      				mainReturn=return
      			end if
      		next
        else
        	cpuID="_total"
    	  	warning=arg(3)
      		critical=arg(5)
        	if (Wscript.Arguments.Count=10) then
        		call((GetCPUEx(sComputer,arg(7),arg(9),"")))
        	else
        		call((GetCPUEx(sComputer,arg(7),arg(9),arg(11))))
        	end if
		if (longarray=0) then
			f_Error("Error!  Couldn't access WMI!")
		end if

        	f_getreturn()
        	mainRefix=mainRefix & refix
      		if return>mainReturn then
      			mainReturn=return
      		end if
        end if
        	f_Error()
        	wscript.echo mainRefix
      		wscript.quit mainReturn
      else 
	  	wscript.echo ("Error ! Wrong Arguments!")
	    wscript.quit(3)	  
      end if
    end if
  End If
End If