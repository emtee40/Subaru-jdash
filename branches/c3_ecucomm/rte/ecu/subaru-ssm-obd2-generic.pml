<!-- ======================================================
     Including the standard Subaru ECU values, this file defines the addresses specific to the MY98-MY00
     ====================================================== -->
<parameters name="Subaru SSM/OBD-II generic">

    <description>This parameter definition will communicate with any OBD-II/SSM Subaru ECU through the Tactrix OpenPort cable using the SSM Protocol.  This monitor does not retreive DTC codes.</description>

    <!-- The standard known subaru SSM addresses -->
    <include file="subaru-ssm-obd2-common.iml"/>
    
</parameters>
