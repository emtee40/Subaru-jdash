<!-- ======================================================
     EVERY known to us  SSM parameter
     ====================================================== -->
<parameters name="Nissan Consult Protocol" monitor-class="net.sourceforge.JDash.ecu.comm.NissanConsultMonitor">

    <description>This monitor is still very much in testing status.  We're trying to get the Nissan Consult protocol working, but I don't have a Nissan to test it on.</description>
    
    <include file="dtc.iml"/>
	
	<parameter name="E_RPM_H">
		<description>Engine Speed High Byte - Divide value by 4 to get RPM</description>
		<address>
			<byte>0x00</byte>
		</address>
	</parameter>
	<parameter name="E_RPM_L">
		<description>Engine Speed Low Byte - Divide value by 4 to get RPM</description>
		<address>
			<byte>0x01</byte>
		</address>
	</parameter>
	<parameter name="E_KPH">
		<description>Vehicle Speed in KPH</description>
		<address>
			<byte>0x0b</byte>
		</address>
	</parameter>



	
    <meta-parameter name="RPM">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return Math.round(meta.makeint(meta.getParamValue("E_RPM_H"), meta.getParamValue("E_RPM_L")) * 12.5)
            </arg>
            <arg name="dependant" value="E_RPM_H"/>
            <arg name="dependant" value="E_RPM_L"/>
        </args>
    </meta-parameter>
    
    
    <meta-parameter name="KPH">
        <handler>net.sourceforge.JDash.ecu.param.JSMetaParam</handler>
        <args>
            <arg name="script">
            	return Math.round(meta.getParamValue("E_KPH") * 2.0)
            </arg>
            <arg name="dependant" value="E_KPH"/>
        </args>
    </meta-parameter>
    
    
    
</parameters>
