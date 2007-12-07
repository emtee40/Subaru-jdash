<!-- ======================================================
     EVERY known to us  SSM parameter
     ====================================================== -->
<parameters name="Subaru SSM/OBD-1 (A30113)" monitor-class="net.sourceforge.JDash.ecu.comm.SSMOBD1Monitor">

    <description>This parameter include file contains the known set of SSM parameter addresses on an OBD-I subaru with a ROM-ID of A30113 such as the 97/98 EDM Impreza GT. 
However, this monitor is still not working as I don't have easy access to an OBD-I subaru.</description>

	<include file="dtc.iml"/>
	
	<parameter name="E_BATTERY_VOLTS">
		<description>Battery Voltage - N * 0.08</description>
		<address>
			<byte>0x00</byte>
			<byte>0x07</byte>
		</address>
	</parameter>
	
	<parameter name="E_KPH">
		<description>Vehicle Speed - N * 2</description>
		<address>
			<byte>0x00</byte>
			<byte>0x08</byte>
		</address>
	</parameter>
	
	<parameter name="E_RPM">
		<description>Engine Speed - N * 25</description>
		<address>
			<byte>0x00</byte>
			<byte>0x09</byte>
		</address>
	</parameter>
	
	<parameter name="E_COOLANT_TEMP" rate="5000">
		<description>Coolant Temp Subtract 50 to get C - Multiply by (9/5) and add 32to get Fahrenheit</description>
		<address>
			<byte>0x00</byte>
			<byte>0x0A</byte>
		</address>
	</parameter>
	
	<parameter name="IG_ADVANCE">
		<description>Ignition Advance</description>
		<address>
			<byte>0x00</byte>
			<byte>0x0B</byte>
		</address>
	</parameter>
	
	<parameter name="E_MAF">
		<description>Air Flow Sensor - N * 5 / 256</description>
		<address>
			<byte>0x00</byte>
			<byte>0x0C</byte>
		</address>
	</parameter>
	
	<parameter name="LOAD">
		<description>Engine Load - Multiply value by 100.0 and divide by 255 to get percent</description>
		<address>
			<byte>0x00</byte>
			<byte>0x0D</byte>
		</address>
	</parameter>

	<parameter name="E_TPS">
		<description>Throttle Opening Angle - N * 5 / 256</description>
		<address>
			<byte>0x00</byte>
			<byte>0x0f</byte>
		</address>
	</parameter>	
	
	<parameter name="E_INJ_1_PULSE">
		<description>Fuel Injection #1 Pulse Width - N * 256 / 1000</description>
		<address>
			<byte>0x00</byte>
			<byte>0x10</byte>
		</address>
	</parameter>
	
	<parameter name="E_ISU_DUTY_VALVE">
		<description>The ISU Duty Valve - N * 100 / 256</description>
		<address>
			<byte>0x00</byte>
			<byte>0x11</byte>
		</address>
	</parameter>
	
	<parameter name="E_O2_AVERAGE">
		<description>The O2 Average - N * 5000 / 512</description>
		<address>
			<byte>0x00</byte>
			<byte>0x12</byte>
		</address>
	</parameter>
	
	<parameter name="E_O2_MINIMUM">
		<description>The O2 Minimum - N * 5000 / 256</description>
		<address>
			<byte>0x00</byte>
			<byte>0x13</byte>
		</address>
	</parameter>
	
	<parameter name="E_O2_MAXIMUM">
		<description>The O2 Maximum - N * 5000 / 256</description>
		<address>
			<byte>0x00</byte>
			<byte>0x14</byte>
		</address>
	</parameter>
	
	<parameter name="KNOCK_COR">
		<description>Knock Correction - Subtract 128 from value and divide by 2 to get degrees</description>
		<address>
			<byte>0x00</byte>
			<byte>0x15</byte>
		</address>
	</parameter>
	
	<parameter name="E_AF_COR_1">
		<description>Air/Fuel Correction #1 - N - 128</description>
		<address>
			<byte>0x00</byte>
			<byte>0x1c</byte>
		</address>
	</parameter>
	
	<parameter name="E_ATMO">
		<description>Atmospheric Pressure - N * 8</description>
		<address>
			<byte>0x00</byte>
			<byte>0x1F</byte>
		</address>
	</parameter>
	
	<parameter name="E_VAC_BOOST">
		<description>Manifold Relative Pressure - ( N - 128 ) / 85</description>
		<address>
			<byte>0x00</byte>
			<byte>0x20</byte>
		</address>
	</parameter>
	
	<parameter name="E_BOOST_SOL_DC">
		<description>Boost Solenoid Duty Cycle - N * 100 / 256</description>
		<address>
			<byte>0x00</byte>
			<byte>0x22</byte>
		</address>
	</parameter>
	
	
	
	<!-- The MIL Status is not yet supported by our SSM implementation. We don't know the memory location -->
	<meta-parameter name="MIL_STATUS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script" value=" return 0.0 " />
        </args>
    </meta-parameter>
	
	
	<meta-parameter name="BATTERY_VOLTS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_BATTERY_VOLTS") * 0.08 
            </arg>
            <arg name="dependant" value="E_BATTERY_VOLTS"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="KPH">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_KPH") * 2.0  
            </arg>
            <arg name="dependant" value="E_KPH"/>
        </args>
    </meta-parameter>
    
    <meta-parameter name="MPH">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("KPH") * 0.6215 
            </arg>
            <arg name="dependant" value="KPH"/>
        </args>
    </meta-parameter>
	
	<meta-parameter name="RPM">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_RPM") * 25.0  
            </arg>
            <arg name="dependant" value="E_RPM"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="COOLANT_TEMP_C">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_COOLANT_TEMP") - 50.0  
            </arg>
            <arg name="dependant" value="E_COOLANT_TEMP"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="MAF_VOLTS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_MAF") * 0.08  
            </arg>
            <arg name="dependant" value="E_MAF"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="TPS_VOLTS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_TPS") * 5.0 / 256.0  
            </arg>
            <arg name="dependant" value="E_TPS"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="INJ_1_PULS_MS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_INJ_1_PULSE") * 256.0 / 1000.0  
            </arg>
            <arg name="dependant" value="E_INJ_1_PULSE"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="ISU_DUTY_VALVE_DC">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_ISU_DUTY_VALVE") * 100.0 / 256.0  
            </arg>
            <arg name="dependant" value="E_ISU_DUTY_VALVE"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="O2_AVERAGE_VOLTS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_O2_AVERAGE") * 5000.0 / 512.0 * 1000  
            </arg>
            <arg name="dependant" value="E_O2_AVERAGE"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="O2_MINIMUM_VOLTS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_O2_MINIMUM") * 5000.0 / 256.0 * 1000  
            </arg>
            <arg name="dependant" value="E_O2_MINIMUM"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="O2_MAXIMUM_VOLTS">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_O2_MAXIMUM") * 5000.0 / 256.0 * 1000  
            </arg>
            <arg name="dependant" value="E_O2_MAXIMUM"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="AF_COR_1_PERCENT">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_AF_COR_1") - 128.0  
            </arg>
            <arg name="dependant" value="E_AF_COR_1"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="ATMO_MMHG">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_ATMO") * 8.0  
            </arg>
            <arg name="dependant" value="E_ATMO"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="ATMO_PSI">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("ATMO_MMHG") * 0.01933677  
            </arg>
            <arg name="dependant" value="ATMO_MMHG"/>
        </args>
    </meta-parameter>
    
	<meta-parameter name="VAC_BOOST_BAR">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return (meta.getParamValue("E_VAC_BOOST") - 128.0) / 85.0  
            </arg>
            <arg name="dependant" value="E_VAC_BOOST"/>
        </args>
    </meta-parameter>
    
    <meta-parameter name="VAC_BOOST_PSI">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("VAC_BOOST_BAR") *  14.5037737730209  
            </arg>
            <arg name="dependant" value="VAC_BOOST_BAR"/>
        </args>
    </meta-parameter>
    
	BoostSolenoidDutyCycleAddress
	<meta-parameter name="BOOST_SOL_DC">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.getParamValue("E_BOOST_SOL_DC") * 100.0 / 256.0  
            </arg>
            <arg name="dependant" value="E_BOOST_SOL_DC"/>
        </args>
    </meta-parameter>
    
</parameters>
