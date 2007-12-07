<!-- ======================================================
     This definition file defines the OBD-II byte codes that will be used to communicate with an ELM module
     such as the ELM323 or ELM327.  The order of the 3 bytes is important.
     byte-1 = PID
     
     For more info see
     http://en.wikipedia.org/wiki/OBD-II_PIDs
     ====================================================== -->
<parameters name="ELM Module (323/327)" monitor-class="net.sourceforge.JDash.ecu.comm.ELMScanMonitor">

    <description>Standard ELMScan module. Tested with 323, but should work with any ELM module such as the new ELM327.</description>

	<include file="dtc.iml"/>

	<parameter name="E_MIL_STATUS" rate="60000">
		<description>
The 4 byte CEL count and status.  The return is 4 bytes of data.
Bit A7 = MIL light status
Bit A0-A6 = the count of DTCs
Bytes 2,3 and 4 represent info about on-board tests
		</description>
		<address>
			<byte>0x01</byte>
		</address>
	</parameter>

	<parameter name="E_LOAD">
		<description>Calculated engine load - {n * 100 / 255}</description>
		<address>
			<byte>0x04</byte>
		</address>
	</parameter>

	<parameter name="E_COOLANT_TEMP">
		<description>Coolan Temp in C - {n - 40}</description>
		<address>
			<byte>0x05</byte>
		</address>
	</parameter>

	<parameter name="E_STFT_1">
		<description>Short Term Fuel Trim #1 - {0.7812 * (n - 128)}</description>
		<address>
			<byte>0x06</byte>
		</address>
	</parameter>

	<parameter name="E_LTFT_1">
		<description>Long Term Fuel Trim #1 - {0.7812 * (n - 128)}</description>
		<address>
			<byte>0x07</byte>
		</address>
	</parameter>
	
	<parameter name="E_STFT_2">
		<description>Short Term Fuel Trim #2 - {0.7812 * (n - 128)}</description>
		<address>
			<byte>0x08</byte>
		</address>
	</parameter>

	<parameter name="E_LTFT_2">
		<description>Long Term Fuel Trim #2 - {0.7812 * (n - 128)}</description>
		<address>
			<byte>0x09</byte>
		</address>
	</parameter>
	
	<parameter name="E_FUEL_PRESSURE">
		<description>Fuel Pressure in kPa - {n * 3}</description>
		<address>
			<byte>0x0A</byte>
		</address>
	</parameter>

	<parameter name="E_MAP">
		<description>Intake Manifold Absolute Pressure in kPa - {n}</description>
		<address>
			<byte>0x0B</byte>
		</address>
	</parameter>

	<parameter name="E_RPM">
		<description>Engine RPM - {n / 4}</description>
		<address>
			<byte>0x0C</byte>
		</address>
	</parameter>
	
	<parameter name="KPH">
		<description>Vehicle speed in KPH - {n}</description>
		<address>
			<byte>0x0D</byte>
		</address>
	</parameter>
	
	<parameter name="E_TIMING">
		<description>Timing Advance - {n / 2 - 64}</description>
		<address>
			<byte>0x0E</byte>
		</address>
	</parameter>
	
	<parameter name="E_INTAKE_AIR_TEMP">
		<description>Intake Air Temp in C - {n - 40}</description>
		<address>
			<byte>0x0F</byte>
		</address>
	</parameter>
	
	<parameter name="E_MAF">
		<description>MAF air flow rate in g/s - {n / 100}</description>
		<address>
			<byte>0x10</byte>
		</address>
	</parameter>
	
	<parameter name="E_TPS">
		<description>Throttle Positon Sensor % - {n * 100 / 255}</description>
		<address>e
			<byte>0x11</byte>
		</address>
	</parameter>

	<parameter name="E_O2_SENSOR_PRESENT">
		<description>O2 sensor present - {bits 0-3 = bank 1 sensors 1-4;  bits 4-7 = bank 2 sensors 1-4}</description>
		<address>
			<byte>0x13</byte>
		</address>
	</parameter>
	
	<parameter name="E_LAMBDA_1">
		<description>O2 Lambda #1 - Ratio={(n right-bitshift 16) * 0.0000305} Voltage={(0x00ff AND n)*0.000122}</description>
		<address>
			<byte>0x24</byte>
		</address>
	</parameter>
	
	<parameter name="E_ATMO">
		<description>Atmospheric Pressure - {in kPa absolute}</description>
		<address>
			<byte>0x24</byte>
		</address>
	</parameter>

	<!-- The ELM monitor REQUIRES this meta parameter for CEL processing -->
	<meta-parameter name="MIL_STATUS">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	return meta.bitcheck(meta.getParamValue("E_MIL_STATUS"), 32)
            </arg>
            <arg name="dependant" value="E_MIL_STATUS"/>
        </args>
    </meta-parameter>	


	<meta-parameter name="LOAD">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_LOAD") * 100.0 / 255.0 
			</arg>
			<arg name="dependant" value="E_LOAD"/>
        </args>
    </meta-parameter>	
		
	<meta-parameter name="COOLANT_TEMP_C">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<description>Coolan Temp in C - {n - 40}</description>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_COOLANT_TEMP") - 4.0 
			</arg>
			<arg name="dependant" value="E_COOLANT_TEMP"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="COOLANT_TEMP_F">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return (meta.getParamValue("COOLANT_TEMP_C") * (9.0/5.0)) + 32.0 
			</arg>
			<arg name="dependant" value="COOLANT_TEMP_C"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="STFT_1">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	 return 0.7812 * (meta.getParamValue("E_STFT_1") - 128.0) 
			</arg>
			<arg name="dependant" value="E_STFT_1"/>
        </args>
    </meta-parameter>	
	
	<meta-parameter name="LTFT_1">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	 return 0.7812 * (meta.getParamValue("E_LTFT_1") - 128.0) 
			</arg>
			<arg name="dependant" value="E_LTFT_1"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="STFT_2">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return 0.7812 * (meta.getParamValue("E_STFT_2") - 128.0) 
			</arg>
			<arg name="dependant" value="E_STFT_2"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="LTFT_2">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return 0.7812 * (meta.getParamValue("E_LTFT_2") - 128.0) 
			</arg>
			<arg name="dependant" value="E_LTFT_2"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="FUEL_PRESSURE_PSI">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return (meta.getParamValue("E_FUEL_PRESSURE") * 3) / 0.14504 
			</arg>
			<arg name="dependant" value="E_FUEL_PRESSURE"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="MAP_PSI">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_MAP") / 0.14504 
			</arg>
			<arg name="dependant" value="E_MAP"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="ATMO_PSI">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_ATMO") / 0.14504 
			</arg>
			<arg name="dependant" value="E_ATMO"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="VAC_BOOST_PSI">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	 return meta.getParamValue("MAP_PSI") - "ATMO_PSI"
			</arg>
			<arg name="dependant" value="MAP_PSI"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="RPM">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	 return meta.getParamValue("E_RPM") / 4.0 
			</arg>
			<arg name="dependant" value="E_RPM"/>
		</args>
    </meta-parameter>	

    <meta-parameter name="MPH">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("KPH") / 0.62134 
			</arg>
			<arg name="dependant" value="KPH"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="IG_TIMING_DEG">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	 return meta.getParamValue("E_TIMING") / 2.0 - 64.0 
			</arg>
			<arg name="dependant" value="E_TIMING"/>
        </args>
    </meta-parameter>	
		
	<meta-parameter name="INTAKE_AIR_TEMP_C">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_INTAKE_AIR_TEMP") - 40.0 
			</arg>
			<arg name="dependant" value="E_INTAKE_AIR_TEMP"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="INTAKE_AIR_TEMP_C">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_INTAKE_AIR_TEMP") - 40.0 
			</arg>
			<arg name="dependant" value="E_INTAKE_AIR_TEMP"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="INTAKE_AIR_TEMP_F">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
                <arg name="script">
            	 return (meta.getParamValue("INTAKE_AIR_TEMP_C") * (9.0/5.0)) + 32.0 
			</arg>
			<arg name="dependant" value="INTAKE_AIR_TEMP_C"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="E_MAF_GPS">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_MAF") / 100.0 
			</arg>
			<arg name="dependant" value="E_MAF"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="TPS">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.getParamValue("E_TPS") * 100.0 / 255.0 
			</arg>
			<arg name="dependant" value="E_TPS"/>
        </args>
    </meta-parameter>	
    
	<meta-parameter name="O2_SENSOR_PRESENT_B1_S1">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.bitcheck(meta.getParamValue("E_O2_SENSOR_PRESENT"), 0) 
			</arg>
			<arg name="dependant" value="E_O2_SENSOR_PRESENT"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="O2_SENSOR_PRESENT_B1_S2">
		<handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
		<args>
            <arg name="script">
            	 return meta.bitcheck(meta.getParamValue("E_O2_SENSOR_PRESENT"), 1) 
			</arg>
			<arg name="dependant" value="E_O2_SENSOR_PRESENT"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="LAMBDA_1">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	 return meta.bitShiftRight(meta.getParamValue("E_LAMBDA_1"),16) * 0.0000305 
			</arg>
			<arg name="dependant" value="E_LAMBDA_1"/>
        </args>
    </meta-parameter>
	
	
</parameters>
