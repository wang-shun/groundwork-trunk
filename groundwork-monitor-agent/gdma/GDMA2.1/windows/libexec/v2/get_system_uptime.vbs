' Copyright 2009 GroundWork Open Source Inc.
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
'
'
' Original Author:  Dr. Dave Blunt at GroundWork Open Source Inc.

'*************************************************************************************************
'                                        Public Variable
'*************************************************************************************************
Const intOK       = 0
Const intWarning  = 1
Const intCritical = 2
Const intError    = 3

Dim argcountcommand
Dim arg(20)
Dim strComputer
Dim strWarning
Dim strCritical
Dim strUser
Dim strPass
Dim strNameSpace
Dim strDescription
Dim strCommandName
Dim strVersion
Dim strResultTemp
Dim strResult
Dim strUpTime

Dim ArgCount
Dim strArgMain(10)
Dim strArgShortDes(10)
Dim strArgDetailDes(10)
Dim strArgExample(10)

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
	
		if (err.number <>0 ) then
			if err.number = -2147023174 then
				Wscript.echo "Critical - Timeout connecting to WMI on this host! Error Number: " & err.number & " Description: " & err.Description
				WScript.Quit(intCritical)
			else
				if err.number = -2147024891 then
					Wscript.echo "Authentication failure to remote host! Error Number: " & err.number & " Description: " & err.description
				else
					if err.number = 462 then
						Wscript.echo "Critical - Timeout connecting to WMI on this host! Error Number: " & err.number & " Description: " & err.Description
						WScript.Quit(intCritical)
					else 
						if err.number=-2147217392 then
							Wscript.echo "Error! Error Number: -2147217392 Description: Invalid Class"
						else	 
							Wscript.echo "Error! Error Number: " & err.number & " Description: " & err.description 
						end if	
					end if
				end if
			end if
			Wscript.Quit(intError)
		end if
				
	End Function
'-------------------------------------------------------------------------------------------------
'Function Name:     f_Help.
'Descripton:        Display help of command include : Description, Arguments, Examples
'Input:				No.
'Output:			No.
'-------------------------------------------------------------------------------------------------
	Function f_Help()
	
		Dim strHelp
		
		
		Dim i
		Dim strtemp1
		Dim strtemp2
		Dim strtemp3
		
		strHelp=""
		
		strtemp1=""
		strtemp2=""
		strtemp3=""

		for i=1 to ArgCount
			strtemp1=strtemp1 & " " & strArgMain(i) & " " & strArgShortDes(i)
			strtemp2=strtemp2 & strArgMain(i) & " " & strArgShortDes(i) & "	" & strArgDetailDes(i) & "." & vbCrlF
			strtemp3=strtemp3 & " " & strArgMain(i) & " " & strArgExample(i)
		next
		

  		strHelp=strCommandName&" version "&strVersion&vbCrlF&vbCrlF
		strHelp=strHelp&strDescription&"  If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two (or three) last parameters."&vbCrlF&vbCrlF
  		strHelp=strHelp & "cscript " & strCommandName & strtemp1 & " [-user username -pass password -domain domain]"
  		strHelp=strHelp & vbCrlF
  		strHelp=strHelp & strtemp2  		
    	        strHelp=strHelp & "-user username		Account Administrator on Remote Machine." & vbCrlF
  		strHelp=strHelp & "-pass password		Password Account Administrator on Remote Machine." & vbCrlF
  		strHelp=strHelp & "-domain domain		Domain Name on Remote Machine." & vbCrlF
  		strHelp=strHelp & vbCrlF  		
  		strHelp=strHelp & "Example: cscript " & strCommandName & strtemp3 & " [-user Ser1\Administrator -pass password -domain workgroup]." & vbCrlF
  		strHelp=strHelp & vbCrlF
  		Wscript.echo strHelp
		
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
'Function Name:     f_TestLocalCommand.
'Descripton:        Test structure of command run at local host.
'Input:				No.
'Output:			Yes.
'-------------------------------------------------------------------------------------------------
	Function f_TestLocalCommand()

		On Error Resume Next
		
		Dim i,j
		Dim temp
		Dim count
		Dim check(10)
		
		count=0
		
		for j=1 to ArgCount
			check(j)=0
		next

		if (argcountcommand<>ArgCount*2) then
			f_TestLocalCommand=0
		else
			for i=0 to argcountcommand-1
				if (i mod 2=0) then
					temp=UCase(arg(i))
					for j=1 to ArgCount
						if (temp=UCase(strArgMain)) and (check(j)=0) then
							check(j)=1
							count=count+1
							j=ArgCount
						end if
					next
				end if
			next
			if count=ArgCount then
				f_TestLocalCommand=1
			else
				f_TestLocalCommand=0
			end if
		end if

	End Function
'-------------------------------------------------------------------------------------------------
'Function Name:     f_TestRemoteCommand.
'Descripton:        Test structure of command run at remote host.
'Input:				No.
'Output:			Yes.
'-------------------------------------------------------------------------------------------------
	Function f_TestRemoteCommand()

		On Error Resume Next
		
		Dim i,j
		Dim temp
		Dim count
		Dim check(10)
		Dim extra(5)
		
		count=0
		
		for j=1 to ArgCount
			check(j)=0
		next
		
		for j=1 to 3
			extra(j)=0
		next


		if (argcountcommand=(ArgCount+2)*2) or (argcountcommand=(ArgCount+3)*2) then
			for i=0 to argcountcommand-1
				if (i mod 2=0) then
					temp=UCase(arg(i))
					if (temp="-USER" and extra(1)=0) then
						extra(1)=1
						count=count+1
					else
						if (temp="-PASS" and extra(2)=0) then
							extra(2)=1
							count=count+1
						else
							if (temp="-DOMAIN" and extra(3)=0) then
								extra(3)=1
								count=count+1
							else					
								for j=1 to ArgCount
									if (temp=UCase(strArgMain)) and (check(j)=0) then
										check(j)=1
										count=count+1
										j=ArgCount
									end if
								next
							end if
						end if
					end if
				end if
			next
			if (count*2=argcountcommand) then
				f_TestRemoteCommand=1
			else
				f_TestRemoteCommand=0
			end if
		
		else
		  	f_TestremoteCommand=0
		end if

	End Function
	
'-------------------------------------------------------------------------------------------------
'Function Name:     f_LocalInfo.
'Descripton:        Get infomation from Local Host.
'Input:		    None.
'Output:	    None.
'-------------------------------------------------------------------------------------------------
	Function f_LocalInfo()
		
		On Error Resume Next
		
		Dim objWMIService, colWMI,objWMI
		strResultTemp     = ""
		strLastBootUpTime = ""
		strLocalDateTime  = ""
		strOSName         = ""
		strManufacturer   = ""
		strModel          = ""
		strName           = ""
		strHostDomain	  = ""
		strProcessors     = ""

		Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\" & strNameSpace)
		f_Error()

		Set colWMI = objWMIService.ExecQuery("Select LastBootUpTime,LocalDateTime,Caption,CSDVersion from Win32_OperatingSystem")
		for Each objWMI In colWMI
			strLastBootUpTime = objWMI.LastBootUpTime
			strLocalDateTime  = objWMI.LocalDateTime
			strOSName         = objWMI.Caption & " " & objWMI.CSDVersion
		next

		Set colWMI = objWMIService.ExecQuery("Select Manufacturer,Model,Name,Domain,NumberOfProcessors from Win32_ComputerSystem")
		for Each objWMI In colWMI
			strManufacturer   = objWMI.Manufacturer
			strModel          = objWMI.Model
			strName           = objWMI.Name
			strHostDomain     = objWMI.Domain
			strProcessors     = objWMI.NumberOfProcessors
		next

		strDateTemp  = Left(strLastBootUpTime, 8)
		strYearTemp  = Left(strDateTemp, 4)
		strMonthTemp = Mid(strDateTemp,5,2)
		strDayTemp   = Right(strDateTemp, 2)
		strHourTemp  = Mid(strLastBootUpTime,9,2)
		strMinTemp   = Mid(strLastBootUpTime,11,2)
		strSecTemp   = Mid(strLastBootUpTime,13,2)

		strLastBootUpTime = strYearTemp & "-" & strMonthTemp & "-" & strDayTemp & " " & strHourTemp & ":" & strMinTemp & ":" & strSecTemp
		strBootTimeTemp = DateDiff("s", "12/31/1969 00:00:00", DateSerial(strYearTemp, strMonthTemp, strDayTemp) + TimeSerial(strHourTemp, strMinTemp, strSecTemp))

		strDateTemp  = Left(strLocalDateTime, 8)
		strYearTemp  = Left(strDateTemp, 4)
		strMonthTemp = Mid(strDateTemp,5,2)
		strDayTemp   = Right(strDateTemp, 2)
		strHourTemp  = Mid(strLocalDateTime,9,2)
		strMinTemp   = Mid(strLocalDateTime,11,2)
		strSecTemp   = Mid(strLocalDateTime,13,2)

		strLocalDateTime = strYearTemp & "-" & strMonthTemp & "-" & strDayTemp & " " & strHourTemp & ":" & strMinTemp & ":" & strSecTemp
		strLocalDateTimeTemp = DateDiff("s", "12/31/1969 00:00:00", DateSerial(strYearTemp, strMonthTemp, strDayTemp) + TimeSerial(strHourTemp, strMinTemp, strSecTemp))

		strUpTime = strLocalDateTimeTemp - strBootTimeTemp

		strResultTemp = "Uptime (s) = " & strUpTime & ".  Host name = " & strName & ", domain = " & strHostDomain & ",  OS Name = " & strOSName
		strResultTemp = strResultTemp & ", manufacturer = " & strManufacturer & ", model = " & strModel
		strResultTemp = strResultTemp & ", processors = " & strProcessors & ",  Last boot time (local clock) = " & strLastBootUpTime

	End Function

'-------------------------------------------------------------------------------------------------
'Function Name:     f_RemoteInfo.
'Descripton:        Get infomation from Remote Host.
'Input:		    None.
'Output:	    None.
'-------------------------------------------------------------------------------------------------
	Function f_RemoteInfo(info)
		
		On Error Resume Next
		
		Dim objWMIService, colWMI,objWMI,objSWbemLocator
		Dim strDomain
		strResultTemp     = ""
		strLastBootUpTime = ""
		strLocalDateTime  = ""
		strOSName         = ""
		strManufacturer   = ""
		strModel          = ""
		strName           = ""
		strHostDomain	  = ""
		strProcessors     = ""

		
		Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")

		if ((ArgCount+2)*2=ArgCountCommand) then
			Set objWMIService = objSWbemLocator.ConnectServer _
				(strComputer, strNameSpace , strUser, strPass )	
			f_Error()
		else
			strDomain=f_GetOneArg("-domain")
			Set objWMIService = objSWbemLocator.ConnectServer _
				(strComputer, strNameSpace , strUser, strPass,"MS_409","ntlmdomain:" + strDomain )
			f_Error()
		end if
		
		objWMIService.Security_.ImpersonationLevel = 3
		f_Error()

		Set colWMI = objWMIService.ExecQuery("Select LastBootUpTime,LocalDateTime,Caption,CSDVersion from Win32_OperatingSystem")
		for Each objWMI In colWMI
			strLastBootUpTime = objWMI.LastBootUpTime
			strLocalDateTime  = objWMI.LocalDateTime
			strOSName         = objWMI.Caption & " " & objWMI.CSDVersion
		next

		Set colWMI = objWMIService.ExecQuery("Select Manufacturer,Model,Name,Domain,NumberOfProcessors from Win32_ComputerSystem")
		for Each objWMI In colWMI
			strManufacturer   = objWMI.Manufacturer
			strModel          = objWMI.Model
			strName           = objWMI.Name
			strHostDomain     = objWMI.Domain
			strProcessors     = objWMI.NumberOfProcessors
		next

		strDateTemp  = Left(strLastBootUpTime, 8)
		strYearTemp  = Left(strDateTemp, 4)
		strMonthTemp = Mid(strDateTemp,5,2)
		strDayTemp   = Right(strDateTemp, 2)
		strHourTemp  = Mid(strLastBootUpTime,9,2)
		strMinTemp   = Mid(strLastBootUpTime,11,2)
		strSecTemp   = Mid(strLastBootUpTime,13,2)

		strLastBootUpTime = strYearTemp & "-" & strMonthTemp & "-" & strDayTemp & " " & strHourTemp & ":" & strMinTemp & ":" & strSecTemp
		strBootTimeTemp = DateDiff("s", "12/31/1969 00:00:00", DateSerial(strYearTemp, strMonthTemp, strDayTemp) + TimeSerial(strHourTemp, strMinTemp, strSecTemp))

		strDateTemp  = Left(strLocalDateTime, 8)
		strYearTemp  = Left(strDateTemp, 4)
		strMonthTemp = Mid(strDateTemp,5,2)
		strDayTemp   = Right(strDateTemp, 2)
		strHourTemp  = Mid(strLocalDateTime,9,2)
		strMinTemp   = Mid(strLocalDateTime,11,2)
		strSecTemp   = Mid(strLocalDateTime,13,2)

		strLocalDateTime = strYearTemp & "-" & strMonthTemp & "-" & strDayTemp & " " & strHourTemp & ":" & strMinTemp & ":" & strSecTemp
		strLocalDateTimeTemp = DateDiff("s", "12/31/1969 00:00:00", DateSerial(strYearTemp, strMonthTemp, strDayTemp) + TimeSerial(strHourTemp, strMinTemp, strSecTemp))

		strUpTime = strLocalDateTimeTemp - strBootTimeTemp

		strResultTemp = "Uptime (s) = " & strUpTime & ".  Host name = " & strName & ", domain = " & strHostDomain & ",  OS Name = " & strOSName
		strResultTemp = strResultTemp & ", manufacturer = " & strManufacturer & ", model = " & strModel
		strResultTemp = strResultTemp & ", processors = " & strProcessors & ",  Last boot time (local clock) = " & strLastBootUpTime

	End Function
			
'-------------------------------------------------------------------------------------------------
'Function Name:     f_LocalPerfValue.
'Descripton:        Get perform value at Local Host.
'Input:				No.
'Output:			No.
'-------------------------------------------------------------------------------------------------
	Function f_LocalPerfValue()
		
		On Error Resume Next
		strResult = ""
		strExit   = "UNKNOWN"
		intExit   = 3

		f_LocalInfo()
		strResult = strResult & strResultTemp

		if (Int(strUpTime) <= Int(strCritical)) Then
			strExit = "CRITICAL"
			intExit = intCritical
		Else
			if (Int(strUpTime) <= Int(strWarning)) Then
				strExit = "WARNING"
				intExit = intWarning
			Else
				strExit = "OK"
				intExit = intOK
			End if
		End if

		Wscript.Echo strExit & ": " & strResult
		Wscript.Quit(intExit)
	End Function

'-------------------------------------------------------------------------------------------------
'Function Name:     f_RemotePerfValue.
'Descripton:        Get perform values at Remote Host.
'Input:				No.
'Output:			Values.
'-------------------------------------------------------------------------------------------------
	Function f_RemotePerfValue()
		
		On Error Resume Next
		strResult = ""
		strExit   = "UNKNOWN"
		intExit   = 3

		f_RemoteInfo()
		strResult = strResult & strResultTemp

		if (Int(strUpTime) <= Int(strCritical)) Then
			strExit = "CRITICAL"
			intExit = intCritical
		Else
			if (Int(strUpTime) <= Int(strWarning)) Then
				strExit = "WARNING"
				intExit = intWarning
			Else
				strExit = "OK"
				intExit = intOK
			End if
		End if 

		Wscript.Echo strExit & ": " & strResult
		Wscript.Quit(intExit)
	End Function

'*************************************************************************************************
'                                        Main Function
'*************************************************************************************************

					'/////////////////////

		strCommandName="get_system_uptime.vbs"
		strDescription="Return system uptime and other OS and hardware metrics using WMI."
		strVersion="1.4"

		                        '/////////////////////
		
		ArgCount=3
		
		strArgMain(1)=			"-h"
		strArgShortDes(1)=		"hostname"
		strArgDetailDes(1)=		"Host name"
		strArgExample(1)=		"Ser1"
				
		strArgMain(2)=			"-w"
		strArgShortDes(2)=		"warning"
		strArgDetailDes(2)=		"Warning"
		strArgExample(2)=		"1800"

		strArgMain(3)=			"-c"
		strArgShortDes(3)=		"critical"
		strArgDetailDes(3)=		"Critical"
		strArgExample(3)=		"900"



							
		strNameSpace = 	"root\cimv2"
		
	f_GetAllArg()
	f_Error()

  	if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (argcountcommand=1) then
		f_help()
  	else
  		if (f_TestLocalCommand()) then
  			strComputer=f_GetOneArg("-h")
  			if(strComputer = "localhost") then
  				strComputer = "."
  				Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
				Set colItems = objWMIService.ExecQuery("Select DNSHostName from Win32_ComputerSystem",,48)
				For Each objItem in colItems
					strComputer = objItem.DNSHostName
				Next
			end if
			strWarning   = f_GetOneArg("-w")
			strCritical  = f_GetOneArg("-c")
			f_LocalPerfValue()
  			f_Error()
  		else
  			if (f_TestRemoteCommand()) then
  				strComputer  = f_GetOneArg("-h")
				strWarning   = f_GetOneArg("-w")
				strCritical  = f_GetOneArg("-c")
				strUser      = f_GetOneArg("-user")
				strPass      = f_GetOneArg("-pass")
				f_RemotePerfValue()
  				f_Error()
  			else
  				f_Error()
  				Wscript.echo "Error! Arguments are wrong.  Try -h or --help."
  				Wscript.Quit(intError)
  			end if
  		end if
  	end if
