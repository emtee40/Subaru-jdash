<!-- ======================================================
     Including the standard Subaru ECU values, this file defines the addresses specific to the MY98-MY00
     ====================================================== -->
<parameters name="Subaru SSM/OBD-II (USDM MY04-05)">

    <description>Same as the Generic Subaru definition file, but can also read the 2004-2005 Subaru DTC Codes.</description>

    <!-- The standard known subaru SSM addresses -->
    <include file="subaru-ssm-obd2-common.iml"/>

    <!-- The standard subaru DTC conversions -->
    <include file="subaru-ssm-obd2-dtc-meta.iml"/>

<!--
Thanks to ecuexplorer for these addresses
#define ADDRESS_DTC_CURRENT_04_05_START_1				0x00000123
#define ADDRESS_DTC_CURRENT_04_05_END_1					0x0000012A
#define ADDRESS_DTC_CURRENT_04_05_START_2				0x00000150
#define ADDRESS_DTC_CURRENT_04_05_END_2					0x00000154
#define ADDRESS_DTC_CURRENT_04_05_START_3				0x00000160
#define ADDRESS_DTC_CURRENT_04_05_END_3					0x00000164
#define ADDRESS_DTC_HISTORIC_04_05_START_1				0x0000012B
#define ADDRESS_DTC_HISTORIC_04_05_END_1				0x00000132
#define ADDRESS_DTC_HISTORIC_04_05_START_2				0x00000155
#define ADDRESS_DTC_HISTORIC_04_05_END_2				0x00000159
#define ADDRESS_DTC_HISTORIC_04_05_START_3				0x00000165
#define ADDRESS_DTC_HISTORIC_04_05_END_3				0x00000169
-->
    
    
</parameters>
