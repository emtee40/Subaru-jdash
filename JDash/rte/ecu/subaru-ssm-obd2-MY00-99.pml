<!-- ======================================================
     Including the standard Subaru ECU values, this file defines the addresses specific to the MY98-MY00
     ====================================================== -->
<parameters name="Subaru SSM/OBD-II (USDM MY99-00)">

    <description>Same as the Generic Subaru definition file, but can also read the 1999-2000 Subaru DTC Codes.</description>

    <!-- The standard known subaru SSM addresses -->
    <include file="subaru-ssm-obd2-common.iml"/>


<!--
Thanks to ecuexplorer for these addresses
#define ADDRESS_DTC_CURRENT_99_00_START					0x0000008E
#define ADDRESS_DTC_CURRENT_99_00_END					0x00000097
#define ADDRESS_DTC_HISTORIC_99_00_START				0x000000A4
#define ADDRESS_DTC_HISTORIC_99_00_END					0x000000AD
-->
    
	<parameter name="E_DTC_CUR_0H" rate="10000">
		<description>Current DTC 0H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x8E</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_0L" rate="10000">
		<description>Current DTC 0L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x8F</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_1H" rate="10000">
		<description>Current DTC 1H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x90</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_1L" rate="10000">
		<description>Current DTC 1L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x91</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_2H" rate="10000">
		<description>Current DTC 2H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x92</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_2L" rate="10000">
		<description>Current DTC 2L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x93</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_3H" rate="10000">
		<description>Current DTC 3H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x94</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_3L" rate="10000">
		<description>Current DTC 3L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x95</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_4H" rate="10000">
		<description>Current DTC 4H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x96</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_CUR_4L" rate="10000">
		<description>Current DTC 4L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0x97</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_0H" rate="10000">
		<description>Current DTC 0H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA4</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_0L" rate="10000">
		<description>Current DTC 0L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA5</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_1H" rate="10000">
		<description>Current DTC 1H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA6</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_1L" rate="10000">
		<description>Current DTC 1L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA7</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_2H" rate="10000">
		<description>Current DTC 2H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA8</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_2L" rate="10000">
		<description>Current DTC 2L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xA9</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_3H" rate="10000">
		<description>Current DTC 3H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAA</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_3L" rate="10000">
		<description>Current DTC 3L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAB</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_4H" rate="10000">
		<description>Current DTC 4H</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAC</byte>
		</address>
	</parameter>
	<parameter name="E_DTC_HIST_4L" rate="10000">
		<description>Current DTC 4L</description>
		<address>
			<byte>0x00</byte>
			<byte>0x00</byte>
			<byte>0xAD</byte>
		</address>
	</parameter>
	
	
	    <meta-parameter name="DTC_0">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_CUR_0H"),meta.getParamValue("E_DTC_CUR_0L")) 
            </arg>
            <arg name="dependant" value="E_DTC_CUR_0H"/>
            <arg name="dependant" value="E_DTC_CUR_0L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_1">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_CUR_1H"),meta.getParamValue("E_DTC_CUR_1L"))  
            </arg>
            <arg name="dependant" value="E_DTC_CUR_1H"/>
            <arg name="dependant" value="E_DTC_CUR_1L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_2">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_CUR_2H"),meta.getParamValue("E_DTC_CUR_2L"))  
            </arg>
            <arg name="dependant" value="E_DTC_CUR_2H"/>
            <arg name="dependant" value="E_DTC_CUR_2L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_3">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_CUR_3H"),meta.getParamValue("E_DTC_CUR_3L"))  
            </arg>
            <arg name="dependant" value="E_DTC_CUR_3H"/>
            <arg name="dependant" value="E_DTC_CUR_3L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_4">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_CUR_4H"),meta.getParamValue("E_DTC_CUR_4L"))  
            </arg>
            <arg name="dependant" value="E_DTC_CUR_4H"/>
            <arg name="dependant" value="E_DTC_CUR_4L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_HIST_0">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_HIST_0H"),meta.getParamValue("E_DTC_HIST_0L"))  
            </arg>
            <arg name="dependant" value="E_DTC_HIST_0H"/>
            <arg name="dependant" value="E_DTC_HIST_0L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_HIST_1">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_HIST_1H"),meta.getParamValue("E_DTC_HIST_1L"))  
            </arg>
            <arg name="dependant" value="E_DTC_HIST_1H"/>
            <arg name="dependant" value="E_DTC_HIST_1L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_HIST_2">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_HIST_2H"),meta.getParamValue("E_DTC_HIST_2L"))  
            </arg>
            <arg name="dependant" value="E_DTC_HIST_2H"/>
            <arg name="dependant" value="E_DTC_HIST_2L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_HIST_3">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_HIST_3H"),meta.getParamValue("E_DTC_HIST_3L"))  
            </arg>
            <arg name="dependant" value="E_DTC_HIST_3H"/>
            <arg name="dependant" value="E_DTC_HIST_3L"/>
        </args>
    </meta-parameter>	
    
    <meta-parameter name="DTC_HIST_4">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return meta.makeint(meta.getParamValue("E_DTC_HIST_4H"),meta.getParamValue("E_DTC_HIST_4L"))s
            </arg>
            <arg name="dependant" value="E_DTC_HIST_4H"/>
            <arg name="dependant" value="E_DTC_HIST_4L"/>
        </args>
    </meta-parameter>	
	
    
</parameters>
