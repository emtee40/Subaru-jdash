<!-- ======================================================
     Including the standard Subaru ECU values, this file defines the addresses specific to the MY98-MY00
     ====================================================== -->
<parameters name="Subaru SSM/OBD-II (USDM MY01-03)">

    <description>Same as the Generic Subaru definition file, but can also read the 2001-2003 Subaru DTC Codes.</description>

    <!-- The standard known subaru SSM addresses -->
    <include file="subaru-ssm-obd2-common.iml"/>

    <!-- The standard subaru DTC conversions -->
    <include file="subaru-ssm-obd2-dtc-meta.iml"/>

<!--
Thanks to ecuexplorer for these addresses
#define ADDRESS_DTC_CURRENT_01_03_START_1				0x0000008E
#define ADDRESS_DTC_CURRENT_01_03_END_1					0x000000AD
#define ADDRESS_DTC_CURRENT_01_03_START_2				0x000000F0
#define ADDRESS_DTC_CURRENT_01_03_END_2					0x000000F3
#define ADDRESS_DTC_HISTORIC_01_03_START_1				0x000000AE
#define ADDRESS_DTC_HISTORIC_01_03_END_1				0x000000CD
#define ADDRESS_DTC_HISTORIC_01_03_START_2				0x000000F4
#define ADDRESS_DTC_HISTORIC_01_03_END_2				0x000000F7
-->
    
    
</parameters>
