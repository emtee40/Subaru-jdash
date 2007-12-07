<!-- ======================================================
     Including the standard Subaru ECU values, this file defines the addresses specific to the MY98-MY00
     ====================================================== -->
<parameters name="Subaru SSM/OBD-II">

    <description>This parameter definition will communicate with any OBD-II/SSM Subaru ECU through the Tactrix OpenPort cable using the SSM Protocol</description>

    <!-- The standard known subaru SSM addresses -->
    <include file="subaru-ssm-obd2-common.iml"/>
    <include file="dtc.iml"/>
    
</parameters>
